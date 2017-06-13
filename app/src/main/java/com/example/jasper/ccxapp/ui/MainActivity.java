package com.example.jasper.ccxapp.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextPaint;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jasper.ccxapp.R;
import com.example.jasper.ccxapp.adapter.ShowPhotoAdapter;
import com.example.jasper.ccxapp.db.LocalMessageDB;
import com.example.jasper.ccxapp.db.userDB;
import com.example.jasper.ccxapp.entitiy.CommentItemModel;
import com.example.jasper.ccxapp.entitiy.ShowItemModel;
import com.example.jasper.ccxapp.interfaces.MessageType;
import com.example.jasper.ccxapp.interfaces.ShowMsgSender;
import com.example.jasper.ccxapp.service.factory.SendFactory;
import com.example.jasper.ccxapp.util.GetCurrentTimeUtil;
import com.example.jasper.ccxapp.util.SendMessageUtil;
import com.example.jasper.ccxapp.util.UUIDKeyUtil;
import com.example.jasper.ccxapp.widget.CustomVideoView;
import com.example.jasper.ccxapp.widget.PinnedHeaderExpandableListView;
import com.example.jasper.ccxapp.widget.RecordButton;
import com.example.jasper.ccxapp.widget.RecyclerItemClickListener;
import com.example.jasper.ccxapp.widget.StickyLayout;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.DownloadCompletionCallback;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.content.FileContent;
import cn.jpush.im.android.api.content.ImageContent;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.content.VoiceContent;
import cn.jpush.im.android.api.event.ContactNotifyEvent;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import de.hdodenhof.circleimageview.CircleImageView;
import me.iwf.photopicker.PhotoPreview;

public class MainActivity extends AppCompatActivity implements
        ExpandableListView.OnChildClickListener,
        ExpandableListView.OnGroupClickListener,
        PinnedHeaderExpandableListView.OnHeaderUpdateListener, StickyLayout.OnGiveUpTouchEventListener, MessageType {
    private final int REQUEST_SEND_MSG_ITEM = 0;
    //View
    private PinnedHeaderExpandableListView expandableListView;
    private StickyLayout stickyLayout;
    //变量
    private ArrayList<ShowItemModel> showList = new ArrayList<ShowItemModel>();
    private ArrayList<List<CommentItemModel>> childCommentList = new ArrayList<List<CommentItemModel>>();
    private MyexpandableListAdapter adapter;
    private Conversation mConversation;
    private MediaPlayer mediaPlayer;
    //收到的图片消息
    private ShowItemModel CheckRecievedShowItem = new ShowItemModel();
    private String imgRecieveFlag = "";
    private String checkShowKey = "";
    private String checkCommKey = "";
    private TextView myName;
    private TextView userName;
    private CircleImageView leftUserAvatarCIV;
    private DrawerLayout drawerLayout;
    private ImageView myAvatarCIV;
    private SendFactory sendFactory = new SendFactory();

    private LocalMessageDB messageDB = new LocalMessageDB(MainActivity.this);


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initData();

        adapter = new MyexpandableListAdapter(this);
        expandableListView.setAdapter(adapter);
        // 展开所有group
        for (int i = 0, count = expandableListView.getCount(); i < count; i++) {
            expandableListView.expandGroup(i);
        }
        expandableListView.setOnHeaderUpdateListener(this);
        expandableListView.setOnChildClickListener(this);
        expandableListView.setOnGroupClickListener(this);
        stickyLayout.setOnGiveUpTouchEventListener(this);

        checkPermision(new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE});
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.addnew, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_scan:
                Intent intent = new Intent(MainActivity.this, ShowMsgEditActivity.class);
                startActivityForResult(intent, REQUEST_SEND_MSG_ITEM);
                break;
        }
        return true;
    }

    private void initView() {

        expandableListView = (PinnedHeaderExpandableListView) findViewById(R.id.expandablelist);
        stickyLayout = (StickyLayout) findViewById(R.id.sticky_layout);
        myAvatarCIV = (ImageView) findViewById(R.id.my_avatar_iv);
        userName = (TextView) findViewById(R.id.currentUserName_tv);
        TextPaint tp = userName.getPaint();
        tp.setFakeBoldText(true);
        JMessageClient.getMyInfo().getAvatarBitmap(new GetAvatarBitmapCallback() {
            @Override
            public void gotResult(int i, String s, Bitmap bitmap) {
                if (i == 0) {
                    myAvatarCIV.setImageBitmap(bitmap);
                    leftUserAvatarCIV.setImageBitmap(bitmap);
                }
            }
        });
        try {
            userName.setText(JMessageClient.getMyInfo().getNickname());
        } catch (Exception e) {
            userName.setText(JMessageClient.getMyInfo().getUserName());
        }

        initDrawerLayout();
        drawerLayout.setScrimColor(Color.GRAY);
        //drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
    }

    private void initDrawerLayout() {
        drawerLayout = (DrawerLayout) super.findViewById(R.id.drawer_layout);
        drawerLayout.setScrimColor(Color.TRANSPARENT);

        View v1 = findViewById(R.id.left_drawer);
        myName = (TextView) v1.findViewById(R.id.myName_tv);
        leftUserAvatarCIV = (CircleImageView) v1.findViewById(R.id.left_my_avatar_civ);
        try {
            myName.setText(JMessageClient.getMyInfo().getNickname());
        } catch (Exception e) {
            myName.setText(JMessageClient.getMyInfo().getUserName());
        }
        leftUserAvatarCIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this, UserMessageReviseActivity.class), 1);
            }
        });
    }

    private void initMediaPlayer(String voicePath) {
        try {
            mediaPlayer.setDataSource(voicePath);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /***
     * InitData
     */
    void initData() {
        showList = new ArrayList<ShowItemModel>();
        childCommentList = new ArrayList<List<CommentItemModel>>();
        //打开数据库
        messageDB.open();

        List<ShowItemModel> dbShowItemList = messageDB.readShowItemList();
        if (dbShowItemList != null) {
            for (ShowItemModel s : dbShowItemList) {
                Log.i("test", "DBreadText" + s.getShowText());
                showList.add(s);
                ArrayList<CommentItemModel> commentItemList3 = new ArrayList<CommentItemModel>();
                CommentItemModel noneCommentItem3 = new CommentItemModel();
                noneCommentItem3.setMsgKey("-1");
                commentItemList3.add(noneCommentItem3);
                childCommentList.add(commentItemList3);
            }
        }

        List<CommentItemModel> dbCommentList = messageDB.readCommentItemList();
        for (CommentItemModel dbComm : dbCommentList) {
            for (int i = 0; i < showList.size(); i++) {
                if (showList.get(i).getMsgKey().equals(dbComm.getMsgKey())) {
                    int commIndex = childCommentList.get(i).size();
                    childCommentList.get(i).add(commIndex - 1, dbComm);
                    Log.i("test", "添加Comm" + dbComm.getCommentVoice());
                }
            }
        }

        ShowItemModel showItem = new ShowItemModel();
        showItem.setMsgKey("0");
        showItem.setShowUsername("寸草心官方");
        showItem.setShowText("尊敬的用户：欢迎使用寸草心，\n" +
                "这里是消息的浏览页，\n" +
                "点击右上角的“+”发送消息，\n" +
                "长按消息下方的按钮发送语音。\n" +
                ":)");
        showItem.setShowTime("官方消息");
        ArrayList<String> showImgs = new ArrayList<>();
        showItem.setShowImagesList(showImgs);
        showList.add(showItem);

        ArrayList<CommentItemModel> commentItemList = new ArrayList<CommentItemModel>();
        CommentItemModel noneCommentItem1 = new CommentItemModel();
        noneCommentItem1.setMsgKey("-1");
        commentItemList.add(noneCommentItem1);

        childCommentList.add(commentItemList);
    }

    private boolean createConversation(long groupId) {
        mConversation = Conversation.createGroupConversation(groupId);
        GroupInfo groupInfo = (GroupInfo) mConversation.getTargetInfo();
        List<UserInfo> userInfos = groupInfo.getGroupMembers();
        for (UserInfo userInfo : userInfos) {
            if (userInfo.getUserName().equals(JMessageClient.getMyInfo().getUserName())) {
                return true;
            }
        }
        return false;
    }

    private void sendToGroup(ShowItemModel showItem) {
        //发送的消息体设置
        ShowItemModel showItemForSend = new ShowItemModel();
        showItemForSend.setShowText(showItem.getShowText());
        showItemForSend.setMsgKey(showItem.getMsgKey());
        showItemForSend.setGroupBelongToList(showItem.getGroupBelongToList());

        ShowMsgSender showMsgSender;
        if (showItem.getShowImagesList() != null) {
            //有图片
            showItemForSend.setShowImagesList(showItem.getShowImagesList());
            //发送带图片的消息
            showMsgSender = sendFactory.ShowMsgSenderProduce(SHOW_IMAGE);
            showMsgSender.sendMsg(showItemForSend, mConversation);
        } else if (showItem.getShowVideo() != null) {
            //有视频
            showItemForSend.setShowVideo(showItem.getShowVideo());
            showMsgSender = sendFactory.ShowMsgSenderProduce(SHOW_VIDEO);
            showMsgSender.sendMsg(showItemForSend, mConversation);
        } else if (showItem.getShowImagesList() == null && showItem.getShowVideo() == null) {
            //发送文字信息
            showMsgSender = sendFactory.ShowMsgSenderProduce(SHOW_TEXT);
            showMsgSender.sendMsg(showItemForSend, mConversation);
        }
    }

    /***
     * 数据源
     *
     * @author Administrator
     *
     */
    class MyexpandableListAdapter extends BaseExpandableListAdapter {
        private LayoutInflater inflater;

        public MyexpandableListAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        // 返回父列表个数
        @Override
        public int getGroupCount() {
            return showList.size();
        }

        // 返回子列表个数
        @Override
        public int getChildrenCount(int groupPosition) {
            return childCommentList.get(groupPosition).size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return showList.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return childCommentList.get(groupPosition).get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {

            return true;
        }

        private ShowHolder showHolder;
        private CustomVideoView showVideoView;
        private ImageView showVideoPlayBtn;

        @Override
        public View getGroupView(final int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            showHolder = null;
            if (convertView == null) {
                showHolder = new ShowHolder();
                convertView = inflater.inflate(R.layout.show_item, null);
                showHolder.showUsernameTv = (TextView) convertView.findViewById(R.id.show_username_tv);
                showHolder.showUserAvatarCIv = (CircleImageView) convertView.findViewById(R.id.show_user_avatar_civ);
                showHolder.showTextTv = (TextView) convertView.findViewById(R.id.show_text_content_tv);
                showHolder.showTimeTv = (TextView) convertView.findViewById(R.id.show_time_tv);
                showHolder.expandedIv = (ImageView) convertView.findViewById(R.id.expanded_iv);
                showHolder.showImageRv = (RecyclerView) convertView.findViewById(R.id.show_recycler_view);
                convertView.setTag(showHolder);
            } else {
                showHolder = (ShowHolder) convertView.getTag();
            }

            showVideoView = (CustomVideoView) convertView.findViewById(R.id.show_video_view);
            showVideoPlayBtn = (ImageView) convertView.findViewById(R.id.show_video_play_video_iv);

            final ShowItemModel showItem = (ShowItemModel) getGroup(groupPosition);
            showHolder.showUsernameTv.setText(showItem.getShowUsername());
            showHolder.showTextTv.setText(showItem.getShowText());
            Log.i("test", "time " + showItem.getShowTime());
            showHolder.showTimeTv.setText(showItem.getShowTime() + " ");
            File avatarFile = showItem.getShowAvatar();
            if (avatarFile != null) {
                Log.i("test", "头像不为NULL ");
                showHolder.showUserAvatarCIv.setImageBitmap(BitmapFactory.decodeFile(avatarFile.getPath()));
            } else {
                Log.i("test", "头像为NULL");
            }

            if (showItem.getShowImagesList() != null) {
                showVideoView.setVisibility(View.GONE);
                showVideoPlayBtn.setVisibility(View.GONE);
                showHolder.showImageRv.setVisibility(View.VISIBLE);
                ShowPhotoAdapter showPhotoAdapter = new ShowPhotoAdapter(MainActivity.this, showItem.getShowImagesList());
                showHolder.showImageRv.setLayoutManager(new StaggeredGridLayoutManager(3, OrientationHelper.VERTICAL));
                showHolder.showImageRv.setAdapter(showPhotoAdapter);
                showHolder.showImageRv.addOnItemTouchListener(new RecyclerItemClickListener(MainActivity.this,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                PhotoPreview.builder()
                                        .setPhotos(((ShowItemModel) getGroup(groupPosition)).getShowImagesList())
                                        .setCurrentItem(position)
                                        .setShowDeleteButton(false)
                                        .start(MainActivity.this);
                            }
                        }));
                showHolder.expandedIv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (expandableListView.isGroupExpanded(groupPosition)) {
                            showHolder.expandedIv.setImageResource(R.drawable.ic_angle_up);
                            expandableListView.collapseGroup(groupPosition);
                        } else {
                            showHolder.expandedIv.setImageResource(R.drawable.ic_angle_down);
                            expandableListView.expandGroup(groupPosition);
                        }
                    }
                });
            } else if (showItem.getShowVideo() != null) {
                showHolder.showImageRv.setVisibility(View.GONE);
                showVideoView.setVisibility(View.VISIBLE);
                showVideoPlayBtn.setVisibility(View.VISIBLE);
                showVideoView.setVideoHeight(LinearLayoutCompat.LayoutParams.WRAP_CONTENT);
                showVideoView.setVideoWidth(LinearLayoutCompat.LayoutParams.WRAP_CONTENT);
                showVideoView.setVideoPath(showItem.getShowVideo());
                Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(showItem.getShowVideo(), MediaStore.Video.Thumbnails.MINI_KIND);
                showVideoView.setBackgroundDrawable(new BitmapDrawable(bitmap));
                showVideoView.setVideoPath(showItem.getShowVideo());
                showVideoPlayBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i("test", "点击了播放按钮");
                        showVideoPlayBtn.setVisibility(View.INVISIBLE);
                        showVideoView.setBackgroundDrawable(null);
                        showVideoView.start();
                        showVideoView.requestFocus();
                    }
                });

                showVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        showVideoPlayBtn.setVisibility(View.VISIBLE);
                    }
                });
            } else if (showItem.getShowImagesList() == null && showItem.getShowVideo() == null) {
                showVideoView.setVisibility(View.GONE);
                showVideoPlayBtn.setVisibility(View.GONE);
                showHolder.showImageRv.setVisibility(View.GONE);
            }

            return convertView;
        }

        @Override
        public View getChildView(final int groupPosition, final int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {
            CommentHolder commentHolder;
            if (convertView == null) {
                commentHolder = new CommentHolder();
                convertView = inflater.inflate(R.layout.comment_item, null);
                commentHolder.commentUsernameTv = (TextView) convertView.findViewById(R.id.comment_username_tv);
                commentHolder.playVoiceCommentBtn = (Button) convertView.findViewById(R.id.play_comment_audio_btn);
                commentHolder.timeofvoice = (TextView) convertView.findViewById(R.id.time_of_voice_tv);
                commentHolder.sendVoiceCommentBtn = (RecordButton) convertView.findViewById(R.id.send_comment_audio_btn);
                commentHolder.commentTimeTv = (TextView) convertView.findViewById(R.id.comment_time_tv);
                convertView.setTag(commentHolder);
            } else {
                commentHolder = (CommentHolder) convertView.getTag();
            }

            final CommentItemModel commentItem = (CommentItemModel) getChild(groupPosition, childPosition);
            if (commentItem.getMsgKey().equals("-1")) {
                commentHolder.commentUsernameTv.setVisibility(View.GONE);
                commentHolder.playVoiceCommentBtn.setVisibility(View.GONE);
                commentHolder.timeofvoice.setVisibility(View.GONE);
                commentHolder.commentTimeTv.setVisibility(View.GONE);
                commentHolder.sendVoiceCommentBtn.setVisibility(View.VISIBLE);
                commentHolder.sendVoiceCommentBtn.setOnFinishedRecordListener(new RecordButton.OnFinishedRecordListener() {
                    @Override
                    public void onFinishedRecord(String audioPath, long intervalTime) {
                        CommentItemModel commentItemForSend = new CommentItemModel();
                        commentItemForSend.setMsgKey(((ShowItemModel) getGroup(groupPosition)).getMsgKey());
                        commentItemForSend.setCommentUsername(JMessageClient.getMyInfo().getNickname());
                        commentItemForSend.setCommentVoice(audioPath);
                        Log.i("test", "audioPath：" + audioPath);
                        commentItemForSend.setCommentLength((int) (intervalTime / 1000));
                        commentItemForSend.setCommKey(UUIDKeyUtil.getUUIDKey());
                        commentItemForSend.setCommentTime(GetCurrentTimeUtil.getCurrentTime(new Date()));

                        childCommentList.get(groupPosition).add(getChildrenCount(groupPosition) - 1, commentItemForSend);
                        adapter.notifyDataSetChanged();

                        messageDB.insertComment(commentItemForSend);

                        List<Long> groupBelongtoList = ((ShowItemModel) getGroup(groupPosition)).getGroupBelongToList();
                        if (groupBelongtoList != null) {
                            Log.i("test", "要发送的群id不为空");
                            for (long groupId : groupBelongtoList) {
                                Log.i("test", "要发送的群id：" + groupId);
                                if (createConversation(groupId)) {
                                    SendMessageUtil.sendVoice(commentItemForSend, mConversation);
                                } else {
                                    Log.i("test", "不属于那个群");
                                }
                            }
                        }
                    }
                });
            } else {
                int mywidth=0;
                int time_temp=0;
                time_temp=commentItem.getCommentLength();
                if(time_temp>14) {
                    mywidth=340;
                }else{
                    mywidth=120+(time_temp-1)*15;
                }
                android.view.ViewGroup.LayoutParams widthPar = commentHolder.playVoiceCommentBtn.getLayoutParams();
                widthPar.width=mywidth;
                commentHolder.playVoiceCommentBtn.setLayoutParams(widthPar);
                commentHolder.commentUsernameTv.setVisibility(View.VISIBLE);
                commentHolder.playVoiceCommentBtn.setVisibility(View.VISIBLE);
                commentHolder.timeofvoice.setVisibility(View.VISIBLE);
                commentHolder.commentTimeTv.setVisibility(View.VISIBLE);
                commentHolder.sendVoiceCommentBtn.setVisibility(View.GONE);
                commentHolder.commentUsernameTv.setText(commentItem.getCommentUsername());
                commentHolder.timeofvoice.setText("" + commentItem.getCommentLength() + "''");
                commentHolder.commentTimeTv.setText(commentItem.getCommentTime());
                commentHolder.playVoiceCommentBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mediaPlayer = new MediaPlayer();
                        initMediaPlayer(commentItem.getCommentVoice());
                        Log.i("test", "点击了播放语音" + commentItem.getCommentVoice());
                        mediaPlayer.start();
                    }
                });
            }

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

    @Override
    public boolean onGroupClick(final ExpandableListView parent, final View v,
                                int groupPosition, final long id) {

        return false;
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v,
                                int groupPosition, int childPosition, long id) {
        Toast.makeText(MainActivity.this,
                childCommentList.get(groupPosition).get(childPosition).getCommentUsername() + "的评论", Toast.LENGTH_SHORT)
                .show();

        return false;
    }

    class ShowHolder {
        TextView showUsernameTv;
        CircleImageView showUserAvatarCIv;
        TextView showTextTv;
        RecyclerView showImageRv;
        ImageView expandedIv;
        TextView showTimeTv;
    }

    class CommentHolder {
        TextView commentUsernameTv;
        Button playVoiceCommentBtn;
        TextView timeofvoice;
        RecordButton sendVoiceCommentBtn;
        TextView commentTimeTv;
    }

    @Override
    public View getPinnedHeader() {
        View headerView = (ViewGroup) getLayoutInflater().inflate(R.layout.show_item, null);
        headerView.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return headerView;
    }

    @Override
    public void updatePinnedHeader(View headerView, int firstVisibleGroupPos) {
        ShowItemModel firstVisibleShowItem = (ShowItemModel) adapter.getGroup(firstVisibleGroupPos);
        TextView showUsernameTv = (TextView) headerView.findViewById(R.id.show_username_tv);
        TextView showTextTv = (TextView) headerView.findViewById(R.id.show_text_content_tv);
        TextView showTimeTv = (TextView) headerView.findViewById(R.id.show_time_tv);
        CircleImageView showAvatarIv = (CircleImageView) headerView.findViewById(R.id.show_user_avatar_civ);
        showUsernameTv.setText(firstVisibleShowItem.getShowUsername());
        showTextTv.setText(firstVisibleShowItem.getShowText());
        showTimeTv.setText(firstVisibleShowItem.getShowTime());

        File avatarFile = firstVisibleShowItem.getShowAvatar();
        if (avatarFile != null) {
            Log.i("test", "头像不为NULL ");
            showAvatarIv.setImageBitmap(BitmapFactory.decodeFile(avatarFile.getPath()));
        } else {
            Log.i("test", "头像为NULL");
        }
    }

    @Override
    public boolean giveUpTouchEvent(MotionEvent event) {
        if (expandableListView.getFirstVisiblePosition() == 0) {
            View view = expandableListView.getChildAt(0);
            if (view != null && view.getTop() >= 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 666) {
            try {
                myName.setText(JMessageClient.getMyInfo().getNickname());
            } catch (Exception e) {
                myName.setText(JMessageClient.getMyInfo().getUserName());
            }
            JMessageClient.getMyInfo().getAvatarBitmap(new GetAvatarBitmapCallback() {
                @Override
                public void gotResult(int i, String s, Bitmap bitmap) {
                    if (i == 0) {
                        leftUserAvatarCIV.setImageBitmap(bitmap);
                        myAvatarCIV.setImageBitmap(bitmap);
                    }
                }
            });
        }

        if (requestCode == REQUEST_SEND_MSG_ITEM && resultCode == ShowMsgEditActivity.RESULT_SEND_MSG_ITEM) {
            ShowItemModel showItem = (ShowItemModel) data.getSerializableExtra("showItem");
            List<Long> groupBelongtoList = showItem.getGroupBelongToList();

            for (int i = 0; i < groupBelongtoList.size(); i++) {
                Log.i("test", "要发送的群id：" + groupBelongtoList.get(i));
                createConversation(groupBelongtoList.get(i));
                sendToGroup(showItem);
            }

            UserInfo myInfo = JMessageClient.getMyInfo();
            showItem.setShowUsername(myInfo.getNickname());
            showItem.setShowAvatar(myInfo.getAvatarFile());
            showItem.setShowTime(GetCurrentTimeUtil.getCurrentTime(new Date()));
            showList.add(0, showItem);

            ArrayList<CommentItemModel> commentItemModels = new ArrayList<CommentItemModel>();
            CommentItemModel noneComment = new CommentItemModel();
            noneComment.setMsgKey("-1");
            commentItemModels.add(noneComment);
            childCommentList.add(0, commentItemModels);

            adapter.notifyDataSetChanged();
            Log.i("test", "show内容" + showItem.getShowText());

            messageDB.insertShow(showItem, data.getStringExtra("showType"));
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

    public void onEventMainThread(MessageEvent event) {

        Message msg = event.getMessage();
        switch (msg.getContentType()) {
            case text:
                //处理文字消息
                new RecieveTextTask().execute(msg);
                break;
        }
    }

    public void onEvent(MessageEvent event) {
        Message msg = event.getMessage();

        switch (msg.getContentType()) {
            case voice:
                //处理语音消息
                new RecieveVoiceTask().execute(msg);
                break;
            case image:
                //处理图片消息
                new RecieveImageTask().execute(msg);
                break;
            case file:
                //处理视频消息
                new RecieveVideoTask().execute(msg);
                break;
        }
    }

    public void onEvent(ContactNotifyEvent event) {
        String fromUsername = event.getFromUsername();

        switch (event.getType()) {
            case invite_received://收到好友邀请
                userDB.agreenewfriend(fromUsername);
                userDB.getFriendMessage(fromUsername);
                break;
            default:
                break;
        }
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

    class RecieveTextTask extends AsyncTask<Message, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Message... params) {
            Message msg = params[0];
            TextContent textContent = (TextContent) msg.getContent();
            Log.i("test", "收到文本消息" + textContent.getText() + "  showKey " + textContent.getStringExtra("showKey"));
            //跳过群重复消息
            String tShowKey = textContent.getStringExtra("showKey");
            if (tShowKey.equals(checkShowKey)) {
                Log.i("test", "同一个文本消息，跳过");
                return false;
            }
            checkShowKey = tShowKey;

            UserInfo tUserInfo = msg.getFromUser();
            ShowItemModel textShowItem = new ShowItemModel();
            textShowItem.setShowUsername(tUserInfo.getNickname());
            textShowItem.setShowText(textContent.getText());
            textShowItem.setMsgKey(tShowKey);
            String[] groupIds = textContent.getStringExtra("groupBelongTo").split(",");
            List<Long> groupIdBelongTo = new ArrayList<Long>();
            for (int i = 0; i < groupIds.length; i++) {
                groupIdBelongTo.add(Long.parseLong(groupIds[i]));
                Log.i("test", "解析出群:" + groupIds[i]);
            }
            textShowItem.setGroupBelongToList(groupIdBelongTo);
            textShowItem.setShowAvatar(tUserInfo.getAvatarFile());
            textShowItem.setShowTime(GetCurrentTimeUtil.getCurrentTime(new Date()));
            showList.add(0, textShowItem);

            ArrayList<CommentItemModel> commentItemModels = new ArrayList<CommentItemModel>();
            CommentItemModel noneComment = new CommentItemModel();
            noneComment.setMsgKey("-1");
            commentItemModels.add(noneComment);
            childCommentList.add(0, commentItemModels);
            Log.i("test", "background");
            messageDB.insertShow(textShowItem, SHOW_TEXT);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean) {
                adapter.notifyDataSetChanged();
                Log.i("test", "post");
            }
        }
    }

    class RecieveImageTask extends AsyncTask<Message, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Message... params) {
            Message msg = params[0];
            Log.i("test", "收到图片消息");
            ImageContent imageContent = (ImageContent) msg.getContent();
            UserInfo iUserInfo = msg.getFromUser();
            Map iMsgMap = imageContent.getStringExtras();
            //如果收到的图片key和上一次的一样说明是一个showItem
            if (CheckRecievedShowItem.getMsgKey() != null) {
                if ((iMsgMap.get("showKey")).equals(CheckRecievedShowItem.getMsgKey())) {
                    Log.i("test", "收到多图片消息之一");
                    if (!imageContent.getStringExtra("recieveFlag").equals(imgRecieveFlag)) {
                        Log.i("test", "跳过多余群图片之一");
                        return false;
                    }
                    if (iMsgMap.containsKey("showText")) {
                        CheckRecievedShowItem.setShowText((String) iMsgMap.get("showText"));
                    }
                    ArrayList<String> imgPaths = CheckRecievedShowItem.getShowImagesList();
                    imgPaths.add(imageContent.getLocalThumbnailPath());
                    if (imageContent.getNumberExtra("imageNum").equals(imageContent.getNumberExtra("imageCount"))) {
                        String[] iGroupIds = ((String) iMsgMap.get("groupBelongTo")).split(",");
                        Log.i("test", "解析出群");
                        List<Long> iGroupIdBelongTo = new ArrayList<Long>();
                        for (int i = 0; i < iGroupIds.length; i++) {
                            iGroupIdBelongTo.add(Long.parseLong(iGroupIds[i]));
                            Log.i("test", "解析出群:" + iGroupIds[i]);
                        }
                        CheckRecievedShowItem.setGroupBelongToList(iGroupIdBelongTo);
                        showList.add(0, CheckRecievedShowItem);

                        ArrayList<CommentItemModel> commentItemModels = new ArrayList<CommentItemModel>();
                        CommentItemModel noneComment = new CommentItemModel();
                        noneComment.setMsgKey("-1");
                        commentItemModels.add(noneComment);
                        childCommentList.add(0, commentItemModels);

                        messageDB.insertShow(CheckRecievedShowItem, SHOW_IMAGE);
                    }

                    return true;
                }
            }

            Log.i("test", "收到新图片消息");
            imgRecieveFlag = imageContent.getStringExtra("recieveFlag");
            CheckRecievedShowItem = new ShowItemModel();
            CheckRecievedShowItem.setMsgKey((String) iMsgMap.get("showKey"));
            CheckRecievedShowItem.setShowUsername(iUserInfo.getNickname());
            CheckRecievedShowItem.setShowText("");
            CheckRecievedShowItem.setShowTime(GetCurrentTimeUtil.getCurrentTime(new Date()));
            if (iMsgMap.containsKey("showText")) {
                CheckRecievedShowItem.setShowText((String) iMsgMap.get("showText"));
            }
            ArrayList<String> newImagepaths = new ArrayList<String>();
            newImagepaths.add(imageContent.getLocalThumbnailPath());
            CheckRecievedShowItem.setShowImagesList(newImagepaths);
            Log.i("test", "图片path:" + imageContent.getLocalThumbnailPath());

            if (imageContent.getNumberExtra("imageNum").equals(imageContent.getNumberExtra("imageCount"))) {
                Log.i("test", "新图片添加到ListView 只有一张图片");
                String[] iGroupIds = ((String) iMsgMap.get("groupBelongTo")).split(",");
                List<Long> iGroupIdBelongTo = new ArrayList<Long>();
                for (int i = 0; i < iGroupIds.length; i++) {
                    iGroupIdBelongTo.add(Long.parseLong(iGroupIds[i]));
                    Log.i("test", "解析出群:" + iGroupIds[i]);
                }
                CheckRecievedShowItem.setGroupBelongToList(iGroupIdBelongTo);
                CheckRecievedShowItem.setShowAvatar(iUserInfo.getAvatarFile());
                showList.add(0, CheckRecievedShowItem);

                ArrayList<CommentItemModel> commentItemModels = new ArrayList<CommentItemModel>();
                CommentItemModel noneComment = new CommentItemModel();
                noneComment.setMsgKey("-1");
                commentItemModels.add(noneComment);
                childCommentList.add(0, commentItemModels);

                messageDB.insertShow(CheckRecievedShowItem, SHOW_IMAGE);
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    class RecieveVideoTask extends AsyncTask<Message, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Message... params) {
            final Message msg = params[0];
            Log.i("test", "收到视频消息");
            final FileContent fileContent = (FileContent) msg.getContent();
            //跳过群重复消息
            final String fShowKey = fileContent.getStringExtra("showKey");
            if (fShowKey.equals(checkShowKey)) {
                Log.i("test", "同一个视频消息，跳过");
                return false;
            }
            checkShowKey = fShowKey;

            fileContent.downloadFile(msg, new DownloadCompletionCallback() {
                @Override
                public void onComplete(int i, String s, File file) {
                    if (i == 0) {
                        Log.i("test", "收到的视频下载完成" + i + s + file.getPath());
                        ShowItemModel videoShowItem = new ShowItemModel();
                        UserInfo fUserInfo = msg.getFromUser();
                        videoShowItem.setShowUsername(fUserInfo.getNickname());
                        videoShowItem.setShowText(fileContent.getStringExtra("showText"));
                        videoShowItem.setMsgKey(fShowKey);
                        videoShowItem.setShowTime(GetCurrentTimeUtil.getCurrentTime(new Date()));
                        videoShowItem.setShowVideo(file.getPath());
                        String[] groupIds = fileContent.getStringExtra("groupBelongTo").split(",");
                        List<Long> groupIdBelongTo = new ArrayList<Long>();
                        for (int j = 0; j < groupIds.length; j++) {
                            groupIdBelongTo.add(Long.parseLong(groupIds[j]));
                            Log.i("test", "解析出群:" + groupIds[j]);
                        }
                        videoShowItem.setGroupBelongToList(groupIdBelongTo);
                        final Bitmap[] avatarBitmap = new Bitmap[1];
                        fUserInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                            @Override
                            public void gotResult(int i, String s, Bitmap bitmap) {
                                if (i == 0) {
                                    avatarBitmap[0] = bitmap;
                                }
                            }
                        });
                        videoShowItem.setShowAvatar(fUserInfo.getAvatarFile());
                        showList.add(0, videoShowItem);

                        ArrayList<CommentItemModel> commentItemModels = new ArrayList<CommentItemModel>();
                        CommentItemModel noneComment = new CommentItemModel();
                        noneComment.setMsgKey("-1");
                        commentItemModels.add(noneComment);
                        childCommentList.add(0, commentItemModels);
                        Log.i("test", "background");
                        adapter.notifyDataSetChanged();

                        messageDB.insertShow(videoShowItem, SHOW_VIDEO);
                    }
                }
            });

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean) {
                Log.i("test", "视频接收成功");
            }
        }
    }

    class RecieveVoiceTask extends AsyncTask<Message, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Message... params) {
            Message msg = params[0];
            Log.i("test", "收到语音消息");
            VoiceContent voiceContent = (VoiceContent) msg.getContent();
            UserInfo vUserInfo = msg.getFromUser();
            Map vMsgMap = voiceContent.getStringExtras();
            //跳过群重复消息
            String vCommKey = (String) vMsgMap.get("commKey");
            if (vCommKey.equals(checkCommKey)) {
                Log.i("test", "同一个语音消息，跳过");
                return false;
            }
            checkCommKey = vCommKey;

            CommentItemModel commentItem = new CommentItemModel();
            commentItem.setMsgKey((String) vMsgMap.get("showKey"));
            commentItem.setCommentUsername(vUserInfo.getNickname());
            commentItem.setCommentLength(Integer.parseInt((String) vMsgMap.get("voiceLength")));
            commentItem.setCommentTime(GetCurrentTimeUtil.getCurrentTime(new Date()));
            Log.i("test", "voiceContent.getLocalPath" + voiceContent.getLocalPath());
            commentItem.setCommentVoice(voiceContent.getLocalPath());

            for (int j = 0; j < showList.size(); j++) {
                if (showList.get(j).getMsgKey().equals(commentItem.getMsgKey())) {
                    List<CommentItemModel> commentItemModels = childCommentList.get(j);
                    commentItemModels.add(commentItemModels.size() - 1, commentItem);
                    childCommentList.remove(j);
                    childCommentList.add(j, commentItemModels);
                    Log.i("test", "添加一条语音消息");
                    messageDB.insertComment(commentItem);
                    return true;
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean) {
                adapter.notifyDataSetChanged();
            }
        }
    }
}
