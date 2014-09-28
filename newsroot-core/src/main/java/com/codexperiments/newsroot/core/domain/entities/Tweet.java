package com.codexperiments.newsroot.core.domain.entities;

import com.codexperiments.quickdao.annotation.Column;
import com.codexperiments.quickdao.annotation.Id;
import com.codexperiments.quickdao.annotation.Table;

@Table("TWT_TWEET")
public class Tweet {
    @Id
    @Column("TWT_ID")
    public long id;
    @Column("TWT_TEXT")
    public String text;
    @Column("TWT_CREATED_AT")
    public long createdAt;
    @Column("TWT_USR_ID")
    public User user;

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
        return "Tweet{" + "id=" + id + ", text='" + text + '\'' + ", createdAt=" + createdAt + ", user=" + user + '}';
    }
}
