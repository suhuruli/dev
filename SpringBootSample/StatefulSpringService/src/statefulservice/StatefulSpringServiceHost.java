package statefulservice;

import java.time.Duration;
import java.util.logging.Logger;
import java.util.logging.Level;

import microsoft.servicefabric.services.runtime.ServiceRuntime;

public class StatefulSpringServiceHost {

    private static final Logger logger = Logger.getLogger(StatefulSpringServiceHost.class.getName());

    public static void main(String[] args) throws Exception{
        try {
            ServiceRuntime.registerStatefulServiceAsync("StatefulSpringServiceType", (context)-> new StatefulSpringService(context), Duration.ofSeconds(10));
            logger.log(Level.INFO, "Registered stateful service of type StatefulSpringServiceType");
            Thread.sleep(Long.MAX_VALUE);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Exception occurred", ex);
            throw ex;
        }
    }
}
