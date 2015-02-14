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

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
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
	
	@Drone
	private WebDriver browser;

	/**
	 * Get the forum from a course element.
	 * 
	 * @param browser
	 * @return
	 */
	public static ForumPage getForumPage(WebDriver browser) {
		By forumBy = By.cssSelector("div.o_course_run div.o_forum");
		List<WebElement> forumEl = browser.findElements(forumBy);
		Assert.assertFalse(forumEl.isEmpty());
	
		By mainBy = By.cssSelector("div.o_course_run");
		WebElement main = browser.findElement(mainBy);
		return Graphene.createPageFragment(ForumPage.class, main);
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