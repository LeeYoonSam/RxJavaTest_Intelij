package BasicTest;

import com.sun.tools.javac.util.Pair;
import io.reactivex.Observable;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import static io.reactivex.Observable.just;

/**
 * 스트림을 결합하는 방법:
 * concat()
 * 정적메서드 concat() 이나 인스턴스 메서드인 concatWith()로 Observable 두 개를 잇는다.
 * 첫 번째 Observable이 끝나면 concat()은 두 번째 Observable을 구독
 * 첫 번째 Observable이 끝나야만 두 번째 Observable을 구독할 수 있다.
 *
 *
 * merge()
 *
 * switchOnNext()
 *
 */
public class ConcatMergeSwitchExample {
    static class Data { }

    public static void main(String[] args) {

    }

    /*
    매우 긴 스트림에서 처음 몇 개와 마지막 몇 개의 항목만 받고자 할때
    두번 구독
     */
    static void concatTest() {
        Observable<Data> veryLong = Observable.create(e -> e.onNext(new Data()));

        final Observable<Data> ends = Observable.concat(
                veryLong.take(5),
                veryLong.takeLast(5)
        );

    }

    /*
    첫 번째 스트림이 아무것도 방출하지 않을 때를 대비한 구현
     */
    static void concatException() {
//        Observable<Car> fromCache = loadFromCache();
//        Observable<Car> fromDb = loadFromDb();
//
//        Observable<Car> found = Observable
//                .concat(fromCache, fromDb)
//                .first();
    }

    //
    static void speakTest() {

        Observable<String> alice = speak("To be, or not to be: that is the question", 110);
        Observable<String> bob = speak("Though this be madness, yet there is method in't", 90);
        Observable<String> jane = speak("There are more things in Heaven and Earth, Horatio, than are dreamt of in your philosophy", 100);

        // 모두가 동시에 이야기를 시작한다.

        // merge 사용시 혼란스럽다. 각자 하는 이야기가 서로 뒤섞인다. 문장 구분 불가능.
        // 모든 스트림을 조급히 구독하기 때문에 모든 스트림의 모든 이벤틀르 한번에 전달.
//       Observable.merge(
//                alice,
//                bob,
//                jane
//        );

        // merge()를 concat() 연산자로 바꾸면 상황은 매우 달라진다.
        // 먼저 받은 스트림을 다 받은 후 다음 구독 시작하기 때문에..
        Observable.concat(
                alice,
                bob,
                jane
        );


        /*
        0~4초 사이에서 임의의 지연 시간 이후 말하기 시작
        switchOnNext는 스트림을 구독하다가 다른 인용문이 들어오면 이전 스트림은 버리고 새로 구독한 이벤트만 방출
        Observable이 서로 중첩되지 않고 다음 Observable이 나타나기 전에 완료된다면 switchOnNext()는 이론상 모든 이벤트를 방출할 수 있다.

         */
        Random rnd = new Random();
        Observable<Observable<String>> quotes = just(
                alice.map(w -> "Alice: " + w),
                bob.map(w -> "Bob: " + w),
                jane.map(w -> "Jane: " + w))
                .flatMap(innerObs -> just(innerObs)
                .delay(rnd.nextInt(5), TimeUnit.SECONDS));

        Observable
                .switchOnNext(quotes)
                .subscribe(System.out::println);


    }

    /*
    한 무리의 사람들이 마이크를 하나씩 들고 있다고 가정
    마이크를 Observable<String> 으로 모델링, 개별 이벤트는 단어 하나를 나타낸다.
    이벤트는 말하는 순간 시간에 따라 발생
     */
    static Observable<String> speak(String quote, long millisPerChar) {
        // String으로 임의의 문장을 받아단어로 쪼갠 뒤 정규 표현식으로 구두점 제거
        String[] tokens = quote.replaceAll("[:,]", "").split(" ");

        Observable<String> words = Observable.fromArray(tokens);

//        // 이전 단어의 길이에 따라 단어가 지연되어 나타나기를 원한다. 주어진 단어 길이만큼 정직하게 지연하는 구현으로 수정
//        words.flatMap(word -> Observable
//                .just(word)
//                .delay(word.length() * millisPerChar, TimeUnit.MILLISECONDS));


        /*
        개별 단어를 말할 때 시간이 얼마나 걸릴 지 계산 할 수 있는데, 단순하게 단어 길이에 millisPerChar를 곱한다.
        각각의 단어가 계산된 지연 시간에 맞춰 결과 스트림으로 나탈 것이다.
         */
        Observable<Long> absoluteDelay = words
                .map(String::length)
                .map(len -> len * millisPerChar)
                // scan을 사용해서 누적되는 절대 지연 값 구하기
                .scan((total, current) -> total + current);


        /*
         단어 목록과 절대 시간 지연 목록을 확보 했으니, 이들 두 개의 스트림을 zip() 으로 묶을 수 있다.
         두 스트림이 정확히 같은 크기이며 완벽히 서로 짝이 맞음을 알고 있기에 zip() 연산자는 매우 타당한 선택이다.
         첫 번째 단어는 지연시키고 싶지 않기 때문에 첫 번째 단어 길이로 두 번째 단어이ㅡ 지연에 영향을 주고 싶고, 첫 번째와 두 번째 단어 길이의 합으로세 번째 단어의 지연 시간에 영향을 주고싶다.
         이러한 변경은 0값을 덧붙인 absoluteDelay를 사용하면 쉽게 구현 가능

         Pair - import org.apache.commons.lang3.tuple.Pair;
         getLeft(), getRight()로 변경해야함
          */
        return words
                .zipWith(absoluteDelay.startWith(0L), Pair::of)
                .flatMap(pair -> just(pair.fst)
                        .delay(pair.snd, TimeUnit.MILLISECONDS));
    }

}
