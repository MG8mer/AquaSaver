package com.example.aquasaver.repository;

import android.util.Log;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.*;
import com.example.aquasaver.model.*;

import java.util.*;

public class ChallengeProgressRepository {
    private final CollectionReference ref = FirebaseFirestore.getInstance().collection("challenge_progress");

    public void insertChallengeProgress(ChallengeProgress challengeProgress) {
        ref.add(challengeProgress);
    }

    public void insertAllChallengeProgress(List<ChallengeProgress> challengeProgressList) {
        for (ChallengeProgress progress : challengeProgressList) {
            ref.add(progress);
        }
    }

    public void updateChallengeProgress(String docId, ChallengeProgress challengeProgress) {
        ref.document(docId).set(challengeProgress);
    }

    public void deleteChallengeProgress(String docId) {
        ref.document(docId).delete();
    }

    public void deleteChallengeProgress(String userEmail, OnSuccessListener<Void> listener) {
        ref.whereEqualTo("userEmail", userEmail).get()
                .addOnSuccessListener(query -> {
                    WriteBatch batch = FirebaseFirestore.getInstance().batch();
                    for (DocumentSnapshot doc : query) {
                        batch.delete(doc.getReference());
                    }
                    batch.commit().addOnSuccessListener(listener);
                });
    }

    public void getChallengeProgressById(String title, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("title", title).limit(1).get().addOnSuccessListener(listener);
    }

    public void getChallengeProgressById(String title, String email, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("title", title).whereEqualTo("userEmail", email).get().addOnSuccessListener(listener);
    }

    public void getChallengeProgressByUserEmailAndTitle(String userEmail, String title, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("userEmail", userEmail).whereEqualTo("title", title).limit(1).get().addOnSuccessListener(listener);
    }

    public void getUserChallengeProgress(String userEmail, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("userEmail", userEmail).get().addOnSuccessListener(listener);
    }

    public void getUserChallengeProgressByCompletion(String userEmail, boolean completion, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("userEmail", userEmail).whereEqualTo("completion", completion).get().addOnSuccessListener(listener);
    }

    public void getChallengeByTitle(String title, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("title", title).limit(1).get().addOnSuccessListener(listener);
    }

    public void getTodayChallengeProgress(String email, Date today, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("userEmail", email).whereEqualTo("timestamp", today).limit(1).get().addOnSuccessListener(listener);
    }

    public void getCompletedChallengesByUser(String email, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("userEmail", email).whereEqualTo("completion", true).get().addOnSuccessListener(listener);
    }
}