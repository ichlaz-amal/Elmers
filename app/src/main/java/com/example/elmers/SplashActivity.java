package com.example.elmers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkPermission();
            }
        }, 3000);
    }

    private void checkPermission() {
        boolean getPermission = true;
        String[] permissions = {
                Manifest.permission.CAMERA, Manifest.permission.INTERNET,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                getPermission = false;
            }
        } if (!getPermission) {
            ActivityCompat.requestPermissions(this, permissions,1);
        } else {
            goToMain();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            goToMain();
        } else {
            super.onBackPressed();
        }
    }

    private void goToMain() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Intent intent = user != null ?
                new Intent(SplashActivity.this, RoleActivity.class) :
                new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intent);  finish();
    }
}
