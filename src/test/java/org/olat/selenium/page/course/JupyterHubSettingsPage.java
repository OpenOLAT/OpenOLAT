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
public class JupyterHubSettingsPage {
	
	private final WebDriver browser;
	
	public JupyterHubSettingsPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public JupyterHubSettingsPage enableJupyterLab() {
		By enableBy = By.cssSelector("fieldset.o_sel_jupyterhub_admin_configuration .o_sel_jupyterhub_admin_enable input[name='jupyterHub.courseElement']");
		OOGraphene.waitElement(enableBy, browser);
		OOGraphene.check(browser.findElement(enableBy), Boolean.TRUE);
		OOGraphene.waitElement(By.cssSelector("fieldset.o_sel_jupyterhub_admin_configuration .o_sel_jupyterhub_add"), browser);
		return this;
	}
	
	public JupyterHubSettingsPage addConfiguration(String name, String url) {
		By addButtonBy = By.cssSelector("fieldset.o_sel_jupyterhub_admin_configuration a.o_sel_jupyterhub_add");
		browser.findElement(addButtonBy).click();
		OOGraphene.waitModalDialog(browser);
		
		By nameBy = By.cssSelector("fieldset.o_sel_jupyterhub_configuration .o_sel_jupyterhub_name input[type='text']");
		OOGraphene.waitElement(nameBy, browser);
		browser.findElement(nameBy).sendKeys(name);
		
		By urlBy = By.cssSelector("fieldset.o_sel_jupyterhub_configuration .o_sel_jupyterhub_url input[type='text']");
		browser.findElement(urlBy).sendKeys(url);
		
		By saveBy = By.cssSelector("fieldset.o_sel_jupyterhub_configuration button.btn.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		
		return this;
	}
	
	public JupyterHubSettingsPage assertOnConfiguration(String name) {
		By enableBy = By.xpath("//fieldset[contains(@class,'o_sel_jupyterhub_admin_configuration')]//table[contains(@class,'table')]//tr/td[text()[contains(.,'" + name + "')]]");
		OOGraphene.waitElement(enableBy, browser);
		return this;
	}

}
