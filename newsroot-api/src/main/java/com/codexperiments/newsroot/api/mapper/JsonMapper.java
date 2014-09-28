package com.codexperiments.newsroot.api.mapper;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.instagram.common.json.JsonAnnotationProcessorConstants;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO Cache to avoid reflection?
 */
public class JsonMapper {
    private JsonFactory factory = new JsonFactory();


    public <TParsed> TParsed mapFrom(Class<TParsed> parsedClass, InputStream in) throws IOException {
        JsonParser parser = null;
        try {
            parser = factory.createParser(in);
            parser.nextToken();
            return (TParsed) runParseMethod(parser, parsedClass);
        } finally {
            try {
                if (parser != null) parser.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public <TParsed> List<TParsed> mapListFrom(Class<TParsed> parsedClass, InputStream in) throws IOException {
        JsonParser parser = null;
        try {
            parser = factory.createParser(in);
            if (parser.nextToken() != JsonToken.START_ARRAY) throw new IOException("JSON Array expected");
            List<TParsed> resultList = new ArrayList<>(/*TODO Capacity*/);

            boolean finished = false;
            do {
                switch (parser.nextToken()) {
                    case START_OBJECT:
                        resultList.add((TParsed) runParseMethod(parser, parsedClass));
                        break;
                    case END_ARRAY:
                        finished = true;
                        break;
                    default:
                        throw new IOException("Invalid JSON stream");
                }
            } while (!finished);

            return resultList;
        } finally {
            try {
                if (parser != null) parser.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Object runParseMethod(com.fasterxml.jackson.core.JsonParser parser, Class<?> parsedClass) throws IOException {
        try {
            String parserClassName = parsedClass.getName() + JsonAnnotationProcessorConstants.HELPER_CLASS_SUFFIX;
            Class<?> parseClass = Class.forName(parserClassName);
            Method parseMethod = parseClass.getMethod("parseFromJson", com.fasterxml.jackson.core.JsonParser.class);

            return parseMethod.invoke(null, parser);
        } catch (ClassNotFoundException classNotFoundException) {
            throw new IOException(String.format("Cannot find parse class %s", parsedClass), classNotFoundException);
        } catch (NoSuchMethodException noSuchMethodException) {
            throw new IOException(String.format("Cannot find parse method for class %s", parsedClass), noSuchMethodException);
        } catch (IllegalAccessException | InvocationTargetException invocationException) {
            throw new IOException(String.format("Cannot run parse method for class %s", parsedClass), invocationException);
        }
    }
}
