package statefulservice;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import microsoft.servicefabric.data.ReliableStateManager;
import microsoft.servicefabric.data.Transaction;
import microsoft.servicefabric.services.communication.runtime.ServiceReplicaListener;
import microsoft.servicefabric.services.runtime.StatefulService;
import system.fabric.CancellationToken;
import system.fabric.StatefulServiceContext;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.context.ConfigurableApplicationContext; 

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

@Configuration
@ComponentScan
@EnableAutoConfiguration
public class StatefulSpringService extends StatefulService {
    private ReliableStateManager stateManager;
    private static final Logger logger = Logger.getLogger(StatefulSpringService.class.getName());
    private SpringApplication application; 
    private ConfigurableApplicationContext context; 

    private BufferedWriter bw = null;
    private FileWriter fw = null;

    protected StatefulSpringService (StatefulServiceContext statefulServiceContext) {
        super (statefulServiceContext);
        this.stateManager = this.getReliableStateManager();

        logger.log(Level.INFO, "TESTING");
    }

    @Override
    protected List<ServiceReplicaListener> createServiceReplicaListeners() {
        ServiceReplicaListener listener1 = new ServiceReplicaListener(initParams -> {
            return new HttpCommunicationListener(this.getServiceContext(), 
                    this.getReliableStateManager());
        }, "Listener1");
        List<ServiceReplicaListener> listenerList = new ArrayList<>();
        listenerList.add(listener1);
        return listenerList;
    }

    @Override
    protected CompletableFuture<?> runAsync(CancellationToken cancellationToken) {
        // TODO: Replace the following with your own logic.
        logger.log(Level.INFO, "TESTING1");
        ConfigurableApplicationContext ctx = SpringApplication.run(StatefulSpringService.class, null);
        logger.log(Level.INFO, ctx.toString());
        return CompletableFuture.completedFuture(ctx);
    }
}

@RestController
class GreetingController {
    
    @RequestMapping("/hello/{name}")
    String hello(@PathVariable String name) {
        return "Hello, " + name + "!";
    }
}
