package com.example.aquasaver;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;


import com.example.aquasaver.dao.UserProfileDao;
import com.example.aquasaver.db.AppDatabase;
import com.example.aquasaver.model.UserProfile;
import androidx.room.Room;
import com.example.aquasaver.R;
import com.example.aquasaver.ui.main_pages.MainActivity;

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

        AppDatabase db = Room.databaseBuilder(
                getApplicationContext(),
                AppDatabase.class,
                "aqua_saver.db"
        ).allowMainThreadQueries().build(); // for development only

        UserProfileDao userDao = db.userProfileDao();

        loginBtn.setOnClickListener(v -> {
            String user = username.getText().toString().trim();
            String pass = password.getText().toString().trim();

            // Handling login (SignupActivity.java handels SIGNING UP)
            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else {
                UserProfile existingUser = userDao.getUserProfileByIds(user, pass);
                if (existingUser != null) {
                    Log.d("LoginActivity", "Login success: saving user data: " +
                            existingUser.getEmail() + ", " + existingUser.getLocation());
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                    // Save to SharedPreferences
                    getSharedPreferences("UserProfile", MODE_PRIVATE).edit()
                            .putString("username", existingUser.getEmail())
                            .putString("location", existingUser.getLocation())
                            .apply();
                } else {
                    Log.d("LoginActivity", "Testing login failure");
                    Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Navigate to SignupActivity
        signupBtn.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, com.example.aquasaver.SignupActivity.class);

            // Optional: Clear old signup data
            getSharedPreferences("UserProfile", MODE_PRIVATE).edit().clear().apply();

            startActivity(intent);
        });
    }
}
