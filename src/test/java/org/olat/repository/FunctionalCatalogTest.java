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
package org.olat.repository;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.olat.restapi.support.vo.CourseVO;
import org.olat.restapi.support.vo.RepositoryEntryVO;
import org.olat.test.ArquillianDeployments;
import org.olat.util.FunctionalCourseUtil;
import org.olat.util.FunctionalRepositorySiteUtil;
import org.olat.util.FunctionalUtil;
import org.olat.util.FunctionalVOUtil;
import org.olat.util.FunctionalRepositorySiteUtil.RepositorySiteAction;

import com.thoughtworks.selenium.DefaultSelenium;

/**
 * 
 * @author jkraehemann, joel.kraehemann@frentix.com, frentix.com
 */
public class FunctionalCatalogTest {
	public final static int COURSES = 2;
	
	public final static String[] SUBCATEGORY_PATHS = {
		"/programming",
		"/programming/c",
		"/programming/java"
	};
	public final static String[] SUBCATEGORY_DESCRIPTIONS = {
		"here you may find courses and resources related to programming",
		"about the C programming language",
		"about the Java programming language"
	};
	
	public final static String[] SUBCATECORY_PATHS_INCLUDING_RESOURCE = {
		SUBCATEGORY_PATHS[1],
		SUBCATEGORY_PATHS[2]
	};

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
			functionalCourseUtil = new FunctionalCourseUtil(functionalUtil, functionalRepositorySiteUtil);

			functionalVOUtil = new FunctionalVOUtil(functionalUtil.getUsername(), functionalUtil.getPassword());
			
			initialized = true;
		}
	}

	@Ignore
	@Test
	@RunAsClient
	public void checkCreateSubcategory() throws URISyntaxException, IOException{
		/*
		 * prerequisites for test created via REST
		 */
		/* import wiki */
		RepositoryEntryVO[] wikiVO = new RepositoryEntryVO[COURSES]; 
		
		for(int i = 0; i < COURSES; i++){
			wikiVO[i] = functionalVOUtil.importWiki(deploymentUrl);
		}
		
		/* import course */
		CourseVO[] courseVO = new CourseVO[COURSES];
		
		for(int i = 0; i < COURSES; i++){
			courseVO[i] = functionalVOUtil.importEmptyCourse(deploymentUrl);
		}

		/*
		 * create or configure content
		 */
		/* create categories */
		for(int i = 0; i < SUBCATEGORY_PATHS.length; i++){
			String currentPath = SUBCATEGORY_PATHS[i];
			String currentName = currentPath.substring(currentPath.lastIndexOf('/') + 1);
			String currentDescription = SUBCATEGORY_DESCRIPTIONS[i];
			
			Assert.assertTrue(functionalRepositorySiteUtil.createCatalogSubcategory(browser, currentPath, currentName, currentDescription));
		}
		
		/* edit course and publish thereby adding it to catalog  */
		for(int i = 0; i < COURSES; i++){
			/* open course in edit mode */	
			Assert.assertTrue(functionalRepositorySiteUtil.openCourse(browser, courseVO[i].getRepoEntryKey()));
			
			Assert.assertTrue(functionalCourseUtil.openCourseEditor(browser));
			
			/* choose wiki */
			String currentPath = SUBCATEGORY_PATHS[i];
			String currentName = currentPath.substring(currentPath.lastIndexOf('/') + 1);
			
			Assert.assertTrue(functionalCourseUtil.createWiki(browser, currentName + " wiki", "colaborative " + currentName + " wiki"));
			Assert.assertTrue(functionalCourseUtil.chooseWiki(browser, wikiVO[i].getKey()));
			
			/* publish course */
			Assert.assertTrue(functionalCourseUtil.publishEntireCourse(browser, null, SUBCATEGORY_PATHS[i]));
			
			/* close course */
			Assert.assertTrue(functionalCourseUtil.closeActiveTab(browser));
		}
		
		/*
		 * verify content
		 */
		/* open catalog */
		Assert.assertTrue(functionalUtil.openSite(browser, FunctionalUtil.OlatSite.LEARNING_RESOURCES));
		
		Assert.assertTrue(functionalRepositorySiteUtil.openActionByMenuTree(browser, RepositorySiteAction.CATALOG));
		
		/* verify resources */
		for(int i = 0; i < COURSES; i++){
			String[] selectors = functionalRepositorySiteUtil.createCatalogSelectors(SUBCATECORY_PATHS_INCLUDING_RESOURCE[i]);
			
			for(String currentSelector: selectors){
				/* click first course and retrieve business path */
				StringBuffer selectorBuffer = new StringBuffer();
				
				selectorBuffer.append("xpath=//a[contains(@class, '")
				.append(functionalRepositorySiteUtil.getCourseModuleIconCss())
				.append("')]");
				
				browser.click(selectorBuffer.toString());
				
				functionalUtil.waitForPageToLoad(browser);
				
				String businessPath0 = functionalUtil.currentBusinessPath(browser);
				
				/* open course and retrieve business path */
				functionalRepositorySiteUtil.openCourse(browser, courseVO[i].getRepoEntryKey());
				
				String businessPath1 = functionalUtil.currentBusinessPath(browser);
				
				/* assert collected business paths to be equal */
				Assert.assertEquals(businessPath1, businessPath0);
			}
		}
	}
	
}
