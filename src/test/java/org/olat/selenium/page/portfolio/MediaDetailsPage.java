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
package org.olat.selenium.page.portfolio;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;


/**
 * 
 * Initial date: 08.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaDetailsPage {
	
	private final WebDriver browser;

	public MediaDetailsPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public MediaDetailsPage assertOnMediaDetails(String title) {
		By titleBy = By.xpath("//div//h2[contains(text(),'" + title + "')]");
		OOGraphene.waitElement(titleBy, 5, browser);
		List<WebElement> titleEls = browser.findElements(titleBy);
		Assert.assertFalse(titleEls.isEmpty());
		return this;
	}
	
	public MediaDetailsPage addNewVersion() {
		By addVersionBy = By.cssSelector("div.o_button_group a.o_sel_set_version");
		OOGraphene.waitElement(addVersionBy, browser);
		browser.findElement(addVersionBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public MediaDetailsPage assertOnLogEntries(int numOfEntries) {
		By numOfVersions = By.xpath("//div[contains(@class,'o_sel_logs')]//table/tbody[count(child::tr) = " + numOfEntries + "]");
		OOGraphene.waitElement(numOfVersions, browser);
		return this;
	}
	
	public MediaDetailsPage replaceMedia(File file) {
		By uploadVersionBy = By.cssSelector("div.o_button_group a.o_sel_upload_version");
		OOGraphene.waitElement(uploadVersionBy, browser);
		browser.findElement(uploadVersionBy).click();
		OOGraphene.waitModalDialog(browser);
		
		By inputBy = By.cssSelector("fieldset.o_sel_upload_file_form .o_fileinput input[type='file']");
		OOGraphene.uploadFile(inputBy, file, browser);
		OOGraphene.waitBusy(browser);
		By uploadedBy = By.cssSelector("fieldset.o_sel_upload_file_form .o_sel_file_uploaded");
		OOGraphene.waitElement(uploadedBy, browser);
		
		By saveBy = By.cssSelector("fieldset.o_sel_upload_file_form .o_sel_buttons button.btn.btn-primary");
		OOGraphene.click(saveBy, browser);
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}

}
