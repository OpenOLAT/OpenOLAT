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
 * Initial date: 05.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EfficiencyStatementConfigurationPage {
	
	private final WebDriver browser;
	
	public EfficiencyStatementConfigurationPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public EfficiencyStatementConfigurationPage enableCertificates(boolean auto) {
		By by;
		if(auto) {
			by = By.cssSelector("fieldset.o_sel_course_certificates div.checkbox input[type='checkbox'][value='auto']");
		} else {
			by = By.cssSelector("fieldset.o_sel_course_certificates div.checkbox input[type='checkbox'][value='manual']");
		}
		browser.findElement(by).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public EfficiencyStatementConfigurationPage enableRecertification() {
		By recertificationBy = By.cssSelector("fieldset.o_sel_course_certificates input[type='checkbox'][name='recertification']");
		browser.findElement(recertificationBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public EfficiencyStatementConfigurationPage save() {
		By saveSwitch = By.cssSelector("fieldset.o_sel_course_certificates button.btn.btn-primary");
		browser.findElement(saveSwitch).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Click back to the course
	 * 
	 * @return
	 */
	public CoursePageFragment clickToolbarBack() {
		OOGraphene.closeBlueMessageWindow(browser);
		By toolbarBackBy = By.cssSelector("li.o_breadcrumb_back>a");
		browser.findElement(toolbarBackBy).click();
		OOGraphene.waitBusy(browser);
		return new CoursePageFragment(browser);
	}

}
