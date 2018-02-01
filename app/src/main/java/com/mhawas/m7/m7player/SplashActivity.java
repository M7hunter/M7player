package com.mhawas.m7.m7player;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import gr.net.maroulis.library.EasySplashScreen;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EasySplashScreen splashScreen = new EasySplashScreen(SplashActivity.this);
        splashScreen.withFullScreen()
                .withTargetActivity(PlayerActivity.class)
                .withSplashTimeOut(5000)
                .withBackgroundColor(Color.parseColor("#ffffff"))
                .withLogo(R.drawable.exo_edit_mode_logo)
                .withAfterLogoText("M7")
                .withBeforeLogoText("before")
                .withHeaderText("welcome")
                .withFooterText("fooooot");

        //text color.
        splashScreen.getAfterLogoTextView().setTextColor(Color.BLACK);


    }


}
