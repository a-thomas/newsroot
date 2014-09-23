package com.codexperiments.newsroot.domain.entity;

import com.codexperiments.newsroot.utils.ClassUtil;
import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

@JsonType
public class User {
    @JsonField(fieldName = "id")
    long id;
    @JsonField(fieldName = "name")
    String name;
    @JsonField(fieldName = "screen_name")
    String screenName;

    public User() {
        super();
        id = -1;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    @Override
    public String toString() {
        return ClassUtil.toString(this);
    }
}
