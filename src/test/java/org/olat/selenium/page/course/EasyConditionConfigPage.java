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
package org.olat.selenium.page.course;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/**
 * 
 * Initial date: 24.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EasyConditionConfigPage {
	
	private WebDriver browser;
	
	public EasyConditionConfigPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public EasyConditionConfigPage setCoachExclusive() {
		By coacheExclsuiveBy = By.cssSelector("input[name='coachExclusive'][type='checkbox']");
		browser.findElement(coacheExclsuiveBy).click();
		By dateDisabledBy = By.xpath("//input[@name='dateSwitch'][@value='ison'][@disabled='disabled']");
		OOGraphene.waitElement(dateDisabledBy, browser);
		return this;
	}
	
	public EasyConditionConfigPage setGroupCondition() {
		By groupSwitchBy = By.cssSelector("input[name='groupSwitch'][type='checkbox']");
		browser.findElement(groupSwitchBy).click();
		By createBy = By.cssSelector("a.o_sel_condition_create_groups");
		OOGraphene.waitElement(createBy, browser);
		return this;
	}
	
	public EasyConditionConfigPage createBusinessGroup(String name) {
		By createBy = By.cssSelector("a.o_sel_condition_create_groups");
		browser.findElement(createBy).click();
		OOGraphene.waitModalDialog(browser);
		
		//fill the form
		By nameBy = By.cssSelector(".o_sel_group_edit_title input[type='text']");
		WebElement nameEl = browser.findElement(nameBy);
		nameEl.sendKeys(name);

		//save
		By submitBy = By.cssSelector(".o_sel_group_edit_group_form button.btn-primary");
		browser.findElement(submitBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		
		return this;
	}
	
	public EasyConditionConfigPage setAssessmentCondition(int nodeIndex) {
		By assessmentSwitchBy = By.cssSelector("input[name='assessmentSwitch'][type='checkbox']");
		browser.findElement(assessmentSwitchBy).click();
		OOGraphene.waitBusy(browser);
		
		By assessmentPassedTypeSwitchBy = By.cssSelector("input[name='assessmentTypeSwitch'][type='radio']");
		browser.findElement(assessmentPassedTypeSwitchBy).click();
		OOGraphene.waitBusy(browser);
		
		By selectBy = By.cssSelector("fieldset.o_sel_course_visibility_condition_form select");
		new Select(browser.findElement(selectBy)).selectByIndex(nodeIndex);
		OOGraphene.waitBusy(browser);
		
		return this;
	}
	
	public EasyConditionConfigPage save() {
		By saveBy = By.cssSelector("fieldset.o_sel_course_visibility_condition_form button.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
}