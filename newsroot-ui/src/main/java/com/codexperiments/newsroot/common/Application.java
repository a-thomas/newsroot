package com.codexperiments.newsroot.common;

import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import javax.inject.Qualifier;

@Documented
@Retention(CLASS)
@Qualifier
public @interface Application {
}