package com.example.punchclock.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.punchclock.MainActivity;
import com.example.punchclock.R;
import com.example.punchclock.data.AppDatabase;
import com.example.punchclock.data.LoginLog;
import com.example.punchclock.data.User;
import com.example.punchclock.data.UserDao;
import com.example.punchclock.utils.SecurityUtils;
import com.example.punchclock.utils.SessionManager;

import java.util.Random;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private EditText etCaptchaInput;
    private TextView tvCaptchaDisplay;
    private CheckBox cbRememberMe;
    private UserDao userDao;
    private SessionManager sessionManager;
    private String currentCaptcha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            navigateToHome(sessionManager.isAdmin());
            return;
        }

        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        etCaptchaInput = findViewById(R.id.et_captcha_input);
        tvCaptchaDisplay = findViewById(R.id.tv_captcha_display);
        cbRememberMe = findViewById(R.id.cb_remember_me);
        TextView tvForgotPassword = findViewById(R.id.tv_forgot_password);
        ImageView btnRefreshCaptcha = findViewById(R.id.btn_refresh_captcha);
        Button btnLogin = findViewById(R.id.btn_login);
        Button btnRegister = findViewById(R.id.btn_register);

        userDao = AppDatabase.getDatabase(this).userDao();

        generateCaptcha();

        btnLogin.setOnClickListener(v -> attemptLogin());
        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
        btnRefreshCaptcha.setOnClickListener(v -> generateCaptcha());
        tvForgotPassword.setOnClickListener(v -> Toast.makeText(this, "请联系管理员重置密码", Toast.LENGTH_SHORT).show());
    }

    private void generateCaptcha() {
        Random random = new Random();
        int captcha = 1000 + random.nextInt(9000);
        currentCaptcha = String.valueOf(captcha);
        tvCaptchaDisplay.setText(currentCaptcha);
    }

    private void attemptLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String captchaInput = etCaptchaInput.getText().toString().trim();
        boolean isRemembered = cbRememberMe.isChecked();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "请输入用户名和密码", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!captchaInput.equals(currentCaptcha)) {
            Toast.makeText(this, "验证码错误", Toast.LENGTH_SHORT).show();
            generateCaptcha();
            return;
        }

        AppDatabase.databaseWriteExecutor.execute(() -> {
            User user = userDao.getUserByName(username);
            boolean success = false;
            
            if (user != null) {
                if (!user.isActive) {
                    runOnUiThread(() -> Toast.makeText(this, "账号已被禁用", Toast.LENGTH_SHORT).show());
                    logLoginAttempt(user.id, username, false, "Account Disabled");
                    return;
                }
                
                if (SecurityUtils.verifyPassword(password, user.salt, user.passwordHash)) {
                    success = true;
                    // Update last login
                    user.lastLoginAt = System.currentTimeMillis();
                    userDao.update(user);
                    
                    runOnUiThread(() -> {
                        sessionManager.createLoginSession(user.id, user.username, user.role, isRemembered);
                        navigateToHome("ADMIN".equals(user.role));
                    });
                }
            }

            logLoginAttempt(user != null ? user.id : -1, username, success, success ? "Success" : "Invalid Credentials");

            if (!success) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
                    generateCaptcha();
                });
            }
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
            intent = new Intent(this, MainActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
