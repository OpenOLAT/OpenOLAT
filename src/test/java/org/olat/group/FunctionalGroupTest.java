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
import org.olat.collaboration.CollaborationTools;
import org.olat.restapi.support.vo.GroupVO;
import org.olat.test.ArquillianDeployments;
import org.olat.user.restapi.UserVO;
import org.olat.util.FunctionalGroupsSiteUtil;
import org.olat.util.FunctionalGroupsSiteUtil.GroupOptions;
import org.olat.util.FunctionalGroupsSiteUtil.GroupTools;
import org.olat.util.FunctionalGroupsSiteUtil.GroupsTabAction;
import org.olat.util.FunctionalGroupsSiteUtil.MembersConfiguration;
import org.olat.util.FunctionalUtil;
import org.olat.util.FunctionalUtil.WaitLimitAttribute;
import org.olat.util.FunctionalVOUtil;
import org.olat.util.browser.Student1;
import org.olat.util.browser.Student2;
import org.olat.util.browser.Student3;
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
	
	public final static String CONFIGURE_TOOLS_INFORMATION = "group for testing";
	
	public final static String CONFIGURE_ACCESS_CONTROL_DESCRIPTION = "test access code";
	public final static String CONFIGURE_ACCESS_CONTROL_ACCESS_CODE = "1234";

	public final static String INVITATION_GROUP_NAME = "only members";
	public final static String INVITATION_GROUP_DESCRIPTION = "You must be invited in order to see the group's content";
	
	@Deployment(testable = false)
	public static WebArchive createDeployment() {
		return ArquillianDeployments.createDeployment();
	}
	
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
		/* check for administration */
		Assert.assertTrue(functionalGroupsSiteUtil.openGroupsTabActionByMenuTree(tutor0, GroupsTabAction.ADMINISTRATION));
		
		/* check for waiting list */
		Assert.assertTrue(functionalGroupsSiteUtil.openGroupsTabActionByMenuTree(tutor0, GroupsTabAction.BOOKING));
		
		/* logout */
		functionalUtil.logout(tutor0);
	}

	@Ignore
	@Test
	@RunAsClient
	public void checkConfigureTools(@Drone @Tutor1 DefaultSelenium tutor0) throws IOException, URISyntaxException{
		/*
		 * test setup
		 */
		/* create group via REST */
		int tutorCount = 1;
			
		final UserVO[] tutors = new UserVO[tutorCount];
		functionalVOUtil.createTestAuthors(deploymentUrl, tutorCount).toArray(tutors);
		
		final GroupVO[] groups = new GroupVO[tutorCount];
		functionalVOUtil.createTestCourseGroups(deploymentUrl, tutorCount).toArray(groups);
		
		functionalVOUtil.addOwnerToGroup(deploymentUrl, groups[0], tutors[0]);
		
		/*
		 * test case
		 */
		Assert.assertTrue(functionalUtil.login(tutor0, tutors[0].getLogin(), tutors[0].getPassword(), true));
		
		/* open group */
		Assert.assertTrue(functionalGroupsSiteUtil.openMyGroup(tutor0, groups[0].getName()));
		
		/* apply tools */
		GroupTools[] tools = new GroupTools[]{
				GroupTools.INFORMATION,
				GroupTools.EMAIL,
				GroupTools.CALENDAR,
				GroupTools.FOLDER,
				GroupTools.FORUM,
				GroupTools.CHAT,
				GroupTools.WIKI,
				GroupTools.EPORTFOLIO
			};
		Assert.assertTrue(functionalGroupsSiteUtil.applyTools(tutor0, tools));
		
		/* apply member information */
		Assert.assertTrue(functionalGroupsSiteUtil.applyInformationForMembers(tutor0, CONFIGURE_TOOLS_INFORMATION));
		
		/*
		 * verify
		 */
		/* information page */
		Assert.assertTrue(functionalGroupsSiteUtil.openGroupsTabActionByMenuTree(tutor0, GroupsTabAction.INFORMATION));
		Assert.assertTrue(functionalUtil.waitForPageToLoadContent(tutor0, null,
				CONFIGURE_TOOLS_INFORMATION,
				WaitLimitAttribute.VERY_SAVE, null,
				true));
				
		/* tools */
		for(GroupTools currentTool: tools){
			Assert.assertTrue(functionalGroupsSiteUtil.openGroupsTabActionByMenuTree(tutor0,
					functionalGroupsSiteUtil.findGroupTabActionForTool(currentTool)));
		}
		
		/* logout */
		functionalUtil.logout(tutor0);
	}

	@Ignore
	@Test
	@RunAsClient
	public void checkConfigureMembers(@Drone @Tutor1 DefaultSelenium tutor0) throws IOException, URISyntaxException
	{
		/*
		 * test setup
		 */
		/* create users */
		int tutorCount = 1;
			
		final UserVO[] tutors = new UserVO[tutorCount];
		functionalVOUtil.createTestAuthors(deploymentUrl, tutorCount).toArray(tutors);
		
		/* create group via REST */
		final GroupVO[] groups = new GroupVO[tutorCount];
		functionalVOUtil.createTestCourseGroups(deploymentUrl, tutorCount).toArray(groups);
		
		functionalVOUtil.addOwnerToGroup(deploymentUrl, groups[0], tutors[0]);
		
		/*
		 * test case
		 */
		Assert.assertTrue(functionalUtil.login(tutor0, tutors[0].getLogin(), tutors[0].getPassword(), true));
		
		/* open group */
		Assert.assertTrue(functionalGroupsSiteUtil.openMyGroup(tutor0, groups[0].getName()));
		
		/* apply members configuration */
		MembersConfiguration[] conf = new MembersConfiguration[]{
				MembersConfiguration.CAN_SEE_PARTICIPANTS,
				MembersConfiguration.ALL_CAN_SEE_PARTICIPANTS,
				MembersConfiguration.ALL_CAN_DOWNLOAD_LIST_OF_MEMBERS
		};
		
		Assert.assertTrue(functionalGroupsSiteUtil.applyMembersConfiguration(tutor0, conf));
		
		/*
		 * verify
		 */
		/* WORKAROUND: reset site */
		//TODO:JK: remove in openolat 8.0
		Assert.assertTrue(functionalGroupsSiteUtil.openMyGroup(tutor0, groups[0].getName()));
		
		/* check group with tutor0  */
		Assert.assertTrue(functionalGroupsSiteUtil.openGroupsTabActionByMenuTree(tutor0, GroupsTabAction.MEMBERS));
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//div[contains(@class, '")
		.append(functionalGroupsSiteUtil.getGroupCoachesNotVisibleCss())
		.append("')]");
		
		functionalUtil.waitForPageToLoadElement(tutor0, selectorBuffer.toString());
		
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//div[contains(@class, '")
		.append(functionalGroupsSiteUtil.getGroupParticipantsCss())
		.append("')]");
		
		functionalUtil.waitForPageToLoadElement(tutor0, selectorBuffer.toString());
		
		
		/* logout tutor */
		functionalUtil.logout(tutor0);
	}

	@Test
	@RunAsClient
	public void checkConfigureAccessControl(@Drone @Tutor1 DefaultSelenium tutor0,
			@Drone @Student1 DefaultSelenium student0)
					throws IOException, URISyntaxException{
		/*
		 * test setup
		 */
		/* create users */
		int tutorCount = 1;
			
		final UserVO[] tutors = new UserVO[tutorCount];
		functionalVOUtil.createTestAuthors(deploymentUrl, tutorCount).toArray(tutors);
		
		int studentCount = 1;
		
		final UserVO[] students = new UserVO[studentCount];
		functionalVOUtil.createTestUsers(deploymentUrl, studentCount).toArray(students);
		
		/* create group via REST */
		final GroupVO[] groups = new GroupVO[tutorCount];
		functionalVOUtil.createTestCourseGroups(deploymentUrl, tutorCount).toArray(groups);
		
		functionalVOUtil.addOwnerToGroup(deploymentUrl, groups[0], tutors[0]);
		
		functionalVOUtil.setGroupConfiguration(deploymentUrl, groups[0],
				new String[]{
					CollaborationTools.TOOL_CALENDAR,
					CollaborationTools.TOOL_CHAT,
					CollaborationTools.TOOL_CONTACT,
					CollaborationTools.TOOL_FOLDER,
					CollaborationTools.TOOL_FORUM,
					CollaborationTools.TOOL_NEWS,
					CollaborationTools.TOOL_PORTFOLIO,
					CollaborationTools.TOOL_WIKI,
				},
				false, false,
				false, true,
				true, true);
		
		/*
		 * test case
		 */
		Assert.assertTrue(functionalUtil.login(tutor0, tutors[0].getLogin(), tutors[0].getPassword(), true));
		
		/* open group */
		Assert.assertTrue(functionalGroupsSiteUtil.openMyGroup(tutor0, groups[0].getName()));
		
		/* apply booking method */
		Assert.assertTrue(functionalGroupsSiteUtil.applyBookingAccessCode(tutor0,
				CONFIGURE_ACCESS_CONTROL_DESCRIPTION, CONFIGURE_ACCESS_CONTROL_ACCESS_CODE));
		
		/* logout tutor */
		functionalUtil.logout(tutor0);
		
		/*
		 * verify
		 */
		Assert.assertTrue(functionalUtil.login(student0, students[0].getLogin(), students[0].getPassword(), true));
		
		/* book group */
		Assert.assertTrue(functionalGroupsSiteUtil.bookWithAccessCode(student0,
				groups[0].getName(), CONFIGURE_ACCESS_CONTROL_ACCESS_CODE));
		
		/* verify tools */
		GroupTools[] tools = new GroupTools[]{
				GroupTools.CALENDAR,
				GroupTools.EMAIL,
				GroupTools.EPORTFOLIO,
				GroupTools.FOLDER,
				GroupTools.FORUM,
				GroupTools.INFORMATION,
				GroupTools.WIKI,
		};
		
		for(GroupTools currentTool: tools){
			GroupsTabAction action = functionalGroupsSiteUtil.findGroupTabActionForTool(currentTool);
			
			StringBuffer selectorBuffer = new StringBuffer();
			selectorBuffer.append("xpath=//ul[contains(@class, '")
			.append(functionalUtil.getTreeLevel1Css())
			.append("')]//li//a[contains(@class, '")
			.append(action.getIconCss())
			.append("')]");
			
			functionalUtil.waitForPageToLoadElement(student0, selectorBuffer.toString());
		}
		
		/* logout student */
		functionalUtil.logout(student0);
	}

	@Ignore
	@Test
	@RunAsClient
	public void checkAddUser(@Drone @Tutor1 DefaultSelenium tutor0,
			@Drone @Student1 DefaultSelenium student0)
					throws IOException, URISyntaxException{
		/*
		 * test setup
		 */
		/* create users */
		int tutorCount = 1;
			
		final UserVO[] tutors = new UserVO[tutorCount];
		functionalVOUtil.createTestAuthors(deploymentUrl, tutorCount).toArray(tutors);
		
		int studentCount = 1;
		
		final UserVO[] students = new UserVO[studentCount];
		functionalVOUtil.createTestUsers(deploymentUrl, studentCount).toArray(students);
		
		/* create group via REST */
		final GroupVO[] groups = new GroupVO[tutorCount];
		functionalVOUtil.createTestCourseGroups(deploymentUrl, tutorCount).toArray(groups);
		
		functionalVOUtil.addOwnerToGroup(deploymentUrl, groups[0], tutors[0]);
		
		functionalVOUtil.setGroupConfiguration(deploymentUrl, groups[0],
				new String[]{
					CollaborationTools.TOOL_CALENDAR,
					CollaborationTools.TOOL_CHAT,
					CollaborationTools.TOOL_CONTACT,
					CollaborationTools.TOOL_FOLDER,
					CollaborationTools.TOOL_FORUM,
					CollaborationTools.TOOL_NEWS,
					CollaborationTools.TOOL_PORTFOLIO,
					CollaborationTools.TOOL_WIKI,
				},
				false, false,
				false, true,
				true, true);
		
		/*
		 * test case
		 */
		Assert.assertTrue(functionalUtil.login(tutor0, tutors[0].getLogin(), tutors[0].getPassword(), true));
		
		/* open group */
		Assert.assertTrue(functionalGroupsSiteUtil.openMyGroup(tutor0, groups[0].getName()));

		/* add participant */
		Assert.assertTrue(functionalGroupsSiteUtil.addUser(tutor0, students[0].getLogin(), false, true, false));

		/* logout tutor */
		functionalUtil.idle(tutor0);
		functionalUtil.logout(tutor0);

		/*
		 * verify
		 */
		Assert.assertTrue(functionalUtil.login(student0, students[0].getLogin(), students[0].getPassword(), true));
		
		Assert.assertTrue(functionalGroupsSiteUtil.openMyGroup(student0, groups[0].getName()));
		

		/* logout student */
		functionalUtil.idle(student0);
		functionalUtil.logout(student0);
	}
	
	
	@Test
	@RunAsClient
	public void checkInvitation(@Student1 @Drone DefaultSelenium student0,
			@Student2 @Drone DefaultSelenium student1,
			@Student3 @Drone DefaultSelenium student2)
					throws IOException, URISyntaxException
	{
		/*
		 * test setup
		 */
		/* create users */
		int studentCount = 1;
		
		final UserVO[] students = new UserVO[studentCount];
		functionalVOUtil.createTestUsers(deploymentUrl, studentCount).toArray(students);
		
		/*
		 * test case
		 */
		/* user#0 - login with user */
		functionalUtil.login(student0, students[0].getLogin(), students[0].getPassword(), true);
		
		/* user#0 - create group */
		Assert.assertTrue(functionalGroupsSiteUtil.createGroup(student0,
				INVITATION_GROUP_NAME, INVITATION_GROUP_DESCRIPTION,
				3,
				null
		));
		
		/* user#0 - apply tools */
		GroupTools[] tools = new GroupTools[]{
				GroupTools.INFORMATION,
				GroupTools.EMAIL,
				GroupTools.CALENDAR,
				GroupTools.FOLDER,
				GroupTools.FORUM,
				GroupTools.CHAT,
				GroupTools.WIKI,
				GroupTools.EPORTFOLIO
			};
		Assert.assertTrue(functionalGroupsSiteUtil.applyTools(student0, tools));
		
		/* user#3 - login with user */
		functionalUtil.login(student2, students[2].getLogin(), students[2].getPassword(), true);
		
		/* user#0 - read visiting card link */
		String link = functionalGroupsSiteUtil.readVisitingCardLink(student0, INVITATION_GROUP_NAME);
		Assert.assertNotNull(link);
		
		/* open visiting card */
		functionalUtil.idle(student2);
		student2.open(link);
		
		StringBuffer locatorBuffer = new StringBuffer();
		
		locatorBuffer.append("xpath=//div[contains(@class, '")
		.append(FunctionalGroupsSiteUtil.GROUP_VISITING_CARD_CONTENT_CSS)
		.append("')]");
		
		functionalUtil.waitForPageToLoadElement(student2, locatorBuffer.toString());
		
		/* check members menu item not visible */
		functionalGroupsSiteUtil.openMyGroup(student2, INVITATION_GROUP_NAME);
		functionalGroupsSiteUtil.openGroupsTabActionByMenuTree(student2, GroupsTabAction.INFORMATION);
		
		locatorBuffer = new StringBuffer();
		
		locatorBuffer.append("xpath=//ul[contains(@class, '")
		.append(functionalUtil.getTreeLevel1Css())
		.append("')]//li[contains(@class, '")
		.append(GroupsTabAction.MEMBERS.getIconCss())
		.append("')]");
		
		Assert.assertFalse(functionalUtil.waitForPageToLoadElement(student2, locatorBuffer.toString(), false));
		
		/* user#0 - apply tools */
		Assert.assertTrue(functionalGroupsSiteUtil.applyTools(student0, new GroupTools[]{
				GroupTools.CALENDAR,
				GroupTools.CHAT,
				GroupTools.EMAIL,
				GroupTools.EPORTFOLIO,
				GroupTools.FOLDER,
				GroupTools.FORUM,
				GroupTools.INFORMATION,
				GroupTools.WIKI,
		}));
		
		/* user#2 - close and open visiting card */
		
	}
}
