package reactor;

import org.springframework.util.StopWatch;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by jeremy on 02/21/2019.
 */
public class LoadTest {
    static AtomicInteger counter = new AtomicInteger(0);

    public static void main(String[] args) {
        ExecutorService es = Executors.newFixedThreadPool(100);

        WebClient wc = WebClient.builder()
            .baseUrl("http://localhost:8080/test")
            .build();

        StopWatch main = new StopWatch();
        main.start();

        for (int i = 0; i < 100; i++) {
            es.execute(() -> {
                int idx = counter.addAndGet(1);
                System.out.println(Thread.currentThread().getName() + " Thread " + idx);

                StopWatch sw = new StopWatch();
                sw.start();

                wc.get().exchange().block();
                sw.stop();

                System.out.println(Thread.currentThread().getName() + " Elapsed: " + idx + " " + sw.getTotalTimeSeconds());
            });
        }
    }
}
