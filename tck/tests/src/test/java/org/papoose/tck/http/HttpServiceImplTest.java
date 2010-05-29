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

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Dictionary;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
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
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import org.papoose.http.HttpServer;
import org.papoose.http.HttpServiceImpl;
import org.papoose.http.JettyHttpServer;
import org.papoose.tck.http.servlets.InitParameterTestServlet;
import org.papoose.tck.http.servlets.ParameterTestServlet;


/**
 * @version $Revision: $ $Date: $
 */
@RunWith(JUnit4TestRunner.class)
public class HttpServiceImplTest
{
    @Inject
    private BundleContext bundleContext = null;
    private HttpServer server;
    private HttpServiceImpl httpService;
    private ServiceRegistration registration;

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
    public void testServletRegistrations() throws Exception
    {
        ServiceReference sr = bundleContext.getServiceReference(HttpService.class.getName());
        HttpService service = (HttpService) bundleContext.getService(sr);

        final AtomicBoolean hit = new AtomicBoolean(false);
        service.registerServlet("/a/b", new GenericServlet()
        {
            @Override
            public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException
            {
                hit.set(true);
            }
        }, null, null);

        URL url = new URL("http://localhost:8080/a/b/HttpServiceImplTest.class");

        url.openStream();

        assertTrue(hit.get());

        service.unregister("/a/b");

        try
        {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if (conn.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) throw new IOException("404");
            fail("Simple servlet improperly available");
        }
        catch (IOException e)
        {
        }
    }

    @Test
    public void testServletParameters() throws Exception
    {
        ServiceReference sr = bundleContext.getServiceReference(HttpService.class.getName());
        HttpService service = (HttpService) bundleContext.getService(sr);

        service.registerServlet("/a/b", new ParameterTestServlet(), null, null);

        URL url = new URL("http://localhost:8080/a/b/HttpServiceImplTest.class?a=1&b=2&c=3");

        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));

        assertEquals("#a:1#b:2#c:3#", br.readLine());

        service.unregister("/a/b");

        try
        {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if (conn.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) throw new IOException("404");
            fail("Simple servlet improperly available");
        }
        catch (IOException e)
        {
        }
    }

    @Test
    public void testServletInitParameters() throws Exception
    {
        ServiceReference sr = bundleContext.getServiceReference(HttpService.class.getName());
        HttpService service = (HttpService) bundleContext.getService(sr);

        Dictionary<Object, Object> init = new Properties();
        init.put("a", "1");
        init.put("b", "2");
        init.put("c", "3");

        service.registerServlet("/a/b", new InitParameterTestServlet(), init, null);

        URL url = new URL("http://localhost:8080/a/b/HttpServiceImplTest.class");

        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));

        assertEquals("#a:1#b:2#c:3#", br.readLine());

        service.unregister("/a/b");

        try
        {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if (conn.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) throw new IOException("404");
            fail("Simple servlet improperly available");
        }
        catch (IOException e)
        {
        }
    }

    @Test
    public void testResourceRegistrations() throws Exception
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

    @Test
    public void testResourceAbsolute() throws Exception
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

        try
        {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if (conn.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) throw new IOException("404");
            fail("Simple servlet improperly available");
        }
        catch (IOException e)
        {
        }
    }

    @Test
    public void testResourceRelative() throws Exception
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

        try
        {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if (conn.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) throw new IOException("404");
            fail("Simple servlet improperly available");
        }
        catch (IOException e)
        {
        }
    }

    @Before
    public void startup()
    {
        Properties properties = new Properties();

        properties.setProperty(HttpServer.HTTP_PORT, "8080");

        server = JettyHttpServer.generate(properties);

        server.start();

        httpService = new HttpServiceImpl(bundleContext, server.getServletDispatcher());

        httpService.start();

        registration = bundleContext.registerService(HttpService.class.getName(), httpService, null);
    }

    @After
    public void teardown()
    {
        registration.unregister();
        httpService.stop();
        server.stop();
    }
}
