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

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 10 juin 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21GradingPage {
	
	private final WebDriver browser;
	
	public QTI21GradingPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public QTI21GradingSettingsPage settings() {
		selectSegment("repository.configuration");
		return new QTI21GradingSettingsPage(browser);
	}
	
	public QTI21GradingGradersPage graders() {
		selectSegment("repository.graders");
		return new QTI21GradingGradersPage(browser);
	}
	
	private void selectSegment(String action) {
		By segmentBy = By.xpath("//div[contains(@class,'o_segments')]/a[contains(@onclick,'" + action + "')]");
		OOGraphene.waitElement(segmentBy, browser);
		browser.findElement(segmentBy).click();
		OOGraphene.waitBusy(browser);
		By segmentSelectedBy = By.xpath("//div[contains(@class,'o_segments')]/a[contains(@class,'btn-primary')][contains(@onclick,'" + action + "')]");
		OOGraphene.waitElement(segmentSelectedBy, browser);
	}
}
