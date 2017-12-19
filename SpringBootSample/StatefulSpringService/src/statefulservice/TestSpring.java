package statefulservice;

import java.util.logging.Level;
import java.util.logging.Logger;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.*;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import microsoft.servicefabric.data.ReliableStateManager;
import microsoft.servicefabric.data.Transaction;
import microsoft.servicefabric.data.collections.ReliableHashMap;
import microsoft.servicefabric.data.utilities.AsyncEnumeration;
import microsoft.servicefabric.data.utilities.KeyValuePair;

@Configuration
@ComponentScan
@EnableAutoConfiguration
public class TestSpring {
    private static final Logger logger = Logger.getLogger(TestSpring.class.getName());
    private ConfigurableApplicationContext ctx; 

    public TestSpring(){
        logger.log(Level.INFO, "Booting up the Spring Application");
        ctx = SpringApplication.run(TestSpring.class, new String[]{});
        logger.log(Level.INFO, "Booted up the Spring Application");
    }

    public void closeApplication(){
        this.ctx.close(); 
    }
}

@RestController
class GreetingController {
    
    @RequestMapping("/hello/{name}")
    String hello(@PathVariable String name) {
        return "Hello, " + name + "!";
    }
}