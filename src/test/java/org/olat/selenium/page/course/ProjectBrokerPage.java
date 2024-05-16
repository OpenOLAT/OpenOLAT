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

import java.io.File;

import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 22 nov. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ProjectBrokerPage {
	
	private final WebDriver browser;
	
	public ProjectBrokerPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public ProjectBrokerPage assertOnProjectBrokerList() {
		By startBy = By.cssSelector("div.o_project_broker_list");
		OOGraphene.waitElement(startBy, browser);
		return this;
	}
	
	public ProjectBrokerPage assertOnProjectBrokerInList(String title) {
		By inListBy = By.xpath("//div[contains(@class,'o_project_broker_list')]//table//td/a[text()[contains(.,'" + title + "')]]");
		OOGraphene.waitElement(inListBy, browser);
		return this;
	}
	
	public ProjectBrokerPage createNewProject(String title) {
		By createProjectBy = By.cssSelector("div.o_project_broker_list a.o_sel_broker_create_new_project");
		OOGraphene.waitElement(createProjectBy, browser);
		browser.findElement(createProjectBy).click();
		
		By projectBy = By.cssSelector(".o_project fieldset.o_sel_project_details_form");
		OOGraphene.waitElement(projectBy, browser);
		OOGraphene.waitTinymce(browser);
		
		By titleBy = By.cssSelector("fieldset.o_sel_project_details_form .o_sel_project_title input[type='text']");
		WebElement titleEl = browser.findElement(titleBy);
		titleEl.clear();
		titleEl.sendKeys(title);
		
		By saveBy = By.cssSelector("fieldset.o_sel_project_details_form .o_sel_buttons button.btn-primary");
		browser.findElement(saveBy).sendKeys(title);
		
		By inListBy = By.xpath("//div[contains(@class,'o_project_broker_list')]//table//td/a[text()[contains(.,'" + title + "')]]");
		OOGraphene.waitElement(inListBy, browser);
		return  assertOnProjectBrokerInList(title);
	}
	
	public ProjectBrokerPage enrollInProject(String title) {
		By enrollBy = By.xpath("//div[contains(@class,'o_project_broker_list')]//table//tr[td/a[text()[contains(.,'" + title + "')]]]/td/a[contains(@onclick,'cmd.select')]");
		OOGraphene.waitElement(enrollBy, browser);
		browser.findElement(enrollBy).click();
		By enrolledBy = By.xpath("//div[contains(@class,'o_project_broker_list')]//table//tr/td/strong[@class='o_state_enrolled']");
		OOGraphene.waitElement(enrolledBy, browser);
		return this;
	}
	
	public ProjectBrokerPage selectProject(String title) {
		By selectBy = By.xpath("//div[contains(@class,'o_project_broker_list')]//table//tr/td/a[text()[contains(.,'" + title + "')]]");
		OOGraphene.waitElement(selectBy, browser);
		browser.findElement(selectBy).click();
		OOGraphene.waitElement(By.className("o_project"), browser);
		return this;
	}
	
	public ProjectBrokerPage selectFolders() {
		By foldersBy = By.cssSelector(".o_tabbed_pane ul.nav-tabs>li.o_sel_project_broker_folders>a");
		OOGraphene.waitElement(foldersBy, browser);
		browser.findElement(foldersBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public ProjectBrokerPage assertOnDropbox() {
		OOGraphene.waitElement(By.id("collapseDropbox"), browser);
		return this;
	}
	
	public ProjectBrokerPage assertOnFileInDropbox(String filename) {
		By fileBy = By.xpath("//div[@id='collapseDropbox']//div[contains(@class,'o_folder_table')]//h5/a/span[contains(text(),'" + filename + "')]");	
		OOGraphene.waitElement(fileBy, browser);
		return this;
	}
	
	public ProjectBrokerPage selectFolderInDropbox(UserVO user) {
		String name = user.getLastName();
		By folderBy = By.xpath("//div[@id='collapseDropbox']//div[contains(@class,'o_folder_table')]//div[div[contains(@class,'o_folder_card_thumbnail') and contains(@class,'o_folder_openable')]][div/div/div/h5/a[span[contains(text(),'" + name + "')]]]/div[contains(@class,'o_folder_card_thumbnail')]");
		OOGraphene.waitElement(folderBy, browser);
		browser.findElement(folderBy).click();
		
		// On Linux, the screenshot update the window of Chrome
		OOGraphene.waitingALittleLonger();
		OOGraphene.takeScreenshotInMemory(browser);
		
		By breadCrumbBy = By.xpath("//div[@id='collapseDropbox']//ol/li[contains(@class,'o_breadcrumb_crumb')][not(contains(@class,'o_display_none'))]//span[text()[contains(.,'" + user.getLastName() + "')]]");
		OOGraphene.waitElementSlowly(breadCrumbBy, 5, browser);
		return this;
	}
	
	public ProjectBrokerPage uploadDropbox(File file) {
		By uploadButtonBy = By.xpath("//div[@id='collapseDropbox']//a[contains(@onclick,'dropbox.upload')]");
		OOGraphene.waitElement(uploadButtonBy, browser);
		browser.findElement(uploadButtonBy).click();
		OOGraphene.waitModalDialog(browser);
		
		By inputBy = By.cssSelector(".o_sel_course_gta_upload_task_form .o_fileinput input[type='file']");
		OOGraphene.uploadFile(inputBy, file, browser);
		OOGraphene.waitBusy(browser);
		By uploadedBy = By.cssSelector(".o_sel_course_gta_upload_task_form .o_sel_file_uploaded");
		OOGraphene.waitElement(uploadedBy, browser);
		
		By saveButtonBy = By.cssSelector(".o_sel_course_gta_upload_task_form button.btn-primary");
		OOGraphene.clickAndWait(saveButtonBy, browser);
		OOGraphene.waitModalDialogDisappears(browser);
		OOGraphene.waitAndCloseBlueMessageWindow(browser);
		return this;
	}
	
	
}
