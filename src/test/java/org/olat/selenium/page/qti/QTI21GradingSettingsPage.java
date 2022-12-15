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
package org.olat.selenium.page.qti;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 10 juin 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21GradingSettingsPage {
	
	private final WebDriver browser;
	
	public QTI21GradingSettingsPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public QTI21GradingSettingsPage enable() {
		By enableBy = By.xpath("//fieldset[contains(@class,'o_sel_repo_grading_enable')]//input[@name='grading.repo.enabled'][@type='checkbox']");
		OOGraphene.waitElement(enableBy, browser);
		browser.findElement(enableBy).click();
		OOGraphene.waitBusy(browser);
		
		By reminderBy = By.className("o_sel_repo_grading_notification_type");
		OOGraphene.waitElement(reminderBy, browser);
		return this;
	}
	
	public QTI21GradingSettingsPage setPeriods(int gradingPeriod, int firstReminder, int secondReminder) {
		By gradingPeriodBy = By.cssSelector("input.o_sel_repo_grading_period[type='text']");
		browser.findElement(gradingPeriodBy).sendKeys(Integer.toString(gradingPeriod));
		
		By firstReminderBy = By.cssSelector("input.o_sel_repo_grading_first_reminder_period[type='text']");
		browser.findElement(firstReminderBy).sendKeys(Integer.toString(firstReminder));
		
		By secondReminderBy = By.cssSelector("input.o_sel_repo_grading_second_reminder_period[type='text']");
		browser.findElement(secondReminderBy).sendKeys(Integer.toString(secondReminder));
		return this;
	}
	
	public QTI21GradingSettingsPage selectMailTemplate() {
		By templatesMenuCaret = By.cssSelector("button.o_sel_repo_grading_templates");
		browser.findElement(templatesMenuCaret).click();
		
		By templatesMenu = By.cssSelector("ul.o_sel_repo_grading_templates");
		OOGraphene.waitElement(templatesMenu, browser);
		
		By firstTemplateBy = By.xpath("//ul[contains(@class,'o_sel_repo_grading_templates')]/li[1]/a");
		OOGraphene.waitElement(firstTemplateBy, browser);
		browser.findElement(firstTemplateBy).click();
		OOGraphene.waitBusy(browser);
		
		By notificationSubjectBy = By.xpath("//div[contains(@class,'o_sel_repo_grading_notification_subject')]//input[@value[string()]]");
		OOGraphene.waitElement(notificationSubjectBy, browser);
		return this;
	}
	
	public QTI21GradingSettingsPage save() {
		By saveBy = By.cssSelector("fieldset.o_sel_repo_grading_settings_form button.btn.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}

}
