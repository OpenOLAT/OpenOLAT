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
	
	public LectureRepositoryParticipantsPage assertOnParticipantLectureBlockAbsent(UserVO participant, int absence) {
		By blocks = By.xpath("//div[contains(@class,'o_sel_lecture_participants_overview')]//table//tr[td[contains(text(),'" + participant.getFirstName() + "')]][td[contains(text(),'" + absence + "')]]");
		OOGraphene.waitElement(blocks, browser);
		return this;
	}
}
