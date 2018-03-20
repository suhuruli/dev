// ------------------------------------------------------------
//  Copyright (c) Microsoft Corporation.  All rights reserved.
//  Licensed under the MIT License (MIT). See License.txt in the repo root for license information.
// ------------------------------------------------------------

package statelessservice;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import microsoft.servicefabric.services.communication.runtime.CommunicationListener;
import microsoft.servicefabric.services.runtime.StatelessServiceContext;
import system.fabric.CancellationToken;

public class HttpCommunicationListener implements CommunicationListener {    
    
    private StatelessServiceContext context;
    private final int port;
    
    public HttpCommunicationListener(StatelessServiceContext context, int port) {
        this.context = context;
        this.port = port;
    }
    
    public void start() {
        
        new Thread(() -> {            
            try {
	    			Tomcat tomcat = new Tomcat();
	            tomcat.setBaseDir(createTempDir());
	            tomcat.setPort(8080);
	            Context ctx = tomcat.addContext("", new File(".").getAbsolutePath());
	            Tomcat.addServlet(ctx, "Embedded", new HttpServlet() {
	                @Override
	                protected void service(HttpServletRequest req, HttpServletResponse resp) 
	                        throws ServletException, IOException {
	                    
	                    Writer w = resp.getWriter();
	                    w.write("Embedded Tomcat servlet.\n");
	                    w.flush();
	                    w.close();
	                }
	            });
	            ctx.addServletMapping("/*", "Embedded");
	            tomcat.start();
	            tomcat.getServer().await();
            } catch (Exception ex) {
            		ex.printStackTrace();
            }
        }).start();
        

    }

    @Override
    public CompletableFuture<String> openAsync(CancellationToken cancellationToken) {
        this.start();
        String publishUri = String.format("http://%s:%d/", this.context.getNodeContext().getIpAddressOrFQDN(), port);
        return CompletableFuture.completedFuture(publishUri);
    }

    @Override
    public CompletableFuture<?> closeAsync(CancellationToken cancellationToken) {
        return CompletableFuture.completedFuture(true);
    }
    
    private static String createTempDir() {
        try {
            File tempDir = File.createTempFile("tomcat.", "." + 8080);
            tempDir.delete();
            tempDir.mkdir();
            tempDir.deleteOnExit();
            return tempDir.getAbsolutePath();
        } catch (IOException ex) {
            throw new RuntimeException(
                    "Unable to create tempDir. java.io.tmpdir is set to " + System.getProperty("java.io.tmpdir"),
                    ex
            );
        }
    }

	@Override
	public void abort() {
	}
};