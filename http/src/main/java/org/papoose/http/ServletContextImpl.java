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
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
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
    private final static ThreadLocal<ServletRegistration> CURRENT_SERVLET = new ThreadLocal<ServletRegistration>();
    private final Map<String, Object> attributes = new Hashtable<String, Object>();
    private final HttpContext httpContext;
    private final ServletDispatcher servletDispatcher;
    private int referenceCount;

    ServletContextImpl(HttpContext httpContext, ServletDispatcher servletDispatcher)
    {
        this.httpContext = httpContext;
        this.servletDispatcher = servletDispatcher;
    }

    HttpContext getHttpContext()
    {
        return httpContext;
    }

    int getReferenceCount()
    {
        return referenceCount;
    }

    void incrementReferenceCount()
    {
        referenceCount--;
    }

    void decrementReferenceCount()
    {
        referenceCount++;
    }

    public String getContextPath()
    {
        return CURRENT_SERVLET.get().getAlias();
    }

    public ServletContext getContext(String uripath)
    {
        return servletDispatcher.getContext(uripath);
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
        LOGGER.entering(CLASS_NAME, "getMimeType", file);

        String mimeType = httpContext.getMimeType(file);

        if (mimeType == null) mimeType = servletDispatcher.getServletContext().getMimeType(file);

        LOGGER.exiting(CLASS_NAME, "getMimeType", mimeType);

        return mimeType;
    }

    public Set getResourcePaths(String path)
    {
        return null;
    }

    public URL getResource(String path)
    {
        return httpContext.getResource(path);
    }

    public InputStream getResourceAsStream(String path)
    {
        URL url = getResource(path);
        if (url != null)
        {
            try
            {
                return url.openStream();
            }
            catch (IOException ignore)
            {
            }
        }

        return null;
    }

    public RequestDispatcher getRequestDispatcher(String path)
    {
        return servletDispatcher.getRequestDispatcher(path);
    }

    public RequestDispatcher getNamedDispatcher(String name)
    {
        return servletDispatcher.getNamedDispatcher(name);
    }

    public Servlet getServlet(String name) throws ServletException
    {
        return null;
    }

    public Enumeration getServlets()
    {
        return Collections.enumeration(Collections.<Object>emptySet());
    }

    public Enumeration getServletNames()
    {
        return Collections.enumeration(Collections.<Object>emptySet());
    }

    public void log(String msg)
    {
        servletDispatcher.getServletContext().log(msg);
    }

    public void log(Exception exception, String msg)
    {
        servletDispatcher.getServletContext().log(exception, msg);
    }

    public void log(String message, Throwable throwable)
    {
        servletDispatcher.getServletContext().log(message, throwable);
    }

    public String getRealPath(String path)
    {
        return null;
    }

    public String getServerInfo()
    {
        return "Papoose OSGi HTTP server";
    }

    public String getInitParameter(String name)
    {
        return (String) CURRENT_SERVLET.get().getInitParams().get(name);
    }

    public Enumeration getInitParameterNames()
    {
        return CURRENT_SERVLET.get().getInitParams().keys();
    }

    public Object getAttribute(String name)
    {
        return attributes.get(name);
    }

    public Enumeration getAttributeNames()
    {
        return Collections.enumeration(attributes.keySet());
    }

    public void setAttribute(String name, Object object)
    {
        attributes.put(name, object);
    }

    public void removeAttribute(String name)
    {
        attributes.remove(name);
    }

    public String getServletContextName()
    {
        return null;
    }

    static void insertCurrentServlet(ServletRegistration initParams)
    {
        CURRENT_SERVLET.set(initParams);
    }
}
