/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.selenium.page.course;

import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/**
 * 
 * Initial date: 6 sept. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TBrokerCoachPage {

	private final WebDriver browser;
	
	public TBrokerCoachPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public TBrokerCoachPage assertOnParticipant(UserVO participant) {
		By nameBy = By.xpath("//table[contains(@class,'table')]//tr/td/a[text()[contains(.,'" + participant.getFirstName() + "')]]");
		OOGraphene.waitElement(nameBy, browser);
		return this;
	}

	public TBrokerCoachPage expandParticipantDetails(UserVO participant) {
		By detailsBy = By.xpath("//table[contains(@class,'table')]//tr[td/a[text()[contains(.,'" + participant.getFirstName() + "')]]]/td/div/a[i[contains(@class,'o_icon_table_details_expand')]]");
		OOGraphene.waitElement(detailsBy, browser);
		browser.findElement(detailsBy).click();
		By participantDetailsBy = By.xpath("//div[contains(@class,'o_tb_participant_selections')]//div[contains(@class,'o_user_info')]//div[contains(@class,'o_user_info_profile_name')][text()[contains(.,'" + participant.getFirstName() + "')]]");
		OOGraphene.waitElement(participantDetailsBy, browser);
		return this;
	}
	
	public TBrokerCoachPage collapseParticipantDetails(UserVO participant) {
		By detailsBy = By.xpath("//table[contains(@class,'table')]//tr[td/a[text()[contains(.,'" + participant.getFirstName() + "')]]]/td/div/a[i[contains(@class,'o_icon_table_details_collaps')]]");
		OOGraphene.waitElement(detailsBy, browser);
		browser.findElement(detailsBy).click();
		By participantDetailsBy = By.cssSelector("table.table div.o_tb_participant_selections");
		OOGraphene.waitElementDisappears(participantDetailsBy, 5, browser);
		return this;
	}
	
	public TBrokerCoachPage selectTopic(String topic) {
		By selectTopicBy = By.cssSelector("table.table div.o_tb_participant_selections a.o_sel_tb_select_topic");
		OOGraphene.waitElement(selectTopicBy, browser);
		OOGraphene.click(selectTopicBy, browser);
		OOGraphene.waitModalDialog(browser);
		
		By selectBy = By.cssSelector("dialog fieldset.o_sel_tb_select_topic_form .o_sel_tb_topic select");
		WebElement selectEl = browser.findElement(selectBy);
		new Select(selectEl).selectByVisibleText(topic);
		
		By enrollBy = By.cssSelector("dialog fieldset.o_sel_tb_select_topic_form .o_sel_tb_enroll input[type='checkbox'][name='selection.enroll'][value='enroll']");
		WebElement enrollEl = browser.findElement(enrollBy);
		OOGraphene.check(enrollEl, Boolean.TRUE);
		
		By saveBy = By.cssSelector("dialog.dialog .o_sel_tb_select_topic_form button.btn.btn-primary");
		OOGraphene.click(saveBy, browser);
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	
	
}
