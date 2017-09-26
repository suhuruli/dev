package statelessservice;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.FileNotFoundException;

import system.fabric.CancellationToken;
import system.fabric.description.EndpointResourceDescription;
import microsoft.servicefabric.services.communication.runtime.ServiceInstanceListener;
import microsoft.servicefabric.services.runtime.StatelessService;

public class VotingStatelessService extends StatelessService {
    private static final String webEndpointName = "WebEndpoint";

	private URI serviceName = null; 
    @Override
    protected List<ServiceInstanceListener> createServiceInstanceListeners() {
        // TODO: If your service needs to handle user requests, return the list of ServiceInstanceListeners from here.
        try {
			serviceName = new URI("fabric:/ReliableCollectionsSample/RCMainService");
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
        
        EndpointResourceDescription endpoint = this.getServiceContext().getCodePackageActivationContext().getEndpoint(webEndpointName);
        int port = endpoint.getPort();
        
        List<ServiceInstanceListener> listeners = new ArrayList<ServiceInstanceListener>();
        listeners.add(new ServiceInstanceListener((context) -> new HttpCommunicationListener(serviceName, context, port)));
        return listeners;
    }

    @Override
    protected CompletableFuture<?> runAsync(CancellationToken cancellationToken) {
        // TODO: Replace the following with your own logic.
        return super.runAsync(cancellationToken);
    }
}
