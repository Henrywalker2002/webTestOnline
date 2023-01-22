package com.model;

import org.bson.codecs.pojo.annotations.BsonProperty;

public class Account {
    public String username, name, password, email, avatar, profile;
    public int type;// type : 0 is student, 1 is teacher and 2 is admin
    @BsonProperty(value = "class")
    int class_; 

    public Account() {}
    public Account(String username, String name, String password, String email, String avatar, int type, String profile) {
        this.username = username;
        this.type = type;
        this.password = password;
        this.email = email;
        this.name = name;
        this.avatar = avatar;
        this.profile = profile;
    }

    public Account(String username, String name, String password, String email, String avatar, int type, int class_) {
        this.username = username;
        this.type = type;
        this.password = password;
        this.email = email;
        this.name = name;
        this.avatar = avatar;
        this.class_ = class_;
    }
    
    public void setClass_(int class_) {
        this.class_ = class_;
    }

    public int getClass_() {
        return this.class_;
    }
}

