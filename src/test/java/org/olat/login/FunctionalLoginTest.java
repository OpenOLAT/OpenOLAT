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
package org.olat.login;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.util.EntityUtils;
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
import org.olat.restapi.RestConnection;
import org.olat.test.ArquillianDeployments;
import org.olat.user.restapi.UserVO;
import org.olat.util.FunctionalUtil;

import com.thoughtworks.selenium.DefaultSelenium;

/**
 * 
 * 
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@RunWith(Arquillian.class)
public class FunctionalLoginTest {

	@Deployment(testable = false)
	public static WebArchive createDeployment() {
		return ArquillianDeployments.createDeployment();
	}

	@Drone
	DefaultSelenium browser;

	@ArquillianResource
	URL deploymentUrl;

	FunctionalUtil functionalUtil;

	@Before
	public void setup(){
		functionalUtil = new FunctionalUtil();
	}

	@Test
	@RunAsClient
	public void loadIndex() {
		browser.open(deploymentUrl + "dmz");
		browser.waitForPageToLoad("5000");
		boolean isLoginFormPresent = browser.isElementPresent("xpath=//div[@class='o_login_form']");
		Assert.assertTrue(isLoginFormPresent);
	}

	@Test
	@RunAsClient
	public void loadLogin() {
		browser.open(deploymentUrl + "dmz");
		browser.waitForPageToLoad("5000");
		boolean isLoginFormPresent = browser.isElementPresent("xpath=//div[@class='o_login_form']");
		Assert.assertTrue(isLoginFormPresent);

		//type the password
		browser.type("id=o_fiooolat_login_name", "administrator");
		browser.type("id=o_fiooolat_login_pass", "openolat");
		browser.click("id=o_fiooolat_login_button");
		functionalUtil.waitForPageToLoad(browser);

		//check if administrator appears in the footer
		boolean loginAs = browser.isElementPresent("xpath=//div[@id='b_footer_user']//i[contains(text(), 'administrator')]");
		if(!loginAs) {
			boolean acknowledge = browser.isElementPresent("xpath=//input[@name='acknowledge_checkbox']");
			Assert.assertTrue("Acknowledge first!", acknowledge);
			browser.click("name=acknowledge_checkbox");
		} 
	}

	@Test
	@RunAsClient
	public void loginWithRandomUser() throws IOException, URISyntaxException{
		RestConnection restConnection = new RestConnection(deploymentUrl);

		Assert.assertTrue(restConnection.login(functionalUtil.getUsername(), functionalUtil.getPassword()));

		UserVO vo = new UserVO();
		String username = UUID.randomUUID().toString();
		vo.setLogin(username);
		String password = UUID.randomUUID().toString();
		vo.setPassword(password);
		vo.setFirstName("John");
		vo.setLastName("Smith");
		vo.setEmail(username + "@frentix.com");
		vo.putProperty("telOffice", "39847592");
		vo.putProperty("telPrivate", "39847592");
		vo.putProperty("telMobile", "39847592");
		vo.putProperty("gender", "Female");//male or female
		vo.putProperty("birthDay", "12/12/2009");

		URI request = UriBuilder.fromUri(deploymentUrl.toURI()).path("restapi").path("users").build();
		HttpPut method = restConnection.createPut(request, MediaType.APPLICATION_JSON, true);
		restConnection.addJsonEntity(method, vo);
		method.addHeader("Accept-Language", "en");

		HttpResponse response = restConnection.execute(method);
		assertTrue(response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201);
		EntityUtils.consume(response.getEntity());

		functionalUtil.setDeploymentUrl(deploymentUrl.toString());
		Assert.assertTrue(functionalUtil.login(browser));

		restConnection.shutdown();
	}
}
