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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static junit.framework.Assert.fail;
import org.junit.Test;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.service.http.HttpContext;


/**
 * @version $Revision: $ $Date: $
 */
public class ServletDispatcherTest
{
    @Test
    public void testNoRegistrants() throws Exception
    {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletDispatcher dispatcher = new ServletDispatcher();

        when(request.getPathInfo()).thenReturn("/a/b/c");

        dispatcher.service(request, response);

        verify(response, only()).sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testOneRegistrant() throws Exception
    {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletDispatcher dispatcher = new ServletDispatcher();
        Servlet servlet = mock(Servlet.class);
        HttpContext context = mock(HttpContext.class);
        ServletContextImpl sc = new ServletContextImpl(null, context, null);
        ServletRegistration registration = new ServletRegistration("/a/b", servlet, sc, null);

        doAnswer(new Answer()
        {
            public Object answer(InvocationOnMock invocation)
            {
                HttpServletResponse resp = (HttpServletResponse) invocation.getArguments()[1];
                resp.setStatus(HttpServletResponse.SC_OK);
                return null;
            }
        }).when(servlet).service(request, response);
        when(context.handleSecurity(request, response)).thenReturn(true);
        when(request.getPathInfo()).thenReturn("/a/b/c");

        dispatcher.register(registration);
        dispatcher.service(request, response);

        verify(context, only()).handleSecurity(request, response);
        verify(response, only()).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testFailedSecurityCheck() throws Exception
    {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletDispatcher dispatcher = new ServletDispatcher();
        Servlet servlet = mock(Servlet.class);
        HttpContext context = mock(HttpContext.class);
        ServletContextImpl sc = new ServletContextImpl(null, context, null);
        ServletRegistration registration = new ServletRegistration("/a/b", servlet, sc, null);

        when(context.handleSecurity(request, response)).thenAnswer(new Answer()
        {
            public Boolean answer(InvocationOnMock invocation)
            {
                HttpServletResponse resp = (HttpServletResponse) invocation.getArguments()[1];
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
        });
        when(request.getPathInfo()).thenReturn("/a/b/c");

        dispatcher.register(registration);
        dispatcher.service(request, response);

        verify(context, only()).handleSecurity(request, response);
        verify(response, only()).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void testSimplePathRegistrant() throws Exception
    {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletDispatcher dispatcher = new ServletDispatcher();
        Servlet servlet = mock(Servlet.class);
        HttpContext context = mock(HttpContext.class);
        ServletContextImpl sc = new ServletContextImpl(null, context, null);
        ServletRegistration registration = new ServletRegistration("/", servlet, sc, null);

        doAnswer(new Answer()
        {
            public Object answer(InvocationOnMock invocation)
            {
                HttpServletResponse resp = (HttpServletResponse) invocation.getArguments()[1];
                resp.setStatus(HttpServletResponse.SC_OK);
                return null;
            }
        }).when(servlet).service(request, response);
        when(context.handleSecurity(request, response)).thenReturn(true);
        when(request.getPathInfo()).thenReturn("/a/b/c");

        dispatcher.register(registration);
        dispatcher.service(request, response);

        verify(context, only()).handleSecurity(request, response);
        verify(response, only()).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testNaughtyRegistrant() throws Exception
    {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletDispatcher dispatcher = new ServletDispatcher();
        Servlet servlet = mock(Servlet.class);
        HttpContext context = mock(HttpContext.class);
        ServletContextImpl sc = new ServletContextImpl(null, context, null);
        ServletRegistration registration = new ServletRegistration("/a/b", servlet, sc, null);

        doThrow(new NullPointerException()).when(servlet).service(request, response);
        when(context.handleSecurity(request, response)).thenReturn(true);
        when(request.getPathInfo()).thenReturn("/a/b/c");

        dispatcher.register(registration);
        dispatcher.service(request, response);

        verify(context, only()).handleSecurity(request, response);
        verify(response, only()).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testStumblingRegistrant() throws Exception
    {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletDispatcher dispatcher = new ServletDispatcher();
        Servlet servlet = mock(Servlet.class);
        HttpContext context = mock(HttpContext.class);
        ServletContextImpl sc = new ServletContextImpl(null, context, null);
        ServletRegistration registration = new ServletRegistration("/a/b", servlet, sc, null);

        doThrow(new ServletException()).when(servlet).service(request, response);
        when(context.handleSecurity(request, response)).thenReturn(true);
        when(request.getPathInfo()).thenReturn("/a/b/c");

        dispatcher.register(registration);
        try
        {
            dispatcher.service(request, response);
            fail("Should have passed on the exception");
        }
        catch (ServletException ignore)
        {
        }

        verify(context, only()).handleSecurity(request, response);
        verify(response, never()).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testWrongPathRegistrant() throws Exception
    {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletDispatcher dispatcher = new ServletDispatcher();
        Servlet servlet = mock(Servlet.class);
        HttpContext context = mock(HttpContext.class);
        ServletContextImpl sc = new ServletContextImpl(null, context, null);
        ServletRegistration registration = new ServletRegistration("/cad", servlet, sc, null);

        doAnswer(new Answer()
        {
            public Object answer(InvocationOnMock invocation)
            {
                HttpServletResponse resp = (HttpServletResponse) invocation.getArguments()[1];
                resp.setStatus(HttpServletResponse.SC_OK);
                return null;
            }
        }).when(servlet).service(request, response);
        when(context.handleSecurity(request, response)).thenReturn(true);
        when(request.getPathInfo()).thenReturn("/a/b/c");

        dispatcher.register(registration);
        dispatcher.service(request, response);

        verify(context, never()).handleSecurity(request, response);
        verify(response, only()).sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testMatchingRegistrant() throws Exception
    {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletDispatcher dispatcher = new ServletDispatcher();
        Servlet servlet = mock(Servlet.class);
        HttpContext context = mock(HttpContext.class);
        ServletContextImpl sc = new ServletContextImpl(null, context, null);
        ServletRegistration registration = new ServletRegistration("/a/b", servlet, sc, null);

        doAnswer(new Answer()
        {
            public Object answer(InvocationOnMock invocation)
            {
                HttpServletResponse resp = (HttpServletResponse) invocation.getArguments()[1];
                resp.setStatus(HttpServletResponse.SC_OK);
                return null;
            }
        }).when(servlet).service(request, response);
        when(context.handleSecurity(request, response)).thenReturn(true);
        when(request.getPathInfo()).thenReturn("/a/bar");

        dispatcher.register(registration);
        dispatcher.service(request, response);

        verify(context, never()).handleSecurity(request, response);
        verify(response, only()).sendError(HttpServletResponse.SC_NOT_FOUND);
    }
}
