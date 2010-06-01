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
import javax.servlet.ServletException;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;


/**
 * @version $Revision: $ $Date: $
 */
class HttpServiceProxy implements HttpService
{
    private final static String CLASS_NAME = HttpServiceProxy.class.getName();
    private final static Logger LOGGER = Logger.getLogger(CLASS_NAME);
    private final Set<String> aliases = Collections.synchronizedSet(new HashSet<String>());
    private final HttpServiceImpl service;

    HttpServiceProxy(HttpServiceImpl service)
    {
        this.service = service;
    }

    public void registerServlet(String alias, Servlet servlet, Dictionary initparams, HttpContext context) throws ServletException, NamespaceException
    {
        service.registerServlet(alias, servlet, initparams, context);
        aliases.add(alias);
    }

    public void registerResources(String alias, String name, HttpContext context) throws NamespaceException
    {
        service.registerResources(alias, name, context);
        aliases.add(alias);
    }

    public void unregister(String alias)
    {
        service.unregister(alias);
        aliases.remove(alias);
    }

    public HttpContext createDefaultHttpContext()
    {
        return service.createDefaultHttpContext();
    }

    public void unregister()
    {
        for (String alias : aliases)
        {
            service.unregister(alias);
        }
        aliases.clear();
    }
}
