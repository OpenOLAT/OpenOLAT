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
package org.olat.selenium.page.user;

import org.olat.selenium.page.LoginPage;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.dumbster.smtp.SmtpMessage;

/**
 * 
 * Initial date: 5 sept. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RegistrationPage {
	
	private final WebDriver browser;
	
	private RegistrationPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public static RegistrationPage getPage(WebDriver browser) {
		return new RegistrationPage(browser);
	}
	
	public RegistrationPage signIn() {
		By signInBy = By.id("o_co_olat_login_register");
		OOGraphene.waitElement(signInBy, browser).click();
		By modalBy = By.cssSelector("dialog.o_wizard div.modal-body");
		OOGraphene.waitElement(modalBy, browser);
		return this;
	}
	
	public RegistrationPage nextToDisclaimer() {
		By languageBy = By.id("o_fioselect_language_SELBOX");
		WebElement languageEl = OOGraphene.waitElement(languageBy, browser);	
		new Select(languageEl).selectByValue("en");
		OOGraphene.waitBusy(browser);
		OOGraphene.nextStep(browser);
		// wait disclaimer
		By disclaimerBy = By.cssSelector("fieldset.o_disclaimer");
		OOGraphene.waitElement(disclaimerBy, browser);
		return this;
	}
	
	public RegistrationPage acknowledgeDisclaimer() {
		browser.findElement(LoginPage.disclaimerXPath).click();
		OOGraphene.nextStep(browser);	
		By mailBy = By.className("o_sel_registration_email");
		OOGraphene.waitElement(mailBy, browser);
		return this;
	}
	
	public RegistrationPage validate(String email) {
		By emailBy = By.cssSelector(".o_sel_registration_email input[type='text']");
		OOGraphene.waitElement(emailBy, browser).sendKeys(email);
		By validateBy = By.cssSelector(".o_sel_registration_email_form a.btn.btn-primary");
		browser.findElement(validateBy).click();
		
		By otpBy = By.cssSelector(".o_sel_registration_email_form .o_sel_registration_otp");
		OOGraphene.waitElement(otpBy, browser);
		return this;
	}

	public RegistrationPage validateOtp(String otp) {
		By otpBy = By.cssSelector(".o_sel_registration_email_form .o_sel_registration_otp input[type='text']");
		OOGraphene.waitElement(otpBy, browser);
		browser.findElement(otpBy).sendKeys(otp);
		
		By validateOtpBy = By.cssSelector(".o_sel_registration_email_form .o_sel_registration_otp .o_success_with_icon");
		OOGraphene.waitElement(validateOtpBy, browser);
		OOGraphene.nextStep(browser);
		
		By firstNameBy = By.cssSelector(".o_sel_registration_2_form");
		OOGraphene.waitElement(firstNameBy, browser);
		return this;
	}
	
	public static String extractOtp(SmtpMessage message) {
		String body = message.getBody();
		int index = body.indexOf("'otp'>");
		if(index >= 0) {
			body = body.substring(index + 6);
		}
		int nextIndex = body.indexOf("</span");
		if(nextIndex >= 0) {
			body = body.substring(0, nextIndex);
		}
		return body;
	}
	
	public String extractRegistrationLink(SmtpMessage message) {
		String body = message.getBody();
		int index = body.indexOf("http");
		if(index >= 0) {
			int lastIndex = body.indexOf(' ', index + 1);
			return body.substring(index, lastIndex);
		}
		return null;
	}
	
	public RegistrationPage loadRegistrationLink(String link) {
		browser.navigate().to(link);
		return this;
	}
	
	public void finalizeRegistration(String firstName, String lastName, String login, String password) {
		try {
			By firstNameBy = By.cssSelector(".o_sel_registration_firstName input[type='text']");
			OOGraphene.waitElement(firstNameBy, browser).sendKeys(firstName);
			
			By lastNameBy = By.cssSelector(".o_sel_registration_lastName input[type='text']");
			browser.findElement(lastNameBy).sendKeys(lastName);
			
			By loginBy = By.cssSelector(".o_sel_registration_login input[type='text']");
			browser.findElement(loginBy).sendKeys(login);
			
			By cred1By = By.cssSelector(".o_sel_registration_cred1 input[type='password']");
			browser.findElement(cred1By).sendKeys(password);
			By cred2By = By.cssSelector(".o_sel_registration_cred2 input[type='password']");
			browser.findElement(cred2By).sendKeys(password);
			OOGraphene.waitBusy(browser);
			OOGraphene.finishStep(browser, false);
		} catch (Exception e) {
			OOGraphene.takeScreenshot("Finalize registration", browser);
			throw e;
		}
	}
}
