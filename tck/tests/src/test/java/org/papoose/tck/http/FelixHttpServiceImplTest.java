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
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.ops4j.pax.exam.CoreOptions.equinox;
import static org.ops4j.pax.exam.CoreOptions.felix;
import static org.ops4j.pax.exam.CoreOptions.knopflerfish;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.provision;
import static org.ops4j.pax.exam.MavenUtils.asInProject;
import org.ops4j.pax.exam.Option;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.compendiumProfile;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.vmOption;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;


/**
 * @version $Revision: $ $Date: $
 */
@RunWith(JUnit4TestRunner.class)
public class FelixHttpServiceImplTest extends BaseHttpServiceImplTest
{
    @Configuration
    public static Option[] configure()
    {
        return options(
                equinox(),
                felix(),
                knopflerfish(),
                // papoose(),
                // vmOption("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"),
                // this is necessary to let junit runner not timeout the remote process before attaching debugger
                // setting timeout to 0 means wait as long as the remote service comes available.
                // starting with version 0.5.0 of PAX Exam this is no longer required as by default the framework tests
                // will not be triggered till the framework is not started
                // waitForFrameworkStartup()
                provision(
                        mavenBundle().groupId("org.apache.felix").artifactId("org.apache.felix.http.jetty").version(asInProject())
                )
        );
    }

    @Test
    public void testServletContextInitParameters() throws Exception
    {
        // Felix does not provide access to init parameters from the servlet context
    }

    @Test
    public void testResourceAbsolute() throws Exception
    {
        // https://issues.apache.org/jira/browse/FELIX-2386
    }

    @Test
    public void testRegisterNullServlet() throws Exception
    {
        // https://issues.apache.org/jira/browse/FELIX-2387
    }

    /**
     * Workaround for FELIX-2386
     * @throws Exception if an error occurs while testing
     */
    @Test
    public void testResourceContextSharing() throws Exception
    {
        ServiceReference sr = bundleContext.getServiceReference(HttpService.class.getName());
        HttpService service = (HttpService) bundleContext.getService(sr);

        HttpContext context = new HttpContext()
        {
            public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException
            {
                return true;
            }

            public URL getResource(String name)
            {
                return BaseHttpServiceImplTest.class.getResource("/" + name);
            }

            public String getMimeType(String name)
            {
                return null;
            }
        };

        service.registerResources("/a/b", "/org/papoose/tck", context);
        service.registerResources("/a/c", "/org/papoose/tck", context);

        URL url = new URL("http://localhost:8080/a/b/http/BaseHttpServiceImplTest.class");

        DataInputStream reader = new DataInputStream(url.openStream());

        assertEquals((byte) 0xca, reader.readByte());
        assertEquals((byte) 0xfe, reader.readByte());

        assertEquals((byte) 0xba, reader.readByte());
        assertEquals((byte) 0xbe, reader.readByte());

        url = new URL("http://localhost:8080/a/c/http/BaseHttpServiceImplTest.class");

        reader = new DataInputStream(url.openStream());

        assertEquals((byte) 0xca, reader.readByte());
        assertEquals((byte) 0xfe, reader.readByte());

        assertEquals((byte) 0xba, reader.readByte());
        assertEquals((byte) 0xbe, reader.readByte());

        service.unregister("/a/b");
        service.unregister("/a/c");

        try
        {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if (conn.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) throw new IOException("404");
            fail("Resource improperly available");
        }
        catch (IOException e)
        {
        }
    }

    @Test
    public void testBundleUnregsiter() throws Exception
    {
        // https://issues.apache.org/jira/browse/FELIX-2394
    }
}
