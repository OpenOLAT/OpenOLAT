package org.olat.selenium.page.library;

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

}
