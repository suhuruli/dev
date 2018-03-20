package statelessservice;

import java.time.Duration;
import java.util.logging.Logger;
import java.util.logging.Level;

import microsoft.servicefabric.services.runtime.ServiceRuntime;

public class ServiceOneServiceHost {

    private static final Logger logger = Logger.getLogger(ServiceOneServiceHost.class.getName());

    public static void main(String[] args) throws Exception{
        try {
            ServiceRuntime.registerStatelessServiceAsync("ServiceOneType", (context)-> new ServiceOneService(), Duration.ofSeconds(10));
            logger.log(Level.INFO, "Registered stateless service of type ServiceOneType");
            Thread.sleep(Long.MAX_VALUE);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Exception occurred", ex);
            throw ex;
        }
    }
}
