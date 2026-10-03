package com.furkan.hocamprogram;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Scanner;

public class ScheduleActivity extends AppCompatActivity {

    LinearLayout slotContainer;
    Button addSlotButton, filterDateButton, clearFilterButton;

    String getSlotsUrl = "http://10.0.2.2/hocamprogram_api/get_slots.php";
    String updateAttendanceUrl = "http://10.0.2.2/hocamprogram_api/update_attendance.php";

    String getStudentsUrl = "http://10.0.2.2/hocamprogram_api/get_students.php";
    String getLessonsUrl = "http://10.0.2.2/hocamprogram_api/get_teacher_lessons.php";
    String addSlotUrl = "http://10.0.2.2/hocamprogram_api/add_slot.php";

    int teacherId;
    String selectedFilterDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        slotContainer = findViewById(R.id.slotContainer);
        addSlotButton = findViewById(R.id.addSlotButton);
        filterDateButton = findViewById(R.id.filterDateButton);
        clearFilterButton = findViewById(R.id.clearFilterButton);

        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        teacherId = sharedPreferences.getInt("teacherId", -1);

        if (teacherId == -1) {
            teacherId = getIntent().getIntExtra("teacherId", -1);
        }

        if (teacherId == -1) {
            Toast.makeText(this, "Teacher ID not found. Please login again.", Toast.LENGTH_LONG).show();
            return;
        }

        loadSlots();

        addSlotButton.setOnClickListener(v -> loadStudentsAndLessonsForAddSlot());

        filterDateButton.setOnClickListener(v -> showFilterDatePicker());

        clearFilterButton.setOnClickListener(v -> {
            selectedFilterDate = "";
            filterDateButton.setText("Filter by Date");
            loadSlots();
        });
    }

    private void showFilterDatePicker() {
        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                ScheduleActivity.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    int realMonth = selectedMonth + 1;

                    String monthText;
                    if (realMonth < 10) {
                        monthText = "0" + realMonth;
                    } else {
                        monthText = String.valueOf(realMonth);
                    }

                    String dayText;
                    if (selectedDay < 10) {
                        dayText = "0" + selectedDay;
                    } else {
                        dayText = String.valueOf(selectedDay);
                    }

                    selectedFilterDate = selectedYear + "-" + monthText + "-" + dayText;
                    filterDateButton.setText("Date: " + selectedFilterDate);

                    loadSlots();
                },
                year,
                month,
                day
        );

        datePickerDialog.show();
    }

    private void loadSlots() {
        new Thread(() -> {
            try {
                String fullUrl = getSlotsUrl + "?teacherId=" +
                        URLEncoder.encode(String.valueOf(teacherId), "UTF-8");

                if (!selectedFilterDate.isEmpty()) {
                    fullUrl += "&date=" + URLEncoder.encode(selectedFilterDate, "UTF-8");
                }

                URL url = new URL(fullUrl);
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
                    JSONArray slots = jsonObject.getJSONArray("slots");

                    runOnUiThread(() -> {
                        for (int i = 0; i < slots.length(); i++) {
                            try {
                                JSONObject slot = slots.getJSONObject(i);
                                addSlotView(slot);
                            } catch (Exception e) {
                                Toast.makeText(
                                        ScheduleActivity.this,
                                        "Slot view error: " + e.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }

                        if (slots.length() == 0) {
                            Toast.makeText(
                                    ScheduleActivity.this,
                                    "No slots found for this date",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });

                } else {
                    String message = jsonObject.getString("message");

                    runOnUiThread(() ->
                            Toast.makeText(
                                    ScheduleActivity.this,
                                    message,
                                    Toast.LENGTH_LONG
                            ).show()
                    );
                }

                conn.disconnect();

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(
                                ScheduleActivity.this,
                                "Connection error: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
            }
        }).start();
    }

    private void addSlotView(JSONObject slot) throws Exception {
        int slotId = slot.getInt("slotId");

        String slotDate = String.valueOf(slot.get("slotDate"));
        String duration = String.valueOf(slot.get("duration"));
        String attendance = String.valueOf(slot.get("attendance"));
        String paymentInfo = String.valueOf(slot.get("paymentInfo"));
        String period = String.valueOf(slot.get("period"));
        String lessonName = String.valueOf(slot.get("LessonName"));
        String studentName = String.valueOf(slot.get("studentName"));

        String attendanceText = attendance.equals("1") ? "Present" : "Absent";
        String paymentText = paymentInfo.equals("1") ? "Paid" : "Unpaid";

        TextView slotView = new TextView(this);

        String text =
                "Date: " + slotDate + "\n" +
                        "Student: " + studentName + "\n" +
                        "Lesson: " + lessonName + "\n" +
                        "Duration: " + duration + " minutes\n" +
                        "Period: " + period + "\n" +
                        "Attendance: " + attendanceText + "\n" +
                        "Payment: " + paymentText;

        slotView.setText(text);
        slotView.setTextSize(16);
        slotView.setTextColor(0xFF000000);
        slotView.setBackgroundColor(0xFFFFFFFF);
        slotView.setPadding(24, 24, 24, 24);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        params.setMargins(0, 0, 0, 20);
        slotView.setLayoutParams(params);

        slotView.setOnClickListener(v -> showAttendanceDialog(slotId));

        slotContainer.addView(slotView);
    }

    private void showAttendanceDialog(int slotId) {
        String[] options = {"Present", "Absent"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Attendance");

        builder.setItems(options, (dialog, which) -> {
            int attendanceValue = which == 0 ? 1 : 0;
            updateAttendance(slotId, attendanceValue);
        });

        builder.show();
    }

    private void updateAttendance(int slotId, int attendanceValue) {
        new Thread(() -> {
            try {
                URL url = new URL(updateAttendanceUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String postData =
                        "slotId=" + URLEncoder.encode(String.valueOf(slotId), "UTF-8") +
                                "&attendance=" + URLEncoder.encode(String.valueOf(attendanceValue), "UTF-8");

                OutputStream os = conn.getOutputStream();
                os.write(postData.getBytes());
                os.flush();
                os.close();

                Scanner scanner = new Scanner(conn.getInputStream());
                scanner.useDelimiter("\\A");
                String response = scanner.hasNext() ? scanner.next() : "";
                scanner.close();

                JSONObject jsonObject = new JSONObject(response);
                String message = jsonObject.getString("message");
                boolean success = jsonObject.getBoolean("success");

                runOnUiThread(() -> {
                    Toast.makeText(ScheduleActivity.this, message, Toast.LENGTH_SHORT).show();

                    if (success) {
                        loadSlots();
                    }
                });

                conn.disconnect();

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(
                                ScheduleActivity.this,
                                "Update error: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
            }
        }).start();
    }

    private void loadStudentsAndLessonsForAddSlot() {
        new Thread(() -> {
            try {
                JSONArray students = getJsonArrayFromUrl(
                        getStudentsUrl + "?teacherId=" +
                                URLEncoder.encode(String.valueOf(teacherId), "UTF-8"),
                        "students"
                );

                JSONArray lessons = getJsonArrayFromUrl(
                        getLessonsUrl + "?teacherId=" +
                                URLEncoder.encode(String.valueOf(teacherId), "UTF-8"),
                        "lessonList"
                );

                runOnUiThread(() -> showAddSlotDialog(students, lessons));

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(
                                ScheduleActivity.this,
                                "Load error: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
            }
        }).start();
    }

    private JSONArray getJsonArrayFromUrl(String urlString, String arrayName) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");

        Scanner scanner = new Scanner(conn.getInputStream());
        scanner.useDelimiter("\\A");
        String response = scanner.hasNext() ? scanner.next() : "";
        scanner.close();

        JSONObject jsonObject = new JSONObject(response);
        boolean success = jsonObject.getBoolean("success");

        if (!success) {
            throw new Exception(jsonObject.getString("message"));
        }

        conn.disconnect();

        return jsonObject.getJSONArray(arrayName);
    }

    private void showAddSlotDialog(JSONArray students, JSONArray lessons) {
        try {
            if (students.length() == 0) {
                Toast.makeText(this, "No students found", Toast.LENGTH_LONG).show();
                return;
            }

            if (lessons.length() == 0) {
                Toast.makeText(this, "No lessons found", Toast.LENGTH_LONG).show();
                return;
            }

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(40, 20, 40, 10);

            TextView studentText = new TextView(this);
            studentText.setText("Select Student");
            studentText.setTextSize(16);

            Button studentButton = new Button(this);
            studentButton.setText("Choose Student");

            TextView lessonText = new TextView(this);
            lessonText.setText("Select Lesson");
            lessonText.setTextSize(16);

            Button lessonButton = new Button(this);
            lessonButton.setText("Choose Lesson");

            TextView dateText = new TextView(this);
            dateText.setText("Slot Date");
            dateText.setTextSize(16);

            EditText dateInput = new EditText(this);
            dateInput.setHint("Select date");
            dateInput.setFocusable(false);
            dateInput.setClickable(true);

            dateInput.setOnClickListener(v -> {
                Calendar calendar = Calendar.getInstance();

                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        ScheduleActivity.this,
                        (view, selectedYear, selectedMonth, selectedDay) -> {
                            int realMonth = selectedMonth + 1;

                            String monthText;
                            if (realMonth < 10) {
                                monthText = "0" + realMonth;
                            } else {
                                monthText = String.valueOf(realMonth);
                            }

                            String dayText;
                            if (selectedDay < 10) {
                                dayText = "0" + selectedDay;
                            } else {
                                dayText = String.valueOf(selectedDay);
                            }

                            String selectedDate = selectedYear + "-" + monthText + "-" + dayText;
                            dateInput.setText(selectedDate);
                        },
                        year,
                        month,
                        day
                );

                datePickerDialog.show();
            });

            TextView timeText = new TextView(this);
            timeText.setText("Time");
            timeText.setTextSize(16);

            EditText timeInput = new EditText(this);
            timeInput.setHint("14:00:00");

            TextView durationText = new TextView(this);
            durationText.setText("Duration");
            durationText.setTextSize(16);

            EditText durationInput = new EditText(this);
            durationInput.setHint("60");
            durationInput.setInputType(InputType.TYPE_CLASS_NUMBER);

            TextView periodText = new TextView(this);
            periodText.setText("Period");
            periodText.setTextSize(16);

            EditText periodInput = new EditText(this);
            periodInput.setHint("June");

            final int[] selectedStudentId = {-1};
            final int[] selectedLessonId = {-1};

            studentButton.setOnClickListener(v -> {
                try {
                    String[] studentNames = new String[students.length()];

                    for (int i = 0; i < students.length(); i++) {
                        JSONObject student = students.getJSONObject(i);
                        studentNames[i] = String.valueOf(student.get("fullName"));
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Choose Student");

                    builder.setItems(studentNames, (dialog, which) -> {
                        try {
                            JSONObject selectedStudent = students.getJSONObject(which);
                            selectedStudentId[0] = selectedStudent.getInt("id");
                            studentButton.setText(String.valueOf(selectedStudent.get("fullName")));
                        } catch (Exception e) {
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    builder.show();

                } catch (Exception e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            lessonButton.setOnClickListener(v -> {
                try {
                    String[] lessonNames = new String[lessons.length()];

                    for (int i = 0; i < lessons.length(); i++) {
                        JSONObject lesson = lessons.getJSONObject(i);
                        lessonNames[i] = String.valueOf(lesson.get("LessonName"));
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Choose Lesson");

                    builder.setItems(lessonNames, (dialog, which) -> {
                        try {
                            JSONObject selectedLesson = lessons.getJSONObject(which);
                            selectedLessonId[0] = selectedLesson.getInt("lessonId");
                            lessonButton.setText(String.valueOf(selectedLesson.get("LessonName")));
                        } catch (Exception e) {
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    builder.show();

                } catch (Exception e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            layout.addView(studentText);
            layout.addView(studentButton);

            layout.addView(lessonText);
            layout.addView(lessonButton);

            layout.addView(dateText);
            layout.addView(dateInput);

            layout.addView(timeText);
            layout.addView(timeInput);

            layout.addView(durationText);
            layout.addView(durationInput);

            layout.addView(periodText);
            layout.addView(periodInput);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Add Slot");
            builder.setView(layout);

            builder.setPositiveButton("Add", null);
            builder.setNegativeButton("Cancel", null);

            AlertDialog dialog = builder.create();

            dialog.setOnShowListener(d -> {
                Button addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

                addButton.setOnClickListener(v -> {
                    String selectedDate = dateInput.getText().toString().trim();
                    String selectedTime = timeInput.getText().toString().trim();
                    String duration = durationInput.getText().toString().trim();
                    String period = periodInput.getText().toString().trim();

                    String slotDate = selectedDate + " " + selectedTime;

                    if (selectedStudentId[0] == -1) {
                        Toast.makeText(this, "Please choose a student", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (selectedLessonId[0] == -1) {
                        Toast.makeText(this, "Please choose a lesson", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (selectedDate.isEmpty() || selectedTime.isEmpty() || duration.isEmpty() || period.isEmpty()) {
                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    addSlot(
                            selectedStudentId[0],
                            selectedLessonId[0],
                            slotDate,
                            duration,
                            period
                    );

                    dialog.dismiss();
                });
            });

            dialog.show();

        } catch (Exception e) {
            Toast.makeText(this, "Dialog error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void addSlot(int studentId, int lessonId, String slotDate, String duration, String period) {
        new Thread(() -> {
            try {
                URL url = new URL(addSlotUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String postData =
                        "studentId=" + URLEncoder.encode(String.valueOf(studentId), "UTF-8") +
                                "&lessonId=" + URLEncoder.encode(String.valueOf(lessonId), "UTF-8") +
                                "&slotDate=" + URLEncoder.encode(slotDate, "UTF-8") +
                                "&duration=" + URLEncoder.encode(duration, "UTF-8") +
                                "&period=" + URLEncoder.encode(period, "UTF-8");

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
                    Toast.makeText(ScheduleActivity.this, message, Toast.LENGTH_SHORT).show();

                    if (success) {
                        loadSlots();
                    }
                });

                conn.disconnect();

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(
                                ScheduleActivity.this,
                                "Add slot error: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
            }
        }).start();
    }
}