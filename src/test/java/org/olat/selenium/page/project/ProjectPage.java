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
package org.olat.selenium.page.project;

import org.olat.core.util.StringHelper;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 21 août 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ProjectPage {
	
	private final WebDriver browser;
	
	public ProjectPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public ProjectPage fillAndSaveProject(String title, String organisation, String teaser) {
		By titleBy = By.cssSelector("fieldset.o_sel_proj_project_form input.o_sel_proj_project_title[type='text']");
		OOGraphene.waitElement(titleBy, browser).sendKeys(title);
		
		By organisationBy = By.cssSelector("div.o_sel_proj_project_organisation button.o_selection_display");
		browser.findElement(organisationBy).click();
		OOGraphene.waitCallout(browser, ".o_object_selection");
		
		By openOlatOrgBy = By.xpath("//div[contains(@class,'popover')]//div[@class='o_object_selection']//label[div/div/div[contains(text(),'" + organisation + "')]]/input[@type='checkbox']");
		WebElement openOlatOrgEl = browser.findElement(openOlatOrgBy);
		OOGraphene.check(openOlatOrgEl, Boolean.TRUE);

		By updateBy = By.xpath("//div[contains(@class,'popover')]//div[@class='o_object_selection']//span[contains(@class,'o_expand_button_text')][text()[contains(.,'(1)')]]");
		OOGraphene.waitElement(updateBy, browser);
		
		By transferBy = By.cssSelector(".popover .o_object_selection a.o_object_selection_apply"); 
		OOGraphene.waitElement(transferBy, browser).click();
		OOGraphene.waitElementDisappears(openOlatOrgBy, 5, browser);
		
		if(StringHelper.containsNonWhitespace(teaser)) {
			By teaserBy = By.cssSelector("fieldset.o_sel_proj_project_form input.o_sel_proj_project_teaser[type='text']");
			OOGraphene.waitElement(teaserBy, browser).sendKeys(teaser);
		}
		
		By saveBy = By.cssSelector("fieldset.o_sel_proj_project_form button.btn.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	public ProjectPage assertOnDashboard(String title) {
		By titleBy = By.xpath("//div[contains(@class,'o_proj_dashboard')]/div[contains(@class,'o_proj_title')]//h2[@class='o_proj_dashboard_title'][text()[contains(.,'" + title + "')]]");
		OOGraphene.waitElement(titleBy, browser);
		return this;
	}
	
	public ProjectPage addNotice(String title) {
		By addNoticeBy = By.cssSelector(".o_proj_dashboard .o_proj_title a.o_sel_proj_add_notice");
		browser.findElement(addNoticeBy).click();
		OOGraphene.waitModalDialog(browser);
		
		By titleBy = By.cssSelector("div.o_sel_notice_title input[type='text']");
		OOGraphene.waitElement(titleBy, browser).sendKeys(title);
		
		By closeBy = By.cssSelector(".o_sel_buttons a.o_sel_notice_close");
		browser.findElement(closeBy).click();
		
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	public ProjectPage assertOnNoticeQuickStart(String title) {
		By quickStartBy = By.xpath("//div[contains(@class,'o_proj_quick_artefacts')]//a//div[contains(@class,'o_proj_quick_name')][text()[contains(.,'" + title + "')]]");
		OOGraphene.waitElement(quickStartBy, browser);
		return this;
	}
	
	public ProjectPage assertOnNoticeTimeline(String title) {
		By noticeBy = By.xpath("//div[contains(@class,'o_proj_timeline_list')]//div[contains(@class,'o_proj_timeline_row')]//a[span[text()[contains(.,'" + title + "')]]]");
		OOGraphene.waitElement(noticeBy, browser);
		return this;
	}
	
	public ProjectPage quickAddToDo(String todo) {
		By openAddMenuBy = By.cssSelector(".o_proj_dashboard .o_proj_title button.o_sel_proj_quick_create_dropdown");
		OOGraphene.waitElement(openAddMenuBy, browser).click();
		
		By menuBy = By.cssSelector("ul.o_sel_proj_quick_create_dropdown");
		OOGraphene.waitElement(menuBy, browser);
		
		By addToDoBy = By.cssSelector(".o_sel_proj_quick_create_dropdown a.o_sel_proj_create_todo");
		browser.findElement(addToDoBy).click();
		OOGraphene.waitModalDialog(browser);
		
		By titleBy = By.cssSelector("div.o_todo_task_edit .o_sel_task_title input[type='text']");
		OOGraphene.waitElement(titleBy, browser).sendKeys(todo);
		
		By saveBy = By.cssSelector(".modal-dialog .o_sel_buttons button.btn.btn-primary");
		browser.findElement(saveBy).click();
		
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	public ProjectPage assertOnToDoTimeline(String title) {
		By todoBy = By.xpath("//div[contains(@class,'o_proj_timeline_list')]//div[contains(@class,'o_proj_timeline_row')][div/div/span/i[contains(@class,'o_icon_todo_task')]]//a[span[text()[contains(.,'" + title + "')]]]");
		OOGraphene.waitElement(todoBy, browser);
		return this;
	}
	
	public ProjectPage assertOnToDoTaskList(String title) {
		By todoBy = By.xpath("//div[contains(@class,'o_todo_task_list')]//td//a/span[text()[contains(.,'" + title + "')]]");
		OOGraphene.waitElement(todoBy, browser);
		return this;
	}
	
	public ProjectPage selectToDoInTaskList(String title) {
		By todoBy = By.xpath("//div[contains(@class,'o_todo_task_list')]//td//a[span[text()[contains(.,'" + title + "')]]]");
		OOGraphene.waitElement(todoBy, browser).click();
		
		OOGraphene.waitModalDialog(browser);
		
		By editTodoBy = By.className("o_todo_task_edit");
		OOGraphene.waitElement(editTodoBy, browser);
		return this;
	}
	
	/**
	 * Mark the to-do as done, and save it.
	 * @return
	 */
	public ProjectPage checkEditedToDo() {
		String checkSelector = ".o_todo_task_edit .o_todo_task_do_row button.o_toggle_check";
		OOGraphene.toggle(checkSelector, true, true, browser);
		
		By saveBy = By.cssSelector(".modal-dialog .o_sel_buttons button.btn.btn-primary");
		browser.findElement(saveBy).click();
		
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	public ProjectPage assertNothingToDo() {
		By emptyToDoBy = By.xpath("//div[contains(@class,'o_proj_todo_widget')]//table//button[@role='checkbox'][i[contains(@class,'o_icon_toggle_check_off')]]");
		OOGraphene.waitElementAbsence(emptyToDoBy, 5, browser);
		return this;
	}
	
	

}
