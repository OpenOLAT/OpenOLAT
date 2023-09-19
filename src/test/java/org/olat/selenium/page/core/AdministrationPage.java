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
package org.olat.selenium.page.core;

import java.util.List;

import org.junit.Assert;
import org.olat.selenium.page.course.BigBlueButtonSettingsPage;
import org.olat.selenium.page.course.JupyterHubSettingsPage;
import org.olat.selenium.page.course.LTI13SettingsPage;
import org.olat.selenium.page.course.ZoomSettingsPage;
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.selenium.page.lecture.LectureAdminSettingsPage;
import org.olat.selenium.page.library.LibraryAdminPage;
import org.olat.selenium.page.qpool.QuestionPoolAdminPage;
import org.olat.selenium.page.taxonomy.TaxonomyAdminPage;
import org.olat.selenium.page.tracing.ContactTracingAdminPage;
import org.olat.selenium.page.user.PasskeyAdminPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Drive the administration site
 * 
 * Initial date: 07.07.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AdministrationPage {

	private final WebDriver browser;

	public AdministrationPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public AdministrationMessagesPage selectInfoMessages() {
		try {
			selectSystemInfo();
			
			By messagesBy = By.cssSelector(".o_sel_sysinfo span.o_tree_level_label_leaf>a");
			browser.findElement(messagesBy).click();
			By messagesActiveBy = By.cssSelector(".o_sel_sysinfo span.active.o_tree_level_label_leaf>a");
			OOGraphene.waitElement(messagesActiveBy, browser);
		} catch (Exception e) {
			OOGraphene.takeScreenshot("Select infos messages", browser);
			throw e;
		}
		return new AdministrationMessagesPage(browser);
	}
	
	public AdministrationPage selectSystemInfo() {
		By systemLinkBy = By.xpath("//div[contains(@class,'o_tree')]//span[contains(@class,'o_tree_link')]/a[contains(@onclick,'systemParent')]");
		OOGraphene.waitElement(systemLinkBy, browser);
		browser.findElement(systemLinkBy).click();
		By systemLinkOpenBy = By.xpath("//div[contains(@class,'o_tree')]//div[contains(@class,'active')][a/i[contains(@class,'o_icon_close_tree')]]/span[contains(@class,'o_tree_link')][contains(@class,'active')]/a[contains(@onclick,'systemParent')]");
		OOGraphene.waitElement(systemLinkOpenBy, browser);
		return this;
	}
	
	public AdministrationPage selectCoreConfiguration() {
		By coreConfigurationLinkBy = By.xpath("//div[contains(@class,'o_tree')]//span[contains(@class,'o_tree_link')]/a[contains(@onclick,'sysconfigParent')]");
		OOGraphene.waitElement(coreConfigurationLinkBy, browser);
		browser.findElement(coreConfigurationLinkBy).click();
		By coreConfigurationLinkOpenBy = By.xpath("//div[contains(@class,'o_tree')]//div[contains(@class,'active')][a/i[contains(@class,'o_icon_close_tree')]]/span[contains(@class,'o_tree_link')][contains(@class,'active')]/a[contains(@onclick,'sysconfigParent')]");
		OOGraphene.waitElement(coreConfigurationLinkOpenBy, browser);
		return this;
	}
	
	public AdministrationPage selectLogin() {
		By loginLinkBy = By.xpath("//div[contains(@class,'o_tree')]//span[contains(@class,'o_tree_link')]/a[contains(@onclick,'loginAndSecurityParent')]");
		OOGraphene.waitElement(loginLinkBy, browser);
		browser.findElement(loginLinkBy).click();
		By loginLinkOpenBy = By.xpath("//div[contains(@class,'o_tree')]//div[contains(@class,'o_tree_l0')][a/i[contains(@class,'o_icon_close_tree')]]/span[contains(@class,'o_tree_link')][contains(@class,'active_parent')]/a[contains(@onclick,'loginAndSecurityParent')]");
		OOGraphene.waitElement(loginLinkOpenBy, browser);
		return this;
	}
	
	public AdministrationPage selectModules() {
		By moduleLinkBy = By.xpath("//div[contains(@class,'o_tree')]//span[contains(@class,'o_tree_link')]/a[contains(@onclick,'modulesParent')]");
		OOGraphene.waitElement(moduleLinkBy, browser);
		browser.findElement(moduleLinkBy).click();
		By moduleLinkOpenBy = By.xpath("//div[contains(@class,'o_tree')]//span[contains(@class,'o_tree_link')][contains(@class,'active_parent')]/a[contains(@onclick,'modulesParent')]");
		OOGraphene.waitElement(moduleLinkOpenBy, browser);
		return this;
	}
	
	public AdministrationPage selectAssessment() {
		By assessmentLinkBy = By.xpath("//div[contains(@class,'o_tree')]//span[contains(@class,'o_tree_link')]/a[contains(@onclick,'eAssessmentParent')]");
		OOGraphene.waitElement(assessmentLinkBy, browser);
		browser.findElement(assessmentLinkBy).click();
		By assessmentLinkOpenBy = By.xpath("//div[contains(@class,'o_tree')]//span[contains(@class,'o_tree_link')][contains(@class,'active_parent')]/a[contains(@onclick,'eAssessmentParent')]");
		OOGraphene.waitElement(assessmentLinkOpenBy, browser);
		return this;
	}
	
	public AdministrationPage selectExternalTools() {
		By externalToolsLinkBy = By.xpath("//div[contains(@class,'o_tree')]//a[contains(@onclick,'externalToolsParent')]");
		OOGraphene.waitElement(externalToolsLinkBy, browser);
		browser.findElement(externalToolsLinkBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public AdministrationPage clearCache(String cacheName) {
		selectSystemInfo();
		
		//cache tree node
		WebElement cacheLink = browser.findElement(By.cssSelector(".o_sel_caches span.o_tree_level_label_leaf>a"));
		cacheLink.click();
		OOGraphene.waitBusy(browser);
		//table
		WebElement emptyLink = null;
		List<WebElement> rows = browser.findElements(By.cssSelector(".o_table_wrapper table>tbody>tr"));
		for(WebElement row:rows) {
			if(row.getText().contains(cacheName)) {
				emptyLink = row.findElement(By.tagName("a"));
			}
		}
		Assert.assertNotNull(emptyLink);
		//click to empty
		emptyLink.click();
		OOGraphene.waitBusy(browser);
		//confirm
		WebElement yesLink = browser.findElement(By.xpath("//div[contains(@class,'modal-dialog')]//a[contains(@onclick,'link_0')]"));
		yesLink.click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public LectureAdminSettingsPage openLecturesSettings() {
		selectModules();
		openSubMenu("o_sel_lectures");
		OOGraphene.waitBusyAndScrollTop(browser);
		
		By configurationFormBy = By.cssSelector("fieldset.o_sel_lectures_configuration_form");
		OOGraphene.waitElement(configurationFormBy, browser);
		return new LectureAdminSettingsPage(browser);
	}
	
	public LibraryAdminPage openLibrarySettings() {
		selectModules();
		openSubMenu("o_sel_library");
		OOGraphene.waitBusyAndScrollTop(browser);
		
		By configurationFormBy = By.cssSelector("fieldset.o_sel_library_configuration");
		OOGraphene.waitElement(configurationFormBy, browser);
		return new LibraryAdminPage(browser);
	}
	
	public AdministrationPage openGroupSettings() {
		selectModules();
		
		By groupBy = By.cssSelector(".o_sel_group span.o_tree_level_label_leaf>a");
		OOGraphene.waitElement(groupBy, browser);
		OOGraphene.click(groupBy, browser);
		OOGraphene.waitBusyAndScrollTop(browser);
		By groupConfigBy = By.cssSelector("fieldset.o_sel_group_admin_dedup");
		OOGraphene.waitElement(groupConfigBy, browser);
		return this;
	}
	
	public AdministrationPage setGroupConfirmationForUser(boolean mandatory) {
		By userConfirmationCheckBy = By.xpath("//label/input[@name='mandatory.membership' and @value='user']");
		OOGraphene.waitElementPresence(userConfirmationCheckBy, 5, browser);
		OOGraphene.scrollTo(userConfirmationCheckBy, browser);
		
		WebElement userConfirmationCheckEl = browser.findElement(userConfirmationCheckBy);
		OOGraphene.check(userConfirmationCheckEl, Boolean.valueOf(mandatory));
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public AdministrationPage setGroupConfirmationForAuthor(boolean mandatory) {
		By authorConfirmationCheckBy = By.xpath("//label/input[@name='mandatory.membership' and @value='author']");
		OOGraphene.waitElementPresence(authorConfirmationCheckBy, 5, browser);
		OOGraphene.scrollTo(authorConfirmationCheckBy, browser);
		
		WebElement authorConfirmationCheckEl = browser.findElement(authorConfirmationCheckBy);
		OOGraphene.check(authorConfirmationCheckEl, Boolean.valueOf(mandatory));
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public PasskeyAdminPage openPassKey() {
		selectLogin();
		
		By tracingBy = By.cssSelector(".o_sel_passkey span.o_tree_level_label_leaf>a");
		browser.findElement(tracingBy).click();
		OOGraphene.waitElement(By.className("o_sel_passkey_admin_configuration"), browser);
		return new PasskeyAdminPage(browser);
	}
	
	public ContactTracingAdminPage openContactTracing() {
		selectModules();
		
		By tracingBy = By.cssSelector(".o_sel_ContactTracing span.o_tree_level_label_leaf>a");
		browser.findElement(tracingBy).click();
		OOGraphene.waitBusy(browser);
		return new ContactTracingAdminPage(browser);
	}
	
	public LicensesAdminstrationPage openLicenses() {
		selectCoreConfiguration();
		
		By licenseBy = By.cssSelector(".o_sel_license span.o_tree_level_label_leaf>a");
		browser.findElement(licenseBy).click();
		By licenseAdminBy = By.cssSelector(".o_sel_license_general");
		OOGraphene.waitElement(licenseAdminBy, browser);
		return new LicensesAdminstrationPage(browser);
	}
	
	public QuestionPoolAdminPage openQuestionPool() {
		selectAssessment();
		
		By poolBy = By.cssSelector(".o_sel_qpool span.o_tree_level_label_leaf>a");
		browser.findElement(poolBy).click();
		By configBy = By.cssSelector("fieldset.o_sel_qpool_configuration");
		OOGraphene.waitElement(configBy, browser);
		
		return new QuestionPoolAdminPage(browser).assertOnConfiguration();
	}
	
	public TaxonomyAdminPage openTaxonomy() {
		selectModules();
		openSubMenu("o_sel_taxonomy");
		return new TaxonomyAdminPage(browser)
				.assertOnTaxonomyList();
	}
	
	public BigBlueButtonSettingsPage openBigBlueButtonSettings() {
		selectExternalTools();
		
		By bigBlueButtonBy = By.cssSelector(".o_sel_bigbluebutton span.o_tree_level_label_leaf>a");
		OOGraphene.waitElement(bigBlueButtonBy, browser);
		browser.findElement(bigBlueButtonBy).click();
		By bbbConfigBy = By.cssSelector("fieldset.o_sel_bbb_admin_configuration");
		OOGraphene.waitElement(bbbConfigBy, browser);
		return new BigBlueButtonSettingsPage(browser);
	}
	
	public LTI13SettingsPage openLti13Settings() {
		selectExternalTools();
		
		By ltiBy = By.cssSelector(".o_sel_lti13 span.o_tree_level_label_leaf>a");
		OOGraphene.waitElement(ltiBy, browser);
		browser.findElement(ltiBy).click();
		By ltiConfigBy = By.cssSelector("fieldset.o_sel_lti13_admin_settings");
		OOGraphene.waitElement(ltiConfigBy, browser);
		return new LTI13SettingsPage(browser);
	}
	
	public ZoomSettingsPage openZoomSettings(boolean openExternalTools) {
		if(openExternalTools) {
			selectExternalTools();
		}
		
		By zoomBy = By.cssSelector(".o_sel_zoom span.o_tree_level_label_leaf>a");
		OOGraphene.waitElement(zoomBy, browser);
		browser.findElement(zoomBy).click();
		By zoomConfigBy = By.cssSelector("fieldset.o_sel_zoom_admin_configuration");
		OOGraphene.waitElement(zoomConfigBy, browser);
		return new ZoomSettingsPage(browser);
	}
	
	public JupyterHubSettingsPage openJupyterHubSettings(boolean openExternalTools) {
		if(openExternalTools) {
			selectExternalTools();
		}
		
		By zoomBy = By.cssSelector(".o_sel_jupyterHub span.o_tree_level_label_leaf>a");
		OOGraphene.waitElement(zoomBy, browser);
		browser.findElement(zoomBy).click();
		By zoomConfigBy = By.cssSelector("fieldset.o_sel_jupyterhub_admin_configuration");
		OOGraphene.waitElement(zoomConfigBy, browser);
		return new JupyterHubSettingsPage(browser);
	}
	
	private void openSubMenu(String liClass) {
		By menuItemBy = By.xpath("//li[contains(@class,'" + liClass + "')]");
		OOGraphene.waitElement(menuItemBy, browser);
		OOGraphene.moveTo(menuItemBy, browser);
		
		By menuItemLinkBy = By.xpath("//li[contains(@class,'" + liClass + "')]//span[contains(@class,'o_tree_level_label_leaf')]/a[span[@class='o_tree_item']]");
		browser.findElement(menuItemLinkBy).click();
		
		By activeMenuItemLinkBy = By.xpath("//li[contains(@class,'" + liClass + "')][contains(@class,'active')]//span[contains(@class,'o_tree_level_label_leaf')]/a[span[@class='o_tree_item']]");
		OOGraphene.waitElementPresence(activeMenuItemLinkBy, 5, browser);// Element possibly not on screen
	}
}
