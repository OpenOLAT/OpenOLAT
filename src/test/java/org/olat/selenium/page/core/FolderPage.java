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
	
	/**
	 * Assert that the table is visible.
	 * s
	 * @return Itself
	 */
	public FolderPage assertOnFolderTable() {
		By folderCmpBy = By.cssSelector(".o_folder .o_folder_table");
		OOGraphene.waitElement(folderCmpBy, browser);
		return this;
	}
	
	/**
	 * Add a new folder.
	 * 
	 * @param name The folder name
	 * @return Itself
	 */
	public FolderPage createDirectory(String name) {
		By newFileBy = By.cssSelector(".o_folder_create_group>div>button.btn");
		OOGraphene.waitElement(newFileBy, browser).click();
		
		By calloutBy = By.cssSelector(".o_folder_create_group ul.dropdown-menu");
		OOGraphene.waitElement(calloutBy, browser);
		
		By newFolderBy = By.xpath("//div[contains(@class,'o_folder_create_group')]//ul[contains(@class,'dropdown-menu')]/li/a[i[contains(@class,'o_icon_new_folder')]]");
		OOGraphene.waitElement(newFolderBy, browser).click();
		OOGraphene.waitModalDialog(browser);

		By folderNameBy = By.cssSelector(".o_sel_folder_new_folder_name input[type='text']");
		OOGraphene.waitElement(folderNameBy, browser).sendKeys(name);
		
		By createBy = By.cssSelector(".modal-dialog button.btn-primary");
		browser.findElement(createBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	public FolderPage assertOnDirectory(String name) {
		By directoryBy = By.xpath("//div[contains(@class,'o_folder_table')]//div[contains(@class,'o_folder_card')]//h5/a/span[contains(text(),'" + name + "')]");
		OOGraphene.waitElement(directoryBy, browser);
		return this;
	}
	
	public FolderPage createHTMLFile(String name, String content) {
		By newFileBy = By.cssSelector(".o_folder_create_group>div>button.btn");
		OOGraphene.waitElement(newFileBy, browser).click();
		
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
		OOGraphene.waitElement(htmlTypeBy, browser).click();
		
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
		OOGraphene.waitElement(saveAndCloseDirtyBy, browser).click();
		
		browser.switchTo().window(startWindow);
		return this;
	}
	
	/**
	 * Upload a file via the upload dialog.
	 * 
	 * @param file The file to upload
	 * @return Itself
	 */
	public FolderPage uploadFile(File file) {
		try {
			By newFileBy = By.cssSelector(".o_folder_create_group>div>button.btn");
			OOGraphene.waitElement(newFileBy, browser).click();
			
			By calloutBy = By.cssSelector(".o_folder_create_group ul.dropdown-menu");
			OOGraphene.waitElement(calloutBy, browser);
			
			By onlineButtonBy = By.xpath("//div[contains(@class,'o_folder_create_group')]//ul[contains(@class,'dropdown-menu')]/li/a[i[contains(@class,'o_icon_filehub_add')]]");
			OOGraphene.waitElement(onlineButtonBy, browser).click();
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
	
	/**
	 * Use the quick upload field in the folder component to upload a file.
	 * 
	 * @param file The file to upload
	 * @return Itselfs
	 */ 
	public FolderPage quickUploadFile(File file) {
		By addFileBy = By.cssSelector(".o_folder_cmds .o_folder_create_group div.o_fileinput");
		OOGraphene.waitElement(addFileBy, browser);
		By inputFileBy = By.cssSelector(".o_folder_cmds .o_folder_create_group div.o_fileinput input[type='file']");
		OOGraphene.uploadFile(inputFileBy, file, browser);
		return assertOnFileCard(file.getName());
	}
	
	public FolderPage unzipFile(String filename) {
		By unzipBy = By.xpath("//div[@class='o_folder_table']//div[contains(@class,'o_folder_card_meta')]/div[div/h5/a/span[text()[contains(.,'" + filename + "')]]]/a[i[contains(@class,'o_icon_actions')]]");
		OOGraphene.waitElement(unzipBy, browser).click();
		
		OOGraphene.waitCallout(browser, "ul.o_dropdown");

		By unzippedFolderBy = By.xpath("//dialog//div[contains(@class,'popover-content')]//ul[contains(@class,'o_dropdown')]/li/a[contains(@onclick,'unzip')][i[contains(@class,'o_filetype_zip')]]");
		OOGraphene.waitElement(unzippedFolderBy, browser).click();
		OOGraphene.waitCalloutDisappears(browser, " li > a > i.o_filetype_zip");
		
		String directoryName = filename.replace(".zip", "");
		return assertOnDirectory(directoryName);
	}
	
	public FolderPage selectRootDirectory() {
		By rootBy = By.xpath("//div[@class='o_folder_breadcrumb']//ol/li[@class='o_breadcrumb_root']/a");
		OOGraphene.waitElement(rootBy, browser).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public FolderPage assertOnFileCard(String filename) {
		By fileBy = By.xpath("//div[contains(@class,'o_folder_table')]//h5/a/span[contains(text(),'" + filename + "')]");
		OOGraphene.waitElement(fileBy, browser);
		return this;
	}

	public FolderPage assertOnEmptyFolderCard() {
		By emptyBy = By.cssSelector(".o_folder .o_folder_table .o_empty_state .o_empty_msg");
		OOGraphene.waitElement(emptyBy, browser);
		return this;
	}
}