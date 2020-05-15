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
import org.openqa.selenium.support.ui.Select;

/**
 * 
 * Initial date: 2 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21ConfigurationCEPage {
	
	private final WebDriver browser;
	
	public QTI21ConfigurationCEPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public QTI21ConfigurationCEPage selectConfiguration() {
		By configBy = By.className("o_qti_21_configuration");
		return selectTab(configBy);
	}
	
	public QTI21ConfigurationCEPage showScoreOnHomepage(boolean showResults) {
		By scoreBy = By.cssSelector(".o_sel_results_on_homepage select#o_fioqti_showresult_SELBOX");
		WebElement scoreEl = browser.findElement(scoreBy);
		String val = showResults ? "false" : "no";
		new Select(scoreEl).selectByValue(val);
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public QTI21ConfigurationCEPage showResultsOnHomepage(Boolean show, QTI21AssessmentResultsOptions options) {
		By showResultsBy = By.cssSelector("div.o_sel_qti_show_results input[type='checkbox']");
		WebElement showResultsEl = browser.findElement(showResultsBy);
		OOGraphene.check(showResultsEl, show);
		OOGraphene.waitBusy(browser);
		
		By resultsLevelBy = By.cssSelector("div.o_sel_qti_show_results_options input[type='checkbox']");
		OOGraphene.waitElement(resultsLevelBy, 5, browser);

		if(options.isMetadata()) {
			By levelBy = By.cssSelector("div.o_sel_qti_show_results_options input[type='checkbox'][value='metadata']");
			OOGraphene.check(browser.findElement(levelBy), Boolean.TRUE);
		}
		if(options.isSectionSummary()) {
			By levelBy = By.cssSelector("div.o_sel_qti_show_results_options input[type='checkbox'][value='sectionsSummary']");
			OOGraphene.check(browser.findElement(levelBy), Boolean.TRUE);
		}
		if(options.isQuestionSummary()) {
			By levelBy = By.cssSelector("div.o_sel_qti_show_results_options input[type='checkbox'][value='questionSummary']");
			OOGraphene.check(browser.findElement(levelBy), Boolean.TRUE);
		}
		if(options.isUserSolutions()) {
			By levelBy = By.cssSelector("div.o_sel_qti_show_results_options input[type='checkbox'][value='userSolutions']");
			OOGraphene.check(browser.findElement(levelBy), Boolean.TRUE);
		}
		if(options.isCorrectSolutions()) {
			By levelBy = By.cssSelector("div.o_sel_qti_show_results_options input[type='checkbox'][value='correctSolutions']");
			OOGraphene.check(browser.findElement(levelBy), Boolean.TRUE);
		}
		return this;
	}
	
	public QTI21ConfigurationCEPage saveConfiguration() {
		By saveBy = By.cssSelector(".o_qti_21_configuration button");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public QTI21LayoutConfigurationCEPage selectLayoutConfiguration() {
		By configBy = By.className("o_qti_21_layout_configuration");
		selectTab(configBy);
		return new QTI21LayoutConfigurationCEPage(browser);
	}
	
	private QTI21ConfigurationCEPage selectTab(By tabBy) {
		OOGraphene.selectTab("o_node_config", tabBy, browser);
		return this;
	}

}
