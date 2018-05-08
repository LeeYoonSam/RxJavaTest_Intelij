package BasicTest.Migration;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

import java.util.concurrent.TimeUnit;

public class NoneBlockingExample {
    static class Flight{}
    static class Passenger{}
    static class Ticket{}
    static class SmtpResponse{}

    static Flight lookupFlight(String flightNo) { return new Flight(); }
    static Passenger findPassenger(long id) { return new Passenger(); }
    static Ticket bookTicket(Flight flight, Passenger passenger) { return new Ticket(); }
    static SmtpResponse sendEmail(Ticket ticket) { return new SmtpResponse(); }

    public static void main(String[] args) {
        /*
         전형적인 블로킹 코드
         4단계를 거치며, 처음 두 단계는 서로 관련이 없다.
         세 번째 단계인 bookTicket()에서 lookupFlight()와 findPassenger()의 결과가 필요할 뿐이다. 명백하게 동시성의 장점을 취할 기회가 존재한다.
         다루기 힘든 쓰레드풀이나 Future, 콜백 등을 필요로 하기 때문
          */
//        Flight flight = lookupFlight("LOT 783");
//        Passenger passenger = findPassenger(42);
//        Ticket ticket = bookTicket(flight, passenger);
//        sendEmail(ticket);


        /*
        전통적인 블로킹 방식 프로그램과 Observable로 만든 것 모두 정확히 같은 방식으로 동작
        기본적으로는 느긋하지만 작동 순서는 근본적으로 같다.
        우선 Observable<Flight>, Observable<Passenger>를 만들었는데 아무일도 하지 않는다. 누군가 Flight에 요청하지 않으면 이 Observable은 그저 느긋한 플레이스홀더 일 뿐이다.(차가운 Observable)

        여러 Observabledㅡㄹ 동시에 구독하는 자연스러운 방법은 zip이나 zipWith이다
        zip은 두개의 독립적인 스트림 데이터를 짝맞춰 묶을 때 훨씬 더 자주 사용
        ob1.zip(ob2).subscribe(...)는 결국 ob1과 ob2이 모두 완료 됐을때 이벤트 하나를 받는다는 뜻
        두 개 이상의 비동기 값을 기다리되 순서는 상관 없을때 선택 가능한 방법
         */
        Observable<Flight> flight = rxLookupFlight("LOT 783");
        Observable<Passenger> passenger  = rxFindPassenger(42);
        Observable<Ticket> ticket =
                flight.zipWith(passenger, (f, p) -> bookTicket(f, p));
        // 지금까지 어떠한 동작도 진행되지 않았다, subscribe()로 구독하면 명시적 Ticket 요청이 일어난다.
        ticket.subscribe(makeTicket -> sendEmail(makeTicket));

        /*
         동시성을 더해야 한다면, 스레드방식을 선택 할 수 있다.
         구독이 시작되면 클라이언트 스레드가 아니라 제공된 Scheduler에서 실행
          */
        Observable<Flight> flightThread = rxLookupFlight("LOT 783").subscribeOn(Schedulers.io());
        Observable<Passenger> passengerThread  = rxFindPassenger(42).subscribeOn(Schedulers.io());

        /*
         지체되는 경우 타임아웃 적용 가능
         lookupFlight() 메서드가 100ms 이상 지체된다면 다운스트림의 모든 구독자에게 방출값 대신 TimeoutException을 보낸다.
          */
        rxLookupFlight("LOT 783")
                .subscribeOn(Schedulers.io())
                .timeout(100, TimeUnit.MILLISECONDS);
    }

    /*
    블로킹 코드를 Observable로 간단하게 포장할 수 있음을 기억
     */
    static Observable<Flight> rxLookupFlight(String flightNo) {
        return Observable.defer( () ->
                Observable.just(lookupFlight(flightNo))
        );
    }

    static Observable<Passenger> rxFindPassenger(long id) {
        return Observable.defer( () ->
                Observable.just(findPassenger(id))
        );
    }


}
