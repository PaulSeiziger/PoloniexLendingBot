package com.dremanovich.leadingbot.retrofit.interceptors;

import com.dremanovich.leadingbot.helpers.RequestHelper;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;

public class RequestPrinterInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        long t1 = System.nanoTime();
        System.out.println("Sending request \n" + request.url() + "\n" + chain.connection() + "\n" + request.headers());
        System.out.println("REQUEST BODY BEGIN\n" + RequestHelper.bodyToString(request) + "\nREQUEST BODY END\n");

        Response response = chain.proceed(request);

        ResponseBody responseBody = response.body();
        String responseBodyString = response.body().string();

        // now we have extracted the response body but in the process
        // we have consumed the original reponse and can't read it again
        // so we need to build a new one to return from this method

        Response newResponse = response.newBuilder().body(ResponseBody.create(responseBody.contentType(), responseBodyString.getBytes())).build();

        long t2 = System.nanoTime();
        System.out.println("Received response for " + response.request().url() + " in \n" + ((t2 - t1) / 1e6d) + "\n" + response.headers());
        System.out.println("RESPONSE BODY BEGIN:\n " + responseBodyString + "\nRESPONSE BODY END\n");

        return newResponse;
    }

}
