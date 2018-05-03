package BasicTest;

import io.reactivex.Observable;
import twitter4j.*;

/**
 * rx.subjects.Subject
 * Observable을 상속(확장, extends)하면서 동시에 Observer도 상속(구현, implement)했다.
 * 클라이언트 쪽에서는 (업스트림 이벤트를 구독하는) Observable처럼 다루면서 서버 쪽에서는 (필요할때 onNext()를 호출하여 다운스트림으로 이벤트를 방출하는) Observer로 다룰수 있다.
 *
 * PublishSubject는 Subject의 다른 형태 중 하나이다.
 * 업스트림 시스템에서 조급히 이벤트를 받기 시작하여 단순히 모든 Subscriber로 밀어낼 수 있다.
 * Subject가 내부적으로 이벤트를 추적 하므로 더 이상 직접 통제할 필요가 없다.
 * Subject는 내부적으로 Subscriber의 생명 주기를 관리
 *
 */

public class PublishSubject {
    class TwitterSubject {
        private final io.reactivex.subjects.PublishSubject<Status> subject = io.reactivex.subjects.PublishSubject.create();

        public TwitterSubject() {
            TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
            twitterStream.addListener(new StatusListener() {
                @Override
                public void onStatus(twitter4j.Status status) {
                    subject.onNext(status);
                }

                @Override
                public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {

                }

                @Override
                public void onTrackLimitationNotice(int numberOfLimitedStatuses) {

                }

                @Override
                public void onScrubGeo(long userId, long upToStatusId) {

                }

                @Override
                public void onStallWarning(StallWarning warning) {

                }

                @Override
                public void onException(Exception ex) {
                    subject.onError(ex);
                }
            });

            twitterStream.sample();
        }

        public Observable<Status> observe() {
            return subject;
        }
    }
}
