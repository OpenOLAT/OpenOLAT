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

/**
 * 
 * Initial date: 6 sept. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TBrokerPage {

	private final WebDriver browser;
	
	public TBrokerPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public TBrokerPage assertOnTopicTitle(String title) {
		By topicBy = By.xpath("//div[contains(@class,'o_tb_topic_table')]//div[contains(@class,'o_tb_topic_card')]//div[@class='o_tb_topic_card_meta']//h4[text()[contains(.,'" + title + "')]]");
		OOGraphene.waitElement(topicBy, browser);
		return this;
	}
	
	public TBrokerPage assertOnSelectedTopicInTable(String title) {
		By selectedBy = By.xpath("//table[contains(@class,'table')]//tr[td/a[text()[contains(.,'" + title + "')]]]/td/div/span[contains(@class,'o_tb_label_light_selected')]");
		OOGraphene.waitElement(selectedBy, browser);
		return this;
	}
	
	public TBrokerPage assertOnSelectedTopicAsCard(String title) {
		By selectedBy = By.xpath("//div[contains(@class,'o_tb_topic_card')][div/div/div/h4[contains(@class,'o_tb_topic_card_title')][text()[contains(.,'" + title + "')]]]/div[contains(@class,'o_tb_topic_card_thumbnail')]//span[contains(@class,'o_tb_label_light_selected')]");
		OOGraphene.waitElement(selectedBy, browser);
		return this;
	}
	
	public TBrokerPage assertEnrolledByUser(UserVO user, int priority) {
		By enrolledBy = By.xpath("//table[contains(@class,'table')]//tr[td/a[text()[contains(.,'" + user.getFirstName() + "')]]]/td/div[@class='o_tb_priority_labels']/div[@class='o_tb_priority_label']/div[@class='o_tb_priority_enrolled'][text()[contains(.,'" + priority + "')]]");
		OOGraphene.waitElement(enrolledBy, browser);
		return this;
	}
	
	public TBrokerPage assertEnrolledByTopic(String title, int priority) {
		By enrolledBy = By.xpath("//table[contains(@class,'table')]//tr[td/a[text()[contains(.,'" + title + "')]]][td/div[@class='o_tb_priority_labels']/div[@class='o_tb_priority_label']/div[@class='o_tb_priority_enrolled'][text()[contains(.,'" + priority + "')]]]/td/div/span[contains(@class,'o_tb_label_light_enrolled')]");
		OOGraphene.waitElement(enrolledBy, browser);
		return this;
	}
	
	/**
	 * The select needs an assert after.
	 * 
	 * @param title The topic to select
	 * @return Itself
	 */
	public TBrokerPage selectTopic(String title) {
		By selectBy = By.xpath("//div[contains(@class,'o_tb_topic_table')]//div[contains(@class,'o_tb_topic_card')][div[@class='o_tb_topic_card_meta']/div/div/h4[text()[contains(.,'" + title + "')]]]/div[@class='o_tb_topic_card_cmds']//a[contains(@class,'btn-primary')]");
		OOGraphene.waitElement(selectBy, browser);
		OOGraphene.click(selectBy, browser);
		OOGraphene.waitBusy(browser);
		return assertOnSelectedTopicAsCard(title);
	}

	public TBrokerPage selectTopicHighest(String title) {
		By selectBy = By.xpath("//div[contains(@class,'o_tb_topic_table')]//div[contains(@class,'o_tb_topic_card')][div[@class='o_tb_topic_card_meta']/div/div/h4[text()[contains(.,'" + title + "')]]]/div[@class='o_tb_topic_card_cmds']//button[contains(@class,'dropdown-toggle')]");
		OOGraphene.waitElement(selectBy, browser);
		OOGraphene.click(selectBy, browser);
		By selectFirstBy = By.xpath("//div[contains(@class,'o_tb_topic_table')]//div[contains(@class,'o_tb_topic_card')][div[@class='o_tb_topic_card_meta']/div/div/h4[text()[contains(.,'" + title + "')]]]/div[@class='o_tb_topic_card_cmds']//ul/li/a[i[contains(@class,'o_icon_tb_select_first')]]");
		OOGraphene.waitElement(selectFirstBy, browser);
		OOGraphene.click(selectFirstBy, browser);
		return assertOnSelectedTopicAsCard(title);
	}
	
	public TBrokerPage startEnrollment() {
		By startBy = By.cssSelector("a.o_sel_tb_enrollment_start");
		OOGraphene.waitElement(startBy, browser);
		browser.findElement(startBy).click();
		OOGraphene.waitModalDialog(browser);
		return this;
	}
	
	public TBrokerPage confirmEnrollment(int numOfEnrollment) {
		By runBy = By.xpath("//dialog//div[contains(@class,'o_button_group_right')]/a[i[contains(@class,'o_icon_tb_run_start')]]");
		OOGraphene.waitElement(runBy, browser);
		browser.findElement(runBy).click();
		
		By enrollmentStatsBy = By.xpath("//dialog//div[contains(@class,'o_widget')][div/div[@class='o_widget_icon']/i[contains(@class,'o_icon_tb_enrollments')]]/div[@class='o_widget_content']//span[contains(@class,'o_widget_figure_value')][text()[contains(.,'" + numOfEnrollment + "')]]");
		OOGraphene.waitElement(enrollmentStatsBy, browser);
		
		By useBy = By.xpath("//dialog//div[contains(@class,'buttons')]/button[contains(@class,'btn-primary')][contains(@class,'o_button_dirty')]");
		OOGraphene.waitElement(useBy, browser);
		OOGraphene.click(useBy, browser);

		OOGraphene.waitModalDialog(browser, "fieldset.o_sel_confirm_form");
		
		By confirmElBy = By.cssSelector("dialog.dialog fieldset.o_sel_confirm_form label>input[type='checkbox']");
		WebElement confirmEl = browser.findElement(confirmElBy);
		OOGraphene.check(confirmEl, Boolean.TRUE);
		
		By confirmBy = By.cssSelector("dialog.dialog fieldset.o_sel_confirm_form a.o_sel_confirm");
		browser.findElement(confirmBy).click();
		
		OOGraphene.waitModalDialogWithFieldsetDisappears(browser, "o_sel_confirm_form");
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
}
