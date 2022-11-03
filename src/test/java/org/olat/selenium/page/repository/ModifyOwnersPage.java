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
package org.olat.selenium.page.repository;

import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.selenium.page.group.MembersWizardPage;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 2 nov. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ModifyOwnersPage {
	
	private final WebDriver browser;
	
	public ModifyOwnersPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public ModifyOwnersPage nextRemoveOwners() {
		OOGraphene.nextStep(browser);
		OOGraphene.waitElement(By.cssSelector("fieldset.o_sel_usersearch_searchform"), browser);
		return this;
	}
	
	/**
	 * Search and select a user to add as owner. Selection trigger next step
	 * in wizard.
	 * 
	 * @param user The user to find
	 * @param admin If the user in browser as admin rights to work with user name
	 * @return Itself
	 */
	public ModifyOwnersPage searchUserAndAdd(UserVO user, boolean admin) {
		new MembersWizardPage(browser)
			.searchOneMember(user, admin);
		OOGraphene.waitElement(By.cssSelector("div.o_sel_modify_owners_review table"), browser);
		return this;
	}
	
	public ModifyOwnersPage assertAddOwner(UserVO user) {
		By addBy = By.xpath("//div[contains(@class,'o_sel_modify_owners_review')]//table//tr[td/div[text()[contains(.,'" + user.getFirstName() + "')]]]/td/span[contains(@class,'o_badge_added')]");
		OOGraphene.waitElement(addBy, browser);
		return this;
	}
	
	public ModifyOwnersPage assertRemoveOwner(UserVO user) {
		By removedBy = By.xpath("//div[contains(@class,'o_sel_modify_owners_review')]//table//tr[td/div[text()[contains(.,'" + user.getFirstName() + "')]]]/td/span[contains(@class,'o_badge_removed')]");
		OOGraphene.waitElement(removedBy, browser);
		return this;
	}
	
	public ModifyOwnersPage removeOwner(UserVO user) {
		By selectBy = By.xpath("//div[contains(@class,'modal')]//div[contains(@class,'o_table_flexi')]//table//tr[td[text()[contains(.,'" + user.getFirstName() + "')]]]/td/input[@name='tb_ms']");
		OOGraphene.waitElement(selectBy, browser);
		browser.findElement(selectBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public ModifyOwnersPage nextAddOwners() {
		OOGraphene.nextStep(browser);
		OOGraphene.waitElement(By.cssSelector("div.o_sel_modify_owners_review table"), browser);
		return this;
	}
	
	public ModifyOwnersPage nextReview() {
		OOGraphene.nextStep(browser);
		OOGraphene.waitElement(By.cssSelector("fieldset.o_sel_users_import_contact"), browser);
		return this;
	}
	
	public void finish() {
		OOGraphene.finishStep(browser);
	}

}
