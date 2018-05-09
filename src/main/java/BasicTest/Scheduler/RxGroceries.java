package BasicTest.Scheduler;

import io.reactivex.Observable;

import java.math.BigDecimal;


public class RxGroceries {
    static void log(String message) {
        System.out.println(Thread.currentThread().getName() + " | " + message);
    }

    Observable<BigDecimal> purchase(String productName, int quantity) {
        return Observable.fromCallable( () ->
                doPurchase(productName, quantity));
    }

    BigDecimal doPurchase(String productName, int quantity) {
        log("Purchasing " + quantity + " " + productName);
        // 실제 로직
        BigDecimal priceForProduct;

        priceForProduct = BigDecimal.valueOf(1000 * quantity);

        log("Done " + quantity + " " + productName);

        return priceForProduct;
    }
}
