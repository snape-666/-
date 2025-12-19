package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {


    private EditText etUsername, etPassword;
    private Button btnLogin, btnRegister;
    private SharedPreferences sharedPreferences;
    private SharedPreferences userInfoSP;
    private SharedPreferences.Editor editor;
    private CheckBox rememberPass;
    private MySQLiteOpenHelper mySQLiteOpenHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mySQLiteOpenHelper = new MySQLiteOpenHelper(this);
        sharedPreferences = getSharedPreferences(getPackageName() + "_preferences", Context.MODE_PRIVATE);
        userInfoSP=getSharedPreferences("user_info",Context.MODE_PRIVATE);
        rememberPass = (CheckBox) findViewById(R.id.remember_pass);
        initViews();
        setupClickListeners();
        loadSavedUserInfo();
    }

    private void initViews() {

        etUsername = findViewById(R.id.et_log_username);
        etPassword = findViewById(R.id.et_log_password);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);
    }

    private void setupClickListeners() {

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                if (username.isEmpty()||password.isEmpty()){
                    Toast.makeText(LoginActivity.this, "账号和密码不能为空", Toast.LENGTH_SHORT).show();
                }
                if (mySQLiteOpenHelper == null) {
                    mySQLiteOpenHelper = new MySQLiteOpenHelper(LoginActivity.this);
                }
                boolean userExists = mySQLiteOpenHelper.isUsernameExists(username);

                if (!userExists) {
                    Toast.makeText(LoginActivity.this, "账号不存在，请先注册！", Toast.LENGTH_LONG).show();
                    return;
                }
                boolean login = mySQLiteOpenHelper.login(username, password);
                if (login) {
                    Toast.makeText(LoginActivity.this, "登录成功！", Toast.LENGTH_SHORT).show();

                    boolean isRemember = rememberPass.isChecked();

                    editor = sharedPreferences.edit();

                    if (isRemember) {
                        editor.putString("saved_username", username);
                        editor.putString("saved_password", password);

                        editor.putBoolean("is_remembered", true);
                    } else {
                        editor.remove("saved_username");
                        editor.remove("saved_password");
                        editor.putBoolean("is_remembered", false);
                    }
                    editor.apply();
                    userInfoSP.edit().putString("account",username).apply();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                else {
                    Toast.makeText(LoginActivity.this, "账号或密码错误！", Toast.LENGTH_LONG).show();
                }
            }
        });


        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });


    }

    private void loadSavedUserInfo() {
        boolean isRemembered = sharedPreferences.getBoolean("is_remembered", false);
        if (isRemembered) {
            String savedUsername = sharedPreferences.getString("saved_username", "");
            String savedPassword = sharedPreferences.getString("saved_password", "");
            etUsername.setText(savedUsername);
            etPassword.setText(savedPassword);
            rememberPass.setChecked(true);
        }
    }

}
