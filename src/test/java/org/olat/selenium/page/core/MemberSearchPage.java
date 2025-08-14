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
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class MemberSearchPage {
	
	private final WebDriver browser;
	
	public MemberSearchPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public MemberSearchPage selectByFirstName(UserVO user) {
		OOGraphene.waitElement(By.cssSelector("div.o_sel_members_search"), browser);
		
		// search user
		By checkBy = By.xpath("//div[@class='o_sel_members_search']//table//tr[td/a[text()[contains(.,'" + user.getFirstName() + "')]]]/td[contains(@class,'o_multiselect')]/input[@name='tb_ms']");	
		browser.findElement(checkBy).click();
		return this;
	}
	
	public void select() {
		By chooseBy = By.xpath("//div[@class='o_sel_members_search']//div[@class='o_button_group']/button[contains(@class,'btn-primary')]");
		OOGraphene.click(chooseBy, browser);
		OOGraphene.waitModalDialogDisappears(browser);
	}
}
