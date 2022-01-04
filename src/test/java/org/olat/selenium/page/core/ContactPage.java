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
 * 
 * Drive the E-mail page of contact
 * 
 * Initial date: 27.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ContactPage {
	
	private final WebDriver browser;
	
	public ContactPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public ContactPage assertOnContact() {
		By calendarToolbatBy = By.className("o_sel_contact_form");
		List<WebElement> calendarToolbarsEl = browser.findElements(calendarToolbatBy);
		Assert.assertFalse(calendarToolbarsEl.isEmpty());
		return this;
	}
	
	public ContactPage setContent(String subject, String body) {
		By subjectBy = By.cssSelector("div.o_sel_contact_subject  input[type='text']");
		OOGraphene.waitElement(subjectBy, browser);
		OOGraphene.waitTinymce(browser);
		
		browser.findElement(subjectBy).sendKeys(subject);
		String containerCssSelector = "div.o_sel_contact_body";
		OOGraphene.tinymce(body, containerCssSelector, browser);
		return this;
	}
	
	public ContactPage send() {
		By buttonsBy = By.xpath("//div[contains(@class,'o_sel_contact_buttons')]");
		OOGraphene.scrollTo(buttonsBy, browser);

		By sendBy = By.cssSelector("fieldset.o_sel_contact_form button.btn-primary");
		browser.findElement(sendBy).click();
		By disabledBy = By.cssSelector("fieldset.o_sel_contact_form div.o_sel_contact_body div.o_disabled");
		OOGraphene.waitElement(disabledBy, browser);
		
		OOGraphene.scrollTop(browser);
		OOGraphene.waitAndCloseBlueMessageWindow(browser);
		return this;
	}
	
	public ContactPage assertSend() {
		By sendBy = By.cssSelector("fieldset.o_sel_contact_form div.o_sel_contact_body div.o_disabled");
		OOGraphene.waitElement(sendBy, browser);
		return this;
	}
}
