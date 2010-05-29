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
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @version $Revision: $ $Date: $
 */
public class ServletDispatcher extends HttpServlet
{
    private final static String CLASS_NAME = ServletDispatcher.class.getName();
    private final static Logger LOGGER = Logger.getLogger(CLASS_NAME);
    private final List<ServletRegistration> registrations = new CopyOnWriteArrayList<ServletRegistration>();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String path = req.getPathInfo();

        while (true)
        {
            for (ServletRegistration registration : registrations)
            {
                if (path.equals(registration.getAlias()))
                {
                    if (registration.getHttpContext().handleSecurity(req, resp))
                    {
                        ServletContextImpl.insertInitParams(registration.getInitParams());

                        try
                        {
                            registration.getServlet().service(req, resp);
                            return;
                        }
                        catch (ServletException e)
                        {
                            throw e;
                        }
                        catch (IOException e)
                        {
                            throw e;
                        }
                        catch (ResourceNotFoundException e)
                        {
                            if (LOGGER.isLoggable(Level.FINEST)) LOGGER.finest("Registration at " + registration.getAlias() + " has no resource");
                        }
                        catch (Throwable t)
                        {
                            LOGGER.log(Level.WARNING, "Problems calling ", t);
                            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                            return;
                        }
                        finally
                        {
                            ServletContextImpl.insertInitParams(null);
                        }
                    }
                    else
                    {
                        return;
                    }
                }
            }

            path = path.substring(0, Math.max(0, path.length() - 1));
            int index = path.lastIndexOf('/');
            if (index == -1) break;
            path = path.substring(0, index);
        }

        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    void register(ServletRegistration registration)
    {
        registrations.add(registration);
    }

    void unregister(ServletRegistration registration)
    {
        registrations.remove(registration);
    }

    void clear()
    {
        registrations.clear();
    }

    public ServletContext getContext(String uripath)
    {
        LOGGER.entering(CLASS_NAME, "getContext", uripath);

        while (true)
        {
            for (ServletRegistration registration : registrations)
            {
                if (uripath.equals(registration.getAlias()))
                {
                    ServletContext context = registration.getContext();

                    LOGGER.exiting(CLASS_NAME, "getContext", context);

                    return context;
                }
            }

            uripath = uripath.substring(0, Math.max(0, uripath.length() - 1));
            int index = uripath.lastIndexOf('/');
            if (index == -1) break;
            uripath = uripath.substring(0, index);
        }

        LOGGER.exiting(CLASS_NAME, "getContext", null);

        return null;
    }

    RequestDispatcher getRequestDispatcher(String path)
    {
        LOGGER.entering(CLASS_NAME, "getRequestDispatcher", path);

        while (true)
        {
            for (final ServletRegistration registration : registrations)
            {
                if (path.equals(registration.getAlias()))
                {
                    RequestDispatcher dispatcher = new RequestDispatcher()
                    {
                        public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException
                        {
                            HttpServletRequest req = (HttpServletRequest) request;
                            HttpServletResponse resp = (HttpServletResponse) response;

                            if (registration.getHttpContext().handleSecurity(req, resp))
                            {
                                ServletContextImpl.insertInitParams(registration.getInitParams());

                                try
                                {
                                    registration.getServlet().service(req, resp);
                                }
                                catch (ServletException e)
                                {
                                    throw e;
                                }
                                catch (IOException e)
                                {
                                    throw e;
                                }
                                catch (ResourceNotFoundException e)
                                {
                                    if (LOGGER.isLoggable(Level.FINEST)) LOGGER.finest("Registration at " + registration.getAlias() + " has no resource");
                                }
                                catch (Throwable t)
                                {
                                    LOGGER.log(Level.WARNING, "Problems calling ", t);
                                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                                }
                                finally
                                {
                                    ServletContextImpl.insertInitParams(null);
                                }
                            }
                        }

                        public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException
                        {
                            HttpServletRequest req = (HttpServletRequest) request;
                            HttpServletResponse resp = (HttpServletResponse) response;

                            if (registration.getHttpContext().handleSecurity(req, resp))
                            {
                                ServletContextImpl.insertInitParams(registration.getInitParams());

                                try
                                {
                                    registration.getServlet().service(req, resp);
                                }
                                catch (ServletException e)
                                {
                                    throw e;
                                }
                                catch (IOException e)
                                {
                                    throw e;
                                }
                                catch (ResourceNotFoundException e)
                                {
                                    if (LOGGER.isLoggable(Level.FINEST)) LOGGER.finest("Registration at " + registration.getAlias() + " has no resource");
                                }
                                catch (Throwable t)
                                {
                                    LOGGER.log(Level.WARNING, "Problems calling ", t);
                                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                                }
                                finally
                                {
                                    ServletContextImpl.insertInitParams(null);
                                }
                            }
                        }

                        @Override
                        public String toString()
                        {
                            return "RequestDispatcher{" +
                                   "r=" + registration +
                                   '}';
                        }
                    };

                    LOGGER.exiting(CLASS_NAME, "getNamedDispatcher", dispatcher);

                    return dispatcher;
                }
            }

            path = path.substring(0, Math.max(0, path.length() - 1));
            int index = path.lastIndexOf('/');
            if (index == -1) break;
            path = path.substring(0, index);
        }

        LOGGER.exiting(CLASS_NAME, "getNamedDispatcher", null);

        return null;
    }

    RequestDispatcher getNamedDispatcher(String name)
    {
        LOGGER.entering(CLASS_NAME, "getNamedDispatcher", name);

        RequestDispatcher dispatcher = getRequestDispatcher(name);

        LOGGER.exiting(CLASS_NAME, "getNamedDispatcher", dispatcher);

        return dispatcher;
    }
}
