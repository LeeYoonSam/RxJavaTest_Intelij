import io.reactivex.Observable;
import io.reactivex.exceptions.CompositeException;
import io.reactivex.observers.TestObserver;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertTrue;

/**
 * http://www.baeldung.com/rxjava-error-handling
 *
 * 오류가 발생하면 일반적으로 어떤 방식으로든 처리해야합니다.
 * 예를 들어, 관련 외부 상태를 변경하고, 기본 결과로 시퀀스를 다시 시작하거나, 오류가 전파 될 수 있도록 그대로 두십시오.
 */
public class TestingRxJavaErrorHandling {
    private UnknownError UNKNOWN_ERROR = new UnknownError("unknown error");
    /*
    3.1. Action on Error
    doOnError를 사용하면 오류가있을 때 필요한 조치를 호출 할 수 있습니다.
     */
    @Test
    public void whenChangeStateOnError_thenErrorThrown() {
        TestObserver testObserver = new TestObserver();
        AtomicBoolean state = new AtomicBoolean(false);

        Observable
                .error(UNKNOWN_ERROR)
                .doOnError(throwable -> state.set(true))
                .subscribe(testObserver);

        testObserver.assertError(UNKNOWN_ERROR);
        testObserver.assertNotComplete();
        testObserver.assertNoValues();

        assertTrue("state should be changed", state.get());
    }

    /*
    액션의 실행 중에 예외가 throw되었을 경우, RxJava는 CompositeException에 예외를 랩합니다.
     */
    @Test
    public void whenExceptionOccurOnError_thenCompositeExceptionThrown() {
        TestObserver testObserver = new TestObserver();

        Observable
                .error(UNKNOWN_ERROR)
                .doOnError(throwable ->  {
                    throw new RuntimeException("unexcepted");
                })
                .subscribe(testObserver);

        testObserver.assertError(CompositeException.class);
        testObserver.assertNotComplete();
        testObserver.assertNoValues();
    }


    /*
    3.2. Resume with Default Items - 에러로 처리하지 않을때
    doOnError를 사용하여 작업을 호출 할 수 있지만 오류로 인해 표준 시퀀스 플로우가 여전히 손상됩니다.
    때로는 기본 옵션을 사용하여 시퀀스를 다시 시작하려고합니다.
     */
    @Test
    public void whenHandleOnErrorResumeItem_thenResumed() {
        TestObserver testObserver = new TestObserver();

        Observable
                .error(UNKNOWN_ERROR)
                .onErrorReturnItem("singleValue")
                .subscribe(testObserver);

        // 에러로 인식하지 않음
        testObserver.assertNoErrors();
        testObserver.assertComplete();
        testObserver.assertValueCount(1);
        testObserver.assertValue("singleValue");
    }

    /*
    동적 기본 항목 공급자가 선호되는 경우 onErrorReturn 사용
     */
    @Test
    public void whenHandleOnErrorReturn_thenResumed() {
        TestObserver testObserver = new TestObserver();

        Observable
                .error(UNKNOWN_ERROR)
                .onErrorReturn(Throwable::getMessage)
                .subscribe(testObserver);

        testObserver.assertNoErrors();
        testObserver.assertComplete();
        testObserver.assertValueCount(1);
        testObserver.assertValue("unknown error");
    }



    /*
    3.3. Resume with Another Sequence

    단일 항목으로 폴백하는 대신 오류가 발생할 경우 onErrorResumeNext를 사용하여 폴백 데이터 시퀀스를 제공 할 수 있습니다. 이렇게하면 오류 전파를 방지하는 데 도움이됩니다.
     */
    @Test
    public void whenHandleOnErrorResume_thenResumed() {
        TestObserver testObserver = new TestObserver();

        Observable
                .error(UNKNOWN_ERROR)
                .onErrorResumeNext(Observable.just("one", "two"))
                .subscribe(testObserver);

        testObserver.assertNoErrors();
        testObserver.assertComplete();
        testObserver.assertValueCount(2);
        testObserver.assertValues("one", "two");
    }

    /*
    대체 시퀀스가 특정 예외 유형에 따라 다르거 나 함수에서 시퀀스를 생성해야하는 경우이 함수를 onErrorResumeNext에 전달할 수 있습니다
     */
    @Test
    public void whenHandleOnErrorResumeFunc_thenResumed() {
        TestObserver testObserver = new TestObserver();

        Observable
                .error(UNKNOWN_ERROR)
                .onErrorResumeNext(throwable -> {
                    Observable
                            .just(throwable, "nextValue");
                })
                .subscribe(testObserver);

        testObserver.assertNoErrors();
        testObserver.assertComplete();
        testObserver.assertValueCount(2);
        testObserver.assertValues("unknown error", "nextValue");
    }


    /*
     3.4. Handle Exception Only

     또한 RxJava는 오류 (예외는 발생하지 않음)가 발생할 때 제공된 Observable을 사용하여 시퀀스를 계속할 수있는 대체 방법을 제공합니다.
     */
    @Test
    public void whenHandleOnException_thenResumed() {
        TestObserver testObserver = new TestObserver();

        Observable
                .error(UNKNOWN_ERROR)
                .onExceptionResumeNext(Observable.just("exceptionResume"))
                .subscribe(testObserver);

        testObserver.assertNoErrors();
        testObserver.assertComplete();
        testObserver.assertValueCount(1);
        testObserver.assertValue("exceptionResume");
    }

    @Test
    public void whenHandleOnException_thenNotResumed() {
        TestObserver testObserver = new TestObserver();

        Observable
                .error(UNKNOWN_ERROR)
                .onExceptionResumeNext(Observable.just("exceptionResumes"))
                .subscribe(testObserver);

        testObserver.assertError(UNKNOWN_ERROR);
        testObserver.assertNotComplete();
    }
    /*
    위의 코드에서 볼 수 있듯이 오류가 발생하면 onExceptionResumeNext는 시퀀스를 다시 시작하지 않습니다.
     */






    /*
    4.1. Retry

    Retry 를 사용하면 Observable은 오류가 없을 때까지 무한 시간에 다시 등록됩니다.
    그러나 대부분의 경우 재시도 횟수를 제한하는것을 선호합니다.
     */
    @Test
    public void whenRetryOnError_thenRetryConfirmed() {
        TestObserver testObserver = new TestObserver();
        AtomicInteger atomicCounter = new AtomicInteger(0);

        Observable
                .error(() -> {
                    atomicCounter.incrementAndGet();
                    return new UnknownError();
                })
                .retry(1)
                .subscribe(testObserver);

        testObserver.assertError(UnknownError.class);
        testObserver.assertNotComplete();
        testObserver.assertNoValues();
        assertTrue("should try twice", atomicCounter.get() == 2);

    }



    /*
    4.2. Retry on Condition

    조건부 재시도 RxJava에서 가능하며, predicates와 함께 retry를 사용하거나 retryUntil을 사용
     */
    @Test
    public void whenRetryConditionallyOnError_thenRetryConfirmed() {
        TestObserver testObserver = new TestObserver();
        AtomicInteger atomicCounter = new AtomicInteger(0);
        Observable
                .error(() -> {
                    atomicCounter.incrementAndGet();
                    return UNKNOWN_ERROR;
                })
                .retry((integer, throwable) -> integer < 4)
                .subscribe(testObserver);

        testObserver.assertError(UNKNOWN_ERROR);
        testObserver.assertNotComplete();
        testObserver.assertNoValues();
        assertTrue("should call 4 times", atomicCounter.get() == 4);
    }

    @Test
    public void whenRetryUntilOnError_thenRetryConfirmed() {
        TestObserver testObserver = new TestObserver();
        AtomicInteger atomicCounter = new AtomicInteger(0);
        Observable
                .error(UNKNOWN_ERROR)
                .retryUntil(() -> atomicCounter.incrementAndGet() > 3)
                .subscribe(testObserver);

        testObserver.assertError(UNKNOWN_ERROR);
        testObserver.assertNotComplete();
        testObserver.assertNoValues();
        assertTrue("should call 4 times", atomicCounter.get() == 4);
    }




    /*
    4.3. RetryWhen

    이러한 기본 옵션 외에도 재시도 방법인 retryWhen이 있습니다.
    예를 들어 "OldO"와 같은 ObservableSource 소스와 동일한 값을 방출하는 Observable, "NewO"를 반환하지만 반환 된 Observable "NewO"가 onComplete 또는 onError를 호출하면 가입자의 onComplete 또는 onError가 호출됩니다.
    "NewO"가 항목을 방출하면 ObservableSource "OldO"소스에 대한 다시 구독이 트리거됩니다.
     */
    @Test
    public void whenRetryWhenOnError_thenRetryConfirmed() {
        TestObserver testObserver = new TestObserver();
        Exception noretryException = new Exception("don't retry");
        Observable
                .error(UNKNOWN_ERROR)
                .retryWhen(throwableObservable -> Observable.error(noretryException))
                .subscribe(testObserver);

        testObserver.assertError(noretryException);
        testObserver.assertNotComplete();
        testObserver.assertNoValues();
    }

    @Test
    public void whenRetryWhenOnError_thenCompleted() {
        TestObserver testObserver = new TestObserver();
        AtomicInteger atomicCounter = new AtomicInteger(0);

        Observable
                .error(() -> {
                    atomicCounter.incrementAndGet();
                    return UNKNOWN_ERROR;
                })
                .retryWhen(throwableObservable -> Observable.empty())
                .subscribe(testObserver);

        testObserver.assertNoErrors();
        testObserver.assertComplete();
        testObserver.assertNoValues();
        assertTrue("should not retry", atomicCounter.get() == 0);
    }

    @Test
    public void whenRetryWhenOnError_thenResubscribed() {
        TestObserver testObserver = new TestObserver();
        AtomicInteger atomicCounter = new AtomicInteger(0);

        Observable
                .error(() -> {
                    atomicCounter.incrementAndGet();
                    return UNKNOWN_ERROR;
                })
                .retryWhen(throwableObservable -> Observable.just("anything"))
                .subscribe(testObserver);

        testObserver.assertNoErrors();
        testObserver.assertComplete();
        testObserver.assertNoValues();
        assertTrue("should retry once", atomicCounter.get() == 1);
    }

    /*
    retryWhen의 일반적인 사용법은 제한된 재시도와 가변 지연이 있습니다.
     */
    @Test
    public void whenRetryWhenForMultipleTimesOnError_thenResumed() {
        TestObserver testObserver = new TestObserver();
        long before = System.currentTimeMillis();

        Observable
                .error(UNKNOWN_ERROR)
                .retryWhen(throwableObservable -> throwableObservable
                        .zipWith(Observable.range(1, 3), (throwable, integer) -> integer)
                        .flatMap(integer -> Observable.timer(integer, TimeUnit.SECONDS)))
                .blockingSubscribe(testObserver);

        testObserver.assertNoErrors();
        testObserver.assertComplete();
        testObserver.assertNoValues();
        long secondsElapsed = (System.currentTimeMillis() - before) / 1000;
        assertTrue("6 seconds should elapse", secondsElapsed == 6);
    }
    /*
    이 논리가 세 번 재 시도되고 각 재시도가 점진적으로 지연되는 것을주의하십시오.
     */
}
