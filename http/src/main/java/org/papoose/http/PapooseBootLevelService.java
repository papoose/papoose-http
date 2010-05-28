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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;

import org.papoose.core.Papoose;


/**
 * @version $Revision: $ $Date: $
 */
public class PapooseBootLevelService
{
    private final static String CLASS_NAME = PapooseBootLevelService.class.getName();
    private final static Logger LOGGER = Logger.getLogger(CLASS_NAME);
    private volatile HttpServer server;
    private volatile HttpServiceImpl httpService;

    public void start(Papoose papoose)
    {
        LOGGER.entering(CLASS_NAME, "start", papoose);

        if (papoose == null) throw new IllegalArgumentException("Papoose instance is null");

        if (server != null)
        {
            LOGGER.log(Level.WARNING, "Http service already started");
            return;
        }

        LOGGER.finest("Provisioning Jetty server");
        server = JettyHttpServer.generate(papoose.getProperties());

        server.start();

        BundleContext bundleContext = papoose.getSystemBundleContext();

        httpService = new HttpServiceImpl(bundleContext, server.getServletDispatcher());

        httpService.start();
        bundleContext.registerService(HttpService.class.getName(), httpService, null);

        LOGGER.exiting(CLASS_NAME, "start");
    }

    /**
     * Stop the boot level service.
     *
     * Boot level services are stopped when the Papoose framework is being
     * stopped. There's no point in unregistering the service since all other
     * bundles have been uninstalled.
     */
    public void stop()
    {
        LOGGER.entering(CLASS_NAME, "stop");

        if (server == null)
        {
            LOGGER.log(Level.WARNING, "Http service already stopped");
            return;
        }

        httpService.stop();
        httpService = null;
        server.stop();
        server = null;

        LOGGER.exiting(CLASS_NAME, "stop");
    }
}
