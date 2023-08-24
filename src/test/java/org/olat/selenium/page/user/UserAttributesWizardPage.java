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
import org.openqa.selenium.support.ui.Select;

/**
 * 
 * Initial date: 29 juin 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserAttributesWizardPage {
	
	private final WebDriver browser;
	
	public UserAttributesWizardPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public UserAttributesWizardPage assertOnAttributes() {
		By attributesBy = By.cssSelector("div.o_wizard_steps_current fieldset.o_sel_user_attributes");
		OOGraphene.waitElement(attributesBy, browser);
		return this;
	}
	
	public UserAttributesWizardPage changeAttribute(String attribute, String value) {
		By attributeSelectionBy = By.xpath("//fieldset[contains(@class,'o_sel_user_attributes')]//input[@type='checkbox'][@value='change" + attribute + "']");
		browser.findElement(attributeSelectionBy).click();
		
		By attributeValueBy = By.cssSelector("fieldset.o_sel_user_attributes div.o_sel_user_firstName input.o_sel_user_" + attribute + "[type='text']");
		OOGraphene.waitElement(attributeValueBy, browser);
		browser.findElement(attributeValueBy).sendKeys(value);
		return this;
	}
	
	public UserAttributesWizardPage nextToRoles() {
		OOGraphene.nextStep(browser);
		By rolesBy = By.cssSelector("fieldset.o_sel_user_roles");
		OOGraphene.waitElement(rolesBy, browser);
		return this;
	}
	
	public UserAttributesWizardPage changeRoles(String role, boolean add) {
		By attributeSelectionBy = By.cssSelector("fieldset.o_sel_user_roles div.o_sel_role_" + role + " label>input[type='checkbox']");
		browser.findElement(attributeSelectionBy).click();
		
		By addRemoveBy = By.cssSelector("fieldset.o_sel_user_roles div.o_sel_role_" + role + " select");
		OOGraphene.waitElement(addRemoveBy, browser);
		String value = add ? "add" : "remove";
		new Select(browser.findElement(addRemoveBy)).selectByValue(value);
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public UserAttributesWizardPage nextToGroups() {
		OOGraphene.nextStep(browser);
		By searchBy = By.cssSelector("fieldset.o_sel_groups_search");
		OOGraphene.waitElement(searchBy, browser);
		return this;
	}
	
	public UserAttributesWizardPage nextToOverview() {
		OOGraphene.nextStep(browser);
		By overviewBy = By.cssSelector("div.o_table_flexi.o_sel_users_overview");
		OOGraphene.waitElement(overviewBy, browser);
		return this;
	}
	
	public UserAttributesWizardPage assertOnNewAttributeOverview(String attributeValue) {
		By attributeValueBy = By.xpath("//div[contains(@class,'o_sel_users_overview')]//table//td/span[@class='o_userbulk_changedcell'][i[contains(@class,'o_icon_new')]][text()[contains(.,'" + attributeValue +"')]]");
		OOGraphene.waitElement(attributeValueBy, browser);
		return this;
	}
	
	public UserAttributesWizardPage finish() {
		OOGraphene.finishStep(browser, true);
		return this;
	}

}
