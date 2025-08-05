package com.example.aquasaver.repository;

import android.util.Log;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.*;
import com.example.aquasaver.model.*;

import java.util.*;

public class ReportsRepository {
    private final CollectionReference ref = FirebaseFirestore.getInstance().collection("reports");

    public void insert(Reports r) { ref.add(r); }

    public void insertAll(List<Reports> list) {
        for (Reports r : list) ref.add(r);
    }

    public void update(String docId, Reports r) { ref.document(docId).set(r); }

    public void delete(String docId) { ref.document(docId).delete(); }

    public void getByUser(String email, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("userEmail", email).get().addOnSuccessListener(listener);
    }
}