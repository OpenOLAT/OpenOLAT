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
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.olat.core.util.StringHelper;
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.virtualauthenticator.Credential;
import org.openqa.selenium.virtualauthenticator.HasVirtualAuthenticator;
import org.openqa.selenium.virtualauthenticator.VirtualAuthenticator;
import org.openqa.selenium.virtualauthenticator.VirtualAuthenticatorOptions;
import org.openqa.selenium.virtualauthenticator.VirtualAuthenticatorOptions.Transport;

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
	private static final String acknowledgeCheckboxXPath = "//div[contains(@class,'modal-dialog')]//fieldset[contains(@class,'o_disclaimer')]//input[@name='acknowledge_checkbox']";
	
	public static final By loginFormBy = By.cssSelector("div.o_login_form");
	private static final By authOrDisclaimerXPath = By.xpath(footerUserDivXPath + "|" + acknowledgeCheckboxXPath);
	public static final By disclaimerXPath = By.xpath(acknowledgeCheckboxXPath);
	public static final By disclaimerButtonXPath = By.xpath("//div[contains(@class,'o_sel_disclaimer_buttons')]/button"); 
	
	public static final By resumeButton = By.className("o_sel_resume_yes");	
	public static final By usernameFooterBy = By.id("o_username");
	
	public static final By maintenanceMessageBy = By.cssSelector("#o_msg_sticky p");
	
	private final WebDriver browser;
	
	public LoginPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public static LoginPage load(WebDriver browser, URL deploymentUrl) {
		LoginPage page = new LoginPage(browser);
		browser.navigate().to(deploymentUrl);
		OOGraphene.waitElement(loginFormBy, browser);
		return page;
	}

	public LoginPage assertOnLoginPage() {
		OOGraphene.waitElement(loginFormBy, browser);
		return this;
	}
	
	public void assertLoggedIn(UserVO user) {
		assertLoggedInByLastName(user.getLastName());
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
		OOGraphene.waitElementSlowlyDisappears(maintenanceMessageBy, 10, browser);
		return this;
	}
	
	/**
	 * Enter OpenOLAT as guest
	 */
	public void asGuest() {
		By guestLinkBy = By.xpath("//a[i[contains(@class,'o_icon_provider_guest')]]");
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
		By footerUserBy = By.cssSelector("#o_footer_user #o_username");
		return loginAs(username, password, footerUserBy);
	}
	
	public LoginPage loginAs(String username, String password, By landingPointBy) {
		//fill login form
		By usernameId = By.id("o_fiooolat_login_name");
		OOGraphene.waitElement(usernameId, browser);//wait the login page
		browser.findElement(usernameId).sendKeys(username);
		By passwordId = By.id("o_fiooolat_login_pass");
		browser.findElement(passwordId).sendKeys(password);
		
		try {
			By loginBy = By.id("o_fiooolat_login_button");
			browser.findElement(loginBy).click();
		} catch (Exception e1) {
			OOGraphene.takeScreenshot("Login button", browser);
			throw e1;
		}
		return postSuccessfulLogin(landingPointBy);
	}
	
	private LoginPage postSuccessfulLogin(By landingPointBy) {
		try {
			OOGraphene.waitElement(authOrDisclaimerXPath, browser);
		} catch (Exception e1) {
			OOGraphene.takeScreenshot("Login button", browser);
			throw e1;
		}
		
		//wipe out disclaimer
		List<WebElement> disclaimer = browser.findElements(disclaimerXPath);
		if(disclaimer.size() > 0) {
			//click the disclaimer
			OOGraphene.waitModalDialog(browser);
			browser.findElement(disclaimerXPath).click();
			browser.findElement(disclaimerButtonXPath).click();
			try {
				OOGraphene.waitElementDisappears(disclaimerXPath, 10, browser);
			} catch (Exception e) {
				OOGraphene.takeScreenshot("Login disclaimer", browser);
				throw e;
			}
		}
		
		//wait until the content appears
		try {
			OOGraphene.waitElement(landingPointBy, 30, browser);
		} catch(Exception e) {
			OOGraphene.takeScreenshot("Login", browser);
		}
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
		browser.findElement(usernameId).sendKeys(username);
		By passwordId = By.id("o_fiooolat_login_pass");
		browser.findElement(passwordId).sendKeys(password);
		
		By loginBy = By.id("o_fiooolat_login_button");
		browser.findElement(loginBy).click();
		OOGraphene.waitBusy(browser);
		
		By errorMessageby = By.cssSelector("div.modal-body.alert.alert-danger");
		OOGraphene.waitElement(errorMessageby, browser);
		return this;
	}
	
	public VirtualAuthenticator registerAuthenticator() {
		VirtualAuthenticatorOptions options = new VirtualAuthenticatorOptions();
		options.setTransport(Transport.INTERNAL)
		       .setHasUserVerification(true)
		       .setIsUserVerified(true)
		       .setIsUserConsenting(true);
		return ((HasVirtualAuthenticator)browser).addVirtualAuthenticator(options);
	}
	
	public PasskeyInformations loginWithRegistrationToPasskey(String username, String password) {
		By footerUserBy = By.cssSelector("#o_footer_user #o_username");
		return loginWithRegistrationToPasskey(username, password, footerUserBy);
	}
	
	public PasskeyInformations loginWithRegistrationToPasskey(String username, String password, By landingPointBy) {
		VirtualAuthenticator authenticator = registerAuthenticator();
		
		// 1. Username
		By usernameId = By.id("o_fiooolat_login_name");
		OOGraphene.waitElement(usernameId, browser);//wait the login page
		browser.findElement(usernameId).sendKeys(username);
		By loginBy = By.id("o_fiooolat_login_button");
		browser.findElement(loginBy).click();
		
		// 2. Password
		By passwordId = By.id("o_fiooolat_login_pass");
		OOGraphene.waitElement(passwordId, browser);//wait the password field
		browser.findElement(passwordId).sendKeys(password);
		browser.findElement(loginBy).click();
		
		// 3. Generate passkey
		By generateBy = By.cssSelector("#o_fiooolat_login_button.o_sel_auth_generate_passkey");
		OOGraphene.waitElement(generateBy, browser);
		browser.findElement(generateBy).click();
		OOGraphene.waitBusy(browser);
		
		// 4. Check Recovery keys
		By recoveryKeysBy = By.className("o_sel_auth_recovery_keys");
		OOGraphene.waitElement(recoveryKeysBy, browser);
		List<String> recoveryKeys = extractRecoveryKeys(recoveryKeysBy);
		
		By nextBy = By.cssSelector("#o_fiooolat_login_button.o_sel_auth_next");
		OOGraphene.waitElement(nextBy, browser);
		browser.findElement(nextBy).click();
		
		List<Credential> credentials = authenticator.getCredentials();
		Assert.assertFalse(credentials.isEmpty());
		postSuccessfulLogin(landingPointBy);
		return new PasskeyInformations(credentials, recoveryKeys, authenticator);
	}
	
	private List<String> extractRecoveryKeys(By recoveryKeysBy) {
		String text = browser.findElement(recoveryKeysBy).getText();
		List<String> recoveryKeys = new ArrayList<>();
		String[] recoveryKeysArr = text.split("\r?\n|\r");
		for(String recoveryKey:recoveryKeysArr) {
			if(StringHelper.containsNonWhitespace(recoveryKey)) {
				recoveryKeys.add(recoveryKey.trim());
			}
		}
		return recoveryKeys;
	}
	
	public LoginPage loginWithPasskey(String username, List<Credential> credentials) {
		By footerUserBy = By.cssSelector("#o_footer_user #o_username");
		return loginWithPasskey(username, footerUserBy, credentials);
	}
	
	public LoginPage loginWithPasskey(String username, By landingPointBy, List<Credential> credentials) {
		VirtualAuthenticator authenticator = registerAuthenticator();
		for(Credential credential:credentials) {
			authenticator.addCredential(Credential.createNonResidentCredential(credential.getId(), "localhost", credential.getPrivateKey(), credential.getSignCount()));
		}
		
		// 1. Username
		By usernameId = By.id("o_fiooolat_login_name");
		OOGraphene.waitElement(usernameId, browser);//wait the login page
		browser.findElement(usernameId).sendKeys(username);
		By loginBy = By.id("o_fiooolat_login_button");
		browser.findElement(loginBy).click();

		return postSuccessfulLogin(landingPointBy);
	}
	
	public LoginPage loginWithPasskeyButRecovery(String username, PasskeyInformations passkeyInfos) {
		By footerUserBy = By.cssSelector("#o_footer_user #o_username");
		return loginWithPasskeyButRecovery(username, footerUserBy, passkeyInfos);
	}
	
	public LoginPage loginWithPasskeyButRecovery(String username, By landingPointBy, PasskeyInformations passkeyInfos) {
		((HasVirtualAuthenticator)browser).removeVirtualAuthenticator(passkeyInfos.authenticator());

		// 1. Username
		By usernameId = By.id("o_fiooolat_login_name");
		OOGraphene.waitElement(usernameId, browser);//wait the login page
		browser.findElement(usernameId).sendKeys(username);
		By loginBy = By.id("o_fiooolat_login_button");
		browser.findElement(loginBy).click();
		
		// 2. Wait recovery key button
		By recoveryKeyButtonBy = By.cssSelector("a.o_sel_auth_recovery_key_send");
		OOGraphene.waitElement(recoveryKeyButtonBy, browser);//wait the login page
		
		// The javascript error is triggered by some user interaction in selenium
		By nextBy = By.cssSelector("#o_fiooolat_login_button.o_sel_auth_next");
		browser.findElement(nextBy).click();
		OOGraphene.waitElement(By.id("o_fiooolat_login_name_error"), browser);
		
		OOGraphene.waitElement(recoveryKeyButtonBy, browser);//wait the login page
		browser.findElement(recoveryKeyButtonBy).click();
		
		// 3. Use a recovery key
		By recoveryKeyBy = By.cssSelector("input.o_sel_auth_recovery_key");
		OOGraphene.waitElement(recoveryKeyBy, browser);//wait the login page
		browser.findElement(recoveryKeyBy).sendKeys(passkeyInfos.recoveryKeys().get(0));
		
		nextBy = By.cssSelector("#o_fiooolat_login_button.o_sel_auth_next");
		browser.findElement(nextBy).click();

		return postSuccessfulLogin(landingPointBy);
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
			resumes.get(0).click();
			OOGraphene.waitModalDialogDisappears(browser);
			OOGraphene.waitElementAbsence(By.xpath("//div[contains(@class,'modal-backdrop')]"), 5, browser);
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
	
	public record PasskeyInformations(List<Credential> credentials, List<String> recoveryKeys, VirtualAuthenticator authenticator) {
		//
	}
}
