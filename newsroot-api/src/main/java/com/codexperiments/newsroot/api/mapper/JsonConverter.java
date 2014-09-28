package com.codexperiments.newsroot.api.mapper;

import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public class JsonConverter implements Converter {
    private final JsonMapper parser;

    public JsonConverter(JsonMapper parser) {
        this.parser = parser;
    }

    @Override
    public Object fromBody(TypedInput body, Type type) throws ConversionException {
        InputStream inStream = null;
        try {
            // Collection of objects
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Type[] argTypes = parameterizedType.getActualTypeArguments();
                if (argTypes.length != 1) {
                    throw new IllegalArgumentException(String.format("One parameter required for the generic type %s", type));
                }

                Class<?> rawClass = (Class<?>) parameterizedType.getRawType();
                if (!List.class.isAssignableFrom(rawClass)) {
                    throw new IllegalArgumentException(String.format("Only the List type can be parsed, not %s", rawClass));
                }

                inStream = body.in();
                Class<?> paramClass = (Class<?>) argTypes[0];
                return parser.mapListFrom(paramClass, body.in());
            }
            // Single object
            else if (type instanceof Class<?>) {
                inStream = body.in();
                return parser.mapFrom((Class<?>) type, body.in());
            }
            // Unknown case
            else throw new IllegalArgumentException(String.format("Unhandled Type class %s", type.getClass()));
        } catch (IOException ioException) {
            throw new ConversionException(ioException);
        } finally {
            try {
                if (inStream != null) inStream.close();
            } catch (IOException e) {
                // Ignored.
            }
        }
    }

    @Override
    public TypedOutput toBody(Object object) {
        return null;
    }
}
