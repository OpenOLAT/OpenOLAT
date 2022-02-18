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
 * Initial date: 2 nov. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DocumentConfigurationPage {

	private final WebDriver browser;
	
	public DocumentConfigurationPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public DocumentConfigurationPage selectConfiguration() {
		By dialogConfigBy = By.cssSelector("fieldset.o_sel_document_settings_upload");
		OOGraphene.selectTab("o_node_config", dialogConfigBy, browser);
		return this;
	}
	
	public DocumentConfigurationPage uploadDocument(File file) {
		By inputBy = By.xpath("//fieldset[contains(@class,'o_sel_document_settings_upload')]//div[contains(@class,'o_fileinput')]/input[@type='file']");
		OOGraphene.uploadFile(inputBy, file, browser);
		return assertOnPreview(file.getName());
	}
	

	public DocumentConfigurationPage selectDocument(String name) {
		By inputBy = By.cssSelector("fieldset.o_sel_document_settings_upload a.o_sel_doc_select_repository_entry");
		OOGraphene.waitElement(inputBy, browser);
		browser.findElement(inputBy).click();
		OOGraphene.waitModalDialog(browser);
		
		//find the row
		By rowBy = By.xpath("//div[contains(@class,'')]//div[contains(@class,'o_segments_content')]//table[contains(@class,'o_table')]//tr/td/a[text()[contains(.,'" + name + "')]]");
		OOGraphene.waitElement(rowBy, browser);
		browser.findElement(rowBy).click();
		OOGraphene.waitBusy(browser);
		
		//confirm
		By confirmBy = By.xpath("//div[contains(@class,'modal-dialog')]//a[contains(@onclick,'link_0')]");
		OOGraphene.waitElement(confirmBy, browser);
		browser.findElement(confirmBy).click();
		OOGraphene.waitBusy(browser);
		
		return assertOnPreview(name);
	}
	
	public DocumentConfigurationPage assertOnPreview(String name) {
		By previewBy = By.xpath("//div[contains(@class,'o_cnd_document')]/div[contains(@class,'o_cnd_header')]/h4[text()[contains(.,'" + name + "')]]");
		OOGraphene.waitElement(previewBy, browser);
		return this;
	}
	
	

}
