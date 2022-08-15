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
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/**
 * 
 * Initial date: 12.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InfoMessageCEPage {

	private final WebDriver browser;
	
	public InfoMessageCEPage(WebDriver browser) {
		this.browser = browser;
	}
	
	/**
	 * Open the wizard to create a new message.
	 * 
	 * @return This page
	 */
	public InfoMessageCEPage createMessage() {
		By createBy = By.className("o_sel_course_info_create_msg");
		browser.findElement(createBy).click();
		OOGraphene.waitModalDialog(browser);
		return this;
	}
	
	/**
	 * Set / replace the title and set the content of the message
	 * form.
	 * 
	 * @param title
	 * @param content
	 * @return This page
	 */
	public InfoMessageCEPage setMessage(String title, String content) {
		By titleBy = By.cssSelector(".o_sel_info_title input[type='text']");
		WebElement titleEl = browser.findElement(titleBy);
		titleEl.clear();
		titleEl.sendKeys(title);
		OOGraphene.tinymce(content, browser);
		return this;
	}
	
	/**
	 * Click next in the create wizard.
	 * 
	 * @return This page
	 */
	public InfoMessageCEPage next() {
		OOGraphene.nextStep(browser);
		OOGraphene.waitElement(By.cssSelector("fieldset.o_sel_info_contact"), browser);
		return this;
	}
	
	/**
	 * Last step of the create wizard.
	 * 
	 * @return This page
	 */
	public InfoMessageCEPage finish() {
		OOGraphene.finishStep(browser);
		return this;
	}
	
	/**
	 * Create quickly a message with the 2 steps wizard.
	 * 
	 * @param title
	 * @param content
	 * @return This page
	 */
	public InfoMessageCEPage quickMessage(String title, String content) {
		createMessage()
			.setMessage(title, content)
			.next()
			.finish();
		return this;
	}
	
	/**
	 * Check if a message exists with the specified title.
	 * 
	 * @param title
	 * @return This page
	 */
	public InfoMessageCEPage assertOnMessageTitle(String title) {
		By titleBy = By.xpath("//div[contains(@class,'o_msg')]//div//h3[contains(text(),'" + title + "')]");
		String titleEl = browser.findElement(titleBy).getText();
		Assert.assertTrue(titleEl.contains(title));
		return this;
	}
	
	/**
	 * Click the edit button of the message specified by its title.
	 * 
	 * @param title
	 * @return This page
	 */
	public InfoMessageCEPage editMessage(String title) {
		By editBy = By.xpath("//div[contains(@class,'o_msg')][div//h3[contains(text(),'" + title + "')]]//a[contains(@class,'o_sel_info_edit_msg')]");
		OOGraphene.waitElement(editBy, browser);
		browser.findElement(editBy).click();
		OOGraphene.waitModalDialog(browser);
		return this;
	}
	
	/**
	 * Save the edit form.
	 * 
	 * @return This page
	 */
	public InfoMessageCEPage save() {
		By saveBy = By.cssSelector(".o_sel_info_form button.btn.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Click the delete button of the message specified by its title.
	 * 
	 * @param title
	 * @return This page
	 */
	public InfoMessageCEPage deleteMessage(String title) {
		By editBy = By.xpath("//div[contains(@class,'o_msg')][div//h3[contains(text(),'" + title + "')]]//a[contains(@class,'o_sel_info_delete_msg')]");
		List<WebElement> editEls = browser.findElements(editBy);
		Assert.assertFalse(editEls.isEmpty());
		editEls.get(0).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Click the first button of the modal dialog.
	 * 
	 * @return This page
	 */
	public InfoMessageCEPage confirmDelete() {
		By buttonsBy = By.cssSelector("div.modal-dialog div.modal-footer a.btn.btn-default");
		List<WebElement> buttonEls = browser.findElements(buttonsBy);
		Assert.assertEquals(2, buttonEls.size());
		buttonEls.get(0).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * @return The number of displayed messages
	 */
	public int countMessages() {
		By titleBy = By.cssSelector(".o_msg .o_head h3.o_title");
		return browser.findElements(titleBy).size();
	}
	
	/**
	 * Click the new messages button. Raise an error if the button is not there.
	 * 
	 * @return  This page
	 */
	public InfoMessageCEPage newMessages() {
		By newMessagesBy = By.className("o_sel_course_info_new_msgs");
		browser.findElement(newMessagesBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public InfoMessageCEPage scrollToMessage(String title) {
		By titleBy = By.xpath("//div[contains(@class,'o_msg')]//div//h3[contains(text(),'" + title + "')]");
		OOGraphene.scrollTo(titleBy, browser);
		return this;
	}
	
	/**
	 * Click the old messages button. Raise an error if the button is not there.
	 * 
	 * @return This page
	 */
	public InfoMessageCEPage oldMessages() {
		By oldMessagesBy = By.cssSelector("a.o_sel_course_info_old_msgs");
		OOGraphene.waitElement(oldMessagesBy, browser);
		OOGraphene.click(oldMessagesBy, browser);
		OOGraphene.scrollTop(browser);
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Select the configuration tab of the course element specific
	 * to the info message course element.
	 * 
	 * @return This page
	 */
	public InfoMessageCEPage selectConfiguration() {
		By configBy = By.className("o_sel_course_info_form");
		return selectTab(configBy);
	}
	
	/**
	 * Configure the course element and save it.
	 * 
	 * @param length
	 * @return This page
	 */
	public InfoMessageCEPage configure(int length) {
		By lengthBy = By.id("o_fiopane_tab_infos_config_max_shown_SELBOX");
		WebElement lengthSelect = browser.findElement(lengthBy);
		new Select(lengthSelect).selectByValue(Integer.toString(length));
		OOGraphene.waitBusy(browser);
		
		By selectedLengthBy = By.xpath("//select[@id='o_fiopane_tab_infos_config_max_shown_SELBOX']/option[@value='" + length + "'][@selected]");
		OOGraphene.waitElement(selectedLengthBy, browser);
		return this;
	}
	
	private InfoMessageCEPage selectTab(By tabBy) {
		OOGraphene.selectTab("o_node_config", tabBy, browser);
		return this;
	}
}
