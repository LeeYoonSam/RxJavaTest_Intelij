package BasicTest;

import io.reactivex.Observable;
import io.reactivex.Single;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * 고수준 연산자
 * collect(), reduce(), scan(), distint(), groupBy()
 *
 * 어떤 연산자는 순열을 훑으면서 값을 집계하거나 평균을 구하는 등, 보다 더 고수준 변화를 지원
 * 몇몇 연산자는 심지어 상태를 지니고 있기 때문에 순열을 처리하는 동안 상태를 관리
 * 이는 distint가 작동하는 방식으로서, 이미 처리한 값을 캐시하고 버린다.
 */
public class ScanCollectReduceExample {
    public static void main(String[] args) {
        scanTest();

        collectTest();
    }

    static void scanTest() {
        Observable<Long> progress = Observable.just(10L, 14L, 12L, 13L, 14L, 16L);
        // Observable<Long> totalProgress = [10, 24, 36, 49, 63, 79]

        /*
        scan() - 데이터를 누적하면서 총량을 쌓기

        scan()은 인자 두 개를 받는데, 누산기라고도 부르는 마지막으로 생성된 값과 업스트림 Observable의 현재 값
        소스(업스트림) Observable을 밀고 나가서 항목을 모아 놓는다.
         */
        System.out.println("TotalProgress");
        Observable<Long> totalProgress = progress.scan((total, chunk) -> total + chunk);
        totalProgress.subscribe(System.out::println);

        System.out.println("Fatorials");
        /*
         초기값이 단순히 첫 번째 항목이 아니라면 중복 정의된 scan()을 사용해 초기값을 제공할 수 있다.
         업스트림 Observable은 2부터 시작하는데 다운스트림은 1부터 시작하는 모습

         BigInteger.ONE로 다운스트림의 초기 인자값을 1로 설정
          */
        Observable<BigInteger> factorials = Observable
                .range(2, 100)
                .scan(BigInteger.ONE, (big, cur) ->
                        big.multiply(BigInteger.valueOf(cur))
                );
        factorials.subscribe(System.out::println);
    }

    /*
    reduce()
    중간 과정에 관심이 없고 단지 마지막 결과만 원할 때
    scan()을 사용하지만 최종 결과값 1개만 보낸다
    중간 진행 과정이 아닌 총 바이트 수만 원한다고 하거나,
    ArrayList같은 어떤 가변 자료 구조의 개별 요소 총합을 구할때 reduce를 사용
    도중에 어떤 이벤트도 방출하지 않는다.
     */
    static void reduceTest() {
        Observable<CashTransfer> transfers = Observable.just(new CashTransfer(BigDecimal.valueOf(20)), new CashTransfer(BigDecimal.valueOf(50)), new CashTransfer(BigDecimal.valueOf(100)));

//        Observable<BigDecimal> total1 = transfers
//                .reduce(BigDecimal.ZERO,
//                        (totalSofar, transfer) ->
//                                totalSofar.add(transfer.getAmount()));
//
//        Observable<BigDecimal> total2 = transfers
//                .map(CashTransfer::getAmount)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    static class CashTransfer {

        private BigDecimal value;
        public CashTransfer(BigDecimal value) {
            this.value = value;
        }

        public BigDecimal getAmount() {
            return value;
        }
    }

    /*
    reduce()와 collect() 두 가지 모두 논블로킹이다.
    방출된 모든 숫자를 포함하는 결과는 모든 업스트림 신호과 완료되어야 한다.

    collect는 Single만 사용하는걸로 바꼇나보다..
      */
    static void collectTest() {
        // ArrayList 리턴하기 - Observable로 하니 List::add에 에러가 났는데 Single로 변경하니 작동함.
        Single<List<Integer>> allReduce = Observable
                .range(10, 20)
                .reduce(new ArrayList<>(), (list, item) -> {
                    list.add(item);
                    return list;
                });

        allReduce.subscribe(System.out::println);


        // ArrayList 리턴하기 - Observable로 하니 List::add에 에러가 났는데 Single로 변경하니 작동함.
        Single<ArrayList<Integer>> all = Observable
                .range(10, 20)
                .collect(ArrayList::new, List::add);

        all.subscribe(System.out::println);


        // StringBuilder로 문자열 만들어서 String 리턴
        Single<String> str = Observable
                .range(1, 10)
                .collect(StringBuilder::new,
                        (sb, x) -> sb.append(x).append(", "))
                .map(StringBuilder::toString);

        str.subscribe(System.out::println);
    }
}
