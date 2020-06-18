package r4j.buffer;


import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.vavr.control.Try;

public class RingBufferDemo
{
    static int callCounter=0;
    CBConfig cbConfig;
    CircuitBreaker cb;
    TimeLimiter timeLimiter;
    public RingBufferDemo(CBConfig cbConfig){
        this.cbConfig = cbConfig;
        this.cb = cb = this.cbConfig.getCircuitBreaker("hello");
        this.timeLimiter = this.cbConfig.getTimeLimiter("hello");
    }

    public static void main(String[] args)
            throws InterruptedException
    {
        RingBufferDemo demo = new RingBufferDemo(new CBConfig());

        for(int i=0;i<120;i++){
            System.out.println("counter = " + (i + 1));
            Thread.sleep(1000);
            CompletableFuture completableFuture = getCompleteableFuture(demo);
            Callable<String> callable = createCallable(completableFuture,demo.timeLimiter);
            callable = CircuitBreaker.decorateCallable(demo.cb, callable);
            Try.of(callable::call)
               .onSuccess(result->{
                   System.out.println(result);
                   demo.sayGreet();
                   System.out.println("Successful call count: " + demo.cb.getMetrics().getNumberOfSuccessfulCalls()
                                      + " | Failed call count: " +  demo.cb.getMetrics().getNumberOfFailedCalls()
                                      + " | Failure rate %:" +  demo.cb.getMetrics().getFailureRate() + " | State: "
                                      +  demo.cb.getState() +" | Buffered cals:" + demo.cb.getMetrics().getNumberOfBufferedCalls());
               })
               .onFailure(failure-> {
                   System.out.println("Successful call count: " + demo.cb.getMetrics().getNumberOfSuccessfulCalls()
                                      + " | Failed call count: " +  demo.cb.getMetrics().getNumberOfFailedCalls()
                                      + " | Failure rate %:" +  demo.cb.getMetrics().getFailureRate() + " | State: "
                                      +  demo.cb.getState() +" | Buffered cals:" + demo.cb.getMetrics().getNumberOfBufferedCalls());
                   System.out.println("FAILED");
                   demo.cb.getEventPublisher()
                          .onStateTransition(event -> {
                                                        if(event.getStateTransition().getToState().equals(CircuitBreaker.State.HALF_OPEN)){
                                                            callCounter=0;
                                                        }
                   });
               });
        }



    }

    private static Callable<String> createCallable(CompletableFuture completableFuture,
                                                   TimeLimiter timeLimiter)
    {
        return  TimeLimiter.decorateFutureSupplier(timeLimiter,()->completableFuture);
    }

    private static CompletableFuture getCompleteableFuture(RingBufferDemo demo)
    {
        return CompletableFuture.supplyAsync(()-> demo.remoteCall("Dinesh"));
    }

    private String remoteCall(String name){
        callCounter++;
        if (callCounter <= 10) {
            return "Hi " + name;
        } else {
            try
            {
                Thread.sleep(1000);
                return "Hi " + name;
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        return null;


    }

    private void sayGreet(){
        System.out.println("I am good. Nice to meet you");
    }
}
