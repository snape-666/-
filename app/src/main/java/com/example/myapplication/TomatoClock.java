package com.example.myapplication;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class TomatoClock extends AppCompatActivity {

    private CircleProgress progressBar;
    private TextView timerText;
    private TextView tomatoBack;
    private CountDownTimer currentTimer;//倒计时控件
    private long totalTimeMs = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tomatoclock);

        progressBar = findViewById(R.id.progressBar);
        timerText = findViewById(R.id.timerText);
        tomatoBack =findViewById(R.id.back);
        Button giveUpBtn = findViewById(R.id.giveUpBtn);
        TextView workBtn = findViewById(R.id.workBtn);
        TextView shortRestBtn = findViewById(R.id.shortRestBtn);
        TextView longRestBtn = findViewById(R.id.longRestBtn);

        workBtn.setOnClickListener(v -> startTimer(10 * 60 * 1000));
        shortRestBtn.setOnClickListener(v -> startTimer(5 * 60 * 1000));
        longRestBtn.setOnClickListener(v -> startTimer(20 * 60 * 1000));
        giveUpBtn.setOnClickListener(v -> cancelTimer());
            tomatoBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Toast.makeText(TomatoClock.this, "返回主页", Toast.LENGTH_SHORT).show();


                    Intent intent = new Intent(TomatoClock.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
    }



    private void startTimer(long durationMs) {
        cancelTimer();
        totalTimeMs = durationMs;

        currentTimer = new CountDownTimer(durationMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long min = (millisUntilFinished / 1000) / 60;
                long sec = (millisUntilFinished / 1000) % 60;
                timerText.setText(String.format("%02d:%02d", min, sec));

                float progress = (1 - (float) millisUntilFinished / totalTimeMs) * 100;
                progressBar.setProgress(progress);
            }

            @Override
            public void onFinish() {
                timerText.setText("00:00");
                progressBar.setProgress(100);
            }
        }.start();
    }


    private void cancelTimer() {
        if (currentTimer != null) {
            currentTimer.cancel();
            currentTimer = null;
        }
    }

    @Override
    protected void onDestroy() {
        cancelTimer();
        super.onDestroy();
    }

}
