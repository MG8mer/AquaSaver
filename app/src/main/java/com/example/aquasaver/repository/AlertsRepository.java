
package com.example.aquasaver.repository;

import android.util.Log;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.*;
import com.example.aquasaver.model.*;

import java.util.*;

public class AlertsRepository {
    private final CollectionReference ref = FirebaseFirestore.getInstance().collection("alerts");

    public void insertAlert(Alerts a) { ref.add(a); }

    public void insertAllAlerts(List<Alerts> list) {
        for (Alerts a : list) ref.add(a);
    }

    public void updateAlert(String docId, Alerts a) { ref.document(docId).set(a); }

    public void deleteAlert(String docId) { ref.document(docId).delete(); }

    public void deleteAlertsForLocation(String location) {
        ref.whereEqualTo("location", location)
                .get().addOnSuccessListener(snapshot -> {
                    for (DocumentSnapshot doc : snapshot) {
                        doc.getReference().delete();
                    }
                });
    }

    public void deleteAlertsByType(String alertType) {
        ref.whereEqualTo("alertType", alertType)
                .get().addOnSuccessListener(snapshot -> {
                    for (DocumentSnapshot doc : snapshot) {
                        doc.getReference().delete();
                    }
                });
    }

    public void deleteAlertsByLocationAndType(String location, String alertType) {
        ref.whereEqualTo("location", location)
                .whereEqualTo("alertType", alertType)
                .get().addOnSuccessListener(snapshot -> {
                    for (DocumentSnapshot doc : snapshot) {
                        doc.getReference().delete();
                    }
                });
    }

    public void getAllAlerts(OnSuccessListener<QuerySnapshot> listener) {
        ref.get().addOnSuccessListener(listener);
    }

    public void getAlertById(String docId, OnSuccessListener<DocumentSnapshot> listener) {
        ref.document(docId).get().addOnSuccessListener(listener);
    }

    public void getAlertsByLocation(String location, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("location", location).get().addOnSuccessListener(listener);
    }

    public void getActiveAlertsByLocation(String location, long currentTime, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("location", location)
                .whereLessThanOrEqualTo("startTime", currentTime)
                .whereGreaterThanOrEqualTo("endTime", currentTime)
                .get().addOnSuccessListener(listener);
    }

    public void getAlertsByType(String alertType, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("alertType", alertType).get().addOnSuccessListener(listener);
    }

    public void getActiveAlertsByType(String alertType, long currentTime, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("alertType", alertType)
                .whereLessThanOrEqualTo("startTime", currentTime)
                .whereGreaterThanOrEqualTo("endTime", currentTime)
                .get().addOnSuccessListener(listener);
    }

    public void getAlertsByLocationAndType(String location, String alertType, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("location", location)
                .whereEqualTo("alertType", alertType)
                .get().addOnSuccessListener(listener);
    }

    public void getActiveAlertsByLocationAndType(String location, String alertType, long currentTime, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("location", location)
                .whereEqualTo("alertType", alertType)
                .whereLessThanOrEqualTo("startTime", currentTime)
                .whereGreaterThanOrEqualTo("endTime", currentTime)
                .get().addOnSuccessListener(listener);
    }
}
