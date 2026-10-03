package com.furkan.hocamprogram;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class StudentActivity extends AppCompatActivity {

    int teacherId;
    String teacherName;
    String teacherEmail;

    Button btnShowAdd, btnShowDelete, btnShowUpdate;

    LinearLayout layoutAddStudent, layoutDeleteStudent, layoutUpdateStudent;
    LinearLayout updateStudentListContainer, deleteStudentListContainer;

    EditText etStudentId, etFullName, etEmail, etPhoneNumber, etStatus;
    EditText etReferenceName, etPredictedHours, etHourlyWage, etParentId;
    EditText etSlotId;
    Button btnAddStudent;

    EditText etUpdateStudentId, etUpdateFullName, etUpdateEmail, etUpdatePhoneNumber;
    EditText etUpdateStatus, etUpdateReferenceName, etUpdatePredictedHours;
    EditText etUpdateHourlyWage, etUpdateParentId;
    Button btnUpdateStudent;

    String addUrl = "http://10.0.2.2/hocamprogram_api/add_student.php";
    String deleteUrl = "http://10.0.2.2/hocamprogram_api/delete_student.php";
    String updateUrl = "http://10.0.2.2/hocamprogram_api/update_student.php";
    String getStudentsUrl = "http://10.0.2.2/hocamprogram_api/get_students_for_update_delete.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

        teacherId = getIntent().getIntExtra("teacherId", -1);
        teacherName = getIntent().getStringExtra("teacherName");
        teacherEmail = getIntent().getStringExtra("teacherEmail");

        if (teacherId == -1) {
            Toast.makeText(this, "Teacher ID not found", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        btnShowAdd = findViewById(R.id.btnShowAdd);
        btnShowDelete = findViewById(R.id.btnShowDelete);
        btnShowUpdate = findViewById(R.id.btnShowUpdate);

        layoutAddStudent = findViewById(R.id.layoutAddStudent);
        layoutDeleteStudent = findViewById(R.id.layoutDeleteStudent);
        layoutUpdateStudent = findViewById(R.id.layoutUpdateStudent);

        updateStudentListContainer = findViewById(R.id.updateStudentListContainer);
        deleteStudentListContainer = findViewById(R.id.deleteStudentListContainer);

        etStudentId = findViewById(R.id.etStudentId);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etStatus = findViewById(R.id.etStatus);
        etReferenceName = findViewById(R.id.etReferenceName);
        etPredictedHours = findViewById(R.id.etPredictedHours);
        etHourlyWage = findViewById(R.id.etHourlyWage);
        etParentId = findViewById(R.id.etParentId);
        etSlotId = findViewById(R.id.etSlotId);
        btnAddStudent = findViewById(R.id.btnAddStudent);

        etUpdateStudentId = findViewById(R.id.etUpdateStudentId);
        etUpdateFullName = findViewById(R.id.etUpdateFullName);
        etUpdateEmail = findViewById(R.id.etUpdateEmail);
        etUpdatePhoneNumber = findViewById(R.id.etUpdatePhoneNumber);
        etUpdateStatus = findViewById(R.id.etUpdateStatus);
        etUpdateReferenceName = findViewById(R.id.etUpdateReferenceName);
        etUpdatePredictedHours = findViewById(R.id.etUpdatePredictedHours);
        etUpdateHourlyWage = findViewById(R.id.etUpdateHourlyWage);
        etUpdateParentId = findViewById(R.id.etUpdateParentId);
        btnUpdateStudent = findViewById(R.id.btnUpdateStudent);

        etUpdateStudentId.setEnabled(false);

        btnShowAdd.setOnClickListener(v -> showOnly(layoutAddStudent));

        btnShowDelete.setOnClickListener(v -> {
            showOnly(layoutDeleteStudent);
            loadStudentsForDelete();
        });

        btnShowUpdate.setOnClickListener(v -> {
            showOnly(layoutUpdateStudent);
            loadStudentsForUpdate();
        });

        btnAddStudent.setOnClickListener(v -> addStudentToDatabase());
        btnUpdateStudent.setOnClickListener(v -> updateStudentToDatabase());
    }

    private void showOnly(LinearLayout selectedLayout) {
        layoutAddStudent.setVisibility(View.GONE);
        layoutDeleteStudent.setVisibility(View.GONE);
        layoutUpdateStudent.setVisibility(View.GONE);

        selectedLayout.setVisibility(View.VISIBLE);
    }

    private void addStudentToDatabase() {
        String studentId = etStudentId.getText().toString().trim();
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String status = etStatus.getText().toString().trim();
        String referenceName = etReferenceName.getText().toString().trim();
        String predictedHours = etPredictedHours.getText().toString().trim();
        String hourlyWage = etHourlyWage.getText().toString().trim();
        String parentId = etParentId.getText().toString().trim();
        String slotId = etSlotId.getText().toString().trim();

        if (studentId.isEmpty() || fullName.isEmpty() || email.isEmpty()
                || status.isEmpty() || predictedHours.isEmpty()
                || hourlyWage.isEmpty() || parentId.isEmpty()
                || slotId.isEmpty()) {

            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest request = new StringRequest(
                Request.Method.POST,
                addUrl,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);

                        boolean success = jsonObject.getBoolean("success");
                        String message = jsonObject.getString("message");

                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

                        if (success) {
                            clearAddFields();
                        }

                    } catch (Exception e) {
                        Toast.makeText(this, "JSON error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> Toast.makeText(this, "Volley error: " + error.getMessage(), Toast.LENGTH_LONG).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("teacherId", String.valueOf(teacherId));
                params.put("slotId", slotId);

                params.put("id", studentId);
                params.put("fullName", fullName);
                params.put("email", email);
                params.put("phoneNumber", phoneNumber);
                params.put("status", status);

                // Optional field
                params.put("refaransName", referenceName);

                params.put("predicted_lesson_hours", predictedHours);
                params.put("hourlyWage", hourlyWage);
                params.put("idParent", parentId);

                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void loadStudentsForUpdate() {
        updateStudentListContainer.removeAllViews();

        String url = getStudentsUrl + "?teacherId=" + teacherId;

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean success = jsonObject.getBoolean("success");

                        updateStudentListContainer.removeAllViews();

                        if (success) {
                            JSONArray students = jsonObject.getJSONArray("students");

                            if (students.length() == 0) {
                                Toast.makeText(this, "No student found", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            for (int i = 0; i < students.length(); i++) {
                                JSONObject student = students.getJSONObject(i);
                                addStudentCardForUpdate(student);
                            }

                        } else {
                            String message = jsonObject.getString("message");
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                        }

                    } catch (Exception e) {
                        Toast.makeText(this, "JSON error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> Toast.makeText(this, "Student list error: " + error.getMessage(), Toast.LENGTH_LONG).show()
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void loadStudentsForDelete() {
        deleteStudentListContainer.removeAllViews();

        String url = getStudentsUrl + "?teacherId=" + teacherId;

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean success = jsonObject.getBoolean("success");

                        deleteStudentListContainer.removeAllViews();

                        if (success) {
                            JSONArray students = jsonObject.getJSONArray("students");

                            if (students.length() == 0) {
                                Toast.makeText(this, "No student found", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            for (int i = 0; i < students.length(); i++) {
                                JSONObject student = students.getJSONObject(i);
                                addStudentCardForDelete(student);
                            }

                        } else {
                            String message = jsonObject.getString("message");
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                        }

                    } catch (Exception e) {
                        Toast.makeText(this, "JSON error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> Toast.makeText(this, "Student list error: " + error.getMessage(), Toast.LENGTH_LONG).show()
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void addStudentCardForUpdate(JSONObject student) throws Exception {
        String studentId = student.optString("id", "");
        String fullName = student.optString("fullName", "");
        String email = student.optString("email", "");
        String phoneNumber = student.optString("phoneNumber", "");
        String status = student.optString("status", "");
        String referenceName = student.optString("refaransName", "");
        String predictedHours = student.optString("predicted_lesson_hours", "");
        String hourlyWage = student.optString("hourlyWage", "");
        String parentId = student.optString("idParent", "");

        TextView studentCard = new TextView(this);

        String text =
                "Student ID: " + studentId + "\n" +
                        "Name: " + fullName + "\n" +
                        "Email: " + email + "\n" +
                        "Phone: " + phoneNumber + "\n\n" +
                        "Tap to edit";

        studentCard.setText(text);
        studentCard.setTextSize(16);
        studentCard.setTextColor(0xFF000000);
        studentCard.setBackgroundColor(0xFFFFFFFF);
        studentCard.setPadding(24, 24, 24, 24);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        params.setMargins(0, 0, 0, 20);
        studentCard.setLayoutParams(params);

        studentCard.setOnClickListener(v -> {
            etUpdateStudentId.setText(studentId);
            etUpdateFullName.setText(fullName);
            etUpdateEmail.setText(email);
            etUpdatePhoneNumber.setText(phoneNumber);
            etUpdateStatus.setText(status);
            etUpdateReferenceName.setText(referenceName);
            etUpdatePredictedHours.setText(predictedHours);
            etUpdateHourlyWage.setText(hourlyWage);
            etUpdateParentId.setText(parentId);

            Toast.makeText(this, fullName + " selected", Toast.LENGTH_SHORT).show();
        });

        updateStudentListContainer.addView(studentCard);
    }

    private void addStudentCardForDelete(JSONObject student) throws Exception {
        String studentId = student.optString("id", "");
        String fullName = student.optString("fullName", "");
        String email = student.optString("email", "");
        String phoneNumber = student.optString("phoneNumber", "");

        TextView studentCard = new TextView(this);

        String text =
                "Student ID: " + studentId + "\n" +
                        "Name: " + fullName + "\n" +
                        "Email: " + email + "\n" +
                        "Phone: " + phoneNumber + "\n\n" +
                        "Tap to delete";

        studentCard.setText(text);
        studentCard.setTextSize(16);
        studentCard.setTextColor(0xFF000000);
        studentCard.setBackgroundColor(0xFFFFFFFF);
        studentCard.setPadding(24, 24, 24, 24);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        params.setMargins(0, 0, 0, 20);
        studentCard.setLayoutParams(params);

        studentCard.setOnClickListener(v -> showDeleteDialog(studentId, fullName));

        deleteStudentListContainer.addView(studentCard);
    }

    private void showDeleteDialog(String studentId, String fullName) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Student")
                .setMessage("Are you sure you want to delete " + fullName + "?")
                .setPositiveButton("Yes", (dialog, which) -> deleteStudentFromDatabase(studentId))
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteStudentFromDatabase(String studentId) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                deleteUrl,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);

                        boolean success = jsonObject.getBoolean("success");
                        String message = jsonObject.getString("message");

                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

                        if (success) {
                            loadStudentsForDelete();
                            clearUpdateFields();
                        }

                    } catch (Exception e) {
                        Toast.makeText(this, "JSON error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> Toast.makeText(this, "Volley error: " + error.getMessage(), Toast.LENGTH_LONG).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("teacherId", String.valueOf(teacherId));
                params.put("id", studentId);

                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void updateStudentToDatabase() {
        String studentId = etUpdateStudentId.getText().toString().trim();
        String fullName = etUpdateFullName.getText().toString().trim();
        String email = etUpdateEmail.getText().toString().trim();
        String phoneNumber = etUpdatePhoneNumber.getText().toString().trim();
        String status = etUpdateStatus.getText().toString().trim();
        String referenceName = etUpdateReferenceName.getText().toString().trim();
        String predictedHours = etUpdatePredictedHours.getText().toString().trim();
        String hourlyWage = etUpdateHourlyWage.getText().toString().trim();
        String parentId = etUpdateParentId.getText().toString().trim();

        if (studentId.isEmpty()) {
            Toast.makeText(this, "Please select a student from list", Toast.LENGTH_SHORT).show();
            return;
        }

        if (fullName.isEmpty() || email.isEmpty()
                || status.isEmpty() || predictedHours.isEmpty()
                || hourlyWage.isEmpty() || parentId.isEmpty()) {

            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest request = new StringRequest(
                Request.Method.POST,
                updateUrl,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);

                        boolean success = jsonObject.getBoolean("success");
                        String message = jsonObject.getString("message");

                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

                        if (success) {
                            clearUpdateFields();
                            loadStudentsForUpdate();
                        }

                    } catch (Exception e) {
                        Toast.makeText(this, "JSON error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> Toast.makeText(this, "Volley error: " + error.getMessage(), Toast.LENGTH_LONG).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("teacherId", String.valueOf(teacherId));

                params.put("id", studentId);
                params.put("fullName", fullName);
                params.put("email", email);
                params.put("phoneNumber", phoneNumber);
                params.put("status", status);

                // Optional field
                params.put("refaransName", referenceName);

                params.put("predicted_lesson_hours", predictedHours);
                params.put("hourlyWage", hourlyWage);
                params.put("idParent", parentId);

                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void clearAddFields() {
        etStudentId.setText("");
        etFullName.setText("");
        etEmail.setText("");
        etPhoneNumber.setText("");
        etStatus.setText("");
        etReferenceName.setText("");
        etPredictedHours.setText("");
        etHourlyWage.setText("");
        etParentId.setText("");
        etSlotId.setText("");
    }

    private void clearUpdateFields() {
        etUpdateStudentId.setText("");
        etUpdateFullName.setText("");
        etUpdateEmail.setText("");
        etUpdatePhoneNumber.setText("");
        etUpdateStatus.setText("");
        etUpdateReferenceName.setText("");
        etUpdatePredictedHours.setText("");
        etUpdateHourlyWage.setText("");
        etUpdateParentId.setText("");
    }
}