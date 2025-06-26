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

import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 8 mai 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumElementMembersPage {
	
	private final WebDriver browser;
	
	public CurriculumElementMembersPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public CurriculumElementMembersPage assertOnMembersList() {
		By resourcesBy = By.cssSelector("div.o_sel_curriculum_element_members");
		OOGraphene.waitElement(resourcesBy, browser);
		return this;
	}
	
	public CurriculumElementMembersPage activeScope() {
		By pendingScopeBy = By.xpath("//ul[contains(@class,'o_scopes')]//button[div/span/div/div/i[contains(@class,'o_icon_circle_check')]]");
		OOGraphene.waitElement(pendingScopeBy, browser).click();
		OOGraphene.waitElement(By.className("o_sel_curriculum_element_members"), browser);
		return this;
	}
	
	public CurriculumElementMembersPage assertOnMemberInList(UserVO user) {
		By memberBy = By.xpath("//div[contains(@class,'o_sel_curriculum_element_members')]//table//td/a[text()[contains(.,'" + user.getFirstName() + "')]]");
		OOGraphene.waitElement(memberBy, browser);
		return this;
	}
	
	public CurriculumElementMembersWizardPage addMember() {
		By addMemberBy = By.cssSelector(".o_sel_curriculum_element_members a.o_sel_curriculum_element_add_member");
		OOGraphene.waitElement(addMemberBy, browser).click();
		
		OOGraphene.waitModalWizard(browser);
		return new CurriculumElementMembersWizardPage(browser);
	}
	
	public CurriculumElementMembersPage pendingScope() {
		By pendingScopeBy = By.xpath("//ul[contains(@class,'o_scopes')]//button[div/span/div/div/i[contains(@class,'o_icon_timelimit_half')]]");
		OOGraphene.waitElement(pendingScopeBy, browser).click();
		OOGraphene.waitElement(By.className("o_sel_curriculum_element_pending_members"), browser);
		return this;
	}
	
	public CurriculumElementMembersPage assertOnPendingMemberInList(UserVO user) {
		By memberBy = By.xpath("//div[contains(@class,'o_sel_curriculum_element_pending_members')]//table//td/a[text()[contains(.,'" + user.getFirstName() + "')]]");
		OOGraphene.waitElement(memberBy, browser);
		return this;
	}
	
	public CurriculumElementMembersPage acceptPendingMemberInList(UserVO user) {
		By toolsBy = By.xpath("//div[contains(@class,'o_sel_curriculum_element_pending_members')]//table//tr[td/a[text()[contains(.,'" + user.getFirstName() + "')]]]/td/div/a[i[contains(@class,'o_icon_actions')]]");
		OOGraphene.waitElement(toolsBy, browser).click();
		
		OOGraphene.waitCallout(browser, "ul.o_dropdown a>i.o_icon_accepted");
		By acceptBy = By.xpath("//ul[contains(@class,'o_dropdown')]/li/a[i[contains(@class,'o_icon_accepted')]]");
		OOGraphene.waitElement(acceptBy, browser).click();
		
		OOGraphene.waitModalDialog(browser, "div.o_sel_accept_cancel_membership");
		
		By confirmBy = By.cssSelector("dialog div.o_sel_accept_cancel_membership .o_button_group button.btn.btn-primary");
		browser.findElement(confirmBy).click();
		
		OOGraphene.waitModalDialogDisappears(browser);
		
		return this;
	}

}
