package com.example.jasper.ccxapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.jasper.ccxapp.R;
import com.example.jasper.ccxapp.db.userDB;
import com.example.jasper.ccxapp.interfaces.UserBackListener;

import java.util.regex.Pattern;

import static com.example.jasper.ccxapp.util.ShowProcessUtil.hideProgressDialog;
import static com.example.jasper.ccxapp.util.ShowProcessUtil.showProgressDialog;

/**
 * Created by Jasper on 2017/4/7.
 */

public class SignUpActivity extends AppCompatActivity {

    private EditText userName;
    private EditText password1;
    private EditText password2;
    private Button forRegister;
    private TextView toLogin;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        userName = (EditText) findViewById(R.id.sing_up_userName_et);
        password1 = (EditText) findViewById(R.id.sing_up_password_et);
        password2 = (EditText) findViewById(R.id.sing_up_password2_et);
        forRegister = (Button) findViewById(R.id.sign_up_btn);
        toLogin = (TextView) findViewById(R.id.to_login_tv);


        forRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forRegister();
            }
        });

        toLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toLogin();
            }
        });


    }

    private void toLogin() {
        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
        this.finish();
    }

    private void forRegister() {
        //获得输入信息
        final String username = userName.getText().toString().trim();
        final String pwd = password1.getText().toString().trim();
        String pwd2 = password2.getText().toString().trim();
        if(!checkUserName(username)){
            return;
        }else if(!checkpassword(pwd)){
            return;
        }
        if(!pwd.equals(pwd2)){
            showDialog("请输入相同的密码");
            return;
        }
        if(!showProgressDialog(this, "系统提示", "信息加载中，请稍后")){
            return;
        }
        //添加新用户
        userDB.addNewUser(username, pwd, new UserBackListener(){
                    @Override
                    public void showResult(boolean result, String message) {
                        hideProgressDialog();
                        if(result){
                            Intent myIntent = new Intent(SignUpActivity.this,AddUserMessageActivity.class);
                            myIntent.putExtra("userName", userName.getText().toString().trim());
                            myIntent.putExtra("password", password1.getText().toString().trim());
                            startActivity(myIntent);
                            finish();
                        }else{
                            showDialog("用户名已存在!");
                        }
                    }
                });
    }

    private boolean checkUserName(String userName){
        boolean isPhone = Pattern.compile("^1\\d{10}$").matcher(userName).matches();
        if(!isPhone){
            showDialog("请输入手机号码");
            return false;
        }
        return true;
    }

    private boolean checkpassword(String password){
        if(password.length() < 6){
            showDialog("密码不应少于6位字符");
            return false;
        }
        return true;
    }

    private void showDialog(String message) {
        new AlertDialog.Builder(this).setTitle("系统提示").setMessage(message)
                .setPositiveButton("确定", null).show();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode==KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
            Intent myIntent;
            myIntent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(myIntent);
            this.finish();
        }
        return false;
    }
}
