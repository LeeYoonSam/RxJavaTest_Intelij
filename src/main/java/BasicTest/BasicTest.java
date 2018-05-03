package BasicTest;

import io.reactivex.Observable;

import static io.reactivex.Observable.empty;
import static io.reactivex.Observable.just;

public class BasicTest {
    public static void main(String[] args) {
        quizObservable();
        System.out.println();

        usingDoOnNext();
        System.out.println();

        flatmapTest();
        System.out.println();
    }

    static void quizObservable() {
        just(8, 9 ,10)
                .filter(i -> i % 3 > 0)
                .map(i -> "#" + i * 10)
                .filter(s -> s.length() < 4)
                .subscribe(System.out::println);
    }

    static void usingDoOnNext() {
        /*
        데이터가 하나씩 흐르는면서 모든 단계를 거치는것을 확인할 수 있다.
        output:
            A: 8
            B: 8
            C: #80
            D: #80
            A: 9
            A: 10
            B: 10
            C: #100
         */
        just(8, 9, 10)
                .doOnNext(i -> System.out.println("A: " + i))
                .filter(i -> i % 3 > 0)
                .doOnNext(i -> System.out.println("B: " + i))
                .map(i -> "#" + i * 10)
                .doOnNext(s -> System.out.println("C: " + s))
                .filter(s -> s.length() < 4)
                .subscribe(s -> System.out.println("D: " + s));
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
}
