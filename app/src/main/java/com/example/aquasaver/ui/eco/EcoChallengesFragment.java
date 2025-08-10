package com.example.aquasaver.ui.eco;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aquasaver.R;
import com.example.aquasaver.model.Challenges;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EcoChallengesFragment extends Fragment {

    private RecyclerView recyclerView;
    private ChallengesAdapter adapter;
    private List<Challenges> allChallenges = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_eco_challenges, container, false);

        db = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.challengesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ChallengesAdapter(allChallenges);
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.buttonViewConservationTips).setOnClickListener(v -> {
            // navigate to conservation tips screen
        });
        view.findViewById(R.id.buttonViewHome).setOnClickListener(v -> {
            // navigate back home
        });

        loadChallenges();

        return view;
    }

    private void loadChallenges() {
        db.collection("challenges")
                .whereEqualTo("completed", false)
                .get()
                .addOnSuccessListener(snapshot -> {
                    allChallenges.clear();
                    for (var doc : snapshot.getDocuments()) {
                        Challenges c = doc.toObject(Challenges.class);
                        if (c != null) allChallenges.add(c);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // show error or empty state
                });
    }
}
