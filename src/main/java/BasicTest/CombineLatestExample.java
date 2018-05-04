package BasicTest;


import io.reactivex.Observable;

import java.util.concurrent.TimeUnit;

public class CombineLatestExample {
    public static void main(String[] args) {
//        Observable<Long> red = Observable.interval(10, TimeUnit.MILLISECONDS);
//        Observable<Long> green = Observable.interval(10, TimeUnit.MILLISECONDS);
//
//        Observable.zip(
//                red.timestamp(),
//                green.timestamp(),
//                (r, g) -> r.getTimestameMillis() - g.getTimestameMillis()
//        ).fooreEach(System.out::println);

//        Observable.interval(5, TimeUnit.SECONDS).map(x -> "S" + x)
//                .subscribe(System.out::println);
//
//        Observable.interval(2, TimeUnit.SECONDS).map(x -> "F" + x)
//                .subscribe(System.out::println);




//        combineLatestTest();
//        withLatestFromTest();
        withLatestFromTest2();
//        startWithTest();

        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static void combineLatestTest() {
        /*
         빠른 스트림과 느린 스트림의 결합

         빠른 쪽 스트림의 이벤트를 인지하고 느린 쪽의 가장 최근 이벤트를 취한다(최소한 하나의 이벤트를 기다려야만 한다)
         차별을 두지 않으므로 느린 쪽에도 이벤트가 인지되면 빠른 쪽의 최근 이벤트를 취한다.
          */
        Observable.combineLatest(
                Observable.interval(2, TimeUnit.SECONDS).map(x -> "S" + x),
                Observable.interval(1, TimeUnit.SECONDS).map(x -> "F" + x),
                (s, f) -> f + ":" + s
        ).forEach(System.out::println);

    }

    /*
    withLatesFrom() 연산자
    combineLatest는 대칭. 즉, 결합할 부속 스트림 사이에 차이를 두지 않는다.

    한쪽 스트림에 새 값이 나타날 경우에만 다른 스트림의 최근 값과 함께 묶어 방출하기를 원할수도 있다.
    두 번째 스트림의 이벤트가 다운스트림 이벤트 방출을 촉발하지 않으며 첫 번째 스트림의 방출 시에만 사용

    ** 문제 **
    첫 번째 fast 이벤트가 나타나기 전에 등장하는 모든 slow 이벤트는 버려진다.
    묶을 대상이 없기 때문에..
     */
    static void withLatestFromTest() {
        Observable<String> fast = Observable.interval(10, TimeUnit.MILLISECONDS).map(x -> "F" + x);
        Observable<String> slow = Observable.interval(17, TimeUnit.MILLISECONDS).map(x -> "S" + x);

        slow.withLatestFrom(fast, (s,f) -> s + " : " + f)
                .forEach(System.out::println);
    }

    /*
    위의 문제를 해결하기
    slow 스트림의 모든 이벤트를 보존하고 싶다면 fast 스트림이 모종의 더미 이벤트를 즉각 방출해야한다.

    startWith() 는 기본적으로 어떤 상수값을 방출하고 이어서 원본 Observable을 방출한다.

    slow 스트림이 시작되면 fast 아이템이 없더라도 FX라는 더미를 방출한다.

output:
    S0 : FX
    S1 : FX
    S2 : FX
    S3 : FX
    S4 : FX
    S5 : FX
    S6 : F0
    S7 : F2
    S8 : F4
    S9 : F6

    어떤 slow 이벤트도 버려지지 않았다.
     */
    static void withLatestFromTest2() {
        Observable<String> fast = Observable.interval(10, TimeUnit.MILLISECONDS)
                .map(x -> "F" + x)
                .delay(100, TimeUnit.MILLISECONDS)
                .startWith("FX");

        Observable<String> slow = Observable.interval(17, TimeUnit.MILLISECONDS).map(x -> "S" + x);

        slow.withLatestFrom(fast, (s,f) -> s + " : " + f)
                .forEach(System.out::println);
    }

    static void startWithTest() {
        // 0이 바로 방출된 상태에서 시작함
        Observable.just(1, 2)
                .startWith(0)
                .subscribe(System.out::println);

    }
}
