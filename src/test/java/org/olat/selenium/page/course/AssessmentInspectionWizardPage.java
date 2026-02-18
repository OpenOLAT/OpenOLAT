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
 * Initial date: 2 juin 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class AssessmentInspectionWizardPage {
	
	private WebDriver browser;
	
	public AssessmentInspectionWizardPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public AssessmentInspectionWizardPage selectCourseElement(String name) {
		By selecBy = By.xpath("//div[contains(@class,'o_sel_assessment_inspection_select_node')]//table//tr[td/div/a/span[text()[contains(.,'" + name + "')]]]/td/input[@name='tb_ms'][@type='radio']");
		OOGraphene.waitElement(selecBy, browser);
		browser.findElement(selecBy).click();
		
		OOGraphene.nextStep(browser);
		OOGraphene.waitElement(By.className("o_sel_assessment_inspection_select_participants"), browser);
		return this;
	}
	
	public AssessmentInspectionWizardPage selectUser(UserVO user) {
		By selectBy = By.xpath("//div[contains(@class,'o_sel_assessment_inspection_select_participants')]//table//tr[td[text()[contains(.,'" + user.getFirstName() + "')]]]/td/input[@name='tb_ms'][@type='checkbox']");
		OOGraphene.waitElement(selectBy, browser);
		browser.findElement(selectBy).click();
		
		OOGraphene.nextStep(browser);
		OOGraphene.waitElement(By.className("o_sel_assessment_inspection_configuration"), browser);
		return this;
	}
	
	public AssessmentInspectionWizardPage configuration(int startHour, int startMinute) {
		String firstDateMsXpath = "//fieldset[contains(@class,'o_sel_assessment_inspection_configuration')]//div[contains(@class,'o_date_ms')][contains(@class,'o_first_ms')]/input[@type='text']";
		By startHourBy = By.xpath(firstDateMsXpath + "[1]");
		OOGraphene.clearAndSendKeys(startHourBy, Integer.toString(startHour), browser);
		By startMinuteBy = By.xpath(firstDateMsXpath + "[2]");
		OOGraphene.clearAndSendKeys(startMinuteBy, Integer.toString(startMinute), browser);

		OOGraphene.tab(browser);
		OOGraphene.waitingALittleBit();
		
		OOGraphene.nextStep(browser);
		OOGraphene.waitingALittleBit();
		OOGraphene.nextStep(browser);
		
		OOGraphene.waitElement(By.className("o_sel_contact_form"), browser);
		return this;
	}
	
	public AssessmentInspectionWizardPage contact() {
		OOGraphene.waitElement(By.className("o_sel_contact_form"), browser);
		OOGraphene.finishStep(browser, false);
		return this;
	}
}
