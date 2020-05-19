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

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.test.JunitTestHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;


/**
 * 
 * Initial date: 16 nov. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21CSVImportWizard {
	
	private static final Logger log = Tracing.createLoggerFor(QTI21CSVImportWizard.class);
	
	public static final By nextBy = By.className("o_wizard_button_next");
	public static final By finishBy = By.className("o_wizard_button_finish");
	
	private WebDriver browser;
	
	public QTI21CSVImportWizard(WebDriver browser) {
		this.browser = browser;
	}
	
	/**
	 * Import a txt/csv file from the test file_resources.
	 * @param filename
	 * @return
	 */
	public QTI21CSVImportWizard importFile(String filename) {
		StringBuilder sb = new StringBuilder(32000);
		try(InputStream inStream = JunitTestHelper.class.getResourceAsStream("file_resources/" + filename)) {
			String content = IOUtils.toString(inStream, StandardCharsets.UTF_8);
			String[] lines = content.split("\r?\n");
			for(String line:lines) {
				String[] cols = line.split("\t");
				for(String col:cols) {
					sb.append(col).append("\\t");
				}
				sb.append("\\n");
			}
		} catch(Exception ex) {
			log.error("", ex);
		}
		
		By importAreaBy = By.cssSelector(".modal-content .o_wizard_steps_current_content textarea");
		WebElement importAreaEl = browser.findElement(importAreaBy);
		OOGraphene.textarea(importAreaEl, sb.toString(), browser);
		return this;
	}
	
	public QTI21CSVImportWizard assertOnNumberOfQuestions(int numOfQuestions) {
		By validRowsBy = By.cssSelector("td.o_dnd_label span i.o_icon.o_icon_accept");
		OOGraphene.waitElement(validRowsBy, 5, browser);
		List<WebElement> validRowEls = browser.findElements(validRowsBy);
		Assert.assertEquals(numOfQuestions, validRowEls.size());
		return this;
	}
	
	/**
	 * Next
	 * @return this
	 */
	public QTI21CSVImportWizard next() {
		browser.findElement(nextBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Finish the wizard
	 * @return this
	 */
	public QTI21CSVImportWizard finish() {
		browser.findElement(finishBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitElementDisappears(By.cssSelector(".modal-content .wizard"), 5, browser);
		return this;
	}
}
