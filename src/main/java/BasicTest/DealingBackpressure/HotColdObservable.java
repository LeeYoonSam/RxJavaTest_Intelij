package BasicTest.DealingBackpressure;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * 일단 Observable 객체를 확보했다면, 해당 스트림이 뜨거운지 차가운지를 이해 해야한다.
 *
 * Cold Observable
 * 전적으로 느긋하여 실제로 누군가 관심을 기울이지 않으면 절대 이벤트 방출을 시작하지 않는다.
 * 관찰자가 없으면 Observable은 단순히 정적 자료 구조일 뿐이다.
 * 캐시 처리되지 않기 때문에 모든 구독자는 각자 별도로 스트림의 복사본을 받는다.
 * 일반적으로 Observable.create()를 사용해서 만든다.
 * 어느정도 Subscriber에 의존한다.
 * ** 데이터베이스 질의 / 네트워크 연결에 사용
 *
 * Hot Observable
 * 획득한 순간 Subscriber 여부와 관계 없이 즉시 이벤트를 방출
 * 구독자가 없어도 이벤트를 다운스트림으로 밀어내기 때문에 이벤트 유실가능성 있음
 * Subscriber의 존재 여부가 영향을 미치지 않으며 서로 완전히 분리되어 있고 독립적이다.
 * 해당값이 언제 발생했는지가 아주 중요하다
 * ** 이벤트 소스를 전혀 통제할 수 없는 경우 발생 - 마우스 움직임, 키보드 입력 등
 *
 */
public class HotColdObservable {
    public static void main(String[] args) {

//        createColdObservables();
//        createHotObservables();

//        useBuffer();
//        useBatchingItems();
        useKippingElements();

        try {
            Thread.sleep(1000000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    // let’s create a simple consumer function that will be used as a consumer of elements from Observables that we will define later:
    public static void compute(Integer v) {
        try {
            System.out.println("compute integer v: " + v);
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*
    2.1. Cold Observables

    Cold Observable은 특정 항목의 시퀀스를 내보내지만 Observer가 편리한 것으로 판단 할 때 관찰자가 원하는 모든 속도로 시퀀스의 무결성을 파괴하지 않고이 시퀀스를 내보낼 수 있습니다.
    Cold Observable은 게으른 방법으로 항목을 제공합니다.
    Observer는 해당 항목을 처리 할 준비가되었을 때만 요소를 가져오고 항목은 당연히 요청되기 때문에 Observable에서 버퍼링 할 필요가 없습니다.
    예를 들어 1 백만에서 1 백만까지의 정적 범위의 요소를 기반으로 Observable을 만드는 경우 Observable은 해당 항목이 얼마나 빈번하게 표시되는지에 관계없이 같은 순서로 항목을 방출합니다.


    Cold Observable 은 pull fashion(요청할때 가져옴) 방식으로 작동하기 때문에 어떠한 형태의 backpressure가 필요하지 않습니다.
    Cold Observable에서 방출되는 항목의 예에는 데이터베이스 쿼리, 파일 검색 또는 웹 요청의 결과가 포함될 수 있습니다.
     */
    public static void createColdObservables() {
        Observable.range(1, 1_000_000)
                .observeOn(Schedulers.computation())
                .subscribe(HotColdObservable::compute);
    }


    /*
    2.2. Hot Observables

    Hot Observable 은 항목 생성을 시작하고 생성 될 때 즉시 방출합니다.
    Cold Observables의 처리 모델과는 반대입니다. Hot Observer는 자체 속도로 항목을 방출하며 관찰자가 따라 잡아야합니다.
    Observer가 Observable에서 생성 한만큼 빨리 Observer가 항목을 소비 할 수없는 경우 버퍼를 처리하거나 다른 방법으로 처리해야합니다.
    그러면 메모리가 가득 차서 OutOfMemoryException이 발생합니다.
    Hot Observable의 예를 들어 보겠습니다.
    이 항목은 해당 항목을 처리하는 최종 소비자에게 1 백만 개의 항목을 생성합니다. Observer의 compute () 메소드가 모든 항목을 처리하는 데 시간이 걸리면 Observable이 메모리로 항목을 채우기 시작하여 프로그램이 실패하게됩니다.
     */
    public static void createHotObservables() {
        PublishSubject<Integer> source = PublishSubject.create();

        source.observeOn(Schedulers.computation())
                .subscribe(HotColdObservable::compute, Throwable::printStackTrace);

        IntStream.range(1, 1_000_000).forEach(source::onNext);
    }
    /*
    Observable을 과도하게 처리하는 방법을 정의하지 않았으므로이 프로그램을 실행하면 MissingBackpressureException이 발생합니다.
    Hot Observable 항목의 예에는 마우스 및 키보드 이벤트, 시스템 이벤트 또는 주가가 포함될 수 있습니다.
     */




    /*
    3. Buffering Overproducing Observable

    Observable을 과도하게 처리하는 첫 번째 방법은 Observer가 처리 할 수없는 요소에 대해 일종의 버퍼를 정의하는 것입니다.
    buffer () 메서드를 호출하여이 작업을 수행 할 수 있습니다.
     */
    public static void useBuffer() {
        PublishSubject<Integer> source = PublishSubject.create();

//        source.buffer(1024)
//                .observeOn(Schedulers.computation())
//                .subscribe(HotColdObservable::compute, Throwable::printStackTrace);
    }
    /*
    크기가 1024 인 버퍼를 정의하면 관찰자는 과도한 소스를 따라 잡을 수 있습니다. 버퍼는 아직 처리되지 않은 항목을 저장합니다.
    우리는 생성된 값을 위한 충분한 공간을 확보하기 위해 버퍼 크기를 늘릴 수 있습니다.
    그러나 소스가 예측 된 버퍼 크기를 과다하게 생성하는 경우에도 오버플로가 여전히 발생할 수 있으므로 일반적으로 임시로 사용.
     */





    /*
    4. Batching Emitted Items

    N 요소의 창에서 항목을 과도하게 배치 할 수 있습니다.
    Observable이 Observer가 처리 할 수있는 것보다 더 빨리 요소를 생성 할 때 생성 된 요소를 그룹화하고 하나씩 요소 대신 하나의 요소 집합을 처리 할 수있는 Observer에 배치를 보내면 이를 완화 할 수 있습니다.
     */
    public static void useBatchingItems() {
        PublishSubject<Integer> source = PublishSubject.create();

//        source.window(500)
//                .observeOn(Schedulers.computation())
//                .subscribe(HotColdObservable::compute, Throwable::printStackTrace);
    }
    /*
    window() 메소드에 변수를 500으로 를 사용하면 Observable이 요소를 500 크기의 배치로 그룹화합니다.
    이 기술을 사용하면 Observer가 처리 요소를 하나씩 처리하는 것과 비교하여 처리 할 수있을 때 Observable을 과도하게 생성하는 문제를 줄일 수 있습니다.
     */




    /*
    5. Skipping Elements

    Observable에 의해 생성 된 값 중 일부를 무시해도 문제가 해결되지 않으면 특정 시간 및 제한된 작업자 내에서 샘플링을 사용할 수 있습니다.
    메소드 sample () 및 throttleFirst ()는 duration을 매개 변수로 사용합니다.

    sample () 메서드는 주기적으로 요소 시퀀스를 조사하고 매개 변수로 지정된 기간 내에 생성 된 마지막 항목을 내 보냅니다
    throttleFirst () 메서드는 매개 변수로 지정된 지속 시간 이후에 생성 된 첫 번째 항목을 내 보냅니다
    지속 시간은 생성 된 요소의 시퀀스에서 하나의 특정 요소를 선택하는 시간입니다. 요소를 건너 뜀으로써 배압을 처리하기위한 전략을 지정할 수 있습니다.
     */
    public static void useKippingElements() {
        PublishSubject<Integer> source = PublishSubject.create();

        source.sample(100, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.computation())
                .subscribe(HotColdObservable::compute, Throwable::printStackTrace);

        IntStream.range(1, 1_000_000).forEach(source::onNext);
    }
    /*
    우리는 요소를 건너 뛰는 전략이 sample () 메소드가 될 것이라고 지정했습니다. 100 밀리 초 기간의 샘플을 원합니다. 그 요소는 Observer에게 방출 될 것입니다.
    그러나 이러한 연산자는 다운 스트림 옵저버 (Observer)가 가치 수신율을 낮추기 때문에 MissingBackpressureException으로 이어질 수 있습니다.
     */


}
