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

import java.io.IOException;
import java.io.StringWriter;
import java.util.Calendar;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.junit.Assert;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.util.FunctionalUtil.OlatSite;

import com.thoughtworks.selenium.Selenium;

/**
 * 
 * @author jkraehemann, joel.kraehemann@frentix.com, frentix.com
 */
public class FunctionalHomeSiteUtil {
	private final static OLog log = Tracing.createLoggerFor(FunctionalHomeSiteUtil.class);
	
	public final static String HOME_SITE_MENU_TREE_SELECTED_CSS = "b_tree_selected";

	public final static String FORM_ELEMENT_WRAPPER = "b_form_element_wrapper";

	/* menu tree */
	public final static String HOME_ACTION_PORTAL_CSS = "o_sel_portal";
	public final static String HOME_ACTION_SETTINGS_CSS = "o_sel_mysettings";
	public final static String HOME_ACTION_CALENDAR_CSS = "o_sel_calendar";
	public final static String HOME_ACTION_SUBSCRIPTIONS_CSS = "o_sel_notifications";
	public final static String HOME_ACTION_BOOKMARKS_CSS = "o_sel_bookmarks";
	public final static String HOME_ACTION_PERSONAL_FOLDER_CSS = "o_sel_userfolder";
	public final static String HOME_ACTION_NOTES_CSS = "o_sel_notelist";
	public final static String HOME_ACTION_EVIDENCES_OF_ACHIEVEMENT_CSS = "o_sel_effstatements";

	public final static String EPORTFOLIO_PARENT_ID = "ddportfolioParent";
	public final static String EPORTFOLIO_ACTION_MY_ARTIFACTS_CSS = "o_sel_EPArtefacts";
	public final static String EPORTFOLIO_ACTION_MY_BINDERS_CSS = "o_sel_EPMaps";
	public final static String EPORTFOLIO_ACTION_MY_PORTFOLIO_TASKS_CSS = "o_sel_EPStructuredMaps";
	public final static String EPORTFOLIO_ACTION_RELEASED_BINDERS_CSS = "o_sel_EPSharedMaps";

	/* portal */
	public final static String PORTAL_EDIT_LINK_CSS = "o_home_portaleditlink";

	public final static String PORTAL_WRAPPER_CSS = "b_portal_wrapper";
	public final static String PORTAL_CSS = "b_portal";
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
	public final static String PORTLET_DYK_CSS = "b_portlet_dyk";

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
	public final static String YES_ON_REQUEST_VALUE = "ondemand";

	public final static String OFF_VALUE = "no";
	public final static String ON_VALUE = "yes";

	/* Reset Configurations */
	public final static String SYSPREFS_BUTTONS_CSS = "o_sel_home_settings_reset_sysprefs_buttons";

	public final static String CONFIGURATIONS_CSS = "o_sel_home_settings_reset_sysprefs";

	public final static String GUI_PREFERENCES_VALUE = "guiprefs";
	public final static String SYS_PREFERENCES_VALUE = "sysprefs";
	public final static String RESUME_VALUE = "resume";

	/* password tab */
	public final static String OLD_PASSWORD_CSS = "o_sel_home_pwd_old";
	public final static String NEW_PASSWORD_CSS = "o_sel_home_pwd_new_1";
	public final static String CONFIRM_PASSWORD_CSS = "o_sel_home_pwd_new_2";

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

	private String ePortfolioParentId;
	private String ePortfolioActionMyArtifactsCss;
	private String ePortfolioActionMyBindersCss;
	private String ePortfolioActionMyPortfolioTasksCss;
	private String ePortfolioActionReleasedBindersCss;

	private String portalEditLinkCss;
	private String portalWrapperCss;
	private String portalCss;
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

		setEPortfolioParentId(EPORTFOLIO_PARENT_ID);
		setEPortfolioActionMyArtifactsCss(EPORTFOLIO_ACTION_MY_ARTIFACTS_CSS);
		setEPortfolioActionMyBindersCss(EPORTFOLIO_ACTION_MY_BINDERS_CSS);
		setEPortfolioActionMyPortfolioTasksCss(EPORTFOLIO_ACTION_MY_PORTFOLIO_TASKS_CSS);
		setEPortfolioActionReleasedBindersCss(EPORTFOLIO_ACTION_RELEASED_BINDERS_CSS);

		setPortalEditLinkCss(PORTAL_EDIT_LINK_CSS);
		setPortalWrapperCss(PORTAL_WRAPPER_CSS);
		setPortalCss(PORTAL_CSS);
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

		setPrefsButtonsCss(PREFS_BUTTONS_CSS);
		setGuiButtonsCss(GUI_BUTTONS_CSS);
		setSysprefsButtonsCss(SYSPREFS_BUTTONS_CSS);

		setOldPasswordCss(OLD_PASSWORD_CSS);
		setNewPasswordCss(NEW_PASSWORD_CSS);
		setConfirmPasswordCss(CONFIRM_PASSWORD_CSS);
	}

	/**
	 * Find the corresponding CSS class for page.
	 * 
	 * @param page
	 * @return the matching CSS class
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
	 * Check if the correct page is open.
	 * 
	 * @param browser
	 * @param action
	 * @return true if match otherwise false
	 */
	public boolean checkCurrentAction(Selenium browser, Object action){
		return(checkCurrentAction(browser, action, -1));
	}

	/**
	 * 
	 * @param browser
	 * @param action
	 * @param timeout
	 * @return
	 */
	public boolean checkCurrentAction(Selenium browser, Object action, long timeout){
		String selectedCss = findCssClassOfAction(action);

		if(selectedCss == null)
			return(false);

		functionalUtil.idle(browser);
		
		StringBuffer selectorBuffer = new StringBuffer();

		selectorBuffer.append("css=ul li.")
		.append(selectedCss)
		.append('.')
		.append(getHomeActionNavigationSelectedCss())
		.append(" a");

		long timeElapsed = 0;
		long startTime = Calendar.getInstance().getTimeInMillis();

		do{
			if(browser.isElementPresent(selectorBuffer.toString())){
				return(true);
			}

			if(timeout != -1){
				try {
					Thread.sleep(FunctionalUtil.POLL_INTERVAL);
				} catch (InterruptedException e) {
					//TODO:JK: Auto-generated catch block
					e.printStackTrace();
				}
			}

			timeElapsed = Calendar.getInstance().getTimeInMillis() - startTime;
		}while(timeElapsed <= timeout && timeout != -1);

		return(false);
	}

	/**
	 * Browse the home site's menu tree.
	 * 
	 * @param browser
	 * @param action
	 * @return true on success otherwise false
	 */
	public boolean openActionByMenuTree(Selenium browser, Object action){
		return(openActionByMenuTree(browser, action, true));
	}
	
	/**
	 * Browse the home site's menu tree.
	 * 
	 * @param browser
	 * @param action
	 * @param checkCurrentAction
	 * @return true on success otherwise false
	 */
	public boolean openActionByMenuTree(Selenium browser, Object action, boolean checkCurrentAction){
		String selectedCss = findCssClassOfAction(action);

		if(selectedCss == null){
			return(false);
		}

		if(!checkCurrentAction || !checkCurrentAction(browser, action, Long.parseLong(functionalUtil.getWaitLimit()))){
			StringBuffer selectorBuffer = new StringBuffer();

			selectorBuffer.append("xpath=//ul//li[contains(@class, '")
			.append(selectedCss)
			.append("')]//a");
			
			if(action instanceof EPortfolioAction && !browser.isElementPresent(selectorBuffer.toString())){
				StringBuffer actionSelectorBuffer = new StringBuffer();

				actionSelectorBuffer.append("xpath=//ul//div[@id='")
				.append(getEPortfolioParentId())
				.append("']//a");

				browser.click(actionSelectorBuffer.toString());
				
				if(browser.isConfirmationPresent()){
					browser.getConfirmation();
				}
				
				functionalUtil.waitForPageToLoad(browser);
			}

			browser.click(selectorBuffer.toString());
			
			if(browser.isConfirmationPresent()){
				browser.getConfirmation();
			}
			
			functionalUtil.waitForPageToLoad(browser);
			functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		}

		return(true);
	}

	/**
	 * Open portal in configuration mode.
	 * 
	 * @param browser
	 */
	public void beginEditingPortal(Selenium browser){
		if(!functionalUtil.checkCurrentSite(browser, OlatSite.HOME)){
			functionalUtil.openSite(browser, OlatSite.HOME);
		}

		/* goto home site */
		//TODO:JK: ugly
		Assert.assertTrue(openActionByMenuTree(browser, HomeSiteAction.PORTAL));
		Assert.assertTrue(checkCurrentAction(browser, HomeSiteAction.PORTAL));

		functionalUtil.idle(browser);
		
		/* begin editing */
		StringBuffer selectorBuffer = new StringBuffer();

		selectorBuffer.append("css=.")
		.append(PORTAL_EDIT_LINK_CSS)
		.append(" a");

		browser.click(selectorBuffer.toString());
	}

	/**
	 * Close portal configuration mode.
	 * 
	 * @param browser
	 */
	public void endEditingPortal(Selenium browser){
		if(!functionalUtil.checkCurrentSite(browser, OlatSite.HOME)){
			functionalUtil.openSite(browser, OlatSite.HOME);
		}

		/* goto home site */
		//TODO:JK: ugly
		Assert.assertTrue(openActionByMenuTree(browser, HomeSiteAction.PORTAL));
		Assert.assertTrue(checkCurrentAction(browser, HomeSiteAction.PORTAL));

		/* end editing */
		functionalUtil.idle(browser);
		
		StringBuffer selectorBuffer = new StringBuffer();

		selectorBuffer.append("css=.")
		.append(PORTAL_EDIT_LINK_CSS)
		.append(" a");

		browser.click(selectorBuffer.toString());
	}

	/**
	 * Check the state of a portlet.
	 * 
	 * @param browser
	 * @param portletCss
	 * @return true if portlet active otherwise false
	 */
	public boolean checkPortletActive(Selenium browser, String portletCss){
		functionalUtil.idle(browser);
		
		StringBuffer selectorBuffer = new StringBuffer();

		selectorBuffer.append("xpath=//div[contains(@class, '")
		.append(getPortalCss())
		.append("') and contains(@class, '")
		.append(getPortalSubcolumnsCss())
		.append("')]//div[contains(@class, '")
		.append(getPortletCss())
		.append("') and contains(@class, '")
		.append(portletCss)
		.append("')]");

		if(browser.isElementPresent(selectorBuffer.toString())){
			return(true);
		}else{
			/* selector of editing portlets */
			selectorBuffer = new StringBuffer();

			selectorBuffer.append("xpath=//div[contains(@class, '")
			.append(getPortalCss())
			.append("') and contains(@class, '")
			.append(getPortalSubcolumnsCss())
			.append("')]//div[contains(@class, '")
			.append(getPortletCss())
			.append("') contains(@class, '")
			.append(getPortletEditCss())
			.append("') and contains(@class, '")
			.append(portletCss)
			.append("')]");

			if(browser.isElementPresent(selectorBuffer.toString())){
				return(true);
			}else{
				return(false);
			}
		}
	}

	/**
	 * Find the position of the portlet within the portal.
	 * 
	 * @param browser
	 * @param portletCss
	 * @param columnCount the count of columns to scan for
	 * @return the x and y position of the portlet, the result may be null if
	 * the portlet is inactive or doesn't exists.
	 */
	public int[] findPortletPosition(Selenium browser, String portletCss, int columnCount){
		functionalUtil.idle(browser);
		
		for(int i = 0; i < columnCount; i++){
			StringBuffer selectorBuffer = new StringBuffer();

			selectorBuffer.append("css=.")
			.append(getPortalCss())
			.append('.')
			.append(getPortalSubcolumnsCss())
			.append(" .")
			.append(getPortalColumnCssPrefix())
			.append(i + 1)
			.append(" .")
			.append(getPortletCss())
			.append('.')
			.append(getPortletEditCss())
			.append('.')
			.append(portletCss);

			if(browser.isElementPresent(selectorBuffer.toString())){
				int position[] = new int[2];

				position[0] = i;

				selectorBuffer = new StringBuffer();

				selectorBuffer.append("css=.")
				.append(getPortalCss())
				.append(" .")
				.append(getPortalColumnCssPrefix())
				.append(i + 1)
				.append(" .")
				.append(getPortletCss());

				VelocityContext context = new VelocityContext();

				context.put("portalCss", getPortalCss());
				context.put("portalSubcolumnsCss", getPortalSubcolumnsCss());
				context.put("portletCss", getPortletCss());
				context.put("portalColumnCssPrefix", getPortalColumnCssPrefix());

				context.put("portlet", portletCss);

				context.put("column", i);
				context.put("j_stop", browser.getCssCount(selectorBuffer.toString()).intValue());

				VelocityEngine engine = null;

				engine = new VelocityEngine();

				StringWriter sw = new StringWriter();

				try {
					engine.evaluate(context, sw, "portletPosition", FunctionalHomeSiteUtil.class.getResourceAsStream("PortletPosition.vm"));

					Integer j = new Integer(browser.getEval(sw.toString()));
					position[1] = j.intValue();

					return(position);
				} catch (ParseErrorException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MethodInvocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ResourceNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


				return(null);
			}
		}

		return(null);
	}

	/**
	 * Add specified portlet by its CSS class.
	 * 
	 * @param browser
	 * @param portletCss
	 * @return true if portlet was inactive and successfully activated
	 * otherwise false.
	 */
	public boolean activatePortlet(Selenium browser, String portletCss){
		functionalUtil.idle(browser);
		
		StringBuffer selectorBuffer = new StringBuffer();

		selectorBuffer.append("css=.")
		.append(getPortletCss())
		.append('.')
		.append(portletCss);
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		
		/*  */
		StringBuffer activateBuffer = new StringBuffer(selectorBuffer);
		
		activateBuffer.append('.')
		.append(getPortletInactiveCss())
		.append(" .")
		.append(getPortletActivateCss())
		.append(" a");

		if(browser.isElementPresent(activateBuffer.toString())){
			browser.click(activateBuffer.toString());

			return(true);
		}else{
			return(false);
		}
	}

	/**
	 * Remove a portlet from portal.
	 * 
	 * @param browser
	 * @param portletCss
	 * @return true if portlet was active and successfully deactivated
	 * otherwise false.
	 */
	public boolean deactivatePortlet(Selenium browser, String portletCss){
		functionalUtil.idle(browser);
		
		StringBuffer selectorBuffer = new StringBuffer();

		selectorBuffer.append("css=.")
		.append(getPortletCss())
		.append('.')
		.append(portletCss);
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		
		/*  */
		StringBuffer inactivateBuffer = new StringBuffer(selectorBuffer);
		
		inactivateBuffer.append('.')
		.append(getPortletEditCss())
		.append(" .")
		.append(getPortletInactivateCss())
		.append(" a");

		if(browser.isElementPresent(inactivateBuffer.toString())){
			browser.click(inactivateBuffer.toString());

			return(true);
		}else{
			return(false);
		}
	}

	/**
	 * Move a portlet to the given direction.
	 * 
	 * @param browser
	 * @param portletCss
	 * @param direction
	 * @return true if portlet was moved otherwise false
	 */
	public boolean movePortlet(Selenium browser, String portletCss, Direction direction){
		functionalUtil.idle(browser);
		
		/* wait till portlet gets loaded */
		StringBuffer selectorBuffer = new StringBuffer();

		selectorBuffer.append("css=.")
		.append(getPortletCss())
		.append('.')
		.append(portletCss);
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		
		/*  */
		selectorBuffer = new StringBuffer();

		selectorBuffer.append("css=.")
		.append(getPortletCss())
		.append('.')
		.append(getPortletEditCss())
		.append('.')
		.append(portletCss)
		.append(" .");

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
	 * Selects the specified language.
	 * 
	 * @param browser
	 * @param language permitted values are: en, es, fr, de, it
	 */
	public void selectLanguage(Selenium browser, String language){
		functionalUtil.openSite(browser, OlatSite.HOME);

		/* goto home site */
		//TODO:JK: ugly
		Assert.assertTrue(openActionByMenuTree(browser, HomeSiteAction.SETTINGS));

		/* open System tab */
		//TODO:JK: ugly
		Assert.assertTrue(functionalUtil.openContentTab(browser, SettingsTab.SYSTEM.ordinal()));

		/* select language */
		functionalUtil.selectOption(browser, LANGUAGE_OPTIONS_ID, language);

		functionalUtil.idle(browser);
		
		StringBuffer selectorBuffer = new StringBuffer();

		selectorBuffer.append("xpath=//form//div[contains(@class, '");
		selectorBuffer.append(getPrefsButtonsCss());
		selectorBuffer.append("')]//");
		selectorBuffer.append("button[@type='button']");

		browser.click(selectorBuffer.toString());
		functionalUtil.waitForPageToLoad(browser);
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
	}

	/**
	 * Enables resume in olat but you must be logged in.
	 * 
	 * @param browser
	 */
	public void enableResume(Selenium browser){
		functionalUtil.openSite(browser, OlatSite.HOME);

		/* goto home site */
		//TODO:JK: ugly
		Assert.assertTrue(openActionByMenuTree(browser, HomeSiteAction.SETTINGS));
		Assert.assertTrue(checkCurrentAction(browser, HomeSiteAction.SETTINGS));

		/* open System tab */
		//TODO:JK: ugly
		Assert.assertTrue(functionalUtil.openContentTab(browser, SettingsTab.SYSTEM.ordinal()));

		/* enable resume */
		//TODO:JK: ugly
		Assert.assertTrue(functionalUtil.clickRadio(browser,
				FunctionalHomeSiteUtil.PortalSettingsForms.SpecificSystemSettingsRadios.RESUME_LAST_SESSION.getGroupCss(),
				FunctionalHomeSiteUtil.PortalSettingsForms.SpecificSystemSettingsRadios.ResumeLastSession.YES_AUTOMATICALLY.getValueAttribute()));

		functionalUtil.idle(browser);
		
		StringBuffer selectorBuffer = new StringBuffer();

		selectorBuffer.append("xpath=//form//div[contains(@class, '");
		selectorBuffer.append(getGuiButtonsCss());
		selectorBuffer.append("')]//");
		selectorBuffer.append("button[@type='button']");

		browser.click(selectorBuffer.toString());
		functionalUtil.waitForPageToLoad(browser);
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
	}

	/**
	 * Enables resume on request in olat but you must be logged in.
	 * 
	 * @param browser
	 */
	public void enableResumeOnRequest(Selenium browser){
		functionalUtil.openSite(browser, OlatSite.HOME);

		/* goto home site */
		//TODO:JK: ugly
		Assert.assertTrue(openActionByMenuTree(browser, HomeSiteAction.SETTINGS));
		Assert.assertTrue(checkCurrentAction(browser, HomeSiteAction.SETTINGS));

		/* open system tab */
		//TODO:JK: ugly
		Assert.assertTrue(functionalUtil.openContentTab(browser, SettingsTab.SYSTEM.ordinal()));

		/* enable resume */
		//TODO:JK: ugly
		Assert.assertTrue(functionalUtil.clickRadio(browser,
				FunctionalHomeSiteUtil.PortalSettingsForms.SpecificSystemSettingsRadios.RESUME_LAST_SESSION.getGroupCss(),
				FunctionalHomeSiteUtil.PortalSettingsForms.SpecificSystemSettingsRadios.ResumeLastSession.YES_ON_REQUEST.getValueAttribute()));

		functionalUtil.idle(browser);
		
		StringBuffer selectorBuffer = new StringBuffer();

		selectorBuffer.append("xpath=//form//div[contains(@class, '");
		selectorBuffer.append(getGuiButtonsCss());
		selectorBuffer.append("')]//");
		selectorBuffer.append("button[@type='button']");

		browser.click(selectorBuffer.toString());
		functionalUtil.waitForPageToLoad(browser);
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
	}

	/**
	 * Disable resume in olat but you must be logged in.
	 * 
	 * @param browser
	 */
	public boolean disableResume(Selenium browser){
		functionalUtil.openSite(browser, OlatSite.HOME);

		/* goto home site */
		//TODO:JK: ugly
		Assert.assertTrue(openActionByMenuTree(browser, HomeSiteAction.SETTINGS));
		//Assert.assertTrue(checkCurrentAction(browser, HomeSiteAction.SETTINGS));

		/* open system tab */
		//TODO:JK: ugly
		Assert.assertTrue(functionalUtil.openContentTab(browser, SettingsTab.SYSTEM.ordinal()));
		
		/* disable resume */
		//TODO:JK: ugly
		Assert.assertTrue(functionalUtil.clickRadio(browser,
				FunctionalHomeSiteUtil.PortalSettingsForms.SpecificSystemSettingsRadios.RESUME_LAST_SESSION.getGroupCss(),
				FunctionalHomeSiteUtil.PortalSettingsForms.SpecificSystemSettingsRadios.ResumeLastSession.NO.getValueAttribute()));

		functionalUtil.idle(browser);
		
		StringBuffer selectorBuffer = new StringBuffer();

		selectorBuffer.append("xpath=//form//div[contains(@class, '");
		selectorBuffer.append(getGuiButtonsCss());
		selectorBuffer.append("')]//");
		selectorBuffer.append("button[@type='button']");

		browser.click(selectorBuffer.toString());
		functionalUtil.waitForPageToLoad(browser);
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());

		return(true);
	}

	/**
	 * 
	 * @param browser
	 * @return
	 */
	public boolean enableBack(Selenium browser){
		functionalUtil.openSite(browser, OlatSite.HOME);

		/* goto home site */
		//TODO:JK: ugly
		Assert.assertTrue(openActionByMenuTree(browser, HomeSiteAction.SETTINGS));
		Assert.assertTrue(checkCurrentAction(browser, HomeSiteAction.SETTINGS));

		/* open system tab */
		//TODO:JK: ugly
		Assert.assertTrue(functionalUtil.openContentTab(browser, SettingsTab.SYSTEM.ordinal()));

		/* enable resume */
		//TODO:JK: ugly
		Assert.assertTrue(functionalUtil.clickRadio(browser,
				FunctionalHomeSiteUtil.PortalSettingsForms.SpecificSystemSettingsRadios.SUPPORT_FOR_BROWSER_BACK.getGroupCss(),
				FunctionalHomeSiteUtil.PortalSettingsForms.SpecificSystemSettingsRadios.SupportForBrowserBack.ON.getValueAttribute()));

		functionalUtil.idle(browser);
		
		StringBuffer selectorBuffer = new StringBuffer();

		selectorBuffer.append("xpath=//form//div[contains(@class, '");
		selectorBuffer.append(getGuiButtonsCss());
		selectorBuffer.append("')]//");
		selectorBuffer.append("button[@type='button']");

		browser.click(selectorBuffer.toString());
		functionalUtil.waitForPageToLoad(browser);
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());

		return(true);
	}

	/**
	 * 
	 * @param browser
	 * @return
	 */
	public boolean disableBack(Selenium browser){
		//TODO:JK: ugly
		functionalUtil.openSite(browser, OlatSite.HOME);

		/* goto home site */
		//TODO:JK: ugly
		Assert.assertTrue(openActionByMenuTree(browser, HomeSiteAction.SETTINGS));
		Assert.assertTrue(checkCurrentAction(browser, HomeSiteAction.SETTINGS));

		/* open system tab */
		//TODO:JK: ugly
		Assert.assertTrue(functionalUtil.openContentTab(browser, SettingsTab.SYSTEM.ordinal()));

		/* enable resume */
		//TODO:JK: ugly
		Assert.assertTrue(functionalUtil.clickRadio(browser,
				FunctionalHomeSiteUtil.PortalSettingsForms.SpecificSystemSettingsRadios.SUPPORT_FOR_BROWSER_BACK.getGroupCss(),
				FunctionalHomeSiteUtil.PortalSettingsForms.SpecificSystemSettingsRadios.SupportForBrowserBack.OFF.getValueAttribute()));

		functionalUtil.idle(browser);
		
		StringBuffer selectorBuffer = new StringBuffer();

		selectorBuffer.append("xpath=//form//div[contains(@class, '");
		selectorBuffer.append(getGuiButtonsCss());
		selectorBuffer.append("')]//");
		selectorBuffer.append("button[@type='button']");

		browser.click(selectorBuffer.toString());
		functionalUtil.waitForPageToLoad(browser);
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());

		return(true);
	}

	/**
	 * Resets portal settings to default.
	 * 
	 * @param browser
	 * @return true on success
	 */
	public boolean resetSettings(Selenium browser){
		log.info("open portal");
		//TODO:JK: ugly
		functionalUtil.openSite(browser, OlatSite.HOME);

		/* open settings page */
		log.info("open settings tab");
		if(!openActionByMenuTree(browser, HomeSiteAction.SETTINGS)){
			return(false);
		}

		/* click system tab */
		log.info("open system settings tab");
		functionalUtil.openContentTab(browser, SettingsTab.SYSTEM.ordinal());
		
		/* using reset configurations form */
		/* click all checkboxes */
		log.info("clicking all reset configurations checkboxes");
		functionalUtil.clickCheckbox(browser, CONFIGURATIONS_CSS, GUI_PREFERENCES_VALUE);

		functionalUtil.clickCheckbox(browser, CONFIGURATIONS_CSS, SYS_PREFERENCES_VALUE);

		functionalUtil.clickCheckbox(browser, CONFIGURATIONS_CSS, RESUME_VALUE);

		/* click Reset */
		functionalUtil.idle(browser);
		
		StringBuffer selectorBuffer = new StringBuffer();

		selectorBuffer.append("xpath=//form//div[contains(@class, '");
		selectorBuffer.append(getSysprefsButtonsCss());
		selectorBuffer.append("')]//");
		selectorBuffer.append("button[@type='button']");

		log.info("submitting changes");
		browser.click(selectorBuffer.toString());
		functionalUtil.waitForPageToLoad(browser);
		//functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		
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

	public String getEPortfolioParentId() {
		return ePortfolioParentId;
	}

	public void setEPortfolioParentId(String ePortfolioParentId) {
		this.ePortfolioParentId = ePortfolioParentId;
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

	public String getPortalCss() {
		return portalCss;
	}

	public void setPortalCss(String portalCss) {
		this.portalCss = portalCss;
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
