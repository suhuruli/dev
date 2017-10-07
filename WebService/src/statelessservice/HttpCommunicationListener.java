// ------------------------------------------------------------
//  Copyright (c) Microsoft Corporation.  All rights reserved.
//  Licensed under the MIT License (MIT). See License.txt in the repo root for license information.
// ------------------------------------------------------------

package statelessservice;

import java.io.*;
import java.net.*;
import com.sun.net.httpserver.*;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.FileNotFoundException;

import microsoft.servicefabric.services.communication.client.FabricServicePartitionClient;
import microsoft.servicefabric.services.communication.runtime.CommunicationListener;
import microsoft.servicefabric.services.communication.client.ExceptionHandler;
import microsoft.servicefabric.services.runtime.StatelessServiceContext;
import microsoft.servicefabric.services.client.ServicePartitionKey;
import system.fabric.CancellationToken;

public class HttpCommunicationListener implements CommunicationListener {

    private static final Logger logger = Logger.getLogger(HttpCommunicationListener.class.getName());
    
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final int STATUS_OK = 200;
    
    private com.sun.net.httpserver.HttpServer server;
    private FabricServicePartitionClient<HttpCommunicationClient> client;
    private StatelessServiceContext context;
    private ServicePartitionKey partitionKey;
    private final int port;

    public HttpCommunicationListener(URI serviceName, StatelessServiceContext context, int port) {
        List<ExceptionHandler> exceptionHandlers = new ArrayList<ExceptionHandler>(){{
            add(new CommunicationExceptionHandler());
        }}; 
        this.partitionKey = new ServicePartitionKey(0); 
        this.client = new FabricServicePartitionClient<HttpCommunicationClient>(new HttpCommunicationClientFactory(null, exceptionHandlers), serviceName, this.partitionKey);
        this.context = context;
        this.port = port;
    }

    public void start() {
        try {
            server = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(this.port), 0);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
        
        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange t) {
                try {
	            	String root = "wwwroot/";
	                URI uri = t.getRequestURI();
	                System.out.println("looking for: "+ root + uri.getPath());
	                String path = "index.html";
	                File file = new File(root + path).getCanonicalFile();
	
	                if (!file.isFile()) {
	                  // Object does not exist or is not a file: reject with 404 error.
	                  String response = "404 (Not Found)\n";
	                  t.sendResponseHeaders(404, response.length());
	                  OutputStream os = t.getResponseBody();
	                  os.write(response.getBytes());
	                  os.close();
	                } else {
	                  // Object exists and is a file: accept with response code 200.
	                  String mime = "text/html";
	                  if(path.substring(path.length()-3).equals(".js")) mime = "application/javascript";
	                  if(path.substring(path.length()-3).equals("css")) mime = "text/css";            
	
	                  Headers h = t.getResponseHeaders();
	                  h.set("Content-Type", mime);
	                  t.sendResponseHeaders(200, 0);              
	
	                  OutputStream os = t.getResponseBody();
	                  FileInputStream fs = new FileInputStream(file);
	                  final byte[] buffer = new byte[0x10000];
	                  int count = 0;
	                  while ((count = fs.read(buffer)) >= 0) {
	                    os.write(buffer,0,count);
	                  }
	                  
	                  fs.close();
	                  os.close();
	                }  
                } catch (Exception e) {
                    logger.log(Level.WARNING, null, e);
                    // Let the handle loop continue
                }
            }
        });

        server.createContext("/getStatelessList", new HttpHandler() {
            @Override
            public void handle(HttpExchange t) {
                try {
                    URI r = t.getRequestURI();
                    String method = t.getRequestMethod();
                    
                    t.sendResponseHeaders(200,0);
                    OutputStream os = t.getResponseBody();
                    client.invokeWithRetryAsync((c) -> {
                        CompletableFuture<Boolean> b = new CompletableFuture<>();
                        String address = c.endPointAddress();
                        int index = address.indexOf('/', 7);
                        if (index != -1) {
                            address = address.substring(0, index);
                        }

                        address = address + "/getList";
                        URL clientUrl;
                        try {
                            clientUrl = new URL(address);
                            HttpURLConnection conn = (HttpURLConnection) clientUrl.openConnection();
                            conn.setRequestMethod(method);
                            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                            String line;
                            ArrayList<String> list = new ArrayList<String>();
                            
                            while ((line = rd.readLine()) != null) {
                                list.add(line);
                            }
                            String json = new Gson().toJson(list);
                            os.write(json.getBytes("UTF-8"));
                            rd.close();
                            b.complete(true);
                        } catch (FileNotFoundException ex) {
                            logger.log(Level.WARNING, null, ex);
                            b.complete(true);
                        } catch (Exception ex) {
                            logger.log(Level.SEVERE, null, ex);
                            b.completeExceptionally(ex);
                        }

                        return b;
                    }).get();
                    os.close();
                } catch (Exception e) {
                    logger.log(Level.WARNING, null, e);
                }
            }
        });
        
        server.createContext("/removeItem", new HttpHandler() {
            @Override
            public void handle(HttpExchange t) {
                try {
                    URI r = t.getRequestURI();
                    Map<String, String> params = queryToMap(r.getQuery());
                    String itemToRemove = params.get("item");
                    
                    String method = t.getRequestMethod();
                    
                    t.sendResponseHeaders(200,0);
                    OutputStream os = t.getResponseBody();
                    client.invokeWithRetryAsync((c) -> {
                        CompletableFuture<Boolean> b = new CompletableFuture<>();
                        String address = c.endPointAddress();
                        int index = address.indexOf('/', 7);
                        if (index != -1) {
                            address = address.substring(0, index);
                        }

                        address = address + "/removeItem?item="+itemToRemove;
                        URL clientUrl;
                        try {
                            clientUrl = new URL(address);
                            HttpURLConnection conn = (HttpURLConnection) clientUrl.openConnection();
                            conn.setRequestMethod(method);
                            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                            String line;
                            ArrayList<String> list = new ArrayList<String>();
                            
                            while ((line = rd.readLine()) != null) {
                                list.add(line);
                            }
                            String json = new Gson().toJson(list);
                            os.write(json.getBytes("UTF-8"));
                            rd.close();
                            b.complete(true);
                        } catch (FileNotFoundException ex) {
                            logger.log(Level.WARNING, null, ex);
                            b.complete(true);
                        } catch (Exception ex) {
                            logger.log(Level.SEVERE, null, ex);
                            b.completeExceptionally(ex);
                        }

                        return b;
                    }).get();
                    os.close();
                } catch (Exception e) {
                    logger.log(Level.WARNING, null, e);
                }
            }
        });
        
        server.createContext("/addItem", new HttpHandler() {
            @Override
            public void handle(HttpExchange t) {
                try {
                    URI r = t.getRequestURI();
                    Map<String, String> params = queryToMap(r.getQuery());
                    String itemToAdd = params.get("item");
                    
                    String method = t.getRequestMethod();
                    
                    t.sendResponseHeaders(200,0);
                    OutputStream os = t.getResponseBody();
                    client.invokeWithRetryAsync((c) -> {
                        CompletableFuture<Boolean> b = new CompletableFuture<>();
                        String address = c.endPointAddress();
                        int index = address.indexOf('/', 7);
                        if (index != -1) {
                            address = address.substring(0, index);
                        }

                        address = address + "/addItem?item="+itemToAdd;
                        URL clientUrl;
                        try {
                            clientUrl = new URL(address);
                            HttpURLConnection conn = (HttpURLConnection) clientUrl.openConnection();
                            conn.setRequestMethod(method);
                            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                            String line;
                            ArrayList<String> list = new ArrayList<String>();
                            
                            while ((line = rd.readLine()) != null) {
                                list.add(line);
                            }
                            String json = new Gson().toJson(list);
                            os.write(json.getBytes("UTF-8"));
                            rd.close();
                            b.complete(true);
                        } catch (FileNotFoundException ex) {
                            logger.log(Level.WARNING, null, ex);
                            b.complete(true);
                        } catch (Exception ex) {
                            logger.log(Level.SEVERE, null, ex);
                            b.completeExceptionally(ex);
                        }

                        return b;
                    }).get();
                    os.close();
                } catch (Exception e) {
                    logger.log(Level.WARNING, null, e);
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

    private void stop() {
        if (null != server)
            server.stop(0);
    }

    @Override
    public CompletableFuture<String> openAsync(CancellationToken cancellationToken) {
        this.start();
        String publishUri = String.format("http://%s:%d/", this.context.getNodeContext().getIpAddressOrFQDN(), port);
        return CompletableFuture.completedFuture(publishUri);
    }

    @Override
    public CompletableFuture<?> closeAsync(CancellationToken cancellationToken) {
        this.stop();
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public void abort() {
        this.stop();
    }
}
