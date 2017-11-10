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

public class StatefulSpringService extends StatefulService {
    // the problem here is that Spring is initializing this and causes a StatefulService base not found error.
    private ReliableStateManager stateManager;
    private static final Logger logger = Logger.getLogger(StatefulSpringService.class.getName());

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
}