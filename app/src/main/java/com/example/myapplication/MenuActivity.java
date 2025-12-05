package com.example.myapplication;

import android.view.Menu;

import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity {

    @Override//CTAL+O为快捷键
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_bottom_menu,menu);
        return true;
    }


}
