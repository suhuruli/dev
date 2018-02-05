package statelessservice;

import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;

import system.fabric.description.EndpointResourceDescription;

import microsoft.servicefabric.services.communication.runtime.ServiceInstanceListener;
import microsoft.servicefabric.services.runtime.StatelessService;

public class VotingWebService extends StatelessService {
    private static final Logger logger = Logger.getLogger(VotingWebService.class.getName());

    // This is the name of the endpoint that specifies the port this service listens on. 
    // This value is gathered from the ServiceManifest.xml for this service located under
    // VotingApplication/VotingWebPkg/ServiceManifest.xml
    private static final String webEndpointName = "WebEndpoint";

    // Method responsible for instantiating the communication listener 
    @Override
    protected List<ServiceInstanceListener> createServiceInstanceListeners() {

        EndpointResourceDescription endpoint = this.getServiceContext().getCodePackageActivationContext().getEndpoint(webEndpointName);
        int port = endpoint.getPort();
        
        List<ServiceInstanceListener> listeners = new ArrayList<ServiceInstanceListener>();
        listeners.add(new ServiceInstanceListener((context) -> new HttpCommunicationListener(context, port)));
        return listeners;
    }
}