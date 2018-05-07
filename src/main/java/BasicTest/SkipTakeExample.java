package BasicTest;

import io.reactivex.Observable;

/**
 * skip(), takeWhile() 등을 사용해서 잘게 쪼개거나 잘라내기
 */
public class SkipTakeExample {

    public static void main(String[] args) {
        takeAndSkip();
        takeUntilTest();
        countTest();
        allAndExistsTest();
    }

    /*
    take(n) 연산자는 원본 Observable의 처음 n개의 값만을 업스트림에서 방출하고 중단하여 결국은 구독을 해지한다.
    업스트림에 n개 미만의 항목만 있을 경우 더 빨리 마친다.

    skip(n) take와 정확히 반대
    업스트림 Observable에서 처음 n개 항목을 버린 다음 n+1번째 이벤트부터 방출 시작

    두 연산자 모두 음수값은 그냥 0으로 간주하고 Observable 크기를 초과해도 문제 없다.
     */
    static void takeAndSkip() {

        System.out.println();

        Observable baseObservable = Observable.range(1,5);

        System.out.println("take(3)");
        baseObservable.take(3).subscribe(System.out::println);

        System.out.println("skip(3)");
        baseObservable.skip(3).subscribe(System.out::println);

        System.out.println("skip(5)");
        // 초과
        baseObservable.skip(5).subscribe(System.out::println);

        /*
        takeLast(n)과 skipLast(n)

        takeLast(n)
        스트림 완료 이전의 n개 값만 방출
        내부적으로 이 연산자는 뒤의 n개 항목을 위한 버퍼를 유지하는데 종료 알림을 받으면 즉시 모든 퍼버 내용 방출
        무한스트림에 taskLast()를 쓰면 아무값도 방출되지 않는다.

        skipLast(n)
        뒤쪽의 n개만을 제외한 업스트림 Observable의 모든 값을 방출
        내부적으로 skipLast()는 n+1번째 항목을 받았을 때 업스트림의 첫 번째 항목을 방출하고, n+2번째를 받았을때 두 번째를 방출
         */
        System.out.println("takeLast(2)");
        baseObservable.takeLast(2).subscribe(System.out::println);

        System.out.println("skipLast(2)");
        baseObservable.skipLast(2).subscribe(System.out::println);
    }

    /*
    takeUntil()은 원본 Observable에서 값을 방출하다가 predicate와 일치하는 첫 번째 항목을 방출한 다음 완료하고 구독을 해지
    takeWhile()은 이벤트를 방출하는 Observable에서 조건에 따라 구독을 해지할 수 있는 수단이므로 매우 중요
    그렇지 않으면 연산자는 어떤식으로든 Subscription객체와 상호 작용해야 하는데 연산자를 호출하지 않는 한 이는 불가능하다.
     */
    static void takeUntilTest() {

        System.out.println();

        // 3이 나올때까지만 구독
        Observable<Integer> takeUntilObservable = Observable.range(1,5).takeUntil(x -> x == 3);

        // 3이 나오기전까지 구독
        Observable<Integer> takeWhileObservable = Observable.range(1, 5).takeWhile(x -> x != 3);

        System.out.println("takeUntil()");
        takeUntilObservable.subscribe(System.out::print);

        System.out.println();

        System.out.println("takeWhile()");
        takeWhileObservable.subscribe(System.out::print);
    }

    /*
    count()
    업스트림 Observable에서 방출한 이벤트 개수를 세는 연산자
    무한 스트림인 경우는 절대로 값을 방출할 수 없다.
    count()는 reduce()로 쉽게 구현 가능
     */
    static void countTest() {

        System.out.println();
        System.out.println("count()");
        Observable<String> size = Observable
                .just("A", "B", "C", "D");

        // count() 사용
        size.count()
                .subscribe(System.out::println);

        // reduce() 사용
        size.reduce(0, (sizeSoFar, ch) -> sizeSoFar + 1)
                .subscribe(System.out::println);
    }

    /*
    all(predicate)
    업스트림의 모든 값이 술어와 맞으면 종료할때 true를 반환

    contains()
    단순히 업스트림의 값과 상수를 비교할때 사용
     */
    static void allAndExistsTest() {
        System.out.println();
        System.out.println("all(), exists()");
        Observable<Integer> numbers = Observable.range(1, 5);

        numbers.all(x -> x != 4).subscribe(System.out::println);
        numbers.contains(4).subscribe(System.out::println);
    }
}


