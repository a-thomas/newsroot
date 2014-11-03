package com.codexperiments.newsroot.core.domain.repository;

import com.codexperiments.newsroot.core.domain.entities.User;

public interface UserRepository {
    void feed(User user);

    void save(User user) throws AlreadyExistsException, ChangedMeanwhileException;

    void delete(User user) throws ChangedMeanwhileException;

    User byId(long userId) throws DoesNotExistException;
}
