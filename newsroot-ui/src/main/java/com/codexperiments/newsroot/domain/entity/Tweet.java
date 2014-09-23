package com.codexperiments.newsroot.domain.entity;

import com.codexperiments.newsroot.utils.ClassUtil;
import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

@JsonType
public class Tweet {
    @JsonField(fieldName = "id")
    long id;
    @JsonField(fieldName = "text")
    String text;
    @JsonField(fieldName = "created_at")
    long createdAt;
    @JsonField(fieldName = "user")
    User user;

    public Tweet() {
        super();
        id = -1;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return ClassUtil.toString(this);
    }
}
