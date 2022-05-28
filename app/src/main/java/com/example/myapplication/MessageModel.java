package com.example.myapplication;

import com.google.firebase.database.IgnoreExtraProperties;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

@IgnoreExtraProperties
public class MessageModel {
    public String chatContent;
    public String messageTime;
    public String userName;

    public MessageModel() {

    }

    public MessageModel(String content, String userName) {
        this.chatContent = content;
        this.userName = userName;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        this.messageTime = formatter.format(new Date());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageModel that = (MessageModel) o;
        return chatContent.equals(that.chatContent) && messageTime.equals(that.messageTime) && userName.equals(that.userName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatContent, messageTime, userName);
    }
}
