package com.example.vaporware;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class HelpActivity extends AppCompatActivity {

    private ImageView star1, star2, star3, star4, star5;
    private int selectedRating = 0; //to track selected rating (1-5)
    private EditText feedbackEditText;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        databaseRef = FirebaseDatabase.getInstance().getReference();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(HelpActivity.this, HomeActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        Button submitButton = findViewById(R.id.submit_button);
        EditText topic = findViewById(R.id.topic_text);
        EditText email = findViewById(R.id.email_text);
        EditText deets = findViewById(R.id.deets_text);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String topicText = topic.getText().toString().trim();
                String emailText = email.getText().toString().trim();
                String deetsText = deets.getText().toString().trim();

                if (topicText.isEmpty() || emailText.isEmpty() || deetsText.isEmpty()) {
                    Toast.makeText(HelpActivity.this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                    return; // Stop further execution
                }

                // Save to Firebase
                saveReportToFirebase(topicText, emailText, deetsText);

                new AlertDialog.Builder(HelpActivity.this)
                        .setTitle("Submission Successful")
                        .setMessage("Form has been submitted.") // Message content
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();

                topic.setText(""); // Clear the 'topic' EditText
                email.setText(""); // Clear the 'email' EditText
                deets.setText(""); // Clear the 'deets' EditText
            }
        });

        setUpFaq(R.id.faq1, R.id.answer1);
        setUpFaq(R.id.faq2, R.id.answer2);
        setUpFaq(R.id.faq3, R.id.answer3);
        setUpFaq(R.id.faq4, R.id.answer4);
        setUpFaq(R.id.faq5, R.id.answer5);
        setUpFaq(R.id.faq6, R.id.answer6);
        setUpFaq(R.id.faq7, R.id.answer7);

        star1 = findViewById(R.id.star1);
        star2 = findViewById(R.id.star2);
        star3 = findViewById(R.id.star3);
        star4 = findViewById(R.id.star4);
        star5 = findViewById(R.id.star5);

        feedbackEditText = findViewById(R.id.feedback_edit_text);
        Button submitFeedButton = findViewById(R.id.submit_feedback_button);

        //setting click listeners for each star
        star1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedRating = 1;
                updateStarRating(); //updating star images based on the selected rating
            }
        });

        star2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedRating = 2;
                updateStarRating();
            }
        });

        star3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedRating = 3;
                updateStarRating();
            }
        });

        star4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedRating = 4;
                updateStarRating();
            }
        });

        star5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedRating = 5;
                updateStarRating();
            }
        });

        submitFeedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitFeedback();
            }
        });
    }

    private void setUpFaq(int faqButtonId, int answerTextViewId) {
        Button faqButton = findViewById(faqButtonId);
        TextView answerTextView = findViewById(answerTextViewId);

        faqButton.setOnClickListener(v -> {
            boolean isExpanded = answerTextView.getVisibility() == View.VISIBLE;
            if (isExpanded) {
                collapseView(answerTextView); //to collapse the answer
                faqButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.right_arrow, 0, 0, 0);
            } else {
                expandView(answerTextView); //to expand the answer
                faqButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.down_arrow, 0, 0, 0);
            }
        });
    }

    private void expandView(View view) {
        view.measure(View.MeasureSpec.makeMeasureSpec(((View) view.getParent()).getWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        int targetHeight = view.getMeasuredHeight();

        view.getLayoutParams().height = 0;
        view.setVisibility(View.VISIBLE);

        ValueAnimator animator = ValueAnimator.ofInt(0, targetHeight);
        animator.addUpdateListener(animation -> {
            view.getLayoutParams().height = (int) animation.getAnimatedValue();
            view.requestLayout();
        });

        animator.addListener(new ValueAnimator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {}

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });

        animator.setDuration(300);
        animator.start();
    }

    private void collapseView(View view) {
        int initialHeight = view.getMeasuredHeight();

        ValueAnimator animator = ValueAnimator.ofInt(initialHeight, 0);
        animator.addUpdateListener(animation -> {
            view.getLayoutParams().height = (int) animation.getAnimatedValue();
            view.requestLayout();
            if ((int) animation.getAnimatedValue() == 0) view.setVisibility(View.GONE);
        });
        animator.setDuration(300);
        animator.start();
    }

    private void updateStarRating() {
        //to update the star images based on the selected rating
        if (selectedRating >= 1) star1.setImageResource(R.drawable.star_filled); else star1.setImageResource(R.drawable.star_empty);
        if (selectedRating >= 2) star2.setImageResource(R.drawable.star_filled); else star2.setImageResource(R.drawable.star_empty);
        if (selectedRating >= 3) star3.setImageResource(R.drawable.star_filled); else star3.setImageResource(R.drawable.star_empty);
        if (selectedRating >= 4) star4.setImageResource(R.drawable.star_filled); else star4.setImageResource(R.drawable.star_empty);
        if (selectedRating >= 5) star5.setImageResource(R.drawable.star_filled); else star5.setImageResource(R.drawable.star_empty);
    }

    private void submitFeedback() {
        String feedback = feedbackEditText.getText().toString().trim(); // Get the feedback text

        if (feedback.isEmpty()) {
            Toast.makeText(HelpActivity.this, "Please provide your comments.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save feedback to Firebase
        saveFeedbackToFirebase(feedback, selectedRating);

        new AlertDialog.Builder(HelpActivity.this)
                .setTitle("Thank You!")
                .setMessage("Thank you for your feedback!") // Message content
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        //to clear the feedback form after submission
                        feedbackEditText.setText("");
                        selectedRating = 0;
                        updateStarRating(); //to reset the stars rating
                    }
                })
                .show();
    }

    private void saveReportToFirebase(String topic, String email, String details) {
        String reportId = databaseRef.child("reports").push().getKey();

        if (reportId != null) {
            Map<String, Object> report = new HashMap<>();
            report.put("topic", topic);
            report.put("email", email);
            report.put("details", details);

            databaseRef.child("reports").child(reportId).setValue(report)
                    .addOnSuccessListener(aVoid -> Log.d("HelpActivity", "Report saved successfully"))
                    .addOnFailureListener(e -> Log.e("HelpActivity", "Failed to save report", e));
        }
    }

    private void saveFeedbackToFirebase(String feedback, int rating) {
        String feedbackId = databaseRef.child("feedbacks").push().getKey();

        if (feedbackId != null) {
            Map<String, Object> feedbackData = new HashMap<>();
            feedbackData.put("feedback", feedback);
            feedbackData.put("rating", rating);

            databaseRef.child("feedbacks").child(feedbackId).setValue(feedbackData)
                    .addOnSuccessListener(aVoid -> Log.d("HelpActivity", "Feedback saved successfully"))
                    .addOnFailureListener(e -> Log.e("HelpActivity", "Failed to save feedback", e));
        }
    }
}