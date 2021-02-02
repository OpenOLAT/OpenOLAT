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

import java.util.List;

import org.junit.Assert;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Fragment which contains the menu tree. The WebElement to create
 * this fragment must be a parent of the div.o_tree
 * 
 * Initial date: 20.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MenuTreePageFragment {
	
	public static final By treeBy = By.className("o_tree");
	
	private final  WebDriver browser;

	public MenuTreePageFragment(WebDriver browser) {
		this.browser = browser;
	}
	
	/**
	 * Click the root link in the tree.
	 * 
	 * @return The menu page fragment
	 */
	public MenuTreePageFragment selectRoot() {
		By rootNodeBy = By.xpath("//div[contains(@class,'o_tree')]//span[contains(@class,'o_tree_link')][contains(@class,'o_tree_l0')]/a");
		OOGraphene.waitElement(rootNodeBy, browser);
		browser.findElement(rootNodeBy).click();
		OOGraphene.waitBusy(browser);
		By rootNodeActiveBy = By.xpath("//div[contains(@class,'o_tree')]//span[contains(@class,'o_tree_link')][contains(@class,'o_tree_l0')][contains(@class,'active')]/a");
		OOGraphene.waitElement(rootNodeActiveBy, browser);
		return this;
	}
	
	public MenuTreePageFragment selectWithTitle(String title) {
		By linkBy = By.xpath("//div[contains(@class,'o_tree')]//li/div/span[contains(@class,'o_tree_link')]/a[span[contains(text(),'" + title + "')]]");
		OOGraphene.waitElement(linkBy, browser);
		browser.findElement(linkBy).click();
		OOGraphene.waitBusy(browser);
		By activeLinkBy = By.xpath("//div[contains(@class,'o_tree')]//li[contains(@class,'active')]/div/span[contains(@class,'o_tree_link')][contains(@class,'active')]/a[span[contains(text(),'" + title + "')]]");
		OOGraphene.waitElement(activeLinkBy, browser);
		return this;
	}

	public MenuTreePageFragment assertWithTitle(String title) {
		By linkBy = By.xpath("//div[contains(@class,'o_tree')]//li/div/span[contains(@class,'o_tree_link')]/a[span[contains(text(),'" + title + "')]]");
		OOGraphene.waitElement(linkBy, browser);
		return this;
	}
	
	public MenuTreePageFragment assertWithTitleSelected(String title) {
		By linkBy = By.xpath("//div[contains(@class,'o_tree')]//li[contains(@class,'active')]/div/span[contains(@class,'o_tree_link')]/a[span[contains(text(),'" + title + "')]]");
		OOGraphene.waitElement(linkBy, browser);
		return this;
	}

	public MenuTreePageFragment assertTitleNotExists(String title) {
		boolean found = false;
		WebElement tree = browser.findElement(treeBy);
		List<WebElement> nodeLinks = tree.findElements(By.cssSelector("li>div>span.o_tree_link>a"));
		for(WebElement nodeLink:nodeLinks) {
			String text = nodeLink.getText();
			if(text.contains(title)) {
				OOGraphene.waitBusy(browser);
				found = true;
			}
		}
		
		Assert.assertFalse("Link found with title: " + title, found);
		return this;
	}
}
