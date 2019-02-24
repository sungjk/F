package reactor;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by jeremy on 02/21/2019.
 */
public class Obs {

    // 1. Complete
    // 2. Error(handling, recover, fallback, ...)

    // Iterable(pull) <---> Observable(push)
    static class IntObservable extends Observable implements Runnable {

        @Override
        public void run() {
            for (int i = 1; i <= 10; i++) {
                setChanged();
                notifyObservers(i);     // push
                // int i = it.next();   // pull
            }
        }
    }

    public static void main(String[] args) {
        Observer ob = (o, arg) -> System.out.println(Thread.currentThread().getName() + " " + arg);

        IntObservable io = new IntObservable();
        io.addObserver(ob);

        ExecutorService es = Executors.newSingleThreadExecutor();
        es.execute(io);

        System.out.println(Thread.currentThread().getName() + " EXIT");
        es.shutdown();
    }

}
