package com.example.aquasaver.ui.main_pages;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aquasaver.repository.UserProfileRepository;
import com.example.aquasaver.model.UserProfile;
import com.example.aquasaver.R;

import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {
    EditText username, password;
    Button loginBtn, signupBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Connect UI components
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        loginBtn = findViewById(R.id.loginBtn);
        signupBtn = findViewById(R.id.signupBtn);

        UserProfileRepository userProfileRepo = new UserProfileRepository();

        // Handle Login button click
        loginBtn.setOnClickListener(v -> {
            String user = username.getText().toString().trim();
            String pass = password.getText().toString().trim();

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            Executors.newSingleThreadExecutor().execute(() -> {
                // Query DB for user with email and password
                final UserProfile[] existingUser = new UserProfile[1];
                userProfileRepo.getUserProfileByIds(user, pass, snapshot -> {
                    if (!snapshot.isEmpty()) {
                        existingUser[0] = snapshot.getDocuments().get(0).toObject(UserProfile.class);
                    }
                });

                runOnUiThread(() -> {
                    if (existingUser[0] != null) {
                        Log.d("LoginActivity", "Login success: user=" + existingUser[0].getEmail());

                        // Save user info in SharedPreferences for later use
                        SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
                        prefs.edit()
                                .putString("username", existingUser[0].getEmail())
                                .putString("location", existingUser[0].getLocation())
                                .apply();

                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();

                        // Navigate to MainActivity and finish LoginActivity
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.d("LoginActivity", "Login failure: user not found for username=" + user);
                        Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        // Handle Signup button click
        signupBtn.setOnClickListener(v -> {
            // Clear previous user data if any
            getSharedPreferences("UserProfile", MODE_PRIVATE).edit().clear().apply();

            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }
}
