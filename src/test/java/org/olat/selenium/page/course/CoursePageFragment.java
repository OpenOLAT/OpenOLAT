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

import java.net.URL;
import java.util.List;

import org.junit.Assert;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.model.SingleRoleRepositoryEntrySecurity.Role;
import org.olat.restapi.support.vo.CourseVO;
import org.olat.selenium.page.core.BookingPage;
import org.olat.selenium.page.core.MenuTreePageFragment;
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.selenium.page.lecture.LectureRepositoryAdminPage;
import org.olat.selenium.page.lecture.LecturesRepositoryPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 20.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CoursePageFragment {
	
	public static final By courseRun = By.className("o_course_run");
	public static final By toolsMenu = By.cssSelector("ul.o_sel_course_tools");
	public static final By toolsMenuCaret = By.cssSelector("a.o_sel_course_tools");
	
	public static final By editCourseBy = By.className("o_sel_course_editor");
	public static final By accessConfigBy = By.className("o_sel_course_access");
	public static final By bookingBy = By.className("o_sel_course_ac_tool");
	public static final By assessmentToolBy = By.className("o_sel_course_assessment_tool");
	public static final By assessmentModeBy = By.className("o_sel_course_assessment_mode");
	public static final By membersCourseBy = By.className("o_sel_course_members");
	public static final By treeContainerBy = By.id("o_main_left_content");
	public static final By efficiencyStatementsBy = By.className("o_sel_course_options_certificates");
	public static final By lecturesAdministrationBy = By.className("o_sel_course_lectures_admin");
	
	private final WebDriver browser;
	
	public CoursePageFragment(WebDriver browser) {
		this.browser = browser;
	}
	
	public static CoursePageFragment getCourse(WebDriver browser, URL deploymentUrl, CourseVO course) {
		browser.get(deploymentUrl.toExternalForm() + "url/RepositoryEntry/" + course.getRepoEntryKey());
		try {
			OOGraphene.waitElementSlowly(courseRun, 10, browser);
		} catch (Exception e) {
			OOGraphene.takeScreenshot("GetcourseByGet", browser);
			throw e;
		}
		return new CoursePageFragment(browser);
	}
	
	public static CoursePageFragment getCourse(WebDriver browser) {
		OOGraphene.waitElement(courseRun, browser);
		return new CoursePageFragment(browser);
	}
	
	public CoursePageFragment assertOnCoursePage() {
		WebElement treeContainer = browser.findElement(treeContainerBy);
		Assert.assertTrue(treeContainer.isDisplayed());
		return this;
	}
	
	public CoursePageFragment assertOnLearnPath() {
		By menuPathBy = By.cssSelector("div.o_tree.o_tree_root_visible.o_course_menu.o_lp_tree");
		OOGraphene.waitElement(menuPathBy, browser);
		return this;
	}
	
	public CoursePageFragment assertOnTitle(String displayName) {
		By titleBy = By.xpath("//h2[span/text()[contains(.,'" + displayName + "')]]");
		OOGraphene.waitElement(titleBy, browser);
		return this;
	}
	
	public CoursePageFragment assertOnLearnPathNodeDone(String nodeTitle) {
		return assertOnLearnPathNodeStatus(nodeTitle, "o_lp_done");
	}
	
	public CoursePageFragment assertOnLearnPathNodeReady(String nodeTitle) {
		return assertOnLearnPathNodeStatus(nodeTitle, "o_lp_ready");
	}
	
	public CoursePageFragment assertOnLearnPathNodeInProgress(String nodeTitle) {
		return assertOnLearnPathNodeStatus(nodeTitle, "o_lp_in_progress");
	}
	
	public CoursePageFragment assertOnLearnPathNodeNotAccessible(String nodeTitle) {
		return assertOnLearnPathNodeStatus(nodeTitle, "o_lp_not_accessible");
	}
	
	private CoursePageFragment assertOnLearnPathNodeStatus(String nodeTitle, String statusCssClass) {
		if(nodeTitle.length() > 20) {
			nodeTitle = nodeTitle.substring(0, 20);
		}
		By nodeDoneBy = By.xpath("//div[contains(@class,'o_lp_tree')]//li[contains(@class,'" + statusCssClass + "')]/div/span[contains(@class,'o_tree_l')]/a[span[text()[contains(.,'" + nodeTitle + "')]]]");
		OOGraphene.waitElement(nodeDoneBy, browser);
		return this;
	}
	
	public CoursePageFragment assertOnLearnPathPercent(int percent) {
		By percentageBy = By.xpath("//span[contains(@class,'o_progress')]//span[contains(@class,'percentage')]//span[text()[contains(.,'" + percent + "%')]]");
		OOGraphene.waitElement(percentageBy, browser);
		return this;
	}
	
	/**
	 * Assert if the password field is displayed.
	 * @return
	 */
	public CoursePageFragment assertOnPassword() {
		By passwordBy = By.cssSelector(".o_sel_course_password_form input[type='password']");
		List<WebElement> passwordEls = browser.findElements(passwordBy);
		Assert.assertEquals(1, passwordEls.size());
		return this;
	}
	
	public CoursePageFragment enterPassword(String password) {
		By passwordBy = By.cssSelector(".o_sel_course_password_form .o_sel_course_password input[type='password']");
		browser.findElement(passwordBy).sendKeys(password);
		
		By enterBy = By.cssSelector(".o_sel_course_password_form button.btn-primary");
		browser.findElement(enterBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Wait until the restart button appears or make an error.
	 * 
	 * @return
	 */
	public CoursePageFragment assertOnRestart() {
		By restartBy = By.cssSelector("a.btn.o_sel_course_restart");
		OOGraphene.waitElement(restartBy, 10, browser);
		return this;
	}
	
	public CoursePageFragment clickRestart() {
		By restartBy = By.cssSelector("a.btn.o_sel_course_restart");
		browser.findElement(restartBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitElement(courseRun, 5, browser);
		return this;
	}
	
	/**
	 * Click the first element of the menu tree.
	 * 
	 * @return The menu tree page
	 */
	public MenuTreePageFragment clickTree() {
		OOGraphene.waitElement(MenuTreePageFragment.treeBy, browser);
		MenuTreePageFragment menuTree = new MenuTreePageFragment(browser);
		return menuTree.selectRoot();
	}
	
	/**
	 * Click the node with the specified title.
	 * 
	 * @param title The title of the course node
	 * @return The menu tree page
	 */
	public MenuTreePageFragment clickTreeNode(String title) {
		OOGraphene.waitElement(MenuTreePageFragment.treeBy, browser);
		MenuTreePageFragment menuTree = new MenuTreePageFragment(browser);
		return menuTree.selectWithTitle(title);
	}
	
	/**
	 * Confirm you have done the course element.
	 * 
	 * @return Itself
	 */
	public CoursePageFragment confirmNode() {
		By confirmationBy = By.cssSelector("div.o_course_pagination div.o_confirm a.btn");
		OOGraphene.waitElement(confirmationBy, browser);
		browser.findElement(confirmationBy).click();
		OOGraphene.waitBusy(browser);

		By confirmedBy = By.cssSelector("div.o_course_pagination div.o_confirm a.btn.o_course_pagination_status_done");
		OOGraphene.waitElement(confirmedBy, browser);
		return this;
	}
	
	/**
	 * Open the tools drop-down.
	 * 
	 * @return IUtself
	 */
	public CoursePageFragment openToolsMenu() {
		browser.findElement(toolsMenuCaret).click();
		OOGraphene.waitElement(toolsMenu, browser);
		return this;
	}
	
	public RemindersPage reminders() {
		if(!browser.findElement(toolsMenu).isDisplayed()) {
			openToolsMenu();
		}
		By reminderBy = By.cssSelector("a.o_sel_course_reminders");
		browser.findElement(reminderBy).click();
		OOGraphene.waitBusy(browser);
		return new RemindersPage(browser);
	}
	
	public CourseSettingsPage settings() {
		if(!browser.findElement(toolsMenu).isDisplayed()) {
			openToolsMenu();
		}
		
		By reminderBy = By.cssSelector("a.o_sel_course_settings");
		browser.findElement(reminderBy).click();
		OOGraphene.waitBusy(browser);

		return new CourseSettingsPage(browser);
	}
	
	/**
	 * Click the editor link in the tools drop-down and
	 * wait the edit mode.
	 * 
	 * @return Itself
	 */
	public CourseEditorPageFragment edit() {
		if(!browser.findElement(toolsMenu).isDisplayed()) {
			openToolsMenu();
		}
		browser.findElement(editCourseBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitElement(By.xpath("//div[contains(@class,'o_edit_mode')]"), browser);
		return new CourseEditorPageFragment(browser);
	}
	
	/**
	 * Try to edit the course but don't wait the edit mode.
	 * 
	 * @return Itself
	 */
	public CourseEditorPageFragment tryToEdit() {
		if(!browser.findElement(toolsMenu).isDisplayed()) {
			openToolsMenu();
		}
		browser.findElement(editCourseBy).click();
		OOGraphene.waitBusy(browser);
		return new CourseEditorPageFragment(browser);
	}
	
	/**
	 * Click the members link in the tools drop-down
	 * @return
	 */
	public MembersPage members() {
		if(!browser.findElement(toolsMenu).isDisplayed()) {
			openToolsMenu();
		}
		browser.findElement(membersCourseBy).click();
		OOGraphene.waitBusy(browser);

		By mainId = By.id("o_main");
		OOGraphene.waitElement(mainId, browser);
		return new MembersPage(browser);
	}
	
	public AssessmentToolPage assessmentTool() {
		if(!browser.findElement(toolsMenu).isDisplayed()) {
			openToolsMenu();
		}
		browser.findElement(assessmentToolBy).click();
		OOGraphene.waitBusy(browser);

		By assessmentTool = By.id("o_assessment_tool_main");
		OOGraphene.waitElement(assessmentTool, browser);
		WebElement main = browser.findElement(assessmentTool);
		Assert.assertTrue(main.isDisplayed());
		return new AssessmentToolPage(browser);
	}
	
	public AssessmentModePage assessmentConfiguration() {
		if(!browser.findElement(toolsMenu).isDisplayed()) {
			openToolsMenu();
		}
		browser.findElement(assessmentModeBy).click();
		OOGraphene.waitBusy(browser);
		return new AssessmentModePage(browser);
	}
	
	public LectureRepositoryAdminPage lecturesAdministration() {
		if(!browser.findElement(toolsMenu).isDisplayed()) {
			openToolsMenu();
		}
		browser.findElement(lecturesAdministrationBy).click();
		OOGraphene.waitBusy(browser);
		return new LectureRepositoryAdminPage(browser)
				.assertOnAdminPage();
	}
	
	public LecturesRepositoryPage lectures() {
		By lecturesBy = By.xpath("//li[contains(@class,'o_tool')]/a[contains(@onclick,'command.lectures')]");
		OOGraphene.waitElement(lecturesBy, browser);
		browser.findElement(lecturesBy).click();
		
		By teacherOverviewBy = By.cssSelector("div.o_lectures_teacher_overview");
		OOGraphene.waitElement(teacherOverviewBy, browser);
		return new LecturesRepositoryPage(browser);
	}
	
	public BookingPage bookingTool() {
		if(!browser.findElement(toolsMenu).isDisplayed()) {
			openToolsMenu();
		}
		browser.findElement(bookingBy).click();
		OOGraphene.waitBusy(browser);
		return new BookingPage(browser);
	}
	
	/**
	 * Set the course status to published.
	 * 
	 * @return Itself
	 */
	public CoursePageFragment publish() {
		return changeStatus(RepositoryEntryStatusEnum.published);
	}
	
	public CoursePageFragment changeStatus(RepositoryEntryStatusEnum status) {
		By statusMenuBy = By.cssSelector("ul.o_repo_tools_status");
		if(!browser.findElement(statusMenuBy).isDisplayed()) {
			By statusMenuCaret = By.cssSelector("a.o_repo_tools_status");
			browser.findElement(statusMenuCaret).click();
			OOGraphene.waitElement(statusMenuBy, browser);
		}
		
		By statusBy = By.cssSelector("ul.o_repo_tools_status>li>a.o_repo_status_" + status.name());
		browser.findElement(statusBy).click();
		OOGraphene.waitBusy(browser);

		return assertStatus(status);
	}
	public CoursePageFragment assertStatus(RepositoryEntryStatusEnum status) {
		By statusViewBy = By.xpath("//li[contains(@class,'o_tool_dropdown')]/a[contains(@class,'o_repo_tools_status')]/span[contains(@class,'o_repo_status_" + status + "')]");
		OOGraphene.waitElement(statusViewBy, browser);
		return this;
	}
	
	public CoursePageFragment switchRole(Role role) {
		String cssRole;
		switch(role) {
			case participant: cssRole = "o_icon_user"; break;
			case coach: cssRole = "o_icon_coach"; break;
			case owner: cssRole = "o_icon_owner"; break;
			default: cssRole = "o_icon_unkown"; break;
		}
		
		By statusMenuBy = By.cssSelector("ul.o_sel_switch_role");
		if(!browser.findElement(statusMenuBy).isDisplayed()) {
			By statusMenuCaret = By.cssSelector("a.o_sel_switch_role");
			browser.findElement(statusMenuCaret).click();
			OOGraphene.waitElement(statusMenuBy, browser);
		}
		
		By statusBy = By.cssSelector("ul.o_sel_switch_role>li>a>i." + cssRole);
		browser.findElement(statusBy).click();
		OOGraphene.waitBusy(browser);
		
		By statusViewBy = By.xpath("//li[contains(@class,'o_tool_dropdown')]/a[contains(@class,'o_sel_switch_role')]/span/i[contains(@class,'" + cssRole + "')]");
		OOGraphene.waitElement(statusViewBy, browser);
		return this;
	}
	
	
	/**
	 * Close the course
	 */
	public CoursePageFragment closeCourse() {
		By closeBy = By.cssSelector("ol.breadcrumb li.o_breadcrumb_close a");
		OOGraphene.waitElement(closeBy, browser);
		browser.findElement(closeBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
}
