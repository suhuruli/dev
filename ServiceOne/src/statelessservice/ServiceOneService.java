package statelessservice;

import java.util.concurrent.CompletableFuture;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import system.fabric.CancellationToken;
import system.fabric.description.EndpointResourceDescription;
import microsoft.servicefabric.services.communication.runtime.ServiceInstanceListener;
import microsoft.servicefabric.services.runtime.StatelessService;

public class ServiceOneService extends StatelessService {
	
    private static final String webEndpointName = "WebEndpoint";

    @Override
    protected List<ServiceInstanceListener> createServiceInstanceListeners() {
        // TODO: If your service needs to handle user requests, return the list of ServiceInstanceListeners from here.
    	
    		EndpointResourceDescription endpoint = this.getServiceContext().getCodePackageActivationContext().getEndpoint(webEndpointName);
        int port = endpoint.getPort();
        
        List<ServiceInstanceListener> listeners = new ArrayList<ServiceInstanceListener>();
        listeners.add(new ServiceInstanceListener((context) -> new HttpCommunicationListener( context, port)));
        return listeners;
    }
    
    @Override
    protected CompletableFuture<?> runAsync(CancellationToken cancellationToken) {
        // TODO: Replace the following with your own logic.
        return super.runAsync(cancellationToken);
    }
}
