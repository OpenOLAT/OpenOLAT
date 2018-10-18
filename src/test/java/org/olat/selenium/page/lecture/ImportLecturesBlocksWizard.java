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
package org.olat.selenium.page.lecture;

import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.test.JunitTestHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 15 Oct 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ImportLecturesBlocksWizard {
	
	private static final OLog log = Tracing.createLoggerFor(ImportLecturesBlocksWizard.class);
	
	private final WebDriver browser;
	
	public ImportLecturesBlocksWizard(WebDriver browser) {
		this.browser = browser;
	}
	
	public ImportLecturesBlocksWizard importFile(String filename) {
		StringBuilder sb = new StringBuilder(32000);
		try(InputStream inStream = JunitTestHelper.class.getResourceAsStream("file_resources/" + filename)) {
			String content = IOUtils.toString(inStream, "UTF-8");
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
	
	public ImportLecturesBlocksWizard assertOnNumberOfNewBlocks(int numOfBlocks) {
		By newRowsBy = By.cssSelector("td.o_dnd_label span i.o_icon.o_icon_check");
		OOGraphene.waitElement(newRowsBy, browser);
		List<WebElement> newRowsEl = browser.findElements(newRowsBy);
		Assert.assertEquals(numOfBlocks, newRowsEl.size());
		return this;
	}
	
	/**
	 * Next
	 * @return this
	 */
	public ImportLecturesBlocksWizard next() {
		OOGraphene.nextStep(browser);
		return this;
	}
	
	/**
	 * Finish the wizard
	 * @return this
	 */
	public ImportLecturesBlocksWizard finish() {
		browser.findElement(OOGraphene.wizardFinishBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitElementDisappears(By.cssSelector(".modal-content .wizard"), 5, browser);
		return this;
	}

}
