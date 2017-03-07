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
package org.olat.selenium.page.group;

import java.util.List;

import org.junit.Assert;
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Drive the wizard to add members
 * 
 * Initial date: 03.07.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MembersWizardPage {

	public static final By nextBy = By.className("o_wizard_button_next");
	public static final By finishBy = By.className("o_wizard_button_finish");
	
	private WebDriver browser;
	
	public MembersWizardPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public MembersWizardPage next() {
		WebElement next = browser.findElement(nextBy);
		Assert.assertTrue(next.isDisplayed());
		Assert.assertTrue(next.isEnabled());
		next.click();
		OOGraphene.waitBusy(browser);
		OOGraphene.closeBlueMessageWindow(browser);
		return this;
	}
	
	public MembersWizardPage finish() {
		WebElement finish = browser.findElement(finishBy);
		Assert.assertTrue(finish.isDisplayed());
		Assert.assertTrue(finish.isEnabled());
		finish.click();
		OOGraphene.waitBusy(browser);
		OOGraphene.closeBlueMessageWindow(browser);
		return this;
	}
	
	/**
	 * Search member and select them
	 * @param user
	 * @return
	 */
	public MembersWizardPage searchMember(UserVO user, boolean admin) {
		//Search by username
		By usernameBy = By.cssSelector(".o_sel_usersearch_searchform input[type='text']");
		OOGraphene.waitElement(usernameBy, browser);
		
		List<WebElement> searchFields = browser.findElements(usernameBy);
		Assert.assertFalse(searchFields.isEmpty());
		String search = admin ? user.getLogin() : user.getFirstName();
		searchFields.get(0).sendKeys(search);

		By searchBy = By.cssSelector(".o_sel_usersearch_searchform a.btn-default");
		WebElement searchButton = browser.findElement(searchBy);
		searchButton.click();
		OOGraphene.waitBusy(browser);
		
		//check
		By checkAllBy = By.cssSelector("div.modal div.o_table_wrapper input[type='checkbox']");
		OOGraphene.waitElement(checkAllBy, 5, browser);
		List<WebElement> checkAll = browser.findElements(checkAllBy);
		Assert.assertFalse(checkAll.isEmpty());
		for(WebElement check:checkAll) {
			check.click();
		}
		return this;
	}
	
	public MembersWizardPage setMembers(UserVO... users) {
		StringBuilder sb = new StringBuilder();
		for(UserVO user:users) {
			if(sb.length() > 0) sb.append("\\n");
			sb.append(user.getLogin());
		}
		By importAreaBy = By.cssSelector(".modal-content textarea");
		WebElement importAreaEl = browser.findElement(importAreaBy);
		OOGraphene.textarea(importAreaEl, sb.toString(), browser);
		return this;
	}
	
	public MembersWizardPage selectRepositoryEntryRole(boolean owner, boolean coach, boolean participant) {
		if(owner) {
			By ownerBy = By.cssSelector("label input[name='repoRights'][type='checkbox'][value='owner']");
			WebElement ownerEl = browser.findElement(ownerBy);
			OOGraphene.check(ownerEl, new Boolean(owner));
			OOGraphene.waitBusy(browser);
		}
		
		if(coach) {
			By coachBy = By.cssSelector("label input[name='repoRights'][type='checkbox'][value='tutor']");
			WebElement coachEl = browser.findElement(coachBy);
			OOGraphene.check(coachEl, new Boolean(coach));
			OOGraphene.waitBusy(browser);
		}
		
		if(participant) {
			By participantBy = By.cssSelector("label input[name='repoRights'][type='checkbox'][value='participant']");
			WebElement participantEl = browser.findElement(participantBy);
			OOGraphene.check(participantEl, new Boolean(participant));
			OOGraphene.waitBusy(browser);
		}
		return this;
	}
	
	public MembersWizardPage selectGroupAsParticipant(String groupName) {
		By rolesBy = By.xpath("//div[contains(@class,'o_table_wrapper')]//table//tr[td[text()='" + groupName + "']]//label[contains(@class,'o_sel_role_participant')]/input");
		List<WebElement> roleEls = browser.findElements(rolesBy);
		roleEls.get(0).click();
		return this;
	}
}
