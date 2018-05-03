package BasicTest;

import static io.reactivex.Observable.just;

public class BasicTest {
    public static void main(String[] args) {
        quizObservable();
        System.out.println();

        usingDoOnNext();
        System.out.println();


    }

    static void quizObservable() {
        just(8, 9, 10)
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
}