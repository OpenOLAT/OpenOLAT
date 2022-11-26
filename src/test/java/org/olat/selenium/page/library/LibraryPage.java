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
package org.olat.selenium.page.library;

import java.io.File;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 25 nov. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LibraryPage {
	
	private WebDriver browser;
	
	public LibraryPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public static LibraryPage getPage(WebDriver browser) {
		return new LibraryPage(browser);
	}
	
	/**
	 * Assert that the overview is visible.
	 * 
	 * @return Itself
	 */
	public LibraryPage assertOnOverview() {
		By overviewBy = By.cssSelector("div.o_library_overview");
		OOGraphene.waitElement(overviewBy, browser);
		return this;
	}
	
	public LibraryPage assertOnMenuFolder(String folder) {
		By menuLinkBy = By.xpath("//ul[contains(@class,'o_tree_l1')]//span/a/span[text()[contains(.,'" + folder + "')]]");
		OOGraphene.waitElement(menuLinkBy, browser);
		return this;
	}
	
	public LibraryPage assertOnNewDocument(String filename) {
		By newDocumentBy = By.xpath("//div[contains(@class,'o_library_newest_files')]//a/span[text()[contains(.,'" + filename + "')]]");
		OOGraphene.waitElement(newDocumentBy, browser);
		return this;
	}
	
	public LibraryPage selectFolder(String folder) {
		By menuLinkBy = By.xpath("//ul[contains(@class,'o_tree_l1')]//span/a[span[text()[contains(.,'" + folder + "')]]]");
		OOGraphene.waitElement(menuLinkBy, browser);
		browser.findElement(menuLinkBy).click();
		return assertOnSelectedFolder(folder);
	}
	
	public LibraryPage assertOnSelectedFolder(String folder) {
		By titleBy = By.xpath("//div[contains(@class,'o_library_catalog_title')]/h3[i[contains(@class,'o_filetype_folder')]][text()[contains(.,'" + folder + "')]]");
		OOGraphene.waitElement(titleBy, browser);		
		return this;
	}
	
	public LibraryPage assertOnPdfFile(String file) {
		By titleBy = By.xpath("//div[contains(@class,'o_library_items')]//h4/a[i[contains(@class,'o_filetype_pdf')]][text()[contains(.,'" + file + "')]]");
		OOGraphene.waitElement(titleBy, browser);		
		return this;
	}
	
	public LibraryPage addComment(String comment, String file) {
		By commentBy = By.xpath("//div[contains(@class,'o_library_item')][h4/a[text()[contains(.,'" + file + "')]]]/div[contains(@class,'o_library_extra')]/a[contains(@class,'o_comments')]");
		OOGraphene.waitElement(commentBy, browser);
		browser.findElement(commentBy).click();
		
		OOGraphene.waitModalDialog(browser);
		OOGraphene.waitTinymce(browser);
		
		// Write the comment
		By commentFieldBy = By.cssSelector(".o_comments .o_richtext_mce");
		OOGraphene.waitElement(commentFieldBy, browser);
		OOGraphene.tinymce(comment, browser);
		
		// Save the comment
		By saveBy = By.cssSelector("div.o_comments button.btn.btn-primary");
		browser.findElement(saveBy).click();
		
		// Assert comment
		By quoteBy = By.xpath("//blockquote[contains(@class,'o_comment')][p[text()[contains(.,'" + comment + "')]]]");
		OOGraphene.waitElement(quoteBy, browser);
		
		// Cancel
		By cancelBy = By.cssSelector("div.o_comments a.btn.btn-default");
		browser.findElement(cancelBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	public LibraryPage assertOnComments(String filename, int numOfComments) {
		By commentBy = By.xpath("//div[contains(@class,'o_library_item')][h4/a[text()[contains(.,'" + filename + "')]]]/div[contains(@class,'o_library_extra')]/a[contains(@class,'o_comments')][span[text()[contains(.,'(" + numOfComments + ")')]]][i[contains(@class,'o_icon_comments')]]");
		OOGraphene.waitElementSlowly(commentBy, 10, browser);
		return this;
	}
	
	public LibraryPage uploadDocument(File file) {
		By uploadBy = By.cssSelector("div.o_library_overview a.o_sel_upload_document");
		OOGraphene.waitElement(uploadBy, browser);
		browser.findElement(uploadBy).click();
		OOGraphene.waitModalDialog(browser);
		
		By inputWrapperBy = By.cssSelector("div.modal-dialog div.o_fileinput");
		OOGraphene.waitElement(inputWrapperBy, browser);
		By inputBy = By.cssSelector("div.modal-dialog div.o_fileinput input[type='file']");
		OOGraphene.uploadFile(inputBy, file, browser);
		By uploadedBy = By.cssSelector("div.modal-dialog div.o_sel_file_uploaded");
		OOGraphene.waitElementSlowly(uploadedBy, 5, browser);
		OOGraphene.waitingALittleBit();
		
		By saveButtonBy = By.cssSelector("div.o_sel_upload_buttons button.btn-primary");
		OOGraphene.moveAndClick(saveButtonBy, browser);
		OOGraphene.waitModalDialogDisappears(browser);
		OOGraphene.waitAndCloseBlueMessageWindow(browser);
		return this;
	}
	
	public LibraryPage reviewDocuments() {
		By reviewDocumentsBy = By.xpath("//div[contains(@class,'o_library_overview')]//a[i[contains(@class,'o_icon_review')]]");
		OOGraphene.waitElement(reviewDocumentsBy, browser);
		browser.findElement(reviewDocumentsBy).click();
		
		By reviewTableBy = By.cssSelector("div.o_review_documents table.o_table");
		OOGraphene.waitElement(reviewTableBy, browser);
		return this;
	}
	
	public LibraryPage assertOnDocumentToReview(String filename) {
		By acceptBy = By.xpath("//div[@class='o_review_documents']//table//tr/td/a[text()[contains(.,'" + filename + "')]]");
		OOGraphene.waitElement(acceptBy, browser);
		return this;
	}
	
	public LibraryWizardPage acceptDocument(String filename) {
		By acceptBy = By.xpath("//div[@class='o_review_documents']//table//tr[td/a[text()[contains(.,'" + filename + "')]]]/td/a[contains(@onclick,'accept')]");
		OOGraphene.waitElement(acceptBy, browser);
		browser.findElement(acceptBy).click();
		OOGraphene.waitModalDialog(browser);
		return new LibraryWizardPage(browser);
	}
	
	public LibraryPage back() {
		By backBy = By.cssSelector("ol.breadcrumb a.o_link_back");
		OOGraphene.waitElement(backBy, browser);
		browser.findElement(backBy).click();
		return assertOnOverview();
	}

}
