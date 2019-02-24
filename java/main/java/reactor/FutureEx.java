package reactor;

import java.util.Objects;
import java.util.concurrent.*;

/**
 * Created by jeremy on 02/21/2019.
 */
public class FutureEx {
    interface SuccessCallback {
        void onSuccess(String result);
    }

    interface ExceptionCallback {
        void onError(Throwable t);
    }

    public static class CallbackFutureTask extends FutureTask<String> {
        SuccessCallback sc;
        ExceptionCallback ec;

        public CallbackFutureTask(Callable<String> callable, SuccessCallback sc, ExceptionCallback ec) {
            super(callable);
            this.sc = Objects.requireNonNull(sc);
            this.ec = Objects.requireNonNull(ec);
        }

        @Override
        protected void done() {
            try {
                sc.onSuccess(get());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                ec.onError(e.getCause());
            }
        }
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService es = Executors.newCachedThreadPool();

//        Future<String> f = es.submit(() -> {
//            Thread.sleep(2000);
//            System.out.println(Thread.currentThread().getName() + " Async");
//            return "Hello";
//        });

//        FutureTask<String> ft = new FutureTask<String>(() -> {
//            Thread.sleep(2000);
//            System.out.println(Thread.currentThread().getName() + " AsyncTask");
//            return "Hello";
//        }) {
//            @Override
//            protected void done() {
//                try {
//                    System.out.println(Thread.currentThread().getName() + " " + get());
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                } catch (ExecutionException e) {
//                    e.printStackTrace();
//                }
//            }
//        };

        CallbackFutureTask cft = new CallbackFutureTask(() -> {
            Thread.sleep(2000);
            if (true) throw new RuntimeException("CallbackTask Error!!");
            System.out.println(Thread.currentThread().getName() + " CallbackTask");
            return "Hello";
        }, result -> {
            System.out.println(Thread.currentThread().getName() + " Result: " + result);
        }, error -> {
            System.out.println(Thread.currentThread().getName() + " Error: " + error.getMessage());
        });

        es.execute(cft);
        es.shutdown();
    }
}
