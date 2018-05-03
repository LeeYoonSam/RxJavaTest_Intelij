package BasicTest.FlatMap;

import BasicTest.BasicTest;
import io.reactivex.Observable;

import java.time.DayOfWeek;
import java.util.concurrent.TimeUnit;

import static io.reactivex.Observable.empty;
import static io.reactivex.Observable.just;
import static io.reactivex.Observable.timer;

public class Flatmap {
    public static void main(String[] args) {

//        flatmapTest();
//        System.out.println();
//
//        delayFlatmapTest();
//        System.out.println();
//
//        delayFlatmapTest2();
//        System.out.println();

        flatmapOrder();
        System.out.println();
    }

    static void flatmapTest() {
        // map
        just(1, 2, 3, 4, 5)
                .map(x -> x * 2)
                .filter(x -> x != 10)
                .subscribe(System.out::println);

        System.out.println();

        // flatmap
        just(1, 2, 3, 4, 5)
                .flatMap(x -> just(x * 2))
                .flatMap(x -> (x != 10) ? just(x) : empty())
                .subscribe(System.out::println);
    }

    static void delayFlatmapTest() {
//        Observable
//                .just("Lorem", "ipsum", "dolor", "sit", "amet","consectetur", "adipiscing","elit")
//                .delay(word -> timer(word.length(), TimeUnit.SECONDS))
//                .subscribe(System.out::println);

        Observable
                .just("Lorem", "ipsum", "dolor", "sit", "amet","consectetur", "adipiscing","elit")
                .flatMap(word -> timer(word.length(), TimeUnit.SECONDS).map(x -> word))
                .subscribe(System.out::println);

        try {
            TimeUnit.SECONDS.sleep(15);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static void delayFlatmapTest2() {
        Observable.just(10L, 1L)
                .flatMap(x -> just(x).delay(x, TimeUnit.SECONDS))
                .subscribe(System.out::println);

        try {
            TimeUnit.SECONDS.sleep(10L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    static void flatmapOrder() {
        /*
        flatmap을 사용하게 되면 순설르 보장 받지 못한다.

        output:
            Mon-0
            Sun-0
            Mon-1
            Sun-1
            Mon-2
            Mon-3
            Sun-2
            Mon-4
            Sun-3
            Sun-4
         */
//        Observable
//                .just(DayOfWeek.SUNDAY, DayOfWeek.MONDAY)
//                .flatMap(dow -> loadRecordsFor(dow))
//                .subscribe(System.out::println);


        /*
        concatMap() 으로 순서 유지하기

        output:

         */
        System.out.println("concatMap으로 순서 보장");
        Observable
                .just(DayOfWeek.SUNDAY, DayOfWeek.MONDAY)
                .concatMap(dow -> loadRecordsFor(dow))
                .subscribe(System.out::println);

        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static Observable<String> loadRecordsFor(DayOfWeek dow) {
        switch (dow) {
            case SUNDAY:
                return Observable
                        .interval(90, TimeUnit.MILLISECONDS)
                        .take(5)
                        .map(i -> "Sun-" + i);
            case MONDAY:
                return Observable
                        .interval(65, TimeUnit.MILLISECONDS)
                        .take(5)
                        .map(i -> "Mon-" + i);
        }

        return empty();
    }
}
