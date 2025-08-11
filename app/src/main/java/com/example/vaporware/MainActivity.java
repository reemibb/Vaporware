package com.example.vaporware;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.*;
import android.animation.ObjectAnimator;
import android.view.animation.DecelerateInterpolator;

public class MainActivity extends AppCompatActivity {
    private ImageView swipeButton;
    private View swipeTrack;
    private TextView swipeText;
    private float dX = 0f;
    private boolean isSwiped = false;

    private TextView textView2;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        textView2 = findViewById(R.id.textView2);

        textView2.setTranslationX(-1000f);

        animateTextView2();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        swipeButton = findViewById(R.id.swipe_button);
        swipeTrack = findViewById(R.id.swipe_track);
        swipeText = findViewById(R.id.swipe_text);

        swipeButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    dX = event.getRawX() - v.getX();
                    return true;

                case MotionEvent.ACTION_MOVE:
                    float moveX = event.getRawX() - dX;
                    if (moveX > 0 && moveX < swipeTrack.getWidth() - swipeButton.getWidth()) {
                        v.setX(moveX);
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                    if (v.getX() > swipeTrack.getWidth() * 0.7) {
                        completeSwipe();
                    } else {
                        resetButton();
                    }
                    return true;
            }
            return false;
        });
    }



    @SuppressLint("SetTextI18n")
    private void completeSwipe() {
        isSwiped = true;
        swipeButton.animate()
                .x(swipeTrack.getWidth() - swipeButton.getWidth())
                .setDuration(300)
                .withEndAction(() -> {
                    swipeText.setText("Loading...");
                    Intent intent = new Intent(MainActivity.this, LogActivity.class);
                    startActivity(intent);
                    finish();
                });
    }

    private void resetButton() {
        swipeButton.animate()
                .x(0)
                .setDuration(300)
                .start();
    }

    private void animateTextView2() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(textView2, "translationX", 0f);
        animator.setDuration(1500);
        animator.start();
        animator.setInterpolator(new DecelerateInterpolator());
    }
}

