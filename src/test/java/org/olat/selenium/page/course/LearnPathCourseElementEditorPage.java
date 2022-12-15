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

import org.olat.course.learningpath.FullyAssessedTrigger;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 27 mai 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LearnPathCourseElementEditorPage {
	
	private final WebDriver browser;
	
	public LearnPathCourseElementEditorPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public LearnPathCourseElementEditorPage setCompletionCriterion(FullyAssessedTrigger trigger) {
		By triggerBy = By.xpath("//fieldset[contains(@class,'o_lp_config_edit')]//fieldset[@id='o_coconfig_trigger']//input[@name='config.trigger'][@value='" + trigger.name() + "']");
		OOGraphene.waitElement(triggerBy, browser);
		browser.findElement(triggerBy).click();
		OOGraphene.waitingLong();//SEL wait focus jump
		By checkedBy = By.xpath("//fieldset[contains(@class,'o_lp_config_edit')]//fieldset[@id='o_coconfig_trigger']//input[@name='config.trigger'][@value='" + trigger.name() + "'][@checked='checked']");
		OOGraphene.waitElement(checkedBy, browser);
		return this;
	}
	
	public LearnPathCourseElementEditorPage save() {
		By saveBy = By.cssSelector("fieldset.o_lp_config_edit button.btn.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}

}
