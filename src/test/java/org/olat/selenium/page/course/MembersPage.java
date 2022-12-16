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
import org.olat.selenium.page.group.GroupPage;
import org.olat.selenium.page.group.GroupsPage;
import org.olat.selenium.page.group.MembersWizardPage;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Drive the members management page in course
 * 
 * 
 * Initial date: 12.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MembersPage {

	private final WebDriver browser;
	
	public MembersPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public MembersWizardPage addMember() {
		By addMemberBy = By.className("o_sel_course_add_member");
		browser.findElement(addMemberBy).click();
		OOGraphene.waitModalWizard(browser);
		OOGraphene.waitElementSlowly(By.cssSelector("fieldset.o_sel_usersearch_searchform"), 5, browser);
		return new MembersWizardPage(browser);
	}
	
	public InvitationWizardPage addInvitation() {
		By moreBy = By.cssSelector("button.btn.o_sel_add_more");
		browser.findElement(moreBy).click();
		By moreDropdownBy = By.cssSelector("ul.dropdown-menu.o_sel_add_more");
		OOGraphene.waitElement(moreDropdownBy, browser);
		By addInvitationBy = By.cssSelector("ul.o_sel_add_more a.o_sel_course_invitations");
		browser.findElement(addInvitationBy).click();
		OOGraphene.waitModalWizard(browser);
		OOGraphene.waitElement(By.cssSelector("fieldset.o_sel_import_type_form"), browser);
		return new InvitationWizardPage(browser);
	}
	
	public MembersWizardPage importMembers() {
		By moreBy = By.className("o_sel_add_more");
		OOGraphene.waitElement(moreBy, browser);
		browser.findElement(moreBy).click();

		By importMembersBy = By.className("o_sel_course_import_members");
		OOGraphene.waitElement(importMembersBy, browser);
		browser.findElement(importMembersBy).click();
		
		OOGraphene.waitModalWizard(browser);
		OOGraphene.waitElement(By.cssSelector("div.o_sel_user_import textarea.form-control"), browser);
		return new MembersWizardPage(browser);
	}
	
	public MembersPage selectBusinessGroups() {
		By groupsItemBy = By.cssSelector("li.o_sel_membersmgt_groups a");
		browser.findElement(groupsItemBy).click();
		OOGraphene.waitBusy(browser);
		By groupTitleby = By.cssSelector("h4 i.o_icon.o_icon_group");
		OOGraphene.waitElement(groupTitleby, 5, browser);
		return this;
	}
	
	public MembersPage selectMembers() {
		By groupsItemBy = By.cssSelector("li.o_sel_membersmgt_members a");
		browser.findElement(groupsItemBy).click();
		OOGraphene.waitBusy(browser);
		By groupTitleby = By.cssSelector("h4 i.o_icon.o_icon_group");
		OOGraphene.waitElement(groupTitleby, 5, browser);
		return this;
	}
	
	public GroupPage selectBusinessGroup(String name) {
		return new GroupsPage(browser).selectGroup(name);
	}
	
	public MembersPage createBusinessGroup(String name, String description,
			int maxParticipants, boolean waitingList, boolean auto) {
		By createBy = By.className("o_sel_course_new_group");
		browser.findElement(createBy).click();
		OOGraphene.waitModalDialog(browser);
		
		By popupBy = By.cssSelector("div.modal-content fieldset.o_sel_group_edit_group_form");
		OOGraphene.waitElement(popupBy, browser);
		OOGraphene.waitTinymce(browser);
		
		//fill the form
		By nameBy = By.cssSelector(".o_sel_group_edit_title input[type='text']");
		browser.findElement(nameBy).sendKeys(name);
		OOGraphene.tinymce(description, browser);
		
		if(maxParticipants > 0) {
			By maxParticipantBy = By.cssSelector(".o_sel_group_edit_max_members input[type='text']");
			browser.findElement(maxParticipantBy).sendKeys(Integer.toString(maxParticipants));
		}
		
		if(waitingList) {
			By waitingListBy = By.cssSelector(".o_sel_group_edit_waiting_list input[type='checkbox']");
			browser.findElement(waitingListBy).click();
		}
		if(auto) {
			By autoBy = By.cssSelector(".o_sel_group_edit_auto_close_ranks input[type='checkbox']");
			browser.findElement(autoBy).click();
		}
		
		//save
		By submitBy = By.cssSelector(".o_sel_group_edit_group_form button.btn-primary");
		OOGraphene.click(submitBy, browser);
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	/**
	 * Add a user by username as participant
	 * 
	 * @param user
	 */
	public void quickAdd(UserVO user) {
		addMember()
			.searchMember(user, true)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
	}
	
	/**
	 * Add a user by username with the specified roles.
	 * 
	 * @param user The user to add
	 * @param owner true if the user will be owner
	 * @param coach true if the user will be coach
	 */
	public void quickAdd(UserVO user, boolean owner, boolean coach) {
		addMember()	
			.searchMember(user, true)
			.nextUsers()
			.nextOverview()
			.selectRepositoryEntryRole(owner, coach, false)
			.nextPermissions()
			.finish();
	}
	
	/**
	 * Import the specified users as participants.
	 * 
	 * @param users Users to import
	 */
	public void quickImport(UserVO... users) {
		importMembers()
			.setMembers(users)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
	}
	
	/**
	 * @param name The name of the user
	 * @return Itself
	 */
	public MembersPage openMembership(String name) {
		By toolBy = By.xpath("//div//tr[td/a[text()[contains(.,'" + name+ "')]]]/td/a[i[contains(@class,'o_icon_actions o_icon-fws')]]");
		OOGraphene.waitElement(toolBy, browser);
		browser.findElement(toolBy).click();
		OOGraphene.waitCallout(browser);
		
		By editBy = By.xpath("//div[contains(@class,'popover')]//ul[contains(@class,'o_dropdown')]/li/a[contains(@onclick,'tbl_edit')]");
		browser.findElement(editBy).click();
		
		OOGraphene.waitModalDialog(browser);
		return this;
	}
	
	/**
	 * 
	 * @param participant true/false to change membership
	 * @return Itself
	 */
	public MembersPage editRepositoryMembership(Boolean participant) {
		if(participant != null) {
			By participantBy = By.cssSelector(".o_sel_edit_permissions label input[name='repoRights'][type='checkbox'][value='participant']");
			WebElement participantEl = browser.findElement(participantBy);
			OOGraphene.check(participantEl, participant);
			OOGraphene.waitBusy(browser);
		}
		return this;
	}
	
	/**
	 * Save the member ship of a user.
	 * 
	 * @return Itself
	 */
	public MembersPage saveMembership() {
		By saveBy = By.cssSelector("div.modal-content div.o_button_group button.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		
		// confirm email
		By confirmNoMailBy = By.xpath("//div[contains(@class,'modal-footer')]/a[contains(@onclick,'link_1')]");
		OOGraphene.waitElement(confirmNoMailBy, browser);
		browser.findElement(confirmNoMailBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	/**
	 * Check if the user with the specified first name is in the member list.
	 * @param user
	 * @return
	 */
	public MembersPage assertFirstNameInList(UserVO user) {
		By firstNameBy = By.xpath("//td//a[contains(text(),'" + user.getFirstName() + "')]");
		By rowBy = By.cssSelector(".o_sel_member_list table.table tr");
		List<WebElement> rows = browser.findElements(rowBy);
		boolean found = false;
		for(WebElement row:rows) {
			List<WebElement> firstNameEl = row.findElements(firstNameBy);
			if(firstNameEl.size() > 0) {
				found = true;
				break;
			}
		}
		Assert.assertTrue(found);
		return this;
	}
	
	public MembersPage assertMembersManagement() {
		By membersBy = By.cssSelector("div.o_members_mgmt");
		OOGraphene.waitElement(membersBy, browser);
		return this;
	}
	
	/**
	 * Click back to the course
	 * 
	 * @return
	 */
	public CoursePageFragment clickToolbarBack() {
		OOGraphene.clickBreadcrumbBack(browser);
		return new CoursePageFragment(browser);
	}

}
