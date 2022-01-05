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
package org.olat.selenium.page.qti;

import org.olat.ims.qti21.QTI21AssessmentResultsOptions;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 17 nov. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21OptionsPage {
	
	private final WebDriver browser;
	
	public QTI21OptionsPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public QTI21OptionsPage showResults(Boolean show, QTI21AssessmentResultsOptions options) {
		OOGraphene.moveTo(By.cssSelector("fieldset.o_sel_qti_resource_options div.o_sel_qti_show_results"), browser);
		
		By showResultsBy = By.cssSelector("div.o_sel_qti_show_results input[type='checkbox']");
		WebElement showResultsEl2 = browser.findElement(showResultsBy);
		OOGraphene.check(showResultsEl2, show);

		By resultsLevelBy = By.cssSelector("div.o_sel_qti_show_results_options input[type='checkbox']");
		OOGraphene.waitElement(resultsLevelBy, browser);

		if(options.isMetadata()) {
			By levelBy = By.cssSelector("div.o_sel_qti_show_results_options input[type='checkbox'][value='metadata']");
			browser.findElement(levelBy).click();
		}
		if(options.isSectionSummary()) {
			By levelBy = By.cssSelector("div.o_sel_qti_show_results_options input[type='checkbox'][value='sectionsSummary']");
			browser.findElement(levelBy).click();
		}
		if(options.isQuestionSummary()) {
			By levelBy = By.cssSelector("div.o_sel_qti_show_results_options input[type='checkbox'][value='questionSummary']");
			browser.findElement(levelBy).click();
		}
		if(options.isUserSolutions()) {
			By levelBy = By.cssSelector("div.o_sel_qti_show_results_options input[type='checkbox'][value='userSolutions']");
			browser.findElement(levelBy).click();
		}
		if(options.isCorrectSolutions()) {
			By levelBy = By.cssSelector("div.o_sel_qti_show_results_options input[type='checkbox'][value='correctSolutions']");
			browser.findElement(levelBy).click();
		}
		return this;
	}
	
	public QTI21OptionsPage enableSuspend() {
		By suspendBy = By.cssSelector(".o_sel_qti_enable_suspend input[type='checkbox']");
		WebElement suspendEl = browser.findElement(suspendBy);
		OOGraphene.check(suspendEl, Boolean.TRUE);
		return this;
	}
	
	public QTI21OptionsPage save() {
		By saveBy = By.cssSelector("fieldset.o_sel_qti_resource_options button");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);		
		return this;
	}
}
