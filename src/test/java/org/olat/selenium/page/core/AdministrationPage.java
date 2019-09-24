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
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.selenium.page.lecture.LectureAdminSettingsPage;
import org.olat.selenium.page.qpool.QuestionPoolAdminPage;
import org.olat.selenium.page.taxonomy.TaxonomyAdminPage;
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
		this.selectSystemInfo();
		
		By messagesBy = By.cssSelector(".o_sel_sysinfo span.o_tree_level_label_leaf>a");
		browser.findElement(messagesBy).click();
		OOGraphene.waitBusy(browser);
		return new AdministrationMessagesPage(browser);
	}
	
	public AdministrationPage selectSystemInfo() {
		By systemLinkBy = By.xpath("//div[contains(@class,'o_tree')]//a[contains(@onclick,'systemParent')]");
		OOGraphene.waitElement(systemLinkBy, browser);
		browser.findElement(systemLinkBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public AdministrationPage selecCoreConfiguration() {
		By coreConfigurationLinkBy = By.xpath("//div[contains(@class,'o_tree')]//a[contains(@onclick,'sysconfigParent')]");
		OOGraphene.waitElement(coreConfigurationLinkBy, browser);
		browser.findElement(coreConfigurationLinkBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public AdministrationPage selectModules() {
		By moduleLinkBy = By.xpath("//div[contains(@class,'o_tree')]//a[contains(@onclick,'modulesParent')]");
		OOGraphene.waitElement(moduleLinkBy, browser);
		browser.findElement(moduleLinkBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public AdministrationPage selectAssessment() {
		By systemLinkBy = By.xpath("//div[contains(@class,'o_tree')]//a[contains(@onclick,'eAssessmentParent')]");
		OOGraphene.waitElement(systemLinkBy, browser);
		browser.findElement(systemLinkBy).click();
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
		WebElement yesLink = browser.findElement(By.xpath("//div[contains(@class,'modal-dialog')]//a[contains(@href,'link_0')]"));
		yesLink.click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public LectureAdminSettingsPage openLecturesSettings() {
		selectModules();
		
		By lecturesBy = By.cssSelector(".o_sel_lectures span.o_tree_level_label_leaf>a");
		browser.findElement(lecturesBy).click();
		OOGraphene.waitBusy(browser);
		return new LectureAdminSettingsPage(browser);
	}
	
	public AdministrationPage openGroupSettings() {
		selectModules();
		
		WebElement groupLink = browser.findElement(By.cssSelector(".o_sel_group span.o_tree_level_label_leaf>a"));
		groupLink.click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public AdministrationPage setGroupConfirmationForUser(boolean mandatory) {
		By userConfirmationCheckBy = By.xpath("//label/input[@name='mandatory.membership' and @value='user']");
		
		OOGraphene.waitElement(userConfirmationCheckBy, browser);
		OOGraphene.scrollTo(userConfirmationCheckBy, browser);
		
		WebElement userConfirmationCheckEl = browser.findElement(userConfirmationCheckBy);
		OOGraphene.check(userConfirmationCheckEl, Boolean.valueOf(mandatory));
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public AdministrationPage setGroupConfirmationForAuthor(boolean mandatory) {
		By authorConfirmationCheckBy = By.xpath("//label/input[@name='mandatory.membership' and @value='author']");
		
		OOGraphene.waitElement(authorConfirmationCheckBy, browser);
		OOGraphene.scrollTo(authorConfirmationCheckBy, browser);
		
		WebElement authorConfirmationCheckEl = browser.findElement(authorConfirmationCheckBy);
		OOGraphene.check(authorConfirmationCheckEl, Boolean.valueOf(mandatory));
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public LicensesAdminstrationPage openLicenses() {
		selecCoreConfiguration();
		
		By licenseBy = By.cssSelector(".o_sel_license span.o_tree_level_label_leaf>a");
		browser.findElement(licenseBy).click();
		OOGraphene.waitBusy(browser);
		
		By licenseAdminBy = By.cssSelector(".o_sel_license_general");
		OOGraphene.waitElement(licenseAdminBy, browser);
		return new LicensesAdminstrationPage(browser);
	}
	
	public QuestionPoolAdminPage openQuestionPool() {
		selectAssessment();
		
		By poolBy = By.cssSelector(".o_sel_qpool span.o_tree_level_label_leaf>a");
		browser.findElement(poolBy).click();
		OOGraphene.waitBusy(browser);
		
		return new QuestionPoolAdminPage(browser).assertOnConfiguration();
	}
	
	public TaxonomyAdminPage openTaxonomy() {
		selectModules();
		
		By taxonomyBy = By.cssSelector(".o_sel_taxonomy span.o_tree_level_label_leaf>a");
		OOGraphene.waitElement(taxonomyBy, browser);
		browser.findElement(taxonomyBy).click();
		OOGraphene.waitBusy(browser);
		
		return new TaxonomyAdminPage(browser).assertOnTaxonomyList();
	}
}
