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
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

/**
 * 
 * Initial date: 29.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BulkAssessmentPage {

	public static final By nextBy = By.className("o_wizard_button_next");
	public static final By finishBy = By.className("o_wizard_button_finish");
	
	private WebDriver browser;
	
	public BulkAssessmentPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public BulkAssessmentPage data(BulkAssessmentData[] data) {
		StringBuilder sb = new StringBuilder();
		for(BulkAssessmentData userData:data) {
			if(sb.length() > 0) sb.append("\\n");
			sb.append(userData.getUser().getLogin());
			sb.append("\\t");
			if(userData.getScore() != null) {
				sb.append(userData.getScore());
			}
			sb.append("\\t");
			if(userData.getPassed() != null) {
				sb.append(userData.getPassed().booleanValue() ? "y" : "n");
			}
			sb.append("\\t");
			if(userData.getComment() != null) {
				sb.append(userData.getComment());
			}
		}
		By importAreaBy = By.cssSelector(".modal-content textarea");
		WebElement importAreaEl = browser.findElement(importAreaBy);
		OOGraphene.textarea(importAreaEl, sb.toString(), browser);
		return this;
	}
	
	public BulkAssessmentPage nextNodes() {
		OOGraphene.nextStep(browser);
		OOGraphene.closeBlueMessageWindow(browser);
		OOGraphene.waitElement(By.cssSelector("fieldset.o_sel_bulk_assessment_data"), browser);
		return this;
	}
	
	public BulkAssessmentPage nextData() {
		if(browser instanceof FirefoxDriver) {
			OOGraphene.waitingALittleLonger();
			By modalFooterBy = By.cssSelector(".modal .modal-footer");
			OOGraphene.moveTo(modalFooterBy, browser);
		}
		OOGraphene.nextStep(browser);
		OOGraphene.closeBlueMessageWindow(browser);
		OOGraphene.waitElement(By.cssSelector("fieldset.o_sel_bulk_assessment_columns"), browser);
		return this;
	}
	
	public BulkAssessmentPage nextColumns() {
		OOGraphene.nextStep(browser);
		OOGraphene.closeBlueMessageWindow(browser);
		OOGraphene.waitElement(By.cssSelector("div.o_sel_bulk_assessment_validation"), browser);
		return this;
	}
	
	public BulkAssessmentPage nextValidation() {
		OOGraphene.nextStep(browser);
		OOGraphene.closeBlueMessageWindow(browser);
		OOGraphene.waitElement(By.cssSelector("fieldset.o_sel_bulk_assessment_schedule"), browser);
		return this;
	}
	
	public BulkAssessmentPage finish() {
		OOGraphene.finishStep(browser);
		OOGraphene.waitAndCloseBlueMessageWindow(browser);
		return this;
	}
	
	public static class BulkAssessmentData {
		
		private final UserVO user;
		private final Float score;
		private final Boolean passed;
		private final String comment;
		
		public BulkAssessmentData(UserVO user, Float score, Boolean passed, String comment) {
			this.user = user;
			this.score = score;
			this.passed = passed;
			this.comment = comment;
		}

		public UserVO getUser() {
			return user;
		}

		public Float getScore() {
			return score;
		}

		public Boolean getPassed() {
			return passed;
		}

		public String getComment() {
			return comment;
		}
	}
}
