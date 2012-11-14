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

import org.apache.commons.lang.ArrayUtils;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.util.FunctionalUtil.OlatSite;

import com.thoughtworks.selenium.Selenium;

/**
 * 
 * @author jkraehemann, joel.kraehemann@frentix.com, frentix.com
 */
public class FunctionalGroupsSiteUtil {
	private final static OLog log = Tracing.createLoggerFor(FunctionalGroupsSiteUtil.class);
	
	public final static String GROUP_ICON_CSS = "b_group_icon";
	public final static String CREATE_GROUP_CSS = "o_sel_group_create";
	
	public final static String CREATE_GROUP_DIALOG_CSS = "o_sel_group_edit_group_form";
	public final static String CREATE_GROUP_WAITING_LIST_VALUE = "create.form.enableWaitinglist";
	public final static String CREATE_GROUP_AUTO_CLOSE_RANKS_VALUE = "create.form.enableAutoCloseRanks";
	
	public final static String SEARCH_GROUP_BUTTONS_CSS = "o_sel_group_search_groups_buttons";
	
	public enum GroupsSiteAction {
		MY_GROUPS("o_sel_MyGroups"),
		PUBLISHED_GROUPS("o_sel_OpenGroups"),
		GROUPS_ADMINISTRATION("o_sel_AdminGroups");
		
		private String actionCss;
		
		GroupsSiteAction(String actionCss){
			setActionCss(actionCss);
		}

		public String getActionCss() {
			return actionCss;
		}

		public void setActionCss(String actionCss) {
			this.actionCss = actionCss;
		}
	}
	
	public enum MyGroupsTabs {
		BOOKMARK,
		ALL_GROUPS,
		COACH,
		SEARCH,
	}

	public enum PublishedGroupsTabs {
		ALL_GROUPS,
		SEARCH,
	}
	
	public enum GroupOptions {
		WAITING_LIST,
		AUTO_MOVING_UP,
	}
	
	public enum MyGroupSearchFields {
		ID("o_sel_group_search_id_field"),
		NAME("o_sel_group_search_name_field"),
		COACH("o_sel_group_search_owner_field"),
		DESCRIPTION("o_sel_group_search_description_field"),
		COURSE_TITLE("o_sel_group_search_course_field ");
		
		private String cssClass;
		
		MyGroupSearchFields(String cssClass){
			setCssClass(cssClass);
		}

		public String getCssClass() {
			return cssClass;
		}

		public void setCssClass(String cssClass) {
			this.cssClass = cssClass;
		}
	}
	
	public enum PublicGroupSearchFields {
		ID("o_sel_group_search_id_field"),
		NAME("o_sel_group_search_name_field"),
		COACH("o_sel_group_search_owner_field"),
		DESCRIPTION("o_sel_group_search_description_field");
		
		private String cssClass;
		
		PublicGroupSearchFields(String cssClass){
			setCssClass(cssClass);
		}

		public String getCssClass() {
			return cssClass;
		}

		public void setCssClass(String cssClass) {
			this.cssClass = cssClass;
		}
	}
	
	public enum GroupTools {
		INFORMATION,
		EMAIL,
		CALENDAR,
		FOLDER,
		FORUM,
		WIKI,
		EPORTFOLIO;
	}
	
	public enum GroupsTabAction {
		INFORMATION("o_news_icon"),
		CALENDAR("o_calendar_icon"),
		EMAIL("o_co_icon"),
		FOLDER("o_bc_icon"),
		FORUM("o_fo_icon"),
		WIKI("o_wiki_icon"),
		PORTFOLIO("o_ep_icon"),
		ADMINISTRATION("o_admin_icon"),
		BOOKING("b_order_icon");
		
		private String iconCss;
		
		GroupsTabAction(String iconCss){
			setIconCss(iconCss);
		}

		public String getIconCss() {
			return iconCss;
		}

		public void setIconCss(String iconCss) {
			this.iconCss = iconCss;
		}
	}
	
	public enum AdministrationTabs {
		DESCRIPTION,
		TOOLS,
		MEMBERS,
		COURSES,
		PUBLISHING_AND_BOOKING;
	}

	private String groupIconCss;
	private String createGroupCss;
	
	private String createGroupDialogCss;
	private String createGroupWaitingListValue;
	private String createGroupAutoCloseRanksValue;
	
	private String searchGroupButtonsCss;
	
	private FunctionalUtil functionalUtil;
	
	public FunctionalGroupsSiteUtil(FunctionalUtil functionalUtil){
		this.groupIconCss = GROUP_ICON_CSS;
		this.createGroupCss = CREATE_GROUP_CSS;
		
		this.createGroupDialogCss = CREATE_GROUP_DIALOG_CSS;
		this.createGroupWaitingListValue = CREATE_GROUP_WAITING_LIST_VALUE;
		this.createGroupAutoCloseRanksValue = CREATE_GROUP_AUTO_CLOSE_RANKS_VALUE;
		
		this.searchGroupButtonsCss = SEARCH_GROUP_BUTTONS_CSS;
		
		this.functionalUtil = functionalUtil;
	}
	
	/**
	 * Browse the groups site's navigation.
	 * 
	 * @param browser
	 * @param action
	 * @return true on success otherwise false
	 */
	public boolean openActionByMenuTree(Selenium browser, Object action){
		functionalUtil.idle(browser);
		
		StringBuffer selectorBuffer;
		
		if(action instanceof GroupsSiteAction){
			selectorBuffer = new StringBuffer();
			
			selectorBuffer.append("xpath=//li[contains(@class, '")
			.append(((GroupsSiteAction) action).getActionCss())
			.append("')]//a[contains(@class, '")
			.append(functionalUtil.getTreeLevel0Css())
			.append("')]");;
		}else{
			return(false);
		}

		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		
		return(true);
	}

	/**
	 * @param browser
	 * @param groupName
	 * @param groupDescription
	 * @param maxParticipants
	 * @param options
	 * @return
	 * 
	 * Create a group with the given configuration.
	 */
	public boolean createGroup(Selenium browser, String groupName, String groupDescription, int maxParticipants, GroupOptions[] options){
		if(!functionalUtil.openSite(browser, OlatSite.GROUPS)){
			return(false);
		}
		
		if(!openActionByMenuTree(browser, GroupsSiteAction.MY_GROUPS)){
			return(false);
		}
		
		if(!functionalUtil.openContentSegment(browser, MyGroupsTabs.ALL_GROUPS.ordinal())){
			return(false);
		}
		
		/* click create */
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//a[contains(@class, '")
		.append(getCreateGroupCss())
		.append("')]");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		
		/* fill in group name */
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=(//div[contains(@class, '")
		.append(getCreateGroupDialogCss())
		.append("')]//input[@type='text'])[1]");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.type(selectorBuffer.toString(), groupName);
		
		/* fill in group description */
		functionalUtil.typeMCE(browser, getCreateGroupDialogCss(), groupDescription);
		
		/* fill in max participants */
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=(//div[contains(@class, '")
		.append(getCreateGroupDialogCss())
		.append("')]//input[@type='text'])[2]");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.type(selectorBuffer.toString(), Integer.toString(maxParticipants));
		
		/* set options */
		if(options != null){
			if(ArrayUtils.contains(options, GroupOptions.WAITING_LIST)){
				functionalUtil.clickCheckbox(browser, null, getCreateGroupWaitingListValue());
			}
			
			if(ArrayUtils.contains(options, GroupOptions.AUTO_MOVING_UP)){
				functionalUtil.clickCheckbox(browser, null, getCreateGroupAutoCloseRanksValue());
			}
		}

		/* click finish */
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=(//div[contains(@class, '")
		.append(getCreateGroupDialogCss())
		.append("')]//button[contains(@class, '")
		.append(functionalUtil.getButtonDirtyCss())
		.append("')])[last()]");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		
		return(true);
	}
	
	/**
	 * @param browser
	 * @param groupName
	 * @return
	 * 
	 * Opens the given my group.
	 */
	public boolean openMyGroup(Selenium browser, String groupName){
		if(!functionalUtil.openSite(browser, OlatSite.GROUPS)){
			return(false);
		}
		
		if(!openActionByMenuTree(browser, GroupsSiteAction.MY_GROUPS)){
			return(false);
		}
		
		if(!functionalUtil.openContentSegment(browser, MyGroupsTabs.SEARCH.ordinal())){
			return(false);
		}
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//form//div[contains(@class, '")
		.append(MyGroupSearchFields.NAME.getCssClass())
		.append("')]//input[@type='text']");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		
		browser.type(selectorBuffer.toString(), groupName);
		
		/* click search */
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//div[contains(@class, '")
		.append(getSearchGroupButtonsCss())
		.append("')]//button[contains(@class, '")
		.append(functionalUtil.getButtonDirtyCss())
		.append("')]");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		
		/* open the group */
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//td//a/span[contains(@class, '")
		.append(getGroupIconCss())
		.append("')]/..");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		
		return(true);
	}
	
	/**
	 * @param browser
	 * @param groupName
	 * @return
	 * 
	 * Opens the given published group.
	 */
	public boolean openPublishedGroup(Selenium browser, String groupName){
		if(!functionalUtil.openSite(browser, OlatSite.GROUPS)){
			return(false);
		}
		
		if(!openActionByMenuTree(browser, GroupsSiteAction.PUBLISHED_GROUPS)){
			return(false);
		}
		
		if(!functionalUtil.openContentSegment(browser, PublishedGroupsTabs.SEARCH.ordinal())){
			return(false);
		}
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//form//div[contains(@class, '")
		.append(PublicGroupSearchFields.NAME.getCssClass())
		.append("')]//input[@type='text']");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		
		browser.type(selectorBuffer.toString(), groupName);
		
		/* click search */
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//div[contains(@class, '")
		.append(getSearchGroupButtonsCss())
		.append("')]//button[contains(@class, '")
		.append(functionalUtil.getButtonDirtyCss())
		.append("')]");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		
		/* open the group */
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//td//a/span[contains(@class, '")
		.append(getGroupIconCss())
		.append("')]/..");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		
		return(true);
	}
	
	public GroupsTabAction findGroupTabActionForTool(GroupTools tool){
		GroupsTabAction action = null;
		
		switch(tool){
		case CALENDAR:
		{
			action = GroupsTabAction.CALENDAR;
		}
		break;
		case EMAIL:
		{
			action = GroupsTabAction.EMAIL;
		}
		break;
		case FOLDER:
		{
			action = GroupsTabAction.FOLDER;
		}
		break;
		case FORUM:
		{
			action = GroupsTabAction.FORUM;
		}
		break;
		case WIKI:
		{
			action = GroupsTabAction.WIKI;
		}
		break;
		case EPORTFOLIO:
		{
			action = GroupsTabAction.PORTFOLIO;
		}
		break;
		case INFORMATION:
		{
			action = GroupsTabAction.INFORMATION;
		}
		break;
		}
		
		return(action);
	}
	
	/**
	 * @param browser
	 * @param action
	 * @return
	 * 
	 * Opens the appropriate action.
	 */
	public boolean openGroupsTabActionByMenuTree(Selenium browser, GroupsTabAction action){
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//ul[contains(@class, '")
		.append(functionalUtil.getTreeLevel1Css())
		.append("')]//li//a[contains(@class, '")
		.append(action.getIconCss())
		.append("')]");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		
		return(true);
	}
	
	/**
	 * @param browser
	 * @param tools
	 * @return
	 * 
	 * Toggle specified tools.
	 */
	public boolean applyTools(Selenium browser, GroupTools[] tools){
		if(!openGroupsTabActionByMenuTree(browser, GroupsTabAction.ADMINISTRATION)){
			return(false);
		}
		
		if(!functionalUtil.openContentTab(browser, AdministrationTabs.TOOLS.ordinal())){
			return(false);
		}
		
		if(tools == null)
			return(true);
		
		if(ArrayUtils.contains(tools, GroupTools.INFORMATION)){
			functionalUtil.clickCheckbox(browser, null, Integer.toString(GroupTools.INFORMATION.ordinal()));
			functionalUtil.idle(browser);
		}
		
		if(ArrayUtils.contains(tools, GroupTools.EMAIL)){
			functionalUtil.clickCheckbox(browser, null, Integer.toString(GroupTools.EMAIL.ordinal()));
			functionalUtil.idle(browser);
		}
		
		if(ArrayUtils.contains(tools, GroupTools.CALENDAR)){
			functionalUtil.clickCheckbox(browser, null, Integer.toString(GroupTools.CALENDAR.ordinal()));
			functionalUtil.idle(browser);
		}
		
		if(ArrayUtils.contains(tools, GroupTools.FOLDER)){
			functionalUtil.clickCheckbox(browser, null, Integer.toString(GroupTools.FOLDER.ordinal()));
			functionalUtil.idle(browser);
		}
		
		if(ArrayUtils.contains(tools, GroupTools.FORUM)){
			functionalUtil.clickCheckbox(browser, null, Integer.toString(GroupTools.FORUM.ordinal()));
			functionalUtil.idle(browser);
		}
		
		if(ArrayUtils.contains(tools, GroupTools.WIKI)){
			functionalUtil.clickCheckbox(browser, null, Integer.toString(GroupTools.WIKI.ordinal()));
			functionalUtil.idle(browser);
		}
		
		if(ArrayUtils.contains(tools, GroupTools.EPORTFOLIO)){
			functionalUtil.clickCheckbox(browser, null, Integer.toString(GroupTools.EPORTFOLIO.ordinal()));
			functionalUtil.idle(browser);
		}
		
		return(true);
	}
	
	/**
	 * @param browser
	 * @param information
	 * @return
	 * 
	 * Sets the information for members.
	 */
	public boolean applyInformationForMembers(Selenium browser, String information){
		if(!openGroupsTabActionByMenuTree(browser, GroupsTabAction.ADMINISTRATION)){
			return(false);
		}
		
		if(!functionalUtil.openContentTab(browser, AdministrationTabs.TOOLS.ordinal())){
			return(false);
		}
		
		functionalUtil.typeMCE(browser, information);
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//button[contains(@class, '")
		.append(functionalUtil.getButtonDirtyCss())
		.append("')]");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		
		return(true);
	}
	
	public FunctionalUtil getFunctionalUtil() {
		return functionalUtil;
	}

	public void setFunctionalUtil(FunctionalUtil functionalUtil) {
		this.functionalUtil = functionalUtil;
	}

	public String getGroupIconCss() {
		return groupIconCss;
	}

	public void setGroupIconCss(String groupIconCss) {
		this.groupIconCss = groupIconCss;
	}

	public String getCreateGroupCss() {
		return createGroupCss;
	}

	public void setCreateGroupCss(String createGroupCss) {
		this.createGroupCss = createGroupCss;
	}

	public String getCreateGroupDialogCss() {
		return createGroupDialogCss;
	}

	public void setCreateGroupDialogCss(String createGroupDialogCss) {
		this.createGroupDialogCss = createGroupDialogCss;
	}

	public String getCreateGroupWaitingListValue() {
		return createGroupWaitingListValue;
	}

	public void setCreateGroupWaitingListValue(String createGroupWaitingListValue) {
		this.createGroupWaitingListValue = createGroupWaitingListValue;
	}

	public String getCreateGroupAutoCloseRanksValue() {
		return createGroupAutoCloseRanksValue;
	}

	public void setCreateGroupAutoCloseRanksValue(
			String createGroupAutoCloseRanksValue) {
		this.createGroupAutoCloseRanksValue = createGroupAutoCloseRanksValue;
	}

	public String getSearchGroupButtonsCss() {
		return searchGroupButtonsCss;
	}

	public void setSearchGroupButtonsCss(String searchGroupButtonsCss) {
		this.searchGroupButtonsCss = searchGroupButtonsCss;
	}
}
