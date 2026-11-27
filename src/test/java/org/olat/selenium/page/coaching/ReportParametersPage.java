/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.selenium.page.coaching;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * The dialog with the parameters of a report.
 *
 * Initial date: 27 Nov 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class ReportParametersPage {

	private final WebDriver browser;

	public ReportParametersPage(WebDriver browser) {
		this.browser = browser;
	}

	public ReportParametersPage assertOnDialog() {
		OOGraphene.waitModalDialog(browser, ".o_sel_report_params");
		return this;
	}

	/**
	 * @param relative The relative time range, for example "last4Weeks"
	 * @return Itself
	 */
	public ReportParametersPage selectRelative(String relative) {
		selectCard("relative");
		By relativeBy = By.xpath("//div[contains(@class,'o_sel_report_params_relative')]//input[@type='radio'][@value='" + relative + "']");
		OOGraphene.waitElement(relativeBy, browser);
		OOGraphene.check(browser.findElement(relativeBy), Boolean.TRUE);
		return this;
	}

	public ReportParametersPage selectCustomRange(Date from, Date to) {
		selectCard("specific");
		By customBy = By.xpath("//div[contains(@class,'o_sel_report_params_date_range')]//label[input[@value='custom']]");
		OOGraphene.waitElement(customBy, browser).click();
		OOGraphene.waitBusy(browser);

		Locale locale = OOGraphene.getLocale(browser);
		By fromBy = By.cssSelector("div.o_sel_report_params_custom_dates div.o_first_date input.o_date_day");
		OOGraphene.waitElement(fromBy, browser).clear();
		browser.findElement(fromBy).sendKeys(OOGraphene.formatDate(from, locale));
		browser.findElement(fromBy).sendKeys("\t");
		By toBy = By.cssSelector("div.o_sel_report_params_custom_dates div.o_second_date input.o_date_day");
		browser.findElement(toBy).clear();
		browser.findElement(toBy).sendKeys(OOGraphene.formatDate(to, locale));
		browser.findElement(toBy).sendKeys("\t");
		return this;
	}

	public ReportParametersPage assertChangedSince(boolean available) {
		By changedSinceBy = By.cssSelector("dialog div.o_sel_report_params_changed_since");
		if (available) {
			OOGraphene.waitElementPresence(changedSinceBy, 5, browser);
		} else {
			List<WebElement> elements = browser.findElements(changedSinceBy);
			Assert.assertTrue(elements.isEmpty());
		}
		return this;
	}

	/**
	 * Run the report, the dialog closes.
	 */
	public void run() {
		clickRun();
		OOGraphene.waitModalDialogDisappears(browser);
	}

	/**
	 * Run the report with an invalid range, the dialog stays open with an error.
	 */
	public ReportParametersPage runAndAssertRangeError() {
		clickRun();
		By errorBy = By.cssSelector("dialog div.o_sel_report_params_custom_dates div.o_error");
		OOGraphene.waitElement(errorBy, browser);
		return this;
	}

	public void cancel() {
		By cancelBy = By.xpath("//dialog//fieldset[contains(@class,'o_sel_report_params')]//a[contains(@onclick,'cancel')]");
		OOGraphene.click(cancelBy, browser);
		OOGraphene.waitModalDialogDisappears(browser);
	}

	private void clickRun() {
		By runBy = By.cssSelector("dialog fieldset.o_sel_report_params button.btn.btn-primary");
		OOGraphene.click(runBy, browser);
		OOGraphene.waitBusy(browser);
	}

	private void selectCard(String group) {
		By cardBy = By.xpath("//div[contains(@class,'o_sel_report_params_time_range')]//input[@name='time.range'][@value='" + group + "']");
		OOGraphene.waitElement(cardBy, browser);
		browser.findElement(cardBy).click();
		OOGraphene.waitBusy(browser);
	}
}
