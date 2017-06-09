package com.example.jasper.ccxapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.jasper.ccxapp.R;

import java.util.regex.Pattern;

public class FindPwd1Activity extends AppCompatActivity {

    private EditText findPwdPhone;
    private Button findPwdOK;
    private Button findPwdCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_pwd1);

        findPwdPhone = (EditText)findViewById(R.id.find_pwd_phone_et);
        findPwdOK = (Button)findViewById(R.id.find_pwd_phone_OK_btn);
        findPwdCancel = (Button)findViewById(R.id.find_pwd_phone_cancel_btn);

        findPwdOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findPwd();
            }
        });
        findPwdCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });
    }

    private void cancel() {
        startActivity(new Intent(FindPwd1Activity.this, LoginActivity.class));
        this.finish();
    }

    private void findPwd() {
        String phone = findPwdPhone.getText().toString();
        boolean isPhone = Pattern.compile("^1\\d{10}$").matcher(phone).matches();
        if(!isPhone){
            showDialog("请输入正确的手机号码");
            return;
        }
        Intent i = new Intent(FindPwd1Activity.this, FindPwd2Activity.class);
        i.putExtra("phone", phone);
        startActivity(i);
        this.finish();
    }


    private void showDialog(String message) {
        new AlertDialog.Builder(this).setTitle("系统提示").setMessage(message)
                .setPositiveButton("确定", null).show();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode==KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
            Intent myIntent;
            myIntent = new Intent(FindPwd1Activity.this, LoginActivity.class);
            startActivity(myIntent);
            this.finish();
        }
        return false;
    }
}
