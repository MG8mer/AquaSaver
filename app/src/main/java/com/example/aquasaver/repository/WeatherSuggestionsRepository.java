package com.example.aquasaver.repository;

import android.util.Log;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.*;
import com.example.aquasaver.model.*;

import java.util.*;

public class WeatherSuggestionsRepository {
    private final CollectionReference ref = FirebaseFirestore.getInstance().collection("weather_suggestions");

    public void insert(WeatherSuggestions suggestion) { ref.add(suggestion); }

    public void insertAll(List<WeatherSuggestions> list) {
        for (WeatherSuggestions s : list) ref.add(s);
    }

    public void update(String docId, WeatherSuggestions s) { ref.document(docId).set(s); }

    public void delete(String docId) { ref.document(docId).delete(); }

    public void getByUser(String email, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("userEmail", email)
                .orderBy("date", Query.Direction.DESCENDING)
                .get().addOnSuccessListener(listener);
    }

    public void getByUserAndDate(String email, Date date, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("userEmail", email)
                .whereEqualTo("date", date)
                .orderBy("id", Query.Direction.DESCENDING)
                .get().addOnSuccessListener(listener);
    }

    public void getByDateRange(String email, Date start, Date end, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("userEmail", email)
                .whereGreaterThanOrEqualTo("date", start)
                .whereLessThanOrEqualTo("date", end)
                .orderBy("date", Query.Direction.DESCENDING)
                .get().addOnSuccessListener(listener);
    }
}
