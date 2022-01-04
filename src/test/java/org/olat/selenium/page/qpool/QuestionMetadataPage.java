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
package org.olat.selenium.page.qpool;

import java.util.List;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/**
 * 
 * Initial date: 23 sept. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QuestionMetadataPage {
	
	private final WebDriver browser;
	
	private QuestionMetadataPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public static QuestionMetadataPage getPage(WebDriver browser) {
		return new QuestionMetadataPage(browser);
	}
	
	public QuestionMetadataPage openGeneral() {
		return openMetadata("o_sel_qpool_metadata_general");
	}
	
	public QuestionMetadataPage openItemAnalyse() {
		return openMetadata("o_sel_qpool_metadata_item_analyse");
	}
	
	private QuestionMetadataPage openMetadata(String panelClass) {
		By hrefBy = By.cssSelector("div." + panelClass + ">div>h4>a");
		OOGraphene.waitElement(hrefBy, browser);
		
		By panelInBy = By.cssSelector("div." + panelClass + " div.panel-collapse.collapse.in");
		List<WebElement> panelInEls = browser.findElements(panelInBy);
		if(panelInEls.isEmpty()) {
			browser.findElement(hrefBy).click();
			OOGraphene.waitElement(panelInBy, browser);
			OOGraphene.waitingALittleLonger();// wait the accordion opens up
		}
		
		By formBy = By.cssSelector("div." + panelClass + " div.panel-body fieldset.o_form");
		OOGraphene.waitElement(formBy, browser);
		return this;
	}
	
	public QuestionMetadataPage setGeneralMetadata(String topic, String taxonomy, String level,
			String keywords, String additionalInfos, String coverage, String assessmentType) {
		if(topic != null) {
			By topicBy = By.cssSelector("div.o_sel_qpool_metadata_topic input[type='text']");
			browser.findElement(topicBy).sendKeys(topic);
		}
		
		if(taxonomy != null) {
			By taxonomyBy = By.cssSelector("div.o_sel_qpool_metadata_taxonomy select");
			new Select(browser.findElement(taxonomyBy)).selectByVisibleText(taxonomy);
			OOGraphene.waitBusy(browser);
		}
		
		if(level != null) {
			By levelBy = By.cssSelector("div.o_sel_qpool_metadata_context select");
			new Select(browser.findElement(levelBy)).selectByVisibleText(level);
		}
		
		if(keywords != null) {
			By keywordsBy = By.cssSelector("div.o_sel_qpool_metadata_keywords input[type='text']");
			browser.findElement(keywordsBy).sendKeys(keywords);
		}
		
		if(additionalInfos != null) {
			By additionalInfosBy = By.cssSelector("div.o_sel_qpool_metadata_add_infos input[type='text']");
			browser.findElement(additionalInfosBy).sendKeys(additionalInfos);
		}
		
		if(coverage != null) {
			By coverageBy = By.cssSelector("div.o_sel_qpool_metadata_coverage input[type='text']");
			browser.findElement(coverageBy).sendKeys(coverage);
		}
		
		if(assessmentType != null) {
			By assessmentTypeBy = By.cssSelector("div.o_sel_qpool_metadata_assessment_type select");
			new Select(browser.findElement(assessmentTypeBy)).selectByValue(assessmentType);
		}
		return this;
	}
	
	public QuestionMetadataPage setLearningTime(Integer days, Integer hours, Integer minutes, Integer seconds) {
		
		if(days != null) {
			By dayBy = By.cssSelector("div.o_sel_qpool_metadata_item_analyse input.o_sel_learning_time_d[type='text']");
			WebElement dayEl = browser.findElement(dayBy);
			dayEl.clear();
			dayEl.sendKeys(days.toString());
		}
		if(hours != null) {
			By hourBy = By.cssSelector("div.o_sel_qpool_metadata_item_analyse input.o_sel_learning_time_H[type='text']");
			WebElement hourEl = browser.findElement(hourBy);
			hourEl.clear();
			hourEl.sendKeys(hours.toString());
		}
		if(minutes != null) {
			By minuteBy = By.cssSelector("div.o_sel_qpool_metadata_item_analyse input.o_sel_learning_time_m[type='text']");
			WebElement minuteEl = browser.findElement(minuteBy);
			minuteEl.clear();
			minuteEl.sendKeys(minutes.toString());
		}
		if(seconds != null) {
			By secondBy = By.cssSelector("div.o_sel_qpool_metadata_item_analyse input.o_sel_learning_time_s[type='text']");
			WebElement secondEl = browser.findElement(secondBy);
			secondEl.clear();
			secondEl.sendKeys(seconds.toString());
		}
		return this;
	}
	
	/**
	 * 
	 * @param difficulty Value between 0.0 and 1.0
	 * @param standardDeviation Value between 0.0 and 1.0
	 * @param discriminationIndex Value between -1.0 and 1.0
	 * @param distractors The number of distractors
	 * @param usage Number of times this questions is used
	 * @param correctionTime Time in minutes to correction the question
	 * @return Itself
	 */
	public QuestionMetadataPage setItemAnalyse(Double difficulty, Double standardDeviation,
			Double discriminationIndex, Integer distractors, Integer usage, Integer correctionTime) {
		
		if(difficulty != null) {
			By difficultyBy = By.cssSelector(".o_sel_qpool_metadata_item_analyse .o_sel_difficulty input[type='text']");
			browser.findElement(difficultyBy).sendKeys(difficulty.toString());
		}
		if(standardDeviation != null) {
			By deviationBy = By.cssSelector(".o_sel_qpool_metadata_item_analyse .o_sel_std_dev_difficulty input[type='text']");
			browser.findElement(deviationBy).sendKeys(standardDeviation.toString());
		}
		if(discriminationIndex != null) {
			By discriminationBy = By.cssSelector(".o_sel_qpool_metadata_item_analyse .o_sel_std_differentation input[type='text']");
			browser.findElement(discriminationBy).sendKeys(discriminationIndex.toString());
		}
		if(distractors != null) {
			By distractorsBy = By.cssSelector(".o_sel_qpool_metadata_item_analyse .o_sel_distractors input[type='text']");
			WebElement distractorsEl = browser.findElement(distractorsBy);
			distractorsEl.clear();
			distractorsEl.sendKeys(distractors.toString());
		}
		if(usage != null) {
			By usageBy = By.cssSelector(".o_sel_qpool_metadata_item_analyse .o_sel_usage input[type='text']");
			WebElement usageEl = browser.findElement(usageBy);
			usageEl.clear();
			usageEl.sendKeys(usage.toString());
		}
		if(correctionTime != null) {
			By correctionTimeBy = By.cssSelector(".o_sel_qpool_metadata_item_analyse .o_sel_correction_time input[type='text']");
			browser.findElement(correctionTimeBy).sendKeys(correctionTime.toString());
		}
		return this;
	}
	
	public QuestionMetadataPage assertTopic(String topic) {
		By topicBy = By.xpath("//div[contains(@class,'o_sel_qpool_metadata_topic')]//input[@value='" + topic + "']");
		OOGraphene.waitElement(topicBy, browser);
		return this;
	}
	
	public QuestionMetadataPage assertTaxonomy(String taxonomy) {
		By taxonomyBy = By.xpath("//div[contains(@class,'o_sel_qpool_metadata_taxonomy')]//select/option[@selected='selected'][text()[contains(.,'" + taxonomy + "')]]");
		OOGraphene.waitElement(taxonomyBy, browser);
		return this;
	}
	
	public QuestionMetadataPage assertLevel(String level) {
		By levelBy = By.xpath("//div[contains(@class,'o_sel_qpool_metadata_context')]//select/option[@selected='selected'][text()[contains(.,'" + level + "')]]");
		OOGraphene.waitElement(levelBy, browser);
		return this;
	}
	
	public QuestionMetadataPage assertKeywords(String keywords) {
		By keywordsBy = By.xpath("//div[contains(@class,'o_sel_qpool_metadata_keywords')]//input[@value='" + keywords + "']");
		OOGraphene.waitElement(keywordsBy, browser);
		return this;
	}
	
	public QuestionMetadataPage assertCoverage(String coverage) {
		By coverageBy = By.xpath("//div[contains(@class,'o_sel_qpool_metadata_coverage')]//input[@value='" + coverage + "']");
		OOGraphene.waitElement(coverageBy, browser);
		return this;
	}
	
	public QuestionMetadataPage assertAdditionalInfos(String infos) {
		By infosBy = By.xpath("//div[contains(@class,'o_sel_qpool_metadata_add_infos')]//input[@value='" + infos + "']");
		OOGraphene.waitElement(infosBy, browser);
		return this;
	}
	
	public QuestionMetadataPage assertAssessmentType(String assessmentType) {
		By assessmentTypeBy = By.xpath("//div[contains(@class,'o_sel_qpool_metadata_assessment_type')]//select/option[@selected='selected'][@value='" + assessmentType + "']");
		OOGraphene.waitElement(assessmentTypeBy, browser);
		return this;
	}
	
	public QuestionMetadataPage assertLearningTime(Integer days, Integer hours, Integer minutes, Integer seconds) {
		if(days != null) {
			By dayBy = By.xpath("//div[contains(@class,'o_sel_qpool_metadata_item_analyse')]//input[@value='" + days + "'][contains(@class,'o_sel_learning_time_d')]");
			OOGraphene.waitElement(dayBy, browser);
		}
		if(hours != null) {
			By hourBy = By.xpath("//div[contains(@class,'o_sel_qpool_metadata_item_analyse')]//input[@value='" + hours + "'][contains(@class,'o_sel_learning_time_H')]");
			OOGraphene.waitElement(hourBy, browser);
		}
		if(minutes != null) {
			By minuteBy = By.xpath("//div[contains(@class,'o_sel_qpool_metadata_item_analyse')]//input[@value='" + minutes + "'][contains(@class,'o_sel_learning_time_m')]");
			OOGraphene.waitElement(minuteBy, browser);
		}
		if(seconds != null) {
			By secondBy = By.xpath("//div[contains(@class,'o_sel_qpool_metadata_item_analyse')]//input[@value='" + seconds + "'][contains(@class,'o_sel_learning_time_s')]");
			OOGraphene.waitElement(secondBy, browser);
		}
		return this;
	}
	
	public QuestionMetadataPage assertDifficulty(Double diffculty) {
		By difficultyBy = By.xpath("//div[contains(@class,'o_sel_difficulty')]//input[@value='" + diffculty + "']");
		OOGraphene.waitElement(difficultyBy, browser);
		return this;
	}
	
	public QuestionMetadataPage assertDiscriminationIndex(Double discriminationIndex) {
		By discriminationIndexBy = By.xpath("//div[contains(@class,'o_sel_std_differentation')]//input[@value='" + discriminationIndex + "']");
		OOGraphene.waitElement(discriminationIndexBy, browser);
		return this;
	}
	
	public QuestionMetadataPage assertStandardDeviation(Double stdDeviation) {
		By deviationBy = By.xpath("//div[contains(@class,'o_sel_std_dev_difficulty')]//input[@value='" + stdDeviation + "']");
		OOGraphene.waitElement(deviationBy, browser);
		return this;
	}
	
	public QuestionMetadataPage assertDistractors(Integer distractors) {
		By distractorsBy = By.xpath("//div[contains(@class,'o_sel_distractors')]//input[@value='" + distractors + "']");
		OOGraphene.waitElement(distractorsBy, browser);
		return this;
	}
	
	public QuestionMetadataPage assertUsage(Integer usage) {
		By usageBy = By.xpath("//div[contains(@class,'o_sel_usage')]//input[@value='" + usage + "']");
		OOGraphene.waitElement(usageBy, browser);
		return this;
	}
	
	public QuestionMetadataPage assertCorrectionTime(Integer timeInMinutes) {
		By correctionTimeBy = By.xpath("//div[contains(@class,'o_sel_correction_time')]//input[@value='" + timeInMinutes + "']");
		OOGraphene.waitElement(correctionTimeBy, browser);
		return this;
	}
	
	public QuestionMetadataPage saveGeneralMetadata() {
		return saveMetadata("o_sel_qpool_metadata_general");
	}
	
	public QuestionMetadataPage saveItemAnalyse() {
		return saveMetadata("o_sel_qpool_metadata_item_analyse");
	}
	
	private QuestionMetadataPage saveMetadata(String panelClass) {
		By buttonsBy = By.cssSelector("div." + panelClass + " div.panel-body div.o_sel_qpool_metadata_buttons");
		OOGraphene.moveTo(buttonsBy, browser);
		
		By saveBy = By.cssSelector("div." + panelClass + " div.panel-body div.o_sel_qpool_metadata_buttons button.btn.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.scrollTop(browser);
		return this;
	}

}
