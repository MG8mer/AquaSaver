package com.example.aquasaver.ui.main_pages;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aquasaver.R;
import com.example.aquasaver.model.UserProfile;
import com.example.aquasaver.repository.UserProfileRepository;
import com.example.aquasaver.ui.main_pages.utility.PasswordUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {
    EditText username, password;
    Button loginBtn, signupBtn;

    private UserProfileRepository userProfileRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        loginBtn = findViewById(R.id.loginBtn);
        signupBtn = findViewById(R.id.signupBtn);

        userProfileRepo = new UserProfileRepository();

        loginBtn.setOnClickListener(v -> {
            String user = username.getText().toString().trim();
            String pass = password.getText().toString().trim();

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            Executors.newSingleThreadExecutor().execute(() -> {
                userProfileRepo.getUserProfilesByEmail(user, snapshot -> {
                    if (!snapshot.isEmpty()) {
                        UserProfile existingUser = snapshot.getDocuments().get(0).toObject(UserProfile.class);

                        boolean isMatch = PasswordUtils.verifyPassword(pass, existingUser.getSalt(), existingUser.getPasswordHash());

                        runOnUiThread(() -> {
                            if (isMatch) {

                                SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
                                prefs.edit()
                                        .putString("username", existingUser.getEmail())
                                        .putString("goal", existingUser.getGoalType().toString())
                                        .apply();
                                FirebaseAuth.getInstance().signInWithEmailAndPassword(user, pass)
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                FirebaseUser app_user = FirebaseAuth.getInstance().getCurrentUser();
                                                Log.d("LoginActivity", "Login successful. User: " +
                                                        (app_user != null ? app_user.getEmail() : "null"));

                                                // Don't navigate immediately - wait for auth state
                                                FirebaseAuth.getInstance().addAuthStateListener(authStateListener -> {
                                                    FirebaseUser currentUser = authStateListener.getCurrentUser();
                                                    if (currentUser != null) {
                                                        Log.d("LoginActivity", "Auth state confirmed: " + currentUser.getEmail());
                                                        // NOW navigate to main activity
                                                        startActivity(new Intent(this, MainActivity.class));
                                                        finish();
                                                    }
                                                });
                                            }
                                        });
                                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show());
                    }
                });
            });
        });

        signupBtn.setOnClickListener(v -> {
            getSharedPreferences("UserProfile", MODE_PRIVATE).edit().clear().apply();
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        });
    }
}
