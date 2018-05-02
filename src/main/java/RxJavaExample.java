import io.reactivex.Observable;

public class RxJavaExample {
    public static void main(String[] args) {

//        Observable<Integer> rangeObservable = Observable.range(1,5);
//        rangeObservable.subscribe(it -> System.out.print(it + " "));


        Observable<Integer> observable = Observable.create(subscriber -> {
            subscriber.onNext(1);
            subscriber.onNext(2);
            subscriber.onNext(3);
            subscriber.onNext(4);
            subscriber.onNext(5);

            subscriber.onComplete();
        });

        observable.subscribe(it -> System.out.print(it + " "));
    }
}
