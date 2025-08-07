package com.example.aquasaver.repository;

import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.*;
import com.example.aquasaver.model.*;

import java.util.*;

public class UserProfileRepository {
    private final CollectionReference ref = FirebaseFirestore.getInstance().collection("user_profiles");

    public void insertUserProfile(UserProfile p) {
        String key = p.getEmail() + "_" + p.getLocation();
        ref.document(key).set(p);
    }

    public void updateUserProfile(UserProfile p, OnSuccessListener<Integer> onSuccess, OnFailureListener onFailure) {
        String key = p.getEmail() + "_" + p.getLocation();

        ref.document(key).set(p)
                .addOnSuccessListener(unused -> {
                    onSuccess.onSuccess(1); // 1 means update succeeded
                })
                .addOnFailureListener(e -> {
                    onFailure.onFailure(e); // handle the error
                });
    }

    public void deleteUserProfile(UserProfile p) {
        String key = p.getEmail() + "_" + p.getLocation();
        ref.document(key).delete();
    }

    public void deleteUserProfileByIds(String email, String location) {
        String key = email + "_" + location;
        ref.document(key).delete();
    }

    public void getUserProfileByIds(String email, String password, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("email", email).whereEqualTo("password", password).get().addOnSuccessListener(listener);
    }

    public void getUserProfilesByEmail(String email, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("email", email).get().addOnSuccessListener(listener);
    }

    public void getAllUserProfiles(OnSuccessListener<QuerySnapshot> listener) {
        ref.get().addOnSuccessListener(listener);
    }

    public void doesProfileExist(String email, String location, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("email", email).whereEqualTo("location", location).limit(1).get().addOnSuccessListener(listener);
    }

    public void doesProfileExist(String email, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("email", email).limit(1).get().addOnSuccessListener(listener);
    }

    public void getUserByEmail(String email, OnSuccessListener<QuerySnapshot> listener) {
        ref.whereEqualTo("email", email).limit(1).get().addOnSuccessListener(listener);
    }
}
