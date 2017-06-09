package com.example.jasper.ccxapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.jasper.ccxapp.R;
import com.example.jasper.ccxapp.db.userDB;
import com.example.jasper.ccxapp.interfaces.UserBackListener;

import static com.example.jasper.ccxapp.util.ShowProcessUtil.hideProgressDialog;
import static com.example.jasper.ccxapp.util.ShowProcessUtil.showProgressDialog;

public class FindPwd3Activity extends AppCompatActivity {

    private Button findPwdOK;
    private Button findPwdCancel;
    private EditText pwd1;
    private EditText pwd2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_pwd3);

        findPwdOK = (Button)findViewById(R.id.find_pwd_phone_OK_btn);
        findPwdCancel = (Button)findViewById(R.id.find_pwd_phone_cancel_btn);
        pwd1 = (EditText) findViewById(R.id.pwd1_et);
        pwd2 = (EditText) findViewById(R.id.pwd2_et);

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
    }

    private void cancel() {
        startActivity(new Intent(FindPwd3Activity.this, LoginActivity.class));
        this.finish();
    }

    private void ifTrue() {
        String pwd11 = pwd1.getText().toString();
        String pwd22 = pwd2.getText().toString();
        if(pwd11.length() < 6){
            showDialog("新密码应不少于6位！");
            return;
        }
        if(!pwd11.equals(pwd22)){
            showDialog("请输入相同的密码！");
            return;
        }
        if(!showProgressDialog(this, "系统提示", "信息加载中，请稍后")){
            return;
        }
        userDB.changeUserPwd(getIntent().getStringExtra("phone"), pwd11, new UserBackListener() {
            @Override
            public void showResult(boolean result, String message) {
                hideProgressDialog();
                if(result){
                    Toast.makeText(FindPwd3Activity.this, "密码已修改完成，已为您自动登录", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(FindPwd3Activity.this, LoginActivity.class);
                    startActivity(i);
                    finish();
                }else{
                    showDialog("修改密码错误！");
                }
            }
        });
    }


    private void showDialog(String message) {
        new AlertDialog.Builder(this).setTitle("系统提示").setMessage(message)
                .setPositiveButton("确定", null).show();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode==KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
            Intent myIntent;
            myIntent = new Intent(FindPwd3Activity.this, FindPwd2Activity.class);
            myIntent.putExtra("phone", getIntent().getStringExtra("phone"));
            startActivity(myIntent);
            this.finish();
        }
        return false;
    }
}
