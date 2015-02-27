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
package org.olat.selenium;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.InitialPage;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.selenium.page.LoginPage;
import org.olat.selenium.page.NavigationPage;
import org.olat.selenium.page.Participant;
import org.olat.selenium.page.Student;
import org.olat.selenium.page.User;
import org.olat.selenium.page.core.IMPage;
import org.olat.selenium.page.group.GroupPage;
import org.olat.selenium.page.group.MembersWizardPage;
import org.olat.selenium.page.user.UserToolsPage;
import org.olat.test.ArquillianDeployments;
import org.olat.test.rest.UserRestClient;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 03.07.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@RunWith(Arquillian.class)
public class BusinessGroupTest {
	
	@Deployment(testable = false)
	public static WebArchive createDeployment() {
		return ArquillianDeployments.createDeployment();
	}

	@Drone
	private WebDriver browser;
	@ArquillianResource
	private URL deploymentUrl;	

	@Page
	private UserToolsPage userTools;
	@Page
	private NavigationPage navBar;

	/**
	 * An author create a group, set the visibility to
	 * show owners and participants. Add a member to the
	 * group.
	 * 
	 * The participant log in, search the group and open it.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void groupMembers(@InitialPage LoginPage loginPage,
			@Drone @Participant WebDriver participantBrowser)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createRandomUser("Selena");
		UserVO participant = new UserRestClient(deploymentUrl).createRandomUser("Aoi");
		
		loginPage
			.loginAs(author.getLogin(), author.getPassword())
			.resume();
		
		//go to groups
		String groupName = "Group-1-" + UUID.randomUUID();
		GroupPage group = navBar
			.openGroups(browser)
			.createGroup(groupName, "A very little group");
		
		MembersWizardPage members = group
			.openAdministration()
			.openAdminMembers()
			.setVisibility(true, true, false)
			.addMember();
		
		members.searchMember(participant, false)
			.next()
			.next()
			.next()
			.finish();
		
		LoginPage participantLoginPage = LoginPage.getLoginPage(participantBrowser, deploymentUrl);
		//tools
		participantLoginPage
			.loginAs(participant.getLogin(), participant.getPassword())
			.resume();
		
		NavigationPage participantNavBar = new NavigationPage(participantBrowser);
		participantNavBar
				.openGroups(participantBrowser)
				.selectGroup(groupName);
		
		WebElement contentEl = participantBrowser.findElement(By.id("o_main_center_content_inner"));
		String content = contentEl.getText();
		Assert.assertTrue(content.contains(groupName));
	}
	
	/**
	 * An author creates a group, it opens the tab groups and then "My groups". It
	 * creates a group, enters a number of participants "1", enable the waiting
	 * list. In members visibility, it see coaches, participants and waiting
	 * list visible to members.<br>
	 * A participant and than a student come, book the group. The first enters
	 * the group, the second the waiting list.<br>
	 * The author go in the members list to check if it's in the coach list,
	 * the participant in the participants list and the student in the waiting
	 * list.
	 * 
	 * Should show group starting page, with menu items Administration and Bookings visible
	 * 
	 * @param loginPage
	 * @param participantBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void createGroupWithWaitingList(@InitialPage LoginPage loginPage,
			@Drone @Participant WebDriver participantBrowser,
			@Drone @Student WebDriver studentBrowser)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createRandomUser("Selena");
		UserVO participant = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		UserVO student = new UserRestClient(deploymentUrl).createRandomUser("Asuka");
	
		loginPage
			.loginAs(author.getLogin(), author.getPassword())
			.resume();
		
		//go to groups
		String groupName = "Group-1-" + UUID.randomUUID();
		GroupPage group = navBar
			.openGroups(browser)
			.createGroup(groupName, "A group with a waiting list")
			.openAdministration()
			//set waiting list and 1 participant
			.openEditDetails()
			.setMaxNumberOfParticipants(1)
			.setWaitingList()
			.saveDetails();
		
		//add booking ( token one )
		String token = "secret";
		String description = "The password is secret";
		group.openBookingConfig()
			.openAddDropMenu()
			.addTokenMethod()
			.configureTokenMethod(token, description)
			.assertOnToken(token)
			.save();
		
		//members see members
		group = GroupPage.getGroup(browser)
			.openAdminMembers()
			.setVisibility(true, true, true)
			.openMembers();
		

		//participant search published groups
		LoginPage participantLoginPage = LoginPage.getLoginPage(participantBrowser, deploymentUrl);
		//tools
		participantLoginPage
			.loginAs(participant.getLogin(), participant.getPassword())
			.resume();
		//groups
		NavigationPage participantNavBar = new NavigationPage(participantBrowser);
		participantNavBar
				.openGroups(participantBrowser)
				.publishedGroups()
				.bookGroup(groupName)
				.bookToken(token);
		//are we that we are in the right group?
		GroupPage.getGroup(participantBrowser)
			.assertOnInfosPage(groupName);
		
		
		//student search published groups
		LoginPage studentLoginPage = LoginPage.getLoginPage(studentBrowser, deploymentUrl);
		//tools
		studentLoginPage
			.loginAs(student.getLogin(), student.getPassword())
			.resume();
		//groups
		NavigationPage studentNavBar = new NavigationPage(studentBrowser);
		studentNavBar
				.openGroups(studentBrowser)
				.publishedGroups()
				.bookGroup(groupName)
				.bookToken(token);
		//are we that we are in the right group?
		GroupPage.getGroup(studentBrowser)
			.assertOnWaitingList(groupName);
		
		group = GroupPage.getGroup(browser)
				.openMembers()
				.assertMembersInOwnerList(author)
				.assertMembersInParticipantList(participant)
				.assertMembersInWaitingList(student);
	}
	
	/**
	 * An author create a group, set the visibility to true for owners
	 * and participants, enable the tools and add 2 users to it. The 2
	 * users joins the chat. All three send some messages and read them.
	 * 
	 * @param loginPage
	 * @param kanuBrowser
	 * @param ryomouBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void groupChat(@InitialPage LoginPage loginPage,
			@Drone @Participant WebDriver kanuBrowser,
			@Drone @User WebDriver ryomouBrowser)
	throws IOException, URISyntaxException {

		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO kanu = new UserRestClient(deploymentUrl).createRandomUser("Kanu");
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		
		loginPage
			.loginAs(author.getLogin(), author.getPassword())
			.resume();
		
		//go to groups
		String groupName = "Group-Chat-1-" + UUID.randomUUID();
		GroupPage group = navBar
			.openGroups(browser)
			.createGroup(groupName, "A very little group to chat");
		
		group
			.openAdministration()
			.openAdminTools()
			.enableTools()
			.openAdminMembers()
			.setVisibility(true, true, false);
		//add Kanu to the group
		group
			.openAdminMembers()
			.addMember()
			.searchMember(kanu, true)
			.next().next().next().finish();
		//add Ryomou
		group.addMember()
			.searchMember(ryomou, true)
			.next().next().next().finish();
		
		//Kanu open the group
		LoginPage kanuLoginPage = LoginPage.getLoginPage(kanuBrowser, deploymentUrl);
		kanuLoginPage
			.loginAs(kanu.getLogin(), kanu.getPassword())
			.resume();
		
		NavigationPage kanuNavBar = new NavigationPage(kanuBrowser);
		GroupPage kanuGroup = kanuNavBar
			.openGroups(kanuBrowser)
			.selectGroup(groupName);
		
		//Ryomou open the group
		LoginPage ryomouLoginPage = LoginPage.getLoginPage(ryomouBrowser, deploymentUrl);
		ryomouLoginPage
			.loginAs(ryomou.getLogin(), ryomou.getPassword())
			.resume();
		
		NavigationPage ryomouNavBar = new NavigationPage(ryomouBrowser);
		IMPage ryomouIM = ryomouNavBar
			.openGroups(ryomouBrowser)
			.selectGroup(groupName)
			.openChat()
			.openGroupChat();
		
		//Author send a message to Kanu
		String msg1 = "Hello Kanu " + UUID.randomUUID();
		IMPage authorIM = group
			.openChat()
			.openGroupChat()
			.sendMessage(msg1)
			.assertOnMessage(msg1);
		
		String msg2 = "Hello dear author " + UUID.randomUUID();
		//Kanu opens her chat window
		IMPage kanuIM = kanuGroup
			.openChat()
			.openGroupChat()
			.assertOnMessage(msg1)
			.sendMessage(msg2);
		
		String msg3 = "Hello Kanu and author " + UUID.randomUUID();
		//Ryomou reads her messages
		ryomouIM
			.sendMessage(msg3)
			.assertOnMessage(msg1)
			.assertOnMessage(msg2);
		//Kanu reads her message
		kanuIM
			.assertOnMessage(msg3);
		//Author reads too
		authorIM
			.assertOnMessage(msg2)
			.assertOnMessage(msg3);
	}
}
