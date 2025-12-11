package com.example.myapplication;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.NumberPicker;
import androidx.appcompat.app.AppCompatActivity;


public class TomatoClock extends AppCompatActivity {

    private CircleProgress progressBar;
    private TextView timerText;
    private ImageButton tomatoBack;
    private CountDownTimer currentTimer;//倒计时控件
    private long totalTimeMs;

    private int selectedDuration=30;
    private Dialog numberPickerDialog;
    private String[] durationOptions;
    private TextView tvDuration;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tomatoclock);

        tvDuration=findViewById(R.id.workBtn);
        durationOptions=new String[]{"30","60","90","120","150","自定义"};
        tvDuration.setOnClickListener(v -> showDurationPikerDialog());

        progressBar = findViewById(R.id.progressBar);
        timerText = findViewById(R.id.timerText);
        tomatoBack =findViewById(R.id.back);
        ImageButton giveUpBtn = findViewById(R.id.giveUpBtn);
       // TextView workBtn = findViewById(R.id.workBtn);
        TextView shortRestBtn = findViewById(R.id.shortRestBtn);
        TextView longRestBtn = findViewById(R.id.longRestBtn);

        //workBtn.setOnClickListener(v -> startTimer(1 * 60 * 1000));//默认的番茄钟起始时间
        shortRestBtn.setOnClickListener(v -> startTimer(5 * 60 * 1000));
        longRestBtn.setOnClickListener(v -> startTimer(20 * 60 * 1000));
        giveUpBtn.setOnClickListener(v -> cancelTimer());

        tomatoBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Toast.makeText(TomatoClock.this, "返回主页", Toast.LENGTH_SHORT).show();


                    Intent intent = new Intent(TomatoClock.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
    }
    private void showDurationPikerDialog(){
        numberPickerDialog=new Dialog(this);
        numberPickerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);//无标题栏
        numberPickerDialog.setCancelable(true);//点击外部空白可取消

        View dialogView=getLayoutInflater().inflate(R.layout.dialog_number_picker,null);
        numberPickerDialog.setContentView(dialogView);

        Window window=numberPickerDialog.getWindow();
        WindowManager.LayoutParams lp=window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        window.setAttributes(lp);

        NumberPicker numberPicker = dialogView.findViewById(R.id.num_picker);
        numberPicker.setMinValue(0); // 数组起始下标
        numberPicker.setMaxValue(durationOptions.length - 1); // 数组结束下标
        numberPicker.setDisplayedValues(durationOptions); // 设置显示选项
        numberPicker.setValue(1); // 默认选中60分钟（对应数组下标1）
        numberPicker.setWrapSelectorWheel(false); // 关闭循环（避免自定义和10分钟循环）

        numberPicker.setOnScrollListener(new NumberPicker.OnScrollListener() {
            @Override
            public void onScrollStateChange(NumberPicker view, int scrollState) {
                // scrollState=0：滚动停止；1：开始滚动；2：惯性滚动
                if (scrollState == NumberPicker.OnScrollListener.SCROLL_STATE_IDLE) {
                    handleSelectedDuration(numberPicker.getValue());
                }
            }
        });

        // 5. 显示弹窗
        numberPickerDialog.show();
    }

    private void handleSelectedDuration(int selectedIndex) {
        // 最后一项是“自定义”，单独处理
        if (selectedIndex == durationOptions.length - 1) {
            showCustomInputDialog();
        } else {
            // 选择固定时长：转换为数字，更新显示并关闭弹窗
            selectedDuration = Integer.parseInt(durationOptions[selectedIndex]);
            updateDurationDisplay();
            numberPickerDialog.dismiss(); // 关闭弹窗
        }
    }


    private void showCustomInputDialog(){
        EditText etCustomDuration=new EditText(this);
        etCustomDuration.setHint("请输入专注时长");
        etCustomDuration.setInputType(InputType.TYPE_CLASS_NUMBER);
        etCustomDuration.setGravity(Gravity.CENTER);
        etCustomDuration.setPadding(40,20,40,20);

        new AlertDialog.Builder(this)
                .setTitle("自定义专注时长")
                .setView(etCustomDuration)
                .setPositiveButton("确定",(dialog,which)->{
                 String inputStr=etCustomDuration.getText().toString().trim();
                 if(inputStr.isEmpty()){
                     Toast.makeText(TomatoClock.this,"请输入时长",Toast.LENGTH_SHORT).show();
                     return;
                 }
                 int customDuration = Integer.parseInt(inputStr);
                 selectedDuration=customDuration;
                 updateDurationDisplay();
                 numberPickerDialog.dismiss();
                 dialog.dismiss();
                }).setNegativeButton("取消",(dialog,which)->dialog.dismiss()).show();
    }
    private void updateDurationDisplay() {
        tvDuration.setText("已选专注时长：" + selectedDuration + " 分钟");
    }


    private void startTimer(long durationMs) {
        cancelTimer();
        totalTimeMs = durationMs;

        currentTimer = new CountDownTimer(durationMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long min = (millisUntilFinished / 1000) / 60;
                long sec = (millisUntilFinished / 1000) % 60;
                timerText.setText(String.format("%02d:%02d", min, sec));

                float progress = (1 - (float) millisUntilFinished / totalTimeMs) * 100;
                progressBar.setProgress(progress);
            }

            @Override
            public void onFinish() {
                timerText.setText("00:00");
                progressBar.setProgress(100);
            }
        }.start();
    }


    private void cancelTimer() {
        if (currentTimer != null) {
            currentTimer.cancel();
            currentTimer = null;
        }
    }

    @Override
    protected void onDestroy() {
        cancelTimer();
        super.onDestroy();
    }

}
