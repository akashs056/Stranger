package com.example.omidom.Acitivities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.omidom.databinding.ActivityWelcomeBinding;
import com.google.firebase.auth.FirebaseAuth;

public class Welcome extends AppCompatActivity {
    FirebaseAuth auth;

    ActivityWelcomeBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityWelcomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth=FirebaseAuth.getInstance();

        if (auth.getCurrentUser()!=null){
            goTonextActivity();
        }

        binding.getStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goTonextActivity();
            }
        });
    }
    void goTonextActivity(){
        Intent intent=new Intent(Welcome.this,Login.class);
        startActivity(intent);
        finish();
    }
}