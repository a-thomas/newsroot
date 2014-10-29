package com.codexperiments.newsroot.core.domain.repository;

import com.codexperiments.newsroot.core.domain.entities.User;

public interface UserRepository {
    void save(User user);

    void delete(User user);

    User byId(long userId);
}
