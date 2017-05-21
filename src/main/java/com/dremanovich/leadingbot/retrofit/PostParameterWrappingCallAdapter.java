package com.dremanovich.leadingbot.retrofit;

import com.dremanovich.leadingbot.retrofit.annotations.PostParameter;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.CallAdapter;

import java.lang.reflect.Type;
import java.util.Map;

public class PostParameterWrappingCallAdapter<RETURN_TYPE> implements CallAdapter<RETURN_TYPE> {

    private final CallAdapter<RETURN_TYPE> adapter;
    private final Map<Integer, PostParameter> registration;
    private final PostParameter info;

    PostParameterWrappingCallAdapter(CallAdapter<RETURN_TYPE> adapter, Map<Integer, PostParameter> reg, PostParameter info) {
        this.adapter = adapter;
        this.registration = reg;
        this.info = info;
    }

    @Override
    public Type responseType() {
        return adapter.responseType();
    }

    @Override
    public <R> RETURN_TYPE adapt(Call<R> call) {
        Request request = call.request();
        registration.put(identify(request), info);
        return adapter.adapt(call);
    }

    private Integer identify(Request request) {
        // this is very experimental but it does the job currently
        return (request.url() + request.method()).hashCode();
    }
}
