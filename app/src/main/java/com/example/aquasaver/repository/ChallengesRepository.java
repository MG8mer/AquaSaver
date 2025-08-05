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
}