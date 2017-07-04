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
	
	public ForumPage openForum(String filename) {
		By openForumBy = By.xpath("//table//tr[td/a[contains(text(),'" + filename + "')]]/td/a[contains(@onclick,'startforum')]");
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
