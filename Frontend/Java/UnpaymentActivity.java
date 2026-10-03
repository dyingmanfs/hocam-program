package com.furkan.hocamprogram;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class UnpaymentActivity extends AppCompatActivity {

    SearchView searchUnpaid;
    LinearLayout unpaymentContainer;

    JSONArray allPayments;

    int teacherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_unpayment);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        searchUnpaid = findViewById(R.id.searchUnpaid);
        unpaymentContainer = findViewById(R.id.unpaymentContainer);

        teacherId = getIntent().getIntExtra("teacherId", -1);

        if (teacherId == -1) {
            Toast.makeText(this, "Teacher ID not found", Toast.LENGTH_LONG).show();
            return;
        }

        loadUnpaidDebts();

        searchUnpaid.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterPayments(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterPayments(newText);
                return true;
            }
        });
    }

    private void loadUnpaidDebts() {
        new Thread(() -> {
            try {
                String unpaidDebtsUrl =
                        "http://10.0.2.2/hocamprogram_api/getpaymentinfo.php?teacherId=" + teacherId;

                URL url = new URL(unpaidDebtsUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                Scanner scanner = new Scanner(conn.getInputStream());
                scanner.useDelimiter("\\A");
                String response = scanner.hasNext() ? scanner.next() : "";
                scanner.close();

                JSONObject jsonObject = new JSONObject(response);
                boolean success = jsonObject.getBoolean("success");

                runOnUiThread(() -> unpaymentContainer.removeAllViews());

                if (success) {
                    allPayments = jsonObject.getJSONArray("paymentList");

                    runOnUiThread(() -> showPayments(allPayments));
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
                                UnpaymentActivity.this,
                                "Connection error: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
            }
        }).start();
    }

    private void showPayments(JSONArray paymentList) {
        unpaymentContainer.removeAllViews();

        try {
            for (int i = 0; i < paymentList.length(); i++) {
                JSONObject payment = paymentList.getJSONObject(i);
                addUnpaymentView(payment);
            }
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void filterPayments(String searchText) {
        if (allPayments == null) {
            return;
        }

        try {
            JSONArray filteredList = new JSONArray();

            for (int i = 0; i < allPayments.length(); i++) {
                JSONObject payment = allPayments.getJSONObject(i);

                String fullName = payment.getString("fullName").toLowerCase();
                String id = payment.getString("id");

                String search = searchText.toLowerCase();

                if (fullName.contains(search) || id.contains(search)) {
                    filteredList.put(payment);
                }
            }

            showPayments(filteredList);

        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void addUnpaymentView(JSONObject payment) throws Exception {
        String studentId = payment.getString("id");
        String fullName = payment.getString("fullName");
        String unpaidDebt = payment.getString("unpaidDebt");

        TextView paymentView = new TextView(this);

        String text =
                "Student ID: " + studentId + "\n" +
                        "Name: " + fullName + "\n" +
                        "Unpaid Debt: " + unpaidDebt + " TL\n\n" +
                        "Tap to see unpaid slots";

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

        paymentView.setOnClickListener(v -> {
            Intent intent = new Intent(UnpaymentActivity.this, StudentUnpaidSlotsActivity.class);
            intent.putExtra("teacherId", teacherId);
            intent.putExtra("studentId", studentId);
            intent.putExtra("studentName", fullName);
            startActivity(intent);
        });

        unpaymentContainer.addView(paymentView);
    }
}