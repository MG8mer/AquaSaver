package com.example.aquasaver.ui.eco;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.aquasaver.R;
import com.example.aquasaver.model.ChallengeProgress;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import com.example.aquasaver.model.Challenges;


public class EcoChallengesFragment extends Fragment {

    private TextView challengeTitleText;
    private TextView challengeDescriptionText;
    private Button completeChallengeButton;
    private TextView challengesCompletedText;
    private TextView noChallengesMessage;

    private FirebaseFirestore db;
    private List<Challenges> allChallenges = new ArrayList<>();
    private Challenges currentChallenge;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_eco_challenges, container, false);

        challengeTitleText = view.findViewById(R.id.challengeTitleText);
        challengeDescriptionText = view.findViewById(R.id.challengeDescriptionText);
        completeChallengeButton = view.findViewById(R.id.completeChallengeButton);
        challengesCompletedText = view.findViewById(R.id.challengesCompletedText);
        noChallengesMessage = view.findViewById(R.id.noChallengesMessage);

        db = FirebaseFirestore.getInstance();

        loadOneRandomChallenge();
        setupCompleteChallengeButton();
        updateChallengesCompletedCount();

        return view;
    }

    private void loadOneRandomChallenge() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String userEmail = user.getEmail();

        db.collection("challenges").get().addOnSuccessListener(challengeSnapshot -> {
            List<Challenges> tempChallenges = new ArrayList<>();
            List<Task<DocumentSnapshot>> progressTasks = new ArrayList<>();

            for (DocumentSnapshot doc : challengeSnapshot) {
                Challenges challenge = doc.toObject(Challenges.class);
                tempChallenges.add(challenge);

                // Prepare to fetch corresponding ChallengeProgress for this challenge
                Task<DocumentSnapshot> progressTask = db.collection("challengeProgress")
                        .document(userEmail + "_" + challenge.getTitle()) // Assuming this ID format
                        .get();
                progressTasks.add(progressTask);
            }

            Tasks.whenAllSuccess(progressTasks).addOnSuccessListener(results -> {
                allChallenges.clear();

                for (int i = 0; i < results.size(); i++) {
                    DocumentSnapshot progressDoc = (DocumentSnapshot) results.get(i);
                    Challenges challenge = tempChallenges.get(i);

                    boolean isCompleted = false;
                    if (progressDoc.exists()) {
                        ChallengeProgress progress = progressDoc.toObject(ChallengeProgress.class);
                        isCompleted = progress != null && progress.isCompletion();
                    }

                    if (!isCompleted) {
                        allChallenges.add(challenge);
                    }
                }

                if (allChallenges.isEmpty()) {
                    showNoChallengesMessage();
                } else {
                    Random random = new Random();
                    currentChallenge = allChallenges.get(random.nextInt(allChallenges.size()));
                    displayChallenge(currentChallenge);
                }

            }).addOnFailureListener(e -> {
                showNoChallengesMessage();
            });

        }).addOnFailureListener(e -> {
            showNoChallengesMessage();
        });
    }

    private void displayChallenge(Challenges challenge) {
        challengeTitleText.setText(challenge.getTitle());
        challengeDescriptionText.setText(challenge.getDescription());
        noChallengesMessage.setVisibility(View.GONE);
        completeChallengeButton.setEnabled(true);
    }

    private void setupCompleteChallengeButton() {
        completeChallengeButton.setOnClickListener(v -> {
            if (currentChallenge == null) {
                Toast.makeText(getContext(), "No challenge selected", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            String userEmail = user.getEmail();
            String docId = userEmail + "_" + currentChallenge.getTitle();

            ChallengeProgress progress = new ChallengeProgress(
                    userEmail,
                    currentChallenge.getTitle(),
                    1f, // or appropriate progress value
                    true, // completion = true
                    new Date()
            );

            db.collection("challengeProgress").document(docId)
                    .set(progress)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Challenge completed!", Toast.LENGTH_SHORT).show();
                        loadOneRandomChallenge();
                        updateChallengesCompletedCount();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to update challenge progress", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void updateChallengesCompletedCount() {
        db.collection("challenges").whereEqualTo("completed", true).get()
                .addOnSuccessListener(querySnapshot -> {
                    int completedCount = querySnapshot.size();
                    challengesCompletedText.setText("Completed Challenges: " + completedCount);
                });
    }

    private void showNoChallengesMessage() {
        challengeTitleText.setText("");
        challengeDescriptionText.setText("");
        completeChallengeButton.setEnabled(false);
        noChallengesMessage.setVisibility(View.VISIBLE);
        noChallengesMessage.setText("No challenges available right now. Check back later!");
    }
}
