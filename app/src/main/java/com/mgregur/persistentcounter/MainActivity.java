package com.mgregur.persistentcounter;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.Manifest;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private int counterValue;
    private int counterDailyIncrement;
    private TextView counterText;
    private TextView dailyIncrementLabel;
    private SharedPreferences sharedPreferences;
    protected static final String PREFS_NAME = "CounterPrefs";
    protected static final String COUNTER_KEY = "CounterValue";
    protected static final String INCREMENT_KEY = "CounterDailyIncrement";
    protected static final int COUNTER_STARTING_VALUE = 0;
    protected static final int INCREMENT_STARTING_VALUE = 50;

    public static final String ACTION_MIDNIGHT_UPDATE = "com.mgregur.persistentcounter.MIDNIGHT_UPDATE";

    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        scheduleMidnightAlarm();
                    } else {
                        Toast.makeText(this,
                                "Notification permission denied. Daily updates won't show notifications.",
                                Toast.LENGTH_LONG).show();
                    }
                }
        );
        checkNotificationPermission();

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        counterValue = sharedPreferences.getInt(COUNTER_KEY, COUNTER_STARTING_VALUE);
        counterDailyIncrement = sharedPreferences.getInt(INCREMENT_KEY, INCREMENT_STARTING_VALUE);

        counterText = findViewById(R.id.counterText);
        dailyIncrementLabel = findViewById(R.id.dailyIncrementLabel);
        Button minusTenButton = findViewById(R.id.minusTenButton);
        Button minusFiveButton = findViewById(R.id.minusFiveButton);
        Button plusFiveButton = findViewById(R.id.plusFiveButton);
        Button plusTenButton = findViewById(R.id.plusTenButton);

        EditText dailyIncrementEdit = findViewById(R.id.dailyIncrementInput);
        Button dailyIncrementSave = findViewById(R.id.saveIncrementButton);

        updateCounterDisplay();
        updateDailyIncrementLabel();

        minusTenButton.setOnClickListener(v -> {
            updateCounter(-10);
        });
        minusFiveButton.setOnClickListener(v -> {
            updateCounter(-5);
        });
        plusFiveButton.setOnClickListener(v -> {
            updateCounter(5);
        });
        plusTenButton.setOnClickListener(v -> {
            updateCounter(10);
        });
        dailyIncrementSave.setOnClickListener(v -> {
            String editText = dailyIncrementEdit.getText().toString();

            String message;
            try {
                int value = Integer.parseInt(editText);
                counterDailyIncrement = value;
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(INCREMENT_KEY, value);
                editor.apply();
                message = String.format("Daily increment changed to %d.", value);
                updateDailyIncrementLabel();
            } catch (NumberFormatException e) {
                message = "Invalid number.";
            }

            Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
        });
    }

    private void checkNotificationPermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED) {

            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        } else {
            scheduleMidnightAlarm();
        }
    }

    private void updateCounter(int change) {
        counterValue += change;
        updateCounterDisplay();
        saveCounterValue();
        updateDailyIncrementLabel();
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

    public void scheduleMidnightAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, MidnightReceiver.class);
        intent.setAction(ACTION_MIDNIGHT_UPDATE);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        alarmManager.cancel(pendingIntent);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        alarmManager.setRepeating(
                AlarmManager.RTC,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );
    }

    private void updateDailyIncrementLabel() {
        if (dailyIncrementLabel != null) {
            dailyIncrementLabel.setText(
                    "Increment at 00:00 (" + numToStrWithSign(counterDailyIncrement) + ")"
            );
        }
    }

    private String numToStrWithSign(int value) {
        if (value > 0) {
            return "+" + value;
        } else if (value < 0) {
            return "-" + value;
        } else {
            return String.valueOf(value);
        }
    }

}