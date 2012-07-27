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
package org.olat.portfolio;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

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
import org.olat.restapi.support.vo.RepositoryEntryVO;
import org.olat.test.ArquillianDeployments;
import org.olat.user.restapi.UserVO;
import org.olat.util.FunctionalRepositorySiteUtil;
import org.olat.util.FunctionalUtil;
import org.olat.util.FunctionalVOUtil;
import org.olat.util.FunctionalUtil.OlatSite;

import com.thoughtworks.selenium.DefaultSelenium;

/**
 * 
 * @author jkraehemann, joel.kraehemann@frentix.com, frentix.com
 */
@RunWith(Arquillian.class)
public class FunctionalArtefactTest {
	@Deployment(testable = false)
	public static WebArchive createDeployment() {
		return ArquillianDeployments.createDeployment();
	}

	@Drone
	DefaultSelenium browser;

	@ArquillianResource
	URL deploymentUrl;

	FunctionalUtil functionalUtil;
	FunctionalVOUtil functionalVOUtil;
	FunctionalRepositorySiteUtil functionalResourcesSiteUtil;
	
	UserVO user;
	CourseVO course;
	
	@Before
	public void setup() throws IOException, URISyntaxException{
		functionalUtil = new FunctionalUtil();
		functionalUtil.setDeploymentUrl(deploymentUrl.toString());

		functionalResourcesSiteUtil = new FunctionalRepositorySiteUtil(functionalUtil);
		functionalVOUtil = new FunctionalVOUtil(functionalUtil.getUsername(), functionalUtil.getPassword());
		
		/* create test user with REST */
		List<UserVO> userVO = functionalVOUtil.createTestUsers(deploymentUrl, 1);
		
		user = userVO.get(0);

		/* deploy course with REST */
		course = functionalVOUtil.importAllElementsCourse(deploymentUrl);
		
	}
	
	@Test
	@RunAsClient
	public void checkCollectForumPost(){
		/* login for test setup */
		Assert.assertTrue(functionalUtil.login(browser, user.getLogin(), user.getPassword(), true));
		
		/* open course and check if it's open */
		Assert.assertTrue(functionalResourcesSiteUtil.openCourse(browser, course.getRepoEntryKey()));
	}
	
	@Test
	@RunAsClient
	public void checkCollectWikiArticle(){
		/* login for test setup */
		Assert.assertTrue(functionalUtil.login(browser, user.getLogin(), user.getPassword(), true));
		
		/* open course and check if it's open */
		Assert.assertTrue(functionalResourcesSiteUtil.openCourse(browser, course.getRepoEntryKey()));
		
	}
	
	@Test
	@RunAsClient
	public void checkCollectBlogPost(){
		/* login for test setup */
		Assert.assertTrue(functionalUtil.login(browser, user.getLogin(), user.getPassword(), true));
		
		/* open course and check if it's open */
		Assert.assertTrue(functionalResourcesSiteUtil.openCourse(browser, course.getRepoEntryKey()));
		
	}
	
	@Test
	@RunAsClient
	public void checkAddTextArtefact(){
		/* login for test setup */
		Assert.assertTrue(functionalUtil.login(browser, user.getLogin(), user.getPassword(), true));
		
	}
	
	@Test
	@RunAsClient
	public void checkUploadFileArtefact(){
		/* login for test setup */
		Assert.assertTrue(functionalUtil.login(browser, user.getLogin(), user.getPassword(), true));
		
	}
	
	@Test
	@RunAsClient
	public void checkCreateLearningJournal(){
		/* login for test setup */
		Assert.assertTrue(functionalUtil.login(browser, user.getLogin(), user.getPassword(), true));
		
	}
}
