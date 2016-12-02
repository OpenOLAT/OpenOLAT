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
package org.olat.selenium.page.qti;

import org.olat.selenium.page.core.MenuTreePageFragment;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 16 nov. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21EditorPage {
	private final By elementsMenu = By.cssSelector("ul.o_sel_qti_elements");
	private final By changeMenu = By.cssSelector("ul.o_sel_qti_change_node");
	
	private final WebDriver browser;
	private final MenuTreePageFragment menuTree;
	
	public QTI21EditorPage(WebDriver browser) {
		this.browser = browser;
		menuTree = new MenuTreePageFragment(browser);
	}
	
	public QTI21EditorPage assertOnEditor() {
		By editorBy = By.className("o_assessment_test_editor_and_composer");
		OOGraphene.waitElement(editorBy, 5, browser);
		return this;
	}
	
	public QTI21CSVImportWizard importTable() {
		openElementsMenu();
		
		By importBy = By.xpath("//ul[contains(@class,'o_sel_qti_elements')]//a[contains(@onclick,'import.table')]");
		browser.findElement(importBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitModalDialog(browser);
		return new QTI21CSVImportWizard(browser);
	}
	
	public QTI21EditorPage openElementsMenu() {
		By toolsMenuCaret = By.cssSelector("a.o_sel_qti_elements");
		browser.findElement(toolsMenuCaret).click();
		OOGraphene.waitElement(elementsMenu, browser);
		return this;
	}
	
	public QTI21EditorPage selectNode(String title) {
		menuTree.selectWithTitle(title);
		return this;
	}
	
	public QTI21EditorPage deleteNode() {
		openChangeMenu();
		
		By importBy = By.xpath("//ul[contains(@class,'o_sel_qti_change_node')]//a[contains(@onclick,'tools.delete')]");
		browser.findElement(importBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitModalDialog(browser);
		
		confirm();
		return this;
	}
	
	public QTI21EditorPage openChangeMenu() {
		By changeMenuCaret = By.cssSelector("a.o_sel_qti_change_node");
		browser.findElement(changeMenuCaret).click();
		OOGraphene.waitElement(changeMenu, browser);
		return this;
	}
	
	public QTI21EditorPage confirm() {
		By yesBy = By.xpath("//div[contains(@class,'modal-dialog')]//a[contains(@href,'link_0')]");
		browser.findElement(yesBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
}
