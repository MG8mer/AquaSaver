package com.example.aquasaver.repository;
import android.util.Log;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.*;
import com.example.aquasaver.model.*;

import java.util.*;

public class SuggestionsRepository {
    private final CollectionReference ref = FirebaseFirestore.getInstance().collection("suggestions");

    public void insert(Suggestions s) { ref.add(s); }

    public void insertAll(List<Suggestions> list) {
        for (Suggestions s : list) ref.add(s);
    }

    public void update(String docId, Suggestions s) { ref.document(docId).set(s); }

    public void delete(String docId) { ref.document(docId).delete(); }

    public void getByUser(String email, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("userEmail", email).get().addOnSuccessListener(listener);
    }
}