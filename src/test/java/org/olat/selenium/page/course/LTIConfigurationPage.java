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
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 21 août 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LTIConfigurationPage {
	
	private final WebDriver browser;
	
	public LTIConfigurationPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public LTIConfigurationPage selectConfiguration() {
		By configBy = By.cssSelector("fieldset.o_sel_lti_config_form");
		OOGraphene.selectTab("o_node_config", configBy, browser);
		return this;
	}
	
	public LTIConfigurationPage setLtiPage(String url, String key, String password) {
		By urlBy = By.cssSelector("div.o_sel_lti_config_title input[type=text]");
		browser.findElement(urlBy).sendKeys(url);
		
		By keyBy = By.cssSelector("div.o_sel_lti_config_key input[type=text]");
		browser.findElement(keyBy).sendKeys(key);
		
		By passwordBy = By.cssSelector("div.o_sel_lti_config_pass input[type=text]");
		browser.findElement(passwordBy).sendKeys(password);
		
		return this;
	}
	
	public LTIConfigurationPage enableScore(double scale, double cutValue) {
		By assessableBy = By.xpath("//div[contains(@class,'o_sel_lti_config_assessable')]//input[@type='checkbox']");
		WebElement assessableEl = browser.findElement(assessableBy);
		OOGraphene.check(assessableEl, Boolean.TRUE);
		
		By scaleBy = By.cssSelector("div.o_sel_lti_config_scale input[type=text]");
		OOGraphene.waitElement(scaleBy, browser);
		WebElement scaleEl = browser.findElement(scaleBy);
		scaleEl.clear();
		scaleEl.sendKeys(Double.toString(scale));
		
		By cutValueBy = By.cssSelector("div.o_sel_lti_config_cutval input[type=text]");
		browser.findElement(cutValueBy).sendKeys(Double.toString(cutValue));
		return this;
	}
	
	public LTIConfigurationPage save() {
		By saveBy = By.cssSelector("fieldset.o_sel_lti_config_form button.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.scrollTop(browser);
		return this;
	}

}
