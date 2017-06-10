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
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jasper.ccxapp.R;
import com.example.jasper.ccxapp.db.userDB;
import com.example.jasper.ccxapp.interfaces.UserBackListener;
import com.example.jasper.ccxapp.util.ImageUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.model.UserInfo;

import static com.example.jasper.ccxapp.util.ImageUtil.ACTIVITY_RESULT_ALBUM;
import static com.example.jasper.ccxapp.util.ImageUtil.ACTIVITY_RESULT_IMAGE;
import static com.example.jasper.ccxapp.util.ShowProcessUtil.hideProgressDialog;
import static com.example.jasper.ccxapp.util.ShowProcessUtil.showProgressDialog;

public class UserMessageReviseActivity extends AppCompatActivity {


    private ImageView message_image;
    private TextView btn_image;
    private Button loginout;
    private TextView userName;
    private EditText nickName;
    private RadioGroup message_sex;
    private RadioButton male;
    private RadioButton female;
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

    private Calendar calendar; // 通过Calendar获取系统时间
    private int mYear;
    private int mMonth;
    private int mDay = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_message_revise);

        loginout=(Button)findViewById(R.id.loginout_btn);
        message_image = (ImageView)findViewById(R.id.add_message_image_civ);
        btn_image = (TextView) findViewById(R.id.add_message_image_tv);
        userName = (TextView) findViewById(R.id.message_userName_tv);
        nickName = (EditText)findViewById(R.id.message_nickname_tv);
        message_sex = (RadioGroup)findViewById(R.id.message_sex_rg);
        message_birthday = (EditText)findViewById(R.id.showBirthday_et);
        message_address = (EditText)findViewById(R.id.message_address_et);
        message_explain = (EditText)findViewById(R.id.message_explain_et);
        add_message = (Button)findViewById(R.id.add_message_btn);
        male = (RadioButton)findViewById(R.id.male_rb);
        female = (RadioButton)findViewById(R.id.female_rb);
        imageUtils = new ImageUtil(UserMessageReviseActivity.this);

        initVariable();
        message_image.setOnClickListener(new View.OnClickListener() {
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
        loginout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(UserMessageReviseActivity.this).setTitle("系统提示").setMessage("是否确认退出登录？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                loginOut();
                            }
                        }).show();
            }
        });
        add_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog("确认修改信息?");
            }
        });

        calendar = Calendar.getInstance();
        message_birthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(UserMessageReviseActivity.this,
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
    }

    private void initVariable() {
        oriNickName = JMessageClient.getMyInfo().getNickname();
        oriAddress = JMessageClient.getMyInfo().getAddress();
        oriBirthday = JMessageClient.getMyInfo().getBirthday();
        oriSex = JMessageClient.getMyInfo().getGender();
        oriExplain = JMessageClient.getMyInfo().getSignature();

        JMessageClient.getMyInfo().getAvatarBitmap(new GetAvatarBitmapCallback() {
            @Override
            public void gotResult(int i, String s, Bitmap bitmap) {
                if(i == 0){
                    message_image.setImageBitmap(bitmap);
                }
            }
        });
        userName.setText(JMessageClient.getMyInfo().getUserName());
        nickName.setText(oriNickName);
        message_address.setText(oriAddress);
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
        message_birthday.setText(date.format(oriBirthday));
        if(oriSex.equals(UserInfo.Gender.male)){
            male.setChecked(true);
        }else{
            female.setChecked(true);
        }
        message_explain.setText(oriExplain);
    }

    private void loginOut() {
        JMessageClient.logout();
        startActivity(new Intent(UserMessageReviseActivity.this, LoginActivity.class));
        this.finish();
    }

    public boolean checkPermision(String[] permissions2) {
        boolean flag = false;
        List<String> permissions3 = new ArrayList<String>();
        for(String permission : permissions2){
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                flag = true;
                permissions3.add(permission);
            }
        }
        String[] permissions = new String[permissions3.size()];
        for(int i = 0; i < permissions3.size(); i++){
            permissions[i] = permissions3.get(i);
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

    private void saveUserMessage() {
        boolean flag = false;
        File imagePath = imageUtils.picFile;
        String nickname = nickName.getText().toString().trim();
        int sexid = message_sex.getCheckedRadioButtonId();
        UserInfo.Gender sex;
        if(sexid == R.id.female_rb){
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
            flag = true;
        }
        if(oriSex == sex){
            sex = null;
        }else{
            flag = true;
        }
        if(birthday.equals(oriBirthday)){
            birthday = null;
        }else{
            flag = true;
        }
        if(address.equals(oriAddress)){
            address = null;
        }else{
            flag = true;
        }
        if(explain.equals(oriExplain)){
            explain = null;
        }else{
            flag = true;
        }
        if(imagePath != null && imagePath.exists()){
            flag = true;
        }
        if(flag) {
            showProgressDialog(this, "系统提示", "信息加载中，请稍后");
            userDB.addUserMessage(imagePath, nickname, sex, birthday, address, explain, new UserBackListener() {
                @Override
                public void showResult(boolean result, String message) {
                    hideProgressDialog();
                    if (result) {
                        if (imageUtils.picFile != null || !oriNickName.equals(JMessageClient.getMyInfo().getNickname())) {
                            setResult(666, getIntent());
                        }
                        Toast.makeText(UserMessageReviseActivity.this, "修改信息成功", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(UserMessageReviseActivity.this, "修改信息失败", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else {
            Toast.makeText(UserMessageReviseActivity.this, "并未修改信息", Toast.LENGTH_SHORT).show();
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

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode==KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
            this.finish();
        }
        return false;
    }
}
