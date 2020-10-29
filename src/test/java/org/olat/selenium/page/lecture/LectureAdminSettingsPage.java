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

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 31 oct. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureAdminSettingsPage {
	
	private WebDriver browser;
	
	public LectureAdminSettingsPage(WebDriver browser) {
		this.browser = browser;
	}
	
	/**
	 * Set of options.
	 * 
	 * @param holdPartialLectures Enable or not the option "Allow holding partial lectures"
	 * @param cancelStatus Enable or not the option "Lectures status" "Cancelled"
	 * @param authorizedAbsence Enable or not the option "Authorized absences"
	 * @param defaultAuthorized Enable or not the option "Absence per default authorized"
	 * @param teacherCanAutorizeAbsence Enable or not the option "Coaches can authorize absences"
	 * @return
	 */
	public LectureAdminSettingsPage configure(boolean holdPartialLectures, boolean cancelStatus,
			boolean authorizedAbsence, boolean defaultAuthorized) {
		setHoldPartialLectures(holdPartialLectures);
		setCancelStatus(cancelStatus);
		setAuthorizedAbsence(authorizedAbsence);
		if(authorizedAbsence) {
			setDefaultAuthorisedAbsence(defaultAuthorized);
		}
		return this;
	}
	
	public LectureAdminSettingsPage configurePermissions(boolean teacherCanAutorizeAbsence) {
		By permissionsBy = By.cssSelector("div.o_segments a.btn.o_sel_lectures_admin_permissions");
		OOGraphene.waitElement(permissionsBy, browser);
		browser.findElement(permissionsBy).click();
		OOGraphene.waitBusy(browser);
		By paneBy = By.className("o_sel_lecture_permissions");
		OOGraphene.waitElement(paneBy, browser);
		
		setTeacherCanAuthorizeAbsence(teacherCanAutorizeAbsence);
		return this;
	}
	
	/**
	 * Set the option "Allow holding partial lectures".
	 * 
	 * @param enableHoldingPartialLectures Enable (or not if false) partial lectures.
	 * @return Itself
	 */
	public LectureAdminSettingsPage setHoldPartialLectures(boolean enableHoldingPartialLectures) {
		String checkName = "lecture.status.partially.done.enabled";
		return set(checkName, "on", enableHoldingPartialLectures);	
	}
	
	/**
	 * Set the option "Cancelled" for "Lectures status".
	 * 
	 * @param enabledCancelStatus Enable (or not if false) the status cancelled for lectures.
	 * @return Itself
	 */
	public LectureAdminSettingsPage setCancelStatus(boolean enabledCancelStatus) {
		String checkName = "lecture.status.enabled";
		return set(checkName, "cancelled", enabledCancelStatus);	
	}
	
	/**
	 * Set the option "Authorized absences"
	 * 
	 * @param authorizedAbsence Enable/disable authorized absences
	 * @return Itself
	 */
	private LectureAdminSettingsPage setAuthorizedAbsence(boolean authorizedAbsence) {		
		String checkName = "lecture.authorized.absence.enabled";
		return set(checkName, "on", authorizedAbsence);
	}
	
	/**
	 * Set the option "Coaches can authorize absences". Only available
	 * with the authorized absences enabled.
	 * 
	 * @param teacherCanAuthorizeAbsence Enable/disable the hability for coaches to excuse absences
	 * @return Itself
	 */
	private LectureAdminSettingsPage setTeacherCanAuthorizeAbsence(boolean teacherCanAuthorizeAbsence) {	
		String checkName = "lecture.teacher.can.authorize.absence";
		return set(checkName, "on", teacherCanAuthorizeAbsence);
	}
	
	/**
	 * Set the option "Absence per default authorized". Only available
	 * with the authorized absences enabled.
	 * 
	 * @param defaultAuthorized The absence are per default authorized (or not if false). 
	 * @return Itself
	 */
	public LectureAdminSettingsPage setDefaultAuthorisedAbsence(boolean defaultAuthorized) {
		String checkName = "lecture.absence.default.authorized";
		return set(checkName, "on", defaultAuthorized);	
	}
	
	private LectureAdminSettingsPage set(String name, String value, boolean enable) {
		By enableCheckBy = By.xpath("//label/input[@name='" + name + "' and @value='" + value + "']");
		
		OOGraphene.waitElement(enableCheckBy, browser);
		OOGraphene.scrollTo(enableCheckBy, browser);
		
		WebElement enableCheckEl = browser.findElement(enableCheckBy);
		OOGraphene.check(enableCheckEl, Boolean.valueOf(enable));
		OOGraphene.waitBusy(browser);
		return this;
		
	}
	
	public LectureAdminSettingsPage save() {
		By saveBy = By.cssSelector("div.o_sel_lecture_save_settings button.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public LectureAdminSettingsPage savePermissions() {
		By saveBy = By.cssSelector("div.o_sel_lecture_save_permissions button.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
}
