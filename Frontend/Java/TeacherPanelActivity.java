package com.furkan.hocamprogram;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class TeacherPanelActivity extends AppCompatActivity {

    int teacherId;
    String teacherName;
    String teacherEmail;

    Button scheduleButton;
    Button paymentButton;
    Button lessonButton;
    Button studentButton;
    Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_teacher_panel);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        teacherId = getIntent().getIntExtra("teacherId", -1);
        teacherName = getIntent().getStringExtra("teacherName");
        teacherEmail = getIntent().getStringExtra("teacherEmail");

        if (teacherId == -1) {
            Toast.makeText(this, "Teacher ID not found", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        lessonButton = findViewById(R.id.LessonButton);
        studentButton = findViewById(R.id.StudentButton);
        scheduleButton = findViewById(R.id.SlotButton);
        paymentButton = findViewById(R.id.PaymentButton);

        // XML içinde id küçük harfle: logoutButton
        logoutButton = findViewById(R.id.logoutButton);

        studentButton.setOnClickListener(v -> {
            Intent intent = new Intent(TeacherPanelActivity.this, StudentActivity.class);
            intent.putExtra("teacherId", teacherId);
            intent.putExtra("teacherName", teacherName);
            intent.putExtra("teacherEmail", teacherEmail);
            startActivity(intent);
        });

        scheduleButton.setOnClickListener(v -> {
            Intent intent = new Intent(TeacherPanelActivity.this, ScheduleActivity.class);
            intent.putExtra("teacherId", teacherId);
            intent.putExtra("teacherName", teacherName);
            intent.putExtra("teacherEmail", teacherEmail);
            startActivity(intent);
        });

        paymentButton.setOnClickListener(v -> {
            Intent intent = new Intent(TeacherPanelActivity.this, PaymentActivity.class);
            intent.putExtra("teacherId", teacherId);
            intent.putExtra("teacherName", teacherName);
            intent.putExtra("teacherEmail", teacherEmail);
            startActivity(intent);
        });

        lessonButton.setOnClickListener(v -> {
            Intent intent = new Intent(TeacherPanelActivity.this, LessonsActivity.class);
            intent.putExtra("teacherId", teacherId);
            intent.putExtra("teacherName", teacherName);
            intent.putExtra("teacherEmail", teacherEmail);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(TeacherPanelActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}