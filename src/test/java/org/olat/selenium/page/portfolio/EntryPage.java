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
package org.olat.selenium.page.portfolio;

import org.olat.selenium.page.core.ContentEditorPage;
import org.olat.selenium.page.core.ContentViewPage;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 3 oct. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EntryPage {
	
	private final WebDriver browser;

	public EntryPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public EntryPage assertOnPage(String title) {
		By metaTitleBy = By.xpath("//div[contains(@class,'o_page_lead')]//h2[contains(text(),'" + title + "')]");
		OOGraphene.waitElement(metaTitleBy, browser);
		return this;
	}
	
	/**
	 * 
	 * @return The content editor
	 */
	public ContentEditorPage contentEditor() {
		return new ContentEditorPage(browser, false);
	}
	
	/**
	 * Publish the entry in the page view.
	 * 
	 * @return Itself
	 */
	public EntryPage publishEntry() {
		By publishBy = By.cssSelector("a.o_sel_pf_publish_entry");
		OOGraphene.waitElement(publishBy, browser);
		browser.findElement(publishBy).click();
		confirm();
		By publishedBy = By.cssSelector("div.o_portfolio_status i.o_icon_pf_entry_published");
		OOGraphene.waitElement(publishedBy, browser);
		OOGraphene.scrollTop(browser);
		return this;
	}
	
	public BinderPage moveEntryToTrash() {
		By moveToTrashBy = By.cssSelector("a.o_sel_pf_move_page_to_trash");
		OOGraphene.waitElement(moveToTrashBy, 5, browser);
		browser.findElement(moveToTrashBy).click();
		OOGraphene.waitModalDialog(browser);
		
		BinderPage binder = new BinderPage(browser);
		binder.confirm();
		return binder;
	}
	
	public EntriesPage deleteEntry() {
		By moveToTrashBy = By.cssSelector("a.o_sel_pf_delete_page");
		OOGraphene.waitElement(moveToTrashBy, 5, browser);
		browser.findElement(moveToTrashBy).click();
		OOGraphene.waitModalDialog(browser);
		
		new BinderPage(browser).confirm();
		return new EntriesPage(browser);
	}
	
	public ContentEditorPage openEditor() {
		By closeBy = By.cssSelector("a.o_sel_pf_edit_page");
		OOGraphene.waitElement(closeBy, browser);
		browser.findElement(closeBy).click();
		OOGraphene.waitBusy(browser);
		return contentEditor();
	}
	
	public ContentViewPage closeEditor() {
		By closeBy = By.cssSelector("a.o_sel_pf_edit_page");
		OOGraphene.waitElement(closeBy, browser);
		browser.findElement(closeBy).click();
		OOGraphene.waitBusy(browser);
		return new ContentViewPage(browser);
	}
	
	/**
	 * Yes in a dialog box controller.
	 */
	private void confirm() {
		OOGraphene.waitModalDialog(browser);
		By confirmButtonBy = By.xpath("//div[contains(@class,'modal-dialo')]//div[contains(@class,'modal-footer')]/a[contains(@onclick,'link_0')]");
		OOGraphene.waitElement(confirmButtonBy, browser);
		browser.findElement(confirmButtonBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
	}
}
