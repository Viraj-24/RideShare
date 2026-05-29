package com.example.sample;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Show the splash screen layout
        setContentView(R.layout.activity_splash);

        // Find the logo ImageView
        ImageView logo = findViewById(R.id.splashLogo);

        // Load zoom-out animation
        Animation zoomIn = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
        logo.startAnimation(zoomIn);

        // Delay for 2 seconds and then move to LoginActivity
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }, 3000); // 2000 ms = 2 seconds
    }
}
