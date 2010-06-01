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
package org.papoose.tck.bundles.servlet;

import javax.servlet.ServletException;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;


/**
 * @version $Revision: $ $Date: $
 */
public class Activator implements BundleActivator
{
    public final static String ALIAS = "/bundle";

    public void start(final BundleContext context) throws Exception
    {
        ServiceTracker tracker = new ServiceTracker(context, HttpService.class.getName(), new ServiceTrackerCustomizer()
        {
            public Object addingService(ServiceReference reference)
            {
                try
                {
                    HttpService service = (HttpService) context.getService(reference);

                    service.registerServlet(ALIAS, new TestServlet(), null, null);

                    return service;
                }
                catch (ServletException ignore)
                {
                }
                catch (NamespaceException ignore)
                {
                }
                return null;
            }

            public void modifiedService(ServiceReference reference, Object service)
            {
            }

            public void removedService(ServiceReference reference, Object service)
            {
            }
        });
        tracker.open(true);
    }

    public void stop(BundleContext context) throws Exception
    {
        // normally one would be symmetric and unregister service but,
        // we are testing the service's ability to catch this
    }
}
