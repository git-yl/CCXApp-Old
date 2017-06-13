package com.example.jasper.ccxapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jasper.ccxapp.R;
import com.example.jasper.ccxapp.db.userDB;
import com.example.jasper.ccxapp.interfaces.UserBackListListener;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.model.UserInfo;

public class FindPwd2Activity extends AppCompatActivity {

    private Button findPwdOK;
    private Button findPwdCancel;
    private TextView Q1;
    private EditText A1;
    private TextView Q2;
    private EditText A2;
    private ArrayList<String> messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_pwd2);

        findPwdOK = (Button)findViewById(R.id.find_pwd_phone_OK_btn);
        findPwdCancel = (Button)findViewById(R.id.find_pwd_phone_cancel_btn);
        Q1 = (TextView)findViewById(R.id.Q1_tv);
        Q2 = (TextView)findViewById(R.id.Q2_tv);
        A1 = (EditText) findViewById(R.id.A1_et);
        A2 = (EditText) findViewById(R.id.A2_et);

        findPwdOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ifTrue();
            }
        });
        findPwdCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });

        getOriData();
    }

    private void getOriData() {
        String phone = getIntent().getStringExtra("phone");
        userDB.getSecurityQA(phone, new UserBackListListener() {
            @Override
            public void showResult(boolean result, ArrayList<String> message, List<UserInfo> userInfos) {
                if(result){
                    messages = message;
                    Q1.setText(messages.get(0));
                    Q2.setText(messages.get(2));
                }else{
                    Toast.makeText(FindPwd2Activity.this, "您尚未预留密保信息！", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(FindPwd2Activity.this, FindPwd1Activity.class));
                }
            }
        });
    }

    private void cancel() {
        startActivity(new Intent(FindPwd2Activity.this, LoginActivity.class));
        this.finish();
    }

    private void ifTrue() {
        if(!A1.getText().toString().equals(messages.get(1))){
            showDialog("您输入的答案有误！");
            return;
        }
        if(!A2.getText().toString().equals(messages.get(3))){
            showDialog("您输入的答案有误！");
            return;
        }
        Intent i = new Intent(FindPwd2Activity.this, FindPwd3Activity.class);
        i.putExtra("phone", getIntent().getStringExtra("phone"));
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
            myIntent = new Intent(FindPwd2Activity.this, FindPwd1Activity.class);
            startActivity(myIntent);
            this.finish();
        }
        return false;
    }
}
