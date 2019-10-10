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

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
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
		By hrefBy = By.cssSelector("div.o_sel_qpool_metadata_general>div>h4>a");
		OOGraphene.waitElement(hrefBy, browser);
		browser.findElement(hrefBy).click();
		OOGraphene.waitingALittleLonger();// wait the accordion opens up
		
		By generalMetadata = By.cssSelector("div.o_sel_qpool_metadata_general div.panel-body fieldset.o_form");
		OOGraphene.waitElement(generalMetadata, browser);
		
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
	
	public QuestionMetadataPage saveGeneralMetadata() {
		By buttonsBy = By.cssSelector("div.o_sel_qpool_metadata_general div.panel-body div.o_sel_qpool_metadata_buttons");
		OOGraphene.moveTo(buttonsBy, browser);
		
		By saveBy = By.cssSelector("div.o_sel_qpool_metadata_general div.panel-body div.o_sel_qpool_metadata_buttons button.btn.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.moveTop(browser);
		return this;
	}

}
