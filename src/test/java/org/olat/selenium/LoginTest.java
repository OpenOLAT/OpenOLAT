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

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.selenium.page.LoginPage;
import org.olat.selenium.page.NavigationPage;
import org.olat.selenium.page.Participant;
import org.olat.selenium.page.Student;
import org.olat.selenium.page.core.AdministrationMessagesPage;
import org.olat.test.rest.UserRestClient;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 19.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@RunWith(Arquillian.class)
public class LoginTest extends Deployments {

	@Drone
	private WebDriver browser;
	@ArquillianResource
	private URL deploymentUrl;

	/**
	 * Test if the dmz can be loaded.
	 * @param loginPage
	 */
	@Test
	@RunAsClient
	public void loadIndex(LoginPage loginPage) {
		//check that the login page, or dmz is loaded
		LoginPage.load(browser, deploymentUrl)
			.assertOnLoginPage();
	}
	
	/**
	 * Test login as administrator.
	 * 
	 * @param loginPage
	 */
	@Test
	@RunAsClient
	public void loginAsAdministrator() {
		//load dmz
		LoginPage loginPage = LoginPage
				.load(browser, deploymentUrl)
				.assertOnLoginPage();
		//login as administrator
		loginPage
			.loginAs("administrator", "openolat")
			.resume();
	}
	
	/**
	 * 
	 * Create a new user and try to login with its credentials.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void loginAsNewUser()
	throws IOException, URISyntaxException {
		//create a random user
		UserRestClient userClient = new UserRestClient(deploymentUrl);
		UserVO user = userClient.createRandomUser();

		//load dmz
		LoginPage loginPage = LoginPage
				.load(browser, deploymentUrl)
				.assertOnLoginPage();
		//login
		loginPage.loginAs(user.getLogin(), user.getPassword());
	}
	
	/**
	 * An administrator set a maintenance message. A first user
	 * logs in before and wait until the message appears. A second
	 * user load the login page, check that the message is visible,
	 * logs in and check that the message is visible too.
	 * 
	 * 
	 * @param loginPage
	 * @param reiBrowser
	 * @param kanuBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void maintenanceMessage( 
			@Drone @Participant WebDriver reiBrowser,
			@Drone @Student WebDriver kanuBrowser)
	throws IOException, URISyntaxException {
		
		UserVO rei = new UserRestClient(deploymentUrl).createRandomUser("Rei");
		UserVO kanu = new UserRestClient(deploymentUrl).createRandomUser("Kanu");
		
		//a first user log in
		LoginPage kanuLogin = LoginPage.load(kanuBrowser, deploymentUrl)
			.loginAs(kanu)
			.resume();
		
		// administrator come in, and set a maintenance message
		LoginPage.load(browser, deploymentUrl)
			.assertOnLoginPage()
			.loginAs("administrator", "openolat")
			.resume();
		
		String message = "Hello - " + UUID.randomUUID();
		AdministrationMessagesPage messagesPage = NavigationPage.load(browser)
			.openAdministration()
			.selectInfoMessages()
			.newMaintenanceMessage(message);
		
		//A new user see the login page 	
		LoginPage.load(reiBrowser, deploymentUrl)
			.waitOnMaintenanceMessage(message)
			.loginAs(rei)
			.resume()
			.assertOnMaintenanceMessage(message);
		
		kanuLogin
			.waitOnMaintenanceMessage(message);
		
		//administrator remove the message
		messagesPage
			.clearMaintenanceMessage();
		
		//we wait it disappears
		kanuLogin
			.waitOnMaintenanceMessageCleared();
	}
}
