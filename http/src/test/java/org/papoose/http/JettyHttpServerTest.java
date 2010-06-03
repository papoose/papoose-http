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

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.junit.Test;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.thread.QueuedThreadPool;


/**
 * @version $Revision: $ $Date: $
 */
public class JettyHttpServerTest
{
    @Test
    public void test() throws Exception
    {
        Properties properties = new Properties();

        properties.setProperty(HttpServer.HTTP_PORT, "8080");

        HttpServer server = JettyHttpServer.generate(properties);

        server.start();

        Thread.sleep(1 * 1000);

        server.stop();
    }

    @Test
    public void testJetty() throws Exception
    {
        Server server = new Server();
        server.setThreadPool(new QueuedThreadPool(5));

        Context root = new Context(server, "/", Context.SESSIONS);

        root.addServlet(new ServletHolder(new GenericServlet()
        {
            @Override
            public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException
            {
                HttpServletRequest request = (HttpServletRequest) req;
                System.err.println("ContextPath: " + request.getContextPath());
                System.err.println("getPathInfo: " + request.getPathInfo());
                System.err.println("getServletPath: " + request.getServletPath());

                System.err.println("ContextPath: " + getServletConfig().getServletContext().getContextPath());
                System.err.println("getServerInfo: " + getServletConfig().getServletContext().getServerInfo());
                System.err.println("getServletContextName: " + getServletConfig().getServletContext().getServletContextName());
//                getServletConfig().getServletContext().getServletContextName()
                //Todo change body of implemented methods use File | Settings | File Templates.
            }
        }), "/a/b/*");

        SelectChannelConnector connector = new SelectChannelConnector();

        connector.setPort(8080);

        server.addConnector(connector);

        server.start();

        URL url = new URL("http://localhost:8080/a/b/c/d.xml");

        url.openConnection().getInputStream();

        server.stop();
    }
}
