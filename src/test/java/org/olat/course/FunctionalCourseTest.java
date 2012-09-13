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
import org.olat.test.ArquillianDeployments;
import org.olat.util.FunctionalCourseUtil;
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
	
	public final static String COURSE_TITLE = "wizard-course";
	public final static String COURSE_DESCRIPTION = "course created by wizard";
	
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
		
		Assert.assertTrue(functionalRepositorySiteUtil.createCourseUsingWizard(browser, COURSE_TITLE, COURSE_DESCRIPTION,
				elementArray, null, true, CourseWizardAccess.USERS));
		
		/* click each node once */
		for(int i = 0; i < elementArray.length; i++){
			functionalCourseUtil.open(browser, i);
		}
	}
}
