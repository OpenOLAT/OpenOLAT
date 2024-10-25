/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.selenium.page.core;

import java.util.List;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 24 oct. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaServerPage {
	
	private final WebDriver browser;
	
	public MediaServerPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public MediaServerPage addDomain(String name, String domain) {
		By formBy = By.cssSelector("fieldset.o_sel_media_server_form");
		OOGraphene.waitElement(formBy, browser);
		
		By tableDomainBy = By.xpath("//fieldset[contains(@class,'o_sel_media_server_form')]//table//td[text()[contains(.,'" + domain + "')]]");
		List<WebElement> currentDomainsList = browser.findElements(tableDomainBy);
		if(currentDomainsList.size() == 1) {
			return this;
		}
		
		By addButtonBy = By.cssSelector(".o_sel_media_server_form a.o_sel_add_domain");
		browser.findElement(addButtonBy).click();
		OOGraphene.waitModalDialog(browser);
		
		By nameBy = By.cssSelector(".modal-dialog form .o_sel_media_server_name input[type=text]");
		browser.findElement(nameBy).sendKeys(name);
		By domainBy = By.cssSelector(".modal-dialog form .o_sel_media_server_domain input[type=text]");
		browser.findElement(domainBy).sendKeys(domain);
		
		By saveBy = By.cssSelector(".modal-dialog form button.btn-primary");
		browser.findElement(saveBy).click();
		
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	public MediaServerPage assertOnDomain(String domain) {
		By domainBy = By.xpath("//fieldset[contains(@class,'o_sel_media_server_form')]//table//td[text()[contains(.,'" + domain + "')]]");
		OOGraphene.waitElement(domainBy, browser);
		return this;
	}

}
