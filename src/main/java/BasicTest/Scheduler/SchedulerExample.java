package BasicTest.Scheduler;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static BasicTest.Scheduler.RxGroceries.log;
import static java.util.concurrent.Executors.newFixedThreadPool;

public class SchedulerExample {
    static ExecutorService poolA = newFixedThreadPool(10, threadFactory("Sched-A-%d"));
    static Scheduler schedulerA = Schedulers.from(poolA);

    static ExecutorService poolB = newFixedThreadPool(10, threadFactory("Sched-B-%d"));
    static Scheduler schedulerB = Schedulers.from(poolB);

    static ExecutorService poolC = newFixedThreadPool(10, threadFactory("Sched-C-%d"));
    static Scheduler schedulerC = Schedulers.from(poolC);

    /*
    쓰레드풀을 사용할때 데이터가 많아서 특정 쓰레드에 일이 몰릴 newFixedThreadPool을 사용하면 분할해서 사용가능하다.

    ForkJoinPool - 리프가 100개 일 때까지 분활(fork)해서 각각의 수치를 위로 합쳐서(join) 계산한다
    쓰레드가 개별로 큐를 가지고 있으며, 자신의 큐에 아무것도 없으면 상대방의 큐에서 업무를 가져온다.
    최대한 노는 쓰레드가 없게하기 위한 알고리즘

    ForkJoinTask 는 두가지로 방법을  제공하는데 하나는 리턴이 없는것( RecursiveAction ) 과 리턴이 있는 것 (.RecursiveTask)이다

    출처: http://hamait.tistory.com/612 [HAMA 블로그]
      */
    static ForkJoinPool poolD = new ForkJoinPool(10);
    static Scheduler schedulerD = Schedulers.from(poolD);

    private static ThreadFactory threadFactory(String pattern) {
        return new ThreadFactoryBuilder()
                .setNameFormat(pattern)
                .build();
    }

    public static void main(String[] args) {
//        subscribeOnTest1();
//        subscribeOnTest2();
//        subscribeOnTest3();
//        solutionTest3();
//        observerOnConcurrencyTest4();
//        observerOnMultipleTest();
//        observerOnMultipleTestUpgrade();
        delaySchedulerTest();

        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static Observable<String> simple() {
        return Observable.create(subscribe -> {
            log("Subscribed");
            subscribe.onNext("A");
            subscribe.onNext("B");
            subscribe.onComplete();
        });
    }

    /**
     * schedulerA가 사용된다고 해서 schedulerB 구독이 완전히 무시 되지 않는다.
     * schedulerB는 짧은 순간이나마 사용되기는 하지만 그저 모든 일을 수행하는 schedulerA의 새로운 action을 스케쥴링할 뿐이다.
     * 여러개의 subscribeOn()은 무시될 뿐만 아니라 약간의 오버헤드까지 유발한다.
     */
    static void subscribeOnTest1() {
        log("Starting");
        Observable<String> obs = simple();
        log("Create");

        obs
                .subscribeOn(schedulerA)
                //수많은 연산자
                .subscribeOn(schedulerB)
                .subscribe(
                    x -> log("Got " + x),
                    Throwable::printStackTrace,
                    () -> log("Completed"));

        log("Exiting");
    }

    static void subscribeOnTest2() {
        log("Starting");
        Observable<String> obs = simple();
        log("Create");

        obs
                .doOnNext(x -> log(x))
                .map(x -> x + '1')
                .doOnNext(x -> log(x))
                .map(x -> x + '2')
                .subscribeOn(schedulerA)
                .doOnNext(x -> log(x))
                .subscribe(
                        x -> log("Got " + x),
                        Throwable::printStackTrace,
                        () -> log("Completed"));

        log("Exiting");
    }

    /*
     상품을 구매하기 위해 가능한 한 병렬로 처리하고자 하며 총액은 나중에 한거번에 구하려고 한다.

     * 잘못된 병렬처리
     결과는 맞지만 하나의 값만 가진 Observable이다.
     총액은 reduce()로 계산
     10개의 스레드를 사용하는 schedulerA를 사용하는데도 불구하고 실행은 완전히 순차적
     flatMap()으로 변경해도 마찬가지

     코드가 동시에 작동하지 않는데, 이벤트 흐름이 하나뿐이고 순차적으로 실행 되도록 설계했기 때문이다.

     해결책:
     제품을 방출하는 주 Observable은 병렬 처리할 수 없다.
     개별 제품을 purchase()에서 반환된 것과 같이 새로운 독립적 Observable로 만들 수 있다.
     */
    static void subscribeOnTest3() {
        RxGroceries rxGroceries = new RxGroceries();

        Single<BigDecimal> totalPrice = Observable
                .just("bread", "butter", "milk", "tomato", "cheese")
                .subscribeOn(schedulerA)
                .map(prod -> rxGroceries.doPurchase(prod, 1))
                .reduce(BigDecimal::add)
                .toSingle();

        totalPrice.subscribe(System.out::println);
    }

    /*
    * 동시성

    해결책:
    개별 제품을 purchase()에서 반환된 것과 같이 새로운 독립적 Observable로 만들 수 있다.
    flatMap()안에서 생성된 개별 부속 스트림에 schedulerA를 제공
    Scheduler가 subscribeOn()을 사용하여 새로운 Worker를 반환할 때마다 별개의 스레드에서 수행한다.
     */
    static void solutionTest3() {
        log("Starting");
        long startTime = System.currentTimeMillis();
        log("Begin: " + startTime);
        RxGroceries rxGroceries = new RxGroceries();

        Single<BigDecimal> totalPrice = Observable
                .just("bread", "butter", "milk", "tomato", "cheese","bread", "butter", "milk", "tomato", "cheese")
                .flatMap(prod ->
                        rxGroceries
                                .purchase(prod, 1)
                                .subscribeOn(schedulerA))
//                                .subscribeOn(schedulerD))
                .reduce(BigDecimal::add)
                .toSingle();

        totalPrice.subscribe(System.out::println);
        log("End: " + (System.currentTimeMillis() - startTime) / 1000 + "." + (System.currentTimeMillis() - startTime) / 1000 + (System.currentTimeMillis() - startTime) / 10);
        log("Exiting");
    }

    static void groupByTest() {
//        Observable<BigDecimal> totalPrice = Observable
//                .just("bread", "butter", "egg", "milk", "tomato", "cheese", "tomato", "egg", "egg")
//                .groupBy(prod -> prod)
//                .flatMap(grouped -> grouped
//                        .count()
//                        .map(quantity -> {
//                            String productName = grouped.getKey();
//                            return Pair.of(productName, quantity);
//                        }))
//                .flatMap(order -> store
//                        .purchase(order.getKey(), order.getValue())
//                        .subscribeOn(schedulerA))
//                .reduce(BigDecimal::add)
//                .toSingle();

    }


    /*
    선언적 동시성 처리하기

    subscribeOn()
    OnSubscribe(create() 안의 람다식)를 호출할 때 어떤 Scheduler를 사용할지 선택하게 해준다.
    그러므로 create() 내부의 어떤 코드건 다른 스레드로 밀어낼 수 있다.

    observerOn()
    observerOn()이후에 발생하는 다운 스트림 Subscriber를 호출할 때 어떤 Scheduler를 사용할 것인지 제어
    에를 들어 create() 호출은 UI블로킹을 피하기 위해 (subscribeOn(io())을 통해) io() Scheduler에서 진행
    그렇지만 UI 위젯 갱신은 UI 스레드에서 진행해야 한다. 따라서 연산자나 구독자가 UI를 바꾸기 전에 AndroidSchedulers.mainThread()와 함께 observeOn()을 사용

     */
    static void observerOnConcurrencyTest4() {
        log("Starting");
        Observable<String> obs = simple();
        log("Create");

        obs
                .doOnNext(x -> log("Found 1: " + x))
                .subscribeOn(schedulerA)
                .doOnNext(x -> log("Found 2: " + x))
                .subscribe(
                        x -> log("Got 1: " + x),
                        Throwable::printStackTrace,
                        () -> log("Completed"));

        log("Exiting");
    }

    static void observerOnMultipleTest() {
        log("Starting");
        Observable<String> obs = simple();
        log("Create");

        obs
                .doOnNext(x -> log("Found 1: " + x))
                .observeOn(schedulerB)
                .doOnNext(x -> log("Found 2: " + x))
                .observeOn(schedulerC)
                .doOnNext(x -> log("Found 3: " + x))
                .subscribeOn(schedulerA)
                .subscribe(
                        x -> log("Got 1: " + x),
                        Throwable::printStackTrace,
                        () -> log("Completed"));

        log("Exiting");
    }

    /*
    이벤트는 schedulerA에서 발생하지만, 개별 이벤트는 동시성 향상을 위해 schedulerB를 사용하여 독립적으로 처리
    구독은 또 다른 schedulerC에서 발생
    observerOn()은 UI 이벤트 디스패치(전파) 스레드를 차단하고 싶지 않은 UI가 있는 애플리케이션에서 특히 중요
    안드로이드나 스윙에서 UI를 갱신하는 등의 특정 작업은 특정 스레드 안에서 실행 되어야 한다.
    그렇지만 해당 스레드 안에서 너무 많은 작업을 하면 UI 렌더링 응답성이 떨어지므로 이런 경우 observerOn()을 subscribe() 가까이 끼워 넣어 블록 처리한다.
     */
    static void observerOnMultipleTestUpgrade() {
        log("Starting");
        Observable<String> obs = Observable.create( subscriber -> {
            log("Subscribed");
            subscriber.onNext("A");
            subscriber.onNext("B");
            subscriber.onNext("C");
            subscriber.onNext("D");
            subscriber.onComplete();
        });
        log("Create");

        obs
                .subscribeOn(schedulerA)
                .flatMap(record -> store(record).subscribeOn(schedulerB))
                .observeOn(schedulerC)
                .subscribe(
                        x -> log("Got: " + x),
                        Throwable::printStackTrace,
                        () -> log("Completed"));

        log("Exiting");
    }

    static Observable<UUID> store(String s) {
        return Observable.create(subscriber -> {
            log("Storing " + s);
            subscriber.onNext(UUID.randomUUID());
            subscriber.onComplete();
        });
    }


    /*
    맞춤형 스케쥴러를 공급하지 않으면 delay()아래의 모든 연산자는 computation() Scheduler를 사용한다.
    본질적으로 잘못된 것은 없지만 Subscriber가 I/O에서 블록되면 전역적으로 공유되는 computation() 스케쥴러에서 Worker를 소비하게 되므로 전체 시스템에 영향을 미칠 수도 있다.

    맞춤형 스케쥴러를 지원하는 중요 연산자로는 interval(), range(), timer(), repeat(), skip(), take(), timeout() 등이 있다.
    이런 연산자에 스케쥴러를 제공하지 않으면 computation() 스케쥴러를 사용하는데 대부분의 경우에는 안전한 기본 값이다.

    subscribeOn()과 observerOn()의 차이에 대한 이해는 높은 부하가 걸린 상태에서도 기대하는 대로 정확히 작업이 수행되어야 할 때 특히 중요하다.
    모든 장기 실행 작업이 비동기 방식이고 스레드가 거의 없으므로 스케줄러가 필요하다. 그렇지만 언제나 블로킹 코드를 필요로 하는 API나 의존성이 존재
    */
    static void delaySchedulerTest() {
        Observable
                .just('A', 'B')
                .delay(1, TimeUnit.SECONDS, schedulerA)
                .subscribe(x -> log(x.toString()));

        /*
        output: .delay(1, TimeUnit.SECONDS)
            RxComputationThreadPool-1 | A
            RxComputationThreadPool-1 | B

        output: .delay(1, TimeUnit.SECONDS)
            Sched-A-0 | A
            Sched-A-0 | B
         */
    }
}
