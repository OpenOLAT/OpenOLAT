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
package org.olat.selenium.page.course;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 23 sept. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoConfigurationPage {
	
	private final WebDriver browser;
	
	public VideoConfigurationPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public VideoConfigurationPage selectVideoConfiguration() {
		By tabBy = By.cssSelector("ul.o_node_config li.o_sel_video_configuration>a");
		OOGraphene.waitElement(tabBy, browser);
		browser.findElement(tabBy).click();
		By configBy = By.cssSelector("fieldset.o_sel_video_configuration_form");
		OOGraphene.waitElement(configBy, browser);
		return this;
	}
	
	public VideoConfigurationPage selectVideoUrl(String resourceTitle, String url) {
		By chooseBy = By.cssSelector("fieldset.o_sel_video_configuration_form a.o_sel_video_choose_repofile");
		browser.findElement(chooseBy).click();
		OOGraphene.waitModalDialog(browser);
		
		By importByUrlBy = By.cssSelector("div.o_sel_search_referenceable_entries a.o_sel_repo_popup_import_url_resource");
		OOGraphene.waitElement(importByUrlBy, browser);
		browser.findElement(importByUrlBy).click();
		OOGraphene.waitModalDialog(browser, "fieldset.o_sel_re_import_url_form");
		
		By urlBy = By.cssSelector("fieldset.o_sel_re_import_url_form div.o_sel_import_url input[type='text']");
		browser.findElement(urlBy).sendKeys(url);
		By displayNameBy = By.cssSelector("fieldset.o_sel_re_import_url_form div.o_sel_author_imported_name input[type='text']");
		browser.findElement(displayNameBy).sendKeys("");
		
		By typeBy = By.xpath("//fieldset[contains(@class,'o_sel_re_import_url_form')]//div[contains(@class,'o_sel_import_type')]//select/option[@value='FileResource.VIDEO']"); 
		OOGraphene.waitElement(typeBy, 15, browser);
		
		browser.findElement(displayNameBy).clear();
		browser.findElement(displayNameBy).sendKeys(resourceTitle);
		
		By submitBy = By.cssSelector("fieldset.o_sel_re_import_url_form .o_sel_repo_save_details button.btn.btn-primary");
		browser.findElement(submitBy).click();
		OOGraphene.waitModalDialogWithFieldsetDisappears(browser, "o_sel_re_import_url_form");
		
		OOGraphene.waitModalDialogDisappears(browser);
		
		//double check that the resource is selected (search the preview link)
		By landingBy = By.xpath("//fieldset[contains(@class,'o_sel_video_configuration_form')]//a[contains(@onclick,'command.preview')][i[contains(@class,'o_icon_preview')]][span/text()[contains(.,'" + resourceTitle + "')]]");
		OOGraphene.waitElementSlowly(landingBy, 15, browser);
		return this;
		
	}

}
