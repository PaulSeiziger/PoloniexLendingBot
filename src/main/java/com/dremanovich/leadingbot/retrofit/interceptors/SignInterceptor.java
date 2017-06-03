package com.dremanovich.leadingbot.retrofit.interceptors;

import com.dremanovich.leadingbot.api.NonceReminder;
import com.dremanovich.leadingbot.bot.PoloniexBot;
import com.dremanovich.leadingbot.helpers.RequestHelper;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;


public class SignInterceptor implements Interceptor {

    private static final Logger log = LogManager.getLogger(SignInterceptor.class);

    private String key;
    private String secret;
    private NonceReminder nonceReminder;

    public SignInterceptor(String key, String secret, NonceReminder nonce) {
        this.key = key;
        this.secret = secret;
        this.nonceReminder = nonce;
    }

    @Override
    public okhttp3.Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        //TODO: Create Retrofit annotation for sign request
        if (request.method().equals("POST")){
            //Add Nonce Parameter
            nonceReminder.next();

            Request.Builder requestBuilder = request.newBuilder();

            String postBodyString = RequestHelper.bodyToString(request);
            postBodyString += ((postBodyString.length() > 0) ? "&" : "") +  "nonce=" + nonceReminder.get();
            requestBuilder.post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded;charset=UTF-8"), postBodyString));

            //Sign request
            request = requestBuilder.build();
            requestBuilder.addHeader("Key", key)
                    .addHeader("Sign", generateHMAC(RequestHelper.bodyToString(request), secret));

            request = requestBuilder.build();
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
            log.error(e.getMessage(), e);
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
