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
package org.olat.selenium.page.course;

import java.util.List;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.junit.Assert;
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 12.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentToolPage {
	
	@Drone
	private WebDriver browser;
	
	public AssessmentToolPage() {
		//
	}
	
	public AssessmentToolPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public AssessmentToolPage users() {
		By usersBy = By.cssSelector("li.o_sel_assessment_tool_users a");
		WebElement usersLink = browser.findElement(usersBy);
		usersLink.click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public AssessmentToolPage assertOnUsers(UserVO user) {
		By usersCellsBy = By.cssSelector("div.o_table_layout table tr td.text-left");
		List<WebElement> usersCellsList = browser.findElements(usersCellsBy);
		Assert.assertFalse(usersCellsList.isEmpty());
		
		boolean found = false;
		for(WebElement usersCell:usersCellsList) {
			found |= usersCell.getText().contains(user.getFirstName());
		}
		Assert.assertTrue(found);
		return this;
	}
	
	public AssessmentToolPage selectUser(UserVO user) {
		By userLinksBy = By.xpath("//div[contains(@class,'o_table_layout')]//table//tr//td//a[text()[contains(.,'" + user.getFirstName() + "')]]");
		WebElement userLink = browser.findElement(userLinksBy);
		userLink.click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public AssessmentToolPage assertPassed(UserVO user) {
		By userInfosBy = By.cssSelector("div.panel li.list-group-item");
		List<WebElement> userInfoList = browser.findElements(userInfosBy);
		Assert.assertFalse(userInfoList.isEmpty());
		boolean foundFirstName = false;
		for(WebElement userInfo:userInfoList) {
			foundFirstName |= userInfo.getText().contains(user.getFirstName());
		}
		Assert.assertTrue(foundFirstName);
		
		By passedBy = By.cssSelector("div.o_table_layout table tr td.text-left span.o_state.o_passed");
		List<WebElement> passedEl = browser.findElements(passedBy);
		Assert.assertFalse(passedEl.isEmpty());
		Assert.assertTrue(passedEl.get(0).isDisplayed());
		return this;
	}
}
