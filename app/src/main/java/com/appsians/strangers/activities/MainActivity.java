package com.appsians.strangers.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.appsians.strangers.FakeActivity;
import com.appsians.strangers.databinding.ActivityMainBinding;
import com.appsians.strangers.models.User;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    long coins = 0;
    String[] permissions = new String[] {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private int requestCode = 1;
    User user;
    KProgressHUD progress;
    long user_turn_count = 1;
    long frequency=3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        progress = KProgressHUD.create(this);
        progress.setDimAmount(0.5f);
        progress.show();

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        FirebaseUser currentUser = auth.getCurrentUser();

        database.getReference().child("profiles")
                .child(currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        progress.dismiss();
                        user = snapshot.getValue(User.class);
                        coins = user.getCoins();

                        binding.coins.setText("You have: " + coins);

                        Glide.with(MainActivity.this)
                                .load(user.getProfile())
                                .into(binding.profilePicture);
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
        // get the frequency of video redirect
        database.getReference().child("frequency").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                frequency = snapshot.getValue(Integer.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
// getting the count of turn user finding the match.


        binding.findButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isPermissionsGranted()) {
                    if (coins >= 10) {
                        database.getReference().child("user_turn_count").child(currentUser.getUid()).child("turn_count").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                user_turn_count = snapshot.getValue(Integer.class);
                                Toast.makeText(MainActivity.this, ""+ user_turn_count, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        coins = coins - 10;
                        database.getReference().child("profiles")
                                .child(currentUser.getUid())
                                .child("coins")
                                .setValue(coins);
                        Intent intent;
                        // user_turn_count%frequency==0
                        if(user_turn_count%100==0){

                            intent = new Intent(MainActivity.this, FakeActivity.class);
                        }
                        else {
                            intent = new Intent(MainActivity.this, ConnectingActivity.class);
                        }
                        user_turn_count +=1;
                        Toast.makeText(MainActivity.this, "turn"+user_turn_count, Toast.LENGTH_SHORT).show();
                        database.getReference().child("user_turn_count").child(currentUser.getUid()).child("turn_count").setValue(user_turn_count);
                        intent.putExtra("profile", user.getProfile());
                        startActivity(intent);
                    } else {
                        Toast.makeText(MainActivity.this, "Insufficient Coins", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, RewardActivity.class);
                        startActivity(intent);
                    }
                } else {
                    askPermissions();
                }
            }
        });

        binding.rewardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, RewardActivity.class));
            }
        });




    }

    void askPermissions(){
        ActivityCompat.requestPermissions(this, permissions, requestCode);
    }

    private boolean isPermissionsGranted() {
        for(String permission : permissions ){
            if(ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }

        return true;
    }

}