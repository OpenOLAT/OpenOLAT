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

}
