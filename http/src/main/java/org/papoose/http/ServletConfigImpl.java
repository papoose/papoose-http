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
import javax.servlet.ServletContext;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Properties;


/**
 * @version $Revision: $ $Date: $
 */
class ServletConfigImpl implements ServletConfig
{
    private final String alias;
    private final ServletContextImpl servletContext;
    private final Properties initParams;

    ServletConfigImpl(String alias, ServletContextImpl servletContext, Properties initParams)
    {
        this.alias = alias;
        this.servletContext = servletContext;
        this.initParams = initParams;
    }

    public String getServletName()
    {
        return alias;
    }

    public ServletContext getServletContext()
    {
        return servletContext;
    }

    public String getInitParameter(String name)
    {
        return (String) initParams.get(name);
    }

    public Enumeration getInitParameterNames()
    {
        return initParams.keys();
    }
}
