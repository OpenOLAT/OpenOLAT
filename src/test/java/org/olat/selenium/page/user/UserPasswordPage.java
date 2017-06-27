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

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * To drive the password change page.
 * 
 * Initial date: 26.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserPasswordPage {
	

	public static final By oldPasswordBy = By.className("o_sel_home_pwd_old");
	
	private final WebDriver browser;
	
	private UserPasswordPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public static UserPasswordPage getUserPasswordPage(WebDriver browser) {
		OOGraphene.waitElement(oldPasswordBy, browser);
		return new UserPasswordPage(browser);
	}
	
	public UserPasswordPage setNewPassword(String oldPassword, String newPassword) {
		//fill the form
		By oldPasswordInputBy = By.cssSelector("div.o_sel_home_pwd_old input");
		WebElement oldPasswordEl = browser.findElement(oldPasswordInputBy);
		oldPasswordEl.sendKeys(oldPassword);
		By newPasswordBy = By.cssSelector("div.o_sel_home_pwd_new_1 input");
		WebElement newPasswordEl = browser.findElement(newPasswordBy);
		newPasswordEl.sendKeys(newPassword);
		By passwordConfirmationBy = By.cssSelector("div.o_sel_home_pwd_new_2 input");
		WebElement passwordConfirmationEl = browser.findElement(passwordConfirmationBy);
		passwordConfirmationEl.sendKeys(newPassword);
		//save it
		By saveButtonBy = By.cssSelector("div.form-inline.o_sel_home_pwd_buttons button");
		WebElement saveButton = browser.findElement(saveButtonBy);
		saveButton.click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitAndCloseBlueMessageWindow(browser);
		return this;
	}

}
