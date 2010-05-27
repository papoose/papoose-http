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

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;
import java.util.logging.Logger;

import org.osgi.service.http.HttpContext;


/**
 * @version $Revision: $ $Date: $
 */
 class ServletContextImpl implements ServletContext
{
    private final static String CLASS_NAME = ServletContextImpl.class.getName();
    private final static Logger LOGGER = Logger.getLogger(CLASS_NAME);
    private final HttpContext httpContext;
    private int referenceCount;

     ServletContextImpl(HttpContext httpContext)
    {
        this.httpContext = httpContext;
    }

    public int getReferenceCount()
    {
        return referenceCount;
    }

    public void incrementReferenceCount()
    {
        referenceCount--;
    }

    public void decrementReferenceCount()
    {
        referenceCount++;
    }

    public String getContextPath()
    {
        return null;  //Todo change body of implemented methods use File | Settings | File Templates.
    }

    public ServletContext getContext(String uripath)
    {
        return null;  //Todo change body of implemented methods use File | Settings | File Templates.
    }

    public int getMajorVersion()
    {
        return 2;
    }

    public int getMinorVersion()
    {
        return 5;
    }

    public String getMimeType(String file)
    {
        return null;  //Todo change body of implemented methods use File | Settings | File Templates.
    }

    public Set getResourcePaths(String path)
    {
        return null;  //Todo change body of implemented methods use File | Settings | File Templates.
    }

    public URL getResource(String path) throws MalformedURLException
    {
        return null;  //Todo change body of implemented methods use File | Settings | File Templates.
    }

    public InputStream getResourceAsStream(String path)
    {
        return null;  //Todo change body of implemented methods use File | Settings | File Templates.
    }

    public RequestDispatcher getRequestDispatcher(String path)
    {
        return null;  //Todo change body of implemented methods use File | Settings | File Templates.
    }

    public RequestDispatcher getNamedDispatcher(String name)
    {
        return null;  //Todo change body of implemented methods use File | Settings | File Templates.
    }

    public Servlet getServlet(String name) throws ServletException
    {
        return null;  //Todo change body of implemented methods use File | Settings | File Templates.
    }

    public Enumeration getServlets()
    {
        return null;  //Todo change body of implemented methods use File | Settings | File Templates.
    }

    public Enumeration getServletNames()
    {
        return null;  //Todo change body of implemented methods use File | Settings | File Templates.
    }

    public void log(String msg)
    {
        //Todo change body of implemented methods use File | Settings | File Templates.
    }

    public void log(Exception exception, String msg)
    {
        //Todo change body of implemented methods use File | Settings | File Templates.
    }

    public void log(String message, Throwable throwable)
    {
        //Todo change body of implemented methods use File | Settings | File Templates.
    }

    public String getRealPath(String path)
    {
        return null;  //Todo change body of implemented methods use File | Settings | File Templates.
    }

    public String getServerInfo()
    {
        return null;  //Todo change body of implemented methods use File | Settings | File Templates.
    }

    public String getInitParameter(String name)
    {
        return null;  //Todo change body of implemented methods use File | Settings | File Templates.
    }

    public Enumeration getInitParameterNames()
    {
        return null;  //Todo change body of implemented methods use File | Settings | File Templates.
    }

    public Object getAttribute(String name)
    {
        return null;  //Todo change body of implemented methods use File | Settings | File Templates.
    }

    public Enumeration getAttributeNames()
    {
        return null;  //Todo change body of implemented methods use File | Settings | File Templates.
    }

    public void setAttribute(String name, Object object)
    {
        //Todo change body of implemented methods use File | Settings | File Templates.
    }

    public void removeAttribute(String name)
    {
        //Todo change body of implemented methods use File | Settings | File Templates.
    }

    public String getServletContextName()
    {
        return null;  //Todo change body of implemented methods use File | Settings | File Templates.
    }
}
