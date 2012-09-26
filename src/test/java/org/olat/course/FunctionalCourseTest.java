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
package org.olat.course;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.test.ArquillianDeployments;
import org.olat.util.FunctionalCourseUtil;
import org.olat.util.FunctionalCourseUtil.CourseEditorCourseTab;
import org.olat.util.FunctionalHtmlUtil;
import org.olat.util.FunctionalRepositorySiteUtil;
import org.olat.util.FunctionalRepositorySiteUtil.CourseWizardAccess;
import org.olat.util.FunctionalRepositorySiteUtil.CourseWizardElement;
import org.olat.util.FunctionalUtil;
import org.olat.util.FunctionalUtil.OlatSite;

import com.thoughtworks.selenium.DefaultSelenium;

/**
 * 
 * @author jkraehemann, joel.kraehemann@frentix.com, frentix.com
 */
@RunWith(Arquillian.class)
public class FunctionalCourseTest {
	
	public final static String WIZARD_COURSE_TITLE = "wizard-course";
	public final static String WIZARD_COURSE_DESCRIPTION = "course created by wizard";
	
	public final static String EDITOR_COURSE_TITLE = "editor-course";
	public final static String EDITOR_COURSE_CHANGED_TITLE = "editor-course-renamed";
	public final static String EDITOR_COURSE_DESCRIPTION = "course create within editor";
	public final static String EDITOR_COURSE_OVERVIEW_FILE = "/org/olat/course/overview.html";
	
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
	static FunctionalHtmlUtil functionalHtmlUtil;
	
	static boolean initialized = false;
	
	@Before
	public void setup() throws IOException, URISyntaxException{
		if(!initialized){
			functionalUtil = new FunctionalUtil();
			functionalUtil.setDeploymentUrl(deploymentUrl.toString());

			functionalRepositorySiteUtil = new FunctionalRepositorySiteUtil(functionalUtil);
			functionalCourseUtil = new FunctionalCourseUtil(functionalUtil, functionalRepositorySiteUtil);

			initialized = true;
		}
	}
	
	//@Ignore
	@Test
	@RunAsClient
	public void checkCreateUsingWizard(){
		/* login */
		Assert.assertTrue(functionalUtil.login(browser, functionalUtil.getUsername(), functionalUtil.getPassword(), true));
		
		/* open repository site */
		Assert.assertTrue(functionalUtil.openSite(browser, OlatSite.LEARNING_RESOURCES));
		
		/* create course using wizard */
		CourseWizardElement[] elementArray = new CourseWizardElement[]{
				CourseWizardElement.INFO_PAGE,
				CourseWizardElement.FORUM,
				CourseWizardElement.ENROLLMENT,
				CourseWizardElement.DOWNLOAD_FOLDER,
				CourseWizardElement.EMAIL};
		
		Assert.assertTrue(functionalRepositorySiteUtil.createCourseUsingWizard(browser, WIZARD_COURSE_TITLE, WIZARD_COURSE_DESCRIPTION,
				elementArray, null, true, CourseWizardAccess.USERS));
		
		/* click each node once */
		for(int i = 0; i < elementArray.length; i++){
			functionalCourseUtil.open(browser, i);
		}
	}
	
	@Test
	@RunAsClient
	public void checkCreateUsingEditor() throws FileNotFoundException, IOException{
		/* login */
		Assert.assertTrue(functionalUtil.login(browser, functionalUtil.getUsername(), functionalUtil.getPassword(), true));
		
		/* open repository site */
		Assert.assertTrue(functionalUtil.openSite(browser, OlatSite.LEARNING_RESOURCES));
		
		/* create course */
		Assert.assertTrue(functionalRepositorySiteUtil.createCourseUsingEditor(browser, EDITOR_COURSE_TITLE, EDITOR_COURSE_DESCRIPTION));
		
		/* set single page as overview */
		File overview = new File(EDITOR_COURSE_OVERVIEW_FILE);
		Assert.assertTrue(functionalCourseUtil.uploadOverviewPage(browser, overview.toURI()));
		
		/* publish entire course */
		Assert.assertTrue(functionalCourseUtil.publishEntireCourse(browser, null, null));
		
		/* rename short title */
		Assert.assertTrue(functionalCourseUtil.openCourseEditorCourseTab(browser, CourseEditorCourseTab.TITLE_AND_DESCRIPTION));
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=(//div[contains(@class, 'o_editor')]//form//input[@type='text'])[1]");
		
		browser.type(selectorBuffer.toString(), EDITOR_COURSE_CHANGED_TITLE);
		
		/* publish entire course */
		Assert.assertTrue(functionalCourseUtil.publishEntireCourse(browser, null, null));
		
		/*
		 * Test for content i.e. approve if changes were applied.
		 */
		String courseLink = null;
		
		Assert.assertNotNull(courseLink = functionalCourseUtil.readExternalLink(browser));
		Assert.assertTrue(functionalCourseUtil.closeActiveTab(browser));
		
		browser.open(courseLink);
		functionalUtil.waitForPageToLoad(browser);
		
		browser.isTextPresent(functionalHtmlUtil.stripTags(IOUtils.toString(new FileInputStream(overview))));
		
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//a[contains(@class, '")
		.append(functionalCourseUtil.getStructureIconCss())
		.append("')]")
		.append("//span[contains(text(), '")
		.append(EDITOR_COURSE_CHANGED_TITLE)
		.append("')]");
		
		Assert.assertTrue(browser.isElementPresent(selectorBuffer.toString()));
	}
}
