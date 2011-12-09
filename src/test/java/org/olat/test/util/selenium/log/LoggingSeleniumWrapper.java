/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
*/
package org.olat.test.util.selenium.log;

import junit.framework.AssertionFailedError;

import org.apache.log4j.Logger;

import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleniumException;

public class LoggingSeleniumWrapper implements Selenium {

	private static int highestSeqNum_ = 0;
	
	private final int seqNum_ = highestSeqNum_++;
	
	private final Logger logger_;
	private final Selenium delegate_;
	
	public LoggingSeleniumWrapper(Selenium delegate, Logger logger) {		
		delegate_ = delegate;
		logger_ = logger;
		logger_.info(getLogPrefixDetails()+" <init>");
	}
	
	@Override
	public String toString() {
		return getLogPrefixDetails()+super.toString();
	}
	
	private String getLogPrefixDetails() {
		return "[seleniumwrapper-"+seqNum_+"] ";
	}

	public void addLocationStrategy(String strategyName,
			String functionDefinition) {
		logger_.info(getLogPrefixDetails()+"addLocationStrategy: "+strategyName+": "+functionDefinition);
		delegate_.addLocationStrategy(strategyName, functionDefinition);
	}

	public void addSelection(String locator, String optionLocator) {
		delegate_.addSelection(locator, optionLocator);
	}

	public void allowNativeXpath(String allow) {
		delegate_.allowNativeXpath(allow);
	}

	public void altKeyDown() {
		delegate_.altKeyDown();
	}

	public void altKeyUp() {
		delegate_.altKeyUp();
	}

	public void answerOnNextPrompt(String answer) {
		delegate_.answerOnNextPrompt(answer);
	}

	public void assignId(String locator, String identifier) {
		delegate_.assignId(locator, identifier);
	}

	public void attachFile(String fieldLocator, String fileLocator) {
		delegate_.attachFile(fieldLocator, fileLocator);
	}
	

	public void captureScreenshot(String filename) {
		delegate_.captureScreenshot(filename);
	}

	public void check(String locator) {
		delegate_.check(locator);
	}

	public void chooseCancelOnNextConfirmation() {
		delegate_.chooseCancelOnNextConfirmation();
	}

	public void chooseOkOnNextConfirmation() {
		delegate_.chooseOkOnNextConfirmation();
	}

	public void click(String locator) {
		logger_.info(getLogPrefixDetails()+"click: "+locator);
		if (!locator.startsWith("ui=")) {
			//throw new AssertionError("click must use olat mapping file. add the following to it or find it there already: "+locator);
		}
		try{
			if (!delegate_.isElementPresent(locator)) {
				System.err.println(getLogPrefixDetails()+"UI Element not found: "+locator);
				logger_.warn(getLogPrefixDetails()+"UI Element not found: "+locator);
			}
		} catch(SeleniumException e) {
			System.err.println(getLogPrefixDetails()+"Could not determine whether UI Element exists: "+locator);
			System.err.println(getLogPrefixDetails()+"Got exception instead: "+e);
			throw e;
		}
		try{
			delegate_.click(locator);
		} catch(SeleniumException e) {
			throw e;
		}
	}

	public void clickAt(String locator, String coordString) {
		if (!locator.startsWith("ui=")) {
			throw new AssertionError("click must use olat mapping file. add the following to it or find it there already: "+locator);
		}
		delegate_.clickAt(locator, coordString);
	}

	public void close() {
		delegate_.close();
	}

	public void contextMenu(String locator) {
		delegate_.contextMenu(locator);
	}

	public void contextMenuAt(String locator, String coordString) {
		delegate_.contextMenuAt(locator, coordString);
	}

	public void controlKeyDown() {
		delegate_.controlKeyDown();
	}

	public void controlKeyUp() {
		delegate_.controlKeyUp();
	}

	public void createCookie(String nameValuePair, String optionsString) {
		delegate_.createCookie(nameValuePair, optionsString);
	}

	public void deleteAllVisibleCookies() {
		delegate_.deleteAllVisibleCookies();
	}

	public void deleteCookie(String name, String optionsString) {
		delegate_.deleteCookie(name, optionsString);
	}

	public void doubleClick(String locator) {
		delegate_.doubleClick(locator);
	}

	public void doubleClickAt(String locator, String coordString) {
		delegate_.doubleClickAt(locator, coordString);
	}

	public void dragAndDrop(String locator, String movementsString) {
		delegate_.dragAndDrop(locator, movementsString);
	}

	public void dragAndDropToObject(String locatorOfObjectToBeDragged,
			String locatorOfDragDestinationObject) {
		delegate_.dragAndDropToObject(locatorOfObjectToBeDragged, locatorOfDragDestinationObject);
	}

	public void dragdrop(String locator, String movementsString) {
		delegate_.dragdrop(locator, movementsString);
	}

	public void fireEvent(String locator, String eventName) {
		delegate_.fireEvent(locator, eventName);
	}

	public void focus(String locator) {
		delegate_.focus(locator);
	}

	public String getAlert() {
		return delegate_.getAlert();
	}

	public String[] getAllButtons() {
		return delegate_.getAllButtons();
	}

	public String[] getAllFields() {
		return delegate_.getAllFields();
	}

	public String[] getAllLinks() {
		return delegate_.getAllLinks();
	}

	public String[] getAllWindowIds() {
		return delegate_.getAllWindowIds();
	}

	public String[] getAllWindowNames() {
		return delegate_.getAllWindowNames();
	}

	public String[] getAllWindowTitles() {
		return delegate_.getAllWindowTitles();
	}

	public String getAttribute(String attributeLocator) {
		return delegate_.getAttribute(attributeLocator);
	}

	public String[] getAttributeFromAllWindows(String attributeName) {
		return delegate_.getAttributeFromAllWindows(attributeName);
	}

	public String getBodyText() {
		return delegate_.getBodyText();
	}

	public String getConfirmation() {
		return delegate_.getConfirmation();
	}

	public String getCookie() {
		return delegate_.getCookie();
	}

	public String getCookieByName(String name) {
		return delegate_.getCookieByName(name);
	}

	public Number getCursorPosition(String locator) {
		return delegate_.getCursorPosition(locator);
	}

	public Number getElementHeight(String locator) {
		return delegate_.getElementHeight(locator);
	}

	public Number getElementIndex(String locator) {
		return delegate_.getElementIndex(locator);
	}

	public Number getElementPositionLeft(String locator) {
		return delegate_.getElementPositionLeft(locator);
	}

	public Number getElementPositionTop(String locator) {
		return delegate_.getElementPositionTop(locator);
	}

	public Number getElementWidth(String locator) {
		return delegate_.getElementWidth(locator);
	}

	public String getEval(String script) {
		return delegate_.getEval(script);
	}

	public String getExpression(String expression) {
		return delegate_.getExpression(expression);
	}

	public String getHtmlSource() {
		return delegate_.getHtmlSource();
	}

	public String getLocation() {
		return delegate_.getLocation();
	}

	public Number getMouseSpeed() {
		return delegate_.getMouseSpeed();
	}

	public String getPrompt() {
		return delegate_.getPrompt();
	}

	public String[] getSelectOptions(String selectLocator) {
		return delegate_.getSelectOptions(selectLocator);
	}

	public String getSelectedId(String selectLocator) {
		return delegate_.getSelectedId(selectLocator);
	}

	public String[] getSelectedIds(String selectLocator) {
		return delegate_.getSelectedIds(selectLocator);
	}

	public String getSelectedIndex(String selectLocator) {
		return delegate_.getSelectedIndex(selectLocator);
	}

	public String[] getSelectedIndexes(String selectLocator) {
		return delegate_.getSelectedIndexes(selectLocator);
	}

	public String getSelectedLabel(String selectLocator) {
		return delegate_.getSelectedLabel(selectLocator);
	}

	public String[] getSelectedLabels(String selectLocator) {
		return delegate_.getSelectedLabels(selectLocator);
	}

	public String getSelectedValue(String selectLocator) {
		return delegate_.getSelectedValue(selectLocator);
	}

	public String[] getSelectedValues(String selectLocator) {
		return delegate_.getSelectedValues(selectLocator);
	}

	public String getSpeed() {
		return delegate_.getSpeed();
	}

	public String getTable(String tableCellAddress) {
		return delegate_.getTable(tableCellAddress);
	}

	public String getText(String locator) {
		return delegate_.getText(locator);
	}

	public String getTitle() {
		return delegate_.getTitle();
	}

	public String getValue(String locator) {
		return delegate_.getValue(locator);
	}

	public boolean getWhetherThisFrameMatchFrameExpression(
			String currentFrameString, String target) {
		return delegate_.getWhetherThisFrameMatchFrameExpression(currentFrameString, target);
	}

	public boolean getWhetherThisWindowMatchWindowExpression(
			String currentWindowString, String target) {
		return delegate_.getWhetherThisWindowMatchWindowExpression(currentWindowString, target);
	}

	public Number getXpathCount(String xpath) {
		return delegate_.getXpathCount(xpath);
	}

	public void goBack() {
		delegate_.goBack();
	}

	public void highlight(String locator) {
		delegate_.highlight(locator);
	}

	public void ignoreAttributesWithoutValue(String ignore) {
		delegate_.ignoreAttributesWithoutValue(ignore);
	}

	public boolean isAlertPresent() {
		return delegate_.isAlertPresent();
	}

	public boolean isChecked(String locator) {
		return delegate_.isChecked(locator);
	}

	public boolean isConfirmationPresent() {
		return delegate_.isConfirmationPresent();
	}

	public boolean isCookiePresent(String name) {
		return delegate_.isCookiePresent(name);
	}

	public boolean isEditable(String locator) {
		return delegate_.isEditable(locator);
	}

	public boolean isElementPresent(String locator) {
		logger_.info(getLogPrefixDetails()+"isElementPresent: "+locator);
		boolean elementPresent = false;
		try{
			elementPresent = delegate_.isElementPresent(locator);
		} finally {
			logger_.info(getLogPrefixDetails()+"isElementPresent: "+locator+" => "+elementPresent);
		}
		return elementPresent;
	}

	public boolean isOrdered(String locator1, String locator2) {
		return delegate_.isOrdered(locator1, locator2);
	}

	public boolean isPromptPresent() {
		return delegate_.isPromptPresent();
	}

	public boolean isSomethingSelected(String selectLocator) {
		return delegate_.isSomethingSelected(selectLocator);
	}

	public boolean isTextPresent(String pattern) {
		return delegate_.isTextPresent(pattern);
	}

	public boolean isVisible(String locator) {
		return delegate_.isVisible(locator);
	}

	public void keyDown(String locator, String keySequence) {
		delegate_.keyDown(locator, keySequence);
	}

	public void keyDownNative(String keycode) {
		delegate_.keyDownNative(keycode);
	}

	public void keyPress(String locator, String keySequence) {
		delegate_.keyPress(locator, keySequence);
	}

	public void keyPressNative(String keycode) {
		delegate_.keyPressNative(keycode);
	}

	public void keyUp(String locator, String keySequence) {
		delegate_.keyUp(locator, keySequence);
	}

	public void keyUpNative(String keycode) {
		delegate_.keyUpNative(keycode);
	}

	public void metaKeyDown() {
		delegate_.metaKeyDown();
	}

	public void metaKeyUp() {
		delegate_.metaKeyUp();
	}

	public void mouseDown(String locator) {
		delegate_.mouseDown(locator);
	}

	public void mouseDownAt(String locator, String coordString) {
		delegate_.mouseDownAt(locator, coordString);
	}

	public void mouseMove(String locator) {
		delegate_.mouseMove(locator);
	}

	public void mouseMoveAt(String locator, String coordString) {
		delegate_.mouseMoveAt(locator, coordString);
	}

	public void mouseOut(String locator) {
		delegate_.mouseOut(locator);
	}

	public void mouseOver(String locator) {
		delegate_.mouseOver(locator);
	}

	public void mouseUp(String locator) {
		delegate_.mouseUp(locator);
	}

	public void mouseUpAt(String locator, String coordString) {
		delegate_.mouseUpAt(locator, coordString);
	}

	public void open(String url) {
		delegate_.open(url);
	}

	public void openWindow(String url, String windowID) {
		delegate_.openWindow(url, windowID);
	}

	public void refresh() {
		delegate_.refresh();
	}

	public void removeAllSelections(String locator) {
		delegate_.removeAllSelections(locator);
	}

	public void removeSelection(String locator, String optionLocator) {
		delegate_.removeSelection(locator, optionLocator);
	}

	public void runScript(String script) {
		delegate_.runScript(script);
	}

	public void select(String selectLocator, String optionLocator) {
		logger_.info(getLogPrefixDetails()+"select: "+selectLocator+", "+optionLocator);
		delegate_.select(selectLocator, optionLocator);
	}

	public void selectFrame(String locator) {
		delegate_.selectFrame(locator);
	}

	public void selectWindow(String windowID) {
		delegate_.selectWindow(windowID);
	}

	public void setBrowserLogLevel(String logLevel) {
		delegate_.setBrowserLogLevel(logLevel);
	}

	public void setContext(String context) {
		delegate_.setContext(context);
	}

	public void setCursorPosition(String locator, String position) {
		delegate_.setCursorPosition(locator, position);
	}

	public void setMouseSpeed(String pixels) {
		delegate_.setMouseSpeed(pixels);
	}

	public void setSpeed(String value) {
		delegate_.setSpeed(value);
	}

	public void setTimeout(String timeout) {
		delegate_.setTimeout(timeout);
	}

	public void shiftKeyDown() {
		delegate_.shiftKeyDown();
	}

	public void shiftKeyUp() {
		delegate_.shiftKeyUp();
	}

	public void shutDownSeleniumServer() {
		delegate_.shutDownSeleniumServer();
	}

	public void start() {
		delegate_.start();
	}

	public void stop() {
		delegate_.stop();
	}

	public void submit(String formLocator) {
		delegate_.submit(formLocator);
	}

	public void type(String locator, String value) {
		logger_.info(getLogPrefixDetails()+"type: "+locator+", "+value);
		if (!locator.startsWith("ui=")) {
			throw new AssertionError("type must use olat mapping file. add the following to it or find it there already: "+locator);
		}
		delegate_.type(locator, value);
	}

	public void typeKeys(String locator, String value) {
		if (!locator.startsWith("ui=")) {
			throw new AssertionError("type must use olat mapping file. add the following to it or find it there already: "+locator);
		}
		delegate_.typeKeys(locator, value);
	}

	public void uncheck(String locator) {
		delegate_.uncheck(locator);
	}

	public void waitForCondition(String script, String timeout) {
		delegate_.waitForCondition(script, timeout);
	}

	public void waitForFrameToLoad(String frameAddress, String timeout) {
		delegate_.waitForFrameToLoad(frameAddress, timeout);
	}

	public void waitForPageToLoad(String timeout) {
		logger_.info(getLogPrefixDetails()+"waitForPageToLoad: "+timeout);
		delegate_.waitForPageToLoad(timeout);
		try{
			final String title = delegate_.getTitle();
			final String knownIssuesStr = "KnownIssueException";
			final String bodyText = delegate_.getBodyText();
			if ("OLAT - Error".equals(title)) {
				if (!bodyText.contains(knownIssuesStr)) {
					throw new AssertionFailedError("Red Screen encountered! See System.out for details!!!");
				}
			} else if (title.contains("Error")) {
				if (!bodyText.contains(knownIssuesStr)) {
					throw new AssertionFailedError("Red Screen encountered! See System.out for details!!!");
				}
			} else {
				if (!bodyText.contains(knownIssuesStr)) {
					if (bodyText.contains("An error occured")) {
						throw new AssertionFailedError("Red Screen encountered! See System.out for details!!!");
					} else if (bodyText.contains("translation:::")) {
						throw new AssertionFailedError("Translation Issue encountered! (translation::: found in text) See System.out for details!!!");
					}
				}
			}
		} catch(Exception e) {
			logger_.info(getLogPrefixDetails()+"waitForPageToLoad: couldn't fetch title or body. bummer. but never mind.: "+e);
		}
	}

	public void waitForPopUp(String windowID, String timeout) {
		delegate_.waitForPopUp(windowID, timeout);
	}

	public void windowFocus() {
		delegate_.windowFocus();
	}

	public void windowMaximize() {
		delegate_.windowMaximize();
	}

	@Override
	public void addCustomRequestHeader(String arg0, String arg1) {
		// TODO Auto-generated method stub
		delegate_.addCustomRequestHeader(arg0, arg1);
	}

	@Override
	public void addScript(String arg0, String arg1) {
		// TODO Auto-generated method stub
		delegate_.addScript(arg0, arg1);
	}

	@Override
	public void captureEntirePageScreenshot(String arg0, String arg1) {
		// TODO Auto-generated method stub
		delegate_.captureEntirePageScreenshot(arg0, arg1);
	}

	@Override
	public String captureEntirePageScreenshotToString(String arg0) {
		// TODO Auto-generated method stub
		return delegate_.captureEntirePageScreenshotToString(arg0);
	}

	@Override
	public String captureNetworkTraffic(String arg0) {
		// TODO Auto-generated method stub
		return delegate_.captureNetworkTraffic(arg0);
	}

	@Override
	public String captureScreenshotToString() {
		// TODO Auto-generated method stub
		return delegate_.captureScreenshotToString();
	}

	@Override
	public void deselectPopUp() {
		// TODO Auto-generated method stub
		delegate_.deselectPopUp();
	}

	@Override
	public void mouseDownRight(String arg0) {
		// TODO Auto-generated method stub
		delegate_.mouseDownRight(arg0);
	}

	@Override
	public void mouseDownRightAt(String arg0, String arg1) {
		// TODO Auto-generated method stub
		delegate_.mouseDownRightAt(arg0, arg1);
	}

	@Override
	public void mouseUpRight(String arg0) {
		// TODO Auto-generated method stub
		delegate_.mouseUpRight(arg0);
	}

	@Override
	public void mouseUpRightAt(String arg0, String arg1) {
		// TODO Auto-generated method stub
		delegate_.mouseUpRightAt(arg0, arg1);
	}

	@Override
	public void removeScript(String arg0) {
		// TODO Auto-generated method stub
		delegate_.removeScript(arg0);
	}

	@Override
	public String retrieveLastRemoteControlLogs() {
		// TODO Auto-generated method stub
		return delegate_.retrieveLastRemoteControlLogs();
	}

	@Override
	public void rollup(String arg0, String arg1) {
		// TODO Auto-generated method stub
		delegate_.rollup(arg0, arg1);
	}

	@Override
	public void selectPopUp(String arg0) {
		// TODO Auto-generated method stub
		delegate_.selectPopUp(arg0);
	}

	@Override
	public void setExtensionJs(String arg0) {
		// TODO Auto-generated method stub
		delegate_.setExtensionJs(arg0);
	}

	@Override
	public void showContextualBanner() {
		// TODO Auto-generated method stub
		delegate_.showContextualBanner();
	}

	@Override
	public void showContextualBanner(String arg0, String arg1) {
		// TODO Auto-generated method stub
		delegate_.showContextualBanner(arg0, arg1);
	}

	@Override
	public void start(String arg0) {
		// TODO Auto-generated method stub
		delegate_.start(arg0);
	}

	@Override
	public void start(Object arg0) {
		// TODO Auto-generated method stub
		delegate_.start(arg0);
	}

	@Override
	public void useXpathLibrary(String arg0) {
		// TODO Auto-generated method stub
		delegate_.useXpathLibrary(arg0);
	}

}
