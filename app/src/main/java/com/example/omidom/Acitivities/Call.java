package com.example.omidom.Acitivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.omidom.Models.InterfaceJava;
import com.example.omidom.Models.OnPeerConnectedListener;
import com.example.omidom.Models.User;
import com.example.omidom.R;
import com.example.omidom.databinding.ActivityCallBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Call extends AppCompatActivity implements OnPeerConnectedListener {

    ActivityCallBinding binding;
    String uniqueId="";
    FirebaseAuth auth;

    String username="";
    String friendsUsername="";
    boolean isPeerConnected=false;
    DatabaseReference databaseReference;
    boolean isAudio=true;
    boolean isVideo=true;
    String createdBy;
    boolean pageExit=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth=FirebaseAuth.getInstance();
        uniqueId=auth.getUid();

        databaseReference= FirebaseDatabase.getInstance().getReference().child("online");
        username=getIntent().getStringExtra("username");
        String incoming=getIntent().getStringExtra("incoming");
        createdBy=getIntent().getStringExtra("createdBy");

        friendsUsername="";

//        if (incoming.equalsIgnoreCase(friendsUsername)){
//        }
        friendsUsername=incoming;


        setUpWebView();

        binding.micBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAudio=!isAudio;
                callJavaScriptFunction("javascript:toggleAudio(\""+isAudio+"\")");
                if (isAudio){
                    binding.micBtn.setImageResource(R.drawable.btn_unmute_normal);
                }else{
                    binding.micBtn.setImageResource(R.drawable.btn_mute_normal);
                }
            }
        });

        binding.vedioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isVideo=!isVideo;
                callJavaScriptFunction("javascript:toggleVideo(\""+isVideo+"\")");
                if (isVideo){
                    binding.vedioBtn.setImageResource(R.drawable.btn_video_normal);
                }else{
                    binding.vedioBtn.setImageResource(R.drawable.btn_video_muted);
                }
            }
        });

        binding.endCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
    @SuppressLint("SetJavaScriptEnabled")
    void setUpWebView(){
        binding.webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                request.grant(request.getResources());
            }
        });
        binding.webView.getSettings().setJavaScriptEnabled(true);
        binding.webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        binding.webView.addJavascriptInterface(new InterfaceJava(this),"OmiDom");

        //loadVideoCall
        loadVideoCall();
    }
    public void loadVideoCall(){
        String filePath="file:android_asset/call.html";
        binding.webView.loadUrl(filePath);

        binding.webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                initializePeer();
                isPeerConnected=true;
            }
        });
    }
    @Override
    public void onPeerConnected() {
        isPeerConnected = true;
    }

    void initializePeer(){

        callJavaScriptFunction("javascript:init(\"" + uniqueId + "\")");

        if (createdBy.equalsIgnoreCase(username)){
            if (pageExit) return;
            databaseReference.child(username).child("connId").setValue(uniqueId);
            databaseReference.child(username).child("isAvailable").setValue(false);

            binding.loadingGroup.setVisibility(View.GONE);

            FirebaseDatabase.getInstance().getReference().child("Users")
                    .child(friendsUsername).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User user=snapshot.getValue(User.class);
                            Glide.with(Call.this).load(user.getProfile()).into(binding.profileImage);
                            binding.name.setText(user.getName());
                            binding.city.setText(user.getCity());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }else{
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    friendsUsername=createdBy;
                    FirebaseDatabase.getInstance().getReference().child("Users")
                            .child(friendsUsername).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    User user=snapshot.getValue(User.class);
                                    Glide.with(Call.this).load(user.getProfile()).into(binding.profileImage);
                                    binding.name.setText(user.getName());
                                    binding.city.setText(user.getCity());
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                    FirebaseDatabase.getInstance().getReference().child("online").child(friendsUsername)
                            .child("connId").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.getValue()!=null){
                                        sendCallRequest();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }
            },3000);
        }
    }

    void listenConnId() {
        databaseReference.child(friendsUsername).child("connId").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue()==null){
                    return;
                }else{
                    binding.loadingGroup.setVisibility(View.GONE);
                    String connId=snapshot.getValue(String.class);
                    callJavaScriptFunction("javascript:startCall(\""+connId+"\")");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void sendCallRequest(){
        if (!isPeerConnected){
            Toast.makeText(this, "You are not Connected Please Check your internet", Toast.LENGTH_SHORT).show();
            return;
        }
        listenConnId();
    }

    void  callJavaScriptFunction(String function){
        binding.webView.post(new Runnable() {
            @Override
            public void run() {
            binding.webView.evaluateJavascript(function,null);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pageExit=true;
        databaseReference.child(createdBy).removeValue();
        finish();
    }
}