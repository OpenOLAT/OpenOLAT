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

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.test.ArquillianDeployments;

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
    browser.waitForPageToLoad("15000");
		
    //check if administrator appears in the footer
    boolean loginAs = browser.isElementPresent("xpath=//div[@id='b_footer_user']//i[contains(text(), 'administrator')]");
    Assert.assertTrue("User should be logged in!", loginAs);
	}
}
