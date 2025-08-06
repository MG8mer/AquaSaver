package com.example.aquasaver.repository;

import android.util.Log;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.*;
import com.example.aquasaver.model.*;

import java.util.*;

public class WeatherSuggestionsRepository {
    private final CollectionReference ref = FirebaseFirestore.getInstance().collection("weather_suggestions");

    public void insertSuggestion(WeatherSuggestions suggestion) {
        ref.add(suggestion);
    }

    public void insertAllSuggestions(List<WeatherSuggestions> suggestions) {
        for (WeatherSuggestions s : suggestions) ref.add(s);
    }

    public void updateSuggestion(String docId, WeatherSuggestions suggestion) {
        ref.document(docId).set(suggestion);
    }

    public void deleteSuggestion(String docId) {
        ref.document(docId).delete();
    }

    public void deleteSuggestionsForUser(String userEmail) {
        ref.whereEqualTo("userEmail", userEmail).get()
                .addOnSuccessListener(q -> {
                    for (DocumentSnapshot doc : q.getDocuments()) doc.getReference().delete();
                });
    }

    public void deleteSuggestionsOlderThan(Date dateTimestamp) {
        ref.whereLessThan("date", dateTimestamp).get()
                .addOnSuccessListener(q -> {
                    for (DocumentSnapshot doc : q.getDocuments()) doc.getReference().delete();
                });
    }

    public void getSuggestionById(String docId, OnSuccessListener<DocumentSnapshot> listener) {
        ref.document(docId).get().addOnSuccessListener(listener);
    }

    public void getSuggestionsForUser(String userEmail, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("userEmail", userEmail).orderBy("date", Query.Direction.DESCENDING).get().addOnSuccessListener(listener);
    }

    public void getSuggestionsForUserAndDate(String userEmail, Date date, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("userEmail", userEmail).whereEqualTo("date", date).orderBy("id", Query.Direction.DESCENDING).get().addOnSuccessListener(listener);
    }

    public void getLatestSuggestionForUserAndDate(String userEmail, Date date, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("userEmail", userEmail).whereEqualTo("date", date).orderBy("id", Query.Direction.DESCENDING).limit(1).get().addOnSuccessListener(listener);
    }

    public void getSuggestionsForUserInDateRange(String userEmail, Date start, Date end, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("userEmail", userEmail)
                .whereGreaterThanOrEqualTo("date", start)
                .whereLessThanOrEqualTo("date", end)
                .orderBy("date", Query.Direction.DESCENDING)
                .get().addOnSuccessListener(listener);
    }

    public void getSuggestionForUserAndDate(String email, String loc, Date date, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("userEmail", email)
                .whereEqualTo("location", loc)
                .whereEqualTo("date", date)
                .limit(1).get().addOnSuccessListener(listener);
    }

    public void getTodaySuggestion(String email, Date date, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("userEmail", email)
                .whereEqualTo("date", date)
                .limit(1).get().addOnSuccessListener(listener);
    }
}
