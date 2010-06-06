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
package org.papoose.tck.bundles.servlet;

import javax.servlet.GenericServlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.concurrent.atomic.AtomicReference;

import org.papoose.test.bundles.share.Share;


/**
 * @version $Revision: $ $Date: $
 */
public class TestServlet extends GenericServlet
{
    private final Share share;

    public TestServlet(Share share)
    {
        this.share = share;
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException
    {
        share.put("HIT", Boolean.TRUE);

        HttpServletResponse response = (HttpServletResponse) res;
        BufferedWriter stream = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));

        stream.write("HIT");
        stream.newLine();
        stream.close();

        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    public void init() throws ServletException
    {
        share.put("INIT", true);
    }

    @Override
    public void destroy()
    {
        share.put("DESTROY", true);
    }
}
