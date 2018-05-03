package BasicTest;

import io.reactivex.Observable;

import java.util.concurrent.TimeUnit;

public class TimerInterval {
    public static void main(String[] args) {
        /*
        timer()
        지정한 시간만큼 지연시킨 후 long형 0값을 방출하고 종료하는 단순한 Observable을 만든다.

        현재 스레드를 블로킹하는 대신 Observable을 만들고 subscribe()로 구독한다.
         */
        Observable.timer(1, TimeUnit.SECONDS)
                .subscribe((Long zero) -> log(zero));


        /*
        interval()
        long 순열을 만드는데, 0부터 시작해서 각각의 숫자 사이에 정해진 시간 지연을 삽입한다.

        애니메이션이나 프로세스를 제어하기 위해 이따금 interval()을 사용
        정기적인 데이터 폴링, 사용자 인터페이스 갱신, 시뮬레이션에서 경과 시간 모델링 등 다양한 시나리오에 interval()을 사용할 수 있다.
         */
        Observable.interval(1_000_000 / 60, TimeUnit.MICROSECONDS)
                .subscribe((Long i) -> log(i));

    }

    public static void log(Long zero) {
        System.out.println(" " + zero);
    }
}
