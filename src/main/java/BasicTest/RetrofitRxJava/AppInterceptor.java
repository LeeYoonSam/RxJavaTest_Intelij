package BasicTest.RetrofitRxJava;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class AppInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request mRequest = chain.request();
        Request newRequest = mRequest.newBuilder()
                .addHeader("promiseHeader", "TestHeader")
                .build();

        Response mResponse = chain.proceed(newRequest);
        return mResponse;
    }
}
