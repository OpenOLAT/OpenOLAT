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
package org.olat.selenium.page.user;

import org.junit.Assert;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Drive the profile of a user.
 * 
 * Initial date: 19.04.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserProfilePage {
	
	private WebDriver browser;
	
	public UserProfilePage(WebDriver browser) {
		this.browser = browser;
	}
	
	/**
	 * Check if the profile panel is visible (but doesn't check if it is active)
	 * @return
	 */
	public UserProfilePage assertOnProfile() {
		By profileSegmentBy = By.cssSelector("div.o_segments a.btn.o_sel_usersettings_profile");
		try {
			OOGraphene.waitElement(profileSegmentBy, browser);
		} catch (Exception e) {
			OOGraphene.takeScreenshot("Assertonprofile", browser);
			throw e;
		}
		WebElement profileSegmentEl = browser.findElement(profileSegmentBy);
		Assert.assertTrue(profileSegmentEl.isDisplayed());
		return this;
	}
	
	/**
	 * Check if the user name in the profile segment is visible.
	 * 
	 * @param username
	 * @return
	 */
	public UserProfilePage assertOnUsername(String username) {
		By usernameBy = By.xpath("//div[contains(@class,'o_user_profile_form')]//input[@value='" + username + "']");
		OOGraphene.waitElement(usernameBy, 5, browser);
		WebElement usernameEl = browser.findElement(usernameBy);
		Assert.assertTrue(usernameEl.isDisplayed());
		return this;
	}

}
