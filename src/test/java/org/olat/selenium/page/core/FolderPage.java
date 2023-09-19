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
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.olat.core.gui.render.URLBuilder;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

/**
 * 
 * Drive the folder component of contact
 * 
 * Initial date: 27.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FolderPage {
	
	public static final By folderBy = By.cssSelector("div.o_briefcase_folder");
	
	private final WebDriver browser;
	
	public FolderPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public FolderPage assertOnFolderCmp() {
		By folderCmpBy = By.className("o_briefcase_foldercomp");
		OOGraphene.waitElement(folderCmpBy, browser);
		return this;
	}
	
	public FolderPage createDirectory(String name) {
		By newFolderBy = By.className("b_bc_newfolder");
		browser.findElement(newFolderBy).click();
		OOGraphene.waitModalDialog(browser);
		
		By folderNameBy = By.cssSelector(".o_sel_folder_new_folder_name input[type='text']");
		OOGraphene.waitElement(folderNameBy, browser);
		browser.findElement(folderNameBy).sendKeys(name);
		
		By createBy = By.cssSelector(".o_sel_folder_new_folder button.btn-primary");
		browser.findElement(createBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	public FolderPage assertOnDirectory(String name) {
		// encode name same was as in UI
		String escapedName = URLBuilder.encodeUrl(name);
		By directoryBy = By.xpath("//table[contains(@class,'o_bc_table')]//a[contains(@onclick,'" + escapedName + "')]");
		List<WebElement> directoryEls = browser.findElements(directoryBy);
		Assert.assertFalse(directoryEls.isEmpty());
		return this;
	}
	
	public FolderPage createHTMLFile(String name, String content) {
		By newFileBy = By.className("b_bc_newfile");
		browser.findElement(newFileBy).click();
		OOGraphene.waitModalDialog(browser);
		
		String startWindow = browser.getWindowHandle();
		
		// create a new HTML document
		By typeBy = By.cssSelector("div.o_radio_cards.o_sel_folder_new_doc_type");
		OOGraphene.waitElement(typeBy, browser);
		
		By moreBy = By.cssSelector("div.o_sel_folder_new_doc_type .o_show_more_radios>a");
		browser.findElement(moreBy).click();

		By htmlTypeBy = By.cssSelector("div.o_radio_cards.o_sel_folder_new_doc_type input[name='create.doc.format'][value='html']");
		OOGraphene.waitElement(htmlTypeBy, browser);
		browser.findElement(htmlTypeBy).click();
		
		By filenameBy = By.cssSelector(".o_sel_folder_new_doc_name input[type='text']");
		browser.findElement(filenameBy).sendKeys(name);

		By createBy = By.cssSelector(".o_sel_folder_new_file button.btn-primary");
		browser.findElement(createBy).click();
		OOGraphene.waitBusy(browser);
		
		List<String> allHandles = new ArrayList<>(browser.getWindowHandles());
		allHandles.remove(startWindow);
		String editorHandle = allHandles.get(0);
		
		// switch to the editor and write the text
		browser.switchTo().window(editorHandle);
		OOGraphene.tinymceExec(content, browser);
		
		// save the HTML document
		By saveAndCloseDirtyBy = By.cssSelector(".o_htmleditor #o_button_saveclose a.btn.o_button_dirty");
		OOGraphene.waitElement(saveAndCloseDirtyBy, browser);
		browser.findElement(saveAndCloseDirtyBy).click();
		
		browser.switchTo().window(startWindow);
		return this;
	}
	
	public FolderPage uploadFile(File file) {
		try {
			By newFileBy = By.className("o_bc_upload");
			OOGraphene.waitElement(newFileBy, browser);
			browser.findElement(newFileBy).click();
			OOGraphene.waitModalDialog(browser);
			
			By inputBy = By.cssSelector("div.modal-dialog div.o_fileinput input[type='file']");
			OOGraphene.uploadFile(inputBy, file, browser);
			By uploadedBy = By.cssSelector("div.modal-dialog div.o_sel_file_uploaded");
			OOGraphene.waitElementSlowly(uploadedBy, 5, browser);
			OOGraphene.waitingALittleBit();
			
			By saveButtonBy = By.cssSelector("div.o_sel_upload_buttons button.btn-primary");
			OOGraphene.moveAndClick(saveButtonBy, browser);
			OOGraphene.waitModalDialogDisappears(browser);
		} catch (Error | Exception e) {
			OOGraphene.takeScreenshot("uploadFile", browser);
			throw e;
		}
		return this;
	}
	
	public FolderPage unzipFile(String filename) {
		By unzipBy = By.xpath("//button[contains(@onclick,'o_TableMultiActionEvent') and contains(@onclick,'actionunzip')]");
		browser.findElement(unzipBy).click();
		
		String folderName = filename.replace(".zip", "");
		By unzippedFolderBy = By.xpath("//div[@class='o_briefcase_folder']//tr/td/a[text()[contains(.,'" + folderName + "')]][i[contains(@class,'o_filetype_folder')]]");
		OOGraphene.waitElement(unzippedFolderBy, browser);
		return this;
	}
	
	public FolderPage selectFile(String filename) {
		By selectBy = By.xpath("//div[@class='o_briefcase_folder']//tr[td/a[text()[contains(.,'" + filename + "')]]]/td/input[@type='checkbox']");
		WebElement selectEl = browser.findElement(selectBy);
		OOGraphene.check(selectEl, Boolean.TRUE);
		return this;
	}
	
	public FolderPage selectRootDirectory() {
		By rootBy = By.xpath("//div[@class='o_briefcase_folder']//ol[@class='breadcrumb']/li[1]/a");
		OOGraphene.waitElement(rootBy, browser);
		
		// tooltip of the image sometimes appears and block the click
		By tooltipBy = By.cssSelector("div.tooltip-inner");
		WebElement rootEl = browser.findElement(rootBy);
		List<WebElement> tooltipEls = browser.findElements(tooltipBy);
		if(tooltipEls.size() > 0) {
			new Actions(browser)
				.moveToElement(rootEl)
				.build()
				.perform();
			OOGraphene.waitElementDisappears(tooltipBy, 5, browser);
		}
		
		browser.findElement(rootBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public FolderPage assertOnFile(String filename) {
		By fileBy = By.xpath("//table[contains(@class,'o_bc_table')]//a[contains(text(),'" + filename + "')]");
		OOGraphene.waitElement(fileBy, browser);
		return this;
	}
}