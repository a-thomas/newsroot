package com.codexperiments.newsroot.core.domain.repository;

public class RepositoryException extends Exception {
    public RepositoryException(String detailMessage) {
        super(detailMessage);
    }

    public RepositoryException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public RepositoryException(Throwable throwable) {
        super(throwable);
    }
}
