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

import java.util.Date;
import java.util.Locale;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 6 sept. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TBrokerConfigurationPage {
	
	private final WebDriver browser;
	
	public TBrokerConfigurationPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public TBrokerConfigurationPage selectConfiguration() {
		By tabBy = By.cssSelector("ul.o_node_config li.o_sel_tb_configuration>a");
		OOGraphene.waitElement(tabBy, browser);
		browser.findElement(tabBy).click();
		// By configBy = By.cssSelector("fieldset.o_sel_st_overview_settings");
		// OOGraphene.waitElement(configBy, browser);
		return this;
	}
	
	public TBrokerConfigurationPage selectConfigurationCustomFields() {
		By tabBy = By.cssSelector("ul.o_node_config li.o_sel_tb_custom_fields>a");
		OOGraphene.waitElement(tabBy, browser);
		browser.findElement(tabBy).click();
		// By configBy = By.cssSelector("fieldset.o_sel_st_overview_settings");
		// OOGraphene.waitElement(configBy, browser);
		return this;
	}

	public TBrokerConfigurationPage selectConfigurationTopics() {
		By tabBy = By.cssSelector("ul.o_node_config li.o_sel_tb_topics>a");
		OOGraphene.waitElement(tabBy, browser);
		browser.findElement(tabBy).click();
		// By configBy = By.cssSelector("fieldset.o_sel_st_overview_settings");
		// OOGraphene.waitElement(configBy, browser);
		return this;
	}
	
	public TBrokerConfigurationPage selectPeriod(Date start, Date end) {
		Locale locale = OOGraphene.getLocale(browser);
		String startText =  OOGraphene.formatDate(start, locale);
		By startBy = By.cssSelector("div.o_sel_tb_period div.o_first_date input.o_date_day");
		browser.findElement(startBy).sendKeys(startText);
		browser.findElement(startBy).sendKeys("\t");
		
		String endText =  OOGraphene.formatDate(end, locale);
		By endBy = By.cssSelector("div.o_sel_tb_period div.o_second_date input.o_date_day");
		browser.findElement(endBy).sendKeys(endText);
		browser.findElement(endBy).sendKeys("\t");
		return this;
	}
	
	public TBrokerConfigurationPage saveConfiguration() {
		By saveBy = By.cssSelector("fieldset.o_sel_tb_buttons button.btn.btn-primary");
		OOGraphene.waitElement(saveBy, browser);
		OOGraphene.click(saveBy, browser);
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public TBrokerConfigurationPage addTopic(String identifier, String title, int minParticipants, int maxParticipants) {
		By createButtonBy = By.cssSelector("div.o_tb_topics a.o_sel_tb_create_topic");
		OOGraphene.waitElement(createButtonBy, browser);
		browser.findElement(createButtonBy).click();
		OOGraphene.waitModalDialog(browser, "fieldset.o_sel_tb_standard_fields");
		
		By identifierBy = By.cssSelector("fieldset.o_sel_tb_standard_fields div.o_sel_tb_identifier input[type=text]");
		browser.findElement(identifierBy).sendKeys(identifier);

		By titleBy = By.cssSelector("fieldset.o_sel_tb_standard_fields div.o_sel_tb_title input[type=text]");
		browser.findElement(titleBy).sendKeys(title);
		
		By minBy = By.cssSelector("fieldset.o_sel_tb_standard_fields div.o_tb_participant_num_cols input.o_sel_tb_min_participants[type=text]");
		browser.findElement(minBy).sendKeys(Integer.toString(minParticipants));

		By maxBy = By.cssSelector("fieldset.o_sel_tb_standard_fields div.o_tb_participant_num_cols input.o_sel_tb_max_participants[type=text]");
		browser.findElement(maxBy).sendKeys(Integer.toString(maxParticipants));

		By saveBy = By.xpath("//dialog[contains(@class,'dialog')]//fieldset[contains(@class,'o_form')][div[contains(@class,'o_sel_tb_standard_fields')]]/div//button[contains(@class,'btn-primary')]");
		OOGraphene.click(saveBy, browser);
		OOGraphene.waitBusy(browser);
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
}
