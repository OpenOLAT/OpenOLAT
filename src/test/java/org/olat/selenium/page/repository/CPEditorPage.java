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
package org.olat.selenium.page.repository;

import java.io.File;

import org.junit.Assert;
import org.olat.selenium.page.NavigationPage;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 04.12.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CPEditorPage {
	
	private WebDriver browser;
	
	public CPEditorPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public CPEditorPage clickRoot() {
		By rootBy = By.cssSelector("ul.o_tree_l0 span.o_tree_link.o_tree_l0 a");
		browser.findElement(rootBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public CPEditorPage selectPage(String title) {
		//The i is
		By pageBy = By.xpath("//a[span[text()[contains(.,'" + title + "')]]]//i");
		browser.findElement(pageBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public CPEditorPage deletePage() {
		By deleteBy = By.cssSelector("a.o_sel_cp_delete_link");
		browser.findElement(deleteBy).click();
		
		//confirm
		OOGraphene.waitModalDialog(browser);
		By deleteMenuAndFileBy = By.xpath("//div[contains(@class,'modal-dialog')]//a[contains(@onclick,'link_0')]");
		browser.findElement(deleteMenuAndFileBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	public CPEditorPage newPage(String title) {
		By newBy = By.cssSelector("a.o_sel_cp_new_link");
		browser.findElement(newBy).click();
		OOGraphene.waitModalDialog(browser);
		return fillMetadataForm(title);
	}
	
	public CPEditorPage editMetadata(String title) {
		By metadataBy = By.cssSelector("a.o_sel_cp_edit_metadata");
		browser.findElement(metadataBy).click();
		OOGraphene.waitModalDialog(browser);
		return fillMetadataForm(title);
	}
	
	public CPEditorPage importPage(File page) {
		By metadataBy = By.cssSelector("a.o_sel_cp_import_link");
		browser.findElement(metadataBy).click();
		OOGraphene.waitModalDialog(browser);
		
		//wait popup
		By metadataPopupBy = By.cssSelector("fieldset.o_sel_cp_import");
		OOGraphene.waitElement(metadataPopupBy, browser);
		
		By inputBy = By.cssSelector("fieldset.o_sel_cp_import .o_fileinput input[type='file']");
		OOGraphene.uploadFile(inputBy, page, browser);
		OOGraphene.waitBusy(browser);
		By uploadedBy = By.cssSelector("fieldset.o_sel_cp_import .o_sel_file_uploaded");
		OOGraphene.waitElement(uploadedBy, browser);
		
		//ok save
		By saveBy = By.cssSelector("fieldset.o_sel_cp_import button.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public CPEditorPage closeMetadataForm() {
		//wait popup
		By metadataPopupBy = By.cssSelector("fieldset.o_sel_cp_metadata");
		OOGraphene.waitElement(metadataPopupBy, browser);
		
		//ok save
		By saveBy = By.cssSelector("fieldset.o_sel_cp_metadata button.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
		
	public CPEditorPage fillMetadataForm(String title) {
		//wait popup
		By metadataPopupBy = By.cssSelector("fieldset.o_sel_cp_metadata");
		OOGraphene.waitElement(metadataPopupBy, 2, browser);
		
		//write title
		By titleBy = By.cssSelector(".o_sel_cp_title input[type='text']");
		WebElement titleEl = browser.findElement(titleBy);
		titleEl.clear();
		titleEl.sendKeys(title);
		
		//ok save
		By saveBy = By.cssSelector("fieldset.o_sel_cp_metadata button.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	public CPPage clickToolbarBack() {
		browser.findElement(NavigationPage.toolbarBackBy).click();
		OOGraphene.waitBusy(browser);
		
		WebElement main = browser.findElement(By.id("o_main_wrapper"));
		Assert.assertTrue(main.isDisplayed());
		return new CPPage(browser);
	}

}
