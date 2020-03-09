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
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 9 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class STConfigurationPage {
	
	private final WebDriver browser;
	
	public STConfigurationPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public STConfigurationPage selectOverview() {
		By configBy = By.cssSelector("fieldset.o_sel_st_overview_settings");
		OOGraphene.selectTab("o_node_config", configBy, browser);
		return this;
	}
	
	public STConfigurationPage setDisplay(DisplayType type) {
		By displayTypeBy = By.xpath("//fieldset[contains(@class,'o_sel_st_overview_settings')]//div[contains(@class,'o_sel_st_display_type')]//input[@type='radio'][@value='" + type + "']");
		OOGraphene.click(displayTypeBy, browser);
		OOGraphene.waitBusy(browser);
		
		if(type == DisplayType.system || type == DisplayType.peekview) {
			By displayTwoColumnsBy = By.xpath("//fieldset[contains(@class,'o_sel_st_overview_settings')]//input[@type='checkbox'][@name='displayTwoColumns']");
			OOGraphene.waitElement(displayTwoColumnsBy, browser);	
		}
		return this;
	}
	
	public enum DisplayType {
		system,
		peekview,
		file,
		delegate
	}
}
