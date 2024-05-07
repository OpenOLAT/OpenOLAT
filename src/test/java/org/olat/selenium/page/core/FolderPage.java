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

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

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
	
	public FolderPage assertOnFolderV2() {
		By folderCmpBy = By.cssSelector(".o_folder .o_folder_table");
		OOGraphene.waitElement(folderCmpBy, browser);
		return this;
	}
	
	public FolderPage createDirectoryV2(String name) {
		By newFileBy = By.cssSelector(".o_folder_create_group>div>button.btn");
		OOGraphene.waitElement(newFileBy, browser);
		browser.findElement(newFileBy).click();
		
		By calloutBy = By.cssSelector(".o_folder_create_group ul.dropdown-menu");
		OOGraphene.waitElement(calloutBy, browser);
		
		By newFolderBy = By.xpath("//div[contains(@class,'o_folder_create_group')]//ul[contains(@class,'dropdown-menu')]/li/a[i[contains(@class,'o_icon_new_folder')]]");
		OOGraphene.waitElement(newFolderBy, browser);
		browser.findElement(newFolderBy).click();
		OOGraphene.waitModalDialog(browser);

		By folderNameBy = By.cssSelector(".o_sel_folder_new_folder_name input[type='text']");
		OOGraphene.waitElement(folderNameBy, browser);
		browser.findElement(folderNameBy).sendKeys(name);
		
		By createBy = By.cssSelector(".modal-dialog button.btn-primary");
		browser.findElement(createBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	public FolderPage assertOnDirectoryV2(String name) {
		By directoryBy = By.xpath("//div[contains(@class,'o_folder_table')]//div[contains(@class,'o_folder_card')]//h5/a/span[contains(text(),'" + name + "')]");
		OOGraphene.waitElement(directoryBy, browser);
		return this;
	}
	
	public FolderPage createHTMLFileV2(String name, String content) {
		By newFileBy = By.cssSelector(".o_folder_create_group>div>button.btn");
		OOGraphene.waitElement(newFileBy, browser);
		browser.findElement(newFileBy).click();
		
		By calloutBy = By.cssSelector(".o_folder_create_group ul.dropdown-menu");
		OOGraphene.waitElement(calloutBy, browser);
		
		By onlineButtonBy = By.xpath("//div[contains(@class,'o_folder_create_group')]//ul[contains(@class,'dropdown-menu')]/li/a[contains(@class,'o_sel_folder_new_document')]");
		OOGraphene.waitElement(onlineButtonBy, browser);
		browser.findElement(onlineButtonBy).click();
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
			OOGraphene.click(saveButtonBy, browser);
			OOGraphene.waitModalDialogDisappears(browser);
		} catch (Error | Exception e) {
			OOGraphene.takeScreenshot("uploadFile", browser);
			throw e;
		}
		return this;
	}
	
	
	public FolderPage uploadFileV2(File file) {
		try {
			By newFileBy = By.cssSelector(".o_folder_create_group>div>button.btn");
			OOGraphene.waitElement(newFileBy, browser);
			browser.findElement(newFileBy).click();
			
			By calloutBy = By.cssSelector(".o_folder_create_group ul.dropdown-menu");
			OOGraphene.waitElement(calloutBy, browser);
			
			By onlineButtonBy = By.xpath("//div[contains(@class,'o_folder_create_group')]//ul[contains(@class,'dropdown-menu')]/li/a[i[contains(@class,'o_icon_file_browser')]]");
			OOGraphene.waitElement(onlineButtonBy, browser);
			browser.findElement(onlineButtonBy).click();
			OOGraphene.waitModalDialog(browser);
			
			By inputBy = By.cssSelector("div.modal-dialog div.o_fileinput input[type='file']");
			OOGraphene.uploadFile(inputBy, file, browser);
			By uploadedBy = By.xpath("//div[contains(@class,'modal-dialog')]//div[contains(@class,'o_filemeta')][div[text()[contains(.,'" + file.getName() + "')]]]");
			OOGraphene.waitElementSlowly(uploadedBy, 5, browser);
			OOGraphene.waitingALittleBit();
			
			By saveButtonBy = By.cssSelector("div.modal-dialog button.btn-primary");
			OOGraphene.click(saveButtonBy, browser);
			OOGraphene.waitModalDialogDisappears(browser);
		} catch (Error | Exception e) {
			OOGraphene.takeScreenshot("uploadFile", browser);
			throw e;
		}
		return this;
	}
	
	public FolderPage addFileCard(File file) {
		By addFileBy = By.cssSelector(".o_folder_cmds .o_folder_create_group div.o_fileinput");
		OOGraphene.waitElement(addFileBy, browser);
		By inputFileBy = By.cssSelector(".o_folder_cmds .o_folder_create_group div.o_fileinput input[type='file']");
		OOGraphene.uploadFile(inputFileBy, file, browser);
		assertOnFileCard(file.getName());
		return this;
	}
	
	public FolderPage unzipFileV2(String filename) {
		By unzipBy = By.xpath("//div[@class='o_folder_table']//div[contains(@class,'o_folder_card_meta')]/div[div/h5/div/span[text()[contains(.,'" + filename + "')]]]/a[i[contains(@class,'o_icon_actions')]]");
		OOGraphene.waitElement(unzipBy, browser);
		browser.findElement(unzipBy).click();
		
		OOGraphene.waitCallout(browser, "ul.o_dropdown");

		By unzippedFolderBy = By.xpath("//dialog//div[contains(@class,'popover-content')]//ul[contains(@class,'o_dropdown')]/li/a[contains(@onclick,'unzip')][i[contains(@class,'o_filetype_zip')]]");
		OOGraphene.waitElement(unzippedFolderBy, browser);
		browser.findElement(unzippedFolderBy).click();
		
		String directoryName = filename.replace(".zip", "");
		return assertOnDirectoryV2(directoryName);
	}
	
	public FolderPage selectFile(String filename) {
		By selectBy = By.xpath("//div[@class='o_briefcase_folder']//tr[td/a[text()[contains(.,'" + filename + "')]]]/td/input[@type='checkbox']");
		WebElement selectEl = browser.findElement(selectBy);
		OOGraphene.check(selectEl, Boolean.TRUE);
		return this;
	}
	
	public FolderPage selectRootDirectory() {
		By rootBy = By.xpath("//div[@class='o_folder_breadcrumb']//ol/li[@class='o_breadcrumb_root']/a");
		OOGraphene.waitElement(rootBy, browser);
		browser.findElement(rootBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public FolderPage assertOnFile(String filename) {
		By fileBy = By.xpath("//table[contains(@class,'o_bc_table')]//a[contains(text(),'" + filename + "')]");
		OOGraphene.waitElement(fileBy, browser);
		return this;
	}
	
	public FolderPage assertOnFileCard(String filename) {
		By fileBy = By.xpath("//div[contains(@class,'o_folder_table')]//h5/a/span[contains(text(),'" + filename + "')]");
		OOGraphene.waitElement(fileBy, browser);
		return this;
	}
	
	public FolderPage assertOnEmptyFolder() {
		By emptyBy = By.cssSelector(".o_table_wrapper .o_empty_state .o_empty_msg");
		OOGraphene.waitElement(emptyBy, browser);
		return this;
	}

	public FolderPage assertOnEmptyFolderCard() {
		By emptyBy = By.cssSelector(".o_folder .o_folder_table .o_empty_state .o_empty_msg");
		OOGraphene.waitElement(emptyBy, browser);
		return this;
	}
	
	
}