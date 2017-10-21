package com.dremanovich.lendingbot.retrofit;

import com.dremanovich.lendingbot.retrofit.annotations.PostParameter;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.CallAdapter;

import java.lang.reflect.Type;
import java.util.Map;

public class PostParameterWrappingCallAdapter<T> implements CallAdapter<T> {

    private final CallAdapter<T> adapter;
    private final Map<Integer, PostParameter> registration;
    private final PostParameter info;

    PostParameterWrappingCallAdapter(CallAdapter<T> adapter, Map<Integer, PostParameter> reg, PostParameter info) {
        this.adapter = adapter;
        this.registration = reg;
        this.info = info;
    }

    @Override
    public Type responseType() {
        return adapter.responseType();
    }

    @Override
    public <R> T adapt(Call<R> call) {
        Request request = call.request();
        registration.put(identify(request), info);
        return adapter.adapt(call);
    }

    private Integer identify(Request request) {
        // this is very experimental but it does the job currently
        return (request.url() + request.method()).hashCode();
    }
}
