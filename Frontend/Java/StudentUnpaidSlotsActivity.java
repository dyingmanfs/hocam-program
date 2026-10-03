package com.furkan.hocamprogram;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import java.util.ArrayList;
import java.util.Scanner;

public class StudentUnpaidSlotsActivity extends AppCompatActivity {

    int teacherId;
    String selectedStudentId = "";
    String selectedStudentName = "";

    TextView titleText;
    Spinner studentSpinner;
    Button showUnpaidSlotsButton;
    LinearLayout slotContainer;

    ArrayList<String> studentIds = new ArrayList<>();
    ArrayList<String> studentNames = new ArrayList<>();

    String BASE_URL = "http://10.0.2.2/hocamprogram_api/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_unpaid_slots);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        teacherId = getIntent().getIntExtra("teacherId", -1);

        titleText = findViewById(R.id.titleText);
        studentSpinner = findViewById(R.id.studentSpinner);
        showUnpaidSlotsButton = findViewById(R.id.showUnpaidSlotsButton);
        slotContainer = findViewById(R.id.slotContainer);

        if (teacherId == -1) {
            Toast.makeText(this, "Teacher ID not found", Toast.LENGTH_LONG).show();
            return;
        }

        loadStudents();

        studentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (studentIds.size() > 0) {
                    selectedStudentId = studentIds.get(position);
                    selectedStudentName = studentNames.get(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        showUnpaidSlotsButton.setOnClickListener(v -> {
            if (selectedStudentId.equals("")) {
                Toast.makeText(this, "Please select a student", Toast.LENGTH_SHORT).show();
                return;
            }

            titleText.setText(selectedStudentName + " - Unpaid Slots");
            loadStudentUnpaidSlots();
        });
    }

    private void loadStudents() {
        new Thread(() -> {
            try {
                String urlText = BASE_URL + "get_students.php?teacherId="
                        + URLEncoder.encode(String.valueOf(teacherId), "UTF-8");

                URL url = new URL(urlText);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                Scanner scanner = new Scanner(conn.getInputStream());
                scanner.useDelimiter("\\A");
                String response = scanner.hasNext() ? scanner.next() : "";
                scanner.close();

                JSONObject jsonObject = new JSONObject(response);
                boolean success = jsonObject.getBoolean("success");

                if (success) {
                    JSONArray students = jsonObject.getJSONArray("students");

                    studentIds.clear();
                    studentNames.clear();

                    for (int i = 0; i < students.length(); i++) {
                        JSONObject student = students.getJSONObject(i);

                        studentIds.add(student.getString("id"));
                        studentNames.add(student.getString("fullName"));
                    }

                    runOnUiThread(() -> {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                StudentUnpaidSlotsActivity.this,
                                android.R.layout.simple_spinner_item,
                                studentNames
                        );

                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        studentSpinner.setAdapter(adapter);

                        if (studentNames.size() == 0) {
                            Toast.makeText(this, "No student found", Toast.LENGTH_SHORT).show();
                        } else {
                            selectedStudentId = studentIds.get(0);
                            selectedStudentName = studentNames.get(0);
                        }
                    });

                } else {
                    String message = jsonObject.getString("message");

                    runOnUiThread(() ->
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    );
                }

                conn.disconnect();

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(
                                StudentUnpaidSlotsActivity.this,
                                "Student list error: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
            }
        }).start();
    }

    private void loadStudentUnpaidSlots() {
        new Thread(() -> {
            try {
                String urlText = BASE_URL + "get_student_unpaid_slots.php?teacherId="
                        + URLEncoder.encode(String.valueOf(teacherId), "UTF-8")
                        + "&studentId=" + URLEncoder.encode(selectedStudentId, "UTF-8");

                URL url = new URL(urlText);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                Scanner scanner = new Scanner(conn.getInputStream());
                scanner.useDelimiter("\\A");
                String response = scanner.hasNext() ? scanner.next() : "";
                scanner.close();

                JSONObject jsonObject = new JSONObject(response);
                boolean success = jsonObject.getBoolean("success");

                runOnUiThread(() -> slotContainer.removeAllViews());

                if (success) {
                    JSONArray slotList = jsonObject.getJSONArray("slotList");

                    runOnUiThread(() -> {
                        for (int i = 0; i < slotList.length(); i++) {
                            try {
                                JSONObject slot = slotList.getJSONObject(i);
                                addSlotView(slot);
                            } catch (Exception e) {
                                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        if (slotList.length() == 0) {
                            Toast.makeText(this, "No unpaid slots found", Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    String message = jsonObject.getString("message");

                    runOnUiThread(() ->
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    );
                }

                conn.disconnect();

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(
                                StudentUnpaidSlotsActivity.this,
                                "Connection error: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
            }
        }).start();
    }

    private void addSlotView(JSONObject slot) throws Exception {
        String slotId = slot.getString("slotId");
        String lessonName = slot.getString("LessonName");
        String slotDate = slot.getString("slotDate");
        String duration = slot.getString("duration");
        String hourlyWage = slot.getString("hourlyWage");
        String amount = slot.getString("amount");

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(0xFFFFFFFF);
        card.setPadding(24, 24, 24, 24);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        cardParams.setMargins(0, 0, 0, 20);
        card.setLayoutParams(cardParams);

        TextView slotText = new TextView(this);
        slotText.setText(
                "Lesson: " + lessonName + "\n" +
                        "Date: " + slotDate + "\n" +
                        "Duration: " + duration + " minutes\n" +
                        "Hourly Wage: " + hourlyWage + " TL\n" +
                        "Amount: " + amount + " TL"
        );

        slotText.setTextSize(16);
        slotText.setTextColor(0xFF000000);

        Button paidButton = new Button(this);
        paidButton.setText("✓ Mark as Paid");

        paidButton.setOnClickListener(v -> updatePayment(slotId));

        card.addView(slotText);
        card.addView(paidButton);

        slotContainer.addView(card);
    }

    private void updatePayment(String slotId) {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "update_payment.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String postData = "slotId=" + URLEncoder.encode(slotId, "UTF-8");

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
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

                    if (success) {
                        loadStudentUnpaidSlots();
                    }
                });

                conn.disconnect();

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(
                                StudentUnpaidSlotsActivity.this,
                                "Update error: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
            }
        }).start();
    }
}