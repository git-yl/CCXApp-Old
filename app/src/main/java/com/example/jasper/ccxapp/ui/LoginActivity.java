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
import android.widget.Toast;

import com.example.jasper.ccxapp.R;
import com.example.jasper.ccxapp.db.userDB;
import com.example.jasper.ccxapp.interfaces.UserBackListener;

import cn.jpush.im.android.api.JMessageClient;

import static com.example.jasper.ccxapp.util.ShowProcessUtil.hideProgressDialog;
import static com.example.jasper.ccxapp.util.ShowProcessUtil.showProgressDialog;

/**
 * Created by Jasper on 2017/4/6.
 */

public class LoginActivity extends AppCompatActivity {

    private EditText usernameET;
    private EditText passwordET;
    private Button signInBtn;
    private TextView signUpBtn;
    private String username;
    private String password;
    private TextView findPwd;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        ifLogin();

        usernameET = (EditText) findViewById(R.id.login_username_et);
        passwordET = (EditText) findViewById(R.id.login_password_et);
        signInBtn = (Button) findViewById(R.id.sign_in_btn);
        signUpBtn = (TextView) findViewById(R.id.sign_up_tv);
        findPwd = (TextView)findViewById(R.id.findPassword_tv);

        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forLogin();
            }
        });

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toRegister();
            }
        });

        findPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toFindPwd();
            }
        });
    }

    private void toFindPwd() {
        startActivity(new Intent(LoginActivity.this, FindPwd1Activity.class));
        this.finish();
    }

    private void ifLogin() {
        try {
            if (JMessageClient.getMyInfo().getUserName() == null) {
                return;
            } else {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                this.finish();
            }
        }catch (Exception e){

        }
    }

    private void toRegister() {
        startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        this.finish();
    }

    private void forLogin() {
        username = usernameET.getText().toString().trim();
        password = passwordET.getText().toString().trim();
        if (!checkUserName(username)) {
            return;
        } else if (!checkpassword(password)) {
            return;
        }
        if(!showProgressDialog(this, "系统提示", "信息加载中，请稍后")){
            return;
        }
        userDB.forUserLogin(username, password, new UserBackListener() {
            @Override
            public void showResult(boolean result, String message) {
                hideProgressDialog();
                if (result) {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    showDialog("用户名或密码错误");
                }
            }
        });
    }

    private void showDialog(String message) {
        new AlertDialog.Builder(this).setTitle("系统提示").setMessage(message)
                .setPositiveButton("确定", null).show();
    }

    private long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private boolean checkUserName(String userName) {
        if (userName.length() == 0) {
            showDialog("请输入手机号码");
            return false;
        }
        return true;
    }

    private boolean checkpassword(String password) {
        if (password.length() < 6) {
            showDialog("密码不应少于6位字符");
            return false;
        }
        return true;
    }
}
