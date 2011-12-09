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
package org.olat.test.util.selenium.olatapi.components;

import org.olat.test.util.selenium.olatapi.OLATSeleniumWrapper;

import com.thoughtworks.selenium.Selenium;

/**
 * Utility class for the chat functionality.
 * 
 * @author lavinia
 *
 */
public class ChatComponent extends OLATSeleniumWrapper {

  public ChatComponent(Selenium selenium) {
    super(selenium);
    //TODO: check that Chat is configured for the tested instance
  }
  
  /** COURSE CHAT */
  /**
   * This is available in course.
   */
  public void openChat(boolean showUser) {
    selenium.mouseMoveAt("ui=courseChat::openCourseChat()", "300,300");
    selenium.click("ui=courseChat::openCourseChat()");
    selenium.waitForPageToLoad("90000");
    
    sleepThread(6000);
    
    if(!selenium.isElementPresent("ui=courseChat::withinCourseChat()")) {     
      sleepThread(6000);
    }
    selenium.click("ui=courseChat::withinCourseChat()");
    selenium.waitForPageToLoad("30000");
    
    if(showUser) {
      toggleAnonymous();
    }    
  }
  
  public boolean isCourseChatAvailable() {
    return selenium.isElementPresent("ui=courseChat::openCourseChat()");
  }
  
  
  
  /**
   * COURSE CHAT. <br/>
   * Toggle anonymous checkbox: show/hide username.
   */
  public void toggleAnonymous() {
    selenium.click("ui=courseChat::toggleAnonymous()");
    selenium.waitForPageToLoad("30000");
  }
  
  /**
   * Works for any chat window: course/group/buddy. <br/>
   * This should be called only after calling <code>openChat</code> or <code>openProjectGroupChat</code>.
   * @param msg
   */
  public void sendMessage(String msg, String chatroomLabel) {
    selenium.type("ui=courseChat::labeledSendMsgInputField(label=" + chatroomLabel + ")", msg);
    selenium.click("ui=courseChat::labeledSendMsgButton(label=" + chatroomLabel + ")");
    selenium.waitForPageToLoad("30000");    
  }
  
  /**
   * Works for any chat window: course/group/buddy, provided there is only one chat window open, 
   * else use the method with chatroomLabel parameter. <br/>
   * This should be called only after calling <code>openChat</code> or <code>openProjectGroupChat</code>.
   * @param msg
   */
  public void sendMessage(String msg) {
    selenium.type("ui=courseChat::sendMsgInputField()", msg);
    selenium.click("ui=courseChat::sendMsgButton()");
    selenium.waitForPageToLoad("30000");    
  }
  
  /**
   * BUDDY CHAT
   */
  
  /**
   * BUDDY CHAT. <br/>
   * clicks on the green star in upper right corner.
   */
  public void openStatusChanger() {
    selenium.click("ui=buddiesChat::openStatusChanger()");
    selenium.waitForPageToLoad("30000");
  }
  
  /**
   * Works for any chat window: course/group/buddy. <br/>
   * Closes the labeled chat window.
   */
  public void closeExtWindow(String chatroomLabel) {
    selenium.click("ui=buddiesChat::closeLabeledExtWindow(label=" + chatroomLabel + ")");
    selenium.waitForPageToLoad("30000");
  }
  
  /**
   * Works for any chat window: course/group/buddy. <br/>
   * Closes any chat window.
   */
  public void closeExtWindow() {
    selenium.click("ui=buddiesChat::closeExtWindow()");
    selenium.waitForPageToLoad("30000");
  }
 
  /**
   * Open the buddies chat, this is available upon login in the upper right corner.
   * @param fullname
   */
  public void openBuddyChat(String fullname) {
    selenium.click("ui=buddiesChat::showOnlineBuddies()");
    sleepThread(3000);
    selenium.click("ui=buddiesChat::onlineBuddy(fullName=" + fullname + ")");
    selenium.waitForPageToLoad("30000");  
  }
  /**
   * FIXME: how to switch to the new opened window?
   * @param username
   
  public void openBuddyChat(String username) {
    selenium.click("ui=buddiesChat::openOnlineUserList()");
    //selenium.waitForPageToLoad("90000");
    sleepThread(3000);
    //selenium.selectWindow(selenium.getAllWindowTitles()[1]);
    
    selenium.selectWindow(selenium.getAllWindowTitles()[2]);
    selenium.windowFocus();
    assertTrue(selenium.isTextPresent("List"));    
  }*/
    
  /**
   * This should be called only after calling <code>openBuddyChat</code>.
   */
  public void sendBuddyMessage(String msg) {
    selenium.type("ui=buddiesChat::sendMsgInputField()", msg);
    selenium.click("ui=buddiesChat::sendMsgButton()");
    selenium.waitForPageToLoad("30000"); 
  }
  
  /**
   * This is available upon selection of a chat menu of a project group.
   */
  public void openProjectGoupChat() {
    selenium.click("ui=projectGroupChat::enterChatroom()");
    selenium.waitForPageToLoad("90000");
    
    sleepThread(5000);
  }
  
  /**
   * Works for any chat window: course/group/buddy. <br/>
   * Check if this chat window is still open.
   * @param chatroomLabel
   * @return
   */
  public boolean isOpen(String chatroomLabel) {
    return selenium.isElementPresent("ui=courseChat::labeledSendMsgInputField(label=" + chatroomLabel + ")");
  }
  
  /**
   * BUDDY CHAT
   * Is the green star in upper right corner available.
   * @return
   */
  public boolean isOpenStatusChangerAvailable() {
    return selenium.isElementPresent("ui=buddiesChat::openStatusChanger()");    
  }
  
  /**
   * Works for any chat window: course/group/buddy. <br/>
   * Check if a participant with this name is available for a chat window with this label.
   * @param participantName
   * @param chatroomLabel
   * @return
   */
  public boolean hasParticipant(String participantName, String chatroomLabel) {
    //return selenium.isElementPresent("ui=courseChat::participant(participantName=" + participantName + ", chatroomLabel=" + chatroomLabel + ")");
    return selenium.isElementPresent("ui=courseChat::participant(participantName=" + participantName + ")");
  }
  
}
