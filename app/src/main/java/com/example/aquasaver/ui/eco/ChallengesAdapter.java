package com.example.aquasaver.ui.eco;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aquasaver.R;
import com.example.aquasaver.model.Challenges;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;


public class ChallengesAdapter extends RecyclerView.Adapter<ChallengesAdapter.ViewHolder> {

    private List<Challenges> challenges;

    public ChallengesAdapter(List<Challenges> challenges) {
        this.challenges = challenges;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_challenge, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Challenges challenge = challenges.get(position);
        holder.title.setText(challenge.getTitle());
        holder.description.setText(challenge.getDescription());

        holder.completeBtn.setOnClickListener(v -> {
            FirebaseFirestore.getInstance()
                    .collection("challengeProgress")
                    .document(challenge.getTitle())
                    .update("completion", true)
                    .addOnSuccessListener(aVoid -> {
                        challenges.remove(position);
                        notifyItemRemoved(position);
                    });
        });
    }

    @Override
    public int getItemCount() {
        return challenges.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, description;
        Button completeBtn;

        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.challengeTitle);
            description = itemView.findViewById(R.id.challengeDescription);
            completeBtn = itemView.findViewById(R.id.buttonCompleteChallenge);
        }
    }
}
