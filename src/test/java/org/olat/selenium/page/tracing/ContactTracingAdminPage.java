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
package org.olat.selenium.page.tracing;

import org.olat.core.util.StringHelper;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 16 juin 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ContactTracingAdminPage {
	
	private final WebDriver browser;
	
	public ContactTracingAdminPage(WebDriver browser) {
		this.browser = browser; 
	}
	
	/**
	 * Enable contact tracing.
	 * 
	 * @return Itself
	 */
	public ContactTracingAdminPage enableTracing() {
		By enableBy = By.xpath("//div[contains(@class,'o_sel_contacttracing_enable')]//label/input[@name='contact.tracing.enabled' and @value='on']");
		OOGraphene.waitElement(enableBy, browser);
		WebElement checkEl = browser.findElement(enableBy);
		OOGraphene.check(checkEl, Boolean.TRUE);
		OOGraphene.waitBusy(browser);
		
		By anonymousBy = By.cssSelector("div.o_sel_contacttracing_anonymous");
		OOGraphene.waitElement(anonymousBy, browser);
		
		// save on click
		return this;
	}
	
	/**
	 * Select the locations tab in administration overview.
	 * 
	 * @return Itself
	 */
	public ContactTracingAdminPage selectLocations() {
		By locationsBy = By.cssSelector("div.o_segments a.btn.o_sel_contacttracing_locations");
		OOGraphene.waitElement(locationsBy, browser);
		browser.findElement(locationsBy).click();
		
		By addLocationBy = By.cssSelector("div.o_button_group a.o_sel_contacttracing_add_location");
		OOGraphene.waitElement(addLocationBy, browser);
		return this;
	}
	
	/**
	 * Add and save a new location.
	 * 
	 * @param reference The reference (optional)
	 * @param title The title (optional)
	 * @param building The building (optional)
	 * @return
	 */
	public String addLocation(String reference, String title, String building) {
		By addLocationBy = By.cssSelector("div.o_button_group a.o_sel_contacttracing_add_location");
		OOGraphene.waitElement(addLocationBy, browser);
		browser.findElement(addLocationBy).click();
		OOGraphene.waitModalDialog(browser);
		
		OOGraphene.waitElement(By.cssSelector("fieldset.o_sel_contacttracing_location"), browser);
		
		if(StringHelper.containsNonWhitespace(reference)) {
			By refBy = By.cssSelector("fieldset.o_sel_contacttracing_location div.o_sel_contacttracing_ref input[type='text']");
			browser.findElement(refBy).sendKeys(reference);
		}
		if(StringHelper.containsNonWhitespace(title)) {
			By titleBy = By.cssSelector("fieldset.o_sel_contacttracing_location div.o_sel_contacttracing_title input[type='text']");
			browser.findElement(titleBy).sendKeys(title);
		}
		if(StringHelper.containsNonWhitespace(building)) {
			By buildingBy = By.cssSelector("fieldset.o_sel_contacttracing_location div.o_sel_contacttracing_building input[type='text']");
			browser.findElement(buildingBy).sendKeys(building);
		}
		
		By urlBy = By.xpath("//fieldset[contains(@class,'o_sel_contacttracing_location')]//div[contains(@class,'o_sel_contacttracing_qrid')]//div[contains(@class,'o_form_example')]");
		String url = browser.findElement(urlBy).getText().trim();
		
		By saveBy = By.cssSelector("fieldset.o_sel_contacttracing_location button.btn.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		return url;
	}
	


}
