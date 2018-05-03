package BasicTest;

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

    }
}
