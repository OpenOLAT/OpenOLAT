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


import java.net.URL;
import java.util.List;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.junit.Assert;
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * The login page, annoted to be used as @InitialPage
 * 
 * 
 * Initial date: 19.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LoginPage {
	
	private static final String footerUserDivXPath = "//div[@id='o_footer_user']/span[@id='o_username']";
	private static final String acknowledgeCheckboxXPath = "//input[@name='acknowledge_checkbox']";
	
	public static final By loginFormBy = By.cssSelector("div.o_login_form");
	private static final By authOrDisclaimerXPath = By.xpath(footerUserDivXPath + "|" + acknowledgeCheckboxXPath);
	public static final By disclaimerXPath = By.xpath(acknowledgeCheckboxXPath);
	public static final By disclaimerButtonXPath = By.xpath("//div[contains(@class,'o_sel_disclaimer_buttons')]/button"); 
	
	public static final By resumeButton = By.className("o_sel_resume_yes");	
	public static final By usernameFooterBy = By.id("o_username");
	
	public static final By maintenanceMessageBy = By.cssSelector("#o_msg_sticky p");
	
	@Drone
	private WebDriver browser;
	
	public static LoginPage load(WebDriver browser, URL deploymentUrl) {
		LoginPage page = new LoginPage(browser);
		browser.navigate().to(deploymentUrl);
		OOGraphene.waitElement(loginFormBy, browser);
		return page;
	}
	
	public LoginPage() {
		//
	}
	
	public LoginPage(WebDriver browser) {
		this.browser = browser;
	}

	public LoginPage assertOnLoginPage() {
		OOGraphene.waitElement(loginFormBy, browser);
		return this;
	}
	
	public void assertLoggedIn(UserVO user) {
		WebElement username = browser.findElement(usernameFooterBy);
		Assert.assertNotNull(username);
		Assert.assertTrue(username.isDisplayed());
		String name = username.getText();
		Assert.assertTrue(name.contains(user.getLastName()));
	}
	
	public void assertLoggedInByLastName(String lastName) {
		OOGraphene.waitElement(usernameFooterBy, browser);
		WebElement username = browser.findElement(usernameFooterBy);
		Assert.assertNotNull(username);
		Assert.assertTrue(username.isDisplayed());
		String name = username.getText();
		Assert.assertTrue(name.contains(lastName));
	}
	
	public LoginPage assertOnMaintenanceMessage(String text) {
		WebElement messageEl = browser.findElement(maintenanceMessageBy);
		String message = messageEl.getText();
		Assert.assertTrue(message.contains(text));
		return this;
	}
	
	public LoginPage assertOnMembershipConfirmation() {
		By reservationBy = By.cssSelector("div.o_reservation");
		OOGraphene.waitElement(reservationBy, 10, browser);
		WebElement reservationEl = browser.findElement(reservationBy);
		Assert.assertTrue(reservationEl.isDisplayed());
		return this;
	}
	
	public LoginPage waitOnMaintenanceMessage(String text) {
		OOGraphene.waitElement(maintenanceMessageBy, 10, browser);
		return assertOnMaintenanceMessage(text);
	}
	
	public LoginPage waitOnMaintenanceMessageCleared() {
		OOGraphene.waitElementDisappears(maintenanceMessageBy, 10, browser);
		return this;
	}
	
	/**
	 * Enter OpenOLAT as guest
	 */
	public void asGuest() {
		By guestLinkBy = By.xpath("//a[contains(@href,'menu.guest')]");
		OOGraphene.waitElement(guestLinkBy, browser);
		browser.findElement(guestLinkBy).click();
		By footerUserDivBy = By.id("o_footer_user");
		OOGraphene.waitElement(footerUserDivBy, browser);
	}
	
	/**
	 * Login and accept the disclaimer if there is one.
	 * 
	 * @param user
	 */
	public LoginPage loginAs(UserVO user) {
		return loginAs(user.getLogin(), user.getPassword());
	}
	
	/**
	 * Login and accept the disclaimer if there is one.
	 * 
	 * @param username
	 * @param password
	 */
	public LoginPage loginAs(String username, String password) {
		//fill login form
		By usernameId = By.id("o_fiooolat_login_name");
		OOGraphene.waitElement(usernameId, browser);//wait the login page
		WebElement usernameInput = browser.findElement(usernameId);
		usernameInput.sendKeys(username);
		By passwordId = By.id("o_fiooolat_login_pass");
		WebElement passwordInput = browser.findElement(passwordId);
		passwordInput.sendKeys(password);
		
		By loginBy = By.id("o_fiooolat_login_button");
		browser.findElement(loginBy).click();
		OOGraphene.waitElement(authOrDisclaimerXPath, browser);
		
		//wipe out disclaimer
		List<WebElement> disclaimer = browser.findElements(disclaimerXPath);
		if(disclaimer.size() > 0) {
			//click the disclaimer
			disclaimer.get(0).click();
			browser.findElement(disclaimerButtonXPath).click();
		}
		
		//wait until the content appears
		By footerUserBy = By.cssSelector("#o_footer_user #o_username"); 
		OOGraphene.waitElement(footerUserBy, 30, browser);
		return this;
	}
	
	/**
	 * The login will not be successful. The method assert
	 * on the error message.
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	public LoginPage loginDenied(String username, String password) {
		//fill login form
		By usernameId = By.id("o_fiooolat_login_name");
		OOGraphene.waitElement(usernameId, browser);//wait the login page
		WebElement usernameInput = browser.findElement(usernameId);
		usernameInput.sendKeys(username);
		By passwordId = By.id("o_fiooolat_login_pass");
		WebElement passwordInput = browser.findElement(passwordId);
		passwordInput.sendKeys(password);
		
		By loginBy = By.id("o_fiooolat_login_button");
		browser.findElement(loginBy).click();
		OOGraphene.waitBusy(browser);
		
		By errorMessageby = By.cssSelector("div.modal-body.alert.alert-danger");
		OOGraphene.waitElement(errorMessageby, browser);
		return this;
	}
	
	/**
	 * Resume the session, and assert that the resume panel has popped
	 */
	public LoginPage resumeWithAssert() {
		WebElement resume = browser.findElement(resumeButton);
		Assert.assertNotNull(resume);
		Assert.assertTrue(resume.isDisplayed());
		
		resume.click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public LoginPage resume() {
		List<WebElement> resumes = browser.findElements(resumeButton);
		if(resumes.size() > 0 && resumes.get(0).isDisplayed()) {
			WebElement resume = resumes.get(0);
			resume.click();
			OOGraphene.waitBusy(browser);
		}
		return this;
	}
	
	public LoginPage confirmMembership() {
		By acceptLinkBy = By.xpath("//div[contains(@class,'o_reservation')]//a[i[contains(@class,'o_icon_accept')]]");
		browser.findElement(acceptLinkBy).click();
		OOGraphene.waitBusy(browser);
		
		By okBy = By.cssSelector("button.btn.btn-primary");
		browser.findElement(okBy).click();
		OOGraphene.waitBusy(browser);

		By reservationBy = By.xpath("//div[contains(@class,'o_reservation')]");
		OOGraphene.waitElementDisappears(reservationBy, 5, browser);
		return this;
	}
}
