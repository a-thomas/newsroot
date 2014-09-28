package com.codexperiments.newsroot.core.domain.entities;

import com.codexperiments.quickdao.annotation.Column;
import com.codexperiments.quickdao.annotation.Id;
import com.codexperiments.quickdao.annotation.Table;

@Table("USR_USER")
public class User {
    @Id
    @Column("USR_ID")
    public long id;
    @Column("USR_NAME")
    public String name;
    @Column("USR_SCREEN_NAME")
    public String screenName;

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
        return "User{" + "id=" + id + ", name='" + name + '\'' + ", screenName='" + screenName + '\'' + '}';
    }
}
