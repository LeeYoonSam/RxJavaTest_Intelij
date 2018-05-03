package BasicTest.FlatMap;

import io.reactivex.Observable;

import java.util.ArrayList;
import java.util.List;

public class FlatMapConcurrency {

    static class Profile {
        public String name;
        public int age;
        public String profileImg;
    }
    
    static class User {
        static Observable<Profile> loadProfile() {
            // HTTP 요청 수행

            Profile sample = new Profile();
            return (Observable<Profile>) Observable.just(sample)
                    .subscribe(profile -> System.out.println(profile.name));
        }
    }
    public static void main(String[] args) {
        List<User> veryLargeList = new ArrayList<>();

//        Observable<Profile> profiles = Observable
//                .fromArray(veryLargeList)
//                .flatMap(User::loadProfile());
    }
}
