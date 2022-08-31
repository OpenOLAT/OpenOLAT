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

import org.olat.modules.ceditor.model.ContainerLayout;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

/**
 * 
 * Initial date: 30 août 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ContentEditorPage extends ContentViewPage {

	public final By editFragmentBy = By.cssSelector("div.o_page_fragment_edit");

	private final boolean form;
	
	public ContentEditorPage(WebDriver browser, boolean form) {
		super(browser);
		this.form = form;
	}
	
	public ContentEditorPage addLayout(ContainerLayout layout) {
		By addBy = By.cssSelector(".o_ce_add_main_btns>a.btn.o_sel_add_container_main");
		OOGraphene.waitElement(addBy, browser);
		browser.findElement(addBy).click();
		By addCalloutBy = By.cssSelector("div.popover div.o_inspector_layouts");
		OOGraphene.waitElement(addCalloutBy, browser);
		
		By newLayoutBy = By.xpath("//div[contains(@class,'popover')]//div[@class='o_inspector_layouts']//a/span/div[contains(@class,'" + layout.cssClass() + "')]");
		browser.findElement(newLayoutBy).click();
		OOGraphene.waitElementDisappears(newLayoutBy, 5, browser);
		
		By layoutBy = By.cssSelector(".o_container_part." + layout.cssClass());
		OOGraphene.waitElement(layoutBy, browser);
		return this;
	}
	
	public ContentEditorPage openElementsChooser(int container, int slot) {
		By addBy = By.xpath("//div[contains(@class,'o_page_container_edit')][" + container + "]//div[contains(@class,'o_page_container_slot')][" + slot + "]//a[contains(@class,'btn')][contains(@class,'o_page_add_in_container')]");
		OOGraphene.waitElement(addBy, browser);
		browser.findElement(addBy).click();
		By addCalloutBy = By.cssSelector("div.popover div.o_sel_add_element_callout");
		OOGraphene.waitElement(addCalloutBy, browser);
		return this;
	}
	
	public ContentEditorPage addTitle(String title) {
		By addTitleBy = By.cssSelector("a#o_coadd_el_" + (form ? "form" : "") + "htitle");
		browser.findElement(addTitleBy).click();
		OOGraphene.waitElement(editFragmentBy, browser);
		By titleBy = By.cssSelector(".o_page_fragment_edit.o_fragment_edited .o_page_edit_title");
		OOGraphene.waitElement(titleBy, browser);
		OOGraphene.tinymceExec("<h3>" + title + "</h3>", browser);
		return this;
	}
	
	/**
	 * Change the size of the title.
	 * 
	 * @param size A value between 1 and 6
	 * @return
	 */
	public ContentEditorPage setTitleSize(int size) {
		By titleSize = By.xpath("//div[@class='o_ceditor_inspector']//select[@id='o_fioheading_size_SELBOX']");
		OOGraphene.waitElement(titleSize, browser);

		// Move the focus, important
		new Actions(browser)
			.moveToElement(browser.findElement(titleSize))
			.click().perform();
		
		new Select(browser.findElement(titleSize)).selectByValue(Integer.toString(size));
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public ContentEditorPage addImage(String title, File image) {
		By addImageBy = By.cssSelector("a#o_coadd_el_image");
		browser.findElement(addImageBy).click();
		OOGraphene.waitModalDialog(browser);
		
		By inputBy = By.cssSelector("fieldset.o_sel_pf_collect_image_form .o_fileinput input[type='file']");
		OOGraphene.uploadFile(inputBy, image, browser);
		By previewBy = By.cssSelector("div.o_filepreview>div.o_image>img");
		OOGraphene.waitElement(previewBy, 5, browser);
		
		By titleBy = By.cssSelector("fieldset.o_sel_pf_collect_image_form .o_sel_pf_collect_title input[type='text']");
		browser.findElement(titleBy).sendKeys(title);
		
		//ok save
		By saveBy = By.cssSelector("fieldset.o_sel_pf_collect_image_form button.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public ContentEditorPage addDocument(String title, File document) {
		By addDocumentBy = By.cssSelector("a#o_coadd_el_bc");
		browser.findElement(addDocumentBy).click();
		OOGraphene.waitModalDialog(browser);
		
		By inputBy = By.cssSelector("fieldset.o_sel_pf_collect_document_form .o_fileinput input[type='file']");
		OOGraphene.uploadFile(inputBy, document, browser);
		OOGraphene.waitBusy(browser);
		By uploadedBy = By.cssSelector("fieldset.o_sel_pf_collect_document_form .o_sel_file_uploaded");
		OOGraphene.waitElement(uploadedBy, browser);
		
		By titleBy = By.cssSelector("fieldset.o_sel_pf_collect_document_form .o_sel_pf_collect_title input[type='text']");
		browser.findElement(titleBy).sendKeys(title);
		
		//ok save
		By saveBy = By.cssSelector("fieldset.o_sel_pf_collect_document_form button.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	public ContentEditorPage addCitation(String title, String citation) {
		By addCitationBy = By.cssSelector("a#o_coadd_el_citation");
		browser.findElement(addCitationBy).click();
		OOGraphene.waitModalDialog(browser);
		
		By titleBy = By.cssSelector("fieldset.o_sel_pf_collect_citation_form .o_sel_pf_collect_title input[type='text']");
		browser.findElement(titleBy).sendKeys(title);
		
		String citationSelector = ".o_sel_pf_collect_citation_form .o_sel_pf_collect_citation";
		OOGraphene.tinymce(citation, citationSelector, browser);
		
		//ok save
		By saveBy = By.cssSelector("fieldset.o_sel_pf_collect_citation_form button.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Close the fragment editor.
	 * 
	 * @return Itself
	 */
	public ContentEditorPage closeEditFragment() {
		By toolbarContainerBy = By.xpath("//div[contains(@class,'o_page_content_editor')]/div[1]/div[contains(@class,'o_page_container_tools')]");
		// Move the focus, important
		new Actions(browser)
			.moveToElement(browser.findElement(toolbarContainerBy), 5, 5)
			.click().perform();
		
		OOGraphene.waitBusy(browser);
		try {
			OOGraphene.waitElementDisappears(By.className("o_fragment_edited"), 5, browser);
		} catch (Exception e) {
			OOGraphene.takeScreenshot("Close edit fragment", browser);
			throw e;
		}
		return this;
	}

}
