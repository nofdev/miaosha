package com.shangpin.miaosha.common

import org.vertx.java.core.AsyncResult
import org.vertx.java.core.Future
import org.vertx.java.core.Handler
import org.vertx.java.core.impl.DefaultFutureResult

import java.util.concurrent.LinkedBlockingQueue


public class ThresholdFutureScheduler {
    private int threshold = 20;
    private int count = 0;

    private LinkedBlockingQueue<Future<Future<?>>> queue;

    public ThresholdFutureScheduler() {
        queue = new LinkedBlockingQueue<>();
    }

    public ThresholdFutureScheduler(int threshold) {
        this()
        this.threshold = threshold;
    }

    public <T> void schedule(Future<Future<T>> future) {
        queue.put(future);
        pullAndExecute();
    }

    private synchronized void pullAndExecute() {
        while (count <= threshold && queue.size() > 0) {
            ++count
            def ff = queue.take()
            execWorkerFuture ff
        }
    }

    private void execWorkerFuture(DefaultFutureResult<DefaultFutureResult> ff) {
        def ffh = ff.handler
        ff.handler = new Handler<AsyncResult<DefaultFutureResult>>() {
            @Override
            void handle(AsyncResult<DefaultFutureResult> event) {
                //run the request task sync
                ffh.handle(event)
                //wrap completion event
                wrapCompletionFuture event.result()
            }
        }
        //run the future
        ff.setResult(new DefaultFutureResult());
    }

    private void wrapCompletionFuture(DefaultFutureResult future) {
        def fh = future.handler
        future.handler = new Handler<AsyncResult>() {
            @Override
            void handle(AsyncResult event) {
                fh.handle(event)
                --count
                pullAndExecute()
            }
        }
    }


    public static void main(String[] args) {
        def delay = { long d, Closure c ->
            new Timer().schedule(new TimerTask() {
                @Override
                void run() {
                    c()
                }
            }, d)
        }
        def randIn = { Math.round(Math.random() * it) as long }
        def request = { String s, Closure c ->
            def d = randIn(500)
            println "requsting $s in $d ms"
            delay(d) {
                c s
            }
        }

        def scheduler = new ThresholdFutureScheduler(10)
        for (int i = 0; i < 100; ++i) {
            def ff = new DefaultFutureResult<DefaultFutureResult>()
            ff.handler = new Handler<AsyncResult<DefaultFutureResult>>() {
                @Override
                void handle(AsyncResult<DefaultFutureResult> e) {
                    def f = e.result()
                    f.handler = new Handler<AsyncResult>() {
                        @Override
                        void handle(AsyncResult ee) {
                            println "complete ${ee.result()}"
                        }
                    }
                    request "http://$i", {
                        f.result = it
                    }
                }
            }
            scheduler.schedule ff
        }
    }
}
