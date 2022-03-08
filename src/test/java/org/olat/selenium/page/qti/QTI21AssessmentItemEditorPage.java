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
 * Initial date: 03 may 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class QTI21AssessmentItemEditorPage {
	
	public static final By tabBarBy = By.cssSelector("ul.o_sel_assessment_item_config>li>a");
	
	protected final WebDriver browser;
	
	protected QTI21AssessmentItemEditorPage(WebDriver browser) {
		this.browser = browser;
	}
	
	protected QTI21AssessmentItemEditorPage selectTab(String tabCssClass, By tabBy) {
		By tabItemBy = By.cssSelector("ul li." + tabCssClass + ">a");
		OOGraphene.waitElement(tabItemBy, browser);
		browser.findElement(tabItemBy).click();
		OOGraphene.waitElement(tabBy, browser);
		return this;
	}
}
