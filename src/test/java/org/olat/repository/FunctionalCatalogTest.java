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
import org.olat.util.FunctionalCourseUtil;
import org.olat.util.FunctionalCourseUtil.CourseNodeAlias;
import org.olat.util.FunctionalRepositorySiteUtil;
import org.olat.util.FunctionalUtil;
import org.olat.util.FunctionalVOUtil;
import org.olat.util.FunctionalRepositorySiteUtil.RepositorySiteAction;

import com.thoughtworks.selenium.DefaultSelenium;

/**
 * 
 * @author jkraehemann, joel.kraehemann@frentix.com, frentix.com
 */
@RunWith(Arquillian.class)
public class FunctionalCatalogTest {
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
	
	public final static String[] SUBCATEGORY_PATHS_INCLUDING_RESOURCE = {
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
			functionalCourseUtil = functionalRepositorySiteUtil.getFunctionalCourseUtil();

			functionalVOUtil = new FunctionalVOUtil(functionalUtil.getUsername(), functionalUtil.getPassword());
			
			initialized = true;
		}
	}

	@Test
	@RunAsClient
	public void checkCreateSubcategory() throws URISyntaxException, IOException{
		int courses = SUBCATEGORY_PATHS_INCLUDING_RESOURCE.length;
		
		/*
		 * prerequisites for test created via REST
		 */
		/* import wiki */
		RepositoryEntryVO[] wikiVO = new RepositoryEntryVO[courses]; 
		
		for(int i = 0; i < courses; i++){
			wikiVO[i] = functionalVOUtil.importWiki(deploymentUrl);
		}
		
		/* import course */
		CourseVO[] courseVO = new CourseVO[courses];
		
		for(int i = 0; i < courses; i++){
			courseVO[i] = functionalVOUtil.importEmptyCourse(deploymentUrl);
		}

		/*
		 * create or configure content
		 */
		functionalUtil.login(browser);
		
		/* create categories */
		for(int i = 0; i < SUBCATEGORY_PATHS.length; i++){
			String currentPath = SUBCATEGORY_PATHS[i];
			String currentName = currentPath.substring(currentPath.lastIndexOf('/') + 1);
			String currentDescription = SUBCATEGORY_DESCRIPTIONS[i];
			
			Assert.assertTrue(functionalRepositorySiteUtil.createCatalogSubcategory(browser, currentPath.substring(0, currentPath.lastIndexOf('/')), currentName, currentDescription));
		}
		
		/* edit course and publish thereby adding it to catalog  */
		for(int i = 0; i < courses; i++){
			/* open course in edit mode */	
			Assert.assertTrue(functionalRepositorySiteUtil.openCourse(browser, courseVO[i].getRepoEntryKey()));
			
			Assert.assertTrue(functionalCourseUtil.openCourseEditor(browser));
			
			/* choose wiki */
			String currentPath = SUBCATEGORY_PATHS_INCLUDING_RESOURCE[i];
			String currentName = currentPath.substring(currentPath.lastIndexOf('/') + 1);
			
			Assert.assertTrue(functionalCourseUtil.createCourseNode(browser, CourseNodeAlias.WIKI, "wiki", currentName + " wiki", "colaborative " + currentName + " wiki", 0));
			Assert.assertTrue(functionalCourseUtil.chooseWiki(browser, wikiVO[i].getKey()));
			
			/* publish course */
			Assert.assertTrue(functionalCourseUtil.publishEntireCourse(browser, null, currentPath));
			
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
		for(int i = 0; i < courses; i++){
			
			/* click on catalog root */
			StringBuffer selectorBuffer = new StringBuffer();
			
			selectorBuffer.append("xpath=//div[contains(@class, '")
			.append(functionalRepositorySiteUtil.getCatalogNavigationCss())
			.append("')]//a");
			
			functionalUtil.idle(browser);
			
			if(browser.isElementPresent(selectorBuffer.toString())){
				browser.click(selectorBuffer.toString());
			}
			
			/* navigate tree */
			String[] selectors = functionalRepositorySiteUtil.createCatalogSelectors(SUBCATEGORY_PATHS_INCLUDING_RESOURCE[i]);
			
			for(String currentSelector: selectors){
				functionalUtil.waitForPageToLoadElement(browser, currentSelector.toString());
				browser.click(currentSelector);
			}
			
			functionalUtil.waitForPageToLoad(browser);
			
			/* click first course and retrieve business path */
			selectorBuffer = new StringBuffer();
			
			selectorBuffer.append("xpath=(//a[contains(@class, '")
			.append(functionalRepositorySiteUtil.getCourseModuleIconCss())
			.append("')])");
			
			/* create business path and try to find it */
			String businessPath0 = functionalUtil.getDeploymentUrl() + "/url/RepositoryEntry/" + courseVO[i].getRepoEntryKey();
			boolean found = false;
			
			for(int j = 0; j < browser.getXpathCount(selectorBuffer.toString().substring(6)).intValue(); j++){
				functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString() + "[" + (j + 1) + "]");
			
				browser.click(selectorBuffer.toString() + "[" + (j + 1) + "]");
			
				functionalUtil.waitForPageToLoad(browser);
			
				String businessPath1 = functionalUtil.currentBusinessPath(browser);
				functionalCourseUtil.closeActiveTab(browser);
				
				if(businessPath1.contains(businessPath0)){
					found = true;
					break;
				}
			}
			
			/* assert collected business paths to be equal */
			Assert.assertTrue(found);
		}
	}
	
}
