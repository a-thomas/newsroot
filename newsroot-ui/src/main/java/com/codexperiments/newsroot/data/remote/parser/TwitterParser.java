package com.codexperiments.newsroot.data.remote.parser;

import android.annotation.TargetApi;
import android.os.Build;
import com.codexperiments.newsroot.domain.entity.Tweet;
import com.codexperiments.newsroot.domain.entity.Tweet__JsonHelper;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

public class TwitterParser {
    //region Formatters
    private static final DateTimeFormatter DATE_TIME = DateTimeFormat.forPattern("EEE MMM d HH:mm:ss Z yyyy")
                                                                     .withZone(DateTimeZone.UTC)
                                                                     .withLocale(Locale.ENGLISH);


    public static final String FORMAT_DATE_TO_LONG = "com.codexperiments.newsroot.data.remote.parser.TwitterParser.formatDateToLong(jp)";

    public static final long formatDateToLong(com.fasterxml.jackson.core.JsonParser jsonParser) throws IOException {
        String value = (jsonParser.getCurrentToken() != JsonToken.VALUE_NULL) ? jsonParser.getText() : null;
        return (value != null) ? DATE_TIME.parseDateTime(value).getMillis() : -1;
    }
    //endregion


    //region Parser
    private JsonFactory factory = new JsonFactory();


    public List<Tweet> parseTweetList(String json) {
        try {
            final JsonParser parser = factory.createParser(json);
            return parseList(parser, new Callable<Tweet>() {
                @Override
                public Tweet call() throws Exception {
                    return Tweet__JsonHelper.parseFromJson(parser);
                }
            });
        } catch (IOException ioException) {
            // TODO Conversion
            throw new RuntimeException(ioException);
        }
    }
    //endregion


    //region Utilities
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private static <TType> List<TType> parseList(JsonParser parser, Callable<TType> parseAction) throws IOException {
        try {
            if (parser.nextToken() != JsonToken.START_ARRAY) throw new IOException("No JSON Array found");
            List<TType> list = new ArrayList<>(/*TODO Capacity*/);

            boolean finished = false;
            while (!finished) {
                switch (parser.nextToken()) {
                    case START_OBJECT:
                        list.add(parseAction.call());
                        break;
                    case END_ARRAY:
                        finished = true;
                        break;
                    case NOT_AVAILABLE:
                        throw new IOException();
                    default:
                        break;
                }
            }
            return list;
        } catch (IOException ioException) {
            throw ioException;
        } catch (Exception exception) {
            // Need to catch Exception because sadly Callable has to be used.
            throw new IOException("Unknown parsing exception", exception);
        } finally {
            parser.close();
        }
    }
    //endregion
}
