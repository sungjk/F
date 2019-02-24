package reactor;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by jeremy on 02/21/2019.
 */
public class PubSub1 {

    /*
    https://github.com/reactive-streams/reactive-streams-jvm

    Reactive Streams - Operators

    Publisher -> [Data1] -> Operator1 -> [Data2] -> Operator2 -> [Data3] -> Subscriber

    1. map (data1 -> f -> data2)
    pub -> [Data1] -> mapPub -> [Data2] -> logSub
                   <- subscribe(logSub)
                   -> onSubscribe(s)
                   -> onNext
                   -> onNext
                    -> onComplete
     */

    public static void main(String[] args) {
        Publisher<Integer> pub = iterPub(Stream.iterate(1, a -> a + 1).limit(10).collect(Collectors.toList()));
        Publisher<String> mapPub = mapPub(pub, i -> "[" + i + "]");
//        Publisher<Integer> mapPub2 = mapPub(mapPub, i -> -i);
//        Publisher<Integer> sumPub = sumPub(pub);
        Publisher<StringBuilder> reducePub = reducePub(pub, new StringBuilder(), (a, b) -> a.append(b + "."));

        reducePub.subscribe(logSub());
    }

    // 1, 2, 3, 4, 5
    // 0 -> (0,1) -> 0 + 1 = 1
    // 1 -> (1,2) -> 1 + 2 = 3
    // 3 -> (3,3) -> 3 + 3 = 6
    // ...
    private static <T, R> Publisher<R> reducePub(Publisher<T> pub, R init, BiFunction<R, T, R> bf) {
        return sub -> pub.subscribe(new DelegateSub<T, R>(sub) { // Integer -> String
            R result = init;

            @Override
            public void onNext(T integer) {
                result = bf.apply(result, integer);
            }

            @Override
            public void onComplete() {
                sub.onNext(result);
                sub.onComplete();
            }
        });
    }

    private static Publisher<Integer> sumPub(Publisher<Integer> pub) {
        return sub -> pub.subscribe(new DelegateSub<Integer, Integer>(sub) {
            int sum = 0;

            @Override
            public void onNext(Integer integer) {
                sum += integer;
            }

            @Override
            public void onComplete() {
                sub.onNext(sum);
                sub.onComplete();
            }
        });
    }

    // T -> R
    private static <T, R> Publisher<R> mapPub(Publisher<T> pub, Function<T, R> f) {
        return new Publisher<R>() {
            @Override
            public void subscribe(Subscriber<? super R> sub) { // sub: logSub
                pub.subscribe(new DelegateSub<T, R>(sub) {
                    @Override
                    public void onNext(T integer) {
                        sub.onNext(f.apply(integer));
                    }
                });
            }
        };
    }

    private static <T> Subscriber<T> logSub() {
        return new Subscriber<T>() {
            @Override
            public void onSubscribe(Subscription s) {
                System.out.println(Thread.currentThread().getName() + " onSubscribe");
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(T integer) {
                System.out.println(Thread.currentThread().getName() + " onNext: " + integer);
            }

            @Override
            public void onError(Throwable t) {
                System.out.println(Thread.currentThread().getName() + " onError: " + t.getMessage());
            }

            @Override
            public void onComplete() {
                System.out.println(Thread.currentThread().getName() + " onComplete");
            }
        };
    }

    private static Publisher<Integer> iterPub(final List<Integer> iter) {
        return new Publisher<Integer>() {
            @Override
            public void subscribe(Subscriber<? super Integer> s) {
                s.onSubscribe(new Subscription() {
                    @Override
                    public void request(long n) {
                        try {
                            iter.forEach(s::onNext);
                            s.onComplete();
                        } catch (Throwable t) {
                            s.onError(t);
                        }
                    }

                    @Override
                    public void cancel() {

                    }
                });
            }
        };
    }

}
