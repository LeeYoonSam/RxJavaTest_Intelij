import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.TestScheduler;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsCollectionContaining.hasItems;

/**
 * http://www.baeldung.com/rxjava-testing
 */
public class TestingRxJavaSubscribe {

    /*
    정수 목록에 요소를 추가하면 관찰 가능 객체와 관찰자가 동일한 스레드에서 작업해야하므로 비동기로 작업 할 수 없습니다.
    따라서 RxJava의 가장 큰 장점 중 하나 인 개별 스레드에서 이벤트를 처리하지 못하게됩니다.
     */
    @Test
    public void TestRxJavaCannotWorkAsynchronously() {
        List<String> letters = Arrays.asList("A", "B", "C", "D", "E");
        List<String> results = new ArrayList<>();

        Observable<String> observable = Observable
                .fromIterable(letters)
                .zipWith(Observable.range(1, Integer.MAX_VALUE),
                        (string, index) -> index + "-" + string);

        observable.subscribe(results::add);
        System.out.println(results);

        Assert.assertThat(results, notNullValue());
        Assert.assertThat(results, hasSize(5));
        Assert.assertThat(results, hasItems("1-A", "2-B", "3-C", "4-D", "5-E"));
    }
    /*
    우리는 결과 목록에 요소를 추가하여 관찰자의 결과를 집계합니다.
    관찰자와 관찰 가능한 객체가 같은 스레드에서 작동하므로 우리의 주장이 제대로 차단되고 subscribe () 메소드가 끝날 때까지 기다린다.
     */





    /*
    RxJava에는 비동기 이벤트 처리와 함께 작동하는 테스트를 작성할 수있는 TestObserver 클래스가 있습니다. 이는 관찰 대상을 구독하는 일반 관찰자입니다.
    테스트에서는 TestObserver 상태를 검사하고 해당 상태에 대한 어설 션을 수행 할 수 있습니다.
     */
    @Test
    public void TestRxJavaAsynchronously() {

        List<String> letters = Arrays.asList("A", "B", "C", "D", "E");
        TestObserver<String> subscriber = new TestObserver<>();

        Observable<String> observable = Observable
                .fromIterable(letters)
                .zipWith(
                        Observable.range(1, Integer.MAX_VALUE),
                        ((string, index) -> index + "-" + string));

        observable.subscribe(subscriber);

        // 스트림이 완료 되었는지 체크
        subscriber.assertComplete();

        // 스트림을 받는 도중에 에러가 없었는지 체크
        subscriber.assertNoErrors();

        // 받은 스트림의 갯수 체크
        subscriber.assertValueCount(5);

        // 해당 아이템이 있는지 체크
        assertThat(
                subscriber.values(),
                hasItems("1-A", "2-B", "3-C", "4-D", "5-E"));
    }

    @Test
    public void TestRxJavaExpectedException(){
        List<String> letters = Arrays.asList("A", "B", "C", "D", "E");
        TestObserver<String> subscriber = new TestObserver<>();

        Observable<String> observable = Observable
                .fromIterable(letters)
                .zipWith(Observable.range(1, Integer.MAX_VALUE), ((string, index) -> index + "-" + string))
                .concatWith(Observable.error(new RuntimeException("error in Observable")));

        observable.subscribe(subscriber);

        subscriber.assertError(RuntimeException.class);
        subscriber.assertNotComplete();
    }

    /*
    Observable이 초당 하나의 이벤트를 발생시키고 TestObserver로 그 동작을 테스트하기를 원한다고 가정 해 봅시다.
    Observable.interval () 메서드를 사용하여 시간 기반 Observable을 정의하고 TimeUnit을 인수로 전달할 수 있습니다.
     */
    @Test
    public void TestRxJavaTimeBasedObservable() {
        List<String> letters = Arrays.asList("A", "B", "C", "D", "E");
        TestScheduler scheduler = new TestScheduler();
        TestObserver<String> subscriber = new TestObserver<>();
        Observable<Long> tick = Observable.interval(1, TimeUnit.SECONDS, scheduler);

        Observable<String> observable = Observable.fromIterable(letters)
                .zipWith(tick, (string, index) -> index + "-" + string);

        observable.subscribeOn(scheduler)
                .subscribe(subscriber);

        subscriber.assertNoValues();
        subscriber.assertNotComplete();

        // 1초 지났을때 테스트
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);

        subscriber.assertNoErrors();
        subscriber.assertValueCount(1);
        subscriber.assertValues("0-A");


        // 6초 지났을때 테스트
        scheduler.advanceTimeTo(6, TimeUnit.SECONDS);

        subscriber.assertComplete();
        subscriber.assertNoErrors();
        subscriber.assertValueCount(5);
        assertThat(subscriber.values(), hasItems("0-A", "1-B", "2-C", "3-D", "4-E"));
    }

}
