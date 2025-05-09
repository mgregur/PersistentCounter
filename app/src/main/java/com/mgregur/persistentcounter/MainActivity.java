package com.mgregur.persistentcounter;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    private int counterValue = 0;
    private TextView counterText;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "CounterPrefs";
    private static final String COUNTER_KEY = "CounterValue";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize shared preferences for persistence
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Load the saved counter value
        counterValue = sharedPreferences.getInt(COUNTER_KEY, 0);

        // Initialize views
        counterText = findViewById(R.id.counterText);
        Button minusTenButton = findViewById(R.id.minusTenButton);
        Button minusFiveButton = findViewById(R.id.minusFiveButton);
        Button plusFiveButton = findViewById(R.id.plusFiveButton);
        Button plusTenButton = findViewById(R.id.plusTenButton);

        EditText dailyIncrementEdit = findViewById(R.id.dailyIncrementInput);
        Button dailyIncrementSave = findViewById(R.id.saveIncrementButton);

        // Display the initial counter value
        updateCounterDisplay();

        // Set up click listeners
        minusTenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateCounter(-10);
            }
        });

        minusFiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateCounter(-5);
            }
        });

        plusFiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateCounter(5);
            }
        });

        plusTenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateCounter(10);
            }
        });

        dailyIncrementSave.setOnClickListener(v -> {
                String editText = dailyIncrementEdit.getText().toString();

                int value;
                String message;
                try {
                    value = Integer.parseInt(editText);
                    message = String.format("Daily increment changed to %d.", value);
                } catch (NumberFormatException e) {
                    message = "Invalid number.";
                }

                Toast toast = Toast.makeText(this,message, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0); // adjust Y offset as needed
                toast.show();
        });

        // Set up center counter click listener (optional - could be used to reset)
        findViewById(R.id.counterContainer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Optional: add functionality for the center button if desired
                // For example, reset: updateCounter(-counterValue);
            }
        });


    }

    private void updateCounter(int change) {
        counterValue += change;
        updateCounterDisplay();
        saveCounterValue();
    }

    private void updateCounterDisplay() {
        counterText.setText(String.valueOf(counterValue));
    }

    private void saveCounterValue() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(COUNTER_KEY, counterValue);
        editor.apply();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveCounterValue();
    }
}