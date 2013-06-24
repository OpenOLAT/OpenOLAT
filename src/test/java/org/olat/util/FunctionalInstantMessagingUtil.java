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

package org.olat.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.thoughtworks.selenium.Selenium;

public class FunctionalInstantMessagingUtil {
	
	public final static Pattern CONTACT_COUNT_PATTERN = Pattern.compile("\\(([\\d]+)/([\\d]+)\\)");
	
	public final static String INSTANT_MESSAGING_CLIENT_SUMMARY = "o_topnav_imclient_summary";
	
	public final static String INSTANT_MESSAGING_ROSTER_CSS = "o_extjsPanel_im_roster";
	public final static String INSTANT_MESSAGING_SHOW_OFFLINE_CONTACTS_CSS = "o_instantmessaging_showofflineswitch";
	public final static String INSTANT_MESSAGING_HIDE_OFFLINE_CONTACTS_CSS = "o_instantmessaging_hideofflineswitch";
	public final static String INSTANT_MESSAGING_SHOW_GROUPS_CSS = "o_instantmessaging_showgroupswitch";
	public final static String INSTANT_MESSAGING_HIDE_GROUPS_CSS = "o_instantmessaging_hidegroupswitch";
	
	public final static String INSTANT_MESSAGING_GROUP_CSS = "o_instantmessaging_groupname";
	public final static String INSTANT_MESSAGING_AVAILABLE_CSS = "o_instantmessaging_available_icon";
	public final static String INSTANT_MESSAGING_UNAVAILABLE_CSS = "o_instantmessaging_unavailable_icon";
	
	public final static String INSTANT_MESSAGING_CHAT_CSS = "o_instantmessaging_chat";
	public final static String INSTANT_MESSAGING_BODY_CSS = "o_instantmessaging_body";
	
	public enum UserStatus{
		AVAILABLE,
		BUSY,
		OFFLINE,
	}
	
	private String instantMessagingClientSummary;
	
	private String instantMessagingRosterCss;
	private String instantMessagingShowOfflineContactsCss;
	private String instantMessagingHideOfflineContactsCss;
	private String instantMessagingShowGroupsCss;
	private String instantMessagingHideGroupsCss;
	
	private String instantMessagingGroupCss;
	private String instantMessagingAvailableCss;
	private String instantMessagingUnavailableCss;
	
	private String instantMessagingChatCss;
	private String instantMessagingBodyCss;
	
	private FunctionalUtil functionalUtil;
	
	public FunctionalInstantMessagingUtil(FunctionalUtil functionalUtil){
		setFunctionalUtil(functionalUtil);
		
		setInstantMessagingClientSummary(INSTANT_MESSAGING_CLIENT_SUMMARY);
		
		setInstantMessagingRosterCss(INSTANT_MESSAGING_ROSTER_CSS);
		setInstantMessagingShowOfflineContactsCss(INSTANT_MESSAGING_SHOW_OFFLINE_CONTACTS_CSS);
		setInstantMessagingHideOfflineContactsCss(INSTANT_MESSAGING_HIDE_OFFLINE_CONTACTS_CSS);
		setInstantMessagingShowGroupsCss(INSTANT_MESSAGING_SHOW_GROUPS_CSS);
		setInstantMessagingHideGroupsCss(INSTANT_MESSAGING_HIDE_GROUPS_CSS);
		
		setInstantMessagingGroupCss(INSTANT_MESSAGING_GROUP_CSS);
		setInstantMessagingAvailableCss(INSTANT_MESSAGING_AVAILABLE_CSS);
		setInstantMessagingUnavailableCss(INSTANT_MESSAGING_UNAVAILABLE_CSS);
		
		setInstantMessagingChatCss(INSTANT_MESSAGING_CHAT_CSS);
		setInstantMessagingBodyCss(INSTANT_MESSAGING_BODY_CSS);
	}
	
	/**
	 * Computes the chat summary in the top navigation.
	 * 
	 * @param browser
	 * @return The count of online contacts.
	 */
	public int onlineContactCount(Selenium browser){
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//li[@id='")
		.append(getInstantMessagingClientSummary())
		.append("']//a//span");

		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		String summary = browser.getText(selectorBuffer.toString());
		
		Matcher matcher = CONTACT_COUNT_PATTERN.matcher(summary);
		
		matcher.find();
		int count = Integer.valueOf(matcher.group(1));
		
		return(count);
	}
	
	/**
	 * Computes the chat summary in the top navigation.
	 * 
	 * @param browser
	 * @return The count of available contacts.
	 */
	public int availableContactCount(Selenium browser){
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//li[@id='")
		.append(getInstantMessagingClientSummary())
		.append("']//a//span");

		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		String summary = browser.getText(selectorBuffer.toString());
		
		Matcher matcher = CONTACT_COUNT_PATTERN.matcher(summary);

		matcher.find();
		int count = Integer.valueOf(matcher.group(2));
		
		return(count);
	}
	
	/**
	 * Opens the chat roster.
	 * 
	 * @param browser
	 * @return true on success
	 */
	public boolean openRoster(Selenium browser){
		functionalUtil.idle(browser);
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//li[@id='")
		.append(getInstantMessagingClientSummary())
		.append("']//a");

		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		
		/* wait until dialog appears */
		StringBuffer locatorBuffer = new StringBuffer();
		
		locatorBuffer.append("xpath=//div[contains(@class, '")
		.append(getInstantMessagingRosterCss())
		.append("')]");
		
		functionalUtil.waitForPageToLoadElement(browser, locatorBuffer.toString());
		
		return(true);
	}
	
	/**
	 * Show offline contacts.
	 * 
	 * @param browser
	 * @return true on success
	 */
	private boolean openOfflineContacts(Selenium browser){
		functionalUtil.idle(browser);
		
		/* check if it's already open */
		StringBuffer locatorBuffer = new StringBuffer();
		
		locatorBuffer.append("xpath=//a[contains(@class, '")
		.append(getInstantMessagingShowOfflineContactsCss())
		.append("')]");
		
		/* click if necessary */
		if(browser.isElementPresent(locatorBuffer.toString())){
			browser.click(locatorBuffer.toString());
		}
		
		return(true);
	}
	
	/**
	 * Show groups.
	 * 
	 * @param browser
	 * @return true on success
	 */
	private boolean openGroups(Selenium browser){
		functionalUtil.idle(browser);
		
		/* check if it's already open */
		StringBuffer locatorBuffer = new StringBuffer();
		
		locatorBuffer.append("xpath=//a[contains(@class, '")
		.append(getInstantMessagingShowGroupsCss())
		.append("')]");
		
		/* click if necessary */
		if(browser.isElementPresent(locatorBuffer.toString())){
			browser.click(locatorBuffer.toString());
		}
		
		return(true);
	}
	
	public List<String> findOnlineContacts(Selenium browser){
		if(!openRoster(browser)){
			return(null);
		}
		
		List<String> contacts = new ArrayList<String>();
		
		StringBuffer selectorBufferFragment = new StringBuffer();
		selectorBufferFragment.append("//ul//li[.]//a[contains(@class,'")
		.append(getInstantMessagingAvailableCss())
		.append("')]");
		
		StringBuffer locatorBuffer = new StringBuffer();
		locatorBuffer.append("xpath=")
		.append(selectorBufferFragment.toString());
		
		functionalUtil.idle(browser);
		
		int count = 0;
		
		if(browser.getXpathCount(selectorBufferFragment.toString()) == null){
			return(contacts);
		}else{
			count = browser.getXpathCount(selectorBufferFragment.toString()).intValue();
		}
		
		for(int i = 0; i < count; i++){
			StringBuffer selectorBuffer = new StringBuffer();
			selectorBuffer.append("xpath=(")
			.append(selectorBufferFragment.toString())
			.append(")[")
			.append(i + 1)
			.append("]//span");
			
			functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
			
			String contact = browser.getText(selectorBuffer.toString());
			
			if(contact != null &&
					!contact.isEmpty()){
				contacts.add(contact);
			}
		}
		
		return(contacts);
	}
	
	public List<String> findOfflineContacts(Selenium browser){
		if(!openRoster(browser)){
			return(null);
		}
		
		List<String> contacts = new ArrayList<String>();
		
		StringBuffer selectorBufferFragment = new StringBuffer();
		selectorBufferFragment.append("//ul//li[.]//a[contains(@class,'")
		.append(getInstantMessagingUnavailableCss())
		.append("')]");
		
		StringBuffer locatorBuffer = new StringBuffer();
		locatorBuffer.append("xpath=")
		.append(selectorBufferFragment.toString());
		
		functionalUtil.idle(browser);
		
		int count = 0;
		
		if(browser.getXpathCount(selectorBufferFragment.toString()) == null){
			return(contacts);
		}else{
			count = browser.getXpathCount(selectorBufferFragment.toString()).intValue();
		}
		
		for(int i = 0; i < count; i++){
			StringBuffer selectorBuffer = new StringBuffer();
			selectorBuffer.append("xpath=(")
			.append(selectorBufferFragment.toString())
			.append(")[")
			.append(i + 1)
			.append("]//span");
			
			functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
			
			String contact = browser.getText(selectorBuffer.toString());
			
			if(contact != null &&
					!contact.isEmpty()){
				contacts.add(contact);
			}
		}
		
		return(contacts);
	}
	
	/**
	 * Finds the groups by parsing roster data.
	 * 
	 * @param browser
	 * @return A List containing group names.
	 */
	public List<String> findGroups(Selenium browser){
		if(!openRoster(browser)){
			return(null);
		}
		
		if(!openGroups(browser)){
			return(null);
		}
		
		functionalUtil.idle(browser);
		
		List<String> groups = new ArrayList<String>();
		
		StringBuffer selectorBufferFragment = new StringBuffer();
		selectorBufferFragment.append("//ul//li//div[contains(@class, '")
		.append(getInstantMessagingGroupCss())
		.append("')]");
		
		StringBuffer locatorBuffer = new StringBuffer();
		locatorBuffer.append("xpath=")
		.append(selectorBufferFragment);
		
		for(int i = 0; i < browser.getXpathCount(locatorBuffer.toString()).intValue(); i++){
			StringBuffer selectorBuffer = new StringBuffer();
			selectorBuffer.append("xpath=(")
			.append(selectorBufferFragment)
			.append(")[")
			.append(i + 1)
			.append("]");

			functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
			groups.add(browser.getText(selectorBuffer.toString()));
		}
		
		return(groups);
	}
	
	/**
	 * Retrieve status information of the given user.
	 * 
	 * @param browser
	 * @param firstname
	 * @param surname
	 * @return
	 */
	public UserStatus retrieveUserStatus(Selenium browser, String firstname, String surname){
		
		//TODO:JK: implement me
		
		return(null);
	}
	
	/**
	 * Change your very own status.
	 * 
	 * @param browser
	 * @param status
	 * @return
	 */
	public boolean changeStatus(Selenium browser, UserStatus status){

		//TODO:JK: implement me
		
		return(false);
	}
	
	/**
	 * Opens a chat window with the given user.
	 * 
	 * @param browser
	 * @param firstname
	 * @param surname
	 * @return
	 */
	public boolean openUserChat(Selenium browser, String firstname, String surname){
		if(!openRoster(browser)){
			return(false);
		}

		StringBuffer selectorBuffer = new StringBuffer();
		selectorBuffer.append("xpath=//ul//li//a[contains(@class, '")
		.append(getInstantMessagingAvailableCss())
		.append("')]//span[text()=' ")
		.append(surname)
		.append(", ")
		.append(firstname)
		.append("']");

		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		
		return(true);
	}
	
	/**
	 * Opens a chat window with the given group.
	 * 
	 * @param browser
	 * @param groupName
	 * @return
	 */
	public boolean openGroupChat(Selenium browser, String groupName){
		if(!openRoster(browser)){
			return(false);
		}
		
		StringBuffer selectorBuffer = new StringBuffer();
		selectorBuffer.append("xpath=//ul//li//div[contains(@class, '")
		.append(getInstantMessagingGroupCss())
		.append("') and text()='")
		.append(groupName)
		.append("']//a");

		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		
		return(true);
	}
	
	/**
	 * Sends a message to the given user.
	 * 
	 * @param browser
	 * @param firstname
	 * @param surname
	 * @param message
	 * @return
	 */
	public boolean sendMessageToUser(Selenium browser, String firstname, String surname, String message){
		if(message == null){
			return(true);
		}
		
		if(!openRoster(browser)){
			return(false);
		}
		
		if(!openUserChat(browser, firstname, surname)){
			return(false);
		}
		
		StringBuffer selectorBuffer = new StringBuffer();
		selectorBuffer.append("xpath=//div[contains(@class, '")
		.append(getInstantMessagingChatCss())
		.append("')]//input[@type='text']");

		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.type(selectorBuffer.toString(), message);
		
		return(true);
	}
	
	/**
	 * Sends a message to the given group.
	 * 
	 * @param browser
	 * @param groupName
	 * @param message
	 * @return
	 */
	public boolean sendMessageToGroup(Selenium browser, String groupName, String message){
		if(message == null){
			return(true);
		}
		
		if(!openRoster(browser)){
			return(false);
		}
		
		if(!openGroupChat(browser, groupName)){
			return(false);
		}
		
		StringBuffer selectorBuffer = new StringBuffer();
		selectorBuffer.append("xpath=//div[contains(@class, '")
		.append(getInstantMessagingChatCss())
		.append("')]//input[@type='text']");

		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.type(selectorBuffer.toString(), message);
		
		return(true);	
	}

	public boolean waitForPageToLoadMessage(Selenium browser, String message){
		if(message == null){
			return(true);
		}
		
		StringBuffer selectorBuffer = new StringBuffer();
		selectorBuffer.append("xpath=//div[contains(@class, '")
		.append(getInstantMessagingChatCss())
		.append("')]//div[contains(@class, '")
		.append(getInstantMessagingBodyCss())
		.append("') and contains(text(), '")
		.append(message)
		.append("')]");
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		
		return(functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString()));
	}
	
	public String getInstantMessagingClientSummary() {
		return instantMessagingClientSummary;
	}

	public void setInstantMessagingClientSummary(
			String instantMessagingClientSummary) {
		this.instantMessagingClientSummary = instantMessagingClientSummary;
	}

	public String getInstantMessagingRosterCss() {
		return instantMessagingRosterCss;
	}

	public void setInstantMessagingRosterCss(String instantMessagingRosterCss) {
		this.instantMessagingRosterCss = instantMessagingRosterCss;
	}

	public String getInstantMessagingShowOfflineContactsCss() {
		return instantMessagingShowOfflineContactsCss;
	}

	public void setInstantMessagingShowOfflineContactsCss(
			String instantMessagingShowOfflineContactsCss) {
		this.instantMessagingShowOfflineContactsCss = instantMessagingShowOfflineContactsCss;
	}

	public String getInstantMessagingHideOfflineContactsCss() {
		return instantMessagingHideOfflineContactsCss;
	}

	public void setInstantMessagingHideOfflineContactsCss(
			String instantMessagingHideOfflineContactsCss) {
		this.instantMessagingHideOfflineContactsCss = instantMessagingHideOfflineContactsCss;
	}

	public String getInstantMessagingShowGroupsCss() {
		return instantMessagingShowGroupsCss;
	}

	public void setInstantMessagingShowGroupsCss(
			String instantMessagingShowGroupsCss) {
		this.instantMessagingShowGroupsCss = instantMessagingShowGroupsCss;
	}

	public String getInstantMessagingHideGroupsCss() {
		return instantMessagingHideGroupsCss;
	}

	public void setInstantMessagingHideGroupsCss(
			String instantMessagingHideGroupsCss) {
		this.instantMessagingHideGroupsCss = instantMessagingHideGroupsCss;
	}

	public String getInstantMessagingGroupCss() {
		return instantMessagingGroupCss;
	}

	public void setInstantMessagingGroupCss(String instantMessagingGroupCss) {
		this.instantMessagingGroupCss = instantMessagingGroupCss;
	}

	public String getInstantMessagingAvailableCss() {
		return instantMessagingAvailableCss;
	}

	public void setInstantMessagingAvailableCss(String instantMessagingAvailableCss) {
		this.instantMessagingAvailableCss = instantMessagingAvailableCss;
	}

	public String getInstantMessagingUnavailableCss() {
		return instantMessagingUnavailableCss;
	}

	public void setInstantMessagingUnavailableCss(
			String instantMessagingUnavailableCss) {
		this.instantMessagingUnavailableCss = instantMessagingUnavailableCss;
	}

	public String getInstantMessagingChatCss() {
		return instantMessagingChatCss;
	}

	public void setInstantMessagingChatCss(String instantMessagingChatCss) {
		this.instantMessagingChatCss = instantMessagingChatCss;
	}

	public String getInstantMessagingBodyCss() {
		return instantMessagingBodyCss;
	}

	public void setInstantMessagingBodyCss(String instantMessagingBodyCss) {
		this.instantMessagingBodyCss = instantMessagingBodyCss;
	}

	public FunctionalUtil getFunctionalUtil() {
		return functionalUtil;
	}

	public void setFunctionalUtil(FunctionalUtil functionalUtil) {
		this.functionalUtil = functionalUtil;
	}
}
