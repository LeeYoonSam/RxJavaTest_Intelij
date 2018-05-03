package BasicTest;

import io.reactivex.Observable;

import java.util.concurrent.TimeUnit;

public class RxJavaExample {
    public static void main(String[] args) {

        Observable<Integer> observable = Observable.create(subscriber -> {
            subscriber.onNext(1);
            subscriber.onNext(2);
            subscriber.onNext(3);
            subscriber.onNext(4);
            subscriber.onNext(5);

            subscriber.onComplete();
        });

        observable.subscribe(it -> System.out.print(it + " "));
        System.out.println();
        delayed(10).subscribe(it -> System.out.println(it));
    }

    // 새로 쓰레드를 생성해서 10초간 실행을 멈추는 어설픈 구현
    static <T> Observable<T> delayed(T x) {
        return Observable.create(
                subscriber -> {
                    Runnable r = () -> {
                        sleep(10, TimeUnit.SECONDS);

                        if(!subscriber.isDisposed()) {
                            subscriber.onNext(x);
                            subscriber.onComplete();
                        }
                    };

                    new Thread(r).start();

                    // 깔끔하게 구현하려면 최소한 java.util.concurrent.ScheduledExecutorService정도는 사용해야 한다. rxjava1이라서 변경된듯.. 적용안
//                    final Thread thread = new Thread(r);
//                    thread.start();
//                    subscriber.add(Subscriptions.create(thread::interrupt));


                }
        );
    }

    static void sleep(int timeout, TimeUnit unit) {
        try {
            unit.sleep(timeout);
        } catch (InterruptedException ignored){ }
    }


    static class Data {
        String data;
    }

    static Data load(int id) {
        return new Data();
    }


    static Observable<Data> rxLoad(int id) {
        return Observable.create( subscriber -> {
            // 견고한 스트림을 만들려면 예외를 아무도 이해할 수 없는 언어상의 부차적 특징이 아닌 일급 객체로 다룰 필요가 있다.
            try {
                subscriber.onNext(load(id));
                subscriber.onComplete();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        });
    }

    // rxLoad, rxLoadFromcallable 두 가지 메서드는 의미상으로 동일하나 훨씬 간결하며, create()보다 더 나은 몇 가지 장점도 있다.
    static Observable<Data> rxLoadFromcallable(int id) {
        return Observable.fromCallable( () ->
                load(id));
    }

}
