package com.example.spintracks;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class FinishWorkoutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish_workout);

        Button returnToMenuButton = findViewById(R.id.return_to_menu);
        returnToMenuButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            // Instead of launching a new instance of MainActivity, all of the other
            // activities on top of the existing instance are destroyed.
            // See https://developer.android.com/reference/android/content/Intent#FLAG_ACTIVITY_CLEAR_TOP
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
    }
}
