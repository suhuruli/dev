package statefulservice;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import microsoft.servicefabric.data.ReliableStateManager;
import microsoft.servicefabric.services.communication.runtime.CommunicationListener;
import system.fabric.CancellationToken;
import system.fabric.ServiceContext;
import system.fabric.StatefulServiceContext;
import system.fabric.description.EndpointResourceDescription;

public class HttpCommunicationListener implements CommunicationListener {

    private ServiceContext serviceContext;
    private String listeningAddress;
    private ReliableStateManager stateManager;
    private TestSpring testSpring; 
    private static final Logger logger = Logger.getLogger(HttpCommunicationListener.class.getName());

    public HttpCommunicationListener(ServiceContext serviceContext, ReliableStateManager stateManager) {
        this.serviceContext = serviceContext;
        this.stateManager = stateManager;
    }

    @Override
    public CompletableFuture<String> openAsync(CancellationToken cancellationToken) {
        int port = 8080;
        logger.log(Level.INFO, "Coming through openAsync in HTTPCommunicationListener.");

        this.listeningAddress = String.format("http://%s:%d/", this.serviceContext.getNodeContext().getIpAddressOrFQDN(), port);
        testSpring = new TestSpring();

        return CompletableFuture.completedFuture(this.listeningAddress);
    }

    @Override
    public CompletableFuture<?> closeAsync(CancellationToken cancellationToken) {
        logger.log(Level.INFO, "Coming through closeAsync in HTTPCommunicationListener.");
        testSpring.closeApplication();
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public void abort() {
        logger.log(Level.INFO, "Coming through abort in HTTPCommunicationListener.");        
        testSpring.closeApplication();
    }
}