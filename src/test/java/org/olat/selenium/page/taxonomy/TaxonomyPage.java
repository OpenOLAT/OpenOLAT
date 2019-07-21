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

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 11 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyPage {
	
	private final WebDriver browser;
	
	public TaxonomyPage(WebDriver browser) {
		this.browser = browser;
	}
	
	/**
	 * Assert on the edit page of the taxonomy.
	 * 
	 * @return Itsefl
	 */
	public TaxonomyPage assertOnMetadata() {
		By selectLevelsBy = By.cssSelector(".o_sel_taxonomy_form");
		OOGraphene.waitElement(selectLevelsBy, browser);
		OOGraphene.waitTinymce(browser);
		return this;
	}
	
	/**
	 * Select the tab to manage the taxonomy levels.
	 * 
	 * @return The taxonomy tree page
	 */
	public TaxonomyTreePage selectTaxonomyTree() {
		By selectLevelsBy = By.cssSelector("a.o_sel_taxonomy_levels");
		OOGraphene.waitElement(selectLevelsBy, browser);
		browser.findElement(selectLevelsBy).click();
		return new TaxonomyTreePage(browser).assertOnTaxonomyTree();
	}

}
