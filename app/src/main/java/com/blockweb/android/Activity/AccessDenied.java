package com.blockweb.android.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.blockweb.android.R;

public class AccessDenied extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access_denied);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}