package reactor;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * Created by jeremy on 02/21/2019.
 */
public class DelegateSub implements Subscriber<Integer> {
    @Override
    public void onSubscribe(Subscription s) {
        sub.onSubscribe(s);
    }

    @Override
    public void onNext(Integer integer) {
        sub.onNext(f.apply(integer));
    }

    @Override
    public void onError(Throwable t) {
        sub.onError(t);
    }

    @Override
    public void onComplete() {
        sub.onComplete();
    }
}
