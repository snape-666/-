package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.net.Uri;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.provider.MediaStore;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;



public class MainActivity extends AppCompatActivity {
    private Button btnFocus, btnSaveInfo, btnLogOut;
    private BottomNavigationView bottomNavView;
    private ImageButton btnUserInfo, btnClosePanel;
    private TextView homeTitle, tvAccount, tvEditAvatar, etBirthday;
    private int currentNavItemId = R.id.todo_menu;
    private View sideUserPanel;
    private ImageView ivAvatar;
    private EditText etPassword;
    private RadioGroup rgGender;
    private MySQLiteOpenHelper dbHelper;
    private SharedPreferences sp;
    private static final String SP_NAME = "user_info";
    private Uri avatarUri;
    private boolean isPanelOpen = false;
    private String currentAccount;
    private RecyclerView rvHomeTodo;
    private TodoAdapter homeTodoAdapter;
    private List<Todo> homeTodoList = new ArrayList<>();
    private TodoRepository homeTodoRepo;

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

        dbHelper = new MySQLiteOpenHelper(this);
        sp = getSharedPreferences(SP_NAME, MODE_PRIVATE);
        currentAccount = sp.getString("account", "");
        if (currentAccount.isEmpty()){
            currentAccount="default_user_"+ Build.SERIAL;
            sp.edit().putString("account",currentAccount).apply();
            Log.d("MainActivity","账号为空,使用空账号"+currentAccount);
        }

        initViews();
        initHomeTodoList();
        setupClickListeners();
        setupBottomNavigation();
        bottomNavView = findViewById(R.id.bottom_nav_view);
        bottomNavView.setSelectedItemId(R.id.home_menu);
        loadUserInfo();
        loadHomeTodoData();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


    }

    private void initHomeTodoList() {
        rvHomeTodo = findViewById(R.id.rv_todos);
        if (rvHomeTodo == null) {
            Log.e("MainActivity", "RecyclerView not found");
            return;
        }
        rvHomeTodo.setLayoutManager(new LinearLayoutManager(this));
        homeTodoList = new ArrayList<>();
        TodoDatabase db = TodoDatabase.getInstance(this);
        TodoDao todoDao = db.getTodoDao();
        homeTodoRepo = new TodoRepository(todoDao);

        homeTodoAdapter = new TodoAdapter(homeTodoList, new TodoAdapter.OnTodoClickListener() {
           @Override
           public void onDeleteClick(int position) {
                if (position>=0&&position<homeTodoList.size()){
                    Todo todo=homeTodoList.get(position);
                    homeTodoRepo.deleteTodo(todo);
                    homeTodoList.remove(position);
                    homeTodoAdapter.notifyItemRemoved(position);
                    loadHomeTodoData();
                }
            }

            @Override
            public void onItemClick(int position) {
                    List<Todo> filteredList = homeTodoList;
                    if (position >= 0 && position < filteredList.size()) {
                        Todo todo = filteredList.get(position);
                        Intent intent = new Intent(MainActivity.this, EditTodoActivity.class);
                        intent.putExtra("TODO_ID",todo.getId());
                        startActivity(intent);
                }
            }

            @Override
            public void onCheckboxClick(int position, boolean newState) {
                // 单击复选框修改状态
                    if (position >= 0 && position < homeTodoList.size()) {
                        Todo todo = homeTodoList.get(position);
                        todo.setCompleted(newState);
                        homeTodoRepo.updateTodo(todo);
                        homeTodoAdapter.notifyItemChanged(position);
                        if (newState) {
                            homeTodoList.remove(position);
                            homeTodoAdapter.notifyItemRemoved(position);
                            Toast.makeText(MainActivity.this, "待办已完成", Toast.LENGTH_SHORT).show();
                            loadHomeTodoData();
                        }
                    }
                }
        }, new TodoViewModel());
        rvHomeTodo.setAdapter(homeTodoAdapter);
        rvHomeTodo.post(()->{
            if (homeTodoAdapter!=null){
                homeTodoAdapter.notifyDataSetChanged();
                Log.d("MainActivity","强制刷新RecyclerView");
            }
            int width=rvHomeTodo.getWidth();
            int height=rvHomeTodo.getHeight();
            Log.d("MainActivity","RecyclerView宽高:"+width+"X"+height);
        });
    }



    private void loadHomeTodoData() {
        if (homeTodoRepo==null||currentAccount.isEmpty()){
            Log.e("MainActivity","Repo或账号为空:account="+currentAccount);
            Toast.makeText(this,"加载失败:数据初始化异常",Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d("MainActivity","开始查询账号:"+currentAccount+"的Todo数据");
        homeTodoRepo.getTodosByAccount(currentAccount,todos -> {
                runOnUiThread(() -> {
                    Log.d("MainActivity","数据库返回数据总数:"+todos.size());
                    List<Todo>newHomeTodoList=new ArrayList<>();
                        for (Todo todo : todos) {
                            if (!todo.isCompleted()) {
                                newHomeTodoList.add(todo);
                                Log.d("MainActivity","筛选出未完成Todo:"+todo.getContent());
                            }
                        }
                        Log.d("MainActivity","Main页最终待展示数量:"+newHomeTodoList.size());
                        if (newHomeTodoList.isEmpty()){
                            Toast.makeText(this,"暂无待办事项",Toast.LENGTH_SHORT).show();
                        }
                        if (homeTodoAdapter!=null){
                        homeTodoAdapter.updateTodoList(newHomeTodoList);
                        rvHomeTodo.post(()->homeTodoAdapter.notifyDataSetChanged());
                        }else {
                            Log.e("MainActivity","Adapter未初始化");
                        }
                });
            });
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
        btnFocus.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TomatoClock.class);
            startActivity(intent);
        });

        // 打开/关闭侧边面板
        btnUserInfo.setOnClickListener(v -> {
            if (isPanelOpen) {
                closeUserPanel();
            } else {
                openUserPanel();
            }
        });

        btnClosePanel.setOnClickListener(v -> closeUserPanel());

        ivAvatar.setOnClickListener(v -> pickAvatar());
        tvEditAvatar.setOnClickListener(v -> pickAvatar());
        etBirthday.setOnClickListener(v -> showDatePicker());
        btnLogOut.setOnClickListener(v -> showLogOutDialog());
        btnSaveInfo.setOnClickListener(v -> saveUserInfo());
        btnFocus.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, TomatoClock.class);
                startActivity(intent);
            }
        });
    }

    protected void onResume(){
        super.onResume();
        updateBottomNavSelection();
        currentAccount=sp.getString("account","");
        tvAccount.setText(currentAccount);
        if(homeTodoRepo!=null&&homeTodoAdapter!=null&&rvHomeTodo!=null){
            loadHomeTodoData();
        }
        if (TodoMain.instance != null && TodoMain.shouldRefreshMain) {
            loadHomeTodoData();
            TodoMain.shouldRefreshMain = false;
        }
    }

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

    private void closeUserPanel() {
        sideUserPanel.animate()
                .translationX(300) // 移到屏幕外
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
    private void pickAvatar() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickAvatarLauncher.launch(intent);
    }

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

    private void loadUserInfo() {

        String avatarUriStr = sp.getString("avatar_uri", "");
        if (!avatarUriStr.isEmpty()) {
            avatarUri = Uri.parse(avatarUriStr);
            ivAvatar.setImageURI(avatarUri);
            btnUserInfo.setImageURI(avatarUri);
        }
        tvAccount.setText(currentAccount);
        int gender = sp.getInt("gender", -1);
        if (gender != -1) {
            rgGender.check(gender);
        }
        etBirthday.setText(sp.getString("birthday", ""));
    }


    private void saveUserInfo() {
        String newPassword = etPassword.getText().toString().trim();
        int genderId = rgGender.getCheckedRadioButtonId();
        String birthday = etBirthday.getText().toString().trim();
        String sqlorigPassword= dbHelper.getOriginalPassword(currentAccount);
        boolean isPasswordChanged=!newPassword.isEmpty()&&!newPassword.equals(sqlorigPassword);
        if (!newPassword.isEmpty()&&newPassword.equals(sqlorigPassword)){
            Toast.makeText(this,"新旧密码相同",Toast.LENGTH_SHORT).show();
            return;
        }
        int origGender=sp.getInt("gender",-1);
        String origBirthday=sp.getString("birthday","");
        boolean isGenderChanged=genderId!=-1&&genderId!=origGender;
        boolean isBirthdayChanged=!birthday.equals(origBirthday);
        if (!isPasswordChanged&&!isGenderChanged&&!isBirthdayChanged){
            Toast.makeText(this,"无修改",Toast.LENGTH_SHORT).show();
            return;
        }
        SharedPreferences.Editor editor=sp.edit();
        if (isPasswordChanged){
            editor.putString("password",newPassword);
            boolean updateSuccess=dbHelper.updatePassword(currentAccount,newPassword);
            if (!updateSuccess){
                Toast.makeText(this, "密码更新失败", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        editor.putInt("gender",genderId);
        editor.putString("birthday",birthday);
        editor.apply();
        Toast.makeText(this, "信息保存成功", Toast.LENGTH_SHORT).show();
        closeUserPanel();
    }


    private void showLogOutDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("退出登录")
                .setMessage("确定要退出当前账号吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    logout();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void logout() {
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.apply();
        ivAvatar.setImageResource(R.drawable.user_photo);
        btnUserInfo.setBackgroundResource(R.drawable.user);
        tvAccount.setText("");
        etPassword.setText("");
        rgGender.clearCheck();
        etBirthday.setText("");

        closeUserPanel();

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
        Toast.makeText(this, "已退出登录", Toast.LENGTH_SHORT).show();
    }

    private void fixBottomNavIconColor() {
        bottomNavView.setItemIconTintList(null);
        bottomNavView.setItemTextColor(null);
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


    private void updateBottomNavSelection() {
        if (bottomNavView == null) {
            return;
        }
        bottomNavView.setSelectedItemId(R.id.home_menu);
        currentNavItemId = R.id.home_menu;
        homeTitle.setText("首页");
    }

}
