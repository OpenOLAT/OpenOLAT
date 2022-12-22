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
package org.olat.selenium.page.repository;

import java.io.File;

import org.junit.Assert;
import org.olat.course.CourseModule;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.selenium.page.course.CoursePageFragment;
import org.olat.selenium.page.course.CourseSettingsPage;
import org.olat.selenium.page.course.CourseWizardPage;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Select;

/**
 * Page to control the author environnment.
 * 
 * 
 * Initial date: 20.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AuthoringEnvPage {
	
	public static final By createMenuBy = By.cssSelector("ul.o_sel_author_create");
	public static final By generaltabBy = By.className("o_sel_edit_repositoryentry");
	
	private WebDriver browser;
	
	public AuthoringEnvPage(WebDriver browser) {
		this.browser = browser;
	}
	
	/**
	 * Check that the segment for the "Search" in author environment is selected.
	 * 
	 * @return
	 */
	public AuthoringEnvPage assertOnGenericSearch() {
		By genericSearchBy = By.xpath("//ul[contains(@class,'o_segments')]/li/a[contains(@class,'btn-primary')][contains(@class,'o_sel_author_search')]");
		OOGraphene.waitElement(genericSearchBy, browser);
		WebElement genericSearchSegment = browser.findElement(genericSearchBy);
		Assert.assertTrue(genericSearchSegment.isDisplayed());
		return this;
	}
	
	public RepositorySettingsPage createCP(String title) {
		return openCreateDropDown()
			.clickCreate(ResourceType.cp)
			.fillCreateForm(title);
	}
	
	public RepositorySettingsPage createWiki(String title) {
		return openCreateDropDown()
			.clickCreate(ResourceType.wiki)
			.fillCreateForm(title);
	}
	
	public RepositorySettingsPage createSurvey(String title) {
		return openCreateDropDown()
			.clickCreate(ResourceType.survey)
			.fillCreateForm(title);
	}
	
	public CourseSettingsPage createCourse(String title) {
		return createCourse(title, false);
	}
	
	public CourseSettingsPage createCourse(String title, boolean learnPath) {
		openCreateDropDown()
			.clickCreate(ResourceType.course)
			.fillCreateCourseForm(title, learnPath)
			.assertOnInfos();
		return new CourseSettingsPage(browser);
	}
	
	public RepositoryEditDescriptionPage createPortfolioBinder(String title) {
		return openCreateDropDown()
			.clickCreate(ResourceType.portfolio)
			.fillCreateForm(title)
			.assertOnInfos();
	}
	
	public RepositoryEditDescriptionPage createQTI21Test(String title) {
		return openCreateDropDown()
			.clickCreate(ResourceType.qti21Test)
			.fillCreateForm(title)
			.assertOnInfos();
	}
	
	public RepositoryEditDescriptionPage createSharedFolder(String title) {
		return openCreateDropDown()
			.clickCreate(ResourceType.sharedFolder)
			.fillCreateForm(title)
			.assertOnInfos();
	}
	
	/**
	 * Open the drop-down to create a new resource.
	 * @return
	 */
	public AuthoringEnvPage openCreateDropDown() {
		By createMenuCaretBy = By.cssSelector("button.o_sel_author_create");
		OOGraphene.waitElement(createMenuCaretBy, browser);
		browser.findElement(createMenuCaretBy).click();
		OOGraphene.waitElement(createMenuBy, browser);
		return this;
	}

	/**
	 * Click the link to create a learning resource in the create drop-down
	 * @param type
	 * @return
	 */
	public AuthoringEnvPage clickCreate(ResourceType type) {
		WebElement createMenu = browser.findElement(createMenuBy);
		Assert.assertTrue(createMenu.isDisplayed());
		WebElement createLink = createMenu.findElement(By.className("o_sel_author_create-" + type.type()));
		Assert.assertTrue(createLink.isDisplayed());
		createLink.click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Fill the create form and submit
	 * @param displayName The name of the learn resource
	 * @return Itself
	 */
	public RepositorySettingsPage fillCreateForm(String displayName) {
		OOGraphene.waitModalDialog(browser);
		By inputBy = By.cssSelector("div.modal.o_sel_author_create_popup div.o_sel_author_displayname input");
		browser.findElement(inputBy).sendKeys(displayName);
		By submitBy = By.cssSelector("div.modal.o_sel_author_create_popup .o_sel_author_create_submit");
		browser.findElement(submitBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		OOGraphene.waitElement(generaltabBy, browser);
		OOGraphene.waitTinymce(browser);
		return new RepositorySettingsPage(browser);
	}
	
	/**
	 * Fill the create form and submit
	 * @param displayName The name of the course
	 * @param learnPath true to use the new learn path course, false for the old model
	 * @return Itself
	 */
	public RepositorySettingsPage fillCreateCourseForm(String displayName, boolean learnPath) {
		OOGraphene.waitModalDialog(browser);
		By inputBy = By.cssSelector("div.modal.o_sel_author_create_popup div.o_sel_author_displayname input");
		browser.findElement(inputBy).sendKeys(displayName);
		// select node model for the course
		String type = learnPath ? CourseModule.COURSE_TYPE_PATH : CourseModule.COURSE_TYPE_CLASSIC;
		By typeBy = By.xpath("//div[contains(@class,'o_radio_cards') and contains(@class,'o_course_design')]//input[@name='course.design' and @value='" + type + "']");
		browser.findElement(typeBy).click();
		// create the course
		By submitBy = By.cssSelector("div.modal.o_sel_author_create_popup .o_sel_author_create_submit");
		OOGraphene.click(submitBy, browser);
		OOGraphene.waitModalDialogDisappears(browser);
		OOGraphene.waitElement(generaltabBy, browser);
		OOGraphene.waitTinymce(browser);
		return new RepositorySettingsPage(browser);
	}
	
	/**
	 * Fill the create form and start the wizard
	 * @param displayName
	 * @return
	 */
	public CourseWizardPage fillCreateFormAndStartWizard(String displayName) {
		OOGraphene.waitModalDialog(browser);
		By inputBy = By.cssSelector("div.modal.o_sel_author_create_popup div.o_sel_author_displayname input");
		browser.findElement(inputBy).sendKeys(displayName);
		// select node model for the course
		By typeBy = By.xpath("//div[contains(@class,'o_radio_cards') and contains(@class,'o_course_design')]//input[@name='course.design' and @value='classic']");
		browser.findElement(typeBy).click();
		// open the assistant list
		By assistantListBy = By.cssSelector("div.modal-dialog div.o_sel_repo_save_details button.dropdown-toggle");
		browser.findElement(assistantListBy).click();
		By assistantDropdownBy = By.cssSelector("div.modal-dialog ul.dropdown-menu");
		OOGraphene.waitElement(assistantDropdownBy, browser);
		// create the course
		By simpleCourseWizardBy = By.xpath("//div[contains(@class,'modal-dialog')]//a[contains(@class,'o_sel_wizard_simple.course')]");
		browser.findElement(simpleCourseWizardBy).click();
		// wait the wizard
		OOGraphene.waitModalDialog(browser, "div.o_sel_course_elements");
		return new CourseWizardPage(browser);
	}
	
	/**
	 * Short cut to create quickly a course
	 * @param title
	 */
	public void quickCreateCourse(String title) {
		RepositoryEditDescriptionPage editDescription = openCreateDropDown()
			.clickCreate(ResourceType.course)
			.fillCreateForm(title)
			.assertOnInfos();
			
		//from description editor, back to details and launch the course
		editDescription
			.clickToolbarBack();
	}
	
	/**
	 * Try to upload a resource if the type is recognized.
	 * 
	 * @param title The title of the learning resource
	 * @param resource The zip file to import
	 * @return Itself
	 */
	public AuthoringEnvPage uploadResource(String title, File resource) {
		By importBy = By.className("o_sel_author_import");
		OOGraphene.waitElement(importBy, browser);
		browser.findElement(importBy).click();
		OOGraphene.waitBusy(browser);
		
		By inputBy = By.cssSelector(".o_fileinput input[type='file']");
		OOGraphene.uploadFile(inputBy, resource, browser);
		OOGraphene.waitElement(By.className("o_sel_author_imported_name"), browser);
		
		By titleBy = By.cssSelector(".o_sel_author_imported_name input");
		WebElement titleEl = browser.findElement(titleBy);
		titleEl.sendKeys(title);
		
		//save
		By saveBy = By.cssSelector("div.o_sel_repo_save_details button.btn-primary");
		WebElement saveButton = browser.findElement(saveBy);
		if(saveButton.isEnabled()) {
			saveButton.click();
			OOGraphene.waitModalDialogDisappears(browser);
			OOGraphene.waitElement(generaltabBy, browser);
			OOGraphene.waitTinymce(browser);
		}
		return this;
	}
	
	public AuthoringEnvPage assertOnResourceType() {
		By typeEl = By.cssSelector(".o_sel_author_type");
		OOGraphene.waitElement(typeEl, 5, browser);
		return this;
	}
	
	public AuthoringEnvPage assertOnStatus(String title, RepositoryEntryStatusEnum status) {
		By rowBy = By.xpath("//div[@class='o_sel_author_env']//table//tr[td/a[text()[contains(.,'" + title + "')]]]/td/span/i[contains(@class,'o_icon_repo_status_" + status.name() + "')]");
		OOGraphene.waitElement(rowBy, browser);
		return this;
	}
	
	/**
	 * @param title The title of the resource to open
	 */
	public void openResource(String title) {
		By selectBy = By.xpath("//div[contains(@class,'o_coursetable')]//a[contains(text(),'" + title + "')]");
		OOGraphene.waitElement(selectBy, browser);
		browser.findElement(selectBy).click();
		OOGraphene.waitBusy(browser);
	}
	
	/**
	 * Select (for multi-selections) the resource with the specified title.
	 * 
	 * @param title The title of the learn resource
	 * @return Itself
	 */
	public AuthoringEnvPage selectResource(String title) {
		By selectBy = By.xpath("//div[contains(@class,'o_coursetable')]//tr[td/a[contains(text(),'" + title + "')]]/td/input[@name='tb_ms']");
		OOGraphene.waitElement(selectBy, browser);
		browser.findElement(selectBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public AuthoringEnvPage searchResource(String text) {
		By searchPresetBy = By.cssSelector(".o_sel_author_env .o_table_tabs a.o_sel_author_search");
		OOGraphene.waitElement(searchPresetBy, browser);
		browser.findElement(searchPresetBy).click();
		
		By searchFieldBy = By.cssSelector(".o_sel_author_env .o_table_large_search input[type='text']");
		OOGraphene.waitElement(searchFieldBy, browser);
		browser.findElement(searchFieldBy).sendKeys(text);
		
		By searchButtonBy = By.cssSelector(".o_sel_author_env .o_table_large_search a.o_table_search_button");
		browser.findElement(searchButtonBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public void editResource(String title) {
		if(browser instanceof FirefoxDriver) {
			By toolsMenuCaretBy = By.xpath("//div[contains(@class,'o_coursetable')]//tr[td/a[contains(text(),'" + title + "')]]/td[contains(@class,'o_col_action')]/a[i[contains(@class,'o_icon_actions')]]");
			OOGraphene.waitElement(toolsMenuCaretBy, browser);
			browser.findElement(toolsMenuCaretBy).click();
			By toolsMenu = By.cssSelector("ul.o_sel_authoring_tools");
			OOGraphene.waitElement(toolsMenu, browser);
			
			By editBy = By.xpath("//ul[contains(@class,'o_sel_authoring_tools')]/li/a[contains(@onclick,'edit')][i[contains(@class,'o_icon_edit')]]");
			browser.findElement(editBy).click();
		} else {
			By editBy = By.xpath("//div[contains(@class,'o_coursetable')]//tr[//a[contains(text(),'" + title + "')]]//a[contains(@onclick,'edit')]");
			OOGraphene.waitElement(editBy, browser);
			browser.findElement(editBy).click();
		}
		// can be warning in edition or edit mode
		OOGraphene.waitBusy(browser);
	}
	
	public ModifyOwnersPage changeOwner() {
		By modifyOwnersBy = By.cssSelector("div.o_table_batch_buttons a.o_sel_modify_owners");
		OOGraphene.waitElement(modifyOwnersBy, browser);
		browser.findElement(modifyOwnersBy).click();
		OOGraphene.waitModalWizard(browser);
		return new ModifyOwnersPage(browser);
	}
	
	public AuthoringEnvPage modifyStatus(RepositoryEntryStatusEnum status) {
		By modifyOwnersBy = By.cssSelector("div.o_table_batch_buttons a.o_sel_modify_status");
		OOGraphene.waitElement(modifyOwnersBy, browser);
		browser.findElement(modifyOwnersBy).click();
		OOGraphene.waitModalWizard(browser);
		
		By statusBy = By.cssSelector("div.modal-dialog div.o_sel_status select");
		OOGraphene.waitElement(statusBy, browser);
		new Select(browser.findElement(statusBy)).selectByValue(status.name());
		
		By modifyBy = By.cssSelector("div.modal-dialog button.btn-primary");
		browser.findElement(modifyBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		
		return this;
	}
	
	/**
	 * Click back from the editor
	 * 
	 * @return
	 */
	public CoursePageFragment clickToolbarRootCrumb() {
		OOGraphene.clickBreadcrumbBack(browser);
		return new CoursePageFragment(browser);
	}
	
	public enum ResourceType {
		course("CourseModule"),
		cp("FileResource.IMSCP"),
		wiki("FileResource.WIKI"),
		portfolio("BinderTemplate"),
		qti21Test("FileResource.IMSQTI21"),
		survey("FileResource.FORM"),
		sharedFolder("FileResource.SHAREDFOLDER");
		
		private final String type;
		
		private ResourceType(String type) {
			this.type = type;
		}
		
		public String type() {
			return type;
		}
	}
}
