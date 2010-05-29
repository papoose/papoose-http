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

import javax.servlet.Servlet;

import java.util.Dictionary;

import org.osgi.service.http.HttpContext;


/**
 * @version $Revision: $ $Date: $
 */
class ServletRegistration
{
    private final String alias;
    private final Servlet servlet;
    private final ServletContextImpl context;
    private final Dictionary initParams;

    ServletRegistration(String alias, Servlet servlet, ServletContextImpl context, Dictionary initParams)
    {
        this.alias = ("/".equals(alias) ? "" : alias);
        this.servlet = servlet;
        this.context = context;
        this.initParams = initParams;
    }

    public ServletContextImpl getContext()
    {
        return context;
    }

    public HttpContext getHttpContext()
    {
        return context.getHttpContext();
    }

    public String getAlias()
    {
        return alias;
    }

    public Servlet getServlet()
    {
        return servlet;
    }

    public Dictionary getInitParams()
    {
        return initParams;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServletRegistration that = (ServletRegistration) o;

        return alias.equals(that.alias);
    }

    @Override
    public int hashCode()
    {
        return alias.hashCode();
    }

    @Override
    public String toString()
    {
        return "ServletRegistration{" +
               "alias='" + alias + '\'' +
               '}';
    }
}
