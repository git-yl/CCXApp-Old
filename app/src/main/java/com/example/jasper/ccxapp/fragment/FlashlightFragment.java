package com.example.jasper.ccxapp.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.jasper.ccxapp.R;

public class FlashlightFragment extends Fragment {

    private Button flashlight;
    private boolean ifOpen;
    private Camera camera;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_flashlight, container,false);

        flashlight = (Button)view.findViewById(R.id.flashlight_btn);
        ifOpen = false;
        flashlight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ifOpen){
                    closeFlashlight();
                }else{
                    if (checkPermision(new String[]{Manifest.permission.CAMERA})) {
                        openFlashlight();
                    }
                }
            }
        });
        return view;
    }

    private void closeFlashlight() {
        flashlight.setBackground(getActivity().getResources().getDrawable(R.drawable.ic_flashlight_off));
        camera.stopPreview();
        camera.release();
        ifOpen = false;
    }

    private void openFlashlight() {
        flashlight.setBackground(getActivity().getResources().getDrawable(R.drawable.ic_flashlight_on));
        camera = Camera.open();
        Camera.Parameters params = camera.getParameters();
        params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        camera.setParameters(params);
        camera.startPreview();
        ifOpen = true;
    }

    public boolean checkPermision(String[] permissions) {
        boolean flag = false;
        for(String permission : permissions){
            if (ContextCompat.checkSelfPermission(getActivity(), permission) != PackageManager.PERMISSION_GRANTED){
                flag = true;
                break;
            }
        }
        if(flag){
            FlashlightFragment.this.requestPermissions(permissions, 1);
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
                openFlashlight();
            } else {
                new AlertDialog.Builder(getActivity()).setTitle("系统提示").setMessage("由于未赋予相应的权限，无法正常使用手电筒功能！")
                        .setPositiveButton("确定", null).show();
            }
        }
    }

    @Override
    public void onDestroyView(){
        if(ifOpen){
            closeFlashlight();
        }
        super.onDestroyView();
    }
}
