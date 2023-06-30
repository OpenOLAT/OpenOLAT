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
 * Initial date: 30 juin 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class JupyterHubConfigurationPage {
	
	private final WebDriver browser;
	
	public JupyterHubConfigurationPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public JupyterHubConfigurationPage selectConfiguration() {
		By configurationBy = By.cssSelector("ul.o_node_config li.o_sel_jupyterhub_configuration>a");
		OOGraphene.waitElement(configurationBy, browser);
		browser.findElement(configurationBy).click();
		
		By configurationFormBy = By.cssSelector("fieldset.o_sel_jupyterhub_configuration");
		OOGraphene.waitElement(configurationFormBy, browser);
		return this;
	}
	
	public JupyterHubConfigurationPage setImageName(String name) {
		By imageNameBy = By.cssSelector("fieldset.o_sel_jupyterhub_configuration div.o_sel_jupyterhub_image_name input[type='text']");
		OOGraphene.waitElement(imageNameBy, browser);
		browser.findElement(imageNameBy).sendKeys(name);
		return this;
	}
	
	public JupyterHubConfigurationPage saveConfiguration() {
		By saveBy = By.cssSelector("fieldset.o_sel_jupyterhub_configuration button.btn.btn-primary");
		OOGraphene.waitElement(saveBy, browser);
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
}
