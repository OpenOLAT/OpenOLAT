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
import org.olat.selenium.page.core.BookingPage;
import org.olat.selenium.page.core.CalendarPage;
import org.olat.selenium.page.core.ContactPage;
import org.olat.selenium.page.core.FolderPage;
import org.olat.selenium.page.core.IMPage;
import org.olat.selenium.page.forum.ForumPage;
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.selenium.page.portfolio.PortfolioPage;
import org.olat.selenium.page.wiki.WikiPage;
import org.olat.user.restapi.UserVO;
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
	private static final By editDetails = By.className("o_sel_group_edit_title");
	private static final By bookingConfigBy = By.className("o_sel_accesscontrol_create");
	
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
	
	public GroupPage openEditDetails() {
		openAdminTab(editDetails);
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
	
	public BookingPage openBookingConfig() {
		openAdminTab(bookingConfigBy);
		return new BookingPage(browser);
	}
	
	public IMPage openChat() {
		openMenuItem("o_sel_group_chat");
		return new IMPage(browser);
	}
	
	public CalendarPage openCalendar() {
		openMenuItem("o_sel_group_calendar");
		return new CalendarPage(browser);
	}
	
	public ContactPage openContact() {
		openMenuItem("o_sel_group_contact");
		return new ContactPage(browser);
	}
	
	public GroupPage openMembers() {
		return openMenuItem("o_sel_group_members");
	}
	
	public GroupPage openNews() {
		return openMenuItem("o_sel_group_news");
	}
	
	public FolderPage openFolder() {
		openMenuItem("o_sel_group_folder");
		return new FolderPage(browser);
	}
	
	public ForumPage openForum() {
		openMenuItem("o_sel_group_forum");
		return ForumPage.getGroupForumPage(browser);
	}
	
	public WikiPage openWiki() {
		openMenuItem("o_sel_group_wiki");
		return WikiPage.getGroupWiki(browser);
	}
	
	public PortfolioPage openPortfolio() {
		openMenuItem("o_sel_group_portfolio");
		return new PortfolioPage(browser);
	}
	
	private GroupPage openMenuItem(String cssClass) {
		By newsBy = By.cssSelector("li." + cssClass + " a");
		browser.findElement(newsBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public void close() {
		By closeBy = By.cssSelector("a i.o_icon_close_tool");
		browser.findElement(closeBy).click();
		OOGraphene.waitBusy(browser);
	}
	
	public GroupPage setVisibility(boolean owners, boolean participants, boolean waitingList) {	
		if(owners) {
			By showOwnersBy = By.cssSelector(".o_sel_group_show_owners input[type='checkbox']");
			browser.findElement(showOwnersBy).click();
			OOGraphene.waitBusy(browser);
			OOGraphene.closeBlueMessageWindow(browser);
		}
		
		if(participants) {
			By showParticipants = By.cssSelector(".o_sel_group_show_participants input[type='checkbox']");
			browser.findElement(showParticipants).click();
			OOGraphene.waitBusy(browser);
			OOGraphene.closeBlueMessageWindow(browser);
		}
		
		if(waitingList) {
			By showWaitingListBy = By.cssSelector(".o_sel_group_show_waiting_list input[type='checkbox']");
			browser.findElement(showWaitingListBy).click();
			OOGraphene.waitBusy(browser);
			OOGraphene.closeBlueMessageWindow(browser);
		}
		
		OOGraphene.waitBusy(browser);
		OOGraphene.closeBlueMessageWindow(browser);
		return this;
	}
	
	public GroupPage setWaitingList() {
		By waitingListBy = By.cssSelector(".o_sel_group_edit_waiting_list input[type='checkbox']");
		browser.findElement(waitingListBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public GroupPage setMaxNumberOfParticipants(int max) {
		By maxBy = By.cssSelector(".o_sel_group_edit_max_members input[type='text']");
		browser.findElement(maxBy).sendKeys(Integer.toString(max));
		return this;
	}
	
	/**
	 * Save the details form.
	 * @return The group page
	 */
	public GroupPage saveDetails() {
		By submitBy = By.cssSelector(".o_sel_group_edit_group_form button.btn-primary");
		WebElement submitButton = browser.findElement(submitBy);
		submitButton.click();
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
	
	public GroupPage setMembersInfos(String text) {		
		OOGraphene.tinymce(text, browser);
		
		By submitBy = By.cssSelector(".o_sel_collaboration_news_save button.btn-primary");
		browser.findElement(submitBy).click();
		OOGraphene.waitBusy(browser);
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
	
	public GroupPage assertOnInfosPage(String name) {
		By groupNameBy = By.xpath("//div[@id='o_main_center_content_inner']//p[contains(text(),'" + name+ "')]");
		List<WebElement> groupNameEls = browser.findElements(groupNameBy);
		Assert.assertFalse(groupNameEls.isEmpty());
		return this;
	}
	
	public GroupPage assertNews(String name) {
		By groupNameBy = By.xpath("//div[@id='o_main_center_content_inner']//div[@id='o_msg_info']//p[contains(text(),'" + name+ "')]");
		List<WebElement> groupNameEls = browser.findElements(groupNameBy);
		Assert.assertFalse(groupNameEls.isEmpty());
		return this;
	}
	
	public GroupPage assertOnWaitingList(String name) {
		//check group name
		By groupNameBy = By.cssSelector("#o_main_center_content_inner h4");
		WebElement groupNameEl = browser.findElement(groupNameBy);
		Assert.assertTrue(groupNameEl.getText().contains(name));
		//check the warning
		By warningBy = By.xpath("//div[@id='o_main_center_content_inner']//p[contains(@class,'o_warning')]");
		List<WebElement> warningEls = browser.findElements(warningBy);
		Assert.assertFalse(warningEls.isEmpty());
		return this;
	}
	
	public GroupPage assertParticipantList() {
		By participantListBy = By.id("o_sel_group_participants");
		List<WebElement> participantListEl = browser.findElements(participantListBy);
		if(participantListEl.size() == 0) {
			System.out.println();
		}
		return this;
	}
	
	public GroupPage assertMembersInOwnerList(UserVO owner) {
		return assertMembers(owner, "o_sel_group_coaches");
	}
	
	public GroupPage assertMembersInParticipantList(UserVO owner) {
		return assertMembers(owner, "o_sel_group_participants");
	}
	
	public GroupPage assertMembersInWaitingList(UserVO owner) {
		return assertMembers(owner, "o_sel_group_waiting_list");
	}
	
	private GroupPage assertMembers(UserVO member, String cssClass) {
		boolean isMember = isMembers( member, cssClass);
		Assert.assertTrue(isMember);
		return this;
	}
	
	public boolean isInMembersOwnerList(UserVO owner) {
		return isMembers(owner, "o_sel_group_coaches");
	}
	
	public boolean isInMembersParticipantList(UserVO owner) {
		return isMembers(owner, "o_sel_group_participants");
	}
	
	public boolean isInMembersInWaitingList(UserVO owner) {
		return isMembers(owner, "o_sel_group_waiting_list");
	}
	
	private boolean isMembers(UserVO member, String cssClass) {
		String firstName = member.getFirstName();
		By longBy = By.xpath("//div[@id='" + cssClass + "']//table//tr//td//a[contains(text(),'" + firstName + "')]");
		List<WebElement> elements = browser.findElements(longBy);
		return elements.size() > 0;
	}
}
