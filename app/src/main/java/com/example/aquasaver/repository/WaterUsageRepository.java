package com.example.aquasaver.repository;

import android.util.Log;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.*;
import com.example.aquasaver.model.*;

import java.util.*;

public class WaterUsageRepository {
    private final CollectionReference ref = FirebaseFirestore.getInstance().collection("water_usage");

    public void insertLog(WaterUsage waterUsed) {
        ref.add(waterUsed);
    }

    public void insertAllLogs(List<WaterUsage> logs) {
        for (WaterUsage log : logs) ref.add(log);
    }

    public void updateLog(String docId, WaterUsage waterUsed) {
        ref.document(docId).set(waterUsed);
    }

    public void deleteLog(String docId) {
        ref.document(docId).delete();
    }

    public void deleteLogsForUser(String userEmail) {
        ref.whereEqualTo("userEmail", userEmail).get().addOnSuccessListener(q -> {
            for (DocumentSnapshot doc : q.getDocuments()) doc.getReference().delete();
        });
    }

    public void deleteLogsOlderThan(long dateTimestamp) {
        ref.whereLessThan("date", dateTimestamp).get().addOnSuccessListener(q -> {
            for (DocumentSnapshot doc : q.getDocuments()) doc.getReference().delete();
        });
    }

    public void getLogById(String docId, OnSuccessListener<DocumentSnapshot> listener) {
        ref.document(docId).get().addOnSuccessListener(listener);
    }

    public void getLogsForUser(String userEmail, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("userEmail", userEmail).orderBy("date", Query.Direction.DESCENDING).get().addOnSuccessListener(listener);
    }

    public void getLogsForUserAndDate(String userEmail, long dateTimestamp, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("userEmail", userEmail).whereEqualTo("date", dateTimestamp).orderBy("id", Query.Direction.DESCENDING).get().addOnSuccessListener(listener);
    }

    public void getLatestLogForUserAndDate(String userEmail, long dateTimestamp, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("userEmail", userEmail).whereEqualTo("date", dateTimestamp).orderBy("id", Query.Direction.DESCENDING).limit(1).get().addOnSuccessListener(listener);
    }

    public void getLogsForUserInDateRange(String userEmail, long start, long end, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("userEmail", userEmail).whereGreaterThanOrEqualTo("date", start).whereLessThanOrEqualTo("date", end).orderBy("date", Query.Direction.DESCENDING).get().addOnSuccessListener(listener);
    }

    public void getLatestLogForUser(String userEmail, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("userEmail", userEmail).orderBy("date", Query.Direction.DESCENDING).limit(1).get().addOnSuccessListener(listener);
    }

    public void getDailyUsageBetween(String email, long start, long end, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("userEmail", email)
                .whereGreaterThanOrEqualTo("date", start)
                .whereLessThanOrEqualTo("date", end)
                .get().addOnSuccessListener(listener);
    }

    public void getDailyUsageForDay(String email, long dayTimestamp, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("userEmail", email)
                .whereEqualTo("date", dayTimestamp)
                .get().addOnSuccessListener(listener);
    }

    public void getAllDailyUsageForUser(String email, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("userEmail", email)
                .get().addOnSuccessListener(listener);
    }

    public void getLitersUsedToday(String email, long startOfDay, long endOfDay, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("userEmail", email)
                .whereGreaterThanOrEqualTo("date", startOfDay)
                .whereLessThanOrEqualTo("date", endOfDay)
                .get().addOnSuccessListener(listener);
    }

    public void getLitersUsedThisWeek(String email, long startOfWeek, long endOfWeek, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("userEmail", email)
                .whereGreaterThanOrEqualTo("date", startOfWeek)
                .whereLessThanOrEqualTo("date", endOfWeek)
                .get().addOnSuccessListener(listener);
    }

    public void getLitersUsedBetween(String email, long start, long end, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("userEmail", email)
                .whereGreaterThanOrEqualTo("date", start)
                .whereLessThanOrEqualTo("date", end)
                .get().addOnSuccessListener(listener);
    }
}