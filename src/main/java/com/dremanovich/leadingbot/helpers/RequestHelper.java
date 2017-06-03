package com.dremanovich.leadingbot.helpers;

import com.dremanovich.leadingbot.bot.PoloniexBot;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class RequestHelper {

    private static final Logger log = LogManager.getLogger(RequestHelper.class);

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
             log.debug(e.getMessage(), e);
        }

        return result;
    }
}
