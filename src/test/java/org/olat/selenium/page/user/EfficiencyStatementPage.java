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
package org.olat.selenium.page.user;

import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.selenium.page.portfolio.MediaPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Drive the list of efficiency statements and certificates
 * 
 * Initial date: 05.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class EfficiencyStatementPage {
	
	private WebDriver browser;
	
	public EfficiencyStatementPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public EfficiencyStatementPage assertOnEfficiencyStatmentPage() {
		By certificatesBy = By.className("o_sel_certificates_table");
		OOGraphene.waitElement(certificatesBy, browser);
		return this;
	}
	
	/**
	 * 
	 * Statement cut the title, be aware of it
	 * 
	 * @param courseTitle
	 * @return
	 */
	public EfficiencyStatementPage assertOnCertificateAndStatements(String courseTitle) {
		if(courseTitle.length() > 25) {
			courseTitle = courseTitle.substring(0, 25);
		}
		By courseCertificateBy = By.xpath("//div[contains(@class,'o_sel_certificates_table')]//table//tr[td/a[contains(text(),'" + courseTitle + "')]]/td/a/i[contains(@class,'o_filetype_pdf')]");
		OOGraphene.waitElementSlowly(courseCertificateBy, 30, browser);
		return this;
	}
	
	/**
	 * Assert that an efficiency statement of the course specified by the course title
	 * is passed or failed.
	 * 
	 * @param courseTitle
	 * @param passed
	 * @return
	 */
	public EfficiencyStatementPage assertOnStatement(String courseTitle, boolean passed) {
		if(courseTitle.length() > 25) {
			courseTitle = courseTitle.substring(0, 25);
		}
		String passedCss = passed ? "o_passed" : "o_failed";
		By courseCertificateBy = By.xpath("//div[contains(@class,'o_sel_certificates_table')]//table//tr[td/a[contains(text(),'" + courseTitle + "')]]/td/div[contains(@class,'o_state')][contains(@class,'" + passedCss + "')]");
		OOGraphene.waitElement(courseCertificateBy, browser);
		return this;
	}
	
	/**
	 * In the efficiency statement page / course details, check that
	 * the node is in the table and the node is passe / failed.
	 * 
	 * @param testNodeTitle
	 * @param passed
	 * @return
	 */
	public EfficiencyStatementPage assertOnCourseDetails(String testNodeTitle, boolean passed) {
		String stateClassname = passed ? "o_passed" : "o_failed";
		By courseCertificateBy = By.xpath("//div[contains(@class,'o_achievements_details')]//table//tr[td/span[contains(text(),'" + testNodeTitle + "')]]/td[contains(@class,'text-left')]/div[contains(@class,'o_state')][contains(@class,'" + stateClassname + "')]");
		OOGraphene.waitElement(courseCertificateBy, browser);
		return this;
	}
	
	/**
	 * On the page with the certificate and the efficiency statement,
	 * select the efficiency statement tab where there are the course
	 * details.
	 * 
	 * @param courseTitle
	 * @return
	 */
	public EfficiencyStatementPage selectStatement(String courseTitle) {
		if(courseTitle.length() > 25) {
			courseTitle = courseTitle.substring(0, 25);
		}
		By courseCertificateBy = By.xpath("//div[contains(@class,'o_sel_certificates_table')]//table//tr/td/a[contains(text(),'" + courseTitle + "')]");
		OOGraphene.waitElement(courseCertificateBy, browser).click();
 		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public MediaPage addAsMediaInList(String courseTitle) {
		if(courseTitle.length() > 25) {
			courseTitle = courseTitle.substring(0, 25);
		}
		By collectBy = By.xpath("//div[contains(@class,'o_sel_certificates_table')]//table//tr[td/a[contains(text(),'" + courseTitle + "')]]/td/a[contains(@onclick,'cmd.MEDIA')][i[contains(@class,'o_icon_eportfolio_add')]]");
		OOGraphene.waitElement(collectBy, browser);
		OOGraphene.scrollBottom(collectBy, browser);
		browser.findElement(collectBy).click();
		OOGraphene.waitModalDialog(browser);
		OOGraphene.waitTinymce(browser);
		return new MediaPage(browser);
	}
}
