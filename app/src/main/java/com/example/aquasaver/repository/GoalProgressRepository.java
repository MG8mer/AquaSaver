package com.example.aquasaver.repository;

import android.util.Log;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.*;
import com.example.aquasaver.model.*;

import java.util.*;

public class GoalProgressRepository {
    private final CollectionReference ref = FirebaseFirestore.getInstance().collection("goal_progress");

    public void insertGoalProgress(GoalProgress p) { ref.add(p); }

    public void updateGoalProgress(String docId, GoalProgress p) { ref.document(docId).set(p); }

    public void deleteGoalProgress(String docId) { ref.document(docId).delete(); }

    public void deleteAllProgressForUser(String userEmail) {
        ref.whereEqualTo("userEmail", userEmail)
                .get().addOnSuccessListener(snapshot -> {
                    for (DocumentSnapshot doc : snapshot) {
                        doc.getReference().delete();
                    }
                });
    }

    public void getAllProgressForUser(String userEmail, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("userEmail", userEmail)
                .orderBy("progressDate", Query.Direction.DESCENDING)
                .get().addOnSuccessListener(listener);
    }

    public void getProgressForUserInDateRange(String userEmail, Date start, Date end, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("userEmail", userEmail)
                .whereGreaterThanOrEqualTo("progressDate", start)
                .whereLessThanOrEqualTo("progressDate", end)
                .orderBy("progressDate", Query.Direction.DESCENDING)
                .get().addOnSuccessListener(listener);
    }

    public void getUserGoalProgressByCompletion(String userEmail, boolean onTarget, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("userEmail", userEmail)
                .whereEqualTo("onTarget", onTarget)
                .get().addOnSuccessListener(listener);
    }

    public void getTodayProgress(String userEmail, Date today, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("userEmail", userEmail)
                .whereEqualTo("progressDate", today)
                .limit(1)
                .get().addOnSuccessListener(listener);
    }

    public void getWeeklyProgress(String userEmail, Date startOfWeek, Date endOfWeek, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("userEmail", userEmail)
                .whereGreaterThanOrEqualTo("progressDate", startOfWeek)
                .whereLessThan("progressDate", endOfWeek)
                .limit(1)
                .get().addOnSuccessListener(listener);
    }

    public void getMonthlyProgress(String userEmail, Date startOfMonth, Date endOfMonth, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("userEmail", userEmail)
                .whereGreaterThanOrEqualTo("progressDate", startOfMonth)
                .whereLessThan("progressDate", endOfMonth)
                .limit(1)
                .get().addOnSuccessListener(listener);
    }
}
