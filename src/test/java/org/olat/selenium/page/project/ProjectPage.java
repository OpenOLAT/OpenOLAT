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
		OOGraphene.waitElement(titleBy, browser);
		WebElement titleEl = browser.findElement(titleBy);
		titleEl.sendKeys(title);
		
		By organisationBy = By.cssSelector("div.o_sel_proj_project_organisation a.o_msf_button");
		browser.findElement(organisationBy).click();
		OOGraphene.waitCallout(browser, ".o_sel_multiselect");
		
		By openOlatOrgBy = By.xpath("//div[contains(@class,'popover')]//div[contains(@class,'o_sel_multiselect')]//label[text()[contains(.,'" + organisation + "')]]/input[@type='checkbox']");
		WebElement openOlatOrgEl = browser.findElement(openOlatOrgBy);
		OOGraphene.check(openOlatOrgEl, Boolean.TRUE);
		OOGraphene.waitBusy(browser);
		
		By updateBy = By.cssSelector(".popover a.o_sel_flexiql_update");
		browser.findElement(updateBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitElementDisappears(updateBy, 5, browser);
		
		if(StringHelper.containsNonWhitespace(teaser)) {
			By teaserBy = By.cssSelector("fieldset.o_sel_proj_project_form input.o_sel_proj_project_teaser[type='text']");
			OOGraphene.waitElement(teaserBy, browser);
			browser.findElement(teaserBy).sendKeys(teaser);
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
	
	public ProjectPage addNotice(String title, String text) {
		By addNoticeBy = By.cssSelector(".o_proj_dashboard .o_proj_title a.o_sel_proj_add_notice");
		browser.findElement(addNoticeBy).click();
		OOGraphene.waitModalDialog(browser);
		
		By titleBy = By.cssSelector("div.o_sel_notice_title input[type='text']");
		OOGraphene.waitElement(titleBy, browser);
		browser.findElement(titleBy).sendKeys(title);
		
		By textBy = By.cssSelector("div.o_sel_notice_text div.editor[contenteditable=true]");
		browser.findElement(textBy).sendKeys(text);
		
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

}
