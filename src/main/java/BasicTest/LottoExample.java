package BasicTest;

import io.reactivex.Observable;
import io.reactivex.Single;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LottoExample {
    public static void main(String[] args) {
        getLottoNumber();
    }

    static void getLottoNumber() {

        // 1~45까지 번호를 발생시키는 난수 스트림
        Observable<Integer> lottoNumber = Observable.create(subscriber -> {
            Random random = new Random();

            while (!subscriber.isDisposed()) {
                subscriber.onNext(random.nextInt(45) + 1);
            }
        });

        // 난수에서 중복되지 않는 6개의 난수를 뽑아서 문자열로 변환
        Single<String> line = lottoNumber.distinct().take(6).collect(StringBuilder::new, (sb, x) -> sb.append(x).append(" ")).map(StringBuilder::toString);

        // 한줄에 6개짜리 난수번호 5줄 생성(5천원치라고 가정)
        Observable<Integer> lottoLine = Observable.range(1, 5);

        lottoLine.flatMap(x -> line.toObservable())
                .collect(ArrayList::new, List::add)
                .subscribe(System.out::println);
    }
}
