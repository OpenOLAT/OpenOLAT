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
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.support.ui.ExpectedCondition;
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

	private static final Duration waitTinyDuration = Duration.ofSeconds(20);
	private static final Duration driverTimeout = Duration.ofSeconds(60);
	
	private static final Duration polling = Duration.ofMillis(100);
	private static final Duration poolingSlow = Duration.ofMillis(200);
	private static final Duration poolingSlower = Duration.ofMillis(500);
	private static final Duration timeout = Duration.ofSeconds(5);

	private static final By closeBlueBoxButtonBy = By.cssSelector("div.o_alert_info div.o_sel_info_message a.o_alert_close.o_sel_info_close");
	
	public static final By wizardFooterBy = By.xpath("//dialog[contains(@class,'modal')]//div[contains(@class,'modal-footer')]");
	public static final By wizardNextBy = By.xpath("//div[contains(@class,'modal-footer')]//a[contains(@class,'o_wizard_button_next')]");
	public static final By wizardFinishBy = By.xpath("//div[contains(@class,'modal-footer')]//a[contains(@class,'o_wizard_button_finish') and not(contains(@class,'o_disabled'))]");
	
	
	public static FluentWait<WebDriver> wait(WebDriver browser) {
		return wait(browser, timeout);
	}
	
	public static FluentWait<WebDriver> wait(WebDriver browser, Duration timeoutDuration) {
		return new WebDriverWait(browser, driverTimeout)
				.withTimeout(timeoutDuration).pollingEvery(poolingSlow);	
	}
	
	/**
	 * Wait until the busy flag is ok, the browser scrolled
	 * to the top and that the body of the modal dialog is visible.
	 * 
	 * @param browser The browser
	 */
	public static void waitModalDialog(WebDriver browser) {
		waitBusy(browser);
		By modalBy = By.cssSelector("div.o_layered_panel dialog div.modal-body");
		new WebDriverWait(browser, driverTimeout)
			.withTimeout(timeout).pollingEvery(poolingSlow)
			.until(ExpectedConditions.visibilityOfElementLocated(modalBy));
	}
	
	public static void waitModalDialog(WebDriver browser, String additionalCssSelector) {
		waitBusy(browser);
		By modalBy = By.cssSelector("div.o_layered_panel dialog div.modal-body " + additionalCssSelector);
		new WebDriverWait(browser, driverTimeout)
			.withTimeout(timeout).pollingEvery(poolingSlow)
			.until(ExpectedConditions.visibilityOfElementLocated(modalBy));
	}
	
	/**
	 * Wait until the busy flag is ok, the browser scrolled
	 * to the top and that the body of the modal dialog is visible.
	 * 
	 * @param browser The browser
	 */
	public static void waitModalWizard(WebDriver browser) {
		waitBusy(browser);
		By modalBy = By.cssSelector("div.o_layered_panel dialog div.modal-body");
		new WebDriverWait(browser, driverTimeout)
			.withTimeout(timeout).pollingEvery(poolingSlow)
			.until(ExpectedConditions.visibilityOfElementLocated(modalBy));
	}
	
	public static void waitModalDialogDisappears(WebDriver browser) {
		waitModalDialogDisappears(browser, timeout);
	}
	
	public static void waitModalDialogDisappears(WebDriver browser, Duration timeoutDuration) {
		By modalBy = By.xpath("//dialog[contains(@class,'modal') and not(@id='o_form_dirty_message')][div/div[contains(@class,'modal-content')]]");			
		waitModalDialogDisappears(browser, timeoutDuration, modalBy);
	}
	
	public static void waitModalDialogWithFieldsetDisappears(WebDriver browser, String fieldsetCssClass) {
		By modalBy = By.xpath("//dialog[not(@id='o_form_dirty_message')]/div[contains(@class,'modal-dialog')]/div[contains(@class,'modal-content')]//fieldset[contains(@class,'" + fieldsetCssClass + "')]");
		waitModalDialogDisappears(browser, timeout, modalBy);
	}
	
	public static void waitModalDialogWithDivDisappears(WebDriver browser, String divCssClass) {
		By modalBy = By.xpath("//dialog[not(@id='o_form_dirty_message')]/div[contains(@class,'modal-dialog')]/div[contains(@class,'modal-content')]//div[contains(@class,'" + divCssClass + "')]");
		waitModalDialogDisappears(browser, timeout, modalBy);
	}
	
	private static void waitModalDialogDisappears(WebDriver browser, Duration timeoutDuration, By modalBy) {
		try {
			new WebDriverWait(browser, driverTimeout)
				.withTimeout(timeoutDuration).pollingEvery(poolingSlow)
				.until(absenceOfElementLocated(modalBy));
		} catch (Exception e) {
			OOGraphene.takeScreenshot("waitModalDialogDisappears", browser);
			throw e;
		}
	}
	
	/**
	 * Wait until the busy flag is ok, the browser scrolled
	 * to the top and that the body of the top modal dialog is
	 * visible. Top modal dialogs are a separate beast of the 
	 * modal and are use to show a dialog above TinyMCE, the rich
	 * text editor.
	 * 
	 * @param browser The browser
	 */
	public static void waitTopModalDialog(WebDriver browser) {
		waitBusyAndScrollTop(browser);
		By modalBy = By.cssSelector("div.o_ltop_modal_panel div.modal-dialog div.modal-body");
		new WebDriverWait(browser, driverTimeout)
			.withTimeout(timeout).pollingEvery(poolingSlower)
			.until(ExpectedConditions.visibilityOfElementLocated(modalBy));
	}
	
	public static void waitTopModalDialogDisappears(WebDriver browser) {
		By modalBy = By.xpath("//div[@class='o_ltop_modal_panel']//div[contains(@class,'modal-dialog')]/div[contains(@class,'modal-content')]");
		new WebDriverWait(browser, driverTimeout)
			.withTimeout(timeout).pollingEvery(poolingSlow)
			.until(ExpectedConditions.invisibilityOfElementLocated(modalBy));
	}
	
	public static void waitCallout(WebDriver browser) {
		By calloutBy = By.cssSelector("div.popover-content div.o_callout_content");
		waitElement(calloutBy, browser);
	}
	
	public static void waitCallout(WebDriver browser, String cssSelector) {
		By calloutBy = By.cssSelector("div.popover-content div.o_callout_content " + cssSelector);
		waitElement(calloutBy, browser);
	}
	
	public static void waitCalloutDisappears(WebDriver browser, String cssSelector) {
		By calloutBy = By.cssSelector("div.popover-content div.o_callout_content " + cssSelector);
		waitModalDialogDisappears(browser, Duration.ofSeconds(5), calloutBy);
	}
	
	public static void waitBusy(WebDriver browser) {
		waitBusy(browser, timeout);
	}
	
	public static void waitBusy(WebDriver browser, Duration timeoutDuration) {
		new WebDriverWait(browser, driverTimeout)
			.withTimeout(timeoutDuration)
			.pollingEvery(polling)
			.until(new BusyPredicate());
	}
	
	public static void waitUrl(WebDriver browser, String url) {
		new WebDriverWait(browser, driverTimeout)
			.withTimeout(Duration.ofSeconds(5))
			.pollingEvery(polling)
			.until(ExpectedConditions.urlToBe(url));
	}
	
	public static void waitUrlContains(WebDriver browser, String url) {
		new WebDriverWait(browser, driverTimeout)
			.withTimeout(Duration.ofSeconds(5))
			.pollingEvery(polling)
			.until(ExpectedConditions.urlContains(url));
	}
	
	public static void waitSpinnerDisappears(WebDriver browser) {
		By modalBy = By.xpath("//dialog[@id='o_ajax_busy']");
		new WebDriverWait(browser, driverTimeout)
			.withTimeout(timeout).pollingEvery(poolingSlow)
			.until(ExpectedConditions.invisibilityOfElementLocated(modalBy));
	}
	
	/**
	 * 
	 * @param element
	 * @param browser
	 */
	public static WebElement waitElement(By element, WebDriver browser) {
		return waitElement(element, timeout, polling, browser);
	}
	
	public static WebElement waitElementRefreshed(By element, WebDriver browser) {
		return waitElementRefreshed(element, timeout, polling, browser);
	}
	
	public static void waitElementClickable(By element, WebDriver browser) {
		new WebDriverWait(browser, driverTimeout)
			.withTimeout(timeout)
			.pollingEvery(polling)
			.until(ExpectedConditions.elementToBeClickable(element));
	}
	
	/**
	 * Wait until the element has an opacity of 1 or null.
	 * 
	 * @param locator The location of the element
	 * @param browser The web driver
	 */
	public static void waitElementFullOpacity(By locator, WebDriver browser) {
		new WebDriverWait(browser, driverTimeout)
			.withTimeout(timeout)
			.pollingEvery(polling)
			.until(new ExpectedCondition<Boolean>() {
				@Override
				public Boolean apply(WebDriver webDriver) {
			        List<WebElement> elements = webDriver.findElements(locator);
			        if(elements != null && elements.size() == 1) {
			        	String opacity = elements.get(0).getCssValue("opacity");
			        	return opacity == null || opacity.equals("1");
			        }
					return Boolean.FALSE;
				}
			});
	}

	/**
	 * Wait until the element is visible.
	 * 
	 * @param element The selector for the element
	 * @param timeoutInSeconds The timeout in seconds
	 * @param browser The web driver
	 */
	public static WebElement waitElement(By element, long timeoutInSeconds, WebDriver browser) {
		return waitElement(element, Duration.ofSeconds(timeoutInSeconds), polling, browser);
	}
	
	/**
	 * Wait until the element is visible. But slowly poll if the
	 * element exists (every 333ms instead of 100ms)
	 * 
	 * @param element The selector for the element
	 * @param timeoutInSeconds The timeout in seconds
	 * @param browser The web driver
	 */
	public static WebElement waitElementSlowly(By element, long timeoutInSeconds, WebDriver browser) {
		return waitElement(element, Duration.ofSeconds(timeoutInSeconds), poolingSlower, browser);
	}
	
	/**
	 * Wait until the element is present in DOM but it doesn't mean the element is visible.
	 * 
	 * @param element The selector for the element
	 * @param timeoutInSeconds The timeout in seconds
	 * @param browser The web driver
	 */
	public static void waitElementPresence(By element, int timeoutInSeconds, WebDriver browser) {
		waitElementPresence(element, Duration.ofSeconds(timeoutInSeconds), polling, browser);
	}
	
	/**
	 * Wait slowly until the element is present in DOM but it doesn't mean the element is visible.
	 * 
	 * @param element The selector for the element
	 * @param timeoutInSeconds The timeout in seconds
	 * @param browser The web driver
	 */
	public static void waitElementPresenceSlowly(By element, int timeoutInSeconds, WebDriver browser) {
		new WebDriverWait(browser, driverTimeout)
			.withTimeout(Duration.ofSeconds(timeoutInSeconds)).pollingEvery(poolingSlower)
			.until(ExpectedConditions.presenceOfElementLocated(element));
	}
	
	/**
	 * Wait until the element is present in DOM but it doesn't mean the element is visible.
	 * 
	 * @param element The selector for the element
	 * @param timeoutInSeconds The timeout
	 * @param pollingDuration The polling duration
	 * @param browser The web driver
	 */
	public static WebElement waitElement(By element, Duration timeoutDuration, Duration pollingDuration, WebDriver browser) {
		return new WebDriverWait(browser, driverTimeout)
			.withTimeout(timeoutDuration).pollingEvery(pollingDuration)
			.until(ExpectedConditions.visibilityOfElementLocated(element));
	}
	
	public static WebElement waitElementRefreshed(By element, Duration timeoutDuration, Duration pollingDuration, WebDriver browser) {
		return new WebDriverWait(browser, driverTimeout)
			.withTimeout(timeoutDuration).pollingEvery(pollingDuration)
			.until(ExpectedConditions.refreshed(ExpectedConditions.visibilityOfElementLocated(element)));
	}
	
	/**
	 * Wait until the element is present in the DOM.
	 * 
	 * @param element
	 * @param timeoutInSeconds
	 * @param browser
	 */
	public static void waitElementPresence(By element, Duration timeoutDuration, Duration pollingDuration, WebDriver browser) {
		new WebDriverWait(browser, driverTimeout)
			.withTimeout(timeoutDuration).pollingEvery(pollingDuration)
			.until(ExpectedConditions.presenceOfElementLocated(element));
	}
	
	public static void waitElementWithScrollTableRight(By tableElement, By element, Duration timeoutDuration, Duration pollingDuration, WebDriver browser) {
		new WebDriverWait(browser, driverTimeout)
			.withTimeout(timeoutDuration).pollingEvery(pollingDuration)
			.until(elementLocatedRight(tableElement, element));
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
			.withTimeout(Duration.ofSeconds(timeoutInSeconds)).pollingEvery(polling)
			.until(ExpectedConditions.invisibilityOfElementLocated(element));
	}
	
	public static void waitElementSlowlyDisappears(By element, int timeoutInSeconds, WebDriver browser) {
		new WebDriverWait(browser, driverTimeout)
			.withTimeout(Duration.ofSeconds(timeoutInSeconds)).pollingEvery(poolingSlower)
			.until(ExpectedConditions.invisibilityOfElementLocated(element));
	}
	
	public static void waitElementAbsence(By element, int timeoutInSeconds, WebDriver browser) {
		new WebDriverWait(browser, driverTimeout)
			.withTimeout(Duration.ofSeconds(timeoutInSeconds)).pollingEvery(polling)
			.until(absenceOfElementLocated(element));
	}
	
	public static void nextStep(WebDriver browser) {
		OOGraphene.waitElement(wizardNextBy, browser).click();
		waitBusy(browser);
	}
	

	public static void finishStep(WebDriver browser) {
		finishStep(browser, true);
	}
	
	public static void finishStep(WebDriver browser, boolean closeBlueMessage) {
		OOGraphene.waitElement(wizardFinishBy, browser).click();
		if(closeBlueMessage) {
			closeBlueMessageWindow(browser);
		}
		By wizardBy = By.cssSelector("div.o_layered_panel dialog.o_wizard");
		waitElementDisappears(wizardBy, 10, browser);
		waitBusyAndScrollTop(browser);
	}
	
	/**
	 * Utility method to clear and fill an input field. If the driver send a stale
	 * element exception, the method will wait a little and try a second time.
	 * 
	 * @param by The selector
	 * @param text The text to inject in the text field
	 * @param browser The web driver
	 */
	public static void clearAndSendKeys(By by, String text, WebDriver browser) {
		try {
			waitElementRefreshed(by, browser).clear();
			waitElementRefreshed(by, browser).sendKeys(text);
		} catch (StaleElementReferenceException e) {
			log.warn("", e);
			waitingALittleBit();
			browser.findElement(by).clear();
			waitingALittleBit();
			browser.findElement(by).sendKeys(text);
		}
	}
	
	public static void tab(WebDriver browser) {
		try {
			new Actions(browser)
				.sendKeys(Keys.TAB)
				.perform();
		} catch (Exception e) {
			log.warn("", e);
		}
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
		boolean move = getLocationY(buttonBy, browser) > 681;
		if(move) {
			scrollBottom(buttonBy, browser);
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
		boolean move = getLocationY(buttonBy, browser) > 669;
		if(move) {
			scrollBottom(buttonBy, browser);
			browser.findElement(buttonBy).click();
			waitBusyAndScrollTop(browser);
		} else {
			browser.findElement(buttonBy).click();
			waitBusy(browser);
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
		waitElementPresence(buttonBy, 5, browser);
		boolean move = getLocationY(buttonBy, browser) > 669;
		if(move) {
			scrollBottom(buttonBy, browser);
			waitElement(buttonBy, browser);
		}
		browser.findElement(buttonBy).click();
	}
	
	/**
	 * To prevent random stale element exception, try a second time.
	 * 
	 * @param by The selector to find the element
	 * @param browser The web driver
	 * @return The location Y
	 */
	private static int getLocationY(By by, WebDriver browser) {
		int y = 0;
		try {
			y = browser.findElement(by).getLocation().getY();
		} catch (StaleElementReferenceException e) {
			log.warn("", e);
			waitingALittleBit();
			y = browser.findElement(by).getLocation().getY();
		}
		return y;
	}
	
	/**
	 * Scroll 1024 to the bottom (dialog or window) and wait a little longer.
	 * 
	 * @param by The selector to find if the element is in a dialog.
	 * @param browser The browser
	 */
	public static void scrollBottom(By by, WebDriver browser) {
		String scrollBottom = """
				var modal = arguments[0].closest('dialog.dialog .modal-body')?.scrollTo(0, 1024);\n
				if(modal === undefined) { window.scrollTo(0, 1024); }
				""";
		executeScript(browser, by, scrollBottom);
	}
	
	/**
	 * Scroll 1024 to the right (table or window) and wait a little longer.
	 * 
	 * @param by The selector to find if the element is in a scrollable table.
	 * @param browser The browser
	 */
	public static void scrollTableRight(By by, WebDriver browser) {
		String scrollBottom = """
				var modal = arguments[0].closest('.o_scrollable')?.scrollTo(1024, 0);\n
				if(modal === undefined) { window.scrollTo(1024, 0); }
				""";
		executeScript(browser, by, scrollBottom);
	}
	
	/**
	 * Scroll to the top.
	 * 
	 * @param browser The browser
	 */
	public static void scrollTop(WebDriver browser) {
		((JavascriptExecutor)browser).executeScript("window.scrollTo(0, 0);");
		OOGraphene.waitingALittleLonger();
	}
	

	
	/**
	 * Scroll an element into the view. Doesn't work in a dialog!
	 * @param by The selector of the element to move into view.
	 * @param browser The browser
	 */
	public static void scrollTo(By by, WebDriver browser) {
		executeScript(browser, by, "return arguments[0].scrollIntoView({behavior:\"auto\", block: \"end\"});");
	}
	
	private static void executeScript(WebDriver browser, By by, String script) {
		try {
			WebElement element = browser.findElement(by);
			((JavascriptExecutor)browser).executeScript(script, element);
			OOGraphene.waitingALittleLonger();
		} catch (StaleElementReferenceException e) {
			waitingALittleBit();
			WebElement element = browser.findElement(by);
			((JavascriptExecutor)browser).executeScript(script, element);
			waitingALittleLonger();
		}
	}
	
	public static final void waitTinymce(WebDriver browser) {
		new WebDriverWait(browser, driverTimeout).withTimeout(waitTinyDuration)
			.pollingEvery(poolingSlow)
			.until(new TinyMCELoadedPredicate());
	}
	
	// tinymce.get('o_fi1000000416').setContent('<p>Hacked</p>');
	// <div id="o_fi1000000416_diw" class="o_richtext_mce"> <iframe id="o_fi1000000416_ifr">
	public static final void tinymce(String content, WebDriver browser) {
		waitTinymce(browser);
		((JavascriptExecutor)browser).executeScript("tinymce.activeEditor.setContent('" + content + "')");
	}
	
	public static final void tinymceExec(String content, WebDriver browser) {
		waitTinymce(browser);
		((JavascriptExecutor)browser).executeScript("tinymce.activeEditor.execCommand('mceSetContent', true, '" + content + "')");
	}
	
	public static final void tinymce(String content, String containerCssSelector, WebDriver browser) {
		waitTinymce(containerCssSelector, browser);
		
		By tinyIdBy = By.cssSelector(containerCssSelector + " div.o_richtext_mce");
		WebElement tinyIdEl = browser.findElement(tinyIdBy);
		String tinyId = tinyIdEl.getDomAttribute("id").replace("_diw", "");
		waitTinymceById(tinyId, browser);
		((JavascriptExecutor)browser).executeScript("tinymce.activeEditor.editorManager.get('" + tinyId + "').setContent('" + content + "')");
	}
	
	/**
	 * Insert a piece of text in TinyMCE where is the caret.
	 * 
	 * @param content The text to add
	 * @param containerCssSelector A selector to point where the rich text editor is
	 * @param browser The browser
	 */
	public static final void tinymceInsert(String content, String containerCssSelector, WebDriver browser) {
		waitTinymce(containerCssSelector, browser);
		
		By tinyIdBy = By.cssSelector(containerCssSelector + " div.o_richtext_mce");
		WebElement tinyIdEl = browser.findElement(tinyIdBy);
		String tinyId = tinyIdEl.getDomAttribute("id").replace("_diw", "");
		waitTinymceById(tinyId, browser);
		((JavascriptExecutor)browser).executeScript("tinymce.activeEditor.editorManager.get('" + tinyId + "').insertContent('" + content + "')");
	}
	
	/**
	 * Wait until the iframe where the editing happens exists.
	 * 
	 * @param containerCssSelector The container of the textarea element
	 * @param browser The browser
	 */
	private static final void waitTinymce(String containerCssSelector, WebDriver browser) {
		waitElement(By.cssSelector(containerCssSelector + " div.o_richtext_mce div.tox-edit-area>iframe"), browser);
	}

	/**
	 * Check that the TinyMCE editor with the specified id is initialized.
	 * 
	 * @param tinyId The id of the element
	 * @param browser The browser
	 */
	private static final void waitTinymceById(String tinyId, WebDriver browser) {
		new WebDriverWait(browser, driverTimeout).withTimeout(waitTinyDuration)
			.pollingEvery(polling)
			.until(new TinyMCELoadedByIdPredicate(tinyId));
	}
	
	public static void markdown(By by, String text, WebDriver browser) {
		By milkdownBy = By.cssSelector("div.milkdown div.ProseMirror.editor[contenteditable=true]");
		waitElement(milkdownBy, browser);
		
		String insert = """
				arguments[0].querySelector("input[type='hidden']").setAttribute("value",arguments[1]);\n
				arguments[0].querySelector("div.milkdown>div.editor p").append(arguments[1]);
				""";
		WebElement element = browser.findElement(by);
		((JavascriptExecutor)browser).executeScript(insert, element, text);
	}
	
	/**
	 * 
	 * @param ulClass The selector for the tabs bar
	 * @param formBy The selector to found the form
	 * @param browser The browser
	 */
	public static final void selectTab(String ulClass, By formBy, WebDriver browser) {
		selectTab(ulClass, (b) -> {
			List<WebElement> chooseRepoEntry = browser.findElements(formBy);
			return !chooseRepoEntry.isEmpty();
		}, false, browser);
	}
	
	/**
	 * 
	 * @param ulClass The class of the nav-tabs
	 * @param selectTab A predicate to select the right tab
	 * @param browser The driver
	 */
	private static final void selectTab(String ulClass, Predicate<WebDriver> selectTab, boolean slowly, WebDriver browser) {
		OOGraphene.waitElement(By.cssSelector("ul." + ulClass), browser);
		List<WebElement> tabLinks = browser.findElements(By.cssSelector("ul." + ulClass + ">li"));
		int count = tabLinks.size();
		
		int activeIndex = 0;
		for(int i=0; i<count;i++) {
			String cssClass = tabLinks.get(i).getDomAttribute("class");
			if(cssClass != null && cssClass.contains("active")) {
				activeIndex = i;
			}
		}
		
		boolean found;
		if(activeIndex == 0) {
			found = selectTab(ulClass, selectTab, 0, count, slowly, browser);
		} else {
			found = selectTab(ulClass, selectTab, activeIndex, count, slowly, browser);
			if(!found) {
				found = selectTab(ulClass, selectTab, 0, activeIndex, slowly, browser);
			}
		}
		Assert.assertTrue("Found the tab", found);
	}
	
	private static final boolean selectTab(String ulClass, Predicate<WebDriver> selectTab, int start, int end, boolean slowly, WebDriver browser) {
		boolean found = false;
		
		a_a:
		for(int i=start; i<end; i++) {
			By tabLinkBy = By.xpath("//ul[contains(@class,'" + ulClass + "')]/li[" + (i+1) + "]/a");
			WebElement tabEl = browser.findElement(tabLinkBy);
			String tabClass = tabEl.getDomAttribute("onclick");
			if(StringHelper.containsNonWhitespace(tabClass)) {
				tabEl.click();
				By activatedTabLinkBy = By.xpath("//ul[contains(@class,'" + ulClass + "')]/li[" + (i+1) + "][contains(@class,'active')]/a");
				if(slowly) {
					waitElementSlowly(activatedTabLinkBy, 10, browser);
				} else {
					waitElement(activatedTabLinkBy, browser);
				}
				
				if(selectTab.test(browser)) {
					found = true;
					break a_a;
				}
			} else if(selectTab.test(browser)) {
				found = true;
				break a_a;
			}
		}
		
		return found;
	}
	
	/**
	 * Make sure that the checkbox is in the correct state. The method
	 * search the attribute/property "checked".
	 * 
	 * @param checkboxEl The input element of the check box
	 * @param val The value (true/false) (mandatory)
	 */
	public static final boolean check(WebElement checkboxEl, Boolean val) {
		if(val == null) return false;
		
		boolean valueChanged = false;
		String checked = checkboxEl.getDomProperty("checked");
		if(checked == null) {
			checked = checkboxEl.getDomAttribute("checked");
		}
		if(Boolean.TRUE.equals(val)) {
			if(checked == null || "false".equalsIgnoreCase(checked)) {
				checkboxEl.click();
				valueChanged = true;
			}
		} else {
			if(checked != null && ("true".equalsIgnoreCase(checked) || "checked".equalsIgnoreCase(checked))) {
				checkboxEl.click();
				valueChanged = true;
			}
		}
		return valueChanged;
	}
	
	/**
	 * 
	 * @param cssSelector The CSS selector of the toggle button (extra CSS rule will be added)
	 * @param on true if the toggle must be set to on
	 * @param waitChanges Wait that the changes are done
	 * @param browser The browser
	 * @return true if the button was effectively toggled, false if the button was in the right state already
	 */
	public static final boolean toggle(String cssSelector, boolean on, boolean waitChanges, WebDriver browser) {
		// To enabled it, need a button in status false and turn it in true
		String checked = on ? "false" : "true";
		By toggleBy = By.cssSelector(cssSelector + "[aria-checked=" + checked + "]");
		List<WebElement> toggleEls = browser.findElements(toggleBy);
		boolean toggle = toggleEls.size() == 1;
		if(toggle) {
			browser.findElement(toggleBy).click();
			
			if(waitChanges) {
				By newStateBy = By.cssSelector(cssSelector + "[aria-checked=" + (on ?  "true" : "false") + "]");
				OOGraphene.waitElement(newStateBy, browser);
			}
		}
		return toggle;
	}
	
	public static final void textarea(WebElement textareaEl, String content, WebDriver browser) {
		String id = textareaEl.getDomAttribute("id");
		((JavascriptExecutor)browser).executeScript("document.getElementById('" + id + "').value = '" + content + "'");
	}
	
	/**
	 * Inject the formatted date as date in the input field ignoring
	 * the date chooser. The date chooser will not be closed by this
	 * method.
	 * 
	 * @param date The date to enter
	 * @param seleniumCssClass The CSS class wrapping the input field
	 * @param browser The browser
	 */
	public static final void date(Date date, String seleniumCssClass, WebDriver browser) {
		Locale locale = getLocale(browser);
		String dateText = formatDate(date, locale);
		By dateBy = By.cssSelector("div." + seleniumCssClass + " input.o_date_day");
		browser.findElement(dateBy).sendKeys(dateText);
	}
	
	public static String formatDate(Date date, Locale locale) {
		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, locale);
		df.setLenient(false);
		if (df instanceof SimpleDateFormat sdf) {
			// by default year has only two digits, however most people prefer a four digits year, even in short format
			String pattern = sdf.toPattern().replaceAll("y+","yyyy");
			sdf.applyPattern(pattern); 
		}
		return df.format(date);
	}
	
	/**
	 * Select the next month in the jQuery UI (need to be open).
	 * 
	 * @param browser The browser
	 */
	public static final void selectPreviousMonthInDatePicker(WebDriver browser) {
		try {
			By nextBy = By.cssSelector("div.datepicker-dropdown.active div.datepicker-header button.prev-button");
			waitElement(nextBy, browser).click();
			waitingALittleBit();
			waitElement(nextBy, browser);
		} catch (Exception e) {
			takeScreenshot("Select next month", browser);
			throw e;
		}
	}
	
	/**
	 * Select the next month in the jQuery UI (need to be open).
	 * 
	 * @param browser The browser
	 */
	public static final void selectNextMonthInDatePicker(WebDriver browser) {
		try {
			By nextBy = By.cssSelector("div.datepicker-dropdown.active div.datepicker-header button.next-button");
			waitElement(nextBy, browser).click();
			waitingALittleBit();
			waitElement(nextBy, browser);
		} catch (Exception e) {
			takeScreenshot("Select next month", browser);
			throw e;
		}
	}
	
	/**
	 * Select the day in the jQuery UI date picker (need to be open).
	 * 
	 * @param day The day
	 * @param browser The browser
	 */
	public static final void selectDayInDatePicker(int day, WebDriver browser) {
		try {
			By datePickerBy = By.cssSelector("div.datepicker-dropdown.active");
			waitElement(datePickerBy, browser);
			
			By dayBy = By.xpath("//div[contains(@class,'datepicker-dropdown')][contains(@class,'active')]//div[@class='days']//span[normalize-space(text())='" + day + "'][not(contains(@class,'prev'))]");
			waitElement(dayBy, browser).click();
			
			waitElementDisappears(datePickerBy, 5, browser);
		} catch (Exception e) {
			takeScreenshot("Select day in date picker", browser);
			throw e;
		}
	}
	
	public static final void flexiTableSelectAll(WebDriver browser) {
		By selectAll = By.xpath("//th[contains(@class,'o_table_checkall')]/a[i[contains(@class,'o_icon_check_off')]]");
		waitElement(selectAll, browser).click();
		waitBusy(browser);
		By selectedAll = By.xpath("//th[contains(@class,'o_table_checkall')]/a[i[contains(@class,'o_icon_check_on')]]");
		waitElement(selectedAll, browser);
	}
	
	public static final Locale getLocale(WebDriver browser) {
		String cssLanguage = browser.findElement(By.id("o_body")).getDomAttribute("class");
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
			.pollingEvery(polling)
			.until(new TransitionPredicate());
		waitingALittleBit();
	}

	private static final void waiting(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Wait 100ms
	 */
	public static final void waitingALittleBit() {
		waiting(100);
	}
	
	/**
	 * Wait 750ms second
	 */
	public static final void waitingALittleLonger() {
		waiting(750);
	}
	
	/**
	 * Wait 2 seconds.
	 */
	public static final void waitingLong() {
		waiting(2000);
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
		BusyScrollToPredicate predicate = new BusyScrollToPredicate();
		try {
			new WebDriverWait(browser, driverTimeout)
				.pollingEvery(polling)
				.withTimeout(timeout)
				.until(predicate);
		} catch (Exception e) {
			log.error("Predicate failed: {}", predicate.getY(), e);
			OOGraphene.takeScreenshot("waitBusyAndScrollTop", browser);
		}
	}
	
	/**
	 * Click the "<" of the bread crumbs and wait.
	 * 
	 * @param browser The browser
	 */
	public static final void clickBreadcrumbBack(WebDriver browser) {
		By backBy = By.xpath("//ol[@class='breadcrumb']/li[@class='o_breadcrumb_back']/a[i[contains(@class,'o_icon_back')]]");
		try {
			waitElement(backBy, 10, browser).click();
		} catch (StaleElementReferenceException e) {
			log.error("", e);
			waitingALittleLonger();
			browser.findElement(backBy).click();
		}
		waitBusy(browser);
	}
	
	/**
	 * Useful method to close error messages.
	 * 
	 * @param browser The browser
	 */
	public static final void closeErrorBox(WebDriver browser) {
		By errorBoxBy = By.cssSelector("#myFunctionalModal .modal-body.alert.alert-danger");
		waitElement(errorBoxBy, browser);
		By closeButtonBy = By.cssSelector("#myFunctionalModal .modal-header button.close");
		browser.findElement(closeButtonBy).click();
		waitModalDialogDisappears(browser);
	}

	/**
	 * Useful method to close warning messages.
	 * 
	 * @param browser The browser
	 */
	public static final void assertAndCloseWarningBox(WebDriver browser) {
		By warningBy = By.xpath("//dialog[@id='myFunctionalModal'][contains(@class,'in')]//div[contains(@class,'modal-body')][contains(@class,'alert-warning')]/p");
		OOGraphene.waitElement(warningBy, browser);
		OOGraphene.waitingALittleLonger();
		try {
			By closeBy = By.cssSelector("#myFunctionalModal .modal-header button.close");
			browser.findElement(closeBy).click();
			OOGraphene.waitModalDialogWithDivDisappears(browser, "alert-warning");
		} catch (Exception e) {
			log.error("Wait until warning disappears");
			throw e;
		}
	}
	
	public static final void waitAndCloseBlueMessageWindow(WebDriver browser) {
		try {
			new WebDriverWait(browser, driverTimeout)
				.withTimeout(timeout).pollingEvery(polling)
				.until(ExpectedConditions.visibilityOfElementLocated(closeBlueBoxButtonBy));
		} catch (Exception e) {
			log.warn("Wait blue box, but it doesn't appear", e);
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
						e.printStackTrace();
					}
				} catch(Exception e1) {
					try {
						waitingALittleLonger();
						clickCloseButton(browser, closeButton);
					} catch(Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		}
	}
	
	private static final void clickCloseButton(WebDriver browser, WebElement closeButton) {
		closeButton.click();
		new WebDriverWait(browser, driverTimeout)
			.withTimeout(Duration.ofMillis(1000)).pollingEvery(polling)
			.until(new CloseAlertInfoPredicate());
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
			new WebDriverWait(browser, driverTimeout).pollingEvery(polling)
					.until(new NavBarTransitionPredicate());
			waitingALittleBit();
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}
	
	public static void takeScreenshot(String test, WebDriver browser) {
		TakesScreenshot scrShot = ((TakesScreenshot)browser);
		File screenFile = scrShot.getScreenshotAs(org.openqa.selenium.OutputType.FILE);
		
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
		String filename = test + "" + format.format(new Date()) + ".jpg";
		File path = new File("screenshots");
		if(!path.exists() && !path.mkdirs()) {
			path = new File(System.getProperty("java.io.tmpdir"), "screenshots");
			path.mkdirs();
		}
		File screenshotFile = new File(path, filename);
		log.error("Write screenshot: {} {}", test, screenshotFile);
		FileUtils.copyFileToFile(screenFile, screenshotFile, true);
		
		log.error("Take screen shots: {}", test);
	}
	
	public static byte[] takeScreenshotInMemory(WebDriver browser) {
		log.info("Take screen shot in memory.");
		TakesScreenshot scrShot = ((TakesScreenshot)browser);
		return scrShot.getScreenshotAs(org.openqa.selenium.OutputType.BYTES);
	}
	
	public static void logs(WebDriver browser) {
		logs(browser, LogType.BROWSER);
		logs(browser, LogType.DRIVER);
	}
	
	public static void logs(WebDriver browser, String logType) {
		try {
			LogEntries logEntries = browser.manage().logs().get(logType);
			for (LogEntry logEntry : logEntries) {
				java.util.logging.Level level = logEntry.getLevel();
				log.error("{} {}", level.getName(), logEntry.getMessage());
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	public static ExpectedCondition<WebElement> elementLocatedRight(final By tableLocator, final By locator) {
		
		return new ExpectedCondition<>() {
			@Override
			public WebElement apply(WebDriver driver) {
				try {
					OOGraphene.scrollTableRight(tableLocator, driver);
					WebElement element = driver.findElement(locator);
					return element.isDisplayed() ? element : null;
				} catch (StaleElementReferenceException e) {
					return null;
				}
			}

			@Override
			public String toString() {
				return "element to not being present after scroll right: " + locator;
			}
		};
	}
	
	public static ExpectedCondition<Boolean> absenceOfElementLocated(final By locator) {
		return new ExpectedCondition<>() {
			@Override
			public Boolean apply(WebDriver driver) {
				try {
					driver.findElement(locator);
					return false;
				} catch (NoSuchElementException e) {
					return true;
				} catch (StaleElementReferenceException e) {
					return true;
				}
			}

			@Override
			public String toString() {
				return "element to not being present: " + locator;
			}
		};
	}
}
