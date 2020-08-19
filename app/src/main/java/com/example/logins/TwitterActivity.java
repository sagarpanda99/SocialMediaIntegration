package com.example.logins;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class TwitterActivity extends Activity {
    private TextView username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitter);
        String twusername = getIntent().getStringExtra("username");
        username=findViewById(R.id.tuser);
        username.setText(twusername);


    }

}