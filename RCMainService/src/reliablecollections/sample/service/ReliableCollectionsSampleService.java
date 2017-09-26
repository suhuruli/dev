package reliablecollections.sample.service;

import java.util.ArrayList;
import java.util.List;

import microsoft.servicefabric.services.communication.runtime.ServiceReplicaListener;
import microsoft.servicefabric.services.runtime.StatefulService;
import system.fabric.StatefulServiceContext;

public class ReliableCollectionsSampleService extends StatefulService {

    protected ReliableCollectionsSampleService(StatefulServiceContext statefulServiceContext) {
        super(statefulServiceContext);
        System.out.println("After ReliableCollectionsSampleService constructor");
    }
    
    @Override
    public List<ServiceReplicaListener> createServiceReplicaListeners() {
        ServiceReplicaListener listener1 = new ServiceReplicaListener(initParams -> {
                return new HttpCommunicationListener(this.getServiceContext(), 
                        this.getReliableStateManager());
        }, "ReliableCollectionsListener1");
        List<ServiceReplicaListener> listenerList = new ArrayList<>();
        listenerList.add(listener1);
        return listenerList;
    }

}
