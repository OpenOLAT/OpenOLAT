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
package org.olat.selenium.page.repository;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 20 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryCertificateSettingsPage {
	
	private final WebDriver browser;
	
	public RepositoryCertificateSettingsPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public RepositoryCertificateSettingsPage enableCertificates(boolean auto) {
		By enableBy = By.cssSelector("fieldset.o_sel_certificate_settings a.o_button_toggle");
		OOGraphene.waitElement(enableBy, browser);
		browser.findElement(enableBy).click();
		
		By optionsBy = By.cssSelector("fieldset.o_sel_certificate_settings .o_sel_certificate_options");
		OOGraphene.waitElement(optionsBy, browser);
		
		By by;
		if(auto) {
			by = By.cssSelector("fieldset.o_sel_certificate_settings .o_sel_certificate_options input[type='checkbox'][value='auto']");
		} else {
			by = By.cssSelector("fieldset.o_sel_certificate_settings .o_sel_certificate_options input[type='checkbox'][value='manual']");
		}
		WebElement checkEl = browser.findElement(by);
		OOGraphene.check(checkEl, Boolean.TRUE);
		return this;
	}
	
	public RepositoryCertificateSettingsPage enableRecertification() {
		By recertificationBy = By.cssSelector("fieldset.o_sel_certificate_settings input[type='checkbox'][name='recertification.period']");
		WebElement checkEl = browser.findElement(recertificationBy);
		OOGraphene.check(checkEl, Boolean.TRUE);
		
		By timelapseBy = By.xpath("//fieldset[contains(@class,'o_sel_certificate_settings')]//select[@name='timelapse.unit_SELBOX']");
		OOGraphene.waitElement(timelapseBy, browser);
		return this;
	}
	
	public RepositoryCertificateSettingsPage save() {
		By saveSwitch = By.cssSelector("fieldset.o_sel_certificate_settings button.btn.btn-primary");
		browser.findElement(saveSwitch).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	

}
