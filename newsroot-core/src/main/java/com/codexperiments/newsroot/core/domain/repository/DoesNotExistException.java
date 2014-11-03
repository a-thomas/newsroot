package com.codexperiments.newsroot.core.domain.repository;

public class DoesNotExistException extends RepositoryException {
    public DoesNotExistException(String detailMessage) {
        super(detailMessage);
    }

    public DoesNotExistException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public DoesNotExistException(Throwable throwable) {
        super(throwable);
    }
}
