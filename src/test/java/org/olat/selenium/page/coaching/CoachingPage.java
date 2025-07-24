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
package org.olat.selenium.page.coaching;

import org.olat.core.util.Formatter;
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.selenium.page.qti.QTI21CorrectionPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 11 juin 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CoachingPage {
	
	private final WebDriver browser;
	
	public CoachingPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public CoachingPage assertOnOverview() {
		By coachingBy = By.className("o_coaching_overview");
		OOGraphene.waitElement(coachingBy, browser);
		return this;
	}
	
	public CoachingPage openAssessment() {
		By assessmentBy = By.cssSelector(".o_coaching_overview a.o_button_mega.o_sel_coaching_orders");
		OOGraphene.waitElement(assessmentBy, browser);
		browser.findElement(assessmentBy).click();
		return this;
	}
	
	public CoachingPage assertOnGrading() {
		By gradingBy = By.cssSelector("fieldset.o_sel_grading_assignments");
		OOGraphene.waitElement(gradingBy, browser);
		return this;
	}
	
	public QTI21CorrectionPage startGrading(String nodeTitle) {
		By gradingBy = By.xpath("//fieldset[@class='o_sel_grading_assignments']//table//tr[td/a[text()[contains(.,'" + nodeTitle + "')]]]/td/a[contains(@onclick,'grade')]");
		OOGraphene.waitElement(gradingBy, browser).click();
		OOGraphene.waitBusy(browser);
		return new QTI21CorrectionPage(browser);
	}
	
	public CoachingPage openCourses() {
		By assessmentBy = By.cssSelector(".o_coaching_overview a.o_button_mega.o_sel_coaching_courses");
		OOGraphene.waitElement(assessmentBy, browser).click();
		OOGraphene.waitElement(By.className("o_coaching_course_list"), browser);
		return this;
	}
	
	public CoachingPage filterAllCourses() {
		By allBy = By.xpath("//div[@class='o_coaching_course_list']//div[@class='o_table_tabs']//a[contains(@href,'tab')][contains(@href,'All')]");
		OOGraphene.waitElement(allBy, browser).click();
		By allActiveBy = By.xpath("//div[@class='o_coaching_course_list']//div[@class='o_table_tabs']//a[contains(@class,'btn-primary')][contains(@href,'tab')][contains(@href,'All')]");
		OOGraphene.waitElement(allActiveBy, browser);
		return this;
	}

	public void openCourse(String title) {
		title = Formatter.truncateOnly(title, 55);
		By titleBy = By.xpath("//h4[contains(@class,'o_title')]/a[span[text()[contains(.,'" + title + "')]]]");
		OOGraphene.waitElement(titleBy, browser).click();
		OOGraphene.waitBusy(browser);
	}

}
