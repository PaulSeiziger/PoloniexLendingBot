package com.dremanovich.leadingbot.retrofit.interceptors;

import com.dremanovich.leadingbot.helpers.RequestHelper;
import com.dremanovich.leadingbot.retrofit.annotations.PostParameter;
import okhttp3.*;

import java.io.IOException;
import java.util.Map;

public class PostParameterInterceptor implements Interceptor {
    private final Map<Integer, PostParameter> registration;

    public PostParameterInterceptor(Map<Integer, PostParameter> reg) {
        this.registration = reg;
    }

    private Integer identify(Request request) {
        // make sure this is the same method you use in the CallAdapter
        return (request.url() + request.method()).hashCode();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        PostParameter annotation = registration.get(identify(request));

        if (annotation != null) {
            Request.Builder requestBuilder = request.newBuilder();

            String postBodyString = RequestHelper.bodyToString(request);
            postBodyString += ((postBodyString.length() > 0) ? "&" : "") + annotation.key() + "=" + annotation.value();
            request = requestBuilder
                    .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded;charset=UTF-8"), postBodyString))
                    .build();
        }
        return chain.proceed(request);
    }
}
