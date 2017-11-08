package statefulservice;

import java.util.concurrent.CompletableFuture;

import microsoft.servicefabric.data.ReliableStateManager;
import microsoft.servicefabric.services.communication.runtime.CommunicationListener;
import system.fabric.CancellationToken;
import system.fabric.ServiceContext;
import system.fabric.StatefulServiceContext;
import system.fabric.description.EndpointResourceDescription;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.ConfigurableApplicationContext; 

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class HttpCommunicationListener implements CommunicationListener {

    private ServiceContext serviceContext;
    private String listeningAddress;
    private ReliableStateManager stateManager;
    private SpringApplication application;

    public HttpCommunicationListener(ServiceContext serviceContext, ReliableStateManager stateManager) {
        this.serviceContext = serviceContext;
        this.stateManager = stateManager;
    }

    @Override
    public CompletableFuture<String> openAsync(CancellationToken cancellationToken) {
        int port = 8080;

        this.listeningAddress = String.format("http://%s:%d/", this.serviceContext.getNodeContext().getIpAddressOrFQDN(), port);

        return CompletableFuture.completedFuture(this.listeningAddress);
    }

    @Override
    public CompletableFuture<?> closeAsync(CancellationToken cancellationToken) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void abort() {
    }
}
