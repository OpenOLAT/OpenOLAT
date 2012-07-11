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

import org.junit.Assert;
import org.olat.util.FunctionalUtil.OlatSite;

import com.thoughtworks.selenium.Selenium;

/**
 * 
 * @author jkraehemann, joel.kraehemann@frentix.com, frentix.com
 */
public class FunctionalHomeSiteUtil {
	
	public final static String HOME_SITE_MENU_TREE_SELECTED_CSS = "b_tree_selected";
	
	public final static String FORM_ELEMENT_WRAPPER = "b_form_element_wrapper";
	public final static String CLEARFIX = "b_clearfix";
	
	/* menu tree */
	public final static String HOME_ACTION_PORTAL_CSS = "o_sel_portal";
	public final static String HOME_ACTION_SETTINGS_CSS = "o_sel_mysettings";
	public final static String HOME_ACTION_CALENDAR_CSS = "o_sel_calendar";
	public final static String HOME_ACTION_SUBSCRIPTIONS_CSS = "o_sel_notifications";
	public final static String HOME_ACTION_BOOKMARKS_CSS = "o_sel_bookmarks";
	public final static String HOME_ACTION_PERSONAL_FOLDER_CSS = "o_sel_userfolder";
	public final static String HOME_ACTION_NOTES_CSS = "o_sel_notelist";
	public final static String HOME_ACTION_EVIDENCES_OF_ACHIEVEMENT_CSS = "o_sel_effstatements";
	
	public final static String EPORTFOLIO_ACTION_MY_ARTIFACTS_CSS = "o_sel_EPArtefacts";
	public final static String EPORTFOLIO_ACTION_MY_BINDERS_CSS = "o_sel_EPMaps";
	public final static String EPORTFOLIO_ACTION_MY_PORTFOLIO_TASKS_CSS = "o_sel_EPStructuredMaps";
	public final static String EPORTFOLIO_ACTION_RELEASED_BINDERS_CSS = "o_sel_EPSharedMaps";
	
	/* portal */
	public final static String PORTAL_EDIT_LINK_CSS = "o_home_portaleditlink";
	
	public final static String PORTAL_WRAPPER_CSS = "b_portal_wrapper";
	public final static String PORTAL_COLUMN_CSS_PREFIX = "o_sel_portal_col_";
	public final static String PORTAL_SUBCOLUMNS_CSS = "b_subcolumns";
	public final static String PORTAL_INACTIVE_CSS = "b_portal_inactive";
	
	public final static String PORTLET_CSS = "b_portlet";
	public final static String PORTLET_EDIT_CSS = "b_portlet_edit";
	public final static String PORTLET_INACTIVE_CSS = "b_portlet_incactive";
	
	public final static String PORTLET_QUICKSTART_CSS = "o_portlet_quickstart";
	public final static String PORTLET_REPOSITORY_STUDENT_CSS = "o_portlet_repository_student";
	public final static String PORTLET_REPOSITORY_TEACHER_CSS = "o_portlet_repository_teacher";
	public final static String PORTLET_INFOMESSAGES_CSS = "o_portlet_infomessages";
	public final static String PORTLET_CALENDAR_CSS = "o_portlet_calendar";
	public final static String PORTLET_BOOKMARK_CSS = "o_portlet_bookmark";
	public final static String PORTLET_GROUPS_CSS = "o_portlet_groups";
	public final static String PORTLET_NOTI_CSS = "o_portlet_noti";
	public final static String PORTLET_EFF_CSS = "o_portlet_eff";
	public final static String PORTLET_NOTES_CSS = "o_portlet_notes";
	public final static String PORTLET_DYK_CSS = "o_portlet_dyk";
	
	public final static String PORTLET_MOVE_LEFT_CSS = "b_portlet_edit_left";
	public final static String PORTLET_MOVE_RIGHT_CSS = "b_portlet_edit_right";
	public final static String PORTLET_MOVE_UPWARDS_CSS = "b_portlet_edit_up";
	public final static String PORTLET_MOVE_DOWNWARDS_CSS = "b_portlet_edit_down";
	public final static String PORTLET_EDIT_INACTIVATE_CSS = "b_portlet_edit_delete";
	public final static String PORTLET_EDIT_ACTIVATE_CSS = "b_portlet_add";
	
	/* General System Settings */
	public final static String PREFS_BUTTONS_CSS = "o_sel_home_settings_prefs_buttons";
	
	public final static String LANGUAGE_OPTIONS_ID = "o_fioform_language_SELBOX";
	
	public final static String GERMAN_LANGUAGE_VALUE = "de";
	public final static String ENGLISH_LANGUAGE_VALUE = "en";
	public final static String FRENCH_LANGUAGE_VALUE = "fr";
	public final static String SPANISH_LANGUAGE_VALUE = "es";
	public final static String ITALIAN_LANGUAGE_VALUE = "it";
	
	/* Specific System Settings */
	public final static String GUI_BUTTONS_CSS = "o_sel_home_settings_gui_buttons";
	
	public final static String ACCESSIBILITY_CSS = "o_sel_home_settings_accessibility";
	public final static String RESUME_LAST_SESSION_CSS = "o_sel_home_settings_resume";
	public final static String SUPPORT_FOR_BROWSER_BACK_CSS = "o_sel_home_settings_back_enabling";
	
	public final static String NO_VALUE = "none";
	public final static String YES_AUTOMATICALLY_VALUE = "auto";
	public final static String YES_ON_REQUEST_VALUE = "request";
	
	public final static String OFF_VALUE = "no";
	public final static String ON_VALUE = "yes";
	
	/* Reset Configurations */
	public final static String SYSPREFS_BUTTONS_CSS = "o_sel_home_settings_reset_sysprefs_buttons";
	
	public final static String CONFIGURATIONS_CSS = "o_sel_home_settings_reset_sysprefs";
	
	public final static String GUI_PREFERENCES_VALUE = "guiprefs";
	public final static String SYS_PREFERENCES_VALUE = "sysprefs";
	public final static String RESUME_VALUE = "resume";
	
	/* password tab */
	//TODO:JK: add CSS classes to olat
	public final static String OLD_PASSWORD_CSS = "";
	public final static String NEW_PASSWORD_CSS = "";
	public final static String CONFIRM_PASSWORD_CSS = "";
	
	public enum PortalSettingsForms {
		GENERAL_SYSTEM_SETTINGS,
		SPECIFIC_SYSTEM_SETTINGS,
		RESET_CONFIGURATIONS;
		
		public enum SpecificSystemSettingsRadios {
			ACCESSIBILITY(ACCESSIBILITY_CSS),
			RESUME_LAST_SESSION(RESUME_LAST_SESSION_CSS),	
			SUPPORT_FOR_BROWSER_BACK(SUPPORT_FOR_BROWSER_BACK_CSS);
			
			private String groupCss;
			
			SpecificSystemSettingsRadios(String groupCss){
				setGroupCss(groupCss);
			}
			
			public String getGroupCss() {
				return groupCss;
			}

			public void setGroupCss(String groupCss) {
				this.groupCss = groupCss;
			}

			public enum ResumeLastSession{
				NO(NO_VALUE),
				YES_AUTOMATICALLY(YES_AUTOMATICALLY_VALUE),
				YES_ON_REQUEST(YES_ON_REQUEST_VALUE);
				
				private String valueAttribute;
				
				ResumeLastSession(String valueAttribute){
					setValueAttribute(valueAttribute);
				}

				public String getValueAttribute() {
					return valueAttribute;
				}

				public void setValueAttribute(String valueAttribute) {
					this.valueAttribute = valueAttribute;
				}
			}
			
			public enum SupportForBrowserBack {
				ON(ON_VALUE),
				OFF(OFF_VALUE);
				
				private String valueAttribute;
				
				SupportForBrowserBack(String valueAttribute){
					setValueAttribute(valueAttribute);
				}

				public String getValueAttribute() {
					return valueAttribute;
				}

				public void setValueAttribute(String valueAttribute) {
					this.valueAttribute = valueAttribute;
				}
			}
		}
		

		public enum ResetConfigurations{
			Configurations(CONFIGURATIONS_CSS);
			
			private String groupCss;
			
			ResetConfigurations(String groupCss){
				setGroupCss(groupCss);
			}
			
			public String getGroupCss() {
				return groupCss;
			}

			public void setGroupCss(String groupCss) {
				this.groupCss = groupCss;
			}

			public enum ConfigurationsCheckboxes {
				GUI_PREFERENCES(GUI_PREFERENCES_VALUE),
				SYS_PREFERENCES(SYS_PREFERENCES_VALUE),
				RESUME(RESUME_VALUE);
				
				private String valueAttribute;

				ConfigurationsCheckboxes (String valueAttribute){
					setValueAttribute(valueAttribute);
				}
				
				
				public String getValueAttribute() {
					return valueAttribute;
				}

				public void setValueAttribute(String valueAttribute) {
					this.valueAttribute = valueAttribute;
				}
			}
		}
	}
	
	public enum HomeSiteAction {
		PORTAL,
		SETTINGS,
		CALENDAR,
		SUBSCRIPTIONS,
		BOOKMARKS,
		PERSONAL_FOLDER,
		NOTES,
		EVIDENCES_OF_ACHIEVEMENT,
		OTHER_USERS, /* no corresponding CSS class available */
	};
	
	public enum EPortfolioAction {
		MY_ARTIFACTS,
		MY_BINDERS,
		MY_PORTFOLIO_TASKS,
		RELEASED_BINDERS,
	};
	
	public enum Direction {
		UP,
		DOWN,
		LEFT,
		RIGHT;
	}

	public enum SettingsTab {
		PROFILE,
		SYSTEM,
		PASSWORD,
		WEB_DAV,
		TERMS_OF_USE;
	}
	
	private FunctionalUtil functionalUtil;
	
	private String homeActionNavigationSelectedCss;
	
	private String homeActionPortalCss;
	private String homeActionSettingsCss;
	private String homeActionCalendarCss;
	private String homeActionSubscriptionsCss;
	private String homeActionBookmarksCss;
	private String homeActionPersonalFolderCss;
	private String homeActionNotesCss;
	private String homeActionEvidencesOfAchievementCss;

	private String ePortfolioActionMyArtifactsCss;
	private String ePortfolioActionMyBindersCss;
	private String ePortfolioActionMyPortfolioTasksCss;
	private String ePortfolioActionReleasedBindersCss;
	
	private String portalEditLinkCss;
	private String portalWrapperCss;
	private String portalColumnCssPrefix;
	private String portalSubcolumnsCss;
	private String portalInactiveCss;
	
	private String portletCss;
	private String portletEditCss;
	private String portletInactiveCss;
	
	private String portletQuickstartCss;
	private String portletRepositoryStudentCss;
	private String portletRepositoryTeacherCss;
	private String portletInfomessagesCss;
	private String portletCalendarCss;
	private String portletBookmarkCss;
	private String portletGroupsCss;
	private String portletNotiCss;
	private String portletEffCss;
	private String portletNotesCss;
	private String portletDykCss;
	
	private String portletActivateCss;
	private String portletInactivateCss;
	private String portletMoveLeftCss;
	private String portletMoveRightCss;
	private String portletMoveUpCss;
	private String portletMoveDownCss;
	
	private String prefsButtonsCss;
	private String guiButtonsCss;
	private String sysprefsButtonsCss;

	private String oldPasswordCss;
	private String newPasswordCss;
	private String confirmPasswordCss;
	
	/**
	 * @param functionalUtil
	 * 
	 * Constructor
	 */
	public FunctionalHomeSiteUtil(FunctionalUtil functionalUtil){
		setUtil(functionalUtil);
		
		setHomeActionNavigationSelectedCss(HOME_SITE_MENU_TREE_SELECTED_CSS);
		
		setHomeActionPortalCss(HOME_ACTION_PORTAL_CSS);
		setHomeActionSettingsCss(HOME_ACTION_SETTINGS_CSS);
		setHomeActionCalendarCss(HOME_ACTION_CALENDAR_CSS);
		setHomeActionSubscriptionsCss(HOME_ACTION_SUBSCRIPTIONS_CSS);
		setHomeActionBookmarksCss(HOME_ACTION_BOOKMARKS_CSS);
		setHomeActionPersonalFolderCss(HOME_ACTION_PERSONAL_FOLDER_CSS);
		setHomeActionNotesCss(HOME_ACTION_NOTES_CSS);
		setHomeActionEvidencesOfAchievementCss(HOME_ACTION_EVIDENCES_OF_ACHIEVEMENT_CSS);
		
		setEPortfolioActionMyArtifactsCss(EPORTFOLIO_ACTION_MY_ARTIFACTS_CSS);
		setEPortfolioActionMyBindersCss(EPORTFOLIO_ACTION_MY_BINDERS_CSS);
		setEPortfolioActionMyPortfolioTasksCss(EPORTFOLIO_ACTION_MY_PORTFOLIO_TASKS_CSS);
		setEPortfolioActionReleasedBindersCss(EPORTFOLIO_ACTION_RELEASED_BINDERS_CSS);
		
		setPortalEditLinkCss(PORTAL_EDIT_LINK_CSS);
		setPortalWrapperCss(PORTAL_WRAPPER_CSS);
		setPortalColumnCssPrefix(PORTAL_COLUMN_CSS_PREFIX);
		setPortalSubcolumnsCss(PORTAL_SUBCOLUMNS_CSS);
		
		setPortletCss(PORTLET_CSS);
		setPortletEditCss(PORTLET_EDIT_CSS);
		setPortletInactiveCss(PORTLET_INACTIVE_CSS);
		
		setPortletQuickstartCss(PORTLET_QUICKSTART_CSS);
		setPortletRepositoryStudentCss(PORTLET_REPOSITORY_STUDENT_CSS);
		setPortletRepositoryTeacherCss(PORTLET_REPOSITORY_TEACHER_CSS);
		setPortletInfomessagesCss(PORTLET_INFOMESSAGES_CSS);
		setPortletCalendarCss(PORTLET_CALENDAR_CSS);
		setPortletBookmarkCss(PORTLET_BOOKMARK_CSS);
		setPortletGroupsCss(PORTLET_GROUPS_CSS);
		setPortletNotiCss(PORTLET_NOTI_CSS);
		setPortletEffCss(PORTLET_EFF_CSS);
		setPortletNotesCss(PORTLET_NOTES_CSS);
		setPortletDykCss(PORTLET_DYK_CSS);
		
		setPortletActivateCss(PORTLET_EDIT_ACTIVATE_CSS);
		setPortletInactivateCss(PORTLET_EDIT_INACTIVATE_CSS);
		setPortletMoveLeftCss(PORTLET_MOVE_LEFT_CSS);
		setPortletMoveRightCss(PORTLET_MOVE_RIGHT_CSS);
		setPortletMoveUpCss(PORTLET_MOVE_UPWARDS_CSS);
		setPortletMoveDownCss(PORTLET_MOVE_DOWNWARDS_CSS);
		
		setPrefsButtonsCss(FORM_ELEMENT_WRAPPER + " " + PREFS_BUTTONS_CSS + " " + CLEARFIX);
		setGuiButtonsCss(FORM_ELEMENT_WRAPPER + " " + GUI_BUTTONS_CSS + " " + CLEARFIX);
		setSysprefsButtonsCss(FORM_ELEMENT_WRAPPER + " " + SYSPREFS_BUTTONS_CSS + " " + CLEARFIX);
		
		setOldPasswordCss(OLD_PASSWORD_CSS);
		setNewPasswordCss(NEW_PASSWORD_CSS);
		setConfirmPasswordCss(CONFIRM_PASSWORD_CSS);
	}
	
	/**
	 * @param page
	 * @return the matching CSS class
	 * 
	 * Find the corresponding CSS class for page.
	 */
	public String findCssClassOfAction(Object page){
		if(page == null)
			return(null);
		
		String selectedCss = null;

		if(page instanceof HomeSiteAction){
			switch((HomeSiteAction) page){
			case PORTAL:
			{
				selectedCss = getHomeActionPortalCss();
				break;
			}
			case SETTINGS:
			{
				selectedCss = getHomeActionSettingsCss();
				break;
			}
			case CALENDAR:
			{
				selectedCss = getHomeActionCalendarCss();
				break;
			}
			case SUBSCRIPTIONS:
			{
				selectedCss = getHomeActionSubscriptionsCss();
				break;
			}
			case BOOKMARKS:
			{
				selectedCss = getHomeActionBookmarksCss();
				break;
			}
			case PERSONAL_FOLDER:
			{
				selectedCss = getHomeActionPersonalFolderCss();
				break;
			}
			case NOTES:
			{
				selectedCss = getHomeActionNotesCss();
				break;
			}
			case EVIDENCES_OF_ACHIEVEMENT:
				selectedCss = getHomeActionEvidencesOfAchievementCss();
				break;
			}
		}else if(page instanceof EPortfolioAction){
			switch((EPortfolioAction) page){
			case MY_ARTIFACTS:
			{
				selectedCss = getEPortfolioActionMyArtifactsCss();
				break;
			}
			case MY_BINDERS:
			{
				selectedCss = getEPortfolioActionMyBindersCss();
				break;
			}
			case MY_PORTFOLIO_TASKS:
			{
				selectedCss = getEPortfolioActionMyPortfolioTasksCss();
				break;
			}
			case RELEASED_BINDERS:
			{
				selectedCss = getEPortfolioActionReleasedBindersCss();
				break;
			}
			}
		}
		
		return(selectedCss);
	}
	
	/**
	 * @param browser
	 * @param action
	 * @return true if match otherwise false
	 * 
	 * Check if the correct page is open.
	 */
	public boolean checkCurrentAction(Selenium browser, Object action){
		String selectedCss = findCssClassOfAction(action);
		
		if(selectedCss == null)
			return(false);
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("css=ul li.")
		.append(selectedCss)
		.append('.')
		.append(getHomeActionNavigationSelectedCss())
		.append(" * a");
		
		if(browser.isElementPresent(selectorBuffer.toString())){
			return(true);
		}else{
			return(false);
		}
	}
	
	/**
	 * @param browser
	 * @param action
	 * @return true on success otherwise false
	 * 
	 * Browse the home site's menu tree.
	 */
	public boolean openActionByMenuTree(Selenium browser, Object action){
		String selectedCss = findCssClassOfAction(action);
		
		if(selectedCss == null){
			return(false);
		}
		
		if(!checkCurrentAction(browser, action)){
			StringBuffer selectorBuffer = new StringBuffer();
			
			selectorBuffer.append("css=ul .")
			.append(selectedCss)
			.append(" * a");
			
			browser.click(selectorBuffer.toString());
			browser.waitForPageToLoad(functionalUtil.getWaitLimit());
		}
		
		return(true);
	}

	/**
	 * @param browser
	 * 
	 * Open portal in configuration mode.
	 */
	public void beginEditingPortal(Selenium browser){
		if(!functionalUtil.checkCurrentSite(browser, OlatSite.HOME)){
			functionalUtil.openSite(browser, OlatSite.HOME);
		}
		
		/* goto home site */
		Assert.assertTrue(openActionByMenuTree(browser, HomeSiteAction.PORTAL));
		Assert.assertTrue(checkCurrentAction(browser, HomeSiteAction.PORTAL));
		
		/* begin editing */
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("css=.")
		.append(PORTAL_EDIT_LINK_CSS)
		.append(" * a");
		
		browser.click(selectorBuffer.toString());
	}
	
	/**
	 * @param browser
	 * 
	 * Close portal configuration mode.
	 */
	public void endEditingPortal(Selenium browser){
		if(!functionalUtil.checkCurrentSite(browser, OlatSite.HOME)){
			functionalUtil.openSite(browser, OlatSite.HOME);
		}
		
		/* goto home site */
		Assert.assertTrue(openActionByMenuTree(browser, HomeSiteAction.PORTAL));
		Assert.assertTrue(checkCurrentAction(browser, HomeSiteAction.PORTAL));
		
		/* end editing */
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("css=.")
		.append(PORTAL_EDIT_LINK_CSS)
		.append(" * a");
		
		browser.click(selectorBuffer.toString());
	}

	/**
	 * @param browser
	 * @param portletCss
	 * @return true if portlet active otherwise false
	 * 
	 * Check the state of a portlet.
	 */
	public boolean checkPortletActive(Selenium browser, String portletCss){
		//TODO:JK: implement me
		
		return(false);
	}
	
	/**
	 * @param browser
	 * @param portletCss
	 * @param columnCount the count of columns to scan for
	 * @return the x and y position of the portlet, the result may be null if
	 * the portlet is inactive or doesn't exists.
	 * 
	 * Find the position of the portlet within the portal.
	 */
	public int[] findPortletPosition(Selenium browser, String portletCss, int columnCount){
		int position[] = new int[2];
		
		//TODO:JK: implement me
		
		return(null);
	}
	
	/**
	 * @param browser
	 * @param portletCss
	 * @return true if portlet was inactive and successfully activated
	 * otherwise false.
	 * 
	 * Add specified portlet by its CSS class.
	 */
	public boolean activatePortlet(Selenium browser, String portletCss){
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("css=.")
		.append(getPortletCss())
		.append('.')
		.append(getPortletInactiveCss())
		.append('.')
		.append(portletCss)
		.append(" .")
		.append(getPortletActivateCss())
		.append(" * a");
		
		if(browser.isElementPresent(selectorBuffer.toString())){
			browser.click(selectorBuffer.toString());
			
			return(true);
		}else{
			return(false);
		}
	}
	
	/**
	 * @param browser
	 * @param portletCss
	 * @return true if portlet was active and successfully deactivated
	 * otherwise false.
	 * 
	 * Remove a portlet from portal.
	 */
	public boolean deactivatePortlet(Selenium browser, String portletCss){
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("css=.")
		.append(getPortletCss())
		.append('.')
		.append(getPortletEditCss())
		.append('.')
		.append(portletCss)
		.append(" * .")
		.append(getPortletInactivateCss());
		
		if(browser.isElementPresent(selectorBuffer.toString())){
			browser.click(selectorBuffer.toString());
			
			return(true);
		}else{
			return(false);
		}
	}
	
	/**
	 * @param browser
	 * @param portletCss
	 * @param direction
	 * @return true if portlet was moved otherwise false
	 * 
	 * Move a portlet to the given direction.
	 */
	public boolean movePortlet(Selenium browser, String portletCss, Direction direction){
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("css=.")
		.append(getPortletCss())
		.append('.')
		.append(getPortletEditCss())
		.append('.')
		.append(portletCss)
		.append(" * .");
		
		switch(direction){
		case LEFT:
		{
			selectorBuffer.append(getPortletMoveLeftCss());
			
			break;
		}
		case RIGHT:
		{
			selectorBuffer.append(getPortletMoveRightCss());
			
			break;
		}
		case UP:
		{
			selectorBuffer.append(getPortletMoveUpCss());
			
			break;
		}
		case DOWN:
		{
			selectorBuffer.append(getPortletMoveDownCss());
			
			break;
		}
		}
		
		if(browser.isElementPresent(selectorBuffer.toString())){
			browser.click(selectorBuffer.toString());
			
			return(true);
		}else{
			return(false);
		}
			
	}
	
	
	/**
	 * @param browser
	 * @param language permitted values are: en, es, fr, de, it
	 * 
	 * Selects the specified language.
	 */
	public void selectLanguage(Selenium browser, String language){
		functionalUtil.openSite(browser, OlatSite.HOME);
		
		/* goto home site */
		Assert.assertTrue(openActionByMenuTree(browser, HomeSiteAction.SETTINGS));
		Assert.assertTrue(checkCurrentAction(browser, HomeSiteAction.SETTINGS));
		
		/* open System tab */
		Assert.assertTrue(functionalUtil.openContentTab(browser, SettingsTab.SYSTEM.ordinal()));
		
		/* select language */
		functionalUtil.selectOption(browser, LANGUAGE_OPTIONS_ID, language);
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//form//div[@class='");
		selectorBuffer.append(getPrefsButtonsCss());
		selectorBuffer.append("']//");
		selectorBuffer.append("button[@type='button']");
		
		browser.click(selectorBuffer.toString());
		browser.waitForPageToLoad(functionalUtil.getWaitLimit());
	}
	
	/**
	 * @param browser
	 * 
	 * Enables resume in olat but you must be logged in.
	 */
	public void enableResume(Selenium browser){
		functionalUtil.openSite(browser, OlatSite.HOME);
		
		/* goto home site */
		Assert.assertTrue(openActionByMenuTree(browser, HomeSiteAction.SETTINGS));
		Assert.assertTrue(checkCurrentAction(browser, HomeSiteAction.SETTINGS));
		
		/* open System tab */
		Assert.assertTrue(functionalUtil.openContentTab(browser, SettingsTab.SYSTEM.ordinal()));
		
		/* enable resume */
		Assert.assertTrue(functionalUtil.clickRadio(browser,
				FunctionalHomeSiteUtil.PortalSettingsForms.SpecificSystemSettingsRadios.RESUME_LAST_SESSION.getGroupCss(),
				FunctionalHomeSiteUtil.PortalSettingsForms.SpecificSystemSettingsRadios.ResumeLastSession.YES_AUTOMATICALLY.getValueAttribute()));
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//form//div[@class='");
		selectorBuffer.append(getGuiButtonsCss());
		selectorBuffer.append("']//");
		selectorBuffer.append("button[@type='button']");
		
		browser.click(selectorBuffer.toString());
		browser.waitForPageToLoad(functionalUtil.getWaitLimit());
	}

	/**
	 * @param browser
	 * 
	 * Enables resume on request in olat but you must be logged in.
	 */
	public void enableResumeOnRequest(Selenium browser){
		functionalUtil.openSite(browser, OlatSite.HOME);
		
		/* goto home site */
		Assert.assertTrue(openActionByMenuTree(browser, HomeSiteAction.SETTINGS));
		Assert.assertTrue(checkCurrentAction(browser, HomeSiteAction.SETTINGS));
		
		/* open system tab */
		Assert.assertTrue(functionalUtil.openContentTab(browser, SettingsTab.SYSTEM.ordinal()));
		
		/* enable resume */
		Assert.assertTrue(functionalUtil.clickRadio(browser,
				FunctionalHomeSiteUtil.PortalSettingsForms.SpecificSystemSettingsRadios.RESUME_LAST_SESSION.getGroupCss(),
				FunctionalHomeSiteUtil.PortalSettingsForms.SpecificSystemSettingsRadios.ResumeLastSession.YES_ON_REQUEST.getValueAttribute()));
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//form//div[@class='");
		selectorBuffer.append(getGuiButtonsCss());
		selectorBuffer.append("']//");
		selectorBuffer.append("button[@type='button']");
		
		browser.click(selectorBuffer.toString());
		browser.waitForPageToLoad(functionalUtil.getWaitLimit());
	}
	
	/**
	 * @param browser
	 * 
	 * Disable resume in olat but you must be logged in.
	 */
	public boolean disableResume(Selenium browser){
		functionalUtil.openSite(browser, OlatSite.HOME);
		
		/* goto home site */
		Assert.assertTrue(openActionByMenuTree(browser, HomeSiteAction.SETTINGS));
		Assert.assertTrue(checkCurrentAction(browser, HomeSiteAction.SETTINGS));
		
		/* open system tab */
		Assert.assertTrue(functionalUtil.openContentTab(browser, SettingsTab.SYSTEM.ordinal()));
		
		/* enable resume */
		Assert.assertTrue(functionalUtil.clickRadio(browser,
				FunctionalHomeSiteUtil.PortalSettingsForms.SpecificSystemSettingsRadios.RESUME_LAST_SESSION.getGroupCss(),
				FunctionalHomeSiteUtil.PortalSettingsForms.SpecificSystemSettingsRadios.ResumeLastSession.NO.getValueAttribute()));
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//form//div[@class='");
		selectorBuffer.append(getGuiButtonsCss());
		selectorBuffer.append("']//");
		selectorBuffer.append("button[@type='button']");
		
		browser.click(selectorBuffer.toString());
		browser.waitForPageToLoad(functionalUtil.getWaitLimit());
		
		return(true);
	}
	
	public boolean enableBack(Selenium browser){
		functionalUtil.openSite(browser, OlatSite.HOME);
		
		/* goto home site */
		Assert.assertTrue(openActionByMenuTree(browser, HomeSiteAction.SETTINGS));
		Assert.assertTrue(checkCurrentAction(browser, HomeSiteAction.SETTINGS));
		
		/* open system tab */
		Assert.assertTrue(functionalUtil.openContentTab(browser, SettingsTab.SYSTEM.ordinal()));
		
		/* enable resume */
		Assert.assertTrue(functionalUtil.clickRadio(browser,
				FunctionalHomeSiteUtil.PortalSettingsForms.SpecificSystemSettingsRadios.SUPPORT_FOR_BROWSER_BACK.getGroupCss(),
				FunctionalHomeSiteUtil.PortalSettingsForms.SpecificSystemSettingsRadios.SupportForBrowserBack.ON.getValueAttribute()));
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//form//div[@class='");
		selectorBuffer.append(getGuiButtonsCss());
		selectorBuffer.append("']//");
		selectorBuffer.append("button[@type='button']");
		
		browser.click(selectorBuffer.toString());
		browser.waitForPageToLoad(functionalUtil.getWaitLimit());
		
		return(true);
	}
	
	public boolean disableBack(Selenium browser){
		functionalUtil.openSite(browser, OlatSite.HOME);
		
		/* goto home site */
		Assert.assertTrue(openActionByMenuTree(browser, HomeSiteAction.SETTINGS));
		Assert.assertTrue(checkCurrentAction(browser, HomeSiteAction.SETTINGS));
		
		/* open system tab */
		Assert.assertTrue(functionalUtil.openContentTab(browser, SettingsTab.SYSTEM.ordinal()));
		
		/* enable resume */
		Assert.assertTrue(functionalUtil.clickRadio(browser,
				FunctionalHomeSiteUtil.PortalSettingsForms.SpecificSystemSettingsRadios.SUPPORT_FOR_BROWSER_BACK.getGroupCss(),
				FunctionalHomeSiteUtil.PortalSettingsForms.SpecificSystemSettingsRadios.SupportForBrowserBack.OFF.getValueAttribute()));
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//form//div[@class='");
		selectorBuffer.append(getGuiButtonsCss());
		selectorBuffer.append("']//");
		selectorBuffer.append("button[@type='button']");
		
		browser.click(selectorBuffer.toString());
		browser.waitForPageToLoad(functionalUtil.getWaitLimit());
		
		return(true);
	}
	
	/**
	 * @param browser
	 * @return true on success
	 * 
	 * Resets portal settings to default.
	 */
	public boolean resetSettings(Selenium browser){
		functionalUtil.openSite(browser, OlatSite.HOME);
		
		/* open settings page */
		if(!openActionByMenuTree(browser, HomeSiteAction.SETTINGS)){
			return(false);
		}
		
		/* click system tab */
		functionalUtil.openContentTab(browser, 1);
		
		/* using reset configurations form */
		/* click all checkboxes */
		functionalUtil.clickCheckbox(browser, CONFIGURATIONS_CSS, GUI_PREFERENCES_VALUE);
		
		functionalUtil.clickCheckbox(browser, CONFIGURATIONS_CSS, SYS_PREFERENCES_VALUE);
		
		functionalUtil.clickCheckbox(browser, CONFIGURATIONS_CSS, RESUME_VALUE);
		
		/* click Reset */
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//form//div[@class='");
		selectorBuffer.append(getSysprefsButtonsCss());
		selectorBuffer.append("']//");
		selectorBuffer.append("button[@type='button']");
		
		browser.click(selectorBuffer.toString());
		browser.waitForPageToLoad(functionalUtil.getWaitLimit());
		
		return(true);
	}
	
	public FunctionalUtil getUtil() {
		return functionalUtil;
	}

	public void setUtil(FunctionalUtil functionalUtil) {
		this.functionalUtil = functionalUtil;
	}

	public String getHomeActionNavigationSelectedCss() {
		return homeActionNavigationSelectedCss;
	}

	public void setHomeActionNavigationSelectedCss(
			String homeActionNavigationSelectedCss) {
		this.homeActionNavigationSelectedCss = homeActionNavigationSelectedCss;
	}

	public String getHomeActionPortalCss() {
		return homeActionPortalCss;
	}

	public void setHomeActionPortalCss(String homeActionPortalCss) {
		this.homeActionPortalCss = homeActionPortalCss;
	}

	public String getHomeActionSettingsCss() {
		return homeActionSettingsCss;
	}

	public void setHomeActionSettingsCss(String homeActionSettingsCss) {
		this.homeActionSettingsCss = homeActionSettingsCss;
	}

	public String getHomeActionCalendarCss() {
		return homeActionCalendarCss;
	}

	public void setHomeActionCalendarCss(String homeActionCalendarCss) {
		this.homeActionCalendarCss = homeActionCalendarCss;
	}

	public String getHomeActionSubscriptionsCss() {
		return homeActionSubscriptionsCss;
	}

	public void setHomeActionSubscriptionsCss(String homeActionSubscriptionsCss) {
		this.homeActionSubscriptionsCss = homeActionSubscriptionsCss;
	}

	public String getHomeActionBookmarksCss() {
		return homeActionBookmarksCss;
	}

	public void setHomeActionBookmarksCss(String homeActionBookmarksCss) {
		this.homeActionBookmarksCss = homeActionBookmarksCss;
	}

	public String getHomeActionPersonalFolderCss() {
		return homeActionPersonalFolderCss;
	}

	public void setHomeActionPersonalFolderCss(String homeActionPersonalFolderCss) {
		this.homeActionPersonalFolderCss = homeActionPersonalFolderCss;
	}

	public String getHomeActionNotesCss() {
		return homeActionNotesCss;
	}

	public void setHomeActionNotesCss(String homeActionNotesCss) {
		this.homeActionNotesCss = homeActionNotesCss;
	}

	public String getHomeActionEvidencesOfAchievementCss() {
		return homeActionEvidencesOfAchievementCss;
	}

	public void setHomeActionEvidencesOfAchievementCss(
			String homeActionEvidencesOfAchievementCss) {
		this.homeActionEvidencesOfAchievementCss = homeActionEvidencesOfAchievementCss;
	}

	public String getEPortfolioActionMyArtifactsCss() {
		return ePortfolioActionMyArtifactsCss;
	}

	public void setEPortfolioActionMyArtifactsCss(String ePortfolioActionMyArtifactsCss) {
		this.ePortfolioActionMyArtifactsCss = ePortfolioActionMyArtifactsCss;
	}

	public String getEPortfolioActionMyBindersCss() {
		return ePortfolioActionMyBindersCss;
	}

	public void setEPortfolioActionMyBindersCss(String ePortfolioActionMyBindersCss) {
		this.ePortfolioActionMyBindersCss = ePortfolioActionMyBindersCss;
	}

	public String getEPortfolioActionMyPortfolioTasksCss() {
		return ePortfolioActionMyPortfolioTasksCss;
	}

	public void setEPortfolioActionMyPortfolioTasksCss(
			String ePortfolioActionMyPortfolioTasksCss) {
		this.ePortfolioActionMyPortfolioTasksCss = ePortfolioActionMyPortfolioTasksCss;
	}

	public String getEPortfolioActionReleasedBindersCss() {
		return ePortfolioActionReleasedBindersCss;
	}

	public void setEPortfolioActionReleasedBindersCss(
			String ePortfolioActionReleasedBindersCss) {
		this.ePortfolioActionReleasedBindersCss = ePortfolioActionReleasedBindersCss;
	}

	public String getPortalEditLinkCss() {
		return portalEditLinkCss;
	}

	public void setPortalEditLinkCss(String portalEditLinkCss) {
		this.portalEditLinkCss = portalEditLinkCss;
	}

	public String getPortalWrapperCss() {
		return portalWrapperCss;
	}

	public void setPortalWrapperCss(String portalWrapperCss) {
		this.portalWrapperCss = portalWrapperCss;
	}

	public String getPortalColumnCssPrefix() {
		return portalColumnCssPrefix;
	}

	public void setPortalColumnCssPrefix(String portalColumnCssPrefix) {
		this.portalColumnCssPrefix = portalColumnCssPrefix;
	}

	public String getPortalSubcolumnsCss() {
		return portalSubcolumnsCss;
	}

	public void setPortalSubcolumnsCss(String portalSubcolumnsCss) {
		this.portalSubcolumnsCss = portalSubcolumnsCss;
	}

	public String getPortalInactiveCss() {
		return portalInactiveCss;
	}

	public void setPortalInactiveCss(String portalInactiveCss) {
		this.portalInactiveCss = portalInactiveCss;
	}

	public String getPortletCss() {
		return portletCss;
	}

	public void setPortletCss(String portletCss) {
		this.portletCss = portletCss;
	}

	public String getPortletEditCss() {
		return portletEditCss;
	}

	public void setPortletEditCss(String portletEditCss) {
		this.portletEditCss = portletEditCss;
	}

	public String getPortletInactiveCss() {
		return portletInactiveCss;
	}

	public void setPortletInactiveCss(String portletInactiveCss) {
		this.portletInactiveCss = portletInactiveCss;
	}

	public String getPortletQuickstartCss() {
		return portletQuickstartCss;
	}

	public void setPortletQuickstartCss(String portletQuickstartCss) {
		this.portletQuickstartCss = portletQuickstartCss;
	}

	public String getPortletRepositoryStudentCss() {
		return portletRepositoryStudentCss;
	}

	public void setPortletRepositoryStudentCss(String portletRepositoryStudentCss) {
		this.portletRepositoryStudentCss = portletRepositoryStudentCss;
	}

	public String getPortletRepositoryTeacherCss() {
		return portletRepositoryTeacherCss;
	}

	public void setPortletRepositoryTeacherCss(String portletRepositoryTeacherCss) {
		this.portletRepositoryTeacherCss = portletRepositoryTeacherCss;
	}

	public String getPortletInfomessagesCss() {
		return portletInfomessagesCss;
	}

	public void setPortletInfomessagesCss(String portletInfomessagesCss) {
		this.portletInfomessagesCss = portletInfomessagesCss;
	}

	public String getPortletCalendarCss() {
		return portletCalendarCss;
	}

	public void setPortletCalendarCss(String portletCalendarCss) {
		this.portletCalendarCss = portletCalendarCss;
	}

	public String getPortletBookmarkCss() {
		return portletBookmarkCss;
	}

	public void setPortletBookmarkCss(String portletBookmarkCss) {
		this.portletBookmarkCss = portletBookmarkCss;
	}

	public String getPortletGroupsCss() {
		return portletGroupsCss;
	}

	public void setPortletGroupsCss(String portletGroupsCss) {
		this.portletGroupsCss = portletGroupsCss;
	}

	public String getPortletNotiCss() {
		return portletNotiCss;
	}

	public void setPortletNotiCss(String portletNotiCss) {
		this.portletNotiCss = portletNotiCss;
	}

	public String getPortletEffCss() {
		return portletEffCss;
	}

	public void setPortletEffCss(String portletEffCss) {
		this.portletEffCss = portletEffCss;
	}

	public String getPortletNotesCss() {
		return portletNotesCss;
	}

	public void setPortletNotesCss(String portletNotesCss) {
		this.portletNotesCss = portletNotesCss;
	}

	public String getPortletDykCss() {
		return portletDykCss;
	}

	public void setPortletDykCss(String portletDykCss) {
		this.portletDykCss = portletDykCss;
	}

	public String getPortletActivateCss() {
		return portletActivateCss;
	}

	public void setPortletActivateCss(String portletActivateCss) {
		this.portletActivateCss = portletActivateCss;
	}

	public String getPortletInactivateCss() {
		return portletInactivateCss;
	}

	public void setPortletInactivateCss(String portletInactivateCss) {
		this.portletInactivateCss = portletInactivateCss;
	}

	public String getPortletMoveLeftCss() {
		return portletMoveLeftCss;
	}

	public void setPortletMoveLeftCss(String portletMoveLeftCss) {
		this.portletMoveLeftCss = portletMoveLeftCss;
	}

	public String getPortletMoveRightCss() {
		return portletMoveRightCss;
	}

	public void setPortletMoveRightCss(String portletMoveRightCss) {
		this.portletMoveRightCss = portletMoveRightCss;
	}

	public String getPortletMoveUpCss() {
		return portletMoveUpCss;
	}

	public void setPortletMoveUpCss(String portletMoveUpCss) {
		this.portletMoveUpCss = portletMoveUpCss;
	}

	public String getPortletMoveDownCss() {
		return portletMoveDownCss;
	}

	public void setPortletMoveDownCss(String portletMoveDownCss) {
		this.portletMoveDownCss = portletMoveDownCss;
	}

	public String getPrefsButtonsCss() {
		return prefsButtonsCss;
	}

	public void setPrefsButtonsCss(String prefsButtonsCss) {
		this.prefsButtonsCss = prefsButtonsCss;
	}

	public String getGuiButtonsCss() {
		return guiButtonsCss;
	}

	public void setGuiButtonsCss(String guiButtonsCss) {
		this.guiButtonsCss = guiButtonsCss;
	}

	public String getSysprefsButtonsCss() {
		return sysprefsButtonsCss;
	}

	public void setSysprefsButtonsCss(String sysprefsButtonsCss) {
		this.sysprefsButtonsCss = sysprefsButtonsCss;
	}

	public String getOldPasswordCss() {
		return oldPasswordCss;
	}

	public void setOldPasswordCss(String oldPasswordCss) {
		this.oldPasswordCss = oldPasswordCss;
	}

	public String getNewPasswordCss() {
		return newPasswordCss;
	}

	public void setNewPasswordCss(String newPasswordCss) {
		this.newPasswordCss = newPasswordCss;
	}

	public String getConfirmPasswordCss() {
		return confirmPasswordCss;
	}

	public void setConfirmPasswordCss(String confirmPasswordCss) {
		this.confirmPasswordCss = confirmPasswordCss;
	}
}
