package reliablecollections.sample.service;

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
                    String mapName1 = new String("myMap1");
                    System.out.println("Start");
                    ReliableHashMap<String, String> map1 = stateManager
                            .<String, String> getOrAddReliableHashMapAsync(mapName1).get();
                    System.out.println("After adding map1");
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
        server.createContext(this.baseAddress + "/TRTest", new HttpHandler() {
            @Override
            public void handle(HttpExchange t) {
                try {
                    System.out.println("Start");
                    ReliableHashMap<String, String> mp1 = stateManager
                            .<String, String> getOrAddReliableHashMapAsync("mp1").get();
                    System.out.println("After Get Or Add map1");
                    ReliableHashMap<String, String> mp2 = stateManager
                            .<String, String> getOrAddReliableHashMapAsync("mp2").get();
                    System.out.println("After Get Or Add map2");
                    ReliableHashMap<String, String> mp3 = stateManager
                            .<String, String> getOrAddReliableHashMapAsync("mp3").get();
                    System.out.println("After Get Or Add map3");
                    ConditionalValue<ReliableHashMap<String, String>> cv = stateManager.<String, String>tryGetReliableHashMapAsync("mp2").get();
                    System.out.println("Try Get 1 :: " + cv.hasValue() + " :: " + cv.getValue());

                    AsyncEnumeration<ReliableState> e = stateManager.getAsyncEnumerator();
                    System.out.println(" Enumerator 1 ");
                    while (e.hasMoreElementsAsync().get()) {
                        System.out.println(e.nextElementAsync().get().getName());
                    }
                    stateManager.removeAsync("mp1").get();
                    ConditionalValue<ReliableHashMap<String, String>> cv1 = stateManager.<String, String>tryGetReliableHashMapAsync("mp1").get();
                    System.out.println("Try Get 2 :: " + cv1.hasValue() + " :: " + cv1.getValue());
                    e = stateManager.getAsyncEnumerator();
                    System.out.println(" Enumerator 2 ");
                    while (e.hasMoreElementsAsync().get()) {
                        System.out.println(e.nextElementAsync().get().getName());
                    }
                    t.sendResponseHeaders(200, 0);
                    OutputStream os = t.getResponseBody();

                    os.write("Done".getBytes());
                    os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        server.createContext(this.baseAddress + "/Complex", new HttpHandler() {
            @Override
            public void handle(HttpExchange t) {
                try {
                    System.out.println("Start");
                    ComplexType ctype = new ComplexType();
                    ctype.a = 5;
                    ctype.b = "Hello World";
                    ctype.c = 3.1;

                    ReliableHashMap<String, ComplexType> map = stateManager
                            .<String, ComplexType> getOrAddReliableHashMapAsync("mp1").get();
                    System.out.println("After Get Or Add map1");

                    Transaction tx = stateManager.createTransaction();
                    map.putAsync(tx, "k1", ctype).get();
                    System.out.println(" Added Complex type");
                    tx.commitAsync().get();
                    tx.close();

                    Transaction tx1 = stateManager.createTransaction();
                    ComplexType ct2 = map.getAsync(tx1, "k1").get().getValue();
                    System.out.println("Get Complex type: a - " + ct2.a + " b - " + ct2.b + " c - " + ct2.c);
                    tx1.close();

                    t.sendResponseHeaders(200, 0);
                    OutputStream os = t.getResponseBody();

                    os.write("Done".getBytes());
                    os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        server.createContext(this.baseAddress + "/BasicStateful", new HttpHandler() {
            @Override
            public void handle(HttpExchange t) {
                try {
                    System.out.println("Start");
                    ComplexType ctype = new ComplexType();
                    ctype.a = 5;
                    ctype.b = "Hello World";
                    ctype.c = 3.1;

                    ReliableHashMap<String, Long> map = stateManager
                            .<String, Long> getOrAddReliableHashMapAsync("myHashMap").get();
                    System.out.println("After Get Or Add map1");

                    while (true) {
                        // cancellationToken.throwIfCancellationRequested();
				            Transaction tx = stateManager.createTransaction();
				            CompletableFuture<Long> res = new CompletableFuture<>();
				                map.getAsync(tx, "counter").thenApply( cv-> {
				                if (cv.hasValue()){
				                    long value = cv.getValue();
				                    //logger.log(Level.INFO, "The counter has value : {0}" + value);
				                    map.replaceAsync(tx, "counter", ++value).whenComplete((result, ex) -> {
				                    	//logger.log(Level.INFO, "Incremented counter value");
				                    });
				                    System.out.println("counter : " + value);
				                } else {
				                    map.putAsync(tx, "counter", 1L).whenComplete((result, ex) -> {
				                    	//logger.log(Level.INFO, "Initialized counter value");
				                    	
				                    });
				                }
				                
				                try {
                            // are
                            // discarded, and nothing is saved to the secondary
                            // replicas.
									tx.commitAsync().get();
									tx.close();
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
				                
				                
				                return res;
				            });

				            Thread.sleep(10);
				        }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        server.createContext(this.baseAddress + "/Add", new HttpHandler() {
            @Override
            public void handle(HttpExchange t) {

                try {

                    if (map == null) {
                        try {
                            map = stateManager.<String, String> getOrAddReliableHashMapAsync("ReliableHashMap1").get();
                        } catch (InterruptedException | ExecutionException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    String kv = t.getRequestURI().getQuery();
                    t.sendResponseHeaders(200, 0);
                    String token[] = kv.split(":");
                    OutputStream os = t.getResponseBody();
                    Transaction tx = stateManager.createTransaction();
                    map.putAsync(tx, token[0], token[1]).get();
                    tx.commitAsync().get();
                    tx.close();
                    os.write(("Written [Key, Value]:[" + token[0] + ", " + token[1] + "]").getBytes("UTF-8"));
                    os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        server.createContext(this.baseAddress + "/Enumerate", new HttpHandler() {
            @Override
            public void handle(HttpExchange t) {
                try {

                    try {
                        map = stateManager.<String, String> getOrAddReliableHashMapAsync(
                                "ReliableHashMap" + new Random(10000).nextInt()).get();
                    } catch (InterruptedException | ExecutionException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    t.sendResponseHeaders(200, 0);
                    OutputStream os = t.getResponseBody();
                    Transaction tx = stateManager.createTransaction();
                    map.putAsync(tx, "k1", "v1").get();
                    map.putAsync(tx, "k2", "v2").get();
                    map.putAsync(tx, "k3", "v3").get();
                    map.putAsync(tx, "k4", "v4").get();
                    System.out.println(" Added Key value pairs");
                    tx.commitAsync().get();
                    tx.close();
                    Transaction tx2 = stateManager.createTransaction();
                    System.out.println(" Before Keys Enumeration");
                    AsyncEnumeration<String> keys = map.keysAsync(tx2).get();
                    while (keys.hasMoreElementsAsync().get()) {
                        System.out.println(keys.nextElementAsync().get());
                    }

                    System.out.println(" Before values Enumeration");
                    AsyncEnumeration<String> values = map.elementsAsync(tx2).get();
                    while (values.hasMoreElementsAsync().get()) {
                        System.out.println(values.nextElementAsync().get());
                    }

                    System.out.println(" Before Key value Enumeration");
                    AsyncEnumeration<KeyValuePair<String, String>> kv = map.keyValuesAsync(tx2).get();
                    while (kv.hasMoreElementsAsync().get()) {
                        KeyValuePair<String, String> k = kv.nextElementAsync().get();
                        System.out.println(k.getKey() + " :: " + k.getValue());
                    }
                    tx2.commitAsync().get();
                    tx2.close();
                    os.write(("Success").getBytes("UTF-8"));
                    os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        server.createContext(this.baseAddress + "/UncommitedGetTest", new HttpHandler() {
            @Override
            public void handle(HttpExchange t) {

                try {

                    if (map == null) {
                        try {
                            map = stateManager.<String, String> getOrAddReliableHashMapAsync("ReliableHashMap2").get();
                        } catch (InterruptedException | ExecutionException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    String kv = t.getRequestURI().getQuery();
                    t.sendResponseHeaders(200, 0);
                    String token[] = kv.split(":");
                    OutputStream os = t.getResponseBody();
                    Transaction tx = stateManager.createTransaction();
                    map.putAsync(tx, token[0], token[1]).get();
                    // yet to commit
                    Transaction tx1 = stateManager.createTransaction();
                    ConditionalValue<String> res = map.getAsync(tx1, token[0]).get();
                    String str = ("Get for uncommited value [Key, Value]:[" + token[0] + ", " + res.getValue() + "]");
                    tx1.commitAsync().get();
                    tx.commitAsync().get();
                    tx.close();
                    os.write((str + ". Written [Key, Value]:[" + token[0] + ", " + token[1] + "]").getBytes("UTF-8"));
                    os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        server.createContext(this.baseAddress + "/Update", new HttpHandler() {
            @Override
            public void handle(HttpExchange t) {
                try {

                    if (map == null) {
                        try {
                            map = stateManager.<String, String> getOrAddReliableHashMapAsync("ReliableHashMap1").get();
                        } catch (InterruptedException | ExecutionException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    String kv = t.getRequestURI().getQuery();
                    t.sendResponseHeaders(200, 0);
                    String token[] = kv.split(":");
                    OutputStream os = t.getResponseBody();
                    Transaction tx = stateManager.createTransaction();
                    map.replaceAsync(tx, token[0], token[1]).get();
                    tx.commitAsync().get();
                    tx.close();
                    os.write(("Written [Key, Value]:[" + token[0] + ", " + token[1] + "]").getBytes("UTF-8"));
                    os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        server.createContext(this.baseAddress + "/Get", new HttpHandler() {
            @Override
            public void handle(HttpExchange t) {
                try {
                    if (map == null) {
                        try {
                            map = stateManager.<String, String> getOrAddReliableHashMapAsync("ReliableHashMap1").get();
                        } catch (InterruptedException | ExecutionException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    URI r = t.getRequestURI();
                    String word = t.getRequestURI().getQuery();

                    t.sendResponseHeaders(200, 0);
                    OutputStream os = t.getResponseBody();
                    Transaction tx = stateManager.createTransaction();
                    ConditionalValue<String> cv = map.getAsync(tx, word).get();
                    boolean found = cv.hasValue();
                    String value = cv.getValue();

                    tx.commitAsync();
                    tx.close();
                    os.write((word + (found == false ? " not found." : " found with value \" " + value))
                            .getBytes("UTF-8"));
                    os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        server.createContext(this.baseAddress + "/Compute", new HttpHandler() {
            @Override
            public void handle(HttpExchange t) {
                try {
                    if (map == null) {
                        try {
                            map = stateManager.<String, String> getOrAddReliableHashMapAsync("ReliableHashMap1").get();
                        } catch (InterruptedException | ExecutionException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    URI r = t.getRequestURI();

                    String key = t.getRequestURI().getQuery();
                    t.sendResponseHeaders(200, 0);
                    OutputStream os = t.getResponseBody();
                    Transaction tx = stateManager.createTransaction();
                    // CompletableFuture<String> cf = new
                    // CompletableFuture<String>();
                    // CompletableFuture<String> cf =
                    // (CompletableFuture<String>) map.computeAsync(tx, key, (k,
                    // v)-> v+v, Duration.ofSeconds(60), null);
                    // String str = cf.get();
                    String str = null;
                    try {
                        str = map.computeAsync(tx, key, (k, v) -> k + k).get();
                    } catch (InterruptedException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    } catch (ExecutionException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }

                    tx.commitAsync().get();
                    tx.close();

                    os.write((key + ":" + str).getBytes("UTF-8"));
                    // os.write((key + " : " ).getBytes("UTF-8"));
                    os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        server.createContext(this.baseAddress + "/ComputeIfPresent", new HttpHandler() {
            @Override
            public void handle(HttpExchange t) {
                try {
                    if (map == null) {
                        try {
                            map = stateManager.<String, String> getOrAddReliableHashMapAsync("ReliableHashMap1").get();
                        } catch (InterruptedException | ExecutionException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    URI r = t.getRequestURI();

                    String key = t.getRequestURI().getQuery();
                    t.sendResponseHeaders(200, 0);
                    OutputStream os = t.getResponseBody();

                    Transaction tx = stateManager.createTransaction();
                    // CompletableFuture<String> cf = new
                    // CompletableFuture<String>();
                    CompletableFuture<String> cf = map.computeIfPresent(tx, key, (k, v) -> v + v);

                    String str = cf.get();
                    // String str = map.computeAsync(tx, key, (k, v)-> v+v,
                    // Duration.ofSeconds(60), null).get();

                    tx.commitAsync().get();
                    tx.close();

                    os.write((key + ":" + str).getBytes("UTF-8"));
                    os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        server.createContext(this.baseAddress + "/ComputeIfAbsent", new HttpHandler() {
            @Override
            public void handle(HttpExchange t) {
                try {
                    if (map == null) {
                        try {
                            map = stateManager.<String, String> getOrAddReliableHashMapAsync("ReliableHashMap1").get();
                        } catch (InterruptedException | ExecutionException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    URI r = t.getRequestURI();

                    String key = t.getRequestURI().getQuery();
                    t.sendResponseHeaders(200, 0);
                    OutputStream os = t.getResponseBody();

                    Transaction tx = stateManager.createTransaction();
                    // CompletableFuture<String> cf = new
                    // CompletableFuture<String>();
                    CompletableFuture<String> cf = map.computeIfAbsent(tx, key, k -> "computed " + k);

                    String str = cf.get();
                    // String str = map.computeAsync(tx, key, (k, v)-> v+v,
                    // Duration.ofSeconds(60), null).get();

                    tx.commitAsync().get();
                    tx.close();

                    os.write((key + ":" + str).getBytes("UTF-8"));
                    os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        server.createContext(this.baseAddress + "/ContainsKey", new HttpHandler() {
            @Override
            public void handle(HttpExchange t) {
                try {

                    if (map == null) {
                        try {
                            map = stateManager.<String, String> getOrAddReliableHashMapAsync("ReliableHashMap1").get();
                        } catch (InterruptedException | ExecutionException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    String key = t.getRequestURI().getQuery();
                    t.sendResponseHeaders(200, 0);

                    OutputStream os = t.getResponseBody();
                    Transaction tx = stateManager.createTransaction();

                    Boolean keyExists = map.containsKeyAsync(tx, key).get();
                    tx.commitAsync().get();
                    tx.close();
                    if (keyExists)
                        os.write(("Key Exists : " + key).getBytes("UTF-8"));
                    else
                        os.write(("Key not found : " + key).getBytes("UTF-8"));
                    os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        server.createContext(this.baseAddress + "/Remove", new HttpHandler() {
            @Override
            public void handle(HttpExchange t) {
                try {

                    if (map == null) {
                        try {
                            map = stateManager.<String, String> getOrAddReliableHashMapAsync("ReliableHashMap1").get();
                        } catch (InterruptedException | ExecutionException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    String key = t.getRequestURI().getQuery();
                    t.sendResponseHeaders(200, 0);

                    OutputStream os = t.getResponseBody();
                    Transaction tx = stateManager.createTransaction();

                    Boolean removed = map.removeAsync(tx, key).get();
                    tx.commitAsync().get();
                    tx.close();
                    if (removed)
                        os.write(("Removed Key " + key).getBytes("UTF-8"));
                    os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        server.createContext(this.baseAddress + "/PutIfAbsent", new HttpHandler() {
            @Override
            public void handle(HttpExchange t) {

                try {

                    if (map == null) {
                        try {
                            map = stateManager.<String, String> getOrAddReliableHashMapAsync("ReliableHashMap1").get();
                        } catch (InterruptedException | ExecutionException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    String kv = t.getRequestURI().getQuery();
                    t.sendResponseHeaders(200, 0);
                    String token[] = kv.split(":");
                    OutputStream os = t.getResponseBody();
                    Transaction tx = stateManager.createTransaction();
                    map.putIfAbsentAsync(tx, token[0], token[1]).get();
                    tx.commitAsync().get();
                    tx.close();
                    os.write(("Written [Key, Value]:[" + token[0] + ", " + token[1] + "]").getBytes("UTF-8"));
                    os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        server.createContext(this.baseAddress + "/Size", new HttpHandler() {
            @Override
            public void handle(HttpExchange t) {
                try {
                    if (map == null) {
                        try {
                            map = stateManager.<String, String> getOrAddReliableHashMapAsync("ReliableHashMap1").get();
                        } catch (InterruptedException | ExecutionException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    URI r = t.getRequestURI();

                    t.sendResponseHeaders(200, 0);
                    OutputStream os = t.getResponseBody();
                    long size = map.size();

                    os.write(("size of the map " + size).getBytes("UTF-8"));
                    os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        server.createContext(this.baseAddress + "/PerfBBAdd", new HttpHandler() {
            @Override
            public void handle(HttpExchange t) {
                try {

                    if (map == null) {
                        try {
                            map = stateManager.<String, String> getOrAddReliableHashMapAsync("ReliableHashMap1").get();
                        } catch (InterruptedException | ExecutionException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    int itrCount = Integer.parseInt(t.getRequestURI().getQuery());
                    t.sendResponseHeaders(200, 0);
                    OutputStream os = t.getResponseBody();
                    Transaction tx = stateManager.createTransaction();
                    CompletableFuture[] futures = new CompletableFuture[itrCount];
                    long startTime = System.nanoTime();
                    for (int i = 0; i < itrCount; i++) {
                        futures[i] = map.putAsync(tx, "BBKey" + i, "BBValue" + i);
                    }
                    CompletableFuture.allOf(futures).thenRun(() -> {
                        System.out.println("_______Taken taken to execute is " + (System.nanoTime() - startTime));
                    });
                    tx.commitAsync().get();
                    tx.close();
                    os.write(("Written " + itrCount + " Key value pairs").getBytes("UTF-8"));
                    os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        server.createContext(this.baseAddress + "/PerfBBGet", new HttpHandler() {
            @Override
            public void handle(HttpExchange t) {
                try {

                    if (map == null) {
                        try {
                            map = stateManager.<String, String> getOrAddReliableHashMapAsync("ReliableHashMap1").get();
                        } catch (InterruptedException | ExecutionException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    int itrCount = Integer.parseInt(t.getRequestURI().getQuery());
                    t.sendResponseHeaders(200, 0);
                    OutputStream os = t.getResponseBody();
                    Transaction tx = stateManager.createTransaction();
                    @SuppressWarnings({ "unchecked" })
					CompletableFuture<ConditionalValue<String>>[] futures = new CompletableFuture[itrCount];
                    long startTime = System.nanoTime();
                    for (int i = 0; i < itrCount; i++) {
                        futures[i] = map.getAsync(tx, "BBKey" + i);
                    }
                    CompletableFuture.allOf(futures).thenRun(() -> {
                        System.out.println("___________ Taken taken to execute is " + (System.nanoTime() - startTime));
                    });
                    tx.commitAsync().get();
                    tx.close();
                    for (int i = 0; i < itrCount; i++) {
                        System.out.println("Key : " + futures[i].get().getValue());
                    }
                    os.write(("Written " + itrCount + " Key value pairs").getBytes("UTF-8"));
                    os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        server.createContext(this.baseAddress + "/PerfBAAdd", new HttpHandler() {
            @Override
            public void handle(HttpExchange t) {
                try {

                    if (map == null) {
                        try {
                            map = stateManager.<String, String> getOrAddReliableHashMapAsync("ReliableHashMap1").get();
                        } catch (InterruptedException | ExecutionException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    map.useByteBuffers(false);
                    int itrCount = Integer.parseInt(t.getRequestURI().getQuery());
                    t.sendResponseHeaders(200, 0);
                    OutputStream os = t.getResponseBody();
                    Transaction tx = stateManager.createTransaction();
                    CompletableFuture[] futures = new CompletableFuture[itrCount];
                    long startTime = System.nanoTime();
                    for (int i = 0; i < itrCount; i++) {
                        futures[i] = map.putAsync(tx, "BAKey" + i, "BAValue" + i);
                    }
                    CompletableFuture.allOf(futures).thenRun(() -> {
                        System.out.println("________ Taken taken to execute is " + (System.nanoTime() - startTime));
                    });
                    tx.commitAsync().get();
                    tx.close();
                    os.write(("Written " + itrCount + " Key value pairs").getBytes("UTF-8"));
                    os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        server.createContext(this.baseAddress + "/PerfBAGet", new HttpHandler() {
            @Override
            public void handle(HttpExchange t) {
                try {

                    if (map == null) {
                        try {
                            map = stateManager.<String, String> getOrAddReliableHashMapAsync("ReliableHashMap1").get();
                        } catch (InterruptedException | ExecutionException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    map.useByteBuffers(false);
                    int itrCount = Integer.parseInt(t.getRequestURI().getQuery());
                    t.sendResponseHeaders(200, 0);
                    OutputStream os = t.getResponseBody();
                    Transaction tx = stateManager.createTransaction();
                    @SuppressWarnings("unchecked")
					CompletableFuture<ConditionalValue<String>>[] futures = new CompletableFuture[itrCount];
                    long startTime = System.nanoTime();
                    for (int i = 0; i < itrCount; i++) {
                        futures[i] = map.getAsync(tx, "BAKey" + i);
                    }
                    CompletableFuture.allOf(futures).thenRun(() -> {
                        System.out
                                .println("_____________ Taken taken to execute is " + (System.nanoTime() - startTime));
                    });
                    tx.commitAsync().get();
                    tx.close();
                    for (int i = 0; i < itrCount; i++) {
                        System.out.println("Key : " + futures[i].get().getValue());
                    }
                    os.write(("Written " + itrCount + " Key value pairs").getBytes("UTF-8"));
                    os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        server.createContext(this.baseAddress + "/Perf", new HttpHandler() {
            @Override
            public void handle(HttpExchange t) {
                String msg = "";
                OutputStream os = null;
                try {
                    t.sendResponseHeaders(200, 0);
                    os = t.getResponseBody();
                    int itrCount = 10;
                    while (itrCount <= 10000) {

                        ReliableHashMap<String, String> baMap = null;
                        ReliableHashMap<String, String> bbMap = null;
                        if (map == null) {
                            try {
                                baMap = stateManager.<String, String> getOrAddReliableHashMapAsync(
                                        "RMap1" + UUID.randomUUID().toString()).get();
                                bbMap = stateManager.<String, String> getOrAddReliableHashMapAsync(
                                        "RMap2" + UUID.randomUUID().toString()).get();
                            } catch (InterruptedException | ExecutionException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                        baMap.useByteBuffers(false);
                        // int itrCount =
                        // Integer.parseInt(t.getRequestURI().getQuery());

                        // Add Key Value pairs to using Byte array logic
                        Transaction tx = stateManager.createTransaction();
                        CompletableFuture[] futures = new CompletableFuture[itrCount];
                        long startTime = System.nanoTime();
                        for (int i = 0; i < itrCount; i++) {
                            futures[i] = baMap.putAsync(tx, "BAKey" + i, "BAValue" + i);
                        }
                        CompletableFuture.allOf(futures).get();
                        tx.commitAsync().get();
                        tx.close();
                        long baAddTime = System.nanoTime() - startTime;

                        // Wait for 1 min probably for replication to complete
                        Thread.sleep(60000);

                        // Add Key Value pairs to using Byte Buffer logic
                        tx = stateManager.createTransaction();
                        futures = new CompletableFuture[itrCount];
                        startTime = System.nanoTime();
                        for (int i = 0; i < itrCount; i++) {
                            futures[i] = bbMap.putAsync(tx, "BBKey" + i, "BBvalue" + i);
                        }
                        CompletableFuture.allOf(futures).get();
                        tx.commitAsync().get();
                        tx.close();
                        long bbAddTime = System.nanoTime() - startTime;
                        Thread.sleep(60000);

                        // Get Key Value pairs to using Byte array logic
                        tx = stateManager.createTransaction();
                        @SuppressWarnings("unchecked")
						CompletableFuture<ConditionalValue<String>>[] futures1 = new CompletableFuture[itrCount];
                        startTime = System.nanoTime();
                        for (int i = 0; i < itrCount; i++) {
                            futures1[i] = baMap.getAsync(tx, "BAKey" + i);
                        }
                        CompletableFuture.allOf(futures1).get();
                        tx.commitAsync().get();
                        tx.close();
                        long baGetTime = System.nanoTime() - startTime;
                        Thread.sleep(60000);

                        // Add Key Value pairs to using Byte Buffer logic
                        tx = stateManager.createTransaction();
                        futures = new CompletableFuture[itrCount];
                        startTime = System.nanoTime();
                        for (int i = 0; i < itrCount; i++) {
                            futures[i] = bbMap.getAsync(tx, "BBKey" + i);
                        }
                        CompletableFuture.allOf(futures).get();
                        tx.commitAsync().get();
                        tx.close();
                        long bbGetTime = System.nanoTime() - startTime;
                        Thread.sleep(60000);

                        System.out.println("______ Time taken to add " + itrCount
                                + " Key value pairs using Byte Array serializer :: " + baAddTime);
                        System.out.println("______ Time taken to add " + itrCount
                                + " Key value pairs using Byte Buffer serializer :: " + bbAddTime);
                        System.out.println("______ Time taken to get " + itrCount
                                + " Key value pairs using Byte Array deserializer :: " + baGetTime);
                        System.out.println("______ Time taken to get " + itrCount
                                + " Key value pairs using Byte Buffer deserializer :: " + bbGetTime);
                        System.out.println("\n \n");

                        itrCount = itrCount * 10;

                    }
                    msg = "Test Succeeded";
                } catch (Exception e) {
                    e.printStackTrace();
                    msg = "Test Failed";
                } finally {
                    try {
                        os.write((msg).getBytes("UTF-8"));
                        os.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

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

class ComplexType implements java.io.Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public int a;
    public String b;
    public double c;
}
