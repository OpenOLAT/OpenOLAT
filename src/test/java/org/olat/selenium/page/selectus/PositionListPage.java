/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.selenium.page.selectus;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 23.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionListPage {

	private WebDriver browser;
	
	public PositionListPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public EditPositionPage addPosition() {
		By addBy = By.cssSelector("a.o_sel_add_position");
		browser.findElement(addBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitElement(By.cssSelector("fieldset.o_sel_edit_position_form"), browser);
		return new EditPositionPage(browser);
	}
	
	public PositionListPage assertPositionInList(String title) {
		By titleLinksBy = By.xpath("//div[contains(@class,'o_sel_position_list')]//table//a[contains(@onclick,'select')][contains(text(),'" + title + "')]");
		OOGraphene.waitElement(titleLinksBy, browser);
		return this;
	}
	
	public PositionPage selectPositionInList(String title) {
		By titleLinksBy = By.xpath("//div[contains(@class,'o_sel_position_list')]//table//a[contains(@onclick,'select')][contains(text(),'" + title + "')]");
		OOGraphene.waitElement(titleLinksBy, browser);
		browser.findElement(titleLinksBy).click();
		OOGraphene.waitBusy(browser);
		return new PositionPage(browser);
	}
	
	/**
	 * @return The number of deletable positions.
	 */
	public int numOfPositions() {
		By tableBy = By.xpath("//div[contains(@class,'o_sel_position_list')]//table");
		OOGraphene.waitElement(tableBy, browser);
		By deleteLinkBy = By.xpath("//div[contains(@class,'o_sel_position_list')]//table//a[contains(@onclick,'delete')]");
		return browser.findElements(deleteLinkBy).size();
	}
	
	/**
	 * Click to delete the position specified by the index.
	 * 
	 * @param index The index of the position
	 * @return Itself
	 */
	public PositionListPage deletePosition(int index) {
		By deleteLinkBy = By.xpath("//div[contains(@class,'o_sel_position_list')]//table//a[contains(@onclick,'delete')][" + index + "]");
		OOGraphene.waitElement(deleteLinkBy, browser).click();
		OOGraphene.waitModalDialog(browser);
		return this;
	}
	
	public PositionListPage assertOnBackup() {
		By backupLinkBy = By.xpath("//div[contains(@class,'modal-content')]//a[contains(@class,'o_sel_export_backup')]");
		OOGraphene.waitElement(backupLinkBy, browser);
		return this;
	}
	
	public PositionListPage confirmDeletePosition() {
		By deleteButtonBy = By.xpath("//div[contains(@class,'modal-content')]//div[contains(@class,'o_button_group')]/a[contains(@class,'o_sel_delete_position')]");
		OOGraphene.waitElement(deleteButtonBy, browser).click();
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	public PositionListPage confirmDeletePositionDefinitively() {
		By deleteButtonBy = By.xpath("//div[contains(@class,'modal-content')]//div[contains(@class,'o_button_group')]/a[contains(@class,'o_sel_delete_position')]");
		OOGraphene.waitElement(deleteButtonBy, browser).click();
		OOGraphene.waitModalDialogWithFieldsetDisappears(browser, "o_sel_confirm_delete_position_anonymous");
		
		OOGraphene.waitModalDialog(browser, "fieldset.o_sel_confirm_delete_position_permanently");
		
		By confirmBy = By.cssSelector("fieldset.o_sel_confirm_delete_position_permanently .o_sel_confirm input[type='checkbox'][name='confirm.delete']");
		WebElement confirmEl = browser.findElement(confirmBy);
		OOGraphene.check(confirmEl, Boolean.TRUE);
		By deletePermanentlyButtonBy = By.xpath("//div[contains(@class,'modal-content')]//div[contains(@class,'o_button_group')]/a[contains(@class,'o_sel_delete_permanently')]");
		browser.findElement(deletePermanentlyButtonBy).click();
		OOGraphene.waitModalDialogWithFieldsetDisappears(browser, "o_sel_confirm_delete_position_permanently");
		
		return this;
	}
}
