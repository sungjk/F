package reactor;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by jeremy on 02/21/2019.
 */
public class PubSub {

    /*
    onSubscribe onNext* (onError | onComplete)?

    public interface Subscriber<T> {
        public void onSubscribe(Subscription s);
        public void onNext(T t);
        public void onError(Throwable t);
        public void onComplete();
    }
     */

    public static void main(String[] args) throws InterruptedException {
        Iterable<Integer> iter = Arrays.asList(1, 2, 3, 4, 5);
        ExecutorService es = Executors.newCachedThreadPool();

        Publisher pub = (Publisher<Object>) s -> {
            Iterator<Integer> it = iter.iterator();

            s.onSubscribe(new Subscription() {
                @Override
                public void request(long n) {
                    try {
                        es.execute(() -> {
                            int i = 0;
                            while (i++ < n) {
                                if (it.hasNext()) {
                                    s.onNext(it.next());
                                } else {
                                    s.onComplete();
                                    es.shutdown();
                                    break;
                                }
                            }
                        });
                    } catch (RuntimeException e) {
                        s.onError(e);
                    }
                }

                @Override
                public void cancel() {

                }
            });
        };

        Subscriber<Integer> sub = new Subscriber<Integer>() {
            Subscription subscription;

            @Override
            public void onSubscribe(Subscription s) {
                System.out.println(Thread.currentThread().getName() + " onSubscribe");
                this.subscription = s;
                this.subscription.request(1);
            }

            @Override
            public void onNext(Integer integer) {
                System.out.println(Thread.currentThread().getName() + " onNext " + integer);
                this.subscription.request(1);
            }

            @Override
            public void onError(Throwable t) {
                System.out.println(Thread.currentThread().getName() + " onError " + t.getMessage());
            }

            @Override
            public void onComplete() {
                System.out.println(Thread.currentThread().getName() + " onComplete");
            }
        };

        pub.subscribe(sub);

        es.awaitTermination(10, TimeUnit.HOURS);
        es.shutdown();
    }

}
