package com.example.omidom.Acitivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.omidom.Models.User;
import com.example.omidom.databinding.ActivityMainBinding;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    long coins=0;
    String[] permissions=new String[]{Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO};
    private int REQUEST_CODE=1;
    User user1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth=FirebaseAuth.getInstance();
        FirebaseUser user=auth.getCurrentUser();

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

            }
        });

        database=FirebaseDatabase.getInstance();

        database.getReference().child("Users")
                        .child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        user1=snapshot.getValue(User.class);
                        coins=user1.getCoins();
                        binding.youhavecoins.setText("You Have : "+coins);
                        Glide.with(MainActivity.this).load(user1.getProfile()).into(binding.profileImage);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        binding.find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPermissionsGranted()) {
                    if (coins > 5) {
                        coins=coins-5;
                        database.getReference().child("Users")
                                .child(auth.getUid())
                                .child("coins")
                                .setValue(coins);
                        Intent intent = new Intent(MainActivity.this, Connecting.class);
                        startActivity(intent);
                        intent.putExtra("profile",user1.getProfile());
                    } else {
                        Toast.makeText(MainActivity.this, "Insufficient Coins", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        binding.rewardbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RewardActivity.class));
            }
        });


    }

    void askPermissions(){
        ActivityCompat.requestPermissions(this,permissions,REQUEST_CODE);
    }

    private boolean isPermissionsGranted(){
        for(String p:permissions){
            if (ActivityCompat.checkSelfPermission(MainActivity.this,p)!= PackageManager.PERMISSION_GRANTED){
                askPermissions();
                return  false;
            }
        }
        return  true;
    }
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == REQUEST_CODE) {
//            // Check if all permissions are granted
//            boolean allGranted = true;
//            for (int result : grantResults) {
//                if (result != PackageManager.PERMISSION_GRANTED) {
//                    allGranted = false;
//                    break;
//                }
//            }
//
//            if (!allGranted) {
//                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
//                askPermissions();
//            } else {
//
//            }
//        }
//    }
}