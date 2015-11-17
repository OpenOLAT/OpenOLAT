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
package org.olat.selenium.page.forum;

import java.util.List;

import org.junit.Assert;
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.selenium.page.portfolio.ArtefactWizardPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Drive the forum
 * 
 * 
 * Initial date: 01.07.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ForumPage {

	private WebDriver browser;
	
	public ForumPage(WebDriver browser) {
		this.browser = browser;
	}

	/**
	 * Get the forum from a course element.
	 * 
	 * @param browser
	 * @return
	 */
	public static ForumPage getCourseForumPage(WebDriver browser) {
		By forumBy = By.cssSelector("div.o_course_run div.o_forum");
		List<WebElement> forumEl = browser.findElements(forumBy);
		Assert.assertFalse(forumEl.isEmpty());
	
		By mainBy = By.cssSelector("div.o_course_run");
		WebElement main = browser.findElement(mainBy);
		Assert.assertTrue(main.isDisplayed());
		return new ForumPage(browser);
	}
	
	public static ForumPage getGroupForumPage(WebDriver browser) {
		By forumBy = By.cssSelector("div.o_forum");
		List<WebElement> forumEl = browser.findElements(forumBy);
		Assert.assertFalse(forumEl.isEmpty());
		return new ForumPage(browser);
	}
	
	/**
	 * Create a new thread
	 * 
	 * @param title
	 * @param content
	 * @return
	 */
	public ForumPage createThread(String title, String content) {
		By newThreadBy = By.className("o_sel_forum_thread_new");
		WebElement newThreadButton = browser.findElement(newThreadBy);
		newThreadButton.click();
		OOGraphene.waitBusy(browser);
		
		//fill the form
		By titleBy = By.cssSelector("div.modal-content form input[type='text']");
		WebElement titleEl = browser.findElement(titleBy);
		titleEl.sendKeys(title);
		
		OOGraphene.tinymce(content, browser);
		
		//save
		By saveBy = By.cssSelector("div.modal-content form button.btn-primary");
		WebElement saveButton = browser.findElement(saveBy);
		saveButton.click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public ForumPage openThread(String title) {
		By threadBy = By.xpath("//table[contains(@class,'table')]//tr//a[text()='" + title + "']");
		browser.findElement(threadBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public ForumPage openThreadInPeekview(String title) {
		By threadBy = By.xpath("//div[contains(@class,'o_forum_peekview_message')]//a[span[text()='" + title + "']]");
		browser.findElement(threadBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public ForumPage flatView() {
		By flatBy = By.cssSelector("a.o_forum_all_flat_messages");
		browser.findElement(flatBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public ForumPage assertMessageBody(String text) {
		By messageBodyBy = By.className("o_forum_message_body");
		List<WebElement> messages = browser.findElements(messageBodyBy);
		boolean found = false;
		for(WebElement message:messages) {
			if(message.getText().contains(text)) {
				found = true;
			}
		}
		Assert.assertTrue(found);
		return this;
	}
	
	public ForumPage waitMessageBody(String text) {
		By messageBy = By.xpath("//div[contains(@class,'o_forum_message_body')][//p[contains(text(),'" + text + "')]]");
		OOGraphene.waitElement(messageBy, 10, browser);
		return this;
	}

	public ForumPage replyToMessage(String reference, String title, String reply) {
		replyToMessageNoWait(reference, title, reply);
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public ForumPage replyToMessageNoWait(String reference, String title, String reply) {
		By replyBy = By.xpath("//div[contains(@class,'o_forum_message')][//h4[contains(text(),'" + reference + "')]]//a[contains(@class,'o_sel_forum_reply')]");
		browser.findElement(replyBy).click();
		OOGraphene.waitBusy(browser);
		
		if(title != null) {
			By titleBy = By.cssSelector(".o_sel_forum_message_title input[type='text']");
			browser.findElement(titleBy).sendKeys(title);
		}
		OOGraphene.tinymce(reply, browser);
		
		By saveBy = By.cssSelector("fieldset.o_sel_forum_message_form button.btn-primary");
		browser.findElement(saveBy).click();
		return this;
	}
	
	/**
	 * Add the thread to my artefacts
	 * 
	 */
	public ArtefactWizardPage addAsArtfeact() {
		By addAsArtefactBy = By.className("o_eportfolio_add");
		WebElement addAsArtefactButton = browser.findElement(addAsArtefactBy);
		addAsArtefactButton.click();
		OOGraphene.waitBusy(browser);
		return ArtefactWizardPage.getWizard(browser);
	}
}