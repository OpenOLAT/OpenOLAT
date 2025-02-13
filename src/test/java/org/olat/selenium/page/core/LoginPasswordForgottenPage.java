/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.selenium.page.core;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 12 févr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LoginPasswordForgottenPage {
	
	private final WebDriver browser;
	
	public LoginPasswordForgottenPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public LoginPasswordForgottenPage userIdentification(String emailAddress) {
		By nameBy = By.cssSelector(".modal-content .o_form .o_sel_pw_change input[type='text']");
		OOGraphene.waitElement(nameBy, browser);
		browser.findElement(nameBy).sendKeys(emailAddress);
		OOGraphene.nextStep(browser);
		
		By otpBy = By.cssSelector(".o_wizard_steps_current .o_sel_registration_otp");
		OOGraphene.waitElement(otpBy, browser);
		return this;
	}
	
	public LoginPasswordForgottenPage confirmOtp(String otp) {
		By otpBy = By.cssSelector(".o_wizard_steps_current .o_sel_registration_otp input[type='text']");
		OOGraphene.waitElement(otpBy, browser);
		browser.findElement(otpBy).sendKeys(otp);
		
		By validatedBy = By.cssSelector(".o_sel_registration_otp .o_success_with_icon");
		OOGraphene.waitElement(validatedBy, browser);
		OOGraphene.nextStep(browser);
		
		By newPasswordBy = By.cssSelector(".modal-content fieldset.o_sel_new_password_form");
		OOGraphene.waitElement(newPasswordBy, browser);
		return this;
	}
	
	public LoginPasswordForgottenPage newPassword(String value) {
		try {
			By newBy = By.cssSelector(".o_sel_new_password_form .o_sel_new_password input[type='password']");
			OOGraphene.waitElement(newBy, browser);
			browser.findElement(newBy).sendKeys(value);
			By repeatBy = By.cssSelector(".o_sel_new_password_form .o_sel_password_confirmation input[type='password']");
			browser.findElement(repeatBy).sendKeys(value);
			OOGraphene.nextStep(browser);
			
			By confirmationBy = By.cssSelector("fieldset.o_sel_changed_confirmation");
			OOGraphene.waitElement(confirmationBy, browser);
			OOGraphene.finishStep(browser);
		} catch (Exception e) {
			OOGraphene.takeScreenshot("Forgotten password", browser);
			throw e;
		}
		return this;
	}
	

}
