package reactor;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by jeremy on 02/21/2019.
 */
public class SchedulerEx {

    // onSubscribe onNext* (onError | onComplete)?

    public static void main(String[] args) {
        System.out.println(Thread.currentThread().getName() + " Start");

        Publisher<Integer> pub = sub -> {
            sub.onSubscribe(new Subscription() {
                @Override
                public void request(long n) {
                    System.out.println(Thread.currentThread().getName() + " request()");
                    sub.onNext(1);
                    sub.onNext(2);
                    sub.onNext(3);
                    sub.onNext(4);
                    sub.onNext(5);
                    sub.onComplete();
                }

                @Override
                public void cancel() {

                }
            });
        };
        // pub

//        Publisher<Integer> subOnPub = sub -> {
//            ExecutorService es = Executors.newSingleThreadExecutor();
//            es.execute(() -> pub.subscribe(sub));
//        };

        Publisher<Integer> pubOnPub = sub -> {
            pub.subscribe(new Subscriber<Integer>() {
                ExecutorService es = Executors.newSingleThreadExecutor();

                @Override
                public void onSubscribe(Subscription s) {
                    sub.onSubscribe(s);
                }

                @Override
                public void onNext(Integer integer) {
                    es.execute(() -> sub.onNext(integer));
                }

                @Override
                public void onError(Throwable t) {
                    es.execute(() -> sub.onError(t));
                    es.shutdown();
                }

                @Override
                public void onComplete() {
                    es.execute(() -> sub.onComplete());
                    es.shutdown();
                }
            });
        };

        // sub
        pubOnPub.subscribe(new Subscriber<Integer>() {
            @Override
            public void onSubscribe(Subscription s) {
                System.out.println(Thread.currentThread().getName() + " onSubscribe");
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(Integer integer) {
                System.out.println(Thread.currentThread().getName() + " onNext: " + integer);
            }

            @Override
            public void onError(Throwable t) {
                System.out.println(Thread.currentThread().getName() +  " onError: " + t.getMessage());
            }

            @Override
            public void onComplete() {
                System.out.println(Thread.currentThread().getName() + " onComplete");
            }
        });

        System.out.println(Thread.currentThread().getName() + " Exit");
    }

}
