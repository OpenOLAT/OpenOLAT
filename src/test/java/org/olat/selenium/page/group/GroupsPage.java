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
package org.olat.selenium.page.group;

import java.util.List;

import org.junit.Assert;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Drive the groups site
 * 
 * Initial date: 03.07.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GroupsPage {
	
	private WebDriver browser;
	
	public GroupsPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public GroupPage createGroup(String name, String description) {
		//click create button
		By createBy = By.className("o_sel_group_create");
		WebElement createButton = browser.findElement(createBy);
		createButton.click();
		OOGraphene.waitBusy(browser);
		
		//fill the form
		By nameBy = By.cssSelector(".o_sel_group_edit_title input[type='text']");
		WebElement nameEl = browser.findElement(nameBy);
		nameEl.sendKeys(name);
		OOGraphene.tinymce(description, browser);
		
		//save
		By submitBy = By.cssSelector(".o_sel_group_edit_group_form button.btn-primary");
		WebElement submitButton = browser.findElement(submitBy);
		submitButton.click();
		OOGraphene.waitBusy(browser);
		
		return new GroupPage(browser);
	}
	
	/**
	 * Select a group in the list by its name
	 * @param name
	 * @return
	 */
	public GroupPage selectGroup(String name) {
		By linkBy = By.cssSelector("div.o_table_wrapper td a");
		
		WebElement groupLink = null;
		List<WebElement> links = browser.findElements(linkBy);
		for(WebElement link:links) {
			if(link.getText().contains(name)) {
				groupLink = link;
			}
		}
		
		Assert.assertNotNull(groupLink);
		groupLink.click();
		
		By rootTreeNodeBy = By.xpath("//div[contains(@class,'o_tree')]//a/span[contains(text(),'" + name+ "')]");
		OOGraphene.waitElement(rootTreeNodeBy, browser);
		
		return new GroupPage(browser);
	}
}
