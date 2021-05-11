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

import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * An helper page to search a user with the standard modal dialog.
 * 
 * Initial date: 11 mai 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserSearchPage {
	
	private final WebDriver browser;
	
	public UserSearchPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public UserSearchPage searchUserByFirstName(UserVO user) {
		OOGraphene.waitElement(By.cssSelector("fieldset.o_sel_usersearch_searchform"), browser);
		
		// search user
		By firstNameBy = By.cssSelector(".o_sel_usersearch_searchform div.o_sel_user_search_firstname input.o_sel_user_search_firstname[type='text']");	
		browser.findElement(firstNameBy).sendKeys(user.getFirstName());
		
		By searchBy = By.cssSelector(".o_sel_usersearch_searchform a.btn-default");
		OOGraphene.moveAndClick(searchBy, browser);
		return this;
	}
	
	public UserSearchPage selectAll() {
		By selectAll = By.xpath("//div[contains(@class,'modal')]//th/div[contains(@class,'o_table_checkall')]/a[i[contains(@class,'o_icon_check_off')]]");
		OOGraphene.waitElement(selectAll, browser);
		browser.findElement(selectAll).click();
		OOGraphene.waitBusy(browser);
		By selectedAll = By.xpath("//div[contains(@class,'modal')]//th/div[contains(@class,'o_table_checkall')]/a[i[contains(@class,'o_icon_check_on')]]");
		OOGraphene.waitElement(selectedAll, browser);
		return this;
	}
	
	public void choose() {
		By chooseBy = By.xpath("//fieldset[@class='o_sel_usersearch_searchform']//div[@class='o_table_footer']/div[@class='o_button_group']/button[@name='msc']");
		browser.findElement(chooseBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
	}

}
