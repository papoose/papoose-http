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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Properties;
import java.util.logging.Logger;

import org.osgi.service.http.HttpContext;


/**
 * @version $Revision: $ $Date: $
 */
class ResourceServletWrapper extends HttpServlet
{
    private final static String CLASS_NAME = ResourceServletWrapper.class.getName();
    private final static Logger LOGGER = Logger.getLogger(CLASS_NAME);
    private final String alias;
    private final String name;
    private final HttpContext httpContext;
    private final AccessControlContext acc;
    private final Properties mime;

    ResourceServletWrapper(String alias, String name, HttpContext httpContext, AccessControlContext acc, Properties mime)
    {
        this.alias = alias;
        this.name = name;
        this.httpContext = httpContext;
        this.acc = acc;
        this.mime = mime;
    }

    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
    {
        LOGGER.entering(CLASS_NAME, "service", new Object[]{ req, resp });

        String path = req.getPathInfo();
        path = name + path;

        final URL url = httpContext.getResource(path);

        if (url == null) throw new ResourceNotFoundException();

        String contentType = httpContext.getMimeType(path);
        if (contentType == null)
        {
            int index = path.lastIndexOf(".");
            if (index > 0) contentType = mime.getProperty(path.substring(index + 1));
        }
        if (contentType != null) resp.setContentType(contentType);

        try
        {
            if (System.getSecurityManager() == null)
            {
                generateResponse(url, resp, req);
            }
            else
            {
                AccessController.doPrivileged(new PrivilegedExceptionAction<Void>()
                {
                    public Void run() throws Exception
                    {
                        generateResponse(url, resp, req);

                        return null;
                    }
                }, acc);
            }
        }
        catch (PrivilegedActionException pae)
        {
            Exception exception = pae.getException();
            if (exception instanceof ServletException) throw (ServletException) exception;
            if (exception instanceof IOException) throw (IOException) exception;

            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        catch (IOException ioe)
        {
            throw ioe;
        }
        catch (Exception e)
        {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        LOGGER.exiting(CLASS_NAME, "service");
    }

    private void generateResponse(URL url, HttpServletResponse resp, HttpServletRequest req) throws IOException
    {
        LOGGER.entering(CLASS_NAME, "generateResponse", new Object[]{ url, resp, req });

        URLConnection conn = url.openConnection();

        long lastModified = conn.getLastModified();
        if (lastModified != 0) resp.setDateHeader("Last-Modified", lastModified);

        long modifiedSince = req.getDateHeader("If-Modified-Since");
        if (lastModified == 0 || modifiedSince == -1 || lastModified > modifiedSince)
        {
            resp.setContentLength(copyResource(conn.getInputStream(), resp.getOutputStream()));
            resp.setStatus(HttpServletResponse.SC_OK);
        }
        else
        {
            resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        }

        LOGGER.exiting(CLASS_NAME, "generateResponse");
    }

    private int copyResource(InputStream in, OutputStream out) throws IOException
    {
        LOGGER.entering(CLASS_NAME, "copyResource", new Object[]{ in, out });

        byte[] buf = new byte[4096];
        int length = 0;
        int n;

        try
        {
            while ((n = in.read(buf, 0, buf.length)) != -1)
            {
                out.write(buf, 0, n);
                length += n;
            }

            LOGGER.exiting(CLASS_NAME, "copyResource", length);

            return length;
        }
        finally
        {
            in.close();
            out.close();
        }
    }

}
