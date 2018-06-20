package BasicTest.VertxRxJava;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.core.file.FileSystem;
import io.vertx.reactivex.core.http.HttpClient;
import io.vertx.reactivex.core.http.HttpClientRequest;
import io.vertx.reactivex.core.http.HttpClientResponse;
import org.reactivestreams.Publisher;

import java.time.ZonedDateTime;

/**
 * Example of Vertx and RxJava
 * http://www.baeldung.com/vertx-rx-java
 */
public class VertxRxJavaExample {

    /*
    Vert.x의 FileSystem은 반응적인 방식으로 파일 시스템에 대한 액세스를 제공하는 반면 Vert.x의 HttpClient는 HTTP에 대해 동일한 작업을 수행합니다.
     */
    static Vertx vertx = Vertx.vertx();

    /*
    vertx-rx-java2 라이브러리는 io.vertx.core.Vertx와 io.vertx.reactivex.core.Vertx의 두 클래스를 제공합니다.
    전자는 Vert.x를 기반으로하는 애플리케이션의 일반적인 시작점이지만,
    후자의 io.vertx.reactivex.core.Vertx는 RxJava와의 통합을 위해 사용해야 합니다.
     */
    static FileSystem fileSystem = vertx.fileSystem();
    static HttpClient httpClient = vertx.createHttpClient();

    public static void main(String[] args) {
        ReactiveChain();
    }


    /*
    4. Reactive Chain

    reactive context에서는 몇 가지 간단한 reactive 연산자를 연결하여 의미있는 계산을 쉽게 구할 수 있습니다.
     */
    public static void ReactiveChain() {
        fileSystem
                .rxReadFile("/Users/Albert-IM/Documents/Dev_App/GitHub/RxJavaTest_Intelij/src/main/java/BasicTest/VertxRxJava/cities.txt").toFlowable()
                .flatMap(buffer -> Flowable.fromArray(buffer.toString().split("\\r?\\n")))
                .flatMap(city -> searchByCityName(httpClient, city))
                .flatMap(HttpClientResponse::toFlowable)
                .map(extractingWoeid())
                .flatMap(cityId -> getDataByPlaceId(httpClient, cityId))
                .flatMap(toBufferFlowable())
                .map(Buffer::toJsonObject)
                .map(toCityAndDayLength())
                .subscribe(System.out::println, Throwable::printStackTrace);

    }

    static Flowable<HttpClientResponse> searchByCityName(HttpClient httpClient, String cityName) {
        HttpClientRequest req = httpClient.get(
                new RequestOptions()
                        .setHost("www.metaweather.com")
                        .setPort(443)
                        .setSsl(true)
                        .setURI(String.format("/api/location/search/?query=%s", cityName)));
        return req
                .toFlowable()
                .doOnSubscribe(subscription -> req.end());
    }

    private static Function<Buffer, Long> extractingWoeid() {
        return cityBuffer -> cityBuffer
                .toJsonArray()
                .getJsonObject(0)
                .getLong("woeid");
    }

    static Flowable<HttpClientResponse> getDataByPlaceId(
            HttpClient httpClient, long placeId) {

        return autoPerformingReq(
                httpClient,
                String.format("/api/location/%s/", placeId));
    }

    static Flowable<HttpClientResponse> autoPerformingReq(HttpClient httpClient, String format) {
        HttpClientRequest req = httpClient.get(
                new RequestOptions()
                        .setHost("www.metaweather.com")
                        .setPort(443)
                        .setSsl(true)
                        .setURI(format));
        return req
                .toFlowable()
                .doOnSubscribe(subscription -> req.end());
    }

    static Function<HttpClientResponse, Publisher<? extends Buffer>>
    toBufferFlowable() {
        return response -> response
                .toObservable()
                .reduce(
                        Buffer.buffer(),
                        Buffer::appendBuffer).toFlowable();
    }

    static Function<JsonObject, CityAndDayLength> toCityAndDayLength() {
        return json -> {
            ZonedDateTime sunRise = ZonedDateTime.parse(json.getString("sun_rise"));
            ZonedDateTime sunSet = ZonedDateTime.parse(json.getString("sun_set"));
            String cityName = json.getString("title");
            return new CityAndDayLength(
                    cityName, sunSet.toEpochSecond() - sunRise.toEpochSecond());
        };
    }

    static class CityAndDayLength {
        String cityName;
        long zoneDataTime;

        public CityAndDayLength(String cityName, long zoneDataTime) {
            this.cityName = cityName;
            this.zoneDataTime = zoneDataTime;
        }

        @Override
        public String toString() {
            return cityName + " is zoneDataTime " + zoneDataTime;
        }
    }
}
