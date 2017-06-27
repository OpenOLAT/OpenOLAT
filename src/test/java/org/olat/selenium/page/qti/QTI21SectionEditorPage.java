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

import java.util.List;

import org.junit.Assert;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 26 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21SectionEditorPage {
	
	public static final By tabBarBy = By.cssSelector("ul.o_sel_assessment_section_config>li>a");
	
	private final WebDriver browser;
	
	public QTI21SectionEditorPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public QTI21SectionEditorPage selectExpertOptions() {
		By expertTabBy = By.cssSelector("fieldset.o_sel_assessment_section_expert_options");
		return selectTab(expertTabBy);
	}
	
	public QTI21SectionEditorPage sectionTitle(boolean visible) {
		By radioBy;
		if(visible) {
			radioBy = By.xpath("//div[contains(@class,'o_sel_assessment_section_visible')]//input[@type='radio'][@name='visible'][@value='y']");
		} else {
			radioBy = By.xpath("//div[contains(@class,'o_sel_assessment_section_visible')]//input[@type='radio'][@name='visible'][@value='n']");
		}
		browser.findElement(radioBy).click();
		return this;
	}
	
	public QTI21SectionEditorPage save() {
		By saveBy = By.cssSelector("fieldset.o_sel_assessment_section_expert_options button.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	protected QTI21SectionEditorPage selectTab(By tabBy) {
		List<WebElement> tabLinks = browser.findElements(tabBarBy);

		boolean found = false;
		a_a:
		for(WebElement tabLink:tabLinks) {
			tabLink.click();
			OOGraphene.waitBusy(browser);
			List<WebElement> tabEls = browser.findElements(tabBy);
			if(tabEls.size() > 0) {
				found = true;
				break a_a;
			}
		}

		Assert.assertTrue("Found the tab", found);
		return this;
	}

}
