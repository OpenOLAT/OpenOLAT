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
package org.olat.selenium.page.course;

import java.util.List;

import org.junit.Assert;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Attention! The browser instance is dependent of the location. Jump
 * to the iframe need a jump back to OpenOLAT.
 * 
 * 
 * Initial date: 21 août 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LTIPage {
	
	private WebDriver browser;
	
	public LTIPage(WebDriver browser) {
		this.browser = browser;
	}
	
	/**
	 * A successful start will switch the browser to the iframe. If you need
	 * to go back to the LMS, you need to switch back to the default content.
	 * 
	 * @return Itself
	 */
	public LTIPage start() {
		try {
			By startBy = By.xpath("//div[contains(@class,'o_button_group')]/a[contains(@onclick,'start')]");
			OOGraphene.waitElement(startBy, browser);
			browser.findElement(startBy).click();
			
			By iframeBy = By.cssSelector(".o_iframedisplay iframe");
			OOGraphene.waitElement(iframeBy, browser);
			OOGraphene.waitingALittleLonger();
			
			WebElement iframe = browser.findElement(iframeBy);
			browser = browser.switchTo().frame(iframe);
			
			By launchedBy = By.xpath("//p[contains(text(),'Launch Validated.')]");
			OOGraphene.waitElement(launchedBy, browser);
		} catch (Exception e) {
			OOGraphene.takeScreenshot("LTIPage_start", browser);

			throw e;
		}
		return this;
	}
	
	public LTIPage outcomeToolProvider() {
		By outcomeToolLinkBy = By.xpath("//a[contains(@href,'tool_provider_outcome.php')]");
		OOGraphene.waitElement(outcomeToolLinkBy, browser);
		browser.findElement(outcomeToolLinkBy).click();
		return this;
	}
	
	public LTIPage sendGrade(double grade) {
		By gradeBy = By.cssSelector("input[type=text][name=grade]");
		OOGraphene.waitElement(gradeBy, browser);
		browser.findElement(gradeBy).sendKeys(Double.toString(grade));
		
		By sendGradeBy = By.cssSelector("input[type=submit][value='Send Grade']");
		browser.findElement(sendGradeBy).click();
		
		By preBy = By.tagName("pre");
		OOGraphene.waitElement(preBy, browser);
		List<WebElement> preEls = browser.findElements(preBy);
		boolean success = true;
		for(WebElement preEl:preEls) {
			String text = preEl.getText();
			if(text.contains("<imsx_codeMajor>success</imsx_codeMajor>")) {
				success |= true;
			}
		}
		Assert.assertTrue(success);
		return this;
	}
	
	public LTIPage backToOpenOLAT() {
		browser = browser.switchTo().defaultContent();
		return this;
	}

}
