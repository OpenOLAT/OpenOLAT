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

import java.util.List;

import org.junit.Assert;
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/**
 * 
 * Drive the user import wizard.
 * 
 * Initial date: 04.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ImportUserPage {

	public static final By nextBy = By.className("o_wizard_button_next");
	public static final By finishBy = By.className("o_wizard_button_finish");
	
	private WebDriver browser;
	
	public ImportUserPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public ImportUserPage fill(String csv) {
		By importTextareaBy = By.cssSelector("div.o_wizard_steps_current_content textarea");
		WebElement importTextareaEl = browser.findElement(importTextareaBy);
		//focus
		importTextareaEl.sendKeys("");
		OOGraphene.textarea(importTextareaEl, csv, browser);
		return this;
	}
	
	/**
	 * In the first mandatory option, select the update password one.
	 * @return
	 */
	public ImportUserPage updatePasswords() {
		By updatePassword = By.id("o_fioupdate_password_SELBOX");
		Select passwordDropdown = new Select(browser.findElement(updatePassword));
		passwordDropdown.selectByValue("update");
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * In the second mandatory option, select the update existing user one.
	 * @return
	 */
	public ImportUserPage updateUsers() {
		By updatePassword = By.id("o_fioupdate_user_SELBOX");
		Select passwordDropdown = new Select(browser.findElement(updatePassword));
		passwordDropdown.selectByValue("update");
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Append a line in the form:<br/>
	 * Benutzername *	Passwort	Sprache	Vorname *	Nachname *	E-Mail *	Institution	Institutionsnummer	Institutions E-Mail<br/>
	 * demo	olat4you	de	Peter	Muster	peter.muster@openolat.org	Universität Zürich	08-123-987	peter.muster@frentix.com<br/>
	 * 
	 * 
	 * 
	 * @param username
	 * @param password
	 * @param firstName
	 * @param lastName
	 * @param sb
	 */
	public UserVO append(String username, String password, String firstName, String lastName, StringBuilder sb) {
		if(sb.length() > 0) {
			sb.append("\\n");
		}
		
		String email = username.replace("-", "") + "@frentix.com";
		String institution = "frentix GmbH";
		String institutionNumber = "034-" + System.currentTimeMillis();
		String institutionEmail = username.replace("-", "") + "@openolat.org";
		
		sb.append(username).append("	")
		  .append(password).append("	")
		  .append("de").append("	")
		  .append(firstName).append("	")
		  .append(lastName).append("	")
		  .append(email).append("	")
		  .append(institution).append("	")
		  .append(institutionNumber).append("	")
		  .append(institutionEmail);
		
		UserVO userVo = new UserVO();
		userVo.setLogin(username);
		userVo.setFirstName(firstName);
		userVo.setLastName(lastName);
		userVo.setEmail(email);
		return userVo;
	}
	
	public UserVO append(UserVO userVo, String newLastName, String password, StringBuilder sb) {
		if(sb.length() > 0) {
			sb.append("\\n");
		}
		sb.append(userVo.getLogin()).append("	")
		  .append(password).append("	")
		  .append("de").append("	")
		  .append(userVo.getFirstName()).append("	");
		if(newLastName != null) {
			sb.append(newLastName).append("	");
			userVo.setLastName(newLastName);
		} else {
			sb.append(userVo.getLastName()).append("	");
		}
		sb.append(userVo.getEmail()).append("	")
		  .append("").append("	")
		  .append("").append("	")
		  .append("");
		return userVo;
	}
	
	public ImportUserPage assertGreen(int numOfGreen) {
		By greenBy = By.cssSelector(".o_dnd_label i.o_icon_new");
		List<WebElement> greenEls = browser.findElements(greenBy);
		Assert.assertEquals(numOfGreen, greenEls.size());
		return this;
	}
	
	public ImportUserPage assertWarn(int numOfWarns) {
		By warnBy = By.cssSelector(".o_dnd_label i.o_icon_warn");
		List<WebElement> warnEls = browser.findElements(warnBy);
		Assert.assertEquals(numOfWarns, warnEls.size());
		return this;
		
	}

	public ImportUserPage nextData() {
		OOGraphene.nextStep(browser);
		OOGraphene.waitElement(By.cssSelector("fieldset.o_sel_import_users_overview"), browser);
		return this;
	}
	
	public ImportUserPage nextOverview() {
		OOGraphene.nextStep(browser);
		OOGraphene.waitElement(By.cssSelector("fieldset.o_sel_groups_search"), browser);
		return this;
	}
	
	public ImportUserPage nextGroups() {
		OOGraphene.nextStep(browser);
		OOGraphene.waitElement(By.cssSelector("fieldset.o_sel_users_import_contact"), browser);
		return this;
	}
	
	/**
	 * Finish the wizard
	 * @return this
	 */
	public ImportUserPage finish() {
		OOGraphene.finishStep(browser);
		return this;
	}

}
