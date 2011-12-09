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
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/
package org.olat.test.util.selenium.olatapi.course.run;

import org.olat.test.util.selenium.olatapi.OLATSeleniumWrapper;
import org.olat.test.util.selenium.olatapi.portfolio.EPCollectWizard;

import com.thoughtworks.selenium.Selenium;

/**
 * This is the Forum course element run page.
 * 
 * @author Lavinia Dumitrescu
 *
 */
public class Forum extends OLATSeleniumWrapper {

	/**
	 * @param selenium
	 */
	public Forum(Selenium selenium) {
		super(selenium);		
		
    //	Check that we're on the right place
		if(!selenium.isElementPresent("ui=course::content_forum_newTopic()") && !selenium.isElementPresent("ui=course::content_forum_displayForum()")) {
			throw new IllegalStateException("This is not the - Forum - page");
		}		
	}
	
	private void displayForumIfNecessary() {
    //	if Display forum visible - click on it
		if(selenium.isElementPresent("ui=course::content_forum_displayForum()")) {
			selenium.click("ui=course::content_forum_displayForum()");
			selenium.waitForPageToLoad("30000");
		}
	}

	/**
	 * Open new forum topic with the given title and text.
	 * @param title
	 * @param body
	 */
	public void openNewTopic(String title, String body) {		
		displayForumIfNecessary();
		if(selenium.isElementPresent("ui=course::content_forum_newTopic()")) {
			selenium.click("ui=course::content_forum_newTopic()");
			selenium.waitForPageToLoad("30000");
			selenium.click("ui=course::content_forum_typeMsgTitle()");
			selenium.type("ui=course::content_forum_typeMsgTitle()", title);
			//the message body shows up in an iframe
			//selenium.selectFrame("//iframe[contains(@src,'javascript:\"\"')]");
			selenium.type("ui=course::content_forum_clickMsgBody()", body);
			//selenium.selectFrame("relative=top");	
			selenium.click("ui=course::content_forum_save()");
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {				
			}
			selenium.waitForPageToLoad("30000");			
		} else {
			throw new IllegalStateException("Cannot open new topic!");
		}
	}
	
	/**
	 * Selects the topic with this title.
	 * @param title
	 */
	public void viewTopic(String topicTitle) {
		displayForumIfNecessary();
		if(selenium.isElementPresent("ui=course::content_forum_viewTopic(nameOfTopic=" + topicTitle + ")")) {
		  selenium.click("ui=course::content_forum_viewTopic(nameOfTopic=" + topicTitle + ")");
		  selenium.waitForPageToLoad("30000");
		} else {
			throw new IllegalStateException("Cannot viewTopic: " + topicTitle);
		}
	}
	
		
	/**
	 * Deletes the topic with this title.
	 * Assumes that the Forum node is selected.
	 * @param title
	 */
	public void deleteForumTopic(String title) {
		viewTopic(title);		
		if(selenium.isElementPresent("ui=course::content_forum_delete()")) {
		selenium.click("ui=course::content_forum_delete()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=course::content_forum_deleteYes()");
		selenium.waitForPageToLoad("30000");
		} else {
			throw new IllegalStateException("Cannot delete topic with this title!");
		}
	}
	
	/**
	 * Replay to the topic with the topicTitle title, it assusmes that the topic contains only one message.
	 * @param topicTitle
	 * @param body
	 * @param replyWithQuotation
	 */
	public void replyToTopic(String topicTitle, String body, boolean replyWithQuotation) {
		viewTopic(topicTitle);
		replyToCurrentMessage(body, replyWithQuotation);
	}
	
	/**
	 * It is assumed that a topic (with one message) was selected.
	 * @param body
	 * @param replyWithQuotation
	 */
	public void replyToCurrentMessage(String body, boolean replyWithQuotation) {
		if(replyWithQuotation) {
		  selenium.click("ui=course::content_forum_replyWithQuotation()");
		} else {
			selenium.click("ui=course::content_forum_replyWithoutQuotation()");
		}
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {				
		}
		selenium.waitForPageToLoad("30000");
	//the message body shows up in an iframe
		//selenium.selectFrame("//iframe[contains(@src,'javascript:\"\"')]");
		selenium.click("ui=course::content_forum_clickMsgBody()");
		selenium.type("ui=course::content_forum_clickMsgBody()", body);
		//selenium.selectFrame("relative=top");	
		selenium.click("ui=course::content_forum_save()");
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {				
		}
		selenium.waitForPageToLoad("30000");		
	}
	
	/**
	 * Assumes that a certain message in this forum is selected, so the "Edit" link is present.
	 * @param fileName
	 */
	public void attachFileToMsg(String fileName) {
		editCurrentMessage();
		
		selenium.type("ui=upload::fileChooser()", fileName);
		selenium.waitForPageToLoad("30000");	
		sleepThread(5000);
		selenium.click("ui=course::content_forum_save()");
		selenium.waitForPageToLoad("30000");		
	}
	
	/**
	 * Deletes the attached file from the current selected message, if possible,
	 * and saves the message.
	 *
	 */
	public void deleteAttachedFile(String fileName) {
		editCurrentMessage();		
		
		selenium.click("ui=course::content_forum_deleteFile(nameOfFile=" + fileName + ")");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=groups::content_deleteYes()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=course::content_forum_save()");
		selenium.waitForPageToLoad("30000");	
	}
	
	/**
	 * Only one selenium step. A test case needs openNewTopic decomposed in composing steps.
	 */
	public void clickNewTopic() {
		selenium.click("ui=course::content_forum_newTopic()");
		selenium.waitForPageToLoad("30000");
	}
	
	/**
	 * Type in a new forum message assuming that we already have an open message editor.
	 * Save message.
	 * @param title
	 * @param body
	 */
	public void typeInNewMessage(String title, String body) {
		selenium.click("ui=course::content_forum_typeMsgTitle()");
		selenium.type("ui=course::content_forum_typeMsgTitle()", title);
		//the message body shows up in an iframe
		//selenium.selectFrame("//iframe[contains(@src,'javascript:\"\"')]");
		selenium.type("ui=course::content_forum_clickMsgBody()", body);
		//selenium.selectFrame("relative=top");	
		selenium.click("ui=course::content_forum_save()");
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {				
		}
		selenium.waitForPageToLoad("30000");		
	}
	
	/**
	 * Start editing the current forum message, if "Edit" button present.
	 */
	private void editCurrentMessage() {
		if(selenium.isElementPresent("ui=course::content_forum_edit()")) {
			selenium.click("ui=course::content_forum_edit()");
			selenium.waitForPageToLoad("30000");
		}  else if (!selenium.isElementPresent("ui=course::content_forum_save()")) {
			throw new IllegalStateException("Edit - button not available!");
		}
	}
	
	/**
	 * click the ePortfolio button to start collecting message as artefact
	 */
	public EPCollectWizard collectAsArtefact() {
		if (selenium.isElementPresent("ui=course::content_forum_collectAsArtefact()")) {
			selenium.click("ui=course::content_forum_collectAsArtefact()");
			selenium.waitForPageToLoad("30000");			
			return new EPCollectWizard(selenium);
		}
		return null;		
	}
}
