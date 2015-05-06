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

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.List;

import org.junit.Assert;
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

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
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(csv), null);
		if(Platform.MAC.equals(Platform.getCurrent())) {
			importTextareaEl.sendKeys(Keys.COMMAND + "v");
		} else {
			importTextareaEl.sendKeys(Keys.CONTROL + "v");
		}
		return this;
	}
	
	/**
	 * Append a line in the form:<br/>
	 * Benutzername *	Passwort	Sprache	Vorname *	Nachname *	E-Mail *	Institution	Institutionsnummer	Institutions E-Mail<br/>
	 * demo	olat4you	de	Peter	Muster	peter.muster@demo.ch	Universität Zürich	08-123-987	peter.muster@uzh.ch<br/>
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
		  .append(institutionEmail).append('\n');
		
		UserVO userVo = new UserVO();
		userVo.setLogin(username);
		userVo.setFirstName(firstName);
		userVo.setLastName(lastName);
		userVo.setEmail(email);
		return userVo;
	}
	
	public ImportUserPage assertGreen(int numOfGreen) {
		By greenBy = By.cssSelector(".o_dnd_label i.o_icon_new");
		List<WebElement> greenEls = browser.findElements(greenBy);
		Assert.assertEquals(numOfGreen, greenEls.size());
		return this;
	}
	
	/**
	 * Next
	 * @return this
	 */
	public ImportUserPage next() {
		WebElement next = browser.findElement(nextBy);
		Assert.assertTrue(next.isDisplayed());
		Assert.assertTrue(next.isEnabled());
		next.click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Finish the wizard
	 * @return this
	 */
	public ImportUserPage finish() {
		WebElement finish = browser.findElement(finishBy);
		Assert.assertTrue(finish.isDisplayed());
		Assert.assertTrue(finish.isEnabled());
		finish.click();
		OOGraphene.waitBusy(browser);
		OOGraphene.closeBlueMessageWindow(browser);
		return this;
	}

}
