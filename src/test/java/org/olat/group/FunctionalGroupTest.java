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
package org.olat.group;

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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.test.ArquillianDeployments;
import org.olat.user.restapi.UserVO;
import org.olat.util.FunctionalGroupsSiteUtil;
import org.olat.util.FunctionalGroupsSiteUtil.GroupOptions;
import org.olat.util.FunctionalUtil;
import org.olat.util.FunctionalVOUtil;
import org.olat.util.browser.Student1;
import org.olat.util.browser.Tutor1;

import com.thoughtworks.selenium.DefaultSelenium;

/**
 * 
 * @author jkraehemann, joel.kraehemann@frentix.com, frentix.com
 */
@RunWith(Arquillian.class)
public class FunctionalGroupTest {
	
	public final static String CREATE_GROUP_NAME = "club of dead poets";
	public final static String CREATE_GROUP_DESCRIPTION = "If you feel disappointed by the attitude and stance on free software of certain companies you're welcome.";
	
	@Deployment(testable = false)
	public static WebArchive createDeployment() {
		return ArquillianDeployments.createDeployment();
	}

	@Drone
	DefaultSelenium browser;
	
	@ArquillianResource
	URL deploymentUrl;
	
	static FunctionalUtil functionalUtil;
	static FunctionalGroupsSiteUtil functionalGroupsSiteUtil;
	static FunctionalVOUtil functionalVOUtil;

	static boolean initialized = false;
	
	@Before
	public void setup() throws IOException, URISyntaxException{
		if(!initialized){
			functionalUtil = new FunctionalUtil();
			functionalUtil.setDeploymentUrl(deploymentUrl.toString());
			
			functionalGroupsSiteUtil = functionalUtil.getFunctionalGroupsSiteUtil();

			functionalVOUtil = new FunctionalVOUtil(functionalUtil.getUsername(), functionalUtil.getPassword());

			initialized = true;
		}
	}
	
	@Ignore
	@Test
	@RunAsClient
	public void checkCreate(@Drone @Tutor1 DefaultSelenium tutor0) throws IOException, URISyntaxException{
		/*
		 * Setup
		 */
		/* create author */
		int tutorCount = 1;
			
		final UserVO[] tutors = new UserVO[tutorCount];
		functionalVOUtil.createTestUsers(deploymentUrl, tutorCount).toArray(tutors);
		
		/*
		 * create content
		 */
		Assert.assertTrue(functionalUtil.login(tutor0, tutors[0].getLogin(), tutors[0].getPassword(), true));
		
		/* create group */
		Assert.assertTrue(functionalGroupsSiteUtil.createGroup(tutor0,
				CREATE_GROUP_NAME, CREATE_GROUP_DESCRIPTION,
				3,
				new GroupOptions[]{
					GroupOptions.WAITING_LIST
				}
		));
		
		/*
		 * verify
		 */
	}

	@Ignore
	@Test
	@RunAsClient
	public void checkConfigureTools(){
		
	}
	
	@Ignore
	@Test
	@RunAsClient
	public void checkConfigureMembers(){
		
	}

	@Ignore
	@Test
	@RunAsClient
	public void checkConfigureAccessControl(){
		
	}
	
	@Ignore
	@Test
	@RunAsClient
	public void checkBookGroup(){
		
	}

	@Ignore
	@Test
	@RunAsClient
	public void checkAddUser(){
		
	}
}
