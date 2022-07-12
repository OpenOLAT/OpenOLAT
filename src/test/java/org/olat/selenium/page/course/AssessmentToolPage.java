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

import java.time.Duration;
import java.util.List;

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

	private final WebDriver browser;
	
	public AssessmentToolPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public AssessmentToolPage users() {
		By usersBy = By.cssSelector("a.o_sel_assessment_tool_assessed_users");
		browser.findElement(usersBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public AssessmentToolPage assertOnUsers(UserVO user) {
		By usersCellsBy = By.cssSelector("div.o_table_flexi table tr td.text-left");
		List<WebElement> usersCellsList = browser.findElements(usersCellsBy);
		Assert.assertFalse(usersCellsList.isEmpty());
		
		boolean found = false;
		for(WebElement usersCell:usersCellsList) {
			found |= usersCell.getText().contains(user.getFirstName());
		}
		Assert.assertTrue(found);
		return this;
	}
	
	/**
	 * Select a user in "Users".
	 * 
	 * @param user
	 * @return
	 */
	public AssessmentToolPage selectUser(UserVO user) {
		By userLinksBy = By.xpath("//div[contains(@class,'o_table_flexi')]//table//tr//td//a[text()[contains(.,'" + user.getFirstName() + "')]]");
		browser.findElement(userLinksBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * To see the list of course elements
	 * @return Itself
	 */
	public AssessmentToolPage courseElements() {
		By elementsBy = By.cssSelector("a.o_sel_assessment_tool_assessable_course_nodes");
		browser.findElement(elementsBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Select the course node in "Users" > "Course nodes".
	 * 
	 * @param nodeTitle The title of the course node
	 * @return Itself
	 */
	public AssessmentToolPage selectUsersCourseNode(String nodeTitle) {
		By rowsBy = By.xpath("//div[contains(@class,'o_table_wrapper')]//table//tr[td/span[contains(text(),'" + nodeTitle + "')]]/td/a[contains(@onclick,'cmd.select.node')]");
		OOGraphene.waitElement(rowsBy, browser);
		List<WebElement> rowEls = browser.findElements(rowsBy);
		Assert.assertEquals(1, rowEls.size());
		OOGraphene.scrollTo(rowsBy, browser);
		browser.findElement(rowsBy).click();
		OOGraphene.waitElement(By.cssSelector("div.o_assessment_panel"), browser);
		return this;
	}
	
	/**
	 * Select the course node in the tree > "Course nodes".
	 * 
	 * @param nodeTitle The title of the course node
	 * @return Itself
	 */
	public AssessmentToolPage selectElementsCourseNode(String nodeTitle) {
		By elementBy = By.xpath("//div[contains(@class,'o_tree')]//ul//li[div/span/a/span[@class='o_tree_item'][contains(text(),'" + nodeTitle + "')]]/div/span/a[contains(@onclick,'nidle')]");
		OOGraphene.waitElement(elementBy, browser);
		browser.findElement(elementBy).click();
		By statsBy = By.cssSelector("div.panel.o_assessment_stats");
		OOGraphene.waitElement(statsBy, browser);
		return this;
	}
	
	/**
	 * Select the list of identities to assess.
	 * 
	 * @return Itself
	 */
	public AssessmentToolPage selectIdentitiesList() {
		By identitiesListSegmentBy = By.cssSelector("div.o_segments a.btn.o_sel_assessment_tool_node_participants");
		OOGraphene.waitElement(identitiesListSegmentBy, browser);
		browser.findElement(identitiesListSegmentBy).click();
		By identitiesListBy = By.cssSelector("div.o_table_flexi.o_sel_assessment_tool_table");
		OOGraphene.waitElement(identitiesListBy, browser);
		return this;
	}
	
	/**
	 * Check in "Users" > "Course nodes" if a specific course node
	 * is passed.
	 * 
	 * @param nodeTitle
	 * @return
	 */
	public AssessmentToolPage assertUserPassedCourseNode(String nodeTitle) {
		By rowsBy = By.xpath("//div[contains(@class,'o_table_wrapper')]//table//tr[td/span[contains(text(),'" + nodeTitle + "')]]");
		List<WebElement> rowEls = browser.findElements(rowsBy);
		Assert.assertEquals(1, rowEls.size());
		By passedBy = By.cssSelector("td div.o_state.o_passed");
		WebElement passedEl = rowEls.get(0).findElement(passedBy);
		Assert.assertTrue(passedEl.isDisplayed());
		return this;
	}
	
	/**
	 * Set the score in the assessment form
	 * @param score
	 * @return
	 */
	public AssessmentToolPage setAssessmentScore(float score) {
		By scoreBy = By.xpath("//input[contains(@class,'o_sel_assessment_form_score')][@type='text']");
		browser.findElement(scoreBy).sendKeys(Float.toString(score));
		return this;
	}
	
	public AssessmentToolPage setAssessmentPassed(Boolean passed) {
		String val = passed == null ? "undefined" : passed.toString();
		By passedBy = By.cssSelector(".o_sel_assessment_form_passed input[type='radio'][value='" + val + "']");
		browser.findElement(passedBy).click();
		return this;
	}
	
	public AssessmentToolPage closeAndPublishAssessment() {
		By saveBy = By.cssSelector("button.btn.o_sel_assessment_form_save_and_done");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public AssessmentToolPage reopenAssessment() {
		By saveBy = By.cssSelector("a.o_sel_assessment_form_reopen");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		By assessmentPanelBy = By.cssSelector("div.o_personal.o_assessment_panel");
		OOGraphene.waitElement(assessmentPanelBy, browser);
		return this;
	}
	
	public AssessmentToolPage assertPassed(UserVO user) {
		By userInfosBy = By.xpath("//div[@class='o_user_infos']/div[@class='o_user_infos_inner']//tr[contains(@class,'o_userDisplayName')]/td[text()[contains(.,'" + user.getFirstName() + "')]]");
		OOGraphene.waitElement(userInfosBy, browser);
		
		By passedBy = By.cssSelector("div.o_table_wrapper table tr td.text-left div.o_state.o_passed");
		OOGraphene.waitElement(passedBy, browser);
		return this;
	}
	
	/**
	 * 
	 * @param user The user to overview
	 * @param progress The progress in percent
	 * @return Itself
	 */
	public AssessmentToolPage assertProgress(UserVO user, int progress) {
		By progressBy = By.xpath("//div[contains(@class,'o_table_wrapper')]//table//tr[td/a[contains(.,'" + user.getFirstName() + "')]]/td/div[@class='progress'][div[@title='" + progress + "%']]");
		OOGraphene.waitElement(progressBy, Duration.ofSeconds(15), Duration.ofSeconds(1), browser);
		return this;
	}
	
	/**
	 * Wait slowly that the user has passed the test.
	 * 
	 * @param user The assessed user
	 * @return Itself
	 */
	public AssessmentToolPage assertTablePassed(UserVO user) {
		By doneBy = By.xpath("//div[contains(@class,'o_table_wrapper')]//table//tr[td/a[contains(.,'" + user.getFirstName() + "')]]/td/div/i[contains(@class,'o_icon_passed')]");
		OOGraphene.waitElementSlowly(doneBy, 10, browser);
		return this;
	}
	
	/**
	 * The status done is off screen. It is important to wait first the passed status and
	 * after call this assert. The assert will move the window right to see and wait the done
	 * status.
	 * 
	 * @param user The assessed user
	 * @return Itself
	 */
	public AssessmentToolPage assertTableStatusDone(UserVO user) {
		try {
			OOGraphene.moveTo(By.xpath("//div[contains(@class,'o_table_wrapper')]//table//tr/td[count(../td)-1]"), browser);
			By doneBy = By.xpath("//div[contains(@class,'o_table_wrapper')]//table//tr[td/a[contains(.,'" + user.getFirstName() + "')]]/td/div/i[contains(@class,'o_icon_status_done')]");
			OOGraphene.waitElementPresenceSlowly(doneBy, 10, browser);
		} catch (Exception e) {
			OOGraphene.takeScreenshot("Status done", browser);
			throw e;
		}
		return this;
	}
	
	public AssessmentToolPage assertProgressEnded(UserVO user) {
		By progressBy = By.xpath("//div[contains(@class,'o_table_wrapper')]//table//tr[td/a[contains(.,'" + user.getFirstName() + "')]]/td/div[@class='o_sel_ended']");
		OOGraphene.waitElement(progressBy, 10, browser);
		return this;
	}
	
	public AssessmentToolPage generateCertificate() {
		By userLinksBy = By.className("o_sel_certificate_generate");
		OOGraphene.waitElement(userLinksBy, browser);
		browser.findElement(userLinksBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitAndCloseBlueMessageWindow(browser);

		By certificateBy = By.cssSelector("ul.o_certificates a>i.o_icon.o_filetype_pdf");
		OOGraphene.waitElementSlowly(certificateBy, 15, browser);
		return this;
	}
	
	public BulkAssessmentPage bulk() {
		By bulkBy = By.cssSelector("li.o_tool a.o_sel_assessment_tool_bulk");
		browser.findElement(bulkBy).click();
		OOGraphene.waitBusy(browser);
		
		By newBy = By.cssSelector("a.o_sel_assessment_tool_new_bulk_assessment");
		browser.findElement(newBy).click();
		OOGraphene.waitModalDialog(browser);
		OOGraphene.waitElement(By.cssSelector("fieldset.o_sel_bulk_assessment_data"), browser);
		return new BulkAssessmentPage(browser);
	}
	
	public AssessmentToolPage makeAllVisible() {
		OOGraphene.flexiTableSelectAll(browser);
		
		By bulkBy = By.cssSelector("a.btn.o_sel_assessment_bulk_visible");
		OOGraphene.waitElement(bulkBy, browser);
		browser.findElement(bulkBy).click();
		OOGraphene.waitBusy(browser);
		
		By visibleBy = By.xpath("//table//span[i[contains(@class,'o_icon_results_visible')]]");
		OOGraphene.waitElement(visibleBy, browser);
		return this;
	}
	
	/**
	 * Click back to the course
	 * 
	 * @return
	 */
	public CoursePageFragment clickToolbarRootCrumb() {
		By toolbarBackBy = By.xpath("//li[contains(@class,'o_breadcrumb_back')]/following-sibling::li/a");
		browser.findElement(toolbarBackBy).click();
		OOGraphene.waitBusy(browser);
		return new CoursePageFragment(browser);
	}
}