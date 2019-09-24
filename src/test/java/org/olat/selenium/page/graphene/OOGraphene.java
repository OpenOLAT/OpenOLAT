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
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * 
 * Initial date: 20.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OOGraphene {
	
	private static final Logger log = Tracing.createLoggerFor(OOGraphene.class);

	private static final Duration poolingDuration = Duration.ofMillis(100);//ms
	private static final Duration waitTinyDuration = Duration.ofSeconds(50);//seconds
	private static final long driverTimeout = 60;//seconds
	private static final long movePause = 400;//milliseconds
	private static final long moveToPause = 100;//milliseconds
	
	private static final Duration polling = Duration.ofMillis(100);
	private static final Duration poolingSlow = Duration.ofMillis(200);
	private static final Duration poolingSlower = Duration.ofMillis(400);
	private static final Duration timeout = Duration.ofSeconds(5);

	private static final By closeBlueBoxButtonBy = By.cssSelector("div.o_alert_info div.o_sel_info_message a.o_alert_close.o_sel_info_close");
	
	public static final By wizardFooterBy = By.xpath("//div[contains(@class,'modal')]//div[contains(@class,'modal-footer')]");
	public static final By wizardNextBy = By.xpath("//div[contains(@class,'modal-footer')]//a[contains(@class,'o_wizard_button_next')]");
	public static final By wizardFinishBy = By.xpath("//div[contains(@class,'modal-footer')]//a[contains(@class,'o_wizard_button_finish') and not(contains(@class,'o_disabled'))]");
	
	
	public static FluentWait<WebDriver> wait(WebDriver browser) {
		return new WebDriverWait(browser, driverTimeout)
				.withTimeout(timeout).pollingEvery(poolingSlow);	
	}
	
	public static FluentWait<WebDriver> wait(WebDriver browser, Duration timeout) {
		return new WebDriverWait(browser, driverTimeout)
				.withTimeout(timeout).pollingEvery(poolingSlow);	
	}
	
	/**
	 * Wait until the busy flag is ok, the browser scrolled
	 * to the top and that the body of the modal dialog is visible.
	 * 
	 * @param The browser
	 */
	public static void waitModalDialog(WebDriver browser) {
		waitBusyAndScrollTop(browser);
		By modalBy = By.cssSelector("div.o_layered_panel div.modal-dialog div.modal-body");
		new WebDriverWait(browser, driverTimeout)
			.withTimeout(timeout).pollingEvery(poolingSlow)
			.until(ExpectedConditions.visibilityOfElementLocated(modalBy));
	}
	
	/**
	 * Wait until the busy flag is ok, the browser scrolled
	 * to the top and that the body of the modal dialog is visible.
	 * 
	 * @param The browser
	 */
	public static void waitModalWizard(WebDriver browser) {
		waitBusyAndScrollTop(browser);
		By modalBy = By.cssSelector("div.o_layered_panel div.modal-dialog div.modal-body");
		new WebDriverWait(browser, driverTimeout)
			.withTimeout(timeout).pollingEvery(poolingSlow)
			.until(ExpectedConditions.visibilityOfElementLocated(modalBy));
	}
	
	public static void waitModalDialogDisappears(WebDriver browser) {
		By modalBy = By.xpath("//div[not(@id='o_form_dirty_message')]/div[contains(@class,'modal-dialog')]/div[contains(@class,'modal-content')]");
		new WebDriverWait(browser, driverTimeout)
			.withTimeout(timeout).pollingEvery(poolingSlow)
			.until(ExpectedConditions.invisibilityOfElementLocated(modalBy));
	}
	
	public static void waitCallout(WebDriver browser) {
		By calloutBy = By.cssSelector("div.popover-content div.o_callout_content");
		waitElement(calloutBy, 5, browser);
	}
	
	public static void waitBusy(WebDriver browser) {
		new WebDriverWait(browser, driverTimeout)
			.withTimeout(timeout).pollingEvery(poolingDuration)
			.until(new BusyPredicate());
	}
	
	public static void waitBusy(WebDriver browser, int timeoutInSeconds) {
		new WebDriverWait(browser, driverTimeout)
			.withTimeout(Duration.ofSeconds(timeoutInSeconds)).pollingEvery(poolingDuration)
			.until(new BusyPredicate());
	}
	
	/**
	 * 
	 * @param element
	 * @param browser
	 */
	public static void waitElement(By element, WebDriver browser) {
		new WebDriverWait(browser, driverTimeout)
			.withTimeout(timeout).pollingEvery(polling)
			.until(ExpectedConditions.visibilityOfElementLocated(element));
	}
	
	public static void waitElementClickable(By element, WebDriver browser) {
		new WebDriverWait(browser, driverTimeout)
			.withTimeout(timeout).pollingEvery(polling)
			.until(ExpectedConditions.elementToBeClickable(element));
	}
	
	public static void waitElementClickable(WebElement element, WebDriver browser) {
		new WebDriverWait(browser, driverTimeout)
			.withTimeout(timeout).pollingEvery(polling)
			.until(ExpectedConditions.elementToBeClickable(element));
	}
	
	/**
	 * Wait until the element is visible.
	 * 
	 * @param element The selector for the element
	 * @param timeoutInSeconds The timeout in seconds
	 * @param browser The web driver
	 */
	public static void waitElement(By element, int timeoutInSeconds, WebDriver browser) {
		new WebDriverWait(browser, driverTimeout)
			.withTimeout(Duration.ofSeconds(timeoutInSeconds)).pollingEvery(polling)
			.until(ExpectedConditions.visibilityOfElementLocated(element));
	}
	
	/**
	 * Wait until the element is visible. But slowly poll if the
	 * element exists (every 333ms instead of 100ms)
	 * 
	 * @param element The selector for the element
	 * @param timeoutInSeconds The timeout in seconds
	 * @param browser The web driver
	 */
	public static void waitElementSlowly(By element, int timeoutInSeconds, WebDriver browser) {
		new WebDriverWait(browser, driverTimeout)
			.withTimeout(Duration.ofSeconds(timeoutInSeconds))
			.pollingEvery(poolingSlower)
			.until(ExpectedConditions.visibilityOfElementLocated(element));
	}
	
	/**
	 * Wait until the element is visible.
	 * 
	 * @param element The selector for the element
	 * @param timeoutInSeconds The timeout in seconds
	 * @param browser The web driver
	 */
	public static void waitElement(By element, int timeoutInSeconds, int pollingInSeconds, WebDriver browser) {
		new WebDriverWait(browser, driverTimeout)
			.withTimeout(Duration.ofSeconds(timeoutInSeconds)).pollingEvery(Duration.ofSeconds(pollingInSeconds))
			.until(ExpectedConditions.visibilityOfElementLocated(element));
	}
	
	/**
	 * Wait until the element is present in the DOM.
	 * 
	 * @param element
	 * @param timeoutInSeconds
	 * @param browser
	 */
	public static void waitElementPresence(By element, int timeoutInSeconds, WebDriver browser) {
		new WebDriverWait(browser, driverTimeout)
			.withTimeout(Duration.ofSeconds(timeoutInSeconds)).pollingEvery(polling)
			.until(ExpectedConditions.presenceOfElementLocated(element));
	}
	
	/**
	 * Wait until the element is not present.
	 * 
	 * @param element The selector of the element
	 * @param timeoutInSeconds The timeout in seconds
	 * @param browser The web driver
	 */
	public static void waitElementDisappears(By element, int timeoutInSeconds, WebDriver browser) {
		new WebDriverWait(browser, driverTimeout)
			.withTimeout(Duration.ofSeconds(timeoutInSeconds)).pollingEvery(poolingDuration)
			.until(ExpectedConditions.invisibilityOfElementLocated(element));
	}
	
	/**
	 * Wait until the element is not visible.
	 * 
	 * @param element
	 * @param timeoutInSeconds
	 * @param browser
	 */
	public static void waitElementUntilNotVisible(By element, int timeoutInSeconds, WebDriver browser) {
		new WebDriverWait(browser, driverTimeout)
			.withTimeout(Duration.ofSeconds(timeoutInSeconds)).pollingEvery(poolingDuration)
			.until(ExpectedConditions.invisibilityOfElementLocated(element));
	}
	
	public static void nextStep(WebDriver browser) {
		clickAndWait(wizardNextBy, browser);
	}
	
	public static void finishStep(WebDriver browser) {
		moveAndClick(wizardFinishBy, browser);
		closeBlueMessageWindow(browser);
		By wizardBy = By.cssSelector("div.o_layered_panel div.o_wizard");
		waitElementUntilNotVisible(wizardBy, 10, browser);
		waitBusyAndScrollTop(browser);
	}
	
	/**
	 * Verify the location of the button, scroll
	 * if needed and click. There is no wait of
	 * any sort. 
	 * 
	 * @param buttonBy The selector
	 * @param browser The browser
	 */
	public static void click(By buttonBy, WebDriver browser) {
		WebElement buttonEl = browser.findElement(buttonBy);
		boolean move = buttonEl.getLocation().getY() > 681;
		if(move) {
			scrollTo(buttonBy, browser);
		}
		browser.findElement(buttonBy).click();
	}
	
	/**
	 * Check the location of the button. If it's below the visible
	 * window, it scrolls to the button, waits a little longer and
	 * click it. After it wait until the window scroll to the top
	 * and/or make a classic waitBusy
	 * 
	 * @param buttonBy The button to click
	 * @param browser The driver
	 */
	public static void clickAndWait(By buttonBy, WebDriver browser) {
		WebElement buttonEl = browser.findElement(buttonBy);
		boolean move = buttonEl.getLocation().getY() > 669;
		if(move) {
			if(browser instanceof FirefoxDriver) {
				scrollTo(buttonBy, browser);
			}
			new Actions(browser)
				.moveToElement(buttonEl)
				.pause(movePause)
				.click(buttonEl)
				.perform();
			OOGraphene.waitBusyAndScrollTop(browser);
		} else {
			browser.findElement(buttonBy).click();
			OOGraphene.waitBusy(browser);
		}
	}
	
	/**
	 * Check the location of the button. If it's below the visible
	 * window, it scrolls to the button, waits a little longer and
	 * click it.
	 * 
	 * @param buttonBy The button to click
	 * @param browser The driver
	 */
	public static void moveAndClick(By buttonBy, WebDriver browser) {
		waitElement(buttonBy, browser);
		WebElement buttonEl = browser.findElement(buttonBy);
		boolean move = buttonEl.getLocation().getY() > 669;
		if(move) {
			if(browser instanceof FirefoxDriver) {
				scrollTo(buttonEl, browser);
			}
			new Actions(browser)
				.moveToElement(buttonEl)
				.pause(movePause)
				.click(buttonEl)
				.perform();
		} else {
			browser.findElement(buttonBy).click();
		}
	}
	
	/**
	 * Scroll to the element and wait a little longer.
	 * 
	 * @param by The selector
	 * @param browser The browser
	 */
	public static void scrollTo(By by, WebDriver browser) {
		WebElement el = browser.findElement(by);
		scrollTo(el, browser);
	}
	
	/**
	 * Scroll to the element and wait a little longer.
	 * 
	 * @param by The element
	 * @param browser The browser
	 */
	public static void scrollTo(WebElement element, WebDriver browser) {
		((JavascriptExecutor)browser).executeScript("return arguments[0].scrollIntoView({behavior:\"instant\", block: \"end\"});", element);
		OOGraphene.waitingALittleLonger();
	}
	
	public static void moveTo(By by, WebDriver browser) {
		waitElement(by, browser);
		WebElement el = browser.findElement(by);
		if(browser instanceof FirefoxDriver) {
			scrollTo(el, browser);
		}
		new Actions(browser)
			.moveToElement(el)
			.pause(moveToPause)
			.perform();
	}
	
	/**
	 * Scroll to the top anchor.
	 * 
	 * @param browser The browser
	 */
	public static void scrollTop(WebDriver browser) {
		WebElement el = browser.findElement(By.id("o_top"));
		((JavascriptExecutor)browser).executeScript("return arguments[0].scrollIntoView({behavior:\"instant\", block: \"end\"});", el);
		OOGraphene.waitingALittleLonger();
	}
	
	public static void moveTop(WebDriver browser) {
		By topBy = By.id("o_top");
		if(browser instanceof FirefoxDriver) {
			scrollTo(topBy, browser);
		}
		WebElement el = browser.findElement(topBy);
		new Actions(browser)
			.moveToElement(el)
			.pause(moveToPause)
			.perform();
	}
	
	public static final void waitTinymce(WebDriver browser) {
		new WebDriverWait(browser, driverTimeout).withTimeout(waitTinyDuration)
			.pollingEvery(poolingDuration)
			.until(new TinyMCELoadedPredicate());
	}
	
	// top.tinymce.get('o_fi1000000416').setContent('<p>Hacked</p>');
	// <div id="o_fi1000000416_diw" class="o_richtext_mce"> <iframe id="o_fi1000000416_ifr">
	public static final void tinymce(String content, WebDriver browser) {
		waitTinymce(browser);
		((JavascriptExecutor)browser).executeScript("top.tinymce.activeEditor.setContent('" + content + "')");
	}
	
	public static final void tinymceExec(String content, WebDriver browser) {
		waitTinymce(browser);
		((JavascriptExecutor)browser).executeScript("top.tinymce.activeEditor.execCommand('mceInsertRawHTML', true, '" + content + "')");
	}
	
	public static final void tinymce(String content, String containerCssSelector, WebDriver browser) {
		By tinyIdBy = By.cssSelector(containerCssSelector + " div.o_richtext_mce");
		waitElement(tinyIdBy, browser);
		WebElement tinyIdEl = browser.findElement(tinyIdBy);
		String tinyId = tinyIdEl.getAttribute("id").replace("_diw", "");

		new WebDriverWait(browser, driverTimeout).withTimeout(waitTinyDuration)
			.pollingEvery(poolingDuration)
			.until(new TinyMCELoadedByIdPredicate(tinyId));
		((JavascriptExecutor)browser).executeScript("top.tinymce.editors['" + tinyId + "'].setContent('" + content + "')");
	}
	
	/**
	 * Insert a piece of text in TinyMCE where is the caret.
	 * 
	 * @param content The text to add
	 * @param containerCssSelector A selector to point where the rich text editor is
	 * @param browser The browser
	 */
	public static final void tinymceInsert(String content, String containerCssSelector, WebDriver browser) {
		By tinyIdBy = By.cssSelector(containerCssSelector + " div.o_richtext_mce");
		waitElement(tinyIdBy, browser);
		WebElement tinyIdEl = browser.findElement(tinyIdBy);
		String tinyId = tinyIdEl.getAttribute("id").replace("_diw", "");

		new WebDriverWait(browser, driverTimeout).withTimeout(waitTinyDuration)
			.pollingEvery(poolingDuration)
			.until(new TinyMCELoadedByIdPredicate(tinyId));
		((JavascriptExecutor)browser).executeScript("top.tinymce.editors['" + tinyId + "'].insertContent('" + content + "')");
	}
	
	/**
	 * 
	 * @param tabsBy The selector for the tabs bar
	 * @param formBy The selector to found the form
	 * @param browser The browser
	 */
	public static final void selectTab(String ulClass, By formBy, WebDriver browser) {
		selectTab(ulClass, (b) -> {
			List<WebElement> chooseRepoEntry = browser.findElements(formBy);
			return !chooseRepoEntry.isEmpty();
		}, browser);
	}
	
	/**
	 * 
	 * @param ulClass The class of the nav-tabs
	 * @param selectTab A predicate to select the right tab
	 * @param browser The driver
	 */
	public static final void selectTab(String ulClass, Predicate<WebDriver> selectTab, WebDriver browser) {
		List<WebElement> tabLinks = browser.findElements(By.cssSelector("ul." + ulClass + ">li>a"));
		int count = tabLinks.size();
		boolean found = false;
		a_a:
		for(int i=0; i<count; i++) {
			By tabLinkBy = By.xpath("//ul[contains(@class,'" + ulClass + "')]/li[" + (i+1) + "]/a");
			WebElement tabEl = browser.findElement(tabLinkBy);
			String tabClass = tabEl.getAttribute("onclick");
			if(StringHelper.containsNonWhitespace(tabClass)) {
				tabEl.click();
				waitBusy(browser);
				By activatedTabLinkBy = By.xpath("//ul[contains(@class,'" + ulClass + "')]/li[" + (i+1) + "][@class='active']/a");
				waitElement(activatedTabLinkBy, browser);
				if(selectTab.test(browser)) {
					found = true;
					break a_a;
				}
			} else if(selectTab.test(browser)) {
				found = true;
				break a_a;
			}
		}

		Assert.assertTrue("Found the tab", found);
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
		
		By datePickerBy = By.id("ui-datepicker-div");
		waitElementDisappears(datePickerBy, 5, browser);
	}
	
	public static final void flexiTableSelectAll(WebDriver browser) {
		By selectAll = By.xpath("//div[contains(@class,'o_table_checkall')]/a[i[contains(@class,'o_icon_check_on')]]");
		waitElement(selectAll, browser);
		if(browser instanceof FirefoxDriver) {
			OOGraphene.waitingALittleLonger();// link is obscured by the scroll bar
		}
		browser.findElement(selectAll).click();
		waitBusy(browser);
		
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
		new WebDriverWait(browser, driverTimeout)
			.pollingEvery(poolingDuration)
			.until(new TransitionPredicate());
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
	
	/**
	 * Wait 5 seconds. Only use it if you lose all hopes.
	 */
	public static final void waitingTooLong() {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static final void uploadFile(By inputBy, File file, WebDriver browser) {
		WebElement input = browser.findElement(inputBy);
		input.sendKeys(file.getAbsolutePath());
	}
	
	/**
	 * This take longer than the standard busy wait because it waits the
	 * window is stabilized.
	 * 
	 * @param browser
	 */
	public static final void waitBusyAndScrollTop(WebDriver browser) {
		try {
			new WebDriverWait(browser, driverTimeout)
				.pollingEvery(poolingDuration)
				.withTimeout(timeout)
				.until(new BusyScrollToPredicate());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Click the "<" of the bread crumbs and wait.
	 * 
	 * @param browser The browser
	 */
	public static final void clickBreadcrumbBack(WebDriver browser) {
		By backBy = By.xpath("//ol[@class='breadcrumb']/li[@class='o_breadcrumb_back']/a[i[contains(@class,'o_icon_back')]]");
		waitElement(backBy, 10, browser);
		try {
			browser.findElement(backBy).click();
		} catch (StaleElementReferenceException e) {
			log.error("", e);
			waitingALittleLonger();
			browser.findElement(backBy).click();
		}
		waitBusy(browser);
	}
	
	public static final void closeErrorBox(WebDriver browser) {
		By errorBoxBy = By.cssSelector(".modal-body.alert.alert-danger");
		waitElement(errorBoxBy, browser);
		By closeButtonBy = By.xpath("//div[not(@id='o_form_dirty_message')]/div[contains(@class,'modal-dialog')]//button[@class='close']");
		waitElement(closeButtonBy, browser);
		browser.findElement(closeButtonBy).click();
		waitModalDialogDisappears(browser);
	}
	
	public static final void closeWarningBox(WebDriver browser) {
		By errorBoxBy = By.cssSelector(".modal-body.alert.alert-warning");
		waitElement(errorBoxBy, browser);
		By closeButtonBy = By.xpath("//div[not(@id='o_form_dirty_message')]/div[contains(@class,'modal-dialog')]//button[@class='close']");
		waitElement(closeButtonBy, browser);
		browser.findElement(closeButtonBy).click();
		waitModalDialogDisappears(browser);
	}
	
	public static final void waitAndCloseBlueMessageWindow(WebDriver browser) {
		try {
			new WebDriverWait(browser, driverTimeout)
				.withTimeout(timeout).pollingEvery(poolingDuration)
				.until(ExpectedConditions.visibilityOfElementLocated(closeBlueBoxButtonBy));
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
		new WebDriverWait(browser, driverTimeout)
			.withTimeout(Duration.ofMillis(1000)).pollingEvery(poolingDuration)
			.until(new CloseAlertInfoPredicate());
	}
	
	public static final void closeModalDialogWindow(WebDriver browser) {
		By closeModalDialogButtonBy = By.xpath("//div[not(@id='o_form_dirty_message')]/div[contains(@class,'modal-dialog')]//div[contains(@class,'modal-header')]/button[@class='close']");
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
			waitModalDialogDisappears(browser);
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
			new WebDriverWait(browser, driverTimeout).pollingEvery(poolingDuration)
					.until(new NavBarTransitionPredicate());
			waitingALittleBit();
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}
}
