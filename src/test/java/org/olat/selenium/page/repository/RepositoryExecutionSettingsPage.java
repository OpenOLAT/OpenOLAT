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

import java.util.Date;
import java.util.Locale;

import org.olat.core.util.Formatter;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 6 Nov 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryExecutionSettingsPage {
	
	private final WebDriver browser;
	
	public RepositoryExecutionSettingsPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public RepositoryExecutionSettingsPage setLifecycle(Date validFrom, Date validTo, Locale locale) {
		//select private
		By radioPrivateBy = By.cssSelector(".o_sel_repo_lifecycle_type input[type='radio'][value='private']");
		browser.findElement(radioPrivateBy).click();
		OOGraphene.waitBusy(browser);
		
		By validFromBy = By.cssSelector(".o_sel_repo_lifecycle_validfrom .o_date_picker input[type='text']");
		String validFromStr = Formatter.getInstance(locale).formatDate(validFrom);
		browser.findElement(validFromBy).sendKeys(validFromStr);
		
		By validToBy = By.cssSelector(".o_sel_repo_lifecycle_validto .o_date_picker input[type='text']");
		String validToStr = Formatter.getInstance(locale).formatDate(validTo);
		browser.findElement(validToBy).sendKeys(validToStr);
		
		return this;
	}
	
	public RepositoryExecutionSettingsPage save() {
		By saveBy = By.cssSelector("div.o_sel_repo_save_details button.btn-primary");
		OOGraphene.click(saveBy, browser);
		OOGraphene.waitBusy(browser);
		return this;
	}

}
