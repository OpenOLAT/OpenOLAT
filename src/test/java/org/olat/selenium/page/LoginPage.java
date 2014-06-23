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
package org.olat.selenium.page;


import java.util.List;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.page.Location;
import org.jcodec.common.Assert;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * The login page, annoted to be used as @InitialPage
 * 
 * 
 * Initial date: 19.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Location("dmz")
public class LoginPage {
	
	public static final String loginFormClassName = "o_login_form";
	public static final String loginFormXPath = "//div[contains(@class,'o_login_form')]";

	public static final String usernameId = "o_fiooolat_login_name";
	public static final String passwordId = "o_fiooolat_login_pass";
	public static final String loginButtonId = "o_fiooolat_login_button";
	public static final String footerUserDivXPath = "//div[@id='o_footer_user']";
	public static final String acknowledgeCheckboxXPath = "//input[@name='acknowledge_checkbox']";
	
	public static final By authXPath = By.xpath(footerUserDivXPath);
	public static final By loginFormBy = By.xpath(loginFormXPath);
	public static final By authOrDisclaimerXPath = By.xpath(footerUserDivXPath + "|" + acknowledgeCheckboxXPath);
	public static final By disclaimerXPath = By.xpath(acknowledgeCheckboxXPath);
	public static final By disclaimerButtonXPath = By.xpath("//div[contains(@class,'o_sel_disclaimer_buttons')]/button"); 
	
	public static final By resumeButton = By.className("o_sel_resume_yes");

	@FindBy(className = loginFormClassName)
	private WebElement loginDiv;
	@FindBy(xpath = loginFormXPath)
	private WebElement loginDivXPath;
	
	@FindBy(id = usernameId)
	private WebElement usernameInput;
	@FindBy(id = passwordId)
	private WebElement passwordInput;
	@FindBy(id = loginButtonId)
	private WebElement loginButton;
	
	@Drone
	private WebDriver browser;

	public LoginPage assertOnLoginPage() {
		Assert.assertNotNull(loginDiv);
		Assert.assertNotNull(loginDivXPath);

		Assert.assertTrue(loginDiv.isDisplayed());
		Assert.assertTrue(loginDivXPath.isDisplayed());
		return this;
	}
	
	/**
	 * Login and accept the disclaimer if there is one.
	 * 
	 * @param username
	 * @param password
	 */
	public void loginAs(String username, String password) {
		usernameInput.sendKeys(username);
		passwordInput.sendKeys(password);

		Graphene.guardHttp(loginButton).click();
		OOGraphene.waitElement(authOrDisclaimerXPath);
		
		List<WebElement> disclaimer = browser.findElements(disclaimerXPath);
		if(disclaimer.size() > 0) {
			//click the disclaimer
			disclaimer.get(0).click();
			
			WebElement acknowledgeButton = browser.findElement(disclaimerButtonXPath);
			Graphene.guardHttp(acknowledgeButton).click();
			OOGraphene.waitElement(authXPath);
		}
	}
	
	/**
	 * Resume the session, and assert that the resume panel has popped
	 */
	public void resumeWithAssert() {
		WebElement resume = browser.findElement(resumeButton);
		Assert.assertNotNull(resume);
		Assert.assertTrue(resume.isDisplayed());
		
		resume.click();
		OOGraphene.waitBusy();
	}
}
