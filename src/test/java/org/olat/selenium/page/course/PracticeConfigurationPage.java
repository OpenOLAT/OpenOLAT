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
 * Initial date: 30 juin 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PracticeConfigurationPage {
	
	private final WebDriver browser;
	
	public PracticeConfigurationPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public PracticeConfigurationPage selectConfiguration() {
		By tabBy = By.cssSelector("ul.o_node_config li.o_sel_practice_configuration>a");
		OOGraphene.waitElement(tabBy, browser);
		browser.findElement(tabBy).click();
		OOGraphene.waitElement(By.className("o_practice_configuration"), browser);
		return this;
	}
	
	public PracticeConfigurationPage selectTest(String name) {
		By addButtonBy = By.cssSelector("a.o_sel_practice_add_test");
		browser.findElement(addButtonBy).click();
		OOGraphene.waitModalDialog(browser);
		
		By selectBy = By.xpath("//div[@class='o_sel_author_env']//td/a[text()[contains(.,'" + name + "')]]");
		browser.findElement(selectBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		
		By resourcesBy = By.xpath("//div[contains(@class,'o_sel_practice_resources_list')]//td[contains(.,'" + name + "')]");
		OOGraphene.waitElement(resourcesBy, browser);
		return this;
	}
	
	public PracticeConfigurationPage setNumberOfSeries(int numOfSeriesPerChallenge, int numOfChallengesToComplete) {
		By seriesBy = By.cssSelector("fieldset.o_practice_configuration .o_sel_practice_serie_per_challenge input[type='text']");
		OOGraphene.waitElement(seriesBy, browser);
		WebElement seriesEl = browser.findElement(seriesBy);
		seriesEl.clear();
		seriesEl.sendKeys(Integer.toString(numOfSeriesPerChallenge));
		
		
		By challengesBy = By.cssSelector("fieldset.o_practice_configuration .o_sel_practice_challenge_to_complete input[type='text']");
		WebElement challengesEl = browser.findElement(challengesBy);
		challengesEl.clear();
		challengesEl.sendKeys(Integer.toString(numOfChallengesToComplete));
		return this;
	}
	
	public PracticeConfigurationPage saveConfiguration() {
		By saveBy = By.cssSelector("fieldset.o_practice_configuration button.btn.btn-primary");
		OOGraphene.click(saveBy, browser);
		OOGraphene.waitBusy(browser);
		return this;
	}
}
