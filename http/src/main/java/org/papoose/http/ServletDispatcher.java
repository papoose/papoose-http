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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
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
  public void init(ServletConfig config) throws ServletException
  {
    super.init(config);    //Todo change body of overridden methods use File | Settings | File Templates.
  }

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
                    if (registration.getContext().handleSecurity(req, resp))
                    {
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
}
