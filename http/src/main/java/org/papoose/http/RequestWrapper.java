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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.logging.Logger;


/**
 * @version $Revision: $ $Date: $
 */
public class RequestWrapper extends HttpServletRequestWrapper
{
    private final static String CLASS_NAME = RequestWrapper.class.getName();
    private final static Logger LOGGER = Logger.getLogger(CLASS_NAME);
    private final String alias;

    /**
     * Constructs a request object wrapping the given request.
     *
     * @throws IllegalArgumentException if the request is null
     */
    public RequestWrapper(String alias, HttpServletRequest request)
    {
        super(request);
        this.alias = alias;
    }

    @Override
    public String getPathInfo()
    {
        return super.getPathInfo().substring(alias.length());
    }

    @Override
    public String getServletPath()
    {
        return alias;
    }
}
