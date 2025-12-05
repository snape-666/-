package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {


    private EditText etRegUsername, etRegPassword, etConfirmPassword;
    private Button btnRegisterConfirm, btnBackToLogin;
    private MySQLiteOpenHelper mySQLiteOpenHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mySQLiteOpenHelper = new MySQLiteOpenHelper(this);
        initViews();
        setupRegisterButton();
        setupBackToLoginButton();
    }

    private void initViews() {

        etRegUsername = findViewById(R.id.et_reg_username);
        etRegPassword = findViewById(R.id.et_reg_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnRegisterConfirm = findViewById(R.id.btn_register_confirm);
        btnBackToLogin = findViewById(R.id.btn_back_to_login);
    }

    private void setupRegisterButton() {

        btnRegisterConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etRegUsername.getText().toString().trim();
                String password = etRegPassword.getText().toString().trim();
                String confirmPassword = etConfirmPassword.getText().toString().trim();
                if (isInputValid(username, password, confirmPassword)) {
                    if (mySQLiteOpenHelper.isUsernameExists(username)) {
                        Toast.makeText(RegisterActivity.this, "用户名已存在！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    boolean registerSuccess = mySQLiteOpenHelper.registerUser(username, password);
                    if (registerSuccess) {
                        handleSuccessfulRegistration();
                    } else {
                        Toast.makeText(RegisterActivity.this, "注册失败，请重试！", Toast.LENGTH_SHORT).show();
                    }
                }

                }

        });
    }

    private void setupBackToLoginButton() {

        btnBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnToLogin();
            }
        });
    }

    private void returnToLogin() {
        Toast.makeText(RegisterActivity.this, "返回登录界面", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean isInputValid(String username, String password, String confirmPassword) {

        if (username.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "账号不能为空！", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "密码不能为空！", Toast.LENGTH_SHORT).show();
            return false;

        }
        if (confirmPassword.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "请确认密码！", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(RegisterActivity.this, "两次输入的密码不一致！", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (username.length() < 8 || username.length() > 12) {
            etRegUsername.setError("账号长度需为8-12位");
            etRegUsername.requestFocus();
            return false;
        }
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (char c : username.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }
        if (!hasLetter || !hasDigit) {
            etRegUsername.setError("账号需包含字母和数字");
            return false;

        }

        String passwordRegex = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?`~]).{8,16}$";
        if (!password.matches(passwordRegex)) {
            etRegPassword.setError("密码必须包含字母、数字和特殊字符，且长度为8-16位");
            return false;
        }


        etConfirmPassword.setError(null);
        return true;
    }

    private void handleSuccessfulRegistration() {

        Toast.makeText(RegisterActivity.this, "注册成功！", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}





