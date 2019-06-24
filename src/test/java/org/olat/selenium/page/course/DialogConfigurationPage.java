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
		OOGraphene.selectTab("o_node_config", dialogConfigBy, browser);
		return this;
	}
	
	public DialogConfigurationPage uploadFile(File file) {
		By uploadBy = By.cssSelector("fieldset.o_sel_dialog_settings_upload a.o_sel_dialog_upload");
		browser.findElement(uploadBy).click();
		OOGraphene.waitModalDialog(browser);
		
		By inputBy = By.xpath("//div[contains(@class,'modal-body')]//div[@class='o_fileinput']/input[@type='file']");
		OOGraphene.uploadFile(inputBy, file, browser);
		OOGraphene.waitBusy(browser);
		
		By uploadButtonBy = By.cssSelector("div.modal-body div.o_sel_upload_buttons button.btn-primary");
		OOGraphene.waitElement(uploadButtonBy, browser);
		browser.findElement(uploadButtonBy).click();
		
		By rowBy = By.xpath("//table//tr/td/a[contains(text(),'" + file.getName() + "')]");
		OOGraphene.waitElement(rowBy, browser);
		
		return this;
	}

}
