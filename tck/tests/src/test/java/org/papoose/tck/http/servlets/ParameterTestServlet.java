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
package org.papoose.tck.http.servlets;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.SortedSet;
import java.util.TreeSet;


/**
 * @version $Revision: $ $Date: $
 */
public class ParameterTestServlet extends GenericServlet
{
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException
    {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        BufferedWriter stream = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));

        Enumeration<String> enumeration = request.getParameterNames();
        SortedSet<String> names = new TreeSet<String>();
        while (enumeration.hasMoreElements())
        {
            names.add(enumeration.nextElement());
        }

        for (String name : names)
        {
            String value = request.getParameter(name);
            stream.write("#");
            stream.write(name);
            stream.write(":");
            stream.write(value);
        }
        stream.write("#");
        stream.newLine();
        stream.close();

        response.setStatus(HttpServletResponse.SC_OK);
    }

    public String getServletInfo()
    {
        return ParameterTestServlet.class.getName();
    }
}
