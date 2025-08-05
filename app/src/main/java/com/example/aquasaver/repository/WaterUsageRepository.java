package com.example.aquasaver.repository;

import android.util.Log;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.*;
import com.example.aquasaver.model.*;

import java.util.*;

public class WaterUsageRepository {
    private final CollectionReference ref = FirebaseFirestore.getInstance().collection("water_usage_log");

    public void insert(WaterUsage usage) { ref.add(usage); }

    public void delete(String docId) { ref.document(docId).delete(); }

    public void getByUser(String email, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("userEmail", email)
                .orderBy("usageDate", Query.Direction.DESCENDING)
                .get().addOnSuccessListener(listener);
    }

    public void getByUserAndDate(String email, Date date, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("userEmail", email)
                .whereEqualTo("usageDate", date)
                .orderBy("id", Query.Direction.DESCENDING)
                .get().addOnSuccessListener(listener);
    }

    public void getByDateRange(String email, Date start, Date end, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("userEmail", email)
                .whereGreaterThanOrEqualTo("usageDate", start)
                .whereLessThanOrEqualTo("usageDate", end)
                .orderBy("usageDate", Query.Direction.DESCENDING)
                .get().addOnSuccessListener(listener);
    }
}
