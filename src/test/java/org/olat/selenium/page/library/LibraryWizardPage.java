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

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 26 nov. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LibraryWizardPage {
	
	private WebDriver browser;
	
	public LibraryWizardPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public static LibraryWizardPage getPage(WebDriver browser) {
		return new LibraryWizardPage(browser);
	}
	
	public LibraryWizardPage assertOnMetadata() {
		By filenameBy = By.cssSelector("div.o_wizard .o_sel_new_filename");
		OOGraphene.waitElement(filenameBy, browser);
		return this;
	}
	
	/**
	 * Click next on the step which selects the folder.
	 * 
	 * @return Itself
	 */
	public LibraryWizardPage nextFolders(String folder) {
		OOGraphene.nextStep(browser);
		OOGraphene.waitElement(By.cssSelector("div.o_wizard div.o_tree.o_tree_root_visible"), browser);
		
		By folderBy = By.xpath("//div[contains(@class,'o_wizard')]//span[contains(@class,'o_tree_link')][a/span[text()[contains(.,'" + folder + "')]]]/input[@type='checkbox']");
		WebElement folderEl = browser.findElement(folderBy);
		OOGraphene.check(folderEl, Boolean.TRUE);
		return this;
	}
	
	public LibraryWizardPage nextNotifications() {
		OOGraphene.nextStep(browser);
		OOGraphene.waitElement(By.cssSelector("div.o_wizard div.o_sel_notification_body textarea"), browser);
		return this;
	}
	
	public LibraryPage finish() {
		OOGraphene.finishStep(browser);
		return new LibraryPage(browser);
	}
	

}
