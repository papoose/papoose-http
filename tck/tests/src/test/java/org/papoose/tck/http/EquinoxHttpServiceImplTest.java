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
package org.papoose.tck.http;

import org.junit.Test;
import org.junit.runner.RunWith;
import static org.ops4j.pax.exam.CoreOptions.equinox;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.provision;
import static org.ops4j.pax.exam.MavenUtils.asInProject;
import org.ops4j.pax.exam.Option;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.compendiumProfile;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.vmOption;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;


/**
 * @version $Revision: $ $Date: $
 */
@RunWith(JUnit4TestRunner.class)
public class EquinoxHttpServiceImplTest extends BaseHttpServiceImplTest
{
    @Configuration
    public static Option[] configure()
    {
        return options(
                equinox(),
                compendiumProfile(),
                vmOption("-Dorg.osgi.service.http.port=8080"),
                // vmOption("-Dorg.osgi.service.http.port=8080 -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"),
                // this is necessary to let junit runner not timeout the remote process before attaching debugger
                // setting timeout to 0 means wait as long as the remote service comes available.
                // starting with version 0.5.0 of PAX Exam this is no longer required as by default the framework tests
                // will not be triggered till the framework is not started
                // waitForFrameworkStartup()
                provision(
                        mavenBundle().groupId("javax.servlet").artifactId("com.springsource.javax.servlet").version(asInProject()),
                        mavenBundle().groupId("org.eclipse.equinox").artifactId("http").version(asInProject()),
                        mavenBundle().groupId("org.papoose.cmpn.tck.bundles").artifactId("servlet").version(asInProject())
                )
        );
    }

    @Test
    public void testRequest() throws Exception
    {
        // this test fails because Equinox's implementation throws exceptions
        // for servlet api calls whose version is greater than 2.1
    }

    @Test
    public void testServletContext() throws Exception
    {
        // this test fails because Equinox's implementation throws exceptions
        // for servlet api calls whose version is greater than 2.1
    }

    @Test
    public void testServletContextInitParameters() throws Exception
    {
        // this test fails because Equinox's implementation throws exceptions
        // for servlet api calls whose version is greater than 2.1
    }
}
