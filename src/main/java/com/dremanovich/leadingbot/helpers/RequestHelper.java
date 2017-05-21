package com.dremanovich.leadingbot.helpers;

import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;

import java.io.IOException;

public class RequestHelper {
    public static String bodyToString(final Request request){
        String result = "";
        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            final RequestBody body = copy.body();

            if (body != null){
                body.writeTo(buffer);
            }

            result = buffer.readUtf8();
        } catch (final IOException e) {
             e.printStackTrace();
        }

        return result;
    }
}
