package com.example.punchclock.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.punchclock.R;
import com.example.punchclock.data.AppDatabase;
import com.example.punchclock.data.LoginLog;
import com.example.punchclock.data.User;
import com.example.punchclock.data.UserDao;
import com.example.punchclock.utils.SecurityUtils;
import com.example.punchclock.utils.SessionManager;

public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private UserDao userDao;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsername = findViewById(R.id.et_username);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        Button btnRegister = findViewById(R.id.btn_register);
        TextView tvLoginLink = findViewById(R.id.tv_login_link);

        userDao = AppDatabase.getDatabase(this).userDao();
        sessionManager = new SessionManager(this);

        btnRegister.setOnClickListener(v -> attemptRegister());
        tvLoginLink.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "请填写所有必填字段", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
            return;
        }

        AppDatabase.databaseWriteExecutor.execute(() -> {
            User existing = userDao.getUserByName(username);
            if (existing != null) {
                runOnUiThread(() -> Toast.makeText(this, "用户名已存在", Toast.LENGTH_SHORT).show());
                return;
            }

            // Simple admin check
            String role = "USER";
            if ("admin".equalsIgnoreCase(username)) {
                role = "ADMIN";
            }

            String salt = SecurityUtils.generateSalt();
            String hash = SecurityUtils.hashPassword(password, salt);
            
            User newUser = new User(username, email, hash, salt, role, System.currentTimeMillis(), true);
            long id = userDao.insert(newUser);
            newUser.id = id;
            
            logLoginAttempt(id, username, true, "Registration & Login");

            runOnUiThread(() -> {
                Toast.makeText(this, "注册成功", Toast.LENGTH_SHORT).show();
                sessionManager.createLoginSession(newUser.id, newUser.username, newUser.role, true); // Default to remembered on registration
                navigateToHome("ADMIN".equals(newUser.role));
            });
        });
    }

    private void logLoginAttempt(long userId, String username, boolean success, String details) {
        LoginLog log = new LoginLog(userId, username, System.currentTimeMillis(), success, details);
        AppDatabase.getDatabase(this).loginLogDao().insert(log);
    }

    private void navigateToHome(boolean isAdmin) {
        Intent intent;
        if (isAdmin) {
            intent = new Intent(this, AdminActivity.class);
        } else {
            intent = new Intent(this, com.example.punchclock.MainActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
