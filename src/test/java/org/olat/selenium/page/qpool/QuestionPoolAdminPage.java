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
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 27 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QuestionPoolAdminPage {
	
	private final WebDriver browser;
	
	public QuestionPoolAdminPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public QuestionPoolAdminPage assertOnConfiguration() {
		By configBy = By.className("o_sel_qpool_configuration");
		OOGraphene.waitElement(configBy, browser);
		return this;
	}
	
	public QuestionPoolAdminPage enableReviews() {
		assertOnConfiguration();
		
		By reviewBy = By.cssSelector(".o_sel_qpool_configuration .o_sel_qpool_review_process input[type='checkbox']");
		WebElement reviewEl = browser.findElement(reviewBy);
		String checked = reviewEl.getAttribute("checked");
		if(checked == null) {
			OOGraphene.check(reviewEl, Boolean.TRUE);
			OOGraphene.waitModalDialog(browser);
			
			//don't reset the status
			By statusBy = By.cssSelector("fieldset.o_sel_qpool_reset_status_form .o_sel_qpool_reset_status input[type='checkbox']");
			WebElement statusEl = browser.findElement(statusBy);
			OOGraphene.check(statusEl, Boolean.TRUE);

			//engage review process
			By confirmBy = By.cssSelector("fieldset.o_sel_qpool_reset_status_form button.btn-primary");
			browser.findElement(confirmBy).click();
			OOGraphene.waitModalDialogDisappears(browser);
			
			try {
				By saveConfigurationBy = By.cssSelector(".o_sel_qpool_buttons button.btn-primary");
				OOGraphene.click(saveConfigurationBy, browser);
				OOGraphene.waitBusy(browser);
				By savedConfigurationBy = By.xpath("//fieldset[contains(@class,'o_sel_qpool_buttons')]//button[contains(@class,'btn-primary') and not(contains(@class,'o_button_dirty'))]");
				OOGraphene.waitElement(savedConfigurationBy, browser);
			} catch (Exception e) {
				OOGraphene.takeScreenshot("Enable reviews", browser);
				throw e;
			}
		}
		return this;
	}
	
	public QuestionPoolAdminPage reviewsConfiguration(int numOfReviews, int numOfStars) {
		By reviewBy = By.xpath("//div[contains(@class,'o_segments')]/a[contains(@onclick,'segment.review.process')]");
		browser.findElement(reviewBy).click();
		OOGraphene.waitBusy(browser);
		
		By numOfReviewsBy = By.cssSelector("fieldset.o_sel_qpool_review_process_admin .o_sel_qpool_num_of_reviews input[type='text']");
		OOGraphene.waitElement(numOfReviewsBy, browser);
		WebElement numOfReviewEl = browser.findElement(numOfReviewsBy);
		numOfReviewEl.clear();
		numOfReviewEl.sendKeys(Integer.toString(numOfReviews));
		
		By numOfStarsBy = By.xpath("//div[contains(@class,'o_rating')]/div[contains(@class,'o_rating_items')]/a[" + numOfStars + "]");
		browser.findElement(numOfStarsBy).click();
		OOGraphene.waitBusy(browser);
		
		By saveBy = By.cssSelector("fieldset.o_sel_qpool_review_process_admin button.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public QuestionPoolAdminPage selectLevels() {
		By levelsBy = By.xpath("//div[contains(@class,'o_segments')]/a[contains(@onclick,'segment.educational.context')]");
		OOGraphene.waitElement(levelsBy, browser);
		browser.findElement(levelsBy).click();
		return this;
	}
	
	public QuestionPoolAdminPage addLevel(String name) {
		By addLevelBy = By.cssSelector("a.btn.o_sel_add_level");
		OOGraphene.waitElement(addLevelBy, browser);
		browser.findElement(addLevelBy).click();
		
		OOGraphene.waitModalDialog(browser);
		
		By nameBy = By.cssSelector("fieldset.o_sel_edit_level_form div.o_sel_level_name input[type='text']");
		OOGraphene.waitElement(nameBy, browser);
		browser.findElement(nameBy).sendKeys(name);
		
		By saveBy = By.cssSelector("fieldset.o_sel_edit_level_form button.btn.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	public QuestionPoolAdminPage assertLevelInList(String level) {
		By levelBy = By.xpath("//div[contains(@class,'o_table_flexi')]//table//td[text()[contains(.,'" + level + "')]]");
		OOGraphene.waitElement(levelBy, browser);
		return this;
	}
	
}
