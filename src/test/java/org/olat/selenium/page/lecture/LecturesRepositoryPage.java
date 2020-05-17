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
package org.olat.selenium.page.lecture;

import org.olat.selenium.page.course.CoursePageFragment;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * The tool in course for teachers.
 * 
 * Initial date: 7 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LecturesRepositoryPage {
	
	private final WebDriver browser;
	
	public LecturesRepositoryPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public TeacherRollCallPage openRollCall(String lectureBlockTitle) {
		By selectBy = By.xpath("//table//tr[td[contains(text(),'" + lectureBlockTitle + "')]]/td/a[contains(@onclick,'details')]");
		browser.findElement(selectBy).click();
		return new TeacherRollCallPage(browser)
				.assertOnRollCall();
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
