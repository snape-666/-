package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;


public class MainActivity extends AppCompatActivity {
    private Button btnFocus;
    private BottomNavigationView bottomNavView;
    private ImageButton ibBack;
    private TextView homeTitle;
    private int currentNavItemId = R.id.todo_menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        initViews();
        setupClickListeners();
        setupBottomNavigation();
        bottomNavView = findViewById(R.id.bottom_nav_view);
        bottomNavView.setSelectedItemId(R.id.home_menu);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(MainActivity.this, "返回登录界面", Toast.LENGTH_SHORT).show();


                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });


    }


    private void initViews() {
        bottomNavView = findViewById(R.id.bottom_nav_view);
        homeTitle = findViewById(R.id.home);

        btnFocus = findViewById(R.id.focus);
        ibBack = findViewById(R.id.back);
        fixBottomNavIconColor();

    }

    private void fixBottomNavIconColor() {
        // 方法1：完全清除着色（显示原始颜色）
        bottomNavView.setItemIconTintList(null);
        bottomNavView.setItemTextColor(null);
    }

    private void setupClickListeners() {


        btnFocus.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, TomatoClock.class);
                startActivity(intent);
            }
        });
    }

    private void setupBottomNavigation() {
        bottomNavView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == currentNavItemId) {
                    return true;
                }

                if (itemId == R.id.home_menu) {
                    navigateToHome();
                } else if (itemId == R.id.todo_menu) {
                    navigateToTodo();
                } else if (itemId == R.id.clock_menu) {
                    navigateToClock();
                } else if (itemId == R.id.edit_menu) {
                    navigateToEdit();
                }

                currentNavItemId = itemId;
                return true;
            }
        });
    }


    private void navigateToHome() {
        homeTitle.setText("首页");
        //refreshHomeContent();
    }

    private void navigateToTodo() {


        Intent intent = new Intent(MainActivity.this, TodoMain.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

    }

    private void navigateToClock() {

        Intent intent = new Intent(MainActivity.this, TomatoClock.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void navigateToEdit() {

        Intent intent = new Intent(MainActivity.this, NoteActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

    }

  /* private void refreshHomeContent() {
        refreshHomeContent();
        Toast.makeText(this, "已刷新", Toast.LENGTH_SHORT).show();
    }
*/



    @Override
    protected void onResume() {
        super.onResume();
        updateBottomNavSelection();

    }

    private void updateBottomNavSelection() {
        // 确保bottomNavView已经初始化
        if (bottomNavView == null) {
            return;
        }
        bottomNavView.setSelectedItemId(R.id.home_menu);
        currentNavItemId = R.id.home_menu;
        homeTitle.setText("首页");
    }

}
