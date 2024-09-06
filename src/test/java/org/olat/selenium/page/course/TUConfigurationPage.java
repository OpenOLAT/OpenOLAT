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
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 6 sept. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TUConfigurationPage {

	private final WebDriver browser;
	
	public TUConfigurationPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public TUConfigurationPage selectTunnelConfiguration() {
		By tabBy = By.cssSelector("ul.o_node_config li.o_sel_tu_configuration>a");
		OOGraphene.waitElement(tabBy, browser);
		browser.findElement(tabBy).click();
		By configBy = By.cssSelector("fieldset.o_sel_tu_configuration_form");
		OOGraphene.waitElement(configBy, browser);
		return this;
	}
	
	public TUConfigurationPage addURL(String url) {
		By configBy = By.cssSelector("fieldset.o_sel_tu_configuration_form div.o_sel_tu_url input[type='text']");
		OOGraphene.waitElement(configBy, browser);
		browser.findElement(configBy).sendKeys(url);
		return this;
	}
	
	public TUConfigurationPage selectIframeVisible() {
		By configBy = By.cssSelector("fieldset.o_sel_tu_configuration_form div.o_sel_tu_type input[type='radio'][value='directIFrame']");
		OOGraphene.waitElement(configBy, browser);
		browser.findElement(configBy).click();
		return this;
	}
	
	public TUConfigurationPage saveConfiguration() {
		By saveBy = By.cssSelector("fieldset.o_sel_tu_configuration_form button.btn.btn-primary.o_button_dirty");
		OOGraphene.waitElement(saveBy, browser);
		OOGraphene.click(saveBy, browser);
		By dirtySaveBy = By.cssSelector("fieldset.o_sel_tu_configuration_form button.btn.btn-primary.o_button_dirty");
		OOGraphene.waitElementDisappears(dirtySaveBy, 5, browser);
		return this;
	}
}
