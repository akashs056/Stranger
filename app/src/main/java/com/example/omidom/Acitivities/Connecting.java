package com.example.omidom.Acitivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.omidom.Models.User;
import com.example.omidom.R;
import com.example.omidom.databinding.ActivityConnectingBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class Connecting extends AppCompatActivity {

    ActivityConnectingBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;

    User user;

    boolean isOkay=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityConnectingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth=FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();
        FirebaseUser currentUser=auth.getCurrentUser();

        String profile= getIntent().getStringExtra("profile");
        Glide.with(Connecting.this).load(profile).into(binding.profileImage);

        String username=auth.getUid();

        database.getReference().child("online")
                .orderByChild("status")
                .equalTo(0).limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getChildrenCount()>0){
                            isOkay=true;
                            // Room Available
                            for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                                database.getReference().child("online")
                                        .child(dataSnapshot.getKey())
                                        .child("incoming")
                                        .setValue(username);

                                database.getReference().child("online")
                                        .child(dataSnapshot.getKey())
                                        .child("status")
                                        .setValue(1);
                                Intent intent=new Intent(Connecting.this, Call.class);
                                String incoming=dataSnapshot.child("incoming").getValue(String.class);
                                String createdBy=dataSnapshot.child("createdBy").getValue(String.class);
                                Boolean isAvailable=dataSnapshot.child("isAvailable").getValue(Boolean.class);
                                intent.putExtra("username",username);
                                intent.putExtra("incoming",incoming);
                                intent.putExtra("createdBy",createdBy);
                                intent.putExtra("isAvailable",isAvailable);
                                startActivity(intent);
                                finish();
                            }
                        }else {
                            // Room Not Available
                            HashMap<String,Object> room=new HashMap<>();
                            room.put("incoming",username);
                            room.put("createdBy",username);
                            room.put("isAvailable",true);
                            room.put("status",0);
                            database.getReference().child("online")
                                    .child(username)
                                    .setValue(room).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            database.getReference().child("online")
                                                    .child(username).addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            if (snapshot.child("status").exists()){
                                                                if (snapshot.child("status").getValue(Integer.class)==1){
                                                                    if (isOkay) return;
                                                                    isOkay=true;
                                                                    Intent intent=new Intent(Connecting.this, Call.class);
                                                                    String incoming=snapshot.child("incoming").getValue(String.class);
                                                                    String createdBy=snapshot.child("createdBy").getValue(String.class);
                                                                    Boolean isAvailable=snapshot.child("isAvailable").getValue(Boolean.class);
                                                                    intent.putExtra("username",username);
                                                                    intent.putExtra("incoming",incoming);
                                                                    intent.putExtra("createdBy",createdBy);
                                                                    intent.putExtra("isAvailable",isAvailable);
                                                                    startActivity(intent);
                                                                    finish();
                                                                }
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }
}