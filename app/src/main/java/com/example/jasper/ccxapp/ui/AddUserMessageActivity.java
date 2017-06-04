package com.example.jasper.ccxapp.ui;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jasper.ccxapp.R;
import com.example.jasper.ccxapp.db.userDB;
import com.example.jasper.ccxapp.interfaces.userBackListener;
import com.example.jasper.ccxapp.util.ImageUtil;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.UserInfo;

import static com.example.jasper.ccxapp.util.ImageUtil.ACTIVITY_RESULT_ALBUM;
import static com.example.jasper.ccxapp.util.ImageUtil.ACTIVITY_RESULT_IMAGE;
import static com.example.jasper.ccxapp.util.ShowProcessUtil.hideProgressDialog;
import static com.example.jasper.ccxapp.util.ShowProcessUtil.showProgressDialog;

public class AddUserMessageActivity extends AppCompatActivity {

    private ImageView message_image;
    private TextView btn_image;
    private TextView userName;
    private EditText nickName;
    private RadioGroup message_sex;
    private EditText message_birthday;
    private EditText message_address;
    private EditText message_explain;
    private Button add_message;
    private String oriNickName;
    private UserInfo.Gender oriSex;
    private Long oriBirthday;
    private String oriAddress;
    private String oriExplain;
    private ImageUtil imageUtils;

    private Spinner securityQ1;
    private EditText answerQ1;
    private EditText securityQ2;
    private EditText answerQ2;

    private Calendar calendar; // 通过Calendar获取系统时间
    private int mYear;
    private int mMonth;
    private int mDay = -1;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user_message);

        Toast.makeText(this, "注册完成，请填写详细信息", Toast.LENGTH_SHORT).show();

        message_image = (ImageView)findViewById(R.id.add_message_image);
        btn_image = (TextView)findViewById(R.id.add_message_image_btn);
        userName = (TextView)findViewById(R.id.message_userName);
        nickName = (EditText)findViewById(R.id.message_nickname);
        message_sex = (RadioGroup)findViewById(R.id.message_sex);
        message_birthday = (EditText)findViewById(R.id.showBirthday);
        message_address = (EditText)findViewById(R.id.message_address);
        message_explain = (EditText)findViewById(R.id.message_explain);
        add_message = (Button)findViewById(R.id.add_message_btn);
        securityQ1 = (Spinner)findViewById(R.id.securityQ1);
        securityQ2 = (EditText)findViewById(R.id.securityQ2);
        answerQ1 = (EditText)findViewById(R.id.securityA1);
        answerQ2 = (EditText)findViewById(R.id.securityA2);
        imageUtils = new ImageUtil(AddUserMessageActivity.this);

        userName.setText(getIntent().getStringExtra("userName"));
        btn_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] permissions = {Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE};
                if(checkPermision(permissions)){
                    chooseDialog();
                }
            }
        });

        add_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog("确认添加信息?");
            }
        });

        calendar = Calendar.getInstance();
        message_birthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(AddUserMessageActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int day) {
                                mYear = year;
                                mMonth = month;
                                mDay = day;
                                // 更新EditText控件日期 小于10加0
                                message_birthday.setText(new StringBuilder()
                                        .append(mYear)
                                        .append("-")
                                        .append((mMonth + 1) < 10 ? "0"
                                                + (mMonth + 1) : (mMonth + 1))
                                        .append("-")
                                        .append((mDay < 10) ? "0" + mDay : mDay));
                            }
                        }, calendar.get(Calendar.YEAR), calendar
                        .get(Calendar.MONTH), calendar
                        .get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        ArrayList<String> data_list = new ArrayList<String>();
        data_list.add("您的名字是？");
        data_list.add("您父亲的名字是？");
        data_list.add("您的生日是？");
        data_list.add("您最大的愿望是什么？");

        //适配器
        SpinnerAdapter arr_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data_list);
//        //设置样式
//        arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //加载适配器
        securityQ1.setAdapter(arr_adapter);

        addOriMessage();
    }

    public boolean checkPermision(String[] permissions) {
        boolean flag = false;
        for(String permission : permissions){
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                flag = true;
                break;
            }
        }
        if(flag){
            ActivityCompat.requestPermissions(this, permissions, 1);
        }else{
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            boolean isAllGranted = true;

            // 判断是否所有的权限都已经授予了
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }

            if (isAllGranted) {
                //申请权限成功后需要调用的函数
                chooseDialog();
            } else {
                new AlertDialog.Builder(this).setTitle("系统提示").setMessage("由于未赋予相应的权限，该功能无法正常使用！")
                        .setPositiveButton("确定", null).show();
            }
        }
    }

    private void chooseDialog() {
        new AlertDialog.Builder(this)
                .setTitle("选择头像")
                .setNegativeButton("相册", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        imageUtils.byAlbum();
                    }
                })
                .setPositiveButton("拍照", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        String status = Environment.getExternalStorageState();
                        if (status.equals(Environment.MEDIA_MOUNTED)) {// 判断是否存在SD卡
                            imageUtils.byCamera();
                        }
                    }
                }).show();

    }

    // 这里需要注意resultCode，正常情况返回值为 -1 没有任何操作直接后退则返回 0
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println("-->requestCode:" + requestCode + "-->resultCode:" + resultCode);

        switch (requestCode) {
            case ImageUtil.ACTIVITY_RESULT_CAMERA: // 拍照
                try {
                    if (resultCode == -1) {
                        imageUtils.cutImageByCamera();
                    } else {
                        // 因为在无任何操作返回时，系统依然会创建一个文件，这里就是删除那个产生的文件
                        if (imageUtils.picFile != null) {
                            imageUtils.picFile.delete();
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case ACTIVITY_RESULT_ALBUM:
                try {
                    if (resultCode == -1) {
                        Uri selectedImage = data.getData();
                        imageUtils.cutImageByAlbumIntent(selectedImage);
                    } else {
                        // 因为在无任何操作返回时，系统依然会创建一个文件，这里就是删除那个产生的文件
                        if (imageUtils.picFile != null) {
                            imageUtils.picFile.delete();
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case ACTIVITY_RESULT_IMAGE:
                try {
                    if (resultCode == -1) {
                        Bitmap bm_icon = imageUtils.decodeBitmap();
                        if (bm_icon != null) {
                            message_image.setImageBitmap(bm_icon);
                        }
                    } else {
                        // 因为在无任何操作返回时，系统依然会创建一个文件，这里就是删除那个产生的文件
                        if (imageUtils.picFile != null) {
                            imageUtils.picFile.delete();
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private void addOriMessage() {
        oriNickName = getIntent().getStringExtra("userName");
        oriSex = UserInfo.Gender.male;
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date d = date.parse("1990-01-01");
            oriBirthday = d.getTime();
        } catch (ParseException e) {

        }
        oriAddress = "北京";
        oriExplain = "";
        String password = getIntent().getStringExtra("password");
        userDB.forUserLogin(oriNickName, password, new userBackListener() {
            @Override
            public void showResult(boolean result, String message) {
                if(!result){
                    showDialog2("联网错误！");
                }else{
                    userDB.addUserMessage(null, oriNickName, oriSex, oriBirthday, oriAddress,
                            oriExplain, new userBackListener() {
                                @Override
                                public void showResult(boolean result, String message) {
                                    if(!result){
                                        showDialog2("联网错误！");
                                    }else{
                                        userDB.addUserIdentity("young", new userBackListener() {
                                            @Override
                                            public void showResult(boolean result, String message) {
                                                if(!result){
                                                    showDialog2("联网错误！");
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                }
            }
        });
    }

    private void saveUserMessage() {
        //前两个表示是否需要更新信息，中间两个表示是否已经更新信息得到结果，最后两个表示结果是否正确
        final boolean flag[] = {false, false, false, false, false, false};
        File imagePath = imageUtils.picFile;
        String nickname = nickName.getText().toString().trim();
        int sexid = message_sex.getCheckedRadioButtonId();
        UserInfo.Gender sex;
        if(sexid == R.id.female){
            sex = UserInfo.Gender.female;
        }else{
            sex = UserInfo.Gender.male;
        }
        Long birthday = null;
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
        try{
            Date d = date.parse(message_birthday.getText().toString());
            birthday = d.getTime();
        }catch (Exception e){
            birthday = null;
        }
        String address = message_address.getText().toString().trim();
        String explain = message_explain.getText().toString().trim();

        if(nickname.equals(oriNickName) || nickname.equals("")){
            nickname = null;
        }else{
            flag[0] = true;
        }
        if(oriSex == sex){
            sex = null;
        }else{
            flag[0] = true;
        }
        if(birthday.equals(oriBirthday)){
            birthday = null;
        }else{
            flag[0] = true;
        }
        if(address.equals(oriAddress)){
            address = null;
        }else{
            flag[0] = true;
        }
        if(explain.equals(oriExplain)){
            explain = null;
        }else{
            flag[0] = true;
        }
        if(imagePath != null && imagePath.exists()){
            flag[0] = true;
        }
        String Q1 = securityQ1.getSelectedItem().toString();
        String A1 = answerQ1.getText().toString();
        String Q2 = securityQ2.getText().toString();
        String A2 = answerQ2.getText().toString();

        showProgressDialog(this, "系统提示", "信息加载中，请稍后");
        if(!(A1.equals("") && A2.equals(""))){
            flag[1] = true;
            userDB.addSecurityQA(Q1, A1, Q2, A2, new userBackListener() {
                @Override
                public void showResult(boolean result, String message) {
                    flag[3] = true;
                    flag[5] = result;
                    if(result){
                        if(flag[0]){
                            if(flag[2]){
                                if(flag[4]) {
                                    hideProgressDialog();
                                    Toast.makeText(AddUserMessageActivity.this, "添加信息成功", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(AddUserMessageActivity.this, LoginActivity.class));
                                    finish();
                                }else{
                                    hideProgressDialog();
                                    Toast.makeText(AddUserMessageActivity.this, "添加信息失败", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }else{
                            hideProgressDialog();
                            Toast.makeText(AddUserMessageActivity.this, "添加信息成功", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(AddUserMessageActivity.this, LoginActivity.class));
                            finish();
                        }
                    }else {
                        if(flag[2]){
                            hideProgressDialog();
                            Toast.makeText(AddUserMessageActivity.this, "添加信息失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
        if(flag[0]) {
            userDB.addUserMessage(imagePath, nickname, sex, birthday, address, explain, new userBackListener() {
                @Override
                public void showResult(boolean result, String message) {
                    flag[2] = true;
                    flag[4] = result;
                    if (result) {
                        if (flag[1]) {
                            if (flag[3]) {
                                if (flag[5]) {
                                    hideProgressDialog();
                                    Toast.makeText(AddUserMessageActivity.this, "添加信息成功", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(AddUserMessageActivity.this, LoginActivity.class));
                                    finish();
                                } else {
                                    hideProgressDialog();
                                    Toast.makeText(AddUserMessageActivity.this, "添加信息失败", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            hideProgressDialog();
                            Toast.makeText(AddUserMessageActivity.this, "添加信息成功", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(AddUserMessageActivity.this, LoginActivity.class));
                            finish();
                        }
                    } else {
                        if (flag[3]) {
                            hideProgressDialog();
                            Toast.makeText(AddUserMessageActivity.this, "添加信息失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
        if(!flag[0] && !flag[1]){
            hideProgressDialog();
            Toast.makeText(AddUserMessageActivity.this, "添加信息成功", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AddUserMessageActivity.this, LoginActivity.class));
            finish();
        }
    }

    private void showDialog(String message) {
        new AlertDialog.Builder(this).setTitle("系统提示").setMessage(message)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveUserMessage();
                    }
                }).setNegativeButton("取消", null).show();
    }

    private void showDialog2(String message) {
        new AlertDialog.Builder(this).setTitle("系统提示").setMessage(message)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(AddUserMessageActivity.this, LoginActivity.class));
                        finish();
                    }
                }).show();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode==KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
            JMessageClient.logout();
            Intent myIntent;
            myIntent = new Intent(AddUserMessageActivity.this, LoginActivity.class);
            startActivity(myIntent);
            this.finish();
        }
        return false;
    }
}
