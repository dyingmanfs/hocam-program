package com.furkan.hocamprogram;

import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

public class LoginActivity extends AppCompatActivity {

    EditText emailInput, passwordInput;
    Button enterButton;

    String loginUrl = "http://10.0.2.2/hocamprogram_api/login.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailInput = findViewById(R.id.editTextTextEmailAddress);
        passwordInput = findViewById(R.id.editTextTextPassword);
        enterButton = findViewById(R.id.enterbutton);

        enterButton.setOnClickListener(v -> checkLogin());

        passwordInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                checkLogin();
                return true;
            }
            return false;
        });
    }

    private void checkLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        loginFromDatabase(email, password);
    }

    private void loginFromDatabase(String email, String password) {
        new Thread(() -> {
            try {
                URL url = new URL(loginUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String postData =
                        "email=" + URLEncoder.encode(email, "UTF-8") +
                                "&password=" + URLEncoder.encode(password, "UTF-8");

                OutputStream os = conn.getOutputStream();
                os.write(postData.getBytes());
                os.flush();
                os.close();

                Scanner scanner = new Scanner(conn.getInputStream());
                scanner.useDelimiter("\\A");

                String response;
                if (scanner.hasNext()) {
                    response = scanner.next();
                } else {
                    response = "";
                }

                scanner.close();

                JSONObject jsonObject = new JSONObject(response);

                boolean success = jsonObject.getBoolean("success");
                String message = jsonObject.getString("message");

                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();

                    if (success) {
                        try {
                            JSONObject teacher = jsonObject.getJSONObject("teacher");

                            int teacherId = teacher.getInt("id");
                            String teacherName = teacher.getString("fullName");
                            String teacherEmail = teacher.getString("email");

                            Intent intent = new Intent(LoginActivity.this, TeacherPanelActivity.class);
                            intent.putExtra("teacherId", teacherId);
                            intent.putExtra("teacherName", teacherName);
                            intent.putExtra("teacherEmail", teacherEmail);

                            startActivity(intent);
                            finish();

                        } catch (Exception e) {
                            Toast.makeText(
                                    LoginActivity.this,
                                    "Teacher data error: " + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
                });

                conn.disconnect();

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(
                            LoginActivity.this,
                            "Connection error: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        }).start();
    }
}