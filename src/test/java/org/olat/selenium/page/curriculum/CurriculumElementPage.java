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
package org.olat.selenium.page.curriculum;

import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 7 mai 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumElementPage {
	
	private final WebDriver browser;
	
	public CurriculumElementPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public CurriculumElementPage assertOnImplementationDetails() {
		By implementationBy = By.cssSelector(".o_curriculum_title .o_curriculum_avatar_icon i.o_icon_curriculum_implementation_avatar");
		OOGraphene.waitElement(implementationBy, browser);
		return this;
	}
	
	public CurriculumElementResourcePage openResourcesTab() {
		By composerBy = By.xpath("//div[div[contains(@class,'o_curriculum_title')]]//ul/li[@class='o_sel_curriculum_resources']/a");
		OOGraphene.waitElement(composerBy, browser).click();
		
		return new CurriculumElementResourcePage(browser).assertOnResourcesList();
	}
	
	public CurriculumElementMembersPage openMembersTab() {
		By composerBy = By.xpath("//div[div[contains(@class,'o_curriculum_title')]]//ul/li[@class='o_sel_curriculum_members']/a");
		OOGraphene.waitElement(composerBy, browser).click();
		
		return new CurriculumElementMembersPage(browser).assertOnMembersList();
	}
	
	public CurriculumElementOffersPage openOffersTab() {
		By composerBy = By.xpath("//div[div[contains(@class,'o_curriculum_title')]]//ul/li[@class='o_sel_curriculum_offers']/a");
		OOGraphene.waitElement(composerBy, browser).click();
		
		return new CurriculumElementOffersPage(browser).assertOnAccessConfiguration();
	}
	
	public CurriculumElementPage changeStatus(CurriculumElementStatus status) {
		By statusBy = By.xpath("//div[contains(@class,'o_curriculum_dashboard_title')]//a[contains(@class,'o_curriculum_status_')][contains(@class,'o_labeled')]");
		OOGraphene.waitElement(statusBy, browser).click();
		
		By menuBy = By.cssSelector("div.o_curriculum_dashboard_title ul.dropdown-menu.o_with_labeled");
		OOGraphene.waitElement(menuBy, browser);
		
		By newStatusBy = By.cssSelector("div.o_curriculum_dashboard_title ul.dropdown-menu>li>a.o_labeled.o_curriculum_status_" + status.name());
		OOGraphene.waitElement(newStatusBy, browser).click();
		OOGraphene.waitModalDialog(browser, "div.o_curriculum_status_change_status");
		
		By applyBy = By.cssSelector("fieldset.o_curriculum_element_change_status_form button.btn.btn-primary");
		OOGraphene.waitElement(applyBy, browser).click();
		
		OOGraphene.waitModalDialogDisappears(browser);
		
		By statusUpdatedBy = By.xpath("//div[contains(@class,'o_curriculum_dashboard_title')]//a[contains(@class,'o_curriculum_status_" + status.name() + "')][contains(@class,'o_labeled')]");
		OOGraphene.waitElement(statusUpdatedBy, browser).click();

		return this;
	}

}
