package com.furkan.hocamprogram;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

public class PaymentActivity extends AppCompatActivity {

    Button listUnpaidButton, listPaidButton, listStudentPaymentsButton;
    LinearLayout resultContainer;

    int teacherId;

    String unpaidDebtsUrl = "http://10.0.2.2/hocamprogram_api/getpaymentinfo.php";
    String paidPaymentsUrl = "http://10.0.2.2/hocamprogram_api/get_paid_payments.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        listUnpaidButton = findViewById(R.id.listUnpaidButton);
        listStudentPaymentsButton = findViewById(R.id.listStudentPaymentsButton);
        resultContainer = findViewById(R.id.resultContainer);

        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        teacherId = sharedPreferences.getInt("teacherId", -1);

        if (teacherId == -1) {
            teacherId = getIntent().getIntExtra("teacherId", -1);
        }

        if (teacherId == -1) {
            Toast.makeText(this, "Teacher ID not found", Toast.LENGTH_LONG).show();
            return;
        }

        listUnpaidButton.setOnClickListener(v -> loadUnpaidDebts());


        listStudentPaymentsButton.setOnClickListener(v -> {
            Intent intent = new Intent(PaymentActivity.this, StudentUnpaidSlotsActivity.class);
            intent.putExtra("teacherId", teacherId);
            startActivity(intent);
        });
    }

    private void loadUnpaidDebts() {
        new Thread(() -> {
            try {
                String fullUrl = unpaidDebtsUrl + "?teacherId=" +
                        URLEncoder.encode(String.valueOf(teacherId), "UTF-8");

                URL url = new URL(fullUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                Scanner scanner = new Scanner(conn.getInputStream());
                scanner.useDelimiter("\\A");
                String response = scanner.hasNext() ? scanner.next() : "";
                scanner.close();

                JSONObject jsonObject = new JSONObject(response);
                boolean success = jsonObject.getBoolean("success");

                runOnUiThread(() -> resultContainer.removeAllViews());

                if (success) {
                    JSONArray paymentList = jsonObject.getJSONArray("paymentList");

                    runOnUiThread(() -> {
                        for (int i = 0; i < paymentList.length(); i++) {
                            try {
                                JSONObject payment = paymentList.getJSONObject(i);
                                addUnpaidDebtView(payment);
                            } catch (Exception e) {
                                Toast.makeText(
                                        PaymentActivity.this,
                                        e.getMessage(),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }

                        if (paymentList.length() == 0) {
                            Toast.makeText(
                                    PaymentActivity.this,
                                    "No unpaid debt found",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });

                } else {
                    String message = jsonObject.getString("message");

                    runOnUiThread(() ->
                            Toast.makeText(
                                    PaymentActivity.this,
                                    message,
                                    Toast.LENGTH_LONG
                            ).show()
                    );
                }

                conn.disconnect();

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(
                                PaymentActivity.this,
                                "Unpaid debt error: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
            }
        }).start();
    }

    private void loadPaidPayments() {
        new Thread(() -> {
            try {
                String fullUrl = paidPaymentsUrl + "?teacherId=" +
                        URLEncoder.encode(String.valueOf(teacherId), "UTF-8");

                URL url = new URL(fullUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                Scanner scanner = new Scanner(conn.getInputStream());
                scanner.useDelimiter("\\A");
                String response = scanner.hasNext() ? scanner.next() : "";
                scanner.close();

                JSONObject jsonObject = new JSONObject(response);
                boolean success = jsonObject.getBoolean("success");

                runOnUiThread(() -> resultContainer.removeAllViews());

                if (success) {
                    JSONArray payments = jsonObject.getJSONArray("payments");

                    runOnUiThread(() -> {
                        for (int i = 0; i < payments.length(); i++) {
                            try {
                                JSONObject payment = payments.getJSONObject(i);
                                addPaidPaymentView(payment);
                            } catch (Exception e) {
                                Toast.makeText(
                                        PaymentActivity.this,
                                        e.getMessage(),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }

                        if (payments.length() == 0) {
                            Toast.makeText(
                                    PaymentActivity.this,
                                    "No paid payment found",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });

                } else {
                    String message = jsonObject.getString("message");

                    runOnUiThread(() ->
                            Toast.makeText(
                                    PaymentActivity.this,
                                    message,
                                    Toast.LENGTH_LONG
                            ).show()
                    );
                }

                conn.disconnect();

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(
                                PaymentActivity.this,
                                "Paid payment error: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
            }
        }).start();
    }

    private void addUnpaidDebtView(JSONObject payment) throws Exception {
        String studentId = String.valueOf(payment.get("id"));
        String fullName = String.valueOf(payment.get("fullName"));
        String unpaidDebt = String.valueOf(payment.get("unpaidDebt"));

        TextView paymentView = new TextView(this);

        String text =
                "Student ID: " + studentId + "\n" +
                        "Student: " + fullName + "\n" +
                        "Unpaid Debt: " + unpaidDebt;

        paymentView.setText(text);
        paymentView.setTextSize(16);
        paymentView.setTextColor(0xFF000000);
        paymentView.setBackgroundColor(0xFFFFFFFF);
        paymentView.setPadding(24, 24, 24, 24);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        params.setMargins(0, 0, 0, 20);
        paymentView.setLayoutParams(params);

        resultContainer.addView(paymentView);
    }

    private void addPaidPaymentView(JSONObject payment) throws Exception {
        String fullName = String.valueOf(payment.get("fullName"));
        String lessonName = String.valueOf(payment.get("LessonName"));
        String slotDate = String.valueOf(payment.get("slotDate"));
        String duration = String.valueOf(payment.get("duration"));
        String hourlyWage = String.valueOf(payment.get("hourlyWage"));
        String amount = String.valueOf(payment.get("amount"));
        String paymentInfo = String.valueOf(payment.get("paymentInfo"));

        String paymentText = paymentInfo.equals("1") ? "Paid" : "Unpaid";

        TextView paymentView = new TextView(this);

        String text =
                "Student: " + fullName + "\n" +
                        "Lesson: " + lessonName + "\n" +
                        "Slot Date: " + slotDate + "\n" +
                        "Duration: " + duration + " minutes\n" +
                        "Hourly Wage: " + hourlyWage + "\n" +
                        "Amount: " + amount + "\n" +
                        "Payment: " + paymentText;

        paymentView.setText(text);
        paymentView.setTextSize(16);
        paymentView.setTextColor(0xFF000000);
        paymentView.setBackgroundColor(0xFFFFFFFF);
        paymentView.setPadding(24, 24, 24, 24);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        params.setMargins(0, 0, 0, 20);
        paymentView.setLayoutParams(params);

        resultContainer.addView(paymentView);
    }
}