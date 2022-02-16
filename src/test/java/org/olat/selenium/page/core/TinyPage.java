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

import java.io.File;
import java.time.Duration;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * 
 * Initial date: 28 oct. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TinyPage {
	
	private WebDriver browser;
	
	public TinyPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public TinyPage setContent(String content) {
		OOGraphene.tinymce(content, browser);
		return this;
	}
	
	public TinyPage uploadImage(File image) {
		By tinyImageButtonBy = By.xpath("//div[@class='o_richtext_mce']//div[contains(@class,'mce-container-body')]//button[i[contains(@class,'mce-i-image')]]");
		OOGraphene.waitElement(tinyImageButtonBy, browser);
		browser.findElement(tinyImageButtonBy).click();
		
		By windowBy = By.cssSelector("div.mce-window");
		OOGraphene.waitElement(windowBy, browser);
		OOGraphene.waitingALittleLonger();// wait calculation of position...
		By tinyUploadButtonBy = By.xpath("//div[contains(@class,'mce-window')]//button[i[contains(@class,'mce-i-browse')]]");
		OOGraphene.waitElement(tinyUploadButtonBy, browser);
		browser.findElement(tinyUploadButtonBy).click();
		OOGraphene.waitTopModalDialog(browser);
		
		By uploadButtonBy = By.xpath("//div[@id='o_fc_select']/a[contains(@onclick,'o_fc_upload')]");
		OOGraphene.waitElement(uploadButtonBy, browser);
		browser.findElement(uploadButtonBy).click();
		
		By uploadPanelBy = By.xpath("//div[@id='o_fc_upload']//form");
		OOGraphene.waitElement(uploadPanelBy, browser);
	
		By inputBy = By.cssSelector("#o_fc_upload .o_fileinput input[type='file']");
		OOGraphene.uploadFile(inputBy, image, browser);
		OOGraphene.waitBusy(browser);
		By imageBy = By.xpath("//div[@class='o_filemeta'][text()[contains(.,'" + image.getName() + "')]]");
		OOGraphene.waitElement(imageBy, browser);
		
		By saveButtonBy = By.cssSelector("#o_fc_upload div.o_sel_upload_buttons button.btn-primary");
		browser.findElement(saveButtonBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitTopModalDialogDisappears(browser);
		
		By tinyOkButtonBy = By.cssSelector("div.mce-foot div.mce-primary button");
		browser.findElement(tinyOkButtonBy).click();
		waitTinyDialogDisappears();
		OOGraphene.waitingALittleLonger();// let some time to Tiny
		
		return this;
	}
	
	public TinyPage saveContent() {
		By saveAndCloseBy = By.cssSelector("div.o_htmleditor #o_button_saveclose a");
		OOGraphene.waitElement(saveAndCloseBy, browser);
		browser.findElement(saveAndCloseBy).click();
		
		OOGraphene.waitBusy(browser);
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	private void waitTinyDialogDisappears() {
		By modalBy = By.xpath("//div[contains(@class,'mce-window-body')]");
		new WebDriverWait(browser, Duration.ofSeconds(5))
			.withTimeout(Duration.ofSeconds(5)).pollingEvery(Duration.ofMillis(200))
			.until(ExpectedConditions.invisibilityOfElementLocated(modalBy));
	}

}
