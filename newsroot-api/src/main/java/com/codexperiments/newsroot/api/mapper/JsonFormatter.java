package com.codexperiments.newsroot.api.mapper;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.util.Locale;

public class JsonFormatter {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormat.forPattern("EEE MMM d HH:mm:ss Z yyyy")
                                                                     .withZone(DateTimeZone.UTC)
                                                                     .withLocale(Locale.ENGLISH);


    public static final String FORMAT_DATE_TO_LONG = "com.codexperiments.newsroot.api.mapper.JsonFormatter.formatDateToLong(jp)";

    public static final long formatDateToLong(JsonParser jsonParser) throws IOException {
        String value = (jsonParser.getCurrentToken() != JsonToken.VALUE_NULL) ? jsonParser.getText() : null;
        return (value != null) ? DATE_TIME.parseDateTime(value).getMillis() : -1;
    }
}
