package statefulservice; 

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;


import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import microsoft.servicefabric.data.ConditionalValue;
import microsoft.servicefabric.data.ReliableStateManager;
import microsoft.servicefabric.data.Transaction;
import microsoft.servicefabric.data.ReliableState;
import microsoft.servicefabric.data.collections.ReliableHashMap;
import microsoft.servicefabric.data.utilities.AsyncEnumeration;
import microsoft.servicefabric.data.utilities.KeyValuePair;

public class HttpServer {
    String baseAddress;
    int port;
    private com.sun.net.httpserver.HttpServer server;
    private ReliableHashMap<String, String> map;
    private ReliableStateManager stateManager;

    public HttpServer(String baseAddress, int port, ReliableStateManager stateManager) {
        this.baseAddress = baseAddress;
        this.port = port;
        this.stateManager = stateManager;
    }

    public void start() {
        try {
            server = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(this.port), 0);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        server.createContext(this.baseAddress, new HttpHandler() {
            @Override
            public void handle(HttpExchange t) {

                try {
                    // URI mapName1 = new URI("myMap1");
                	String buffer = ""; 
                	
                    String mapName1 = new String("myMap1");
                    System.out.println("Start");
                    buffer += "start"; 
                    
                    ReliableHashMap<String, String> map1 = stateManager
                            .<String, String> getOrAddReliableHashMapAsync(mapName1).get();
                    System.out.println("After adding map1");
                    buffer += "After adding map1"; 
        
                    boolean isPresent = stateManager.tryGetReliableHashMapAsync(mapName1).get().hasValue();
                    System.out.println("Is Map1 present :: " + isPresent);
                    stateManager.removeAsync(mapName1).get();
                    System.out.println("After Remove map1");
                    isPresent = stateManager.tryGetReliableHashMapAsync(mapName1).get().hasValue();
                    System.out.println("Is Map1 present :: " + isPresent);

                    // mapName1 = new URI("myMap2");
                    mapName1 = new String("myMap2");
                    Transaction tx = stateManager.createTransaction();
                    map1 = (ReliableHashMap<String, String>) stateManager
                            .<String, String> getOrAddReliableHashMapAsync(tx, mapName1).get();
                    tx.commitAsync().get();
                    tx.close();
                    System.out.println("After adding map2");
                    isPresent = stateManager.tryGetReliableHashMapAsync(mapName1).get().hasValue();
                    System.out.println("Is Map2 present  ::" + isPresent);
                    tx = stateManager.createTransaction();
                    stateManager.removeAsync(mapName1).get();
                    tx.commitAsync().get();
                    tx.close();
                    System.out.println("After removing map2");
                    isPresent = stateManager.tryGetReliableHashMapAsync(mapName1).get().hasValue();
                    System.out.println("Is Map2 present  ::" + isPresent);

                    t.sendResponseHeaders(200, 0);
                    OutputStream os = t.getResponseBody();
                    os.write("Done".getBytes());
                    os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        server.setExecutor(null);
        server.start();
    }

    public void stop() {
        this.server.stop(0);
    }

}