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
	
	public final static String HOME_PAGE_NAVIGATION_SELECTED_CSS = "b_tree_selected";
	
	/* navigation */
	public final static String HOME_PAGE_PORTAL_CSS = "o_sel_portal";
	public final static String HOME_PAGE_SETTINGS_CSS = "o_sel_mysettings";
	public final static String HOME_PAGE_CALENDAR_CSS = "o_sel_calendar";
	public final static String HOME_PAGE_SUBSCRIPTIONS_CSS = "o_sel_notifications";
	public final static String HOME_PAGE_BOOKMARKS_CSS = "o_sel_bookmarks";
	public final static String HOME_PAGE_PERSONAL_FOLDER_CSS = "o_sel_userfolder";
	public final static String HOME_PAGE_NOTES_CSS = "o_sel_notelist";
	public final static String HOME_PAGE_EVIDENCES_OF_ACHIEVEMENT_CSS = "o_sel_effstatements";
	
	public final static String EPORTFOLIO_PAGE_MY_ARTIFACTS_CSS = "o_sel_EPArtefacts";
	public final static String EPORTFOLIO_PAGE_MY_BINDERS_CSS = "o_sel_EPMaps";
	public final static String EPORTFOLIO_PAGE_MY_PORTFOLIO_TASKS_CSS = "o_sel_EPStructuredMaps";
	public final static String EPORTFOLIO_PAGE_RELEASED_BINDERS_CSS = "o_sel_EPSharedMaps";
	
	/* portal */
	public final static String PORTAL_EDIT_LINK_CSS = "o_home_portaleditlink";
	
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
	public final static String LANGUAGE_OPTIONS_ID = "o_fioform_language_SELBOX";
	
	public final static String GERMAN_LANGUAGE_VALUE = "de";
	public final static String ENGLISH_LANGUAGE_VALUE = "en";
	public final static String FRENCH_LANGUAGE_VALUE = "fr";
	public final static String SPANISH_LANGUAGE_VALUE = "es";
	public final static String ITALIAN_LANGUAGE_VALUE = "it";
	
	/* Specific System Settings */
	public final static String ACCESSIBILITY_CSS = "o_sel_home_settings_accessibility";
	public final static String RESUME_LAST_SESSION_CSS = "o_sel_home_settings_resume";
	public final static String SUPPORT_FOR_BROWSER_BACK_CSS = "o_sel_home_settings_back_enabling";
	
	public final static String NO_VALUE = "none";
	public final static String YES_AUTOMATICALLY_VALUE = "auto";
	public final static String YES_ON_REQUEST_VALUE = "request";
	
	public final static String OFF_VALUE = "no";
	public final static String ON_VALUE = "yes";
	
	/* Reset Configurations */
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
	
	public enum HomePage {//TODO:JK: HomeSiteActions
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
	
	public enum EPortfolioPage {
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
	
	private String homePageNavigationSelectedCss;
	
	private String homePagePortalCss;
	private String homePageSettingsCss;
	private String homePageCalendarCss;
	private String homePageSubscriptionsCss;
	private String homePageBookmarksCss;
	private String homePagePersonalFolderCss;
	private String homePageNotesCss;
	private String homePageEvidencesOfAchievementCss;

	private String ePortfolioPageMyArtifactsCss;
	private String ePortfolioPageMyBindersCss;
	private String ePortfolioPageMyPortfolioTasksCss;
	private String ePortfolioPageReleasedBindersCss;
	
	private String portalEditLinkCss;
	
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
		
		setHomePageNavigationSelectedCss(HOME_PAGE_NAVIGATION_SELECTED_CSS);
		
		setHomePagePortalCss(HOME_PAGE_PORTAL_CSS);
		setHomePageSettingsCss(HOME_PAGE_SETTINGS_CSS);
		setHomePageCalendarCss(HOME_PAGE_CALENDAR_CSS);
		setHomePageSubscriptionsCss(HOME_PAGE_SUBSCRIPTIONS_CSS);
		setHomePageBookmarksCss(HOME_PAGE_BOOKMARKS_CSS);
		setHomePagePersonalFolderCss(HOME_PAGE_PERSONAL_FOLDER_CSS);
		setHomePageNotesCss(HOME_PAGE_NOTES_CSS);
		setHomePageEvidencesOfAchievementCss(HOME_PAGE_EVIDENCES_OF_ACHIEVEMENT_CSS);
		
		setEPortfolioPageMyArtifactsCss(EPORTFOLIO_PAGE_MY_ARTIFACTS_CSS);
		setEPortfolioPageMyBindersCss(EPORTFOLIO_PAGE_MY_BINDERS_CSS);
		setEPortfolioPageMyPortfolioTasksCss(EPORTFOLIO_PAGE_MY_PORTFOLIO_TASKS_CSS);
		setEPortfolioPageReleasedBindersCss(EPORTFOLIO_PAGE_RELEASED_BINDERS_CSS);
		
		setPortalEditLinkCss(PORTAL_EDIT_LINK_CSS);
		
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
		
		setPortletActivateCss(PORTLET_EDIT_ACTIVATE_CSS);
		setPortletInactivateCss(PORTLET_EDIT_INACTIVATE_CSS);
		setPortletMoveLeftCss(PORTLET_MOVE_LEFT_CSS);
		setPortletMoveRightCss(PORTLET_MOVE_RIGHT_CSS);
		setPortletMoveUpCss(PORTLET_MOVE_UPWARDS_CSS);
		setPortletMoveDownCss(PORTLET_MOVE_DOWNWARDS_CSS);
		
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
	public String findCssClassOfPage(Object page){
		if(page == null)
			return(null);
		
		String selectedCss = null;

		if(page instanceof HomePage){
			switch((HomePage) page){
			case PORTAL:
			{
				selectedCss = getHomePagePortalCss();
				break;
			}
			case SETTINGS:
			{
				selectedCss = getHomePageSettingsCss();
				break;
			}
			case CALENDAR:
			{
				selectedCss = getHomePageCalendarCss();
				break;
			}
			case SUBSCRIPTIONS:
			{
				selectedCss = getHomePageSubscriptionsCss();
				break;
			}
			case BOOKMARKS:
			{
				selectedCss = getHomePageBookmarksCss();
				break;
			}
			case PERSONAL_FOLDER:
			{
				selectedCss = getHomePagePersonalFolderCss();
				break;
			}
			case NOTES:
			{
				selectedCss = getHomePageNotesCss();
				break;
			}
			case EVIDENCES_OF_ACHIEVEMENT:
				selectedCss = getHomePageEvidencesOfAchievementCss();
				break;
			}
		}else if(page instanceof EPortfolioPage){
			switch((EPortfolioPage) page){
			case MY_ARTIFACTS:
			{
				selectedCss = getEPortfolioPageMyArtifactsCss();
				break;
			}
			case MY_BINDERS:
			{
				selectedCss = getEPortfolioPageMyBindersCss();
				break;
			}
			case MY_PORTFOLIO_TASKS:
			{
				selectedCss = getEPortfolioPageMyPortfolioTasksCss();
				break;
			}
			case RELEASED_BINDERS:
			{
				selectedCss = getEPortfolioPageReleasedBindersCss();
				break;
			}
			}
		}
		
		return(selectedCss);
	}
	
	/**
	 * @param browser
	 * @param page
	 * @return true if match otherwise false
	 * 
	 * Check if the correct page is open.
	 */
	public boolean checkCurrentPage(Selenium browser, Object page){
		String selectedCss = findCssClassOfPage(page);
		
		if(selectedCss == null)
			return(false);
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("css=ul li.")
		.append(selectedCss)
		.append('.')
		.append(getHomePageNavigationSelectedCss())
		.append(" * a");
		
		if(browser.isElementPresent(selectorBuffer.toString())){
			return(true);
		}else{
			return(false);
		}
	}
	
	/**
	 * @param browser
	 * @param page
	 * @return true on success otherwise false
	 * 
	 * Browse the home site's navigation.
	 */
	public boolean openPageByNavigation(Selenium browser, Object page){
		String selectedCss = findCssClassOfPage(page);
		
		if(selectedCss == null){
			return(false);
		}
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("css=ul .")
		.append(selectedCss)
		.append(" * a");
		
		browser.click(selectorBuffer.toString());
		browser.waitForPageToLoad(functionalUtil.getWaitLimit());
		
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
		Assert.assertTrue(openPageByNavigation(browser, HomePage.PORTAL));
		Assert.assertTrue(checkCurrentPage(browser, HomePage.PORTAL));
		
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
		Assert.assertTrue(openPageByNavigation(browser, HomePage.PORTAL));
		Assert.assertTrue(checkCurrentPage(browser, HomePage.PORTAL));
		
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
	 * 
	 * Add specified portlet by its CSS class.
	 */
	public void activatePortlet(Selenium browser, String portletCss){
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("css=.")
		.append(portletCss)
		.append(getPortletActivateCss())
		.append(" * a");
		
		browser.click(selectorBuffer.toString());
	}
	
	/**
	 * @param browser
	 * @param portletCss
	 * 
	 * Remove a portlet from portal.
	 */
	public void deactivatePortlet(Selenium browser, String portletCss){
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("css=.")
		.append(portletCss)
		.append(" * .")
		.append(getPortletInactivateCss());
		
		browser.click(selectorBuffer.toString());
	}
	
	/**
	 * @param browser
	 * @param portletCss
	 * @param direction
	 * 
	 * Move a portlet to the given direction
	 */
	public void movePortlet(Selenium browser, String portletCss, Direction direction){
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("css=.")
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
		
		browser.click(selectorBuffer.toString());
	}
	
	
	/**
	 * @param browser
	 * @param language permitted values are: en, es, fr, de, it
	 * 
	 * Selects the specified language.
	 */
	public void selectLanguage(Selenium browser, String language){
		if(!functionalUtil.checkCurrentSite(browser, OlatSite.HOME)){
			functionalUtil.openSite(browser, OlatSite.HOME);
		}
		
		/* goto home site */
		Assert.assertTrue(openPageByNavigation(browser, HomePage.SETTINGS));
		Assert.assertTrue(checkCurrentPage(browser, HomePage.SETTINGS));
		
		/* open System tab */
		Assert.assertTrue(functionalUtil.openContentTab(browser, SettingsTab.SYSTEM.ordinal()));
		
		/* select language */
		functionalUtil.selectOption(browser, LANGUAGE_OPTIONS_ID, language);
		
		//FIXME:JK: use CSS classes instead of ordinal
		Assert.assertTrue(functionalUtil.saveForm(browser,
				FunctionalHomeSiteUtil.PortalSettingsForms.GENERAL_SYSTEM_SETTINGS.ordinal()));
	}
	
	/**
	 * @param browser
	 * 
	 * Enables resume in olat but you must be logged in.
	 */
	public void enableResume(Selenium browser){
		if(!functionalUtil.checkCurrentSite(browser, OlatSite.HOME)){
			functionalUtil.openSite(browser, OlatSite.HOME);
		}
		
		/* goto home site */
		Assert.assertTrue(openPageByNavigation(browser, HomePage.SETTINGS));
		Assert.assertTrue(checkCurrentPage(browser, HomePage.SETTINGS));
		
		/* open System tab */
		Assert.assertTrue(functionalUtil.openContentTab(browser, SettingsTab.SYSTEM.ordinal()));
		
		/* enable resume */
		Assert.assertTrue(functionalUtil.clickRadio(browser,
				FunctionalHomeSiteUtil.PortalSettingsForms.SpecificSystemSettingsRadios.RESUME_LAST_SESSION.getGroupCss(),
				FunctionalHomeSiteUtil.PortalSettingsForms.SpecificSystemSettingsRadios.ResumeLastSession.YES_AUTOMATICALLY.getValueAttribute()));
		
		//FIXME:JK: use CSS classes instead of ordinal
		Assert.assertTrue(functionalUtil.saveForm(browser,
				FunctionalHomeSiteUtil.PortalSettingsForms.SPECIFIC_SYSTEM_SETTINGS.ordinal()));
	}

	/**
	 * @param browser
	 * 
	 * Enables resume on request in olat but you must be logged in.
	 */
	public void enableResumeOnRequest(Selenium browser){
		if(!functionalUtil.checkCurrentSite(browser, OlatSite.HOME)){
			functionalUtil.openSite(browser, OlatSite.HOME);
		}
		
		/* goto home site */
		Assert.assertTrue(openPageByNavigation(browser, HomePage.SETTINGS));
		Assert.assertTrue(checkCurrentPage(browser, HomePage.SETTINGS));
		
		/* open system tab */
		Assert.assertTrue(functionalUtil.openContentTab(browser, SettingsTab.SYSTEM.ordinal()));
		
		/* enable resume */
		Assert.assertTrue(functionalUtil.clickRadio(browser,
				FunctionalHomeSiteUtil.PortalSettingsForms.SpecificSystemSettingsRadios.RESUME_LAST_SESSION.getGroupCss(),
				FunctionalHomeSiteUtil.PortalSettingsForms.SpecificSystemSettingsRadios.ResumeLastSession.YES_ON_REQUEST.getValueAttribute()));
		
		//FIXME:JK: use CSS classes instead of ordinal
		Assert.assertTrue(functionalUtil.saveForm(browser,
				FunctionalHomeSiteUtil.PortalSettingsForms.SPECIFIC_SYSTEM_SETTINGS.ordinal()));
	}
	
	/**
	 * @param browser
	 * 
	 * Disable resume in olat but you must be logged in.
	 */
	public boolean disableResume(Selenium browser){
		if(!functionalUtil.checkCurrentSite(browser, OlatSite.HOME)){
			functionalUtil.openSite(browser, OlatSite.HOME);
		}
		
		/* goto home site */
		Assert.assertTrue(openPageByNavigation(browser, HomePage.SETTINGS));
		Assert.assertTrue(checkCurrentPage(browser, HomePage.SETTINGS));
		
		/* open system tab */
		Assert.assertTrue(functionalUtil.openContentTab(browser, SettingsTab.SYSTEM.ordinal()));
		
		/* enable resume */
		Assert.assertTrue(functionalUtil.clickRadio(browser,
				FunctionalHomeSiteUtil.PortalSettingsForms.SpecificSystemSettingsRadios.RESUME_LAST_SESSION.getGroupCss(),
				FunctionalHomeSiteUtil.PortalSettingsForms.SpecificSystemSettingsRadios.ResumeLastSession.NO.getValueAttribute()));
		
		//FIXME:JK: use CSS classes instead of ordinal
		Assert.assertTrue(functionalUtil.saveForm(browser,
				FunctionalHomeSiteUtil.PortalSettingsForms.SPECIFIC_SYSTEM_SETTINGS.ordinal()));
		
		return(true);
	}
	
	public boolean enableBack(Selenium browser){
		if(!functionalUtil.checkCurrentSite(browser, OlatSite.HOME)){
			functionalUtil.openSite(browser, OlatSite.HOME);
		}
		
		/* goto home site */
		Assert.assertTrue(openPageByNavigation(browser, HomePage.SETTINGS));
		Assert.assertTrue(checkCurrentPage(browser, HomePage.SETTINGS));
		
		/* open system tab */
		Assert.assertTrue(functionalUtil.openContentTab(browser, SettingsTab.SYSTEM.ordinal()));
		
		/* enable resume */
		Assert.assertTrue(functionalUtil.clickRadio(browser,
				FunctionalHomeSiteUtil.PortalSettingsForms.SpecificSystemSettingsRadios.SUPPORT_FOR_BROWSER_BACK.getGroupCss(),
				FunctionalHomeSiteUtil.PortalSettingsForms.SpecificSystemSettingsRadios.SupportForBrowserBack.ON.getValueAttribute()));
		
		//FIXME:JK: use CSS classes instead of ordinal
		Assert.assertTrue(functionalUtil.saveForm(browser,
				FunctionalHomeSiteUtil.PortalSettingsForms.SPECIFIC_SYSTEM_SETTINGS.ordinal()));
		
		return(true);
	}
	
	public boolean disableBack(Selenium browser){
		if(!functionalUtil.checkCurrentSite(browser, OlatSite.HOME)){
			functionalUtil.openSite(browser, OlatSite.HOME);
		}
		
		/* goto home site */
		Assert.assertTrue(openPageByNavigation(browser, HomePage.SETTINGS));
		Assert.assertTrue(checkCurrentPage(browser, HomePage.SETTINGS));
		
		/* open system tab */
		Assert.assertTrue(functionalUtil.openContentTab(browser, SettingsTab.SYSTEM.ordinal()));
		
		/* enable resume */
		Assert.assertTrue(functionalUtil.clickRadio(browser,
				FunctionalHomeSiteUtil.PortalSettingsForms.SpecificSystemSettingsRadios.SUPPORT_FOR_BROWSER_BACK.getGroupCss(),
				FunctionalHomeSiteUtil.PortalSettingsForms.SpecificSystemSettingsRadios.SupportForBrowserBack.OFF.getValueAttribute()));
		
		//FIXME:JK: use CSS classes instead of ordinal
		Assert.assertTrue(functionalUtil.saveForm(browser,
				FunctionalHomeSiteUtil.PortalSettingsForms.SPECIFIC_SYSTEM_SETTINGS.ordinal()));
		
		return(true);
	}
	
	/**
	 * @param browser
	 * @return true on success
	 * 
	 * Resets portal settings to default.
	 */
	public boolean resetSettings(Selenium browser){
		/* open settings page */
		if(!openPageByNavigation(browser, HomePage.SETTINGS)){
			return(false);
		}
		
		/* click system tab */
		functionalUtil.openContentTab(browser, 1);
		
		/* using reset configurations form */
		/* click all checkboxes */
		functionalUtil.clickCheckbox(browser, CONFIGURATIONS_CSS, GUI_PREFERENCES_VALUE);
		//browser.waitForPageToLoad(functionalUtil.getWaitLimit());
		
		functionalUtil.clickCheckbox(browser, CONFIGURATIONS_CSS, SYS_PREFERENCES_VALUE);
		//browser.waitForPageToLoad(functionalUtil.getWaitLimit());
		
		functionalUtil.clickCheckbox(browser, CONFIGURATIONS_CSS, RESUME_VALUE);
		//browser.waitForPageToLoad(functionalUtil.getWaitLimit());
		
		/* click Reset */
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//form//button[@type='button']"); //  and @value='Reset'
		
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

	public String getHomePageNavigationSelectedCss() {
		return homePageNavigationSelectedCss;
	}

	public void setHomePageNavigationSelectedCss(
			String homePageNavigationSelectedCss) {
		this.homePageNavigationSelectedCss = homePageNavigationSelectedCss;
	}

	public String getHomePagePortalCss() {
		return homePagePortalCss;
	}

	public void setHomePagePortalCss(String homePagePortalCss) {
		this.homePagePortalCss = homePagePortalCss;
	}

	public String getHomePageSettingsCss() {
		return homePageSettingsCss;
	}

	public void setHomePageSettingsCss(String homePageSettingsCss) {
		this.homePageSettingsCss = homePageSettingsCss;
	}

	public String getHomePageCalendarCss() {
		return homePageCalendarCss;
	}

	public void setHomePageCalendarCss(String homePageCalendarCss) {
		this.homePageCalendarCss = homePageCalendarCss;
	}

	public String getHomePageSubscriptionsCss() {
		return homePageSubscriptionsCss;
	}

	public void setHomePageSubscriptionsCss(String homePageSubscriptionsCss) {
		this.homePageSubscriptionsCss = homePageSubscriptionsCss;
	}

	public String getHomePageBookmarksCss() {
		return homePageBookmarksCss;
	}

	public void setHomePageBookmarksCss(String homePageBookmarksCss) {
		this.homePageBookmarksCss = homePageBookmarksCss;
	}

	public String getHomePagePersonalFolderCss() {
		return homePagePersonalFolderCss;
	}

	public void setHomePagePersonalFolderCss(String homePagePersonalFolderCss) {
		this.homePagePersonalFolderCss = homePagePersonalFolderCss;
	}

	public String getHomePageNotesCss() {
		return homePageNotesCss;
	}

	public void setHomePageNotesCss(String homePageNotesCss) {
		this.homePageNotesCss = homePageNotesCss;
	}

	public String getHomePageEvidencesOfAchievementCss() {
		return homePageEvidencesOfAchievementCss;
	}

	public void setHomePageEvidencesOfAchievementCss(
			String homePageEvidencesOfAchievementCss) {
		this.homePageEvidencesOfAchievementCss = homePageEvidencesOfAchievementCss;
	}

	public String getEPortfolioPageMyArtifactsCss() {
		return ePortfolioPageMyArtifactsCss;
	}

	public void setEPortfolioPageMyArtifactsCss(String ePortfolioPageMyArtifactsCss) {
		this.ePortfolioPageMyArtifactsCss = ePortfolioPageMyArtifactsCss;
	}

	public String getEPortfolioPageMyBindersCss() {
		return ePortfolioPageMyBindersCss;
	}

	public void setEPortfolioPageMyBindersCss(String ePortfolioPageMyBindersCss) {
		this.ePortfolioPageMyBindersCss = ePortfolioPageMyBindersCss;
	}

	public String getEPortfolioPageMyPortfolioTasksCss() {
		return ePortfolioPageMyPortfolioTasksCss;
	}

	public void setEPortfolioPageMyPortfolioTasksCss(
			String ePortfolioPageMyPortfolioTasksCss) {
		this.ePortfolioPageMyPortfolioTasksCss = ePortfolioPageMyPortfolioTasksCss;
	}

	public String getEPortfolioPageReleasedBindersCss() {
		return ePortfolioPageReleasedBindersCss;
	}

	public void setEPortfolioPageReleasedBindersCss(
			String ePortfolioPageReleasedBindersCss) {
		this.ePortfolioPageReleasedBindersCss = ePortfolioPageReleasedBindersCss;
	}

	public String getPortalEditLinkCss() {
		return portalEditLinkCss;
	}

	public void setPortalEditLinkCss(String portalEditLinkCss) {
		this.portalEditLinkCss = portalEditLinkCss;
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
