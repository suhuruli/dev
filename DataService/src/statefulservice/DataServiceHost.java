package statefulservice;

import java.time.Duration;
import java.util.logging.Logger;
import java.util.logging.Level;

import microsoft.servicefabric.services.runtime.ServiceRuntime;

public class DataServiceHost {

    private static final Logger logger = Logger.getLogger(DataServiceHost.class.getName());

    public static void main(String[] args) throws Exception{
        try {
            ServiceRuntime.registerStatefulServiceAsync("DataServiceType", (context)-> new DataService(context), Duration.ofSeconds(10));
            logger.log(Level.INFO, "Registered stateful service of type DataServiceType");
            Thread.sleep(Long.MAX_VALUE);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Exception occurred", ex);
            throw ex;
        }
    }
}
