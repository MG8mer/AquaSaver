package com.example.aquasaver.repository;


import android.util.Log;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.*;
import com.example.aquasaver.model.*;

import java.util.*;

public class ChallengesRepository {
    private final CollectionReference ref = FirebaseFirestore.getInstance().collection("challenges");

    public void insert(Challenges c) { ref.add(c); }

    public void insertAll(List<Challenges> list) {
        for (Challenges c : list) ref.add(c);
    }

    public void update(String docId, Challenges c) { ref.document(docId).set(c); }

    public void delete(String docId) { ref.document(docId).delete(); }

    public void getByUser(String userEmail, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("userEmail", userEmail).get().addOnSuccessListener(listener);
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


}
