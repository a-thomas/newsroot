package com.codexperiments.newsroot.domain.entity;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

import java.io.IOException;

import static com.codexperiments.newsroot.data.remote.parser.TwitterParser.FORMAT_DATE_TO_LONG;

@JsonType
public class Tweet {
    @JsonField(fieldName = "id")
    long id;
    @JsonField(fieldName = "text")
    String text;
    @JsonField(fieldName = "created_at", valueExtractFormatter = FORMAT_DATE_TO_LONG)
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
        try {
            return Tweet__JsonHelper.serializeToJson(this);
        } catch (IOException ioException) {
            return ioException.getMessage();
        }
    }
}
