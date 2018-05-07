package BasicTest;

import io.reactivex.Observable;
import java.util.Random;

public class DistinctExample {
    public static void main(String[] args) {

//        randomInteger();
        takeTest();
    }

    static void randomInteger() {
        Observable<Integer> randomInts = Observable.create(subscriber -> {
            Random random = new Random();

            while (!subscriber.isDisposed()) {
                subscriber.onNext(random.nextInt(1000));
            }
        });

        randomInts.subscribe(System.out::println);
    }

    /*
    10개의 고유한 난수를 뽑아내고 싶다
    내장된 distinct() 연산자가 업스트림 Observable에서 이미 발생한 값을 버려 다운스트림에는 확실하게 단 하나의 이벤트만 지나가도록 한다.
     */
    static void takeTest() {
        Observable<Integer> randomInts = Observable.create(subscriber -> {
            Random random = new Random();

            while (!subscriber.isDisposed()) {
                subscriber.onNext(random.nextInt(1000));
            }
        });

        Observable<Integer> uniqueRandomInts = randomInts
                .distinct()
                .take(10);

        uniqueRandomInts.subscribe(System.out::println);
    }
}
