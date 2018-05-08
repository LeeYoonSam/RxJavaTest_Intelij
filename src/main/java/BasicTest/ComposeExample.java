package BasicTest;


import io.reactivex.Observable;
import org.apache.commons.lang3.tuple.Pair;

import static io.reactivex.Observable.empty;
import static io.reactivex.Observable.just;

public class ComposeExample {
    public static void main(String[] args) {
        Observable<Boolean> trueFalse = just(true, false).doOnNext(x -> System.out.println("trueFalse: " + x)).repeat();
        Observable<Boolean> upstream = just(true, false, false, true, false, false, true, false);
        Observable<Boolean> downstream = upstream
                .zipWith(trueFalse, Pair::of)

                // skip을 사용해서 짝수번째로 바꾸기
//                .zipWith(trueFalse.skip(1), Pair::of)

                .filter(Pair::getRight)
                .map(Pair::getLeft);

        downstream.subscribe(System.out::println);

        System.out.println();

        /*
        withoutThirdparty
        zipWith() 변환은 비었거나 원소 하나짜리 Observable을 반환하는데, 그 결과는 Observable<Observsable<T>> 이다.
        flatMap()을 사용하여 중첩을 풀어낼 수 있다.
        flatMap()안의 람다식은 각각의 입력값에 대해 Observable을 반환한다.

        trueFalse Observable에서 true와 같은 위치쌍(?)에 해당하는 스트림만 upstream에서 가져옴
        --- --- --- --- --- --- ---
        trueFalse: true
        true
        trueFalse: false
        trueFalse: true
        false
        trueFalse: false
        trueFalse: true
        true
        trueFalse: false
        trueFalse: true
        true
        trueFalse: false
        --- --- --- --- --- --- ---
         */
        upstream.zipWith(trueFalse, (
                t, bool) ->
                bool ? just(t) : empty())
                // zipWith() 변환은 비었거나 원소 하나짜리 Observable을 반환하는데, 그 결과는 Observable<Observsable<T>> 이므로 flatMap으로 중첩 풀기
                .flatMap(obs -> obs)
        .subscribe(System.out::println);

        System.out.println();

        // 홀수 항목만 취하도록 재사용 하려면 복사해서 사용하거나 유틸리티 메서드를 만들어야 한다.
        odd(upstream)
                .subscribe(System.out::println);
    }



    // 홀수 항목만 취하도록 재사용 하려면 복사해서 사용하거나 유틸리티 메서드를 만들어야 한다.
    private static <T> Observable odd(Observable<T> upstream) {
        Observable<Boolean> trueFalse = just(true, false).repeat();

        /*
        Pair 쌍에서 left: upstream, right: trueFalse

        getRight(trueFalse Observable)에서 true만 filter해서 같은 위치값 쌍의 getLeft(upstream) 값을 가져옴
         */

        return upstream
                .zipWith(trueFalse, Pair::of)
                .filter(Pair::getRight)
                .map(Pair::getLeft);
    }

//    private <T> Observable.Transform<T, T> odd() {
//        Observable<Boolean> trueFalse = just(true, false).repeat();
//        Observable<Boolean> upstream = just(true, false, false, true, false, false, true, false);
//        Observable<Boolean> downstream = upstream
//                .zipWith(trueFalse, Pair::of)
//                .filter(Pair::getRight)
//                .map(Pair::getLeft);
//
//        /*
//         순차적으로 하나씩 흘러가는 Observable
//         output: [A, B, C, D, E ....]
//          */
//        Observable<Character> alphabet = Observable.range(0, 'Z' - 'A' + 1)
//                .map(c -> (char) ('A' + c));
//
//        /*
//        trueFalse의 홀수번째 upstream 데이터 가져옴
//         output: [A, C, E, G, I ....]
//          */
//        alphabet.compose(odd())
//                .forEach(System.out::println);
//    }
}
