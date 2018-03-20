package statefulservice;

import java.time.Duration;
import java.util.logging.Logger;
import java.util.logging.Level;

import microsoft.servicefabric.services.runtime.ServiceRuntime;

public class ServiceTwoServiceHost {

    private static final Logger logger = Logger.getLogger(ServiceTwoServiceHost.class.getName());

    public static void main(String[] args) throws Exception{
        try {
            ServiceRuntime.registerStatefulServiceAsync("ServiceTwoServiceType", (context)-> new ServiceTwoService(context), Duration.ofSeconds(10));
            logger.log(Level.INFO, "Registered stateful service of type ServiceTwoServiceType");
            Thread.sleep(Long.MAX_VALUE);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Exception occurred", ex);
            throw ex;
        }
    }
}
