package com.example.aquasaver.repository;

import android.util.Log;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.*;
import com.example.aquasaver.model.*;

import java.util.*;

public class UserProfileRepository {
    private final CollectionReference ref = FirebaseFirestore.getInstance().collection("user_profiles");

    public void insert(UserProfile p) { ref.document(p.getEmail()).set(p); }

    public void update(UserProfile p) { ref.document(p.getEmail()).set(p); }

    public void delete(String email) { ref.document(email).delete(); }

    public void getByEmail(String email, OnSuccessListener<DocumentSnapshot> listener) {
        ref.document(email).get().addOnSuccessListener(listener);
    }

    public void getAll(OnSuccessListener<QuerySnapshot> listener) {
        ref.get().addOnSuccessListener(listener);
    }
}