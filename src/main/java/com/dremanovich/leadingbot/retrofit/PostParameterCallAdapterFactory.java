package com.dremanovich.leadingbot.retrofit;

import com.dremanovich.leadingbot.retrofit.annotations.PostParameter;
import retrofit2.CallAdapter;
import retrofit2.Retrofit;
import retrofit2.http.FormUrlEncoded;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class PostParameterCallAdapterFactory extends CallAdapter.Factory {

    private final List<CallAdapter.Factory> callAdapterFactories;
    private final Map<Integer, PostParameter> registration;

    public PostParameterCallAdapterFactory(List<CallAdapter.Factory> callAdapterFactories,
                                           Map<Integer, PostParameter> reg) {
        this.callAdapterFactories = callAdapterFactories;
        this.registration = reg;
    }

    @Override
    public CallAdapter<?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        PostParameter annotation = getPostParameterAnnotation(annotations);
        FormUrlEncoded formUrlEncodedAnnotation = getFormUrlEncodedAnnotation(annotations);

        if (formUrlEncodedAnnotation != null){
            for (int i = 0; i < callAdapterFactories.size(); i++) {
                CallAdapter<?> adapter = callAdapterFactories
                        .get(i).get(returnType, annotations, retrofit);
                if (adapter != null) {
                    if (annotation != null) {
                        // get whatever info you need from your annotation
                        return new PostParameterWrappingCallAdapter<>(adapter, registration, annotation);
                    }
                    return adapter;
                }
            }
        }

        return null;
    }


    private PostParameter getPostParameterAnnotation(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (PostParameter.class == annotation.annotationType()) {
                return (PostParameter) annotation;
            }
        }
        return null;
    }

    private FormUrlEncoded getFormUrlEncodedAnnotation(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (FormUrlEncoded.class == annotation.annotationType()) {
                return (FormUrlEncoded) annotation;
            }
        }
        return null;
    }
}
