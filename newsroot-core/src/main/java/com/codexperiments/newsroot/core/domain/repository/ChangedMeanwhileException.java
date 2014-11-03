package com.codexperiments.newsroot.core.domain.repository;

public class ChangedMeanwhileException extends RepositoryException {
    public ChangedMeanwhileException(String detailMessage) {
        super(detailMessage);
    }

    public ChangedMeanwhileException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ChangedMeanwhileException(Throwable throwable) {
        super(throwable);
    }
}
