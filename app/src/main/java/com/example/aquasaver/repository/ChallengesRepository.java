package com.example.aquasaver.repository;


import android.util.Log;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.*;
import com.example.aquasaver.model.*;

import java.util.*;

public class ChallengesRepository {
    private final CollectionReference ref = FirebaseFirestore.getInstance().collection("challenges");

    public void insertChallenge(Challenges c) { ref.add(c); }

    public void insertAllChallenges(List<Challenges> list) {
        for (Challenges c : list) ref.add(c);
    }

    public void updateChallenge(String docId, Challenges c) { ref.document(docId).set(c); }

    public void deleteChallenge(String docId) { ref.document(docId).delete(); }

    public void deleteUserChallenges(String userEmail) {
        ref.whereEqualTo("userEmail", userEmail).get().addOnSuccessListener(snapshot -> {
            for (DocumentSnapshot doc : snapshot) doc.getReference().delete();
        });
    }

    public void getChallengeById(String docId, OnSuccessListener<DocumentSnapshot> listener) {
        ref.document(docId).get().addOnSuccessListener(listener);
    }

    public void getUserChallenges(String email, OnChallengesLoaded callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("challenges")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Challenges> list = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Challenges c = doc.toObject(Challenges.class);
                        list.add(c);
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    public interface OnChallengesLoaded {
        void onSuccess(List<Challenges> challenges);
        void onFailure(Exception e);
    }

    public void getUserChallengesByTime(String userEmail, long currentDate, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("userEmail", userEmail)
                .whereLessThanOrEqualTo("startDate", currentDate)
                .whereGreaterThanOrEqualTo("endDate", currentDate)
                .get().addOnSuccessListener(listener);
    }

    public void getAllChallengesWithProgress(String userEmail, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("userEmail", userEmail).get().addOnSuccessListener(listener); // Join with ChallengeProgress client-side
    }

    public void countChallengesForUser(String email, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("userEmail", email).get().addOnSuccessListener(listener); // Size = count
    }

    public void getChallengeByTitle(String title, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("title", title).limit(1).get().addOnSuccessListener(listener);
    }

    public void getOneDayChallengesBeforeDate(String userEmail, long currentDate, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("userEmail", userEmail)
                .whereEqualTo("goalAmount", 1)
                .whereLessThanOrEqualTo("endDate", currentDate)
                .get().addOnSuccessListener(listener);
    }

    public void getWeeklyChallengesBeforeDate(String userEmail, long currentDate, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("userEmail", userEmail)
                .whereGreaterThan("goalAmount", 1)
                .whereLessThanOrEqualTo("goalAmount", 7)
                .whereLessThanOrEqualTo("endDate", currentDate)
                .get().addOnSuccessListener(listener);
    }

    public void getMonthlyChallengesBeforeDate(String userEmail, long currentDate, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("userEmail", userEmail)
                .whereGreaterThan("goalAmount", 7)
                .whereLessThanOrEqualTo("goalAmount", 28)
                .whereLessThanOrEqualTo("endDate", currentDate)
                .get().addOnSuccessListener(listener);
    }
}
