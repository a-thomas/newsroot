package com.codexperiments.newsroot.core.domain.entities;

public class Tweet {
    long id;
    long version;
    String text;
    long createdAt;
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

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
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
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        Tweet otherTweet = (Tweet) other;
        if (id != otherTweet.id) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return "Tweet{" + "id=" + id + ", text='" + text + '\'' + ", createdAt=" + createdAt + ", user=" + user + '}';
    }
}
