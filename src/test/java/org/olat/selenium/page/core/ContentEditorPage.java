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
import org.openqa.selenium.support.ui.Select;

/**
 * 
 * Initial date: 30 août 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ContentEditorPage extends ContentViewPage {

	private final boolean form;
	
	public ContentEditorPage(WebDriver browser, boolean form) {
		super(browser);
		this.form = form;
	}
	
	public ContentEditorPage addLayout(ContainerLayout layout) {
		By addBy = By.cssSelector(".o_ce_add_main_btns>a.btn.o_sel_add_container_main");
		OOGraphene.waitElement(addBy, browser);
		browser.findElement(addBy).click();
		By addCalloutBy = By.cssSelector("dialog.popover div.o_inspector_layouts");
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
		By addCalloutBy = By.cssSelector("dialog.popover div.o_sel_add_element_callout");
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
	public ContentEditorPage setTitleSize(int size, boolean waitFocus) {
		By titleSize = By.xpath("//div[@class='o_ceditor_inspector']//select[@id='o_fioheading_size_SELBOX']");
		OOGraphene.waitElement(titleSize, browser);

		new Select(browser.findElement(titleSize)).selectByValue(Integer.toString(size));
		OOGraphene.waitBusy(browser);
		By selectedSize = By.cssSelector("div.o_ceditor_inspector select#o_fioheading_size_SELBOX" + (waitFocus ? ":focus" : "") + ">option[value='" + size +"'][selected='selected']");
		OOGraphene.waitElement(selectedSize, browser);
		
		return this;
	}
	
	public ContentEditorPage addImage(String title, File image) {
		By addImageBy = By.cssSelector("a#o_coadd_el_image");
		browser.findElement(addImageBy).click();
		OOGraphene.waitModalDialog(browser, "div.o_sel_ce_add_image");
		
		By uploadButtonBy = By.cssSelector("dialog.modal div.o_sel_ce_add_image a.o_sel_upload_image");
		browser.findElement(uploadButtonBy).click();
		OOGraphene.waitModalDialog(browser, "fieldset.o_sel_ce_collect_image_form");
		
		By inputBy = By.cssSelector("fieldset.o_sel_ce_collect_image_form .o_fileinput input[type='file']");
		OOGraphene.uploadFile(inputBy, image, browser);
		By previewBy = By.cssSelector("div.o_filepreview>div.o_image>img");
		OOGraphene.waitElement(previewBy, browser);
		
		By titleBy = By.cssSelector("fieldset.o_sel_ce_collect_image_form .o_sel_pf_collect_title input[type='text']");
		browser.findElement(titleBy).sendKeys(title);
		
		//ok save
		By saveBy = By.cssSelector("fieldset.o_sel_ce_collect_image_form button.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitModalDialogWithFieldsetDisappears(browser, "o_sel_ce_collect_image_form");
		OOGraphene.waitModalDialogWithDivDisappears(browser, "o_sel_ce_collect_image_form");
		return this;
	}
	
	public ContentEditorPage addDocument(String title, File document) {
		By addDocumentBy = By.cssSelector("a#o_coadd_el_bc");
		browser.findElement(addDocumentBy).click();
		OOGraphene.waitModalDialog(browser, "div.o_sel_ce_add_file");
		
		By uploadButtonBy = By.cssSelector("dialog.modal div.o_sel_ce_add_file a.o_sel_upload_file");
		browser.findElement(uploadButtonBy).click();
		OOGraphene.waitModalDialog(browser, "fieldset.o_sel_ce_collect_document_form");

		By inputBy = By.cssSelector("fieldset.o_sel_ce_collect_document_form .o_fileinput input[type='file']");
		OOGraphene.uploadFile(inputBy, document, browser);
		OOGraphene.waitBusy(browser);
		By uploadedBy = By.cssSelector("fieldset.o_sel_ce_collect_document_form .o_sel_file_uploaded");
		OOGraphene.waitElement(uploadedBy, browser);
		
		By titleBy = By.cssSelector("fieldset.o_sel_ce_collect_document_form .o_sel_pf_collect_title input[type='text']");
		browser.findElement(titleBy).sendKeys(title);
		
		//ok save
		By saveBy = By.cssSelector("fieldset.o_sel_ce_collect_document_form button.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitModalDialogWithFieldsetDisappears(browser, "o_sel_ce_collect_document_form");
		OOGraphene.waitModalDialogWithDivDisappears(browser, "o_sel_ce_add_file");
		return this;
	}
	
	public ContentEditorPage addCitation(String title, String citation) {
		By addCitationBy = By.cssSelector("a#o_coadd_el_citation");
		browser.findElement(addCitationBy).click();
		OOGraphene.waitModalDialog(browser, "div.o_sel_ce_add_citation");
		
		By addButtonBy = By.cssSelector("dialog.modal div.o_sel_ce_add_citation a.o_sel_add_citation");
		browser.findElement(addButtonBy).click();
		OOGraphene.waitModalDialog(browser, "fieldset.o_sel_ce_collect_citation_form");
		
		By titleBy = By.cssSelector("fieldset.o_sel_ce_collect_citation_form .o_sel_pf_collect_title input[type='text']");
		browser.findElement(titleBy).sendKeys(title);
		
		String citationSelector = ".o_sel_ce_collect_citation_form .o_sel_pf_collect_citation";
		OOGraphene.tinymce(citation, citationSelector, browser);
		
		//ok save
		By saveBy = By.cssSelector("fieldset.o_sel_ce_collect_citation_form button.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitModalDialogWithFieldsetDisappears(browser, "o_sel_ce_collect_citation_form");
		OOGraphene.waitModalDialogWithDivDisappears(browser, "o_sel_ce_add_citation");
		return this;
	}
	
	/**
	 * Close the fragment editor in the page editor by clicking the header surface.
	 * 
	 * @return Itself
	 */
	public ContentEditorPage closeEditFragmentOfPage() {
		By toolbarContainerBy = By.xpath("//div[contains(@class,'o_ce_wrapper')]//div[contains(@class,'o_page_lead')]");
		return closeEditFragment(toolbarContainerBy, toolbarContainerBy);
	}
	
	/**
	 * Close the fragment editor in the editor for survey.
	 * 
	 * @return Itself
	 */
	public ContentEditorPage closeEditFragmentOfResource() {
		By toolbarContainerBy = By.xpath("//div[@id='o_main_container']//div[@class='o_tools_container']/div[@class='container-fluid']");
		By toolbarBreadcrumbBy = By.xpath("//div[@id='o_main_container']//div[contains(@class,'o_breadcrumb')]/ol[@class='breadcrumb']");
		OOGraphene.waitElement(toolbarContainerBy, browser);
		OOGraphene.waitElement(toolbarBreadcrumbBy, browser);
		return closeEditFragment(toolbarContainerBy, toolbarBreadcrumbBy);
	}
	
	private ContentEditorPage closeEditFragment(By containerBy, By alternativeContainerBy) {
		browser.findElement(containerBy).click();
		OOGraphene.waitBusy(browser);
		try {
			OOGraphene.waitElementDisappears(By.className("o_fragment_edited"), 5, browser);
		} catch (Exception e) {
			OOGraphene.takeScreenshot("Close edit fragment", browser);
			
			// Try again
			OOGraphene.waitingALittleLonger();
			browser.findElement(alternativeContainerBy).click();
			OOGraphene.waitElementDisappears(By.className("o_fragment_edited"), 5, browser);
		}
		return this;
	}
	
	public ContentEditorPage scrollTop() {
		OOGraphene.scrollTop(browser);
		return this;
	}
}
