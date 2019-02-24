package reactor;

import reactor.core.publisher.Flux;

/**
 * Created by jeremy on 02/21/2019.
 */
public class ReactorEx {

    public static void main(String[] args) {
        Flux.<Integer>create(e -> {
            e.next(1);
            e.next(2);
            e.next(3);
            e.complete();
        })
        .log()
        .map(i -> i * 10)
        .log()
        .reduce(0, (a, b) -> a + b)
        .log()
        .subscribe(System.out::println);
    }

}
