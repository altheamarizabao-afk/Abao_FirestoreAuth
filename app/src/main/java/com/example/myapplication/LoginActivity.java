package com.example.myapplication;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private TextView tvLoginTitle, tvToggleMode;
    private TextInputLayout tilConfirmPassword;
    private TextInputEditText etEmail, etPassword, etConfirmPassword;
    private Button btnAction;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private boolean isRegisterMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        // Bind views
        tvLoginTitle = findViewById(R.id.tvLoginTitle);
        tvToggleMode = findViewById(R.id.tvToggleMode);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnAction = findViewById(R.id.btnAction);
        progressBar = findViewById(R.id.progressBar);

        // Toggle between Login and Registration modes
        tvToggleMode.setOnClickListener(v -> toggleAuthMode());

        // Handle button click based on current mode
        btnAction.setOnClickListener(v -> {
            if (isRegisterMode) {
                registerUser();
            } else {
                loginUser();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            goToMainActivity();
        }
    }

    private void toggleAuthMode() {
        isRegisterMode = !isRegisterMode;

        // Clear existing errors and confirm password field
        etEmail.setError(null);
        etPassword.setError(null);
        etConfirmPassword.setError(null);
        etConfirmPassword.setText("");

        if (isRegisterMode) {
            tvLoginTitle.setText("CREATE ACCOUNT");
            tilConfirmPassword.setVisibility(View.VISIBLE);
            btnAction.setText("REGISTER");
            btnAction.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#648F24")));
            tvToggleMode.setText("Already have an account? Log in here");
            tvToggleMode.setTextColor(Color.parseColor("#0D3075"));
        } else {
            tvLoginTitle.setText("STUDENT PORTAL LOGIN");
            tilConfirmPassword.setVisibility(View.GONE);
            btnAction.setText("LOG IN");
            btnAction.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0D3075")));
            tvToggleMode.setText("Don't have an account? Register here");
            tvToggleMode.setTextColor(Color.parseColor("#DD1F3A"));
        }
    }

    private void loginUser() {
        String email = getText(etEmail);
        String password = getText(etPassword);

        if (!validateBasicInputs(email, password)) return;

        setLoading(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
                        goToMainActivity();
                    } else {
                        String error = task.getException() != null ?
                                task.getException().getMessage() : "Login failed.";
                        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                    }
                });
    }



    private boolean validateBasicInputs(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return false;
        }
        return true;
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnAction.setEnabled(!isLoading);
        tvToggleMode.setEnabled(!isLoading);
    }
    private void registerUser() {
        String email = getText(etEmail);
        String password = getText(etPassword);
        String confirmPassword = getText(etConfirmPassword);

        if (!validateBasicInputs(email, password)) return;

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        setLoading(true);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        // 1. Sign out the auto-created session so it doesn't enter the home screen
                        mAuth.signOut();

                        // 2. Clear the password boxes
                        etPassword.setText("");
                        etConfirmPassword.setText("");

                        // 3. Switch the screen back to Log In mode
                        if (isRegisterMode) {
                            toggleAuthMode();
                        }

                        Toast.makeText(this, "Account created! Please log in.", Toast.LENGTH_LONG).show();
                    } else {
                        String error = task.getException() != null ?
                                task.getException().getMessage() : "Registration failed.";
                        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    private void goToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}