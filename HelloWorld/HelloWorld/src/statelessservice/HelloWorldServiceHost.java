package statelessservice;

import java.time.Duration;
import java.util.logging.Logger;
import java.util.logging.Level;

import microsoft.servicefabric.services.runtime.ServiceRuntime;

public class HelloWorldServiceHost {

    private static final Logger logger = Logger.getLogger(HelloWorldServiceHost.class.getName());

    public static void main(String[] args) throws Exception {
        try {
            ServiceRuntime.registerStatelessServiceAsync("HelloWorldType", (context) -> new HelloWorldService(), Duration.ofSeconds(10));
            logger.log(Level.INFO, "Registered stateless service of type HelloWorldType. ");
            Thread.sleep(Long.MAX_VALUE);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Exception occured", ex);
            throw ex;
        }
    }
}
