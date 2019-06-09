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
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 7 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureRepositoryParticipantsPage {
	
	private WebDriver browser;
	
	public LectureRepositoryParticipantsPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public LectureRepositoryParticipantsPage assertOnParticipantLectureBlocks() {
		By blocks = By.cssSelector("div.o_sel_repo_lectures_admin table");
		OOGraphene.waitElement(blocks, browser);
		return this;
	}
	
	/**
	 * Edit the participant rate and first admission.
	 * 
	 * @param participant The participant to edit
	 * @return Itself
	 */
	public LectureRepositoryParticipantsPage editParticipant(UserVO participant) {
		By editBy = By.xpath("//div[contains(@class,'o_sel_lecture_participants_overview')]//table//tr[td[contains(text(),'" + participant.getFirstName() + "')]]/td/a[contains(@href,'edit')]");
		OOGraphene.waitElement(editBy, browser);
		browser.findElement(editBy).click();
		OOGraphene.waitModalDialog(browser);
		return this;
	}
	
	/**
	 * Set the first admission a month before the 12th.
	 * 
	 * @return Itself
	 */
	public LectureRepositoryParticipantsPage firstAdmissionBack() {
		//open the date picker
		By firstAdmissionBy = By.cssSelector("fieldset.o_sel_lecture_participant_summary_form div.o_sel_lecture_first_admission span.input-group-addon i");
		OOGraphene.waitElement(firstAdmissionBy, browser);
		browser.findElement(firstAdmissionBy).click();

		// a month before
		By monthBeforeBy = By.cssSelector("a.ui-datepicker-prev.ui-corner-all");
		OOGraphene.waitElement(monthBeforeBy, browser);
		browser.findElement(monthBeforeBy).click();
		
		// select the 12
		By dayBy = By.xpath("//div[@id='ui-datepicker-div']//td//a[normalize-space(text())='12']");
		OOGraphene.waitElement(dayBy, browser);
		browser.findElement(dayBy).click();

		//wait until
		By datePickerBy = By.id("ui-datepicker-div");
		OOGraphene.waitElementUntilNotVisible(datePickerBy, 5, browser);
		return this;
	}
	
	/**
	 * Save the modal dialog to edit the participant rate and
	 * first admission.
	 * 
	 * @return Itself
	 */
	public LectureRepositoryParticipantsPage saveParticipant() {
		By saveBy = By.cssSelector("fieldset.o_sel_lecture_participant_summary_form button.btn.btn-primary");
		OOGraphene.waitElement(saveBy, browser);
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public LectureRepositoryParticipantsPage assertOnParticipantLectureBlockAbsent(UserVO participant, int absence) {
		By blocks = By.xpath("//div[contains(@class,'o_sel_lecture_participants_overview')]//table//tr[td[contains(text(),'" + participant.getFirstName() + "')]]/td/span[contains(@class,'o_sel_absences')][contains(text(),'" + absence + "')]");
		OOGraphene.waitElement(blocks, browser);
		return this;
	}
}
