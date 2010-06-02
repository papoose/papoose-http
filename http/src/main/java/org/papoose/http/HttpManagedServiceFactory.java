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
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.service.http.HttpService;


/**
 * Instances of {@link HttpService} can be created and configured using OSGi's
 * Configuration Admin Service.
 *
 * @version $Revision: 612 $ $Date: 2010-01-31 09:57:04 -0800 (Sun, 31 Jan 2010) $
 * @see ManagedServiceFactory
 * @see HttpService
 */
public class HttpManagedServiceFactory implements ManagedServiceFactory, Closeable
{
    private final static String CLASS_NAME = HttpManagedServiceFactory.class.getName();
    private final static Logger LOGGER = Logger.getLogger(CLASS_NAME);
    private final Map<String, Registration> httpServers = new HashMap<String, Registration>();
    private final BundleContext bundleContext;
    private final String name;

    /**
     * Create a <code>HttpManagedServiceFactory</code> which will be used to
     * manage and configure instances of {@link HttpService} using OSGi's
     * Configuration Admin Service.
     *
     * @param bundleContext The OSGi {@link BundleContext} used to register OSGi service instances.
     * @param name          The name for the factory.
     */
    public HttpManagedServiceFactory(BundleContext bundleContext, String name)
    {
        if (bundleContext == null) throw new IllegalArgumentException("Bundle context cannot be null");
        if (name == null) throw new IllegalArgumentException("Name cannot be null");

        this.bundleContext = bundleContext;
        this.name = name;

        if (LOGGER.isLoggable(Level.CONFIG))
        {
            LOGGER.config("bundleContext: " + bundleContext);
            LOGGER.config("name: " + name);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getName()
    {
        return name;
    }

    /**
     * Update the HTTP service's configuration, unregistering from the OSGi
     * service registry and stopping it if it had already started. The new
     * HTTP service will be started with the new configuration and
     * registered in the OSGi service registry using the configuration
     * parameters as service properties.
     *
     * @param pid        The PID for this configuration.
     * @param dictionary The dictionary used to configure the HTTP service.
     * @throws ConfigurationException Thrown if an error occurs during the HTTP service's configuration.
     */
    public void updated(String pid, Dictionary dictionary) throws ConfigurationException
    {
        LOGGER.entering(CLASS_NAME, "updated", new Object[]{ pid, dictionary });

        deleted(pid);

        if (LOGGER.isLoggable(Level.FINER)) LOGGER.finer("HTTP service " + pid + " starting...");

        HttpServer server = JettyHttpServer.generate(dictionary);

        server.start();

        HttpServiceImpl httpService = new HttpServiceImpl(bundleContext, server.getServletDispatcher());

        httpService.start();

        if (LOGGER.isLoggable(Level.FINE)) LOGGER.fine("HTTP service " + pid + " started successfully");

        ServiceRegistration registration = bundleContext.registerService(HttpService.class.getName(), httpService, dictionary);

        httpServers.put(pid, new Registration(server, httpService, registration));

        LOGGER.exiting(CLASS_NAME, "updated");
    }

    /**
     * Remove the HTTP service identified by a pid that it was associated
     * with when it was first created.
     *
     * @param pid The PID for the HTTP service to be removed.
     */
    public void deleted(String pid)
    {
        LOGGER.entering(CLASS_NAME, "deleted", pid);

        Registration registration = httpServers.remove(pid);
        if (registration != null)
        {
            registration.serviceRegistration.unregister();

            if (LOGGER.isLoggable(Level.FINER)) LOGGER.finer("HTTP service " + pid + " stopping...");

            registration.httpService.stop();
            registration.httpServer.stop();

            if (LOGGER.isLoggable(Level.FINE)) LOGGER.fine("HTTP service " + pid + " stopped successfully");
        }

        LOGGER.exiting(CLASS_NAME, "deleted");
    }

    /**
     * Close all the configured HTTP services.
     */
    public void close()
    {
        LOGGER.entering(CLASS_NAME, "close");

        for (String pid : httpServers.keySet())
        {
            deleted(pid);
        }

        LOGGER.exiting(CLASS_NAME, "close");
    }

    private final static class Registration
    {
        HttpServer httpServer;
        HttpServiceImpl httpService;
        ServiceRegistration serviceRegistration;

        private Registration(HttpServer httpServer, HttpServiceImpl httpService, ServiceRegistration serviceRegistration)
        {
            this.httpServer = httpServer;
            this.httpService = httpService;
            this.serviceRegistration = serviceRegistration;
        }
    }
}