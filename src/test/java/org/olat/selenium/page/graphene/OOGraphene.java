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
package org.olat.selenium.page.graphene;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 20.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OOGraphene {

	private static final long poolingDuration = 25;
	
	public static void waitBusy() {
		Graphene.waitModel().pollingEvery(poolingDuration, TimeUnit.MILLISECONDS).until(new BusyPredicate());
	}
	
	public static void waitElement(By element) {
		Graphene.waitModel().pollingEvery(poolingDuration, TimeUnit.MILLISECONDS).until().element(element).is().visible();
	}
	
	public static void waitElement(WebElement element) {
		Graphene.waitModel().pollingEvery(poolingDuration, TimeUnit.MILLISECONDS).until().element(element).is().visible();
	}
	
	public static final void tinymce(String content, WebDriver browser) {
		Graphene.waitModel().until(new TinyMCELoadedPredicate());
		((JavascriptExecutor)browser).executeScript("top.tinymce.activeEditor.setContent('" + content + "')");
	}
	
	public static final void waitingTransition() {
		Graphene.waitModel().pollingEvery(poolingDuration, TimeUnit.MILLISECONDS).until(new TransitionPredicate());
		waitingALittleBit();
	}
	
	public static final void waitingALittleBit() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static final void uploadFile(By inputBy, File file, WebDriver browser) {
		WebElement input = browser.findElement(inputBy);
		input.sendKeys(file.getAbsolutePath());
	}
	
	public static final void waitAndCloseBlueMessageWindow(WebDriver browser) {
		try {
			Thread.sleep(350);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		closeBlueMessageWindow(browser);
	}
	
	public static final void closeBlueMessageWindow(WebDriver browser) {
		By closeButtonBy = By.cssSelector("div.o_alert_info div.o_sel_info_message i.o_icon.o_icon_close");
		List<WebElement> closeButtons = browser.findElements(closeButtonBy);
		for(WebElement closeButton:closeButtons) {
			if(closeButton.isDisplayed()) {
				try {
					clickCloseButton(closeButton);
				} catch (TimeoutException e) {
					try {
						clickCloseButton(closeButton);
					} catch(Exception e2) {
						//
					}
				}
			}
		}
	}
	
	private static final void clickCloseButton(WebElement closeButton) {
		try {
			closeButton.click();
			Graphene.waitModel()
				.withTimeout(1000, TimeUnit.MILLISECONDS)
				.pollingEvery(poolingDuration, TimeUnit.MILLISECONDS)
				.until(new CloseAlertInfoPredicate());
		} catch (ElementNotVisibleException e) {
			//e.printStackTrace();
		}
	}
}
