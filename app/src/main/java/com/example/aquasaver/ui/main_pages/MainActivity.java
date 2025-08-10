package com.example.aquasaver.ui.main_pages;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.WindowManager;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.analytics.FirebaseAnalytics;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.aquasaver.R;
import com.example.aquasaver.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONObject;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    private final OkHttpClient client = new OkHttpClient();
    private AppBarConfiguration appBarConfiguration;
    private MainViewModel mainViewModel;

    // Firebase components
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase
        initializeFirebase();

        // View Binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup Toolbar
        setSupportActionBar(binding.appBarMain.toolbar);

        // Setup status bar
        setupStatusBar();

        // Setup Navigation
        setupNavigation();

        // Initialize ViewModel
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        Log.d(TAG, "MainActivity created successfully");
    }

    private void initializeFirebase() {
        try {
            // Initialize Firebase App
            FirebaseApp.initializeApp(this);

            // Initialize Firebase Authentication
            mAuth = FirebaseAuth.getInstance();

            // Initialize Firestore Database
            db = FirebaseFirestore.getInstance();

            // Initialize Firebase Analytics
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

            // Get current user
            currentUser = mAuth.getCurrentUser();

            // Set up authentication state listener
            setupAuthStateListener();

            // Log analytics event for app open
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "MainActivity");
            bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "MainActivity");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);

            Log.d(TAG, "Firebase initialized successfully");

            if (currentUser != null) {
                Log.d(TAG, "User is signed in: " + currentUser.getEmail());
            } else {
                Log.d(TAG, "User is not signed in");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase", e);
        }
    }

    private void setupAuthStateListener() {
        mAuth.addAuthStateListener(firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                // User is signed in
                currentUser = user;
                Log.d(TAG, "User signed in: " + user.getEmail());
                onUserSignedIn(user);
            } else {
                // User is signed out
                currentUser = null;
                Log.d(TAG, "User signed out");
                onUserSignedOut();
            }
        });
    }

    private void setupStatusBar() {
        try {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryVariant));
        } catch (Exception e) {
            Log.e(TAG, "Error setting up status bar", e);
            // Fallback to default status bar if colorPrimaryVariant doesn't exist
            getWindow().setStatusBarColor(ContextCompat.getColor(this, android.R.color.black));
        }
    }

    private void setupNavigation() {
        try {
            DrawerLayout drawer = binding.drawerLayout;
            NavigationView navView = binding.navView;

            // Define top-level destinations
            appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.nav_home,
                    R.id.nav_water_usage,
                    R.id.nav_goals,
                    R.id.nav_settings,
                    R.id.nav_conservation_tips)
                    .setOpenableLayout(drawer)
                    .build();

            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            NavigationUI.setupWithNavController(navView, navController);

            Log.d(TAG, "Navigation setup completed");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up navigation", e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu if you have one
        // getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up binding to prevent memory leaks
        if (binding != null) {
            binding = null;
        }
        Log.d(TAG, "MainActivity destroyed");
    }

    // Getter method for ViewModel (useful for fragments)
    public MainViewModel getMainViewModel() {
        return mainViewModel;
    }

    // Method to get OkHttpClient if needed by fragments
    public OkHttpClient getHttpClient() {
        return client;
    }

    // Firebase helper methods
    private void onUserSignedIn(FirebaseUser user) {
        // Handle user sign in - update UI, load user data, etc.
        // You can navigate to appropriate fragments or update navigation drawer
        Log.d(TAG, "Handling user sign in for: " + user.getEmail());

        // Log user login event
        Bundle bundle = new Bundle();
        bundle.putString("user_id", user.getUid());
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);
    }

    private void onUserSignedOut() {
        // Handle user sign out - clear user data, navigate to login, etc.
        Log.d(TAG, "Handling user sign out");

        // You might want to navigate to login screen or show appropriate UI
        // NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        // navController.navigate(R.id.nav_login);
    }

    // Public methods to access Firebase components from fragments
    public FirebaseAuth getFirebaseAuth() {
        return mAuth;
    }

    public FirebaseFirestore getFirestore() {
        return db;
    }

    public FirebaseAnalytics getFirebaseAnalytics() {
        return mFirebaseAnalytics;
    }

    public FirebaseUser getCurrentUser() {
        return currentUser;
    }

    // Method to sign out user
    public void signOutUser() {
        if (mAuth != null) {
            mAuth.signOut();
            Log.d(TAG, "User signed out manually");
        }
    }

    // Method to check if user is authenticated
    public boolean isUserAuthenticated() {
        return currentUser != null;
    }
}