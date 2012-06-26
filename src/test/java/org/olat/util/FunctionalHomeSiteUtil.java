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

import com.thoughtworks.selenium.Selenium;

/**
 * 
 * @author jkraehemann, joel.kraehemann@frentix.com, frentix.com
 */
public class FunctionalHomeSiteUtil {
	
	public final static String HOME_PAGE_NAVIGATION_SELECTED_CSS = "b_tree_selected";
	
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
	
	public enum HomePage {
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
}
