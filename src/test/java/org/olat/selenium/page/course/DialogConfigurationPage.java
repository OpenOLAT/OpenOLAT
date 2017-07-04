package org.olat.selenium.page.course;

import java.io.File;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 3 juil. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DialogConfigurationPage {
	
	private final WebDriver browser;
	
	public DialogConfigurationPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public DialogConfigurationPage selectConfiguration() {
		By dialogConfigBy = By.cssSelector("fieldset.o_sel_dialog_settings_upload");
		OOGraphene.selectTab(CourseEditorPageFragment.navBarNodeConfiguration, dialogConfigBy, browser);
		return this;
	}
	
	public DialogConfigurationPage uploadFile(File file) {
		By uploadBy = By.cssSelector("fieldset.o_sel_dialog_settings_upload a.o_sel_dialog_upload");
		browser.findElement(uploadBy).click();
		OOGraphene.waitBusy(browser);
		
		By inputBy = By.xpath("//div[@class='o_fileinput']/input[@type='file']");
		OOGraphene.uploadFile(inputBy, file, browser);
		
		By uploadButtonBy = By.cssSelector("fieldset.o_sel_dialog_settings_upload button.btn-primary");
		OOGraphene.waitElement(uploadButtonBy, browser);
		browser.findElement(uploadButtonBy).click();
		
		By rowBy = By.xpath("//table//tr/td[contains(text(),'" + file.getName() + "')]");
		OOGraphene.waitElement(rowBy, browser);
		
		return this;
	}

}
