package reactor;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by jeremy on 02/21/2019.
 */
public class FluxScEx {

    /*
    Daemon thread vs. User thread
     */

    public static void main(String[] args) throws InterruptedException {
        System.out.println(Thread.currentThread().getName() + " Start");

//        Flux.range(1, 10)
//            .publishOn(Schedulers.newSingle("pub"))
//            .log()
//            .subscribeOn(Schedulers.newSingle("sub"))
//            .subscribe(System.out::println);

        Flux.interval(Duration.ofMillis(200))
            .take(10)
            .subscribe(s -> System.out.println(Thread.currentThread().getName() + " onNext: " + s));
        TimeUnit.SECONDS.sleep(5);

        System.out.println(Thread.currentThread().getName() + " Exit");
    }

}
