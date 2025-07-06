package com.example.aquasaver;

import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;

import com.example.aquasaver.db.AppDatabase;
import com.example.aquasaver.model.UserProfile;
import com.example.aquasaver.model.WeatherSuggestions;
import com.example.aquasaver.model.enums.GoalType;
import com.example.aquasaver.weatherapi.RetrofitClient;
import com.example.aquasaver.weatherapi.WeatherApi;
import com.example.aquasaver.weatherapi.WeatherResponse;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.aquasaver.databinding.ActivityMainBinding;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        fetchWeather("Denver");
        Toolbar toolbar = binding.username.toolbar;
        setSupportActionBar(binding.username.toolbar);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryVariant));


//        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryVariant));

//Email button (not needed)
//        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null)
//                        .setAnchorView(R.id.fab).show();
//            }
//        });


        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_water_usage, R.id.nav_goals)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void fetchWeather(String location) {
        WeatherApi api = RetrofitClient.getWeatherApi();
        String apiKey = "74a0f3136d13e60fcdd50fd6fd9bb433";

        Call<WeatherResponse> call = api.getWeather(location, apiKey, "metric");

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponse data = response.body();
                    Log.d("Weather", "Location: " + data.name);
                    Log.d("Weather", "Temperature: " + data.main.temp + " °C");
                    Log.d("Weather", "Weather Condition: " + data.weather.get(0).description);

                    String suggestionText = "It’s currently " + data.main.temp + "°C with " + data.weather.get(0).description + ". Consider reducing outdoor water usage.";

                    Date today = new Date();
                    WeatherSuggestions suggestion = new WeatherSuggestions("user@example.com", location, today, suggestionText);

                    new Thread(() -> {
                        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                                AppDatabase.class, "aqua_saver.db").build();
                        UserProfile testUser = new UserProfile("user@example.com", "password", "Denver", false, GoalType.DAILY, true, "daily", true, new Date());
                        db.userProfileDao().insertUserProfile(testUser);
                        db.weatherSuggestionsDao().insertSuggestion(suggestion);

                        List<WeatherSuggestions> suggestions = db.weatherSuggestionsDao()
                                .getSuggestionsForUser("user@example.com");

                        for (WeatherSuggestions s : suggestions) {
                            Log.d("DB_CHECK", "Saved Suggestion: " +
                                    s.location + " | " +
                                    s.usageSuggestionText);
                        }
                    }).start();
                } else {
                    Log.e("WeatherAPI", "Unsuccessful response: Code " + response.code());
                    try {
                        Log.e("WeatherAPI", "Error body: " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Log.e("WeatherAPI", "API call failed", t);
            }
        }); // closing enqueue()
    }

}