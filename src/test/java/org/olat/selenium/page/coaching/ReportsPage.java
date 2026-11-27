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

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * The reports of the Course Planner and of the coaching tool: the list of
 * the report templates and the generated reports.
 *
 * Initial date: 27 Nov 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class ReportsPage {

	private final WebDriver browser;

	public ReportsPage(WebDriver browser) {
		this.browser = browser;
	}

	public ReportsPage assertOnTemplates() {
		By templatesBy = By.cssSelector("div.o_sel_report_templates table");
		OOGraphene.waitElement(templatesBy, browser);
		return this;
	}

	/**
	 * @param templateId The identifier of the template, for example "courseplanner.bookingorders"
	 * @return Itself
	 */
	public ReportsPage assertOnTemplate(String templateId) {
		OOGraphene.waitElement(playBy(templateId), browser);
		return this;
	}

	/**
	 * Open the dialog of the parameters with the name of the template.
	 *
	 * @param templateId The identifier of the template
	 * @return The dialog
	 */
	public ReportParametersPage openTemplate(String templateId) {
		// The name and the play icon both open the dialog, the name is the link without the play icon
		By nameBy = By.xpath("//div[contains(@class,'o_sel_report_templates')]//table//tr[.//a[contains(@class,'" + playCssClass(templateId) + "')]]//a[@aria-haspopup='dialog'][not(i[contains(@class,'o_icon_play')])]");
		OOGraphene.waitElement(nameBy, browser).click();
		return new ReportParametersPage(browser).assertOnDialog();
	}

	/**
	 * Open the dialog of the parameters with the play icon.
	 *
	 * @param templateId The identifier of the template
	 * @return The dialog
	 */
	public ReportParametersPage runTemplate(String templateId) {
		OOGraphene.waitElement(playBy(templateId), browser).click();
		return new ReportParametersPage(browser).assertOnDialog();
	}

	/**
	 * Wait until a report is generated, the task runs in the background.
	 *
	 * @return Itself
	 */
	public ReportsPage assertOnGeneratedReport() {
		OOGraphene.waitElementSlowly(infosBy(), 30, browser);
		return this;
	}

	/**
	 * Open the infos of the first generated report.
	 */
	public ReportsPage openInfos() {
		OOGraphene.waitElement(infosBy(), browser).click();
		OOGraphene.waitModalDialog(browser);
		return this;
	}

	/**
	 * @param text A text of the parameters, for example a date, or null to only check the row
	 * @return Itself
	 */
	public ReportsPage assertOnInfosParameters() {
		String xpath = "//dialog//div[contains(@class,'o_sel_export_parameters')]";
		OOGraphene.waitElement(By.xpath(xpath), browser);
		return this;
	}

	public ReportsPage closeInfos() {
		By closeBy = By.cssSelector("dialog div.modal-body fieldset button.btn.btn-primary");
		OOGraphene.click(closeBy, browser);
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}

	private By playBy(String templateId) {
		return By.cssSelector("div.o_sel_report_templates table a." + playCssClass(templateId));
	}

	private String playCssClass(String templateId) {
		return "o_sel_report_" + templateId.replace('.', '_');
	}

	/**
	 * The info button appears when the report is generated.
	 */
	private By infosBy() {
		return By.xpath("(//div[contains(@class,'o_export_card')]//a[i[contains(@class,'o_icon_description')]])[1]");
	}
}
