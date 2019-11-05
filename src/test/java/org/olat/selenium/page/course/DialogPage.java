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

import java.io.File;

import org.olat.selenium.page.forum.ForumPage;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 3 juil. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DialogPage {
	
	private final WebDriver browser;
	
	public DialogPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public DialogPage uploadFile(File file) {
		By uploadBy = By.cssSelector("div.o_sel_dialog a.o_sel_dialog_upload");
		OOGraphene.waitElement(uploadBy, browser);
		browser.findElement(uploadBy).click();
		OOGraphene.waitModalDialog(browser);

		By inputBy = By.cssSelector("div.modal-dialog div.o_fileinput input[type='file']");
		OOGraphene.uploadFile(inputBy, file, browser);
		OOGraphene.waitBusy(browser);
		
		By saveButtonBy = By.cssSelector("div.o_sel_upload_buttons button.btn-primary");
		browser.findElement(saveButtonBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public DialogPage assertOnFile(String filename) {
		By fileBy = By.xpath("//div[contains(@class,'o_sel_dialog')]//table//tr/td/a[contains(text(),'" + filename + "')]");
		OOGraphene.waitElement(fileBy, browser);
		return this;
	}
	
	/**
	 * Return back to the list of dialog elements from a selected one.
	 * 
	 * @return Itself
	 */
	public DialogPage back() {
		By backBy = By.cssSelector("div.o_sel_dialog a.o_link_back");
		browser.findElement(backBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public ForumPage openForum(String filename) {
		By openForumBy = By.xpath("//table//tr[td/a[contains(text(),'" + filename + "')]]/td/a[contains(@onclick,'forum')]");
		browser.findElement(openForumBy).click();
		OOGraphene.waitBusy(browser);
		By forumBy = By.cssSelector("div.o_sel_dialog div.o_sel_forum");
		OOGraphene.waitElement(forumBy, browser);
		return new ForumPage(browser);
	}
	
	public ForumPage createNewThread(String title, String content) {
		ForumPage forum = new ForumPage(browser);
		return forum.createThread(title, content, null);
	}

}
