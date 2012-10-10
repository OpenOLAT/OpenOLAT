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
package org.olat.course.nodes.portfolio;

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
import org.olat.util.FunctionalEPortfolioUtil;
import org.olat.util.FunctionalHomeSiteUtil;
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
public class FunctionalPortfolioTest {
	
	public final static String PORTFOLIO_TEMPLATE_SHORT_TITLE = "portfolio template";
	public final static String PORTFOLIO_TEMPLATE_LONG_TITLE = "test portfolio template";
	public final static String PORTFOLIO_TEMPLATE_DESCRIPTION = "portfolio template";
	
	public final static String PORTFOLIO_BINDER = "portfolio template";
	public final static String PORTFOLIO_PAGE1_OLD_NAME = "New page";
	public final static String PORTFOLIO_PAGE1_NEW_NAME = "page 1";
	
	@Deployment(testable = false)
	public static WebArchive createDeployment() {
		return ArquillianDeployments.createDeployment();
	}

	@Drone
	DefaultSelenium browser;

	@ArquillianResource
	URL deploymentUrl;

	static FunctionalUtil functionalUtil;
	static FunctionalHomeSiteUtil functionalHomeSiteUtil;
	static FunctionalRepositorySiteUtil functionalRepositorySiteUtil;
	static FunctionalCourseUtil functionalCourseUtil;
	static FunctionalEPortfolioUtil functionalEPortfolioUtil;
	static FunctionalVOUtil functionalVOUtil;

	static boolean initialized = false;
	
	@Before
	public void setup() throws IOException, URISyntaxException{
		if(!initialized){
			functionalUtil = new FunctionalUtil();
			functionalUtil.setDeploymentUrl(deploymentUrl.toString());

			functionalHomeSiteUtil = functionalUtil.getFunctionalHomeSiteUtil();
			functionalRepositorySiteUtil = functionalUtil.getFunctionalRepositorySiteUtil();
			functionalCourseUtil = functionalRepositorySiteUtil.getFunctionalCourseUtil();
			functionalEPortfolioUtil = new FunctionalEPortfolioUtil(functionalUtil, functionalHomeSiteUtil);
			
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
		functionalHomeSiteUtil.selectLanguage(browser, FunctionalHomeSiteUtil.ENGLISH_LANGUAGE_VALUE);
		Assert.assertTrue(functionalUtil.logout(browser));
		Assert.assertTrue(functionalUtil.login(browser, functionalUtil.getUsername(), functionalUtil.getPassword(), true));
		
		/*  */
		Assert.assertTrue(functionalRepositorySiteUtil.openCourse(browser, course.getRepoEntryKey()));
		Assert.assertTrue(functionalCourseUtil.openCourseEditor(browser));
		
		Assert.assertTrue(functionalCourseUtil.createCourseNode(browser, CourseNodeAlias.PORTFOLIO_TASK, PORTFOLIO_TEMPLATE_SHORT_TITLE, PORTFOLIO_TEMPLATE_LONG_TITLE, PORTFOLIO_TEMPLATE_DESCRIPTION, 0));
		Assert.assertTrue(functionalCourseUtil.createPortfolioTask(browser, PORTFOLIO_TEMPLATE_SHORT_TITLE, PORTFOLIO_TEMPLATE_DESCRIPTION));
		
		Assert.assertTrue(functionalCourseUtil.openPortfolioTemplateEditor(browser));
		Assert.assertTrue(functionalEPortfolioUtil.openEditor(browser));
		Assert.assertTrue(functionalEPortfolioUtil.renamePage(browser, PORTFOLIO_BINDER, PORTFOLIO_PAGE1_OLD_NAME, PORTFOLIO_PAGE1_NEW_NAME));
		Assert.assertTrue(functionalEPortfolioUtil.createPage(browser, PORTFOLIO_BINDER, null, null, null, false));
		Assert.assertTrue(functionalCourseUtil.closeTab(browser, PORTFOLIO_TEMPLATE_SHORT_TITLE));

		Assert.assertTrue(functionalRepositorySiteUtil.openCourse(browser, course.getRepoEntryKey()));
		Assert.assertTrue(functionalCourseUtil.openCourseEditor(browser));
		Assert.assertTrue(functionalCourseUtil.publishEntireCourse(browser, null, null));
		
		Assert.assertTrue(functionalCourseUtil.open(browser, course.getRepoEntryKey(), 0));
	}
}
