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

import java.io.Closeable;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.http.HttpService;


/**
 * @version $Revision: $ $Date: $
 */
public class HttpManagedService implements ManagedService, Closeable
{
    private final static String CLASS_NAME = HttpManagedService.class.getName();
    private final static Logger LOGGER = Logger.getLogger(CLASS_NAME);
    private final Object lock = new Object();
    private final BundleContext bundleContext;
    private HttpServer server;
    private HttpServiceImpl httpService;
    private ServiceRegistration registration;

    /**
     * Create a <code>HttpManagedService</code> which will be used to
     * manage and configure instances of {@link HttpService} using OSGi's
     * Configuration Admin Service.
     *
     * @param bundleContext The OSGi {@link BundleContext} used to register OSGi service instances.
     * @param startDefault  Start a default instance of {@link HttpService} if <code>true</code> or wait until the service's configuration is updated otherwise.
     */
    public HttpManagedService(BundleContext bundleContext, boolean startDefault)
    {
        if (bundleContext == null) throw new IllegalArgumentException("Bundle context cannot be null");
        this.bundleContext = bundleContext;

        if (startDefault)
        {
            Properties properties = new Properties();

            server = JettyHttpServer.generate(properties);

            server.start();

            httpService = new HttpServiceImpl(bundleContext, server.getServletDispatcher());

            httpService.start();

            registration = bundleContext.registerService(HttpService.class.getName(), httpService, null);
        }

        if (LOGGER.isLoggable(Level.CONFIG))
        {
            LOGGER.config("bundleContext: " + bundleContext);
            LOGGER.config("startDefault: " + startDefault);
        }
    }

    /**
     * Update the HTTP service's configuration, unregistering from the
     * OSGi service registry and stopping it if it had already started.  The
     * new HTTP service will be started with the new configuration and
     * registered in the OSGi service registry using the configuration
     * parameters as service properties.
     *
     * @param dictionary The dictionary used to configure the HTTP service.
     * @throws ConfigurationException Thrown if an error occurs during the HTTP service's configuration.
     */
    public void updated(Dictionary dictionary) throws ConfigurationException
    {
        LOGGER.entering(CLASS_NAME, "updated", dictionary);

        synchronized (lock)
        {
            if (httpService != null)
            {
                registration.unregister();

                if (LOGGER.isLoggable(Level.FINER)) LOGGER.finer("HTTP service " + this + " stopping...");

                httpService.stop();
                server.stop();

                if (LOGGER.isLoggable(Level.FINE)) LOGGER.fine("HTTP service " + this + " stopped successfully");
            }

            server = JettyHttpServer.generate(dictionary);

            if (LOGGER.isLoggable(Level.FINER)) LOGGER.finer("HTTP service " + this + " starting...");

            server.start();

            httpService = new HttpServiceImpl(bundleContext, server.getServletDispatcher());

            httpService.start();

            if (LOGGER.isLoggable(Level.FINE)) LOGGER.fine("HTTP service " + this + " started successfully");

            registration = bundleContext.registerService(HttpService.class.getName(), httpService, dictionary);
        }

        LOGGER.exiting(CLASS_NAME, "updated");
    }

    /**
     * Shuts down the HTTP service if one exists.
     */
    public void close() throws IOException
    {
        LOGGER.entering(CLASS_NAME, "close");

        synchronized (lock)
        {
            if (httpService != null)
            {
                registration.unregister();

                if (LOGGER.isLoggable(Level.FINER)) LOGGER.finer("HTTP service " + this + " stopping...");

                httpService.stop();
                server.stop();

                if (LOGGER.isLoggable(Level.FINE)) LOGGER.fine("HTTP service " + this + " stopped successfully");
            }
        }

        LOGGER.exiting(CLASS_NAME, "close");
    }
}
