package com.example.aquasaver.repository;

import android.util.Log;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.*;
import com.example.aquasaver.model.*;

import java.util.*;

public class ReportsRepository {
    private final CollectionReference ref = FirebaseFirestore.getInstance().collection("reports");

    // insert one water report
    public void insertReport(Reports report) {
        ref.add(report);
    }

    // insert many water reports
    public void insertAllReports(List<Reports> reportList) {
        for (Reports report : reportList) {
            ref.add(report);
        }
    }

    // updates a specific report
    public void updateReport(String docId, Reports report) {
        ref.document(docId).set(report);
    }

    // deletes a specific report
    public void deleteReport(String docId) {
        ref.document(docId).delete();
    }

    // deletes all user reports and return # rows deleted
    public void deleteUserReports(String userEmail, OnSuccessListener<Integer> listener) {
        ref.whereEqualTo("userEmail", userEmail).get()
                .addOnSuccessListener(snapshot -> {
                    WriteBatch batch = FirebaseFirestore.getInstance().batch();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        batch.delete(doc.getReference());
                    }
                    batch.commit().addOnSuccessListener(unused -> listener.onSuccess(snapshot.size()));
                });
    }

    // Returns a specific report by Id
    public void getReportById(String reportId, OnSuccessListener<DocumentSnapshot> listener) {
        ref.document(reportId).get().addOnSuccessListener(listener);
    }

    // Returns all reports of a particular user
    public void getUserReports(String userEmail, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("userEmail", userEmail)
                .orderBy("startDate", Query.Direction.DESCENDING)
                .get().addOnSuccessListener(listener);
    }

    // Returns all reports of a particular user at a specific time
    public void getUserReportsByTime(String userEmail, long currentDate, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("userEmail", userEmail)
                .whereLessThanOrEqualTo("startDate", currentDate)
                .whereGreaterThanOrEqualTo("endDate", currentDate)
                .get().addOnSuccessListener(listener);
    }
}