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

import org.junit.Assert;
import org.olat.selenium.page.NavigationPage;
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/**
 * 
 * Initial date: 09.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RemindersPage {

	private final WebDriver browser;
	
	public RemindersPage(WebDriver browser) {
		this.browser = browser;
	}
	
	/**
	 * Check that the reminders list is present.
	 * @return
	 */
	public RemindersPage assertOnRemindersList() {
		By reminderListBy = By.className("o_sel_course_reminder_list");
		List<WebElement> reminderListEls = browser.findElements(reminderListBy);
		Assert.assertEquals(1, reminderListEls.size());
		return this;
	}
	
	
	public RemindersPage assertOnReminderInList(String title) {
		By rowBy = By.xpath("//fieldset[contains(@class,'o_sel_course_reminder_list')]//table//tr//td//a[contains(text(), '" + title + "')]");
		List<WebElement> reminderListEls = browser.findElements(rowBy);
		Assert.assertEquals(1, reminderListEls.size());
		return this;
	}
	
	/**
	 * Open the tools menu of the reminder specified by the title.
	 * 
	 * @param title
	 * @return
	 */
	public RemindersPage openActionMenu(String title) {
		By rowBy = By.xpath("//fieldset[contains(@class,'o_sel_course_reminder_list')]//table//tr[//td//a[contains(text(), '" + title + "')]]//td//a[contains(@class,'o_sel_course_reminder_tools')]");
		OOGraphene.waitElement(rowBy, browser);
		browser.findElement(rowBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Click in the tools the "Send reminders to all".
	 * 
	 * @return
	 */
	public RemindersPage sendRemindersToAll() {
		By sendBy = By.cssSelector("div.o_callout_content ul.o_dropdown a.o_sel_course_reminder_send");
		OOGraphene.waitElement(sendBy, browser);
		browser.findElement(sendBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitModalDialog(browser);
		
		By sendAllBy = By.cssSelector("div.modal-dialog a.o_sel_course_reminder_send_all");
		OOGraphene.waitElement(sendAllBy, browser);
		browser.findElement(sendAllBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	/**
	 * Click in the tools the "Show sent reminders".
	 * 
	 * @return
	 */
	public RemindersPage showSentReminders() {
		By sendBy = By.cssSelector("div.o_callout_content ul.o_dropdown a.o_sel_course_reminder_showsent");
		browser.findElement(sendBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public RemindersPage assertSentRemindersList(UserVO user, boolean in) {
		By rowBy = By.xpath("//div[contains(@class,'o_sel_course_sent_reminder_list')]//table//tr//td//a[contains(text(), '" + user.getFirstName() + "')]");
		List<WebElement> reminderListEls = browser.findElements(rowBy);
		if(in) {
			Assert.assertEquals(1, reminderListEls.size());
		} else {
			Assert.assertTrue(reminderListEls.isEmpty());
		}
		return this;
	}
	
	public RemindersPage clickToolbarBack() {
		browser.findElement(NavigationPage.toolbarBackBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Open the log
	 * 
	 * @return
	 */
	public RemindersPage openLog() {
		By logSegmentBy = By.cssSelector("a.o_sel_course_reminder_log_segment");
		browser.findElement(logSegmentBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public RemindersPage assertLogList(UserVO user, String reminderTitle, boolean in) {
		String xpath = "//div[contains(@class,'o_sel_course_sent_reminder_log_list')]//table//tr"
		     + "[//td//a[contains(text(), '" + user.getFirstName() + "')]]"
		     + "[//td//a[contains(text(), '" + reminderTitle + "')]]";
		
		By rowBy = By.xpath(xpath);
		List<WebElement> logListEls = browser.findElements(rowBy);
		if(in) {
			Assert.assertFalse(logListEls.isEmpty());
		} else {
			Assert.assertTrue(logListEls.isEmpty());
		}
		return this;
	}
	
	/**
	 * Create a new reminder
	 * 
	 * @return
	 */
	public RemindersPage addReminder() {
		By addReminderBy = By.cssSelector("a.o_sel_add_course_reminder");
		browser.findElement(addReminderBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitModalWizard(browser);
		return this;
	}
	
	public RemindersPage setDescription(String text) {
		By descBy = By.cssSelector(".o_sel_course_reminder_desc input[type='text']");
		browser.findElement(descBy).sendKeys(text);
		return this;
	}

	public RemindersPage setSubject(String text) {
		By subjectBy = By.cssSelector(".o_sel_course_reminder_subject input[type='text']");
		browser.findElement(subjectBy).sendKeys(text);
		return this;
	}
	
	public RemindersPage setTimeBasedRule(String pos, String type, int time, String unit) {
		//select type
		selectRuleType(pos, type);
		
		//enter time
		By timeBy = By.cssSelector("div.o_sel_row-" + pos + " input[type='text']");
		browser.findElement(timeBy).sendKeys(Integer.toString(time));
		
		//select time unit
		By selectUnitBy = By.xpath("//div[contains(@class,'o_sel_row-" + pos + "')]//select[contains(@name,'launchunit')]");
		WebElement unitSelect = browser.findElement(selectUnitBy);
		new Select(unitSelect).selectByValue(unit);
		OOGraphene.waitBusy(browser);
		
		return this;
	}
	
	/**
	 * Set the configuration of a repository entry role.
	 * 
	 * @param pos
	 * @param type
	 * @param role
	 * @return
	 */
	public RemindersPage setRoleBasedRule(String pos, String type, String role) {
		//select type
		selectRuleType(pos, type);
		
		//select role
		By selectRoleBy = By.xpath("//div[contains(@class,'o_sel_row-" + pos + "')]//select[contains(@name,'role.')]");
		WebElement roleSelect = browser.findElement(selectRoleBy);
		new Select(roleSelect).selectByValue(role);
		OOGraphene.waitBusy(browser);
		
		return this;
	}
	
	/**
	 * Add a rule after the specified row. Warning, the first
	 * position is 1.
	 * 
	 * @param pos
	 * @return
	 */
	public RemindersPage addRule(String type) {
		By selectTypeBy = By.xpath("//div[@id='o_coadd_rule_type_SELBOX']/select[contains(@name,'rule.type')]");
		WebElement typeSelect = browser.findElement(selectTypeBy);
		new Select(typeSelect).selectByValue(type);
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Select the rule type, it's the simple name of the class:
	 * RepositoryEntryLifecycleAfterValidFromRuleSPI,
	 * RepositoryEntryRoleRuleSPI...
	 * 
	 * @param pos
	 * @param type
	 */
	private void selectRuleType(String pos, String type) {
		By selectTypeBy = By.xpath("//div[contains(@class,'o_sel_row-" + pos + "')]//select[contains(@name,'rule.type')]");
		WebElement typeSelect = browser.findElement(selectTypeBy);
		new Select(typeSelect).selectByValue(type);
		OOGraphene.waitBusy(browser);
	}
	
	public RemindersPage nextToReview() {
		OOGraphene.nextStep(browser);
		OOGraphene.waitElement(By.cssSelector("div.o_reminder_rules_view"), browser);
		return this;
	}
	
	/**
	 * Check if the specified user is in the review list
	 * in the wizard, step review.
	 * 
	 * @param user A user
	 * @return Itself
	 */
	public RemindersPage assertOnReviewInList(UserVO user) {
		By firstNameBy = By.xpath("//div[contains(@class,'modal-body')]//table//td[text()[contains(.,'" + user.getFirstName() + "')]]");
		OOGraphene.waitElement(firstNameBy, browser);
		return this;
	}
	
	/**
	 * Go to the e-mail step of the wizard.
	 * 
	 * @return Itself
	 */
	public RemindersPage nextToEmail() {
		OOGraphene.nextStep(browser);
		OOGraphene.waitElement(By.cssSelector("div.o_sel_course_reminder_subject"), browser);
		return this;
	}
	
	/**
	 * Finish the wizard.
	 * 
	 * @return Itself
	 */
	public RemindersPage finish() {
		OOGraphene.finishStep(browser);
		return this;
	}
	
	/**
	 * Save the form to edit a reminder
	 * 
	 * @return
	 */
	public RemindersPage saveReminder() {
		By saveBy = By.cssSelector(".o_sel_course_reminder_config_buttons button.btn-primary");
		List<WebElement> saveEls = browser.findElements(saveBy);
		Assert.assertEquals(1, saveEls.size());
		saveEls.get(0).click();
		OOGraphene.waitBusy(browser);
		return this;
	}

}
