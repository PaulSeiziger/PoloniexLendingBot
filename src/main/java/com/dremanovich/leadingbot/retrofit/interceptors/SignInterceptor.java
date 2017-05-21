package com.dremanovich.leadingbot.retrofit.interceptors;

import com.dremanovich.leadingbot.helpers.RequestHelper;
import okhttp3.Interceptor;
import okhttp3.Request;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

/**
 * Created by PavelDremanovich on 20.05.17.
 */
public class SignInterceptor implements Interceptor {

    private String key;
    private String secret;

    public SignInterceptor(String key, String secret) {
        this.key = key;
        this.secret = secret;
    }

    @Override
    public okhttp3.Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (request.method().equals("POST")){
            request = request.newBuilder()
                    .addHeader("Key", key)
                    .addHeader("Sign", generateHMAC(RequestHelper.bodyToString(request), secret)).build();
        }

        return chain.proceed(request);
    }

    private String generateHMAC( String datas, String secret )
    {
        Mac mac;
        String result = "";
        try
        {
            final SecretKeySpec secretKey = new SecretKeySpec( secret.getBytes(), "HmacSHA512" );
            mac = Mac.getInstance( "HmacSHA512" );
            mac.init( secretKey );

            final byte[] macData = mac.doFinal( datas.getBytes( ) );
            result = toHexString(macData);
        }
        catch ( final NoSuchAlgorithmException | InvalidKeyException e )
        {
            e.printStackTrace();
        }

        return result;

    }

    private static String toHexString(byte[] bytes) {
        Formatter formatter = new Formatter();

        for (byte b : bytes) {
            formatter.format("%02x", b);
        }

        return formatter.toString();
    }

}
