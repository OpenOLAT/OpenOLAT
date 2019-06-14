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

import java.util.List;

import org.junit.Assert;
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.selenium.page.portfolio.MediaPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Drive the list of efficiency statements and certificates
 * 
 * Initial date: 05.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EfficiencyStatementPage {
	
	private WebDriver browser;
	
	public EfficiencyStatementPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public EfficiencyStatementPage assertOnEfficiencyStatmentPage() {
		By certificatesBy = By.className("o_sel_certificates_table");
		List<WebElement> certifiatesTable = browser.findElements(certificatesBy);
		Assert.assertFalse(certifiatesTable.isEmpty());
		return this;
	}
	
	public EfficiencyStatementPage assertOnCertificate(String courseTitle) {
		By courseCertificateBy = By.xpath("//div[contains(@class,'o_sel_certificates_table')]//table//tr[td[contains(text(),'" + courseTitle + "')]]");
		List<WebElement> certifiatesTable = browser.findElements(courseCertificateBy);
		Assert.assertFalse(certifiatesTable.isEmpty());
		
		WebElement rowEl = certifiatesTable.get(0);
		By certificateDownloadBy = By.cssSelector("a i.o_icon.o_filetype_pdf");
		List<WebElement> certificateDownloadEls = rowEl.findElements(certificateDownloadBy);
		Assert.assertFalse(certificateDownloadEls.isEmpty());
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
		WebElement rowToAssert = getStatementRow(courseTitle);
		boolean found = false;
		for(int i=0; i<20; i++) {
			By certificateDownloadBy = By.cssSelector("a i.o_icon.o_filetype_pdf");
			List<WebElement> certificateDownloadEls = rowToAssert.findElements(certificateDownloadBy);
			if(certificateDownloadEls.size() > 0) {
				found = true;
				break;
			}
			OOGraphene.waitingALittleLonger();
		}
		Assert.assertTrue(found);
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
		WebElement rowToAssert = getStatementRow(courseTitle);
		Assert.assertNotNull(rowToAssert);
		if(passed) {
			By passedBy = By.cssSelector(".o_state.o_passed");
			browser.findElement(passedBy);	
		} else {
			By failedBy = By.cssSelector(".o_state.o_failed");
			browser.findElement(failedBy);
		}
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
		By courseCertificateBy = By.xpath("//div[contains(@class,'o_efficiencystatement')]//table//tr[td/span[contains(text(),'" + testNodeTitle + "')]]");
		List<WebElement> certifiatesTable = browser.findElements(courseCertificateBy);
		Assert.assertFalse(certifiatesTable.isEmpty());
		
		By by;
		if(passed) {
			by = By.cssSelector("td.text-left span.o_state.o_passed");
		} else {
			by = By.cssSelector("td.text-left span.o_state.o_failed");
		}
		List<WebElement> passedEl = certifiatesTable.get(0).findElements(by);
		Assert.assertFalse(passedEl.isEmpty());
		Assert.assertTrue(passedEl.get(0).isDisplayed());
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
		WebElement rowToAssert = getStatementRow(courseTitle);
		By courseCertificateBy = By.xpath("//td//a[contains(@href,'cmd.show')]");
		rowToAssert.findElement(courseCertificateBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	private WebElement getStatementRow(String courseTitle) {
		By courseCertificateBy = By.xpath("//div[contains(@class,'o_sel_certificates_table')]//table//tr");
		
		WebElement rowToAssert = null;
		List<WebElement> rowsEl = browser.findElements(courseCertificateBy);
		a_a:
		for(WebElement rowEl:rowsEl) {
			for(WebElement col:rowEl.findElements(By.tagName("td"))) {
				String text = col.getText();
				if(courseTitle.contains(text) || text.contains(courseTitle)) {
					rowToAssert = rowEl;
					break a_a;
				}
			}
		}
		
		Assert.assertNotNull(rowToAssert);
		return rowToAssert;
	}

	public EfficiencyStatementPage selectStatementSegment() {
		By courseDetailsBy = By.className("o_select_statement_segment");
		browser.findElement(courseDetailsBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public MediaPage addAsMediaInList(String courseTitle) {
		if(courseTitle.length() > 25) {
			courseTitle = courseTitle.substring(0, 25);
		}//
		
		By collectBy = By.xpath("//div[contains(@class,'o_sel_certificates_table')]//table//tr[td[contains(text(),'" + courseTitle + "')]]/td/a[contains(@href,'cmd.MEDIA')]");
		OOGraphene.waitElement(collectBy, browser);
		browser.findElement(collectBy).click();
		OOGraphene.waitTinymce(browser);
		OOGraphene.waitModalDialog(browser);
		return new MediaPage(browser);
	}
}
