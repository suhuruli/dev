package reliablecollections.sample.service;

import java.time.Duration;

import microsoft.servicefabric.services.runtime.ServiceRuntime;

public class ReliableCollectionsSampleServiceHost {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Before registering main service");
        ServiceRuntime.registerStatefulServiceAsync("RCMainService", 
                context -> new ReliableCollectionsSampleService(context), 
                Duration.ofMinutes(1));
        System.out.println("Before registering main service");
        Thread.sleep(Long.MAX_VALUE);
    }
    
    static {
        System.loadLibrary("jFabricRuntime");
        System.loadLibrary("jFabricCommon");   
    }
}
