package BasicTest.Migration;

import io.reactivex.Observable;
import io.reactivex.Single;

import java.util.ArrayList;
import java.util.List;

public class fromCallableDeferExample {

    static ArrayList<Person> originList = new ArrayList<>();

    public static void main(String[] args) {
        Observable<Person> peopleStream = listPeople();
        Single<List<Person>> peopleList = peopleStream.toList();

        peopleList.subscribe(
                personList -> setList(personList),
                throwable -> throwable.printStackTrace());
    }

    static Observable<Person> listPeople() {
//        return Observable.fromCallable(() -> (Person) getPeopleList());
        return Observable.fromCallable(() -> (Person) getPeopleList());

        /*
         defer() 사용
         누군가 실제로 구독하기까지 기다리면서 Observable의 실제 생성을 최대한 낮춘다.
          */
//        return Observable.defer(
//                () -> Observable.fromCallable(getPeopleList());
//        );
    }

    static void setList(List<Person> list) {
        originList.addAll(list);

        originList.forEach(System.out::println);
    }

    static List<Person> getPeopleList() {
        List<Person> peoples = new ArrayList<>();

        peoples.add(new Person("albert1", "33"));
        peoples.add(new Person("albert2", "35"));
        peoples.add(new Person("albert3", "31"));
        peoples.add(new Person("albert4", "32"));
        peoples.add(new Person("albert5", "40"));
        peoples.add(new Person("albert6", "23"));
        peoples.add(new Person("albert7", "42"));
        peoples.add(new Person("albert8", "33"));
        peoples.add(new Person("albert9", "55"));
        peoples.add(new Person("albert10", "64"));

        return peoples;
    }

    static class Person {
        String name;
        String age;

        public Person(String name, String age) {
            this.name = name;
            this.age = age;
        }
    }


//    static void deferTest() {
//        Observable<List<Person>> allPages = Observable
//                .range(0, Integer.MAX_VALUE)
//                .map(list -> list(list))
//                .takeWhile(list -> !list.isEmpty());
//    }
}
