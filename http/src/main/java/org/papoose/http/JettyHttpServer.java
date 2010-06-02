/**
 *
 * Copyright 2010 (C) The original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.papoose.http;

import java.util.Dictionary;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.thread.QueuedThreadPool;

import org.papoose.http.util.Util;


/**
 * @version $Revision: $ $Date: $
 */
public class JettyHttpServer implements HttpServer
{
    private final static String CLASS_NAME = JettyHttpServer.class.getName();
    private final static Logger LOGGER = Logger.getLogger(CLASS_NAME);
    private final Server server = new Server();
    private final ServletDispatcher servletDispatcher = new ServletDispatcher();

    public ServletDispatcher getServletDispatcher()
    {
        return servletDispatcher;
    }

    public void start()
    {
        LOGGER.entering(CLASS_NAME, "start");

        try
        {
            server.start();
        }
        catch (Exception e)
        {
            LOGGER.log(Level.WARNING, "Error starting container", e);
        }

        LOGGER.exiting(CLASS_NAME, "start");
    }

    public void stop()
    {
        LOGGER.entering(CLASS_NAME, "stop");

        try
        {
            server.stop();
        }
        catch (Exception e)
        {
            LOGGER.log(Level.WARNING, "Error stopping container", e);
        }

        LOGGER.exiting(CLASS_NAME, "stop");
    }

    public static HttpServer generate(Dictionary dictionary)
    {
        LOGGER.entering(CLASS_NAME, "generate", dictionary);

        JettyHttpServer server = new JettyHttpServer(dictionary);

        LOGGER.exiting(CLASS_NAME, "generate", server);

        return server;
    }

    private JettyHttpServer(Dictionary properties)
    {
        int maxThreads = Util.parseInt(properties.get(HTTP_MAX_THREAD_SIZE), 5);

        if (LOGGER.isLoggable(Level.CONFIG)) LOGGER.config("Max threads: " + maxThreads);

        server.setThreadPool(new QueuedThreadPool(maxThreads));

        Context root = new Context(server, "/", Context.SESSIONS);

        root.addServlet(new ServletHolder(servletDispatcher), "/*");

        int port = Util.parseInt(properties.get(HTTP_PORT), -1);
        int securePort = Util.parseInt(properties.get(HTTP_PORT_SECURE), -1);

        if (port != -1)
        {
            if (LOGGER.isLoggable(Level.CONFIG)) LOGGER.config("port: " + port);

            SelectChannelConnector connector = new SelectChannelConnector();

            connector.setPort(port);

            server.addConnector(connector);
        }

        if (securePort != -1)
        {
            if (LOGGER.isLoggable(Level.CONFIG)) LOGGER.config("securePort: " + securePort);

            SslSocketConnector sslConnector = new SslSocketConnector();

            if (properties.get(HTTP_KEYSTORE) != null) sslConnector.setKeystore((String) properties.get(HTTP_KEYSTORE));
            if (properties.get(HTTP_TRUSTSTORE) != null) sslConnector.setTruststore((String) properties.get(HTTP_TRUSTSTORE));
            if (properties.get(HTTP_KEYSTORE_PASSWORD) != null) sslConnector.setPassword((String) properties.get(HTTP_KEYSTORE_PASSWORD));
            if (properties.get(HTTP_TRUSTSTORE_PASSWORD) != null) sslConnector.setTrustPassword((String) properties.get(HTTP_TRUSTSTORE_PASSWORD));
            if (properties.get(HTTP_SERVER_PASSWORD) != null) sslConnector.setKeyPassword((String) properties.get(HTTP_SERVER_PASSWORD));

            sslConnector.setMaxIdleTime(30000);
            sslConnector.setPort(securePort);

            server.addConnector(sslConnector);
        }
    }
}
