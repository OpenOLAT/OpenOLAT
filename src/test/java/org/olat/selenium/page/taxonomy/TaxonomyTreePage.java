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
package org.olat.selenium.page.taxonomy;

import java.util.List;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 11 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyTreePage {
	
	private final WebDriver browser;
	
	public TaxonomyTreePage(WebDriver browser) {
		this.browser = browser;
	}
	
	public TaxonomyTreePage assertOnTaxonomyTree() {
		By levelsTreeBy = By.className("o_sel_taxonomy_levels_tree");
		OOGraphene.waitElement(levelsTreeBy, browser);
		return this;
	}
	
	/**
	 * Check if there is at least one level. If not, create one.
	 * 
	 * @return Itself
	 */
	public TaxonomyTreePage atLeastOneLevel(String identifier, String name) {
		By tableTreeBy = By.cssSelector("div.o_sel_taxonomy_levels_tree div.o_table_flexi_breadcrumb");
		OOGraphene.waitElement(tableTreeBy, browser);
		
		By tableBy = By.xpath("//div[contains(@class,'o_taxonomy_level_listing')]//table//tr/td/a[text()[contains(.,'" + identifier + "')]]");
		List<WebElement> tableEls = browser.findElements(tableBy);
		if(tableEls.isEmpty()) {
			newLevel(identifier, name);
			clickBackInBreadcrumb();
		}
		return this;
	}
	
	public TaxonomyTreePage clickBackInBreadcrumb() {
		By backBy = By.xpath("//ol[@class='breadcrumb']/li[@class='o_breadcrumb_back']/a[i[contains(@class,'o_icon_back')]]");
		browser.findElement(backBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public TaxonomyLevelPage selectTaxonomyLevel(String identifier) {
		By selectBy = By.xpath("//div[contains(@class,'o_sel_taxonomy_levels_tree')]//table//tr/td/a[contains(@onclick,'select')][text()[contains(.,'" + identifier + "')]]");
		OOGraphene.waitElement(selectBy, browser);
		browser.findElement(selectBy).click();
		OOGraphene.waitBusy(browser);
		return new TaxonomyLevelPage(browser).assertOnTaxonomyLevel();
	}
	
	public TaxonomyTreePage newLevel(String identifier, String name) {
		By newLevelBy = By.cssSelector("div.o_sel_taxonomy_levels_tree a.o_sel_taxonomy_new_level");
		browser.findElement(newLevelBy).click();
		OOGraphene.waitModalDialog(browser);
		
		By identifierBy = By.cssSelector("div.o_sel_taxonomy_level_identifier input[type='text']");
		OOGraphene.waitElement(identifierBy, browser);
		OOGraphene.waitTinymce(browser);// to be nice to firefox
		browser.findElement(identifierBy).sendKeys(identifier);
		
		By nameBy = By.cssSelector("div.o_sel_taxonomy_level_name input[type='text']");
		browser.findElement(nameBy).sendKeys(name);
		
		By saveBy = By.cssSelector("div.o_sel_taxonomy_level_form button.btn.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		
		// wait update in the table
		By editTabsBy = By.className("o_sel_taxonomy_level_tabs");
		OOGraphene.waitElement(editTabsBy, browser);
		return this;
	}

}
