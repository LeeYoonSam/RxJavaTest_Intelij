package BasicTest.RetrofitRxJava;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class LogInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        System.out.println("====== Request ======");
        System.out.println("intercept: request method " + request.method());
        System.out.println("intercept: request url " + request.headers());
        System.out.println("intercept: connection " + chain.connection());


        Response response = chain.proceed(request);
        System.out.println("====== Response ======");
        System.out.println("intercept: response requested url " + response.request().url());
        System.out.println("intercept: header " + response.headers());

        System.out.println("responseCode: " + response.code());
        if(response.code() == 500) {
            System.out.println("intercept: Server Error(500)");
        } else  if(response.code() >= 400 && response.code() < 500) {
            System.out.println("intercept: 400 Error");
        }

        return response;
    }
}
