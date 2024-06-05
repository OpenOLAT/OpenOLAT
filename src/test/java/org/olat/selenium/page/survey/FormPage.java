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
package org.olat.selenium.page.survey;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 5 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FormPage {
	
	private final WebDriver browser;
	
	public FormPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public FormPage assertOnParticipantsList() {
		By participantsListBy = By.cssSelector("div.o_sel_form_participants_list");
		OOGraphene.waitElement(participantsListBy, browser);
		return this;
	}
	
	public EvaluationFormPage selectParticipant(String name) {
		By selectBy = By.xpath("//div[@class='o_sel_form_participants_list']//tr/td/a[text()[contains(.,'" + name + "')]]");
		OOGraphene.waitElement(selectBy, browser);
		browser.findElement(selectBy).click();
		
		OOGraphene.waitElement(By.className("o_evaluation_form"), browser);
		
		
		return new EvaluationFormPage(browser);
	}

}
