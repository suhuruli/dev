package statefulservice; 

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;

import java.util.HashMap;
import java.util.Map;
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

        server.createContext(this.baseAddress+"getList", new HttpHandler() {
            @Override
            public void handle(HttpExchange t) {

                try {
                	
                    String mapName1 = new String("shoppingList");
                    
                    Transaction tx = stateManager.createTransaction();
                    ReliableHashMap<String, String> map1 = stateManager
                            .<String, String> getOrAddReliableHashMapAsync(tx, mapName1).get();
                    tx.commitAsync().get();
                    tx.close();
                    
                    String itemAsKey = "";
                    tx = stateManager.createTransaction();
                    AsyncEnumeration<KeyValuePair<String, String>> kv = map1.keyValuesAsync(tx).get();
                    while (kv.hasMoreElementsAsync().get()) {
                        KeyValuePair<String, String> k = kv.nextElementAsync().get();
                        
                    	itemAsKey += k.getKey();
                    	itemAsKey += ","; 
                    	itemAsKey += k.getValue();
                    	itemAsKey += "\n";
                    }
                    
                    tx.commitAsync().get();
                    tx.close();                    
                    
                    t.sendResponseHeaders(200, 0);
                    OutputStream os = t.getResponseBody();
                    os.write(itemAsKey.getBytes());
                    os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        
        server.createContext(this.baseAddress+"addItem", new HttpHandler() {
            @Override
            public void handle(HttpExchange t) {

                try {
                    URI r = t.getRequestURI();
                    Map<String, String> params = queryToMap(r.getQuery());
                    String itemToAdd = params.get("item");
                    String valueToAdd = new String("1");
                    String mapName1 = new String("shoppingList");
                    
                    Transaction tx = stateManager.createTransaction();
                    ReliableHashMap<String, String> map1 = stateManager
                            .<String, String> getOrAddReliableHashMapAsync(tx, mapName1).get();
                    tx.commitAsync().get();
                    tx.close();
                    
                    tx = stateManager.createTransaction();
                    map1.<String,String> putIfAbsentAsync(tx, itemToAdd, valueToAdd).get();
                    tx.commitAsync().get();
                    tx.close();                   
                    
                    t.sendResponseHeaders(200, 0);
                    OutputStream os = t.getResponseBody();
                    os.write("Success".getBytes());
                    os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        
        server.createContext(this.baseAddress+"removeItem", new HttpHandler() {
            @Override
            public void handle(HttpExchange t) {

                try {
                    URI r = t.getRequestURI();
                    Map<String, String> params = queryToMap(r.getQuery());
                    String itemToRemove = params.get("item");
                    
                    String mapName1 = new String("shoppingList"); 
                    
                    Transaction tx = stateManager.createTransaction();
                    ReliableHashMap<String, String> map1 = stateManager
                            .<String, String> getOrAddReliableHashMapAsync(tx, mapName1).get();
                    tx.commitAsync().get();
                    tx.close();
                    
                    tx = stateManager.createTransaction();
                    map1.<String,String> removeAsync(tx, itemToRemove).get();
                    tx.commitAsync().get();
                    tx.close();                    
                    
                    t.sendResponseHeaders(200, 0);
                    OutputStream os = t.getResponseBody();
                    os.write("Success".getBytes());
                    os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        server.setExecutor(null);
        server.start();
    }
    
    private Map<String, String> queryToMap(String query){
        Map<String, String> result = new HashMap<String, String>();
        for (String param : query.split("&")) {
            String pair[] = param.split("=");
            if (pair.length>1) {
                result.put(pair[0], pair[1]);
            }else{
                result.put(pair[0], "");
            }
        }
        return result;
    }

    public void stop() {
        this.server.stop(0);
    }

}