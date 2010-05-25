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
package org.papoose.tck.http;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.ops4j.pax.exam.CoreOptions.equinox;
import static org.ops4j.pax.exam.CoreOptions.felix;
import static org.ops4j.pax.exam.CoreOptions.knopflerfish;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.provision;
import org.ops4j.pax.exam.Inject;
import static org.ops4j.pax.exam.MavenUtils.asInProject;
import org.ops4j.pax.exam.Option;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.compendiumProfile;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import org.papoose.http.HttpServer;
import org.papoose.http.HttpServiceImpl;
import org.papoose.http.JettyHttpServer;
import org.papoose.http.ServletDispatcher;


/**
 * @version $Revision: $ $Date: $
 */
@RunWith(JUnit4TestRunner.class)
public class HttpServiceImplTest
{
    @Inject
    private BundleContext bundleContext = null;

    @Configuration
    public static Option[] configure()
    {
        return options(
                equinox(),
                felix(),
                knopflerfish(),
                // papoose(),
                compendiumProfile(),
                // vmOption("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"),
                // this is necessary to let junit runner not timeout the remote process before attaching debugger
                // setting timeout to 0 means wait as long as the remote service comes available.
                // starting with version 0.5.0 of PAX Exam this is no longer required as by default the framework tests
                // will not be triggered till the framework is not started
                // waitForFrameworkStartup()
                provision(
                        mavenBundle().groupId("javax.servlet").artifactId("com.springsource.javax.servlet").version(asInProject()),
                        mavenBundle().groupId("org.mortbay.jetty").artifactId("jetty").version(asInProject()),
                        mavenBundle().groupId("org.mortbay.jetty").artifactId("jetty-util").version(asInProject()),
                        mavenBundle().groupId("org.papoose.cmpn").artifactId("papoose-http").version(asInProject())
                )
        );
    }

    @Test
    public void testRegistrations() throws Exception
    {
        Assert.assertNotNull(bundleContext);

        ServletDispatcher dispatcher = new ServletDispatcher();
        HttpServiceImpl httpService = new HttpServiceImpl(bundleContext, dispatcher);

        httpService.start();
        bundleContext.registerService(HttpService.class.getName(), httpService, null);

        try
        {
            ServiceReference sr = bundleContext.getServiceReference(HttpService.class.getName());
            HttpService service = (HttpService) bundleContext.getService(sr);

            service.registerResources("/a/b", "/car", service.createDefaultHttpContext());
            try
            {
                service.registerResources("/a/b", "/car", service.createDefaultHttpContext());
                fail("Should not be able to register with same alias");
            }
            catch (NamespaceException ignore)
            {
            }

            service.unregister("/a/b");
        }
        finally
        {
            httpService.stop();
        }
    }

    @Test
    public void testResourceAbsolute() throws Exception
    {
        Assert.assertNotNull(bundleContext);

        Properties properties = new Properties();

        properties.setProperty(HttpServer.HTTP_PORT, "8080");

        HttpServer server = JettyHttpServer.generate(properties);

        server.start();

        HttpServiceImpl httpService = new HttpServiceImpl(bundleContext, server.getServletDispatcher());

        httpService.start();
        bundleContext.registerService(HttpService.class.getName(), httpService, null);

        try
        {
            ServiceReference sr = bundleContext.getServiceReference(HttpService.class.getName());
            HttpService service = (HttpService) bundleContext.getService(sr);

            service.registerResources("/a/b", "/org/papoose/tck", new HttpContext()
            {
                public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException
                {
                    return true;
                }

                public URL getResource(String name)
                {
                    return HttpServiceImplTest.class.getResource(name);
                }

                public String getMimeType(String name)
                {
                    return null;
                }
            });

            URL url = new URL("http://localhost:8080/a/b/http/HttpServiceImplTest.class");

            DataInputStream reader = new DataInputStream(url.openStream());

            assertEquals((byte) 0xca, reader.readByte());
            assertEquals((byte) 0xfe, reader.readByte());

            assertEquals((byte) 0xba, reader.readByte());
            assertEquals((byte) 0xbe, reader.readByte());

            service.unregister("/a/b");
        }
        finally
        {
            httpService.stop();
            server.stop();
        }
    }

    @Test
    public void testResourceRelative() throws Exception
    {
        Assert.assertNotNull(bundleContext);

        Properties properties = new Properties();

        properties.setProperty(HttpServer.HTTP_PORT, "8080");

        HttpServer server = JettyHttpServer.generate(properties);

        server.start();

        HttpServiceImpl httpService = new HttpServiceImpl(bundleContext, server.getServletDispatcher());

        httpService.start();
        bundleContext.registerService(HttpService.class.getName(), httpService, null);

        try
        {
            ServiceReference sr = bundleContext.getServiceReference(HttpService.class.getName());
            HttpService service = (HttpService) bundleContext.getService(sr);

            service.registerResources("/a/b", ".", new HttpContext()
            {
                public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException
                {
                    return true;
                }

                public URL getResource(String name)
                {
                    name = name.replaceAll("^\\./", "");
                    name = name.replaceAll("/\\./", "/");
                    return HttpServiceImplTest.class.getResource(name);
                }

                public String getMimeType(String name)
                {
                    return null;
                }
            });

            URL url = new URL("http://localhost:8080/a/b/HttpServiceImplTest.class");

            DataInputStream reader = new DataInputStream(url.openStream());

            assertEquals((byte) 0xca, reader.readByte());
            assertEquals((byte) 0xfe, reader.readByte());

            assertEquals((byte) 0xba, reader.readByte());
            assertEquals((byte) 0xbe, reader.readByte());

            service.unregister("/a/b");
        }
        finally
        {
            httpService.stop();
            server.stop();
        }
    }
}
