package com.example.aquasaver.repository;
import android.util.Log;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.*;
import com.example.aquasaver.model.*;

import java.util.*;

public class SuggestionsRepository {
    private final CollectionReference ref = FirebaseFirestore.getInstance().collection("suggestions");

    public void insertSuggestion(Suggestions s) { ref.add(s); }

    public void insertAllSuggestions(List<Suggestions> list) {
        for (Suggestions s : list) ref.add(s);
    }

    public void updateSuggestion(String docId, Suggestions s) { ref.document(docId).set(s); }

    public void deleteSuggestion(String docId) { ref.document(docId).delete(); }

    public void deleteSuggestionsByEmail(String userEmail) {
        ref.whereEqualTo("userEmail", userEmail).get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot) {
                        doc.getReference().delete();
                    }
                });
    }

    public void getUserSuggestions(String userEmail, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("userEmail", userEmail).get().addOnSuccessListener(listener);
    }

    public void getSuggestionByTitle(String title, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("title", title).limit(1).get().addOnSuccessListener(listener);
    }
}