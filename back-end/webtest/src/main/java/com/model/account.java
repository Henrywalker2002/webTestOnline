package com.model;

import java.util.Objects;

public class account {
    public String id, username, name, password, email, avatar;
    public Boolean type;

    account() {}
    account(String id , String username, String name, String password, String email, String avatar, Boolean type) {
        this.id = id;
        this.username = username;
        this.type = type;
        this.password = password;
        this.email = email;
        this.name = name;
        this.avatar = avatar;
    }

    // account(JSONObject rows) {
    //     this.id = rows.get("id").get("_id");

    // }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof account)){
            return false;
        }
        account acc = (account) o;
        return Objects.equals(this.id, acc.id);
    }
}
