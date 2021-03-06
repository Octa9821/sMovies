package com.example.moviesapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoggedInMainActivity extends AppCompatActivity {


    //TODO Add a search function in the Action Bar, or below it if too difficult. ADDED.
    //TODO Add a Watched List and a To Watch List (Needs Database). Quite Difficult. ADDED To Watch List. No need to Watched.
    //TODO Add Now Playing list and Google Maps implementation for Cinemas. ADDED.

    //TODO Prevent user from adding the same movie more than once to the DB.


    List<Movie> popularList;
    List<Movie> topratedList;
    List<Movie> upcomingList;
    RecyclerView recyclerViewPopular;
    RecyclerView recyclerViewTopRated;
    RecyclerView recyclerViewUpcoming;
    EditText searchEditText;
    FloatingActionButton searchButton;

    //firebase variables
    private FirebaseUser user;
    private DatabaseReference reference;
    private String userID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"#FCE205\">" + getString(R.string.app_name) + "</font>"));
        getSupportActionBar().setIcon(R.drawable.ic_logo_all_yellow);
        setContentView(R.layout.activity_main_logged_in);

        popularList = new ArrayList<Movie>();
        topratedList = new ArrayList<Movie>();
        upcomingList = new ArrayList<Movie>();
        recyclerViewPopular = findViewById(R.id.recyclerViewPopular);
        recyclerViewTopRated = findViewById(R.id.recyclerViewTopRated);
        recyclerViewUpcoming = findViewById(R.id.recyclerViewUpcoming);
        searchEditText = findViewById(R.id.search_edit_text);
        searchButton = findViewById(R.id.search_button);
        final TextView welcomeText = findViewById(R.id.welcome_user_text);

        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        userID = user.getUid();

        reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User userProfile = snapshot.getValue(User.class);

                if(userProfile != null){
                    String fullName = userProfile.fullName;
                    welcomeText.setText("Welcome, " + fullName + "!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {            }
        });

//        OkHttpClient.Builder builder = new OkHttpClient.Builder();
//
//        builder.addInterceptor(new Interceptor() {
//            @Override
//            public okhttp3.Response intercept(Chain chain) throws IOException {
//                Request request = chain.request().newBuilder().addHeader("api-key", "b46c711d586cb64dadb67448afdb0919").build();
//                return chain.proceed(request);
//            }
//        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEditText.clearFocus();
                if(searchEditText.getText().toString().trim().length() <= 0) {
                    Toast.makeText(LoggedInMainActivity.this, "Please enter something to search for first.", Toast.LENGTH_SHORT).show();
                }
                else{
                    Intent intent = new Intent(LoggedInMainActivity.this, SearchActivity.class);
                    intent.putExtra("search_text", searchEditText.getText().toString());
                    searchEditText.setText("");
                    startActivity(intent);
                }
            }
        });

        Retrofit retrofitPopular = new Retrofit.Builder()
                .baseUrl("https://api.themoviedb.org/3/movie/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TmdbAPI tmdbAPIpopular = retrofitPopular.create(TmdbAPI.class);

        Call<GetMovieResponse> callPopular = tmdbAPIpopular.getMoviesPopular();
        callPopular.enqueue(new Callback<GetMovieResponse>() {
            @Override
            public void onResponse(Call<GetMovieResponse> call, Response<GetMovieResponse> response) {
                if (response.isSuccessful()) {
                    GetMovieResponse responseBody = response.body();
                    if (responseBody != null) {
                        popularList = responseBody.getMovies();
                        PutDataIntoRecyclerViewPopular(popularList);
                    }
                }
            }

            @Override
            public void onFailure(Call<GetMovieResponse> call, Throwable t) {
                Toast.makeText(LoggedInMainActivity.this, "Error onFailure: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });


        Retrofit retrofitTopRated = new Retrofit.Builder()
                .baseUrl("https://api.themoviedb.org/3/movie/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TmdbAPI tmdbAPItoprated = retrofitTopRated.create(TmdbAPI.class);

        Call<GetMovieResponse> callTopRated = tmdbAPItoprated.getMoviesTopRated();
        callTopRated.enqueue(new Callback<GetMovieResponse>() {
            @Override
            public void onResponse(Call<GetMovieResponse> call, Response<GetMovieResponse> response) {
                if (response.isSuccessful()) {
                    GetMovieResponse responseBody = response.body();
                    if (responseBody != null) {
                        topratedList = responseBody.getMovies();
                        PutDataIntoRecyclerViewTopRated(topratedList);
                    }
                }
            }

            @Override
            public void onFailure(Call<GetMovieResponse> call, Throwable t) {
                Toast.makeText(LoggedInMainActivity.this, "Error onFailure: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        Retrofit retrofitUpcoming = new Retrofit.Builder()
                .baseUrl("https://api.themoviedb.org/3/movie/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TmdbAPI tmdbAPIupcoming = retrofitUpcoming.create(TmdbAPI.class);

        Call<GetMovieResponse> callUpcoming = tmdbAPIupcoming.getMoviesUpcoming();
        callUpcoming.enqueue(new Callback<GetMovieResponse>() {
            @Override
            public void onResponse(Call<GetMovieResponse> call, Response<GetMovieResponse> response) {
                if (response.isSuccessful()) {
                    GetMovieResponse responseBody = response.body();
                    if (responseBody != null) {
                        upcomingList = responseBody.getMovies();
                        PutDataIntoRecyclerViewUpcoming(upcomingList);
                    }
                }
            }

            @Override
            public void onFailure(Call<GetMovieResponse> call, Throwable t) {
                Toast.makeText(LoggedInMainActivity.this, "Error onFailure: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.now_playing_button:
                //Toast.makeText(this, "Now Playing Button Tapped", Toast.LENGTH_SHORT).show();
                openNowPlayingMoviesActivity();
                return true;
            case R.id.go_to_watch_list_button:
                //Toast.makeText(this, "To Watch Button Tapped", Toast.LENGTH_SHORT).show();
                Intent intentWatchList = new Intent(LoggedInMainActivity.this, ToWatchListActivity.class);
                startActivity(intentWatchList);
                return true;
//            case R.id.go_to_watched_list_button:
                //Toast.makeText(this, "Watched Button Tapped", Toast.LENGTH_SHORT).show();
//                return true;
            case R.id.login_button:
                Toast.makeText(this, "You are already logged in", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.account_info:
                Intent intentAccInfo = new Intent(LoggedInMainActivity.this, AccountInfoActivity.class);
                startActivity(intentAccInfo);
                //Toast.makeText(this, "Account Info Button Tapped", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void PutDataIntoRecyclerViewPopular(List<Movie> movieList){
        MyAdapter myAdapter = new MyAdapter(this, movieList);
        recyclerViewPopular.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPopular.setAdapter(myAdapter);
    }

    private void PutDataIntoRecyclerViewTopRated(List<Movie> movieList){
        MyAdapter myAdapter = new MyAdapter(this, movieList);
        recyclerViewTopRated.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTopRated.setAdapter(myAdapter);
    }

    private void PutDataIntoRecyclerViewUpcoming(List<Movie> movieList){
        MyAdapter myAdapter = new MyAdapter(this, movieList);
        recyclerViewUpcoming.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewUpcoming.setAdapter(myAdapter);
    }

    public void openNowPlayingMoviesActivity(){
        Intent intent = new Intent(this, NowPlayingMoviesActivity.class);
        startActivity(intent);
    }

    public void openLoginActivity(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

}