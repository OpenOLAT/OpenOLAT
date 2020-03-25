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

import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.selenium.page.lecture.LectureRepositorySettingsPage;
import org.olat.selenium.page.repository.RepositorySettingsPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 6 Nov 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseSettingsPage extends RepositorySettingsPage {
	
	public CourseSettingsPage(WebDriver browser) {
		super(browser);
	}
	
	public CourseToolbarSettingsPage toolbar() {
		By toolbarSegmentBy = By.cssSelector("ul.o_tools_segments a.o_sel_toolbar");
		OOGraphene.waitElement(toolbarSegmentBy, browser);
		browser.findElement(toolbarSegmentBy).click();
		
		By toolbarSettingsBy = By.cssSelector("fieldset.o_sel_toolbar_settings");
		OOGraphene.waitElement(toolbarSettingsBy, browser);

		return new CourseToolbarSettingsPage(browser);
	}
	
	public EfficiencyStatementConfigurationPage efficiencyStatementConfiguration() {
		By certificateSegmentBy = By.cssSelector("ul.o_tools_segments a.o_sel_assessment");
		OOGraphene.waitElement(certificateSegmentBy, browser);
		browser.findElement(certificateSegmentBy).click();
		
		By toolbarSettingsBy = By.cssSelector("fieldset.o_sel_course_efficiency_statements");
		OOGraphene.waitElement(toolbarSettingsBy, browser);

		return new EfficiencyStatementConfigurationPage(browser);
	}
	
	public LectureRepositorySettingsPage lecturesConfiguration() {
		By certificateSegmentBy = By.cssSelector("ul.o_tools_segments a.o_sel_execution");
		OOGraphene.waitElement(certificateSegmentBy, browser);
		browser.findElement(certificateSegmentBy).click();
		
		By toolbarSettingsBy = By.cssSelector("fieldset.o_sel_repo_lecture_settings_form");
		OOGraphene.waitElement(toolbarSettingsBy, browser);
		
		return new LectureRepositorySettingsPage(browser);
	}
	
	public CoursePageFragment clickToolbarBack() {
		OOGraphene.clickBreadcrumbBack(browser);
		return CoursePageFragment.getCourse(browser);
	}
}
