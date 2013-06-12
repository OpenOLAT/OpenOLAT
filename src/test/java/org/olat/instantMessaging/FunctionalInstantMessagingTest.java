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
package org.olat.instantMessaging;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.test.ArquillianDeployments;
import org.olat.user.restapi.UserVO;
import org.olat.collaboration.CollaborationTools;
import org.olat.restapi.support.vo.GroupVO;
import org.olat.util.FunctionalInstantMessagingUtil;
import org.olat.util.FunctionalUtil;
import org.olat.util.FunctionalVOUtil;
import org.olat.util.browser.Student1;
import org.olat.util.browser.Student2;
import org.olat.util.browser.Student3;
import org.olat.util.browser.Tutor1;

import com.thoughtworks.selenium.Selenium;

/**
 * 
 * @author jkraehemann, joel.kraehemann@frentix.com, frentix.com
 */
@Ignore
@RunWith(Arquillian.class)
public class FunctionalInstantMessagingTest {
	
	public final static String GROUP_CHAT_TUTOR0 = "tutor0";
	public final static String GROUP_CHAT_PARTICIPANT0 = "participant0";
	public final static String GROUP_CHAT_PARTICIPANT1 = "participant1";
	public final static String GROUP_CHAT_PARTICIPANT2 = "participant2";
	public final static Map<String,String> GROUP_CHAT_DIALOG = new HashMap<String,String>();
	
	static{
		int count = 0;
		
		GROUP_CHAT_DIALOG.put(GROUP_CHAT_PARTICIPANT0 + "#" + count++, "hello world!");
		GROUP_CHAT_DIALOG.put(GROUP_CHAT_PARTICIPANT1 + "#" + count++, "clear sky.");
	}
	
	@Deployment(testable = false)
	public static WebArchive createDeployment() {
		return ArquillianDeployments.createDeployment();
	}

	@ArquillianResource
	URL deploymentUrl;

	static FunctionalUtil functionalUtil;
	static FunctionalInstantMessagingUtil functionalInstantMessagingUtil;

	static FunctionalVOUtil functionalVOUtil;
	
	static boolean initialized = false;
	
	@Before
	public void setup() throws IOException, URISyntaxException{
		if(!initialized){
			functionalUtil = new FunctionalUtil();
			functionalUtil.setDeploymentUrl(deploymentUrl.toString());
			functionalInstantMessagingUtil = new FunctionalInstantMessagingUtil(functionalUtil);
			
			functionalVOUtil = new FunctionalVOUtil(functionalUtil.getUsername(), functionalUtil.getPassword());
			
			initialized = true;
		}
	}

	@Ignore
	@Test
	@RunAsClient
	public void checkGroupChat(@Tutor1 Selenium tutor0, @Student1 Selenium student0, @Student2 Selenium student1, @Student3 Selenium student2)
			throws IOException, URISyntaxException
	{
		/*
		 * Prerequisites
		 */
		/* create users and group */
		List<UserVO> tutor = functionalVOUtil.createTestAuthors(deploymentUrl, 1);
		List<UserVO> user = functionalVOUtil.createTestUsers(deploymentUrl, 3);
		
		List<GroupVO> group = functionalVOUtil.createTestCourseGroups(deploymentUrl, 1);
		
		/* set visibility */
		functionalVOUtil.setGroupConfiguration(deploymentUrl, group.get(0),
				new String[]{CollaborationTools.TOOL_CHAT},
				false, false,
				false, false,
				false, false);
		
		/* add users to group */
		functionalVOUtil.addOwnerToGroup(deploymentUrl, group.get(0), tutor.get(0));
		
		for(int i = 0; i < 2; i++){
			functionalVOUtil.addParticipantToGroup(deploymentUrl, group.get(0), user.get(i));
		}
		
		/*
		 * Content
		 */
		/* login */
		Assert.assertTrue(functionalUtil.login(tutor0, tutor.get(0).getLogin(), tutor.get(0).getPassword(), true));
		
		Assert.assertTrue(functionalUtil.login(student0, user.get(0).getLogin(), user.get(0).getPassword(), true));
		
		/* open roaster */
		
		
		/* modify visibility settings in group */
		
		
		/* login */
		Assert.assertTrue(functionalUtil.login(student1, user.get(1).getLogin(), user.get(1).getPassword(), true));
		Assert.assertTrue(functionalUtil.login(student2, user.get(2).getLogin(), user.get(2).getPassword(), true));
		
		/* dialog */
		String[] keys = (String[]) GROUP_CHAT_DIALOG.keySet().toArray();
		
		for(int i = 0; i < GROUP_CHAT_DIALOG.size(); i++){
			String current = GROUP_CHAT_DIALOG.get(keys[i]);
			
			if(keys[i].startsWith(GROUP_CHAT_TUTOR0)){
				//nothing to be done, here.
			}else if(keys[i].startsWith(GROUP_CHAT_PARTICIPANT0)){
				functionalInstantMessagingUtil.sendMessageToGroup(student0, group.get(0).getName(), current);
			}else if(keys[i].startsWith(GROUP_CHAT_PARTICIPANT1)){
				functionalInstantMessagingUtil.sendMessageToGroup(student1, group.get(0).getName(), current);
			}else if(keys[i].startsWith(GROUP_CHAT_PARTICIPANT2)){
				//nothing to be done, here.
			}
		}
	}
}
