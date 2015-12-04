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

import org.junit.Assert;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Drive the configuration of the forum's course element.
 * 
 * Initial date: 01.12.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ForumCEPage {

	private WebDriver browser;
	
	public ForumCEPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public ForumCEPage selectConfiguration() {
		By configBy = By.className("o_sel_course_forum_settings");
		return selectTab(configBy);
	}
	
	public ForumCEPage allowGuest() {
		By allowBy = By.cssSelector("div.o_sel_course_forum_allow_guest label input[type='checkbox']");
		browser.findElement(allowBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	private ForumCEPage selectTab(By tabBy) {
		List<WebElement> tabLinks = browser.findElements(CourseEditorPageFragment.navBarNodeConfiguration);

		boolean found = false;
		a_a:
		for(WebElement tabLink:tabLinks) {
			tabLink.click();
			OOGraphene.waitBusy(browser);
			List<WebElement> chooseRepoEntry = browser.findElements(tabBy);
			if(chooseRepoEntry.size() > 0) {
				found = true;
				break a_a;
			}
		}

		Assert.assertTrue("Found the tab", found);
		return this;
	}

}
