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
	@Drone @Participant
	private WebDriver participantBrowser;
	@ArquillianResource
	private URL deploymentUrl;	

	@Page
	private UserToolsPage userTools;
	@Page
	private NavigationPage navBar;
	

	@Page @Participant
	private NavigationPage participantNavBar;

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
	public void groupMembers(@InitialPage LoginPage loginPage)
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO participant = new UserRestClient(deploymentUrl).createRandomUser();
		
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
			.setVisibility(true, true)
			.addMember();
		
		members.searchMember(participant)
			.next()
			.next()
			.next()
			.finish();
		
		LoginPage participantLoginPage = LoginPage.getLoginPage(participantBrowser, deploymentUrl);
		//tools
		participantLoginPage
			.loginAs(participant.getLogin(), participant.getPassword())
			.resume();
		
		participantNavBar
				.openGroups(participantBrowser)
				.selectGroup(groupName);
		
		WebElement contentEl = participantBrowser.findElement(By.id("o_main_center_content_inner"));
		String content = contentEl.getText();
		Assert.assertTrue(content.contains(groupName));
	}
}
