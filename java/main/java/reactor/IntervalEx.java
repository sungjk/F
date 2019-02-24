package reactor;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by jeremy on 02/21/2019.
 */
public class IntervalEx {

    public static void main(String[] args) {
        Publisher<Integer> pub = sub -> {
            sub.onSubscribe(new Subscription() {
                int num = 0;
                volatile boolean cancelled = false;

                @Override
                public void request(long n) {
                    System.out.println(Thread.currentThread().getName() + " request");
                    ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
                    exec.scheduleAtFixedRate(() -> {
                        if (cancelled) {
                            exec.shutdown();
                            return;
                        }
                        sub.onNext(num++);
                    }, 0, 500, TimeUnit.MILLISECONDS);
                }

                @Override
                public void cancel() {
                    System.out.println(Thread.currentThread().getName() + " cancel");
                    cancelled = true;
                }
            });
        };

        Publisher<Integer> takePub = sub -> {
            pub.subscribe(new Subscriber<Integer>() {
                int count = 0;
                Subscription subc;

                @Override
                public void onSubscribe(Subscription s) {
                    this.subc = s;
                    sub.onSubscribe(s);
                }

                @Override
                public void onNext(Integer integer) {
                    sub.onNext(integer);
                    if (++count >= 5) {
                        subc.cancel();
                    }
                }

                @Override
                public void onError(Throwable t) {
                    sub.onError(t);
                }

                @Override
                public void onComplete() {
                    sub.onComplete();
                }
            });
        };

        takePub.subscribe(new Subscriber<Integer>() {
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
                System.out.println(Thread.currentThread().getName() + " onError: " + t);
            }

            @Override
            public void onComplete() {
                System.out.println(Thread.currentThread().getName() + " onComplete");
            }
        });
    }

}
