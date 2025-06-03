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
package org.olat.selenium.page.course;

import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 30 mai 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentInspectionConfigurationPage {
	
	private final WebDriver browser;
	
	public AssessmentInspectionConfigurationPage(WebDriver browser) {
		this.browser = browser;
	}
	
	/**
	 * Create a new assessment inspection base settings.
	 * 
	 * @return
	 */
	public AssessmentInspectionConfigurationPage createAssessmentInspection(String name) {
		By addBy = By.cssSelector("a.o_sel_assessment_inspection_add");
		browser.findElement(addBy).click();
		
		By generalBy = By.cssSelector("li.o_sel_inspection_general.active > a");
		OOGraphene.waitElement(generalBy, browser);
		
		By nameBy = By.cssSelector(".o_sel_assessment_inspection_general_form .o_sel_assessment_inspection_name input[type='text']");
		OOGraphene.waitElement(nameBy, browser);
		browser.findElement(nameBy).sendKeys(name);
		
		By resultsBy = By.cssSelector(".o_sel_assessment_inspection_general_form .o_sel_assessment_inspection_results input[type='checkbox'][value='metadata']");
		OOGraphene.check(browser.findElement(resultsBy), Boolean.TRUE);
		
		By saveBy = By.cssSelector(".o_sel_assessment_inspection_general_form button.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		
		// Close with  middle bread crumb
		By crumbBy = By.xpath("//li[@class='o_breadcrumb_crumb']/a[contains(@onclick,'crumb_1')]");
		OOGraphene.waitElement(crumbBy, browser);
		browser.findElement(crumbBy).click();
		return this;
	}
	
	public AssessmentInspectionConfigurationPage assertOnAssessmentInspectionInList(String name) {
		By nameBy = By.xpath("//fieldset[@class='o_sel_assessment_inspection_list']//table//tr/td[text()[contains(.,'" + name + "')]]");
		OOGraphene.waitElement(nameBy, browser);
		return this;
	}
	
	public AssessmentInspectionWizardPage addMember() {
		By addMemberBy = By.cssSelector("fieldset.o_sel_assessment_inspection_overview a.o_sel_assessment_inspection_add_member");
		OOGraphene.waitElement(addMemberBy, browser);
		browser.findElement(addMemberBy).click();
		OOGraphene.waitModalWizard(browser);
		return new AssessmentInspectionWizardPage(browser);
	}
	
	public AssessmentInspectionConfigurationPage assertActiveOnParticipantInList(UserVO user) {
		By participantBy = By.xpath("//fieldset[@class='o_sel_assessment_inspection_overview']//table//tr[td[text()[contains(.,'" + user.getFirstName() + "')]]]/td/span[contains(@class,'o_assessment_inspection_active')]");
		OOGraphene.waitElement(participantBy, browser);
		return this;
	}

}
