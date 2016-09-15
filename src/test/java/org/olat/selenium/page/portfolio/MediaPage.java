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
package org.olat.selenium.page.portfolio;

import org.olat.core.util.StringHelper;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 08.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaPage {
	
	private final WebDriver browser;

	public MediaPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public MediaPage fillForumMedia(String title, String description) {
		return fillStandardMedia(title, description);
	}
	
	public MediaPage fillEfficiencyStatementMedia(String title, String description) {
		return fillStandardMedia(title, description);
	}
	
	private MediaPage fillStandardMedia(String title, String description) {
		if(StringHelper.containsNonWhitespace(title)) {
			By titleBy = By.cssSelector(".o_sel_pf_collect_media_title input[type='text']");
			WebElement titleEl = browser.findElement(titleBy);
			titleEl.clear();
			titleEl.sendKeys(title);
		}
		
		if(StringHelper.containsNonWhitespace(description)) {
			String cssSelector = ".o_sel_pf_collect_media_description";
			OOGraphene.tinymce(description, cssSelector, browser);
		}
		
		//save
		By submitBy = By.cssSelector(".o_sel_pf_collect_media_form button.btn-primary");
		WebElement submitButton = browser.findElement(submitBy);
		submitButton.click();
		OOGraphene.waitBusy(browser);
		return this;
	}

}
