package com.codexperiments.newsroot.core.domain.repository;

public class AlreadyExistsException extends RepositoryException {
    public AlreadyExistsException(String detailMessage) {
        super(detailMessage);
    }

    public AlreadyExistsException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public AlreadyExistsException(Throwable throwable) {
        super(throwable);
    }
}
