package BasicTest;

import io.reactivex.Observable;

import java.time.LocalDate;

public class ZipExample {
    public static void main(String[] args) {
        squaresCartesianProduct();
    }

    /*
     스트림 두 개로부터 카테시안 곱을 만든다.
     각각 체스판의 행(ranks, 1부터 8)과 열(files, a부터 h)을 가리키며, 체스ㅏㄴ에서 가능한 모든 경우인 64개의 사각형을 찾고자 한다.
      */
    static void squaresCartesianProduct() {
        System.out.println("squaresCartesianProduct");
        Observable<Integer> oneToEight = Observable.range(1, 8);

        // 1~8의 Integer 값을 String으로 변환
        Observable<String> ranks = oneToEight
                .map(Object::toString);

        // ASCII 값을 구함 97부터 1+ - a(97) ~ h(104)
        Observable<String> files = oneToEight
                .doOnNext(System.out::println)
                .map(x -> 'a' + x - 1)
                .doOnNext(System.out::println)
                .map(ascii -> (char)ascii.intValue())
                .doOnNext(System.out::println)
                .map(ch -> Character.toString(ch))
                .doOnNext(System.out::println);

        // ASCII 값에다가 ranks의 숫자를 더해서 문자열 조합 a1~a8, b1~b8, c1~c8 ... h1~h8 - a스트림부터 시작해서 ranks 만큼 돌아감 (8*8 = 64개)
        Observable<String> squares = files
                .flatMap(file -> ranks.map(rank -> file + rank));

        squares.subscribe(System.out::println);
    }

    /*
    zip() - 연산자
    스트림 두 개이상을 합치되 스트림 각각의 서로 대응하는 개별 이벤트끼리 짝을 맞추는 동작
    업스트림에서 각각의 첫 번째 이벤트를 모아 짝을 맺고, 두 번째 이하도 같은 식으로 짝을 맺어 새로운 이벤트를 만들어 다운스트림 이벤트를 형성
    모든 업스트림 소스가 이벤트를 방출해야만 다운스트림 이벤트가 나타난다.
    여러 스트림을 하나로 묶을때 유용
    관련없는 두 스트림의 방출값을 함께 묵을 때 비로소 의미가 생기기도 한다.

    조금 더 현실적인 카테시안 곱 사용하는 예제
    어떤 도시에서 보낼 하루 휴가 계획을 세우는데, 날씨는 화창해야 하고 항공편과 호텔 비용은 저렴했으면 좋겠다.
    이를 찾기 위해 우선 몇 가지 스트림을 결합하여 모든 가능한 결과를 도출
     */
    static void findSuitableDate() {
        // 10일후 까지 스트림에 담음
        Observable<LocalDate> nextTenDays =
                Observable
                        .range(1, 10)
                        .map(i -> LocalDate.now().plusDays(i));

        Observable<Vacation> possibleVacations =
                Observable
                .just(City.Warsaw, City.London, City.Paris)
                .flatMap(city -> nextTenDays.map(date -> new Vacation(city, date))) // 10일치 Vacation 객체를 만듬
                .flatMap(vacation ->
                        // 날씨가 좋고, 뉴욕에서 출발하는 싼 항공편, 싼 호텔을 묶어서 vacation 스트림으로 전달
                    Observable.zip(
                            vacation.weather().filter(Weather::isSunny),
                            vacation.cheapFlightFrom(City.NewYork),
                            vacation.cheapHotel(),
                            (w, f, h) -> vacation
                    )
                );
    }

    static class Vacation {
        private final City where;
        private final LocalDate when;

        Vacation(City where, LocalDate when) {
            this.where = where;
            this.when = when;
        }

        public Observable<Weather> weather() {
            return Observable.just(new Weather());
        }

        public Observable<Flight> cheapFlightFrom(City from) {
            return Observable.just(new Flight());
        }

        public Observable<Hotel> cheapHotel() {
            return Observable.just(new Hotel());
        }
    }

    static class City {
        private String cityName;

        public City(String cityName) {
            this.cityName = cityName;
        }

        public static final City NewYork = new City("NewYork");
        public static final City Warsaw = new City("Warsaw");
        public static final City London = new City("London");
        public static final City Paris = new City("Paris");
    }
    static class Weather {
        public boolean isSunny() {
            return true;
        }
    }
    static class Flight { }
    static class Hotel { }
}
