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
package org.olat.selenium.page.portfolio;

import java.io.File;

import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 09.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class MediaCenterPage {
	
	private final WebDriver browser;

	public MediaCenterPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public MediaCenterPage assertOnMediaCenter() {
		By mediaCenterBy = By.cssSelector("div.o_personal_folder div.o_media_browser");
		OOGraphene.waitElement(mediaCenterBy, browser);
		return this;
	}
	
	public MediaCenterPage assertOnMedia(String name) {
		By nameBy = By.xpath("//div[contains(@class,'o_media_card')]//a/span[contains(text(),'" + name + "')]");
		OOGraphene.waitElement(nameBy, browser);
		return this;
	}
	
	public MediaCenterPage assertOnMediaTable(String name) {
		By nameBy = By.xpath("//div[contains(@class,'o_media_browser')]//div[contains(@class,'o_medias_table')]//div[@class='o_media_card_cell']//h5/a/span[contains(text(),'" + name + "')]");
		OOGraphene.waitElement(nameBy, browser);
		return this;
	}
	
	/**
	 * Assert on the title of the details page of a media.
	 * 
	 * @param name The name of the media/artefact
	 * @return Itself
	 */
	public MediaCenterPage assertOnMediaDetails(String name) {
		By nameBy = By.xpath("//div[contains(@class,'o_personal_folder')]//h2[contains(text(),'" + name + "')]");
		OOGraphene.waitElement(nameBy, browser);
		OOGraphene.waitElement(By.cssSelector("div.o_personal_folder .o_sel_media_metadata"), browser);
		return this;
	}
	
	public MediaDetailsPage selectMedia(String name) {
		By nameBy = By.xpath("//div[contains(@class,'o_media_card')]//a[span[contains(text(),'" + name + "')]]");
		OOGraphene.waitElement(nameBy, browser);
		browser.findElement(nameBy).click();
		OOGraphene.waitBusy(browser);
		return new MediaDetailsPage(browser);
	}
	
	public MediaCenterPage uploadMedia(String title, File file) {
		By addMedia = By.cssSelector("div.o_personal_folder a.o_sel_add_media");
		browser.findElement(addMedia).click();
		OOGraphene.waitModalDialog(browser);
		
		By titleBy = By.cssSelector("fieldset.o_sel_upload_media_form .o_sel_media_title input[type='text']");
		browser.findElement(titleBy).sendKeys(title);
	
		By inputBy = By.cssSelector("fieldset.o_sel_upload_media_form .o_fileinput input[type='file']");
		OOGraphene.uploadFile(inputBy, file, browser);
		OOGraphene.waitBusy(browser);
		By uploadedBy = By.cssSelector("fieldset.o_sel_upload_media_form .o_sel_file_uploaded");
		OOGraphene.waitElement(uploadedBy, browser);
		
		By saveBy = By.cssSelector("fieldset.o_sel_upload_media_form .o_sel_buttons button.btn.btn-primary");
		OOGraphene.click(saveBy, browser);
		OOGraphene.waitModalDialogDisappears(browser);
		
		return this;
	}

	public MediaCenterPage shareWithMeFilter() {
		By sharedWithMeBy = By.cssSelector("div.o_media_browser div.o_table_tabs ul>li>a.o_sel_media_shared_with_me");
		OOGraphene.waitElement(sharedWithMeBy, browser);
		browser.findElement(sharedWithMeBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public MediaCenterPage openShares() {
		By shareBy = By.cssSelector("ul.nav>li.o_sel_media_relations>a");
		browser.findElement(shareBy).click();
		
		By shareFormBy = By.cssSelector("fieldset.o_sel_media_relations_form");
		OOGraphene.waitElement(shareFormBy, browser);
		return this;
	}
	
	public MediaCenterPage shareWithUser(UserVO user) {
		openShareMenu();
		
		By shareWithUserBy = By.cssSelector("ul a.o_sel_share_with_user");
		browser.findElement(shareWithUserBy).click();
		OOGraphene.waitModalDialog(browser, "fieldset.o_sel_user_search_form");
		
		By nameBy = By.cssSelector(".o_sel_user_search_form .o_sel_user_search_firstname input[type='text']");
		OOGraphene.waitElement(nameBy, browser);
		browser.findElement(nameBy).sendKeys(user.getFirstName());
		
		By searchBy = By.cssSelector(".o_sel_user_search_form a.btn-default.o_sel_user_search_button");
		browser.findElement(searchBy).click();
		
		By selectBy = By.xpath("//fieldset[contains(@class,'o_sel_usersearch_searchform')]//table//tr[td[text()[contains(.,'" + user.getFirstName() + "')]]]/td/a[contains(@onclick,'ssc')]");
		OOGraphene.waitElement(selectBy, browser);
		browser.findElement(selectBy).click();
		
		OOGraphene.waitModalDialogDisappears(browser);
		By sharedWithBy = By.xpath("//fieldset[contains(@class,'o_sel_media_relations_form')]//table//tr/td/div/span[text()[contains(.,'" + user.getFirstName() + "')]]");
		OOGraphene.waitElement(sharedWithBy, browser);

		return this;
	}
	
	private MediaCenterPage openShareMenu() {
		By openMenuBy = By.cssSelector("fieldset.o_sel_media_relations_form button.o_sel_add_shares");
		browser.findElement(openMenuBy).click();
		
		By menuBy = By.cssSelector("fieldset.o_sel_media_relations_form ul.o_sel_add_shares");
		OOGraphene.waitElement(menuBy, browser);
		return this;
	}
}
