package com.example.jasper.ccxapp.entitiy;

import cn.bmob.v3.BmobObject;

/**
 * Created by Administrator on 2017/3/29 0029.
 */

public class NewFriend extends BmobObject {
    private String requestFriend;
    private String responseFriend;
    private String message;

    public String getRequestFriend() {
        return requestFriend;
    }

    public void setRequestFriend(String requestFriend) {
        this.requestFriend = requestFriend;
    }

    public String getResponseFriend() {
        return responseFriend;
    }

    public void setResponseFriend(String responseFriend) {
        this.responseFriend = responseFriend;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
