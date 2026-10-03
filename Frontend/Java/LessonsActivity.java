package com.furkan.hocamprogram;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

public class LessonsActivity extends AppCompatActivity {

    LinearLayout lessonContainer;

    EditText lessonNameInput, typeInput;
    Button addLessonButton;

    int teacherId;

    String getLessonsUrl = "http://10.0.2.2/hocamprogram_api/get_teacher_lessons.php";
    String addLessonUrl = "http://10.0.2.2/hocamprogram_api/add_lesson.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lessons);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        lessonContainer = findViewById(R.id.lessonContainer);

        lessonNameInput = findViewById(R.id.lessonNameInput);
        typeInput = findViewById(R.id.typeInput);
        addLessonButton = findViewById(R.id.addLessonButton);

        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        teacherId = sharedPreferences.getInt("teacherId", -1);

        if (teacherId == -1) {
            teacherId = getIntent().getIntExtra("teacherId", -1);
        }

        if (teacherId == -1) {
            Toast.makeText(this, "Teacher ID not found", Toast.LENGTH_LONG).show();
            return;
        }

        loadTeacherLessons();

        addLessonButton.setOnClickListener(v -> addLesson());
    }

    private void loadTeacherLessons() {
        new Thread(() -> {
            try {
                String urlText = getLessonsUrl + "?teacherId=" +
                        URLEncoder.encode(String.valueOf(teacherId), "UTF-8");

                URL url = new URL(urlText);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                Scanner scanner = new Scanner(conn.getInputStream());
                scanner.useDelimiter("\\A");
                String response = scanner.hasNext() ? scanner.next() : "";
                scanner.close();

                JSONObject jsonObject = new JSONObject(response);
                boolean success = jsonObject.getBoolean("success");

                runOnUiThread(() -> lessonContainer.removeAllViews());

                if (success) {
                    JSONArray lessonList = jsonObject.getJSONArray("lessonList");

                    runOnUiThread(() -> {
                        for (int i = 0; i < lessonList.length(); i++) {
                            try {
                                JSONObject lesson = lessonList.getJSONObject(i);
                                addLessonView(lesson);
                            } catch (Exception e) {
                                Toast.makeText(
                                        LessonsActivity.this,
                                        e.getMessage(),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }

                        if (lessonList.length() == 0) {
                            Toast.makeText(
                                    LessonsActivity.this,
                                    "No lesson found",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });

                } else {
                    String message = jsonObject.getString("message");

                    runOnUiThread(() ->
                            Toast.makeText(
                                    LessonsActivity.this,
                                    message,
                                    Toast.LENGTH_LONG
                            ).show()
                    );
                }

                conn.disconnect();

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(
                                LessonsActivity.this,
                                "Connection error: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
            }
        }).start();
    }

    private void addLesson() {
        String lessonName = lessonNameInput.getText().toString().trim();
        String type = typeInput.getText().toString().trim();

        if (lessonName.isEmpty() || type.isEmpty()) {
            Toast.makeText(this, "Please fill lesson name and type", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                URL url = new URL(addLessonUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String postData =
                        "teacherId=" + URLEncoder.encode(String.valueOf(teacherId), "UTF-8") +
                                "&lessonName=" + URLEncoder.encode(lessonName, "UTF-8") +
                                "&type=" + URLEncoder.encode(type, "UTF-8");

                OutputStream os = conn.getOutputStream();
                os.write(postData.getBytes());
                os.flush();
                os.close();

                Scanner scanner = new Scanner(conn.getInputStream());
                scanner.useDelimiter("\\A");
                String response = scanner.hasNext() ? scanner.next() : "";
                scanner.close();

                JSONObject jsonObject = new JSONObject(response);
                boolean success = jsonObject.getBoolean("success");
                String message = jsonObject.getString("message");

                runOnUiThread(() -> {
                    Toast.makeText(LessonsActivity.this, message, Toast.LENGTH_SHORT).show();

                    if (success) {
                        lessonNameInput.setText("");
                        typeInput.setText("");

                        loadTeacherLessons();
                    }
                });

                conn.disconnect();

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(
                                LessonsActivity.this,
                                "Add lesson error: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
            }
        }).start();
    }

    private void addLessonView(JSONObject lesson) throws Exception {
        String teacherName = String.valueOf(lesson.get("fullName"));
        String lessonName = String.valueOf(lesson.get("LessonName"));
        String type = String.valueOf(lesson.get("Type"));

        TextView lessonView = new TextView(this);

        String text =
                "Teacher: " + teacherName + "\n" +
                        "Lesson: " + lessonName + "\n" +
                        "Type: " + type;

        lessonView.setText(text);
        lessonView.setTextSize(16);
        lessonView.setTextColor(0xFF000000);
        lessonView.setBackgroundColor(0xFFFFFFFF);
        lessonView.setPadding(24, 24, 24, 24);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        params.setMargins(0, 0, 0, 20);
        lessonView.setLayoutParams(params);

        lessonContainer.addView(lessonView);
    }
}