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
	
	public final static Pattern CONTACT_COUNT_PATTERN = Pattern.compile("([\\d])+/([\\d])+");
	
	public final static String INSTANT_MESSAGING_CLIENT_SUMMARY_CSS = "o_topnav_imclient_summary";
	
	public final static String INSTANT_MESSAGING_ROSTER_CSS = "o_extjsPanel_im_roster";
	public final static String INSTANT_MESSAGING_SHOW_OFFLINE_CONTACTS_CSS = "o_instantmessaging_showofflineswitch";
	public final static String INSTANT_MESSAGING_HIDE_OFFLINE_CONTACTS_CSS = "o_instantmessaging_hideofflineswitch";
	public final static String INSTANT_MESSAGING_SHOW_GROUPS_CSS = "o_instantmessaging_showgroupswitch";
	public final static String INSTANT_MESSAGING_HIDE_GROUPS_CSS = "o_instantmessaging_hidegroupswitch";
	
	public final static String INSTANT_MESSAGING_GROUP_CSS = "o_instantmessaging_groupname";
	public final static String INSTANT_MESSAGING_AVAILABLE_CSS = "o_instantmessaging_available_icon";
	
	private String instantMessagingClientSummaryCss;
	
	private String instantMessagingRosterCss;
	private String instantMessagingShowOfflineContactsCss;
	private String instantMessagingHideOfflineContactsCss;
	private String instantMessagingShowGroupsCss;
	private String instantMessagingHideGroupsCss;
	
	private String instantMessagingGroupCss;
	private String instantMessagingAvailableCss;
	
	private FunctionalUtil functionalUtil;
	
	public FunctionalInstantMessagingUtil(FunctionalUtil functionalUtil){
		setFunctionalUtil(functionalUtil);
		
		setInstantMessagingClientSummaryCss(INSTANT_MESSAGING_CLIENT_SUMMARY_CSS);
		
		setInstantMessagingRosterCss(INSTANT_MESSAGING_ROSTER_CSS);
		setInstantMessagingShowOfflineContactsCss(INSTANT_MESSAGING_SHOW_OFFLINE_CONTACTS_CSS);
		setInstantMessagingHideOfflineContactsCss(INSTANT_MESSAGING_HIDE_OFFLINE_CONTACTS_CSS);
		setInstantMessagingShowGroupsCss(INSTANT_MESSAGING_SHOW_GROUPS_CSS);
		setInstantMessagingHideGroupsCss(INSTANT_MESSAGING_HIDE_GROUPS_CSS);
		
		setInstantMessagingAvailableCss(INSTANT_MESSAGING_AVAILABLE_CSS);
	}
	
	/**
	 * Computes the chat summary in the top navigation.
	 * 
	 * @param browser
	 * @return The count of online contacts.
	 */
	public int onlineContactCount(Selenium browser){
		functionalUtil.idle(browser);
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//div[contains(@class, '")
		.append(getInstantMessagingClientSummaryCss())
		.append("')]//a//span");
		
		String summary = browser.getText(selectorBuffer.toString());
		
		Matcher matcher = CONTACT_COUNT_PATTERN.matcher(summary);
		
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
		functionalUtil.idle(browser);
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//div[contains(@class, '")
		.append(getInstantMessagingClientSummaryCss())
		.append("')]//a//span");
		
		String summary = browser.getText(selectorBuffer.toString());
		
		Matcher matcher = CONTACT_COUNT_PATTERN.matcher(summary);
		
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
		
		selectorBuffer.append("xpath=//div[contains(@class, '")
		.append(getInstantMessagingClientSummaryCss())
		.append("')]//a");
		
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
	
	/**
	 * Finds the offline contacts by parsing roster data.
	 * 
	 * @param browser
	 * @return A List containing contact names.
	 */
	public List<String> findOfflineContacts(Selenium browser){
		if(!openRoster(browser)){
			return(null);
		}
		
		if(!openOfflineContacts(browser)){
			return(null);
		}

		functionalUtil.idle(browser);
		
		List<String> contacts = new ArrayList<String>();
		
		StringBuffer selectorBufferFragment = new StringBuffer();
		selectorBufferFragment.append("//ul//li//a[contains(@class, '")
		.append(getInstantMessagingAvailableCss())
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
			.append("]//span");
			
			contacts.add(browser.getText(selectorBuffer.toString()));
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
			
			groups.add(browser.getText(selectorBuffer.toString()));
		}
		
		return(groups);
	}
	
	public boolean openUserChat(Selenium browser, String userName){
		if(!openRoster(browser)){
			return(false);
		}
		
		//TODO:JK: implement me
		
		return(false);
	}
	
	public boolean openGroupChat(Selenium browser, String groupName){
		if(!openRoster(browser)){
			return(false);
		}
		
		//TODO:JK: implement me
		
		return(false);
	}
	
	public boolean sendMessageToUser(Selenium browser, String userName, String message){
		if(!openRoster(browser)){
			return(false);
		}
		
		//TODO:JK: implement me
		
		/* using roster failed, so let's try visiting card */
		
		return(false);
	}
	
	public boolean sendMessageToGroup(Selenium browser, String groupName, String message){
		if(!openRoster(browser)){
			return(false);
		}
		
		//TODO:JK: implement me
		
		return(false);	
	}

	public String getInstantMessagingClientSummaryCss() {
		return instantMessagingClientSummaryCss;
	}

	public void setInstantMessagingClientSummaryCss(
			String instantMessagingClientSummaryCss) {
		this.instantMessagingClientSummaryCss = instantMessagingClientSummaryCss;
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

	public FunctionalUtil getFunctionalUtil() {
		return functionalUtil;
	}

	public void setFunctionalUtil(FunctionalUtil functionalUtil) {
		this.functionalUtil = functionalUtil;
	}
}
