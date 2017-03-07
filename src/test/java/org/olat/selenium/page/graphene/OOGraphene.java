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
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.graphene.Graphene;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

/**
 * 
 * Initial date: 20.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OOGraphene {

	private static final long poolingDuration = 25;//ms
	private static final long waitTinyDuration = 15;//seconds

	private static final By closeBlueBoxButtonBy = By.cssSelector("div.o_alert_info div.o_sel_info_message a.o_alert_close.o_sel_info_close i.o_icon_close");
	private static final By closeModalDialogButtonBy = By.cssSelector("div.modal-dialog div.modal-header button.close");
	
	public static void waitModalDialog(WebDriver browser) {
		By modalBy = By.cssSelector("div.modal-dialog div.modal-body");
		waitElement(modalBy, 5, browser);
	}
	
	public static void waitCallout(WebDriver browser) {
		By calloutBy = By.cssSelector("div.popover-content div.o_callout_content");
		waitElement(calloutBy, 5, browser);
	}
	
	public static void waitBusy(WebDriver browser) {
		Graphene.waitModel(browser).pollingEvery(poolingDuration, TimeUnit.MILLISECONDS).until(new BusyPredicate());
	}
	
	public static void waitBusy(WebDriver browser, int timeoutInSeconds) {
		Graphene.waitModel(browser).withTimeout(timeoutInSeconds, TimeUnit.SECONDS)
			.pollingEvery(poolingDuration, TimeUnit.MILLISECONDS).until(new BusyPredicate());
	}
	
	public static void waitElement(By element, WebDriver browser) {
		Graphene.waitModel(browser)
			.pollingEvery(poolingDuration, TimeUnit.MILLISECONDS).until().element(element).is().visible();
	}
	
	/**
	 * Wait until the element is visible.
	 * 
	 * @param element The selector for the element
	 * @param timeoutInSeconds The timeout in seconds
	 * @param browser The web driver
	 */
	public static void waitElement(By element, int timeoutInSeconds, WebDriver browser) {
		Graphene.waitModel(browser).withTimeout(timeoutInSeconds, TimeUnit.SECONDS)
			.pollingEvery(poolingDuration, TimeUnit.MILLISECONDS).until().element(element).is().visible();
	}
	
	/**
	 * Wait until the element is present.
	 * 
	 * @param element The selector of the element
	 * @param timeoutInSeconds The timeout in seconds
	 * @param browser The web driver
	 */
	public static void waitElementDisappears(By element, int timeoutInSeconds, WebDriver browser) {
		Graphene.waitModel(browser).withTimeout(timeoutInSeconds, TimeUnit.SECONDS)
			.pollingEvery(poolingDuration, TimeUnit.MILLISECONDS).until().element(element).is().not().present();
	}
	
	/**
	 * Wait until the element is visible.
	 * 
	 * @param element The element to be visible
	 * @param browser The web driver
	 */
	public static void waitElement(WebElement element, WebDriver browser) {
		Graphene.waitModel(browser)
			.pollingEvery(poolingDuration, TimeUnit.MILLISECONDS).until().element(element).is().visible();
	}
	
	/**
	 * Wait until the element is visible.
	 * 
	 * @param element The element
	 * @param timeoutInSeconds The timeout in seconds
	 * @param browser The web driver
	 */
	public static void waitElement(WebElement element, int timeoutInSeconds, WebDriver browser) {
		Graphene.waitModel(browser).withTimeout(timeoutInSeconds, TimeUnit.SECONDS)
			.pollingEvery(poolingDuration, TimeUnit.MILLISECONDS).until().element(element).is().visible();
	}
	
	/**
	 * Wait until the element is present.
	 * 
	 * @param element The selector of the element
	 * @param browser The web driver
	 */
	public static void waitElementPresent(By element, WebDriver browser) {
		Graphene.waitModel(browser)
			.pollingEvery(poolingDuration, TimeUnit.MILLISECONDS).until().element(element).is().present();
	}
	
	/**
	 * Wait until the element is present.
	 * 
	 * @param element The selector of the element
	 * @param timeoutInSeconds The timeout in seconds
	 * @param browser The web driver
	 */
	public static void waitElementPresent(By element, int timeoutInSeconds, WebDriver browser) {
		Graphene.waitModel(browser).withTimeout(timeoutInSeconds, TimeUnit.SECONDS)
			.pollingEvery(poolingDuration, TimeUnit.MILLISECONDS).until().element(element).is().present();
	}
	
	public static void waitGui(WebDriver browser) {
		Graphene.waitGui(browser);
	}
	
	public static WebElement moveTo(WebElement element, WebDriver browser) {
		Actions actions = new Actions(browser);
		actions.moveToElement(element).perform();
		return element;
	}
	
	// top.tinymce.get('o_fi1000000416').setContent('<p>Hacked</p>');
	// <div id="o_fi1000000416_diw" class="o_richtext_mce"> <iframe id="o_fi1000000416_ifr">
	public static final void tinymce(String content, WebDriver browser) {
		Graphene.waitModel(browser).withTimeout(waitTinyDuration, TimeUnit.SECONDS)
			.pollingEvery(poolingDuration, TimeUnit.MILLISECONDS).until(new TinyMCELoadedPredicate());
		((JavascriptExecutor)browser).executeScript("top.tinymce.activeEditor.setContent('" + content + "')");
	}
	
	public static final void tinymce(String content, String containerCssSelector, WebDriver browser) {
		By tinyIdBy = By.cssSelector(containerCssSelector + " div.o_richtext_mce");
		waitElement(tinyIdBy, 5, browser);
		WebElement tinyIdEl = browser.findElement(tinyIdBy);
		String tinyId = tinyIdEl.getAttribute("id").replace("_diw", "");

		Graphene.waitModel(browser).withTimeout(waitTinyDuration, TimeUnit.SECONDS)
			.pollingEvery(poolingDuration, TimeUnit.MILLISECONDS).until(new TinyMCELoadedByIdPredicate(tinyId));
		((JavascriptExecutor)browser).executeScript("top.tinymce.editors['" + tinyId + "'].setContent('" + content + "')");
	}
	
	/**
	 * Make sure that the checkbox is in the correct state.
	 * @param checkboxEl
	 * @param val
	 */
	public static final void check(WebElement checkboxEl, Boolean val) {
		if(val == null) return;
		
		String checked = checkboxEl.getAttribute("checked");
		if(Boolean.TRUE.equals(val)) {
			if(checked == null) {
				checkboxEl.click();
			}
		} else {
			if(checked != null) {
				checkboxEl.click();
			}
		}
	}
	
	public static final void textarea(WebElement textareaEl, String content, WebDriver browser) {
		String id = textareaEl.getAttribute("id");
		((JavascriptExecutor)browser).executeScript("document.getElementById('" + id + "').value = '" + content + "'");
	}
	
	public static final void date(Date date, String seleniumCssClass, WebDriver browser) {
		Locale locale = getLocale(browser);
		String dateText = DateFormat.getDateInstance(DateFormat.SHORT, locale).format(date);
		By dateBy = By.cssSelector("div." + seleniumCssClass + " input.o_date_day");
		browser.findElement(dateBy).sendKeys(dateText);
	}
	
	public static final void datetime(Date date, String seleniumCssClass, WebDriver browser) {
		Locale locale = getLocale(browser);
		String dateText = DateFormat.getDateInstance(DateFormat.SHORT, locale).format(date);
		By dateBy = By.cssSelector("div." + seleniumCssClass + " input.o_date_day");
		browser.findElement(dateBy).sendKeys(dateText);
		
		By timeBy = By.cssSelector("div." + seleniumCssClass + " input.o_date_ms");
		List<WebElement> timeEls = browser.findElements(timeBy);
		Assert.assertNotNull(timeEls);
		Assert.assertEquals(2, timeEls.size());
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		timeEls.get(0).click();
		timeEls.get(0).clear();
		timeEls.get(0).sendKeys(Integer.toString(hour));
		timeEls.get(1).clear();
		timeEls.get(1).sendKeys(Integer.toString(minute));
	}
	
	public static final Locale getLocale(WebDriver browser) {
		String cssLanguage = browser.findElement(By.id("o_body")).getAttribute("class");
		if(cssLanguage.contains("o_lang_de")) {
			return Locale.GERMAN;
		}
		return Locale.ENGLISH;
	}
	
	/**
	 * Wait the end of the transition of the user's tools bar.
	 * @param browser
	 */
	public static final void waitingTransition(WebDriver browser) {
		Graphene.waitModel(browser).pollingEvery(poolingDuration, TimeUnit.MILLISECONDS).until(new TransitionPredicate());
		waitingALittleBit();
	}
	
	/**
	 * Wait 100ms
	 */
	public static final void waitingALittleBit() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Wait 0.5 second
	 */
	public static final void waitingALittleLonger() {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static final void uploadFile(By inputBy, File file, WebDriver browser) {
		WebElement input = browser.findElement(inputBy);
		input.sendKeys(file.getAbsolutePath());
	}
	
	public static final void waitScrollTop(WebDriver browser) {
		try {
			Graphene.waitModel(browser).ignoring(TimeoutException.class).pollingEvery(poolingDuration, TimeUnit.MILLISECONDS)
				.until(new ScrollToPredicate());
			waitingALittleBit();
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}
	
	public static final void waitAndCloseBlueMessageWindow(WebDriver browser) {
		try {
			Graphene.waitModel(browser).withTimeout(5, TimeUnit.SECONDS)
				.pollingEvery(poolingDuration, TimeUnit.MILLISECONDS).until().element(closeBlueBoxButtonBy).is().visible();
		} catch (Exception e) {
			//e.printStackTrace();
		}
		closeBlueMessageWindow(browser);
	}
	
	public static final void closeBlueMessageWindow(WebDriver browser) {
		List<WebElement> closeButtons = browser.findElements(closeBlueBoxButtonBy);
		for(WebElement closeButton:closeButtons) {
			if(closeButton.isDisplayed()) {
				try {
					clickCloseButton(browser, closeButton);
				} catch (TimeoutException e) {
					try {
						clickCloseButton(browser, closeButton);
					} catch(Exception e2) {
						//e.printStackTrace();
					}
				} catch(ElementNotVisibleException e1) {
					try {
						waitingALittleLonger();
						clickCloseButton(browser, closeButton);
					} catch(Exception e2) {
						//e2.printStackTrace();
					}
				}
			}
		}
	}
	
	private static final void clickCloseButton(WebDriver browser, WebElement closeButton) {
		closeButton.click();
		Graphene.waitModel(browser)
			.withTimeout(1000, TimeUnit.MILLISECONDS)
			.pollingEvery(poolingDuration, TimeUnit.MILLISECONDS)
			.until(new CloseAlertInfoPredicate());
	}
	
	public static final void closeModalDialogWindow(WebDriver browser) {
		List<WebElement> closeButtons = browser.findElements(closeModalDialogButtonBy);
		for(WebElement closeButton:closeButtons) {
			if(closeButton.isDisplayed()) {
				try {
					clickModalDialogCloseButton(browser, closeButton);
				} catch (TimeoutException e) {
					try {
						clickModalDialogCloseButton(browser, closeButton);
					} catch(Exception e2) {
						//
					}
				}
			}
		}
	}
	
	private static final void clickModalDialogCloseButton(WebDriver browser, WebElement closeButton) {
		try {
			closeButton.click();
			By dialogBy = By.cssSelector("div.modal-dialog");
			OOGraphene.waitElementDisappears(dialogBy, 2, browser);
		} catch (ElementNotVisibleException e) {
			//e.printStackTrace();
		}
	}
	
	public static final void closeOffCanvas(WebDriver browser) {
		By closeBy = By.cssSelector("a.o_offcanvas_close");
		WebElement closeButton = browser.findElement(closeBy);
		if(closeButton.isDisplayed()) {
			//timing issue if the close button is disappearing
			waitNavBarTransition(browser);
			if(closeButton.isDisplayed()) {
				try {
					closeButton.click();
					waitNavBarTransition(browser);
				} catch (Exception e) {
					//e.printStackTrace();
				}
			}
		}
	}
	
	public static final void waitNavBarTransition(WebDriver browser) {
		try {
			Graphene.waitModel(browser).pollingEvery(poolingDuration, TimeUnit.MILLISECONDS)
					.until(new NavBarTransitionPredicate());
			waitingALittleBit();
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}
}
