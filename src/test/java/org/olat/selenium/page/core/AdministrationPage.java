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
 * Drive the administration site
 * 
 * Initial date: 07.07.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AdministrationPage {

	private final WebDriver browser;

	public AdministrationPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public AdministrationMessagesPage selectInfoMessages() {
		this.selectSystemInfo();
		
		By messagesBy = By.cssSelector(".o_sel_sysinfo span.o_tree_level_label_leaf>a");
		browser.findElement(messagesBy).click();
		OOGraphene.waitBusy(browser);
		return new AdministrationMessagesPage(browser);
	}
	
	public AdministrationPage selectSystemInfo() {
		By systemLinkby = By.xpath("//div[contains(@class,'o_tree')]//a[contains(@onclick,'systemParent')]");
		browser.findElement(systemLinkby).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public AdministrationPage selectModules() {
		By systemLinkby = By.xpath("//div[contains(@class,'o_tree')]//a[contains(@onclick,'modulesParent')]");
		browser.findElement(systemLinkby).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public AdministrationPage clearCache(String cacheName) {
		selectSystemInfo();
		
		//cache tree node
		WebElement cacheLink = browser.findElement(By.cssSelector(".o_sel_caches span.o_tree_level_label_leaf>a"));
		cacheLink.click();
		OOGraphene.waitBusy(browser);
		//table
		WebElement emptyLink = null;
		List<WebElement> rows = browser.findElements(By.cssSelector(".o_table_wrapper table>tbody>tr"));
		for(WebElement row:rows) {
			if(row.getText().contains(cacheName)) {
				emptyLink = row.findElement(By.tagName("a"));
			}
		}
		Assert.assertNotNull(emptyLink);
		//click to empty
		emptyLink.click();
		OOGraphene.waitBusy(browser);
		//confirm
		WebElement yesLink = browser.findElement(By.xpath("//div[contains(@class,'modal-dialog')]//a[contains(@href,'link_0')]"));
		yesLink.click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public AdministrationPage openGroupSettings() {
		selectModules();
		
		WebElement groupLink = browser.findElement(By.cssSelector(".o_sel_group span.o_tree_level_label_leaf>a"));
		groupLink.click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public AdministrationPage setGroupConfirmationForUser(boolean mandatory) {
		By membershipConfirmationBy = By.cssSelector("input[name='mandatory.membership'][value='users']");
		OOGraphene.waitElement(membershipConfirmationBy, 5, browser);
		WebElement membershipConfirmationEl = browser.findElement(membershipConfirmationBy);
		OOGraphene.check(membershipConfirmationEl, new Boolean(mandatory));
		OOGraphene.waitBusy(browser);
		
		return this;
	}
}
