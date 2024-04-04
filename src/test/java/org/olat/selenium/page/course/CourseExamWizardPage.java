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

import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/**
 * 
 * Initial date: 4 mai 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseExamWizardPage {
	
	private final WebDriver browser;
	
	public CourseExamWizardPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public static CourseExamWizardPage getWizard(WebDriver browser) {
		return new CourseExamWizardPage(browser);
	}
	
	public CourseExamWizardPage setExamConfiguration(boolean disclaimer, boolean reTest, boolean certificate) {
		if(disclaimer) {
			By disclaimerBy = By.cssSelector(".modal fieldset.o_sel_course_wizard_exam_config input[name='exam.config'][value='disclaimer']");
			browser.findElement(disclaimerBy).click();
		}
		if(reTest) {
			By reTestBy = By.cssSelector(".modal fieldset.o_sel_course_wizard_exam_config input[name='exam.config'][value='retest']");
			browser.findElement(reTestBy).click();
		}
		if(certificate) {
			By certificateBy = By.cssSelector(".modal fieldset.o_sel_course_wizard_exam_config input[name='exam.config'][value='cert']");
			browser.findElement(certificateBy).click();
		}
		return this;
	}
	
	public CourseExamWizardPage setExamMembersConfiguration(boolean coaches, boolean participants) {
		if(coaches) {
			By coachesBy = By.cssSelector(".modal fieldset.o_sel_course_wizard_members input[name='exam.members'][value='coach']");
			browser.findElement(coachesBy).click();
		}
		if(participants) {
			By participantsBy = By.cssSelector(".modal fieldset.o_sel_course_wizard_members input[name='exam.members'][value='paeticipants']");
			browser.findElement(participantsBy).click();
		}
		return this;
	}
	
	/**
	 * Click next on the step with metadata.
	 * 
	 * @return Itself
	 */
	public CourseExamWizardPage nextInfosMetadata() {
		OOGraphene.nextStep(browser);
		OOGraphene.waitElement(By.cssSelector("fieldset.o_sel_course_metadata"), browser);
		return this;
	}
	
	public CourseExamWizardPage nextDisclaimers() {
		OOGraphene.nextStep(browser);
		OOGraphene.waitElement(By.cssSelector("div.o_sel_course_disclaimer_settings"), browser);
		return this;
	}
	
	public CourseExamWizardPage nextTest() {
		OOGraphene.nextStep(browser);
		OOGraphene.waitElement(By.cssSelector("div.o_sel_search_referenceable_entries"), browser);
		return this;
	}
	
	public CourseExamWizardPage selectTest(String testTitle, String courseElementTitle) {
		By rowBy = By.xpath("//div[contains(@class,'')]//div[contains(@class,'o_segments_content')]//table[contains(@class,'o_table')]//tr/td/a[text()[contains(.,'" + testTitle + "')]]");
		OOGraphene.waitElement(rowBy, browser);
		browser.findElement(rowBy).click();
		// Wait configuration
		By infosBy = By.xpath("//dialog[contains(@class,'modal')]//div[contains(@class,'o_cnd_document')]/div/h4[text()[contains(.,'" + testTitle + "')]]");
		OOGraphene.waitElement(infosBy, browser);
		
		By courseElementTitleBy = By.cssSelector(".modal div.o_sel_course_wizard_element_title input[type='text']");
		WebElement courseElementTitleEl = browser.findElement(courseElementTitleBy);
		courseElementTitleEl.clear();
		courseElementTitleEl.sendKeys(courseElementTitle);
		return this;
	}
	
	public CourseExamWizardPage nextTestConfiguration() {
		OOGraphene.nextStep(browser);
		OOGraphene.waitElement(By.cssSelector("div.o_qti_21_datetest"), browser);
		return this;
	}
	
	public CourseExamWizardPage disableTestDate() {
		OOGraphene.toggle(".modal div.o_qti_21_datetest button.o_qti_21_datetest", false, false, browser);
		
		By dateStartBy = By.cssSelector(".modal div.o_qti_21_datetest_start input.o_date_day[type='text']");
		OOGraphene.waitElementAbsence(dateStartBy, 5, browser);
		return this;
	}
	
	public CourseExamWizardPage nextCertificates() {
		OOGraphene.nextStep(browser);
		OOGraphene.waitElement(By.cssSelector("fieldset.o_sel_certificates_options"), browser);
		return this;
	}
	
	public CourseExamWizardPage nextSearchUsers() {
		OOGraphene.nextStep(browser);
		OOGraphene.waitElement(By.cssSelector("div.o_sel_user_import_by_username"), browser);
		return this;
	}
	
	public CourseExamWizardPage importMembers(UserVO... users) {
		StringBuilder sb = new StringBuilder();
		for(UserVO user:users) {
			if(sb.length() > 0) sb.append("\\n");
			sb.append(user.getLogin());
		}
		By importAreaBy = By.cssSelector(".modal div.o_sel_user_import textarea");
		WebElement importAreaEl = browser.findElement(importAreaBy);
		OOGraphene.textarea(importAreaEl, sb.toString(), browser);
		return this;
	}
	
	public CourseExamWizardPage nextUsersOverview() {
		OOGraphene.nextStep(browser);
		OOGraphene.waitElement(By.cssSelector("fieldset.o_sel_user_import_overview"), browser);
		return this;
	}
	
	public CourseExamWizardPage assertOnOverview(UserVO user) {
		By userBy = By.xpath("//dialog[contains(@class,'modal')]//div[contains(@class,'o_table_wrapper')]//td[text()[contains(.,'" + user.getFirstName() + "')]]");
		OOGraphene.waitElement(userBy, browser);
		return this;
	}
	
	public CourseExamWizardPage nextPublication() {
		OOGraphene.nextStep(browser);
		OOGraphene.waitElement(By.cssSelector("fieldset.o_sel_course_wizard_publication"), browser);
		return this;
	}
	
	public CourseExamWizardPage publish() {
		By publishBy = By.cssSelector(".modal fieldset.o_sel_course_wizard_publication select[name='publishedStatus_SELBOX']");
		WebElement publishEl = browser.findElement(publishBy);
		new Select(publishEl).selectByValue(RepositoryEntryStatusEnum.published.name());
		return this;
	}
	
	public void finish() {
		OOGraphene.finishStep(browser, false);
	}
	

}
