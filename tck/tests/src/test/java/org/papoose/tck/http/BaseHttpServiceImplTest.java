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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.ops4j.pax.exam.Inject;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import org.papoose.tck.http.servlets.AttributeInitTestServlet;
import org.papoose.tck.http.servlets.AttributePrintTestServlet;
import org.papoose.tck.http.servlets.ParameterTestServlet;
import org.papoose.tck.http.servlets.PrintTestServlet;
import org.papoose.tck.http.servlets.ServletConfigInitParameterTestServlet;
import org.papoose.tck.http.servlets.ServletContextInitParameterTestServlet;


/**
 * @version $Revision: $ $Date: $
 */
public abstract class BaseHttpServiceImplTest
{
    @Inject
    protected BundleContext bundleContext = null;

    @Test
    public void testServletRegistrations() throws Exception
    {
        assertTrue(false);

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
    public void testServletConfigInitParameters() throws Exception
    {
        ServiceReference sr = bundleContext.getServiceReference(HttpService.class.getName());
        HttpService service = (HttpService) bundleContext.getService(sr);

        Dictionary<Object, Object> init = new Properties();
        init.put("a", "1");
        init.put("b", "2");
        init.put("c", "3");

        service.registerServlet("/a/b", new ServletConfigInitParameterTestServlet(), init, null);

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
    public void testServletContextInitParameters() throws Exception
    {
        ServiceReference sr = bundleContext.getServiceReference(HttpService.class.getName());
        HttpService service = (HttpService) bundleContext.getService(sr);

        Dictionary<Object, Object> init = new Properties();
        init.put("a", "1");
        init.put("b", "2");
        init.put("c", "3");

        service.registerServlet("/a/b", new ServletContextInitParameterTestServlet(), init, null);

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
    public void testSharedHttpContext() throws Exception
    {
        ServiceReference sr = bundleContext.getServiceReference(HttpService.class.getName());
        HttpService service = (HttpService) bundleContext.getService(sr);

        HttpContext sharedContext = service.createDefaultHttpContext();
        HttpContext loneContext = service.createDefaultHttpContext();

        service.registerServlet("/a/init", new AttributeInitTestServlet(), null, sharedContext);
        service.registerServlet("/a/print", new AttributePrintTestServlet(), null, sharedContext);
        service.registerServlet("/a/lone", new AttributePrintTestServlet(), null, loneContext);

        URL url = new URL("http://localhost:8080/a/init");

        url.openStream();

        url = new URL("http://localhost:8080/a/print");

        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));

        assertEquals("#a:1#b:2#c:3#", br.readLine());

        url = new URL("http://localhost:8080/a/lone");

        br = new BufferedReader(new InputStreamReader(url.openStream()));

        assertEquals("#", br.readLine());

        service.unregister("/a/init");
        service.unregister("/a/print");
        service.unregister("/a/lone");
    }

    @Test
    public void testOverlappingAliases() throws Exception
    {
        ServiceReference sr = bundleContext.getServiceReference(HttpService.class.getName());
        HttpService service = (HttpService) bundleContext.getService(sr);

        service.registerServlet("/a", new PrintTestServlet("a"), null, null);
        service.registerServlet("/a/b", new PrintTestServlet("ab"), null, null);
        service.registerServlet("/a/b/c", new PrintTestServlet("abc"), null, null);

        URL url = new URL("http://localhost:8080/a");
        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
        assertEquals("a", br.readLine());

        url = new URL("http://localhost:8080/a/test");
        br = new BufferedReader(new InputStreamReader(url.openStream()));
        assertEquals("a", br.readLine());

        url = new URL("http://localhost:8080/a/b");
        br = new BufferedReader(new InputStreamReader(url.openStream()));
        assertEquals("ab", br.readLine());

        url = new URL("http://localhost:8080/a/b/test");
        br = new BufferedReader(new InputStreamReader(url.openStream()));
        assertEquals("ab", br.readLine());

        url = new URL("http://localhost:8080/a/b/c");
        br = new BufferedReader(new InputStreamReader(url.openStream()));
        assertEquals("abc", br.readLine());

        url = new URL("http://localhost:8080/a/b/c/test");
        br = new BufferedReader(new InputStreamReader(url.openStream()));
        assertEquals("abc", br.readLine());

        service.unregister("/a/b");

        url = new URL("http://localhost:8080/a");
        br = new BufferedReader(new InputStreamReader(url.openStream()));
        assertEquals("a", br.readLine());

        url = new URL("http://localhost:8080/a/test");
        br = new BufferedReader(new InputStreamReader(url.openStream()));
        assertEquals("a", br.readLine());

        url = new URL("http://localhost:8080/a/b");
        br = new BufferedReader(new InputStreamReader(url.openStream()));
        assertEquals("a", br.readLine());

        url = new URL("http://localhost:8080/a/b/test");
        br = new BufferedReader(new InputStreamReader(url.openStream()));
        assertEquals("a", br.readLine());

        url = new URL("http://localhost:8080/a/b/c");
        br = new BufferedReader(new InputStreamReader(url.openStream()));
        assertEquals("abc", br.readLine());

        url = new URL("http://localhost:8080/a/b/c/test");
        br = new BufferedReader(new InputStreamReader(url.openStream()));
        assertEquals("abc", br.readLine());

        try
        {
            url = new URL("http://localhost:8080/z");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if (conn.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) throw new IOException("404");
            fail("Should not have found anything");
        }
        catch (IOException e)
        {
        }

        service.unregister("/a");
        service.unregister("/a/b/c");
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
                return BaseHttpServiceImplTest.class.getResource(name);
            }

            public String getMimeType(String name)
            {
                return null;
            }
        });

        URL url = new URL("http://localhost:8080/a/b/http/BaseHttpServiceImplTest.class");

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
                return BaseHttpServiceImplTest.class.getResource(name);
            }

            public String getMimeType(String name)
            {
                return null;
            }
        });

        URL url = new URL("http://localhost:8080/a/b/BaseHttpServiceImplTest.class");

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
}