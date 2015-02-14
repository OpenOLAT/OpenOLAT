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
package org.olat.selenium.page.group;

import java.util.List;

import org.junit.Assert;
import org.olat.selenium.page.core.IMPage;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Drive the group run page
 * 
 * Initial date: 03.07.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GroupPage {
	

	private static final By showOwners = By.className("o_sel_group_show_owners");
	private static final By toolsBy = By.className("o_sel_collab_tools");
	
	private WebDriver browser;
	
	public GroupPage() {
		//
	}
	
	public GroupPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public static GroupPage getGroup(WebDriver browser) {
		return new GroupPage(browser);
	}
	
	public GroupPage openAdministration() {
		By adminBy = By.xpath("//div[contains(@class,'o_tree')]//a[contains(@href,'MENU_ADMINISTRATION')]");
		WebElement adminLink = browser.findElement(adminBy);
		adminLink.click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public GroupPage openAdminMembers() {
		openAdminTab(showOwners);
		return this;
	}
	
	public GroupPage openAdminTools() {
		openAdminTab(toolsBy);
		return this;
	}
	
	public IMPage openChat() {
		By chatBy = By.cssSelector("li.o_sel_group_chat a");
		WebElement chatNode = browser.findElement(chatBy);
		chatNode.click();
		OOGraphene.waitBusy(browser);
		return new IMPage(browser);
	}
	
	public GroupPage setVisibility(boolean owners, boolean participants) {
		By showOwnersBy = By.cssSelector(".o_sel_group_show_owners input[type='checkbox']");
		WebElement showOwnersEl = browser.findElement(showOwnersBy);
		if(owners) {
			showOwnersEl.click();
		}
		OOGraphene.waitBusy(browser);
		
		By showParticipants = By.cssSelector(".o_sel_group_show_participants input[type='checkbox']");
		WebElement showParticipantsEl = browser.findElement(showParticipants);
		if(participants) {
			showParticipantsEl.click();
		}
		
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public GroupPage enableTools() {
		By checkToolsBy = By.cssSelector(".o_sel_collab_tools input[type='checkbox']");
		List<WebElement> checkTools = browser.findElements(checkToolsBy);
		Assert.assertFalse(checkTools.isEmpty());
		for(WebElement checkTool:checkTools) {
			checkTool.click();
			OOGraphene.waitBusy(browser);
		}
		return this;
	}
	
	public MembersWizardPage addMember() {
		By addMemberBy = By.className("o_sel_group_add_member");
		WebElement addMemberButton = browser.findElement(addMemberBy);
		addMemberButton.click();
		OOGraphene.waitBusy(browser);
		return new MembersWizardPage(browser);
	}
	
	private void openAdminTab(By marker) {
		By navBarAdmin = By.cssSelector("div.o_tabbed_pane ul>li>a");
		OOGraphene.waitElement(navBarAdmin, browser);
		List<WebElement> tabLinks = browser.findElements(navBarAdmin);
		Assert.assertFalse(tabLinks.isEmpty());

		boolean found = false;
		for(WebElement tabLink:tabLinks) {
			tabLink.click();
			OOGraphene.waitBusy(browser);
			List<WebElement> markerEls = browser.findElements(marker);
			if(markerEls.size() > 0) {
				found = true;
				break;
			}
		}
		Assert.assertTrue(found);
	}
}
