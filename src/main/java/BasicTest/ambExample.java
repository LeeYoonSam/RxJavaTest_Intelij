package BasicTest;

import io.reactivex.Observable;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ambExample {
    public static void main(String[] args) {

    }

    /*
    amb() 연산자
    모든 업스트림 Observable을 구독한 다음 맨 처음 항복을 방출할 때까지 대기하다가 Observable 중 하나가 첫 번째 이벤트를 방출하면,
    amb()는 나머지 스트림을 모두 버리고 자신을 깨운 첫 번째 Observable의 이벤트만 전달한다.
     */
    static void ambTest() {

//        Observable.amb(
//                stream(100, 17, "S"),
//                stream(200, 10, "F")
//        ).subscribe(System.out::println);
    }

    /*
    initialDelay - 어떤 Observable을 먼저 방출 시킬지 통제하는 변수
     */
    static Observable<String> stream(int initialDelay, int interval, String name) {
        return Observable
                .interval(initialDelay, interval, TimeUnit.MILLISECONDS)
                .map(x -> name + x);
    }
}
