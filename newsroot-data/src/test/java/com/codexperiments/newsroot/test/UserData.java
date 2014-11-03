package com.codexperiments.newsroot.test;

import com.codexperiments.newsroot.core.domain.entities.User;

import java.util.UUID;

public class UserData {
    public static long USER_1_LEMONDE = 1L;

    private static volatile long idSequence = 1000000;

    public static User createUser() {
        return createUser(++idSequence, 0);
    }

    public static User createUser(long id, long version) {
        UUID uuid = UUID.randomUUID();

        User user = new User();
        user.setId(id);
        user.setName("Name " + uuid.toString());
        user.setScreenName("ScreenName " + uuid.toString());
        user.setVersion(version);
        return user;
    }
}
