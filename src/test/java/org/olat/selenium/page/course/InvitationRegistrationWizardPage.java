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
package org.olat.selenium.page.course;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.dumbster.smtp.SmtpMessage;

/**
 * 
 * Initial date: 16 déc. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InvitationRegistrationWizardPage {
	
	private WebDriver browser;
	
	public InvitationRegistrationWizardPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public String extractLink(SmtpMessage message) {
		String body = message.getBody();
		int index = body.indexOf("http");
		if(index >= 0) {
			body = body.replace("\r", "");
			int lastIndex = body.indexOf('\n', index + 1);
			String link = body.substring(index, lastIndex);
			link = link.replace("=", "").replace("3D", "=");
			int spaceIndex = link.indexOf(' ');
			// Check if they are 2 links because of the HTML link
			if(spaceIndex > 0 && link.lastIndexOf("http") != link.indexOf("http")) {
				link = link.substring(0, spaceIndex);
			}
			
			return link;
		}
		return null;
	}
	
	public InvitationRegistrationWizardPage loadRegistrationLink(String link) {
		browser.navigate().to(link);
		return this;
	}
	
	public InvitationRegistrationWizardPage selectLanguage() {
		By languageBy = By.cssSelector(".modal-body fieldset select#o_fioselect_language_SELBOX");
		WebElement languageEl = OOGraphene.waitElement(languageBy, browser);	
		new Select(languageEl).selectByValue("de");
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public InvitationRegistrationWizardPage nextToDisclaimer() {
		try {
			OOGraphene.nextStep(browser);
			// wait disclaimer
			By disclaimerBy = By.cssSelector("fieldset.o_disclaimer");
			OOGraphene.waitElement(disclaimerBy, browser);
		} catch (Exception e) {
			OOGraphene.takeScreenshot("Registration disclaimer", browser);
			throw e;
		}
		return this;
	}
	
	public InvitationRegistrationWizardPage acknowledgeDisclaimer() {
		By disclaimerBy = By.cssSelector(".o_form input[name='acknowledge_checkbox']");
		WebElement disclaimerEl = browser.findElement(disclaimerBy);
		OOGraphene.check(disclaimerEl, Boolean.TRUE);
		return this;
	}
	
	public InvitationRegistrationWizardPage nextToValidateEmail() {
		OOGraphene.nextStep(browser);
		
		By cred1By = By.cssSelector(".o_wizard_steps_current .o_sel_registration_email");
		OOGraphene.waitElement(cred1By, browser);
		return this;
	}
	
	public InvitationRegistrationWizardPage sendValidationToken() {
		By sendValidationBy = By.cssSelector(".o_wizard_steps_current div.o_sel_registration_validate_form a.btn.btn-primary");
		OOGraphene.waitElement(sendValidationBy, browser).click();
		return this;
	}
	
	public InvitationRegistrationWizardPage validate(String otp) {
		By otpBy = By.cssSelector(".o_wizard_steps_current .o_sel_registration_otp input[type='text']");
		OOGraphene.waitElement(otpBy, browser);
		browser.findElement(otpBy).sendKeys(otp);
		
		By validatedBy = By.cssSelector(".o_sel_registration_otp .o_success_with_icon");
		OOGraphene.waitElement(validatedBy, browser);
		return this;
	}
	
	public InvitationRegistrationWizardPage nextToPassword() {
		OOGraphene.nextStep(browser);
		
		By cred1By = By.className("o_sel_registration_cred1");
		OOGraphene.waitElement(cred1By, browser);
		return this;
	}
	
	public void finalizeRegistration(String password) {
		By cred1By = By.cssSelector(".o_sel_registration_cred1 input[type='password']");
		browser.findElement(cred1By).sendKeys(password);
		By cred2By = By.cssSelector(".o_sel_registration_cred2 input[type='password']");
		browser.findElement(cred2By).sendKeys(password);
		
		OOGraphene.finishStep(browser);
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
}
