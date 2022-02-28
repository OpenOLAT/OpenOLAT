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

import java.util.List;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Drive the configuration of the enrolment course element.
 * 
 * Initial date: 09.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EnrollmentConfigurationPage {
	
	private final WebDriver browser;
	
	public EnrollmentConfigurationPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public EnrollmentConfigurationPage selectConfiguration() {
		By configBy = By.className("o_sel_course_en");
		return selectTab(configBy);
	}
	
	public EnrollmentConfigurationPage selectBusinessGroups() {
		By createGroupBy = By.cssSelector("a.o_form_groupchooser");
		browser.findElement(createGroupBy).click();
		OOGraphene.waitModalDialog(browser);
		
		By checkGroupsBy = By.cssSelector("div.modal-body td.o_multiselect>input[type='checkbox'][name='tb_ms']");
		List<WebElement> checkGroupEls = browser.findElements(checkGroupsBy);
		for(WebElement checkGroupEl:checkGroupEls) {
			checkGroupEl.click();
			OOGraphene.waitBusy(browser);
		}
		
		By selectBy = By.cssSelector("div.modal-body div.o_button_group button.btn.btn-primary");
		browser.findElement(selectBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	/**
	 * Open the popup to create a business group.
	 * @return
	 */
	public EnrollmentConfigurationPage createBusinessGroup(String name, String description,
			int maxParticipants, boolean waitingList, boolean auto) {
		By chooseGroupBy = By.cssSelector("a.o_form_groupcreate");
		browser.findElement(chooseGroupBy).click();
		OOGraphene.waitModalDialog(browser);
		
		//fill the form
		By nameBy = By.cssSelector(".o_sel_group_edit_title input[type='text']");
		browser.findElement(nameBy).sendKeys(name);
		OOGraphene.tinymce(description, browser);
		
		By maxParticipantBy = By.cssSelector(".o_sel_group_edit_max_members input[type='text']");
		browser.findElement(maxParticipantBy).sendKeys(Integer.toString(maxParticipants));
		
		if(waitingList) {
			By waitingListBy = By.cssSelector(".o_sel_group_edit_waiting_list input[type='checkbox']");
			browser.findElement(waitingListBy).click();
		}
		if(auto) {
			By autoBy = By.cssSelector(".o_sel_group_edit_auto_close_ranks input[type='checkbox']");
			browser.findElement(autoBy).click();
		}
		
		//save the group
		By submitBy = By.cssSelector(".o_sel_group_edit_group_form button.btn-primary");
		browser.findElement(submitBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	public EnrollmentConfigurationPage selectMultipleEnrollments(int maxEnrollmentCount) {
		By multiEnroll = By.name("allowMultipleEnroll");
		OOGraphene.waitElement(multiEnroll, browser);
		browser.findElement(multiEnroll).click();
		By maxCountBy = By.cssSelector(".o_sel_enroll_max input[type='text']");
		OOGraphene.waitElement(maxCountBy, browser);
		WebElement maxCountBox = browser.findElement(maxCountBy);
		maxCountBox.clear();
		maxCountBox.sendKeys(Integer.toString(maxEnrollmentCount));
		By saveBy = By.cssSelector(".o_sel_course_en button.btn");
		browser.findElement(saveBy).click();
		By updatedMaxCountBy = By.cssSelector(".o_sel_enroll_max input[type='text'][value='" + maxEnrollmentCount + "']");
		OOGraphene.waitElement(updatedMaxCountBy, browser);
		OOGraphene.scrollTop(browser);
		return this;
	}
	
	private EnrollmentConfigurationPage selectTab(By tabBy) {
		OOGraphene.selectTab("o_node_config", tabBy, browser);
		return this;
	}

}
