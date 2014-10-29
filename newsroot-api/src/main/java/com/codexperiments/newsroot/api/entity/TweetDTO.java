package com.codexperiments.newsroot.api.entity;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

import java.io.IOException;

import static com.codexperiments.newsroot.api.mapper.JsonFormatter.FORMAT_DATE_TO_LONG;

@JsonType
public class TweetDTO {
    @JsonField(fieldName = "id")
    long id;
    @JsonField(fieldName = "text")
    String text;
    @JsonField(fieldName = "created_at", valueExtractFormatter = FORMAT_DATE_TO_LONG)
    long createdAt;
    @JsonField(fieldName = "user")
    UserDTO user;

    public TweetDTO() {
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

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    @Override
    public String toString() {
        try {
            return TweetDTO__JsonHelper.serializeToJson(this);
        } catch (IOException ioException) {
            return ioException.getMessage();
        }
    }
}
