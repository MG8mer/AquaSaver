package com.example.aquasaver.repository;

import android.util.Log;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.*;
import com.example.aquasaver.model.*;

import java.util.*;

public class ChallengeProgressRepository {
    private final CollectionReference ref = FirebaseFirestore.getInstance().collection("challenge_progress");

    public void insert(ChallengeProgress p) { ref.add(p); }

    public void insertAll(List<ChallengeProgress> list) {
        for (ChallengeProgress p : list) ref.add(p);
    }

    public void update(String docId, ChallengeProgress p) { ref.document(docId).set(p); }

    public void delete(String docId) { ref.document(docId).delete(); }

    public void getByUser(String email, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("userEmail", email).get().addOnSuccessListener(listener);
    }
}