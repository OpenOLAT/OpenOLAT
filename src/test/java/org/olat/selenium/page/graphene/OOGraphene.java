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
import org.openqa.selenium.WrapsElement;
import org.openqa.selenium.firefox.FirefoxDriver;

/**
 * 
 * Initial date: 20.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OOGraphene {

	private static final long poolingDuration = 100;//ms
	private static final long waitTinyDuration = 50;//seconds
	private static final long defaultTimeout = 5;//seconds

	private static final By closeBlueBoxButtonBy = By.cssSelector("div.o_alert_info div.o_sel_info_message a.o_alert_close.o_sel_info_close");
	
	public static final By wizardNextBy = By.xpath("//div[contains(@class,'modal-footer')]//a[contains(@class,'o_wizard_button_next')]");
	public static final By wizardFinishBy = By.xpath("//div[contains(@class,'modal-footer')]//a[contains(@class,'o_wizard_button_finish') and not(contains(@class,'o_disabled'))]");
	
	/**
	 * Wait until the busy flag is ok, the browser scrolled
	 * to the top and that the body of the modal dialog is visible.
	 * 
	 * @param The browser
	 */
	public static void waitModalDialog(WebDriver browser) {
		waitBusyAndScrollTop(browser);
		By modalBy = By.cssSelector("div.modal-dialog div.modal-body");
		Graphene.waitModel(browser).withTimeout(5, TimeUnit.SECONDS)
			.pollingEvery(200, TimeUnit.MILLISECONDS).until().element(modalBy).is().visible();
	}
	
	/**
	 * Wait until the busy flag is ok, the browser scrolled
	 * to the top and that the body of the modal dialog is visible.
	 * 
	 * @param The browser
	 */
	public static void waitModalWizard(WebDriver browser) {
		waitBusyAndScrollTop(browser);
		By modalBy = By.cssSelector("div.modal-dialog div.modal-body");
		Graphene.waitModel(browser).withTimeout(defaultTimeout, TimeUnit.SECONDS)
			.pollingEvery(200, TimeUnit.MILLISECONDS).until().element(modalBy).is().visible();
	}
	
	public static void waitModalDialogDisappears(WebDriver browser) {
		By modalBy = By.xpath("//div[not(@id='o_form_dirty_message')]/div[contains(@class,'modal-dialog')]/div[contains(@class,'modal-content')]");
		Graphene.waitModel(browser).withTimeout(5, TimeUnit.SECONDS)
			.pollingEvery(200, TimeUnit.MILLISECONDS).until().element(modalBy).is().not().present();
	}
	
	public static void waitCallout(WebDriver browser) {
		By calloutBy = By.cssSelector("div.popover-content div.o_callout_content");
		waitElement(calloutBy, 5, browser);
	}
	
	public static void waitBusy(WebDriver browser) {
		Graphene.waitModel(browser).withTimeout(defaultTimeout, TimeUnit.SECONDS)
			.pollingEvery(poolingDuration, TimeUnit.MILLISECONDS).until(new BusyPredicate());
	}
	
	public static void waitBusy(WebDriver browser, int timeoutInSeconds) {
		Graphene.waitModel(browser).withTimeout(timeoutInSeconds, TimeUnit.SECONDS)
			.pollingEvery(poolingDuration, TimeUnit.MILLISECONDS).until(new BusyPredicate());
	}
	
	/**
	 * 
	 * @param element
	 * @param browser
	 */
	public static void waitElement(By element, WebDriver browser) {
		Graphene.waitModel(browser).withTimeout(5, TimeUnit.SECONDS)
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
	 * Wait until the element is not present.
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
	 * Wait until the element is not visible.
	 * 
	 * @param element
	 * @param timeoutInSeconds
	 * @param browser
	 */
	public static void waitElementUntilNotVisible(By element, int timeoutInSeconds, WebDriver browser) {
		Graphene.waitModel(browser).withTimeout(timeoutInSeconds, TimeUnit.SECONDS)
			.pollingEvery(poolingDuration, TimeUnit.MILLISECONDS).until().element(element).is().not().visible();
	}
	
	public static void waitGui(WebDriver browser) {
		Graphene.waitGui(browser);
	}
	
	public static void nextStep(WebDriver browser) {
		clickAndWait(wizardNextBy, browser);
	}
	
	public static void finishStep(WebDriver browser) {
		clickAndWait(wizardFinishBy, browser);
		OOGraphene.closeBlueMessageWindow(browser);
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
	 * @param buttonBy
	 * @param browser
	 */
	public static void clickAndWait(By buttonBy, WebDriver browser) {
		WebElement buttonEl = browser.findElement(buttonBy);
		boolean move = buttonEl.getLocation().getY() > 669;
		if(move) {
			scrollTo(buttonBy, browser);
		}
		browser.findElement(buttonBy).click();
		if(move) {
			OOGraphene.waitBusyAndScrollTop(browser);
		} else {
			OOGraphene.waitBusy(browser);
		}
	}
	
	/**
	 * Scroll to the element and wait a little longer.
	 * @param by
	 * @param browser
	 */
	public static void scrollTo(By by, WebDriver browser) {
		WebElement el = browser.findElement(by);
		((JavascriptExecutor)browser).executeScript("return arguments[0].scrollIntoView({behavior:\"instant\", block: \"end\"});", el);
		OOGraphene.waitingALittleLonger();
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
	
	// top.tinymce.get('o_fi1000000416').setContent('<p>Hacked</p>');
	// <div id="o_fi1000000416_diw" class="o_richtext_mce"> <iframe id="o_fi1000000416_ifr">
	public static final void tinymce(String content, WebDriver browser) {
		Graphene.waitModel(browser).withTimeout(waitTinyDuration, TimeUnit.SECONDS)
			.pollingEvery(poolingDuration, TimeUnit.MILLISECONDS)
			.until(new TinyMCELoadedPredicate());
		((JavascriptExecutor)browser).executeScript("top.tinymce.activeEditor.setContent('" + content + "')");
	}
	
	public static final void tinymceExec(String content, WebDriver browser) {
		Graphene.waitModel(browser).withTimeout(waitTinyDuration, TimeUnit.SECONDS)
			.pollingEvery(poolingDuration, TimeUnit.MILLISECONDS)
			.until(new TinyMCELoadedPredicate());
		((JavascriptExecutor)browser).executeScript("top.tinymce.activeEditor.execCommand('mceInsertRawHTML', true, '" + content + "')");
	}
	
	public static final void tinymce(String content, String containerCssSelector, WebDriver browser) {
		By tinyIdBy = By.cssSelector(containerCssSelector + " div.o_richtext_mce");
		waitElement(tinyIdBy, 5, browser);
		WebElement tinyIdEl = browser.findElement(tinyIdBy);
		String tinyId = tinyIdEl.getAttribute("id").replace("_diw", "");

		Graphene.waitModel(browser).withTimeout(waitTinyDuration, TimeUnit.SECONDS)
			.pollingEvery(poolingDuration, TimeUnit.MILLISECONDS)
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
		waitElement(tinyIdBy, 5, browser);
		WebElement tinyIdEl = browser.findElement(tinyIdBy);
		String tinyId = tinyIdEl.getAttribute("id").replace("_diw", "");

		Graphene.waitModel(browser).withTimeout(waitTinyDuration, TimeUnit.SECONDS)
			.pollingEvery(poolingDuration, TimeUnit.MILLISECONDS)
			.until(new TinyMCELoadedByIdPredicate(tinyId));
		((JavascriptExecutor)browser).executeScript("top.tinymce.editors['" + tinyId + "'].insertContent('" + content + "')");
	}
	
	/**
	 * 
	 * @param tabsBy The selector for the tabs bar
	 * @param formBy The selector to found the form
	 * @param browser The browser
	 */
	public static final void selectTab(By tabsBy, By formBy, WebDriver browser) {
		List<WebElement> tabLinks = browser.findElements(tabsBy);

		boolean found = false;
		a_a:
		for(WebElement tabLink:tabLinks) {
			tabLink.click();
			OOGraphene.waitBusy(browser);
			List<WebElement> chooseRepoEntry = browser.findElements(formBy);
			if(chooseRepoEntry.size() > 0) {
				found = true;
				break a_a;
			}
		}
		
		if(!found) {
			System.out.println();
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
	
	public static final void check(WebElement labelEl, WebElement checkboxEl, Boolean val) {
		if(val == null) return;
		
		String checked = checkboxEl.getAttribute("checked");
		if(Boolean.TRUE.equals(val)) {
			if(checked == null) {
				labelEl.click();
			}
		} else {
			if(checked != null) {
				labelEl.click();
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
			Graphene.waitModel(browser)
				.ignoring(TimeoutException.class)
				.pollingEvery(poolingDuration, TimeUnit.MILLISECONDS)
				.until(new BusyScrollToPredicate());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static final void closeErrorBox(WebDriver browser) {
		By errorBoxBy = By.cssSelector(".modal-body.alert.alert-danger");
		waitElement(errorBoxBy, 5, browser);
		By closeButtonBy = By.xpath("//div[not(@id='o_form_dirty_message')]/div[contains(@class,'modal-dialog')]//button[@class='close']");
		waitElement(closeButtonBy, 5, browser);
		browser.findElement(closeButtonBy).click();
		waitModalDialogDisappears(browser);
	}
	
	public static final void closeWarningBox(WebDriver browser) {
		By errorBoxBy = By.cssSelector(".modal-body.alert.alert-warning");
		waitElement(errorBoxBy, 5, browser);
		By closeButtonBy = By.xpath("//div[not(@id='o_form_dirty_message')]/div[contains(@class,'modal-dialog')]//button[@class='close']");
		waitElement(closeButtonBy, 5, browser);
		browser.findElement(closeButtonBy).click();
		waitModalDialogDisappears(browser);
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
			Graphene.waitModel(browser).pollingEvery(poolingDuration, TimeUnit.MILLISECONDS)
					.until(new NavBarTransitionPredicate());
			waitingALittleBit();
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}
	
	public static final WebElement unwrap(WebElement element) {
		if(element instanceof WrapsElement) {
			WebElement wrappedCircleEl = ((WrapsElement)element).getWrappedElement();
			if(wrappedCircleEl instanceof WrapsElement) {
				element = ((WrapsElement)wrappedCircleEl).getWrappedElement();
			}
		}
		return element;
	}
}
