
package com.example.aquasaver.repository;

import android.util.Log;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.*;
import com.example.aquasaver.model.*;

import java.util.*;

public class AlertsRepository {
    private final CollectionReference ref = FirebaseFirestore.getInstance().collection("alerts");

    public void insert(Alerts a) { ref.add(a); }

    public void insertAll(List<Alerts> list) {
        for (Alerts a : list) ref.add(a);
    }

    public void update(String docId, Alerts a) { ref.document(docId).set(a); }

    public void delete(String docId) { ref.document(docId).delete(); }

    public void getAll(OnSuccessListener<QuerySnapshot> listener) {
        ref.get().addOnSuccessListener(listener);
    }

    public void getByLocation(String location, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("location", location).get().addOnSuccessListener(listener);
    }

    public void getActiveByLocation(String location, long currentTime, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("location", location)
                .whereLessThanOrEqualTo("startTime", currentTime)
                .whereGreaterThanOrEqualTo("endTime", currentTime)
                .get().addOnSuccessListener(listener);
    }
}