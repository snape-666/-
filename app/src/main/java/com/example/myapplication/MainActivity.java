package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.net.Uri;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.annotation.NonNull;
import android.app.DatePickerDialog;
import android.provider.MediaStore;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import java.util.Calendar;


public class MainActivity extends AppCompatActivity {
    private Button btnFocus,btnSaveInfo,btnLogOut;
    private BottomNavigationView bottomNavView;
    private ImageButton btnUserInfo,btnClosePanel;
    private TextView homeTitle,tvAccount,tvEditAvatar,etBirthday;
    private int currentNavItemId = R.id.todo_menu;
    private View sideUserPanel;
    private ImageView ivAvatar;
    private EditText etPassword;
    private RadioGroup rgGender;
    private SharedPreferences sp;
    private static final String SP_NAME="user_info";
    private Uri avatarUri;
    private boolean isPanelOpen=false;

    //头像选择启动器
    private final ActivityResultLauncher<Intent> pickAvatarLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        avatarUri = uri;
                        ivAvatar.setImageURI(uri);
                        btnUserInfo.setImageURI(uri); // 同步首页头像
                        sp.edit().putString("avatar_uri", uri.toString()).apply();
                    }
                }
            }
    );
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        //存储用户信息
        sp=getSharedPreferences(SP_NAME,MODE_PRIVATE);
        initViews();
        setupClickListeners();
        setupBottomNavigation();
        bottomNavView = findViewById(R.id.bottom_nav_view);
        bottomNavView.setSelectedItemId(R.id.home_menu);
        loadUserInfo();//加载保存的用户信息

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


      /*  ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(MainActivity.this, "返回登录界面", Toast.LENGTH_SHORT).show();


                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });*/


    }


    private void initViews() {
        bottomNavView = findViewById(R.id.bottom_nav_view);
        homeTitle = findViewById(R.id.home);
        btnUserInfo=findViewById(R.id.user_info);
        btnFocus = findViewById(R.id.focus);
        fixBottomNavIconColor();
        //侧边面板控件
        sideUserPanel=findViewById(R.id.side_user_panel);
        btnClosePanel=findViewById(R.id.btn_close_panel);
        ivAvatar=findViewById(R.id.iv_avatar);
        tvEditAvatar=findViewById(R.id.tv_edit_avatar);
        tvAccount=findViewById(R.id.tv_account);
        etPassword=findViewById(R.id.et_password);
        rgGender=findViewById(R.id.rg_gender);
        etBirthday=findViewById(R.id.et_birthday);
        btnLogOut=findViewById(R.id.btn_logout);
        btnSaveInfo=findViewById(R.id.btn_save_info);

    }
    private void setupClickListeners() {
        // 聚焦按钮
        btnFocus.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TomatoClock.class);
            startActivity(intent);
        });

        // 打开/关闭侧边面板（点击头像按钮）
        btnUserInfo.setOnClickListener(v -> {
            if (isPanelOpen) {
                closeUserPanel();
            } else {
                openUserPanel();
            }
        });

        // 关闭面板按钮
        btnClosePanel.setOnClickListener(v -> closeUserPanel());

        // 修改头像
        ivAvatar.setOnClickListener(v -> pickAvatar());
        tvEditAvatar.setOnClickListener(v -> pickAvatar());

        // 生日选择
        etBirthday.setOnClickListener(v -> showDatePicker());
        //退出登录
        btnLogOut.setOnClickListener(v -> showLogOutDialog());

        // 保存用户信息
        btnSaveInfo.setOnClickListener(v -> saveUserInfo());

        btnFocus.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, TomatoClock.class);
                startActivity(intent);
            }
        });
    }

    //打开侧边面板
    private void openUserPanel(){
        sideUserPanel.setVisibility(View.VISIBLE);
        sideUserPanel.bringToFront();
        sideUserPanel.animate()
                .translationX(0) // 移到屏幕内
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
        isPanelOpen = true;
    }
    //关闭侧边面板(平移动画)
    private void closeUserPanel() {
        sideUserPanel.animate()
                .translationX(300) // 移到屏幕外（面板宽度）
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(()->{
                    isPanelOpen=false;
                    sideUserPanel.setVisibility(View.GONE);
                })
                .start();
        sideUserPanel.setVisibility(View.GONE);
        isPanelOpen = false;
    }
    //选择头像
    private void pickAvatar() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickAvatarLauncher.launch(intent);
    }

    // 日期选择器
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year1, month1, dayOfMonth) -> {
                    String birthday = year1 + "-" + (month1 + 1) + "-" + dayOfMonth;
                    etBirthday.setText(birthday);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    // 加载用户信息
    private void loadUserInfo() {
        // 加载头像
        String avatarUriStr = sp.getString("avatar_uri", "");
        if (!avatarUriStr.isEmpty()) {
            avatarUri = Uri.parse(avatarUriStr);
            ivAvatar.setImageURI(avatarUri);
            btnUserInfo.setImageURI(avatarUri);
        }

        // 加载账号
       // tvAccount.setText(sp.getString("account", ""));
        String loginAccount= sp.getString("account","");
        tvAccount.setText(loginAccount);
        // 加载性别
        int gender = sp.getInt("gender", -1);
        if (gender != -1) {
            rgGender.check(gender);
        }
        // 加载生日
        etBirthday.setText(sp.getString("birthday", ""));
    }

    // 保存用户信息
    private void saveUserInfo() {
        String password = etPassword.getText().toString().trim();
        int genderId = rgGender.getCheckedRadioButtonId();
        String birthday = etBirthday.getText().toString().trim();

        String origPassword=sp.getString("password","");
        int origGender=sp.getInt("gender",-1);
        String origBirthday=sp.getString("birthday","");

        boolean isPasswordChanged=!password.isEmpty()&&!password.equals(origPassword);
        boolean isGenderChanged=genderId!=-1&&genderId!=origGender;
        boolean isBirthdayChanged=!birthday.equals(origBirthday);
        if (!isPasswordChanged&&!isGenderChanged&&!isBirthdayChanged){
            Toast.makeText(this,"无修改",Toast.LENGTH_SHORT).show();
            return;
        }
        SharedPreferences.Editor editor=sp.edit();
        if (!password.isEmpty()){
            editor.putString("password",password);
        }
        editor.putInt("gender",genderId);
        editor.putString("birthday",birthday);
        editor.apply();
        Toast.makeText(this, "信息保存成功", Toast.LENGTH_SHORT).show();
        closeUserPanel(); // 保存后关闭面板
    }
    private void loginSuccess(String account){
        SharedPreferences sp=getSharedPreferences("user_info",MODE_PRIVATE);
        sp.edit().putString("account",account).apply();
        Intent intent=new Intent(MainActivity.this,LoginActivity.class);
        startActivity(intent);
        finish();
    }
    // 退出登录确认弹窗
    private void showLogOutDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("退出登录")
                .setMessage("确定要退出当前账号吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    // 执行退出登录逻辑
                    logout();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    // 退出登录核心逻辑
    private void logout() {
        // 1. 清空SharedPreferences中的用户信息
        SharedPreferences.Editor editor = sp.edit();
        editor.clear(); // 清空所有用户信息（也可针对性删除：editor.remove("avatar_uri")等）
        editor.apply();

        // 2. 重置UI（恢复默认头像、清空输入框）
        ivAvatar.setImageResource(R.drawable.user_photo); // 恢复默认头像
        btnUserInfo.setBackgroundResource(R.drawable.user); // 首页头像恢复默认
        tvAccount.setText("");
        etPassword.setText("");
        rgGender.clearCheck();
        etBirthday.setText("");

        // 3. 关闭侧边面板
        closeUserPanel();

        // 4. 返回登录页面并结束当前页面
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // 结束MainActivity，避免返回键回到首页

        // 5. 提示退出成功
        Toast.makeText(this, "已退出登录", Toast.LENGTH_SHORT).show();
    }







    private void fixBottomNavIconColor() {
        // 完全清除着色（显示原始颜色）
        bottomNavView.setItemIconTintList(null);
        bottomNavView.setItemTextColor(null);
    }

  /*  private void setupClickListeners() {


        btnFocus.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, TomatoClock.class);
                startActivity(intent);
            }
        });
    }*/

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
