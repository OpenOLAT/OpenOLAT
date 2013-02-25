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
package org.olat.course.nodes.feed;

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
import org.olat.util.FunctionalRepositorySiteUtil;
import org.olat.util.FunctionalUtil;
import org.olat.util.FunctionalVOUtil;
import org.olat.util.FunctionalCourseUtil.CourseNodeAlias;

import com.thoughtworks.selenium.DefaultSelenium;

/**
 * 
 * @author jkraehemann, joel.kraehemann@frentix.com, frentix.com
 */
@RunWith(Arquillian.class)
public class FunctionalPodcastTest {
	
	public final static String PODCAST_SHORT_TITLE = "podcast";
	public final static String PODCAST_LONG_TITLE = "test podcast";
	public final static String PODCAST_DESCRIPTION = "podcast";
	public final static String PODCAST_FEED = "http://pod.drs.ch/rock_special_mpx.xml";
	
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

			functionalRepositorySiteUtil = functionalUtil.getFunctionalRepositorySiteUtil();
			functionalCourseUtil = functionalRepositorySiteUtil.getFunctionalCourseUtil();

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
		
		Assert.assertTrue(functionalCourseUtil.createCourseNode(browser, CourseNodeAlias.PODCAST, PODCAST_SHORT_TITLE, PODCAST_LONG_TITLE, PODCAST_DESCRIPTION, 0));
		Assert.assertTrue(functionalCourseUtil.createPodcast(browser, PODCAST_SHORT_TITLE, PODCAST_DESCRIPTION));
		
		Assert.assertTrue(functionalCourseUtil.publishEntireCourse(browser, null, null));
		
		Assert.assertNotNull(functionalCourseUtil.open(browser, course.getRepoEntryKey(), 0));
		Assert.assertTrue(functionalCourseUtil.importPodcastFeed(browser, PODCAST_FEED));
		
		Assert.assertNotNull(functionalCourseUtil.open(browser, course.getRepoEntryKey(), 0));
	}
}
