/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.iq;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.restapi.support.vo.CourseVO;
import org.olat.test.ArquillianDeployments;
import org.olat.util.FunctionalCourseUtil;
import org.olat.util.FunctionalCourseUtil.CourseNodeAlias;
import org.olat.util.FunctionalRepositorySiteUtil;
import org.olat.util.FunctionalUtil;
import org.olat.util.FunctionalVOUtil;

import com.thoughtworks.selenium.DefaultSelenium;

/**
 * 
 * @author jkraehemann, joel.kraehemann@frentix.com, frentix.com
 */
@RunWith(Arquillian.class)
public class FunctionalIQTestTest {
	
	public final static String IQ_TEST_SHORT_TITLE = "Test";
	public final static String IQ_TEST_LONG_TITLE = "Test";
	public final static String IQ_TEST_DESCRIPTION = "test";
	
	@Deployment(testable = false)
	public static WebArchive createDeployment() {
		return ArquillianDeployments.createDeployment();
	}

	@Drone
	DefaultSelenium browser;

	@ArquillianResource
	URL deploymentUrl;

	static FunctionalUtil functionalUtil;
	static FunctionalRepositorySiteUtil functionalRepositorySiteUtil;
	static FunctionalCourseUtil functionalCourseUtil;
	static FunctionalVOUtil functionalVOUtil;

	static boolean initialized = false;
	
	@Before
	public void setup() throws IOException, URISyntaxException{
		if(!initialized){
			functionalUtil = new FunctionalUtil();
			functionalUtil.setDeploymentUrl(deploymentUrl.toString());

			functionalRepositorySiteUtil = new FunctionalRepositorySiteUtil(functionalUtil);
			functionalCourseUtil = new FunctionalCourseUtil(functionalUtil, functionalRepositorySiteUtil);

			functionalVOUtil = new FunctionalVOUtil(functionalUtil.getUsername(), functionalUtil.getPassword());

			initialized = true;
		}
	}
	
	@Test
	@RunAsClient
	public void checkCreate() throws URISyntaxException, IOException{
		CourseVO course = functionalVOUtil.importEmptyCourse(deploymentUrl);
		
		/* login for test setup */
		Assert.assertTrue(functionalUtil.login(browser, functionalUtil.getUsername(), functionalUtil.getPassword(), true));
		
		/*  */
		Assert.assertTrue(functionalRepositorySiteUtil.openCourse(browser, course.getRepoEntryKey()));
		Assert.assertTrue(functionalCourseUtil.openCourseEditor(browser));
		
		Assert.assertTrue(functionalCourseUtil.createCourseNode(browser, CourseNodeAlias.IQ_TEST, IQ_TEST_SHORT_TITLE, IQ_TEST_LONG_TITLE, IQ_TEST_DESCRIPTION, 0));
		Assert.assertTrue(functionalCourseUtil.createQTITest(browser, IQ_TEST_SHORT_TITLE, IQ_TEST_DESCRIPTION));
		
		Assert.assertTrue(functionalCourseUtil.publishEntireCourse(browser, null, null));
		
		Assert.assertTrue(functionalCourseUtil.open(browser, course.getRepoEntryKey(), 0));
	}
}
