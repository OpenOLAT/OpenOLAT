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
	 * @param authorizedAbsence Enable or not the option "Authorized absences"
	 * @param teacherCanAutorizeAbsence Enable or not the option "Coaches can authorize absences"
	 * @return
	 */
	public LectureAdminSettingsPage configure(boolean authorizedAbsence, boolean teacherCanAutorizeAbsence) {
		setAuthorizedAbsence(authorizedAbsence);
		if(authorizedAbsence) {
			setTeacherCanAuthorizeAbsence(teacherCanAutorizeAbsence);
		}
		return this;
	}
	
	/**
	 * Set the option "Coaches can authorize absences". Only available
	 * with the authorized absences enabled.
	 * 
	 * @param teacherCanAuthorizeAbsence Enable/disable the hability for coaches to excuse absences
	 * @return Itself
	 */
	public LectureAdminSettingsPage setTeacherCanAuthorizeAbsence(boolean teacherCanAuthorizeAbsence) {	
		String checkName = "lecture.teacher.can.authorize.absence";
		return set(checkName, teacherCanAuthorizeAbsence);
	}
	
	/**
	 * Set the option "Authorized absences"
	 * 
	 * @param authorizedAbsence Enable/disable authorized absences
	 * @return Itself
	 */
	public LectureAdminSettingsPage setAuthorizedAbsence(boolean authorizedAbsence) {		
		String checkName = "lecture.authorized.absence.enabled";
		return set(checkName, authorizedAbsence);
	}
	
	private LectureAdminSettingsPage set(String name, boolean enable) {
		By enableLabelBy = By.xpath("//label[input[@name='" + name + "' and @value='on']]");
		By enableCheckBy = By.xpath("//label/input[@name='" + name + "' and @value='on']");
		
		OOGraphene.waitElement(enableLabelBy, browser);
		OOGraphene.scrollTo(enableLabelBy, browser);
		
		WebElement enableLabelEl = browser.findElement(enableLabelBy);
		WebElement enableCheckEl = browser.findElement(enableCheckBy);
		OOGraphene.check(enableLabelEl, enableCheckEl, new Boolean(enable));
		OOGraphene.waitBusy(browser);
		return this;
		
	}
	
	public LectureAdminSettingsPage save() {
		By saveBy = By.cssSelector("div.o_sel_lecture_save_settings button.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}

}
