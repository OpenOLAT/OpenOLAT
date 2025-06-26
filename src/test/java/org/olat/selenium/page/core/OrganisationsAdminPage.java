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
 * Initial date: 17 juin 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrganisationsAdminPage {
	
	private WebDriver browser;
	
	public OrganisationsAdminPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public OrganisationsAdminPage assertOnAdminConfiguration() {
		By enableBy = By.cssSelector(".o_sel_org_admin_configuration");
		OOGraphene.waitElement(enableBy, browser);
		return this;
	}
	
	public OrganisationsAdminPage openOrganisationsList() {
		By organisationsListBy = By.cssSelector("div.o_segments a.o_sel_org_organisations_list");
		OOGraphene.waitElement(organisationsListBy, browser).click();
		
		By listBy = By.cssSelector("fieldset.o_sel_org_organisations_list table");
		OOGraphene.waitElement(listBy, browser);
		return this;
	}
	
	public OrganisationPage editOrganisation(String name) {
		By toolsBy = By.xpath("//table//tr[td/div/a[text()[contains(.,'" + name + "')]]]/td[contains(@class,'o_col_action')]/div/a[i[contains(@class,'o_icon_actions')]]");
		OOGraphene.waitElement(toolsBy, browser).click();
		OOGraphene.waitCallout(browser);
		
		By editBy = By.xpath("//dialog//ul[contains(@class,'o_dropdown')]//a[contains(@onclick,'edit')][i[contains(@class,'o_icon_edit')]]");
		OOGraphene.waitElement(editBy, browser).click();
		
		return new OrganisationPage(browser).assertOnMetadata();
	}
}
