package com.example.jasper.ccxapp.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jasper.ccxapp.R;
import com.example.jasper.ccxapp.adapter.PhotoAdapter;
import com.example.jasper.ccxapp.entitiy.ShowItemModel;
import com.example.jasper.ccxapp.interfaces.MessageType;
import com.example.jasper.ccxapp.interfaces.SourceFolder;
import com.example.jasper.ccxapp.util.UUIDKeyUtil;
import com.example.jasper.ccxapp.widget.CustomVideoView;
import com.example.jasper.ccxapp.widget.RecyclerItemClickListener;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetGroupIDListCallback;
import cn.jpush.im.android.api.callback.GetGroupInfoCallback;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.eventbus.EventBus;
import me.iwf.photopicker.PhotoPicker;
import me.iwf.photopicker.PhotoPreview;


/**
 * Created by Jasper on 2017/4/20.
 */

public class ShowMsgEditActivity extends AppCompatActivity implements MessageType, SourceFolder {

    public static final int RESULT_SEND_MSG_ITEM = 1;
    static final int REQUEST_VIDEO_CAPTURE = 9;

    private MessageEvent messageEvent;

    private TextView textEditEt;
    private RecyclerView recyclerView;
    private ImageButton recordVideoIb;
    private CustomVideoView videoView;
    private ImageView playVideoBtn;
    private TextView lableVideoTv;

    private String videoPath;
    private ShowItemModel showItem;
    private ArrayList<String> photosPath = null;
    private PhotoAdapter photoAdapter;
    private ArrayList<String> selectedPhotos = new ArrayList<>();
    private TagFlowLayout mFlowLayout;
    private List<GroupInfo> groupInfoList = new ArrayList<GroupInfo>();
    private List<String> groupTag = new ArrayList<String>();
    private List<Long> selectedGroupId = new ArrayList<Long>();
    private TagAdapter<String> mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_msg_edit);

        initView();

        getAllGroupList();

        selectPhotoAndPreview();

        recordVideo();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.send_msg_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_scan:
                BackShowMsgItemToSend();
                break;
        }
        return true;
    }

    private void initView() {
        textEditEt = (EditText) findViewById(R.id.text_edit_et);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mFlowLayout = (TagFlowLayout) findViewById(R.id.id_flowlayout);
        recordVideoIb = (ImageButton) findViewById(R.id.record_video_ib);
        videoView = (CustomVideoView) findViewById(R.id.video_view);
        playVideoBtn = (ImageView) findViewById(R.id.play_video_iv);
        lableVideoTv = (TextView) findViewById(R.id.labal_video_tv);
    }

    private void getAllGroupList() {
        final LayoutInflater mInflater = LayoutInflater.from(ShowMsgEditActivity.this);

        mAdapter = new TagAdapter<String>(groupTag) {
            @Override
            public View getView(FlowLayout parent, int position, String s) {
                TextView tv = (TextView) mInflater.inflate(R.layout.show_groups_tag_tv,
                        mFlowLayout, false);
                tv.setText(s);
                return tv;
            }
        };
        mFlowLayout.setAdapter(mAdapter);

        JMessageClient.getGroupIDList(new GetGroupIDListCallback() {
            @Override
            public void gotResult(int i, String s, List<Long> list) {
                if (i == 0) {
                    if (!list.isEmpty()) {
                        for (long groupId : list) {
                            JMessageClient.getGroupInfo(groupId, new GetGroupInfoCallback() {
                                @Override
                                public void gotResult(int i, String s, GroupInfo groupInfo) {
                                    if (i == 0) {
                                        groupInfoList.add(groupInfo);
                                        groupTag.add(groupInfo.getGroupName());
                                        mAdapter.notifyDataChanged();
                                        Log.i("test", "获取群" + i + s + groupInfo.getGroupName());
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });

        mFlowLayout.setOnSelectListener(new TagFlowLayout.OnSelectListener() {

            @Override
            public void onSelected(Set<Integer> selectPosSet) {
                selectedGroupId.clear();
                for (int pos : selectPosSet) {
                    selectedGroupId.add(groupInfoList.get(pos).getGroupID());
                }
            }
        });
    }

    private void selectPhotoAndPreview() {
        photoAdapter = new PhotoAdapter(this, selectedPhotos);

        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, OrientationHelper.VERTICAL));
        recyclerView.setAdapter(photoAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        if (photoAdapter.getItemViewType(position) == PhotoAdapter.TYPE_ADD) {
                            String[] permissions = {Manifest.permission.CAMERA,
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE};
                            if (checkPermision(permissions,1)) {
                                choosePhoto();
                            }
                        } else {
                            PhotoPreview.builder()
                                    .setPhotos(selectedPhotos)
                                    .setCurrentItem(position)
                                    .start(ShowMsgEditActivity.this);
                        }
                    }
                }));
    }

    private void choosePhoto() {
        PhotoPicker.builder()
                .setPhotoCount(PhotoAdapter.MAX)
                .setShowCamera(true)
                .setPreviewEnabled(false)
                .setSelected(selectedPhotos)
                .start(ShowMsgEditActivity.this);
    }

    private void recordVideo() {
        recordVideoIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] permissions = {Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE};
                if (checkPermision(permissions,2)) {
                    intentToVideoCamera();
                }
            }
        });
    }

    private void intentToVideoCamera() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(videoFolder + File.separator + UUIDKeyUtil.getUUIDKey() + ".mp4")));
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        startActivityForResult(intent, REQUEST_VIDEO_CAPTURE);
    }

    private void BackShowMsgItemToSend() {
        if (selectedGroupId != null && selectedGroupId.size() > 0) {
            Intent intent = new Intent();
            showItem = new ShowItemModel();
            showItem.setGroupBelongToList(selectedGroupId);
            showItem.setMsgKey(UUIDKeyUtil.getUUIDKey());
            showItem.setShowText(textEditEt.getText().toString().trim());
            if (photosPath != null) {//有图片
                showItem.setShowImagesList(photosPath);
                intent.putExtra("showType", SHOW_IMAGE);
            } else if (videoPath != null) {//有视频
                showItem.setShowVideo(videoPath);
                intent.putExtra("showType", SHOW_VIDEO);
            } else {
                intent.putExtra("showType", SHOW_TEXT);
            }

            intent.putExtra("showItem", showItem);
            setResult(RESULT_SEND_MSG_ITEM, intent);
        }
        selectedPhotos = null;
        showItem = null;
        photosPath = null;
        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK &&
                (requestCode == PhotoPicker.REQUEST_CODE || requestCode == PhotoPreview.REQUEST_CODE)) {
            recordVideoIb.setVisibility(View.GONE);
            lableVideoTv.setVisibility(View.GONE);
            if (data != null) {
                photosPath = data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
            }
            selectedPhotos.clear();
            if (photosPath != null) {
                selectedPhotos.addAll(photosPath);
            }
            if (requestCode == PhotoPreview.REQUEST_CODE) {
                if (selectedPhotos.isEmpty()) {
                    recordVideoIb.setVisibility(View.VISIBLE);
                    lableVideoTv.setVisibility(View.VISIBLE);
                }
            }
            photoAdapter.notifyDataSetChanged();
        } else if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            videoPath = data.getData().getPath();
            recyclerView.setVisibility(View.GONE);
            videoView.setVisibility(View.VISIBLE);
            playVideoBtn.setVisibility(View.VISIBLE);
            videoView.setVideoHeight(LinearLayoutCompat.LayoutParams.WRAP_CONTENT);
            videoView.setVideoWidth(LinearLayoutCompat.LayoutParams.WRAP_CONTENT);
            videoView.setVideoPath(videoPath);
            Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Video.Thumbnails.MINI_KIND);
            videoView.setBackgroundDrawable(new BitmapDrawable(bitmap));
            playVideoBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playVideoBtn.setVisibility(View.INVISIBLE);
                    videoView.setBackgroundDrawable(null);
                    videoView.start();
                }
            });
            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    playVideoBtn.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    public boolean checkPermision(String[] permissions,int requestCode) {
        boolean flag = false;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                flag = true;
                break;
            }
        }
        if (flag) {
            ActivityCompat.requestPermissions(this, permissions, requestCode);
        } else {
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            boolean isPhotoGranted = true;
            // 判断是否所有的权限都已经授予了
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isPhotoGranted = false;
                    break;
                }
            }
            if (isPhotoGranted) {
                //申请权限成功后需要调用的函数
                choosePhoto();
            } else {
                new AlertDialog.Builder(this).setTitle("系统提示").setMessage("由于未赋予相应的权限，该功能无法正常使用！")
                        .setPositiveButton("确定", null).show();
            }
        }else if (requestCode == 2) {
            boolean isVideoGranted = true;
            // 判断是否所有的权限都已经授予了
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isVideoGranted = false;
                    break;
                }
            }
            if (isVideoGranted) {
                //申请权限成功后需要调用的函数
                intentToVideoCamera();
            } else {
                new AlertDialog.Builder(this).setTitle("系统提示").setMessage("由于未赋予相应的权限，该功能无法正常使用！")
                        .setPositiveButton("确定", null).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        JMessageClient.registerEventReceiver(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        JMessageClient.unRegisterEventReceiver(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (messageEvent != null) {
            EventBus.getDefault().post(messageEvent);
        }
    }

    public void onEventMainThread(MessageEvent event) {

        Message msg = event.getMessage();
        switch (msg.getContentType()) {
            case text:
                //处理文字消息
                messageEvent = event;
                break;
        }
    }

    public void onEvent(MessageEvent event) {
        Message msg = event.getMessage();

        switch (msg.getContentType()) {
            case voice:
                //处理语音消息
                messageEvent = event;
                break;
            case image:
                //处理图片消息
                messageEvent = event;
                break;
        }
    }

}

