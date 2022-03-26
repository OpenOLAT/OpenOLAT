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

import java.io.File;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Drive the configuration of the course element of type group task.
 * 
 * Initial date: 03.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GroupTaskConfigurationPage {

	private final WebDriver browser;
	
	public GroupTaskConfigurationPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public GroupTaskConfigurationPage selectWorkflow() {
		return selectTab("o_sel_gta_workflow", By.className("o_sel_course_gta_steps"));
	}
	
	public GroupTaskConfigurationPage optional(boolean optional) {
		By optionalBy;
		if(optional) {
			optionalBy = By.cssSelector("div#o_coobligation input[type='radio'][value='optional']");
		} else {
			optionalBy = By.cssSelector("div#o_coobligation input[type='radio'][value='mandatory']");
		}
		OOGraphene.waitElement(optionalBy, browser);
		OOGraphene.check(browser.findElement(optionalBy), Boolean.TRUE);
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public GroupTaskConfigurationPage enableAssignment(boolean enable) {
		return enableStep("task.assignment", enable);
	}
	
	public GroupTaskConfigurationPage enableSubmission(boolean enable) {
		return enableStep("submission", enable);
	}
	
	public GroupTaskConfigurationPage enableReview(boolean enable) {
		return enableStep("review", enable);
	}
	
	public GroupTaskConfigurationPage enableRevision(boolean enable) {
		return enableStep("revision", enable);
	}
	
	public GroupTaskConfigurationPage enableSolution(boolean enable) {
		return enableStep("sample", enable);
	}
	
	public GroupTaskConfigurationPage enableGrading(boolean enable) {
		return enableStep("grading", enable);
	}
	
	private GroupTaskConfigurationPage enableStep(String name, boolean enable) {
		By checkboxStepBy = By.xpath("//fieldset[contains(@class,'o_sel_course_gta_steps')]//label/input[@name='" + name + "']");
		WebElement checkboxEl = browser.findElement(checkboxStepBy);
		OOGraphene.check(checkboxEl, Boolean.valueOf(enable));
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public GroupTaskConfigurationPage enableSolutionForAll(boolean forAll) {
		By optionBy;
		if(forAll) {
			optionBy = By.xpath("//div[@id='o_covisibleall']//label/input[@name='visibleall'][@value='all']");
		} else {
			optionBy = By.xpath("//div[@id='o_covisibleall']//label/input[@name='visibleall'][@value='restricted']");
		}
		OOGraphene.waitElement(optionBy, browser);
		browser.findElement(optionBy).click();;
		return this;
	}
	
	public GroupTaskConfigurationPage saveWorkflow() {
		By saveBy = By.cssSelector(".o_sel_course_gta_save_workflow button.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public GroupTaskConfigurationPage openBusinessGroupChooser() {
		By chooseGroupBy = By.cssSelector("a.o_form_groupchooser");
		browser.findElement(chooseGroupBy).click();
		OOGraphene.waitModalDialog(browser);
		return this;
	}
	
	public GroupTaskConfigurationPage createBusinessGroup(String name) {
		By createGroupBy = By.cssSelector("div.o_button_group_right a");
		browser.findElement(createGroupBy).click();
		OOGraphene.waitModalDialog(browser);
		
		//fill the form
		By nameBy = By.cssSelector(".o_sel_group_edit_title input[type='text']");
		browser.findElement(nameBy).sendKeys(name);
		OOGraphene.tinymce("-", browser);
		
		//save the group
		By submitBy = By.cssSelector(".o_sel_group_edit_group_form button.btn-primary");
		WebElement submitButton = browser.findElement(submitBy);
		submitButton.click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public GroupTaskConfigurationPage confirmBusinessGroupsSelection() {
		By saveBy = By.cssSelector(".o_sel_group_selection_groups button.btn-primary");
		WebElement saveButton = browser.findElement(saveBy);
		saveButton.click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public GroupTaskConfigurationPage selectAssessment() {
		return selectTab("o_sel_gta_assessment", By.className("o_sel_course_ms_form"));
	}
	
	public GroupTaskConfigurationPage selectAssignment() {
		return selectTab("o_sel_gta_assignment", By.className("o_sel_course_gta_tasks"));
	}
	
	public GroupTaskConfigurationPage selectSolution() {
		return selectTab("o_sel_gta_solution", By.className("o_sel_course_gta_solutions"));
	}
	
	public GroupTaskConfigurationPage uploadTask(String title, File file) {
		By addTaskBy = By.className("o_sel_course_gta_add_task");
		browser.findElement(addTaskBy).click();
		OOGraphene.waitModalDialog(browser);
		
		By titleBy = By.cssSelector(".o_sel_course_gta_upload_task_title input[type='text']");
		browser.findElement(titleBy).sendKeys(title);
		
		By inputBy = By.cssSelector(".o_fileinput input[type='file']");
		OOGraphene.uploadFile(inputBy, file, browser);
		OOGraphene.waitBusy(browser);
		By uploadedBy = By.cssSelector(".o_sel_course_gta_upload_task_form .o_sel_file_uploaded");
		OOGraphene.waitElement(uploadedBy, browser);
		
		//save
		By saveBy = By.cssSelector(".o_sel_course_gta_upload_task_form button.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	public GroupTaskConfigurationPage enableAutoAssignment(boolean enable) {
		//task.assignment.type
		String type = enable ? "auto" : "manual";
		By typeBy = By.xpath("//fieldset[contains(@class,'o_sel_course_gta_task_config_form')]//input[@name='task.assignment.type'][@value='" + type + "']");
		OOGraphene.check(browser.findElement(typeBy), Boolean.TRUE);
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public GroupTaskConfigurationPage saveTasks() {
		By saveBy = By.cssSelector(".o_sel_course_gta_task_config_buttons button.btn-primary");
		OOGraphene.waitElement(saveBy, browser);
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public GroupTaskConfigurationPage uploadSolution(String title, File file) {
		By addTaskBy = By.className("o_sel_course_gta_add_solution");
		browser.findElement(addTaskBy).click();
		OOGraphene.waitBusy(browser);
		
		By titleBy = By.cssSelector(".o_sel_course_gta_upload_solution_title input[type='text']");
		browser.findElement(titleBy).sendKeys(title);
		
		By inputBy = By.cssSelector(".o_fileinput input[type='file']");
		OOGraphene.uploadFile(inputBy, file, browser);
		OOGraphene.waitBusy(browser);
		By uploadedBy = By.cssSelector(".o_sel_course_gta_upload_solution_form .o_sel_file_uploaded");
		OOGraphene.waitElement(uploadedBy, browser);
		
		//save
		By saveBy = By.cssSelector(".o_sel_course_gta_upload_solution_form button.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	public GroupTaskConfigurationPage setAssessmentOptions(Float minVal, Float maxVal, Float cutVal) {
		new AssessmentCEConfigurationPage(browser).setScoreAuto(minVal, maxVal, cutVal);
		return this;
	}
	
	public GroupTaskConfigurationPage saveAssessmentOptions() {
		new AssessmentCEConfigurationPage(browser).saveAssessmentOptions();
		return this;
	}
	
	private GroupTaskConfigurationPage selectTab(String tabCssClass, By panelBy) {
		By tabBy = By.cssSelector("ul.o_node_config li." + tabCssClass + ">a");
		OOGraphene.waitElement(tabBy, browser);
		browser.findElement(tabBy).click();
		OOGraphene.waitElement(panelBy, browser);
		return this;
	}
}
