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
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.httpclient.HttpClient;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.util.FunctionalAdministrationSiteUtil.AdministrationSiteAction;
import org.olat.util.FunctionalGroupsSiteUtil.GroupsSiteAction;
import org.olat.util.FunctionalHomeSiteUtil.HomeSiteAction;
import org.olat.util.FunctionalRepositorySiteUtil.RepositorySiteAction;
import org.olat.util.FunctionalUserManagementSiteUtil.UserManagementSiteAction;
import org.springframework.beans.factory.annotation.Autowired;

import com.thoughtworks.selenium.Selenium;

/**
 * 
 * @author jkraehemann, joel.kraehemann@frentix.com, frentix.com
 */
public class FunctionalUtil {
	private final static OLog log = Tracing.createLoggerFor(FunctionalUtil.class);
	
	public final static String DEPLOYMENT_URL = "http://localhost:8080/olat";
	public final static String WAIT_LIMIT = "5000";
	
	public final static String LOGIN_PAGE = "dmz";
	public final static String ACKNOWLEDGE_CHECKBOX = "acknowledge_checkbox";
	
	public final static String INFO_DIALOG = "o_interceptionPopup";

	public enum WaitLimitAttribute {
		NORMAL("0"),
		EXTENDED("3000"),
		SAVE("7000"),
		VERY_SAVE("12000");
		
		private String extend;
		
		WaitLimitAttribute(String extend){
			setExtend(extend);
		}

		public String getExtend() {
			return extend;
		}

		public void setExtend(String extend) {
			this.extend = extend;
		}
	}
	
	public enum OlatSite {
		HOME,
		GROUPS,
		LEARNING_RESOURCES,
		GROUP_ADMINISTRATION,
		USER_MANAGEMENT,
		ADMINISTRATION,
	}

	public final static String OLAT_TOP_NAVIGATION_LOGOUT_CSS = "o_topnav_logout";
	
	public final static String OLAT_NAVIGATION_SITE_CSS = "b_nav_site";
	public final static String OLAT_ACTIVE_NAVIGATION_SITE_CSS = "b_nav_active";
	
	public final static String OLAT_SITE_HOME_CSS = "o_site_home";
	public final static String OLAT_SITE_GROUPS_CSS = "o_site_groups";
	public final static String OLAT_SITE_LEARNING_RESOURCES_CSS = "o_site_repository";
	public final static String OLAT_SITE_GROUP_ADMINISTRATION_CSS = "o_site_groupsmanagement";
	public final static String OLAT_SITE_USER_MANAGEMENT_CSS = "o_site_useradmin";
	public final static String OLAT_SITE_ADMINISTRATION_CSS = "o_site_admin";

	public final static String CONTENT_CSS = "b_main";
	public final static String CONTENT_TAB_CSS = "b_item_";
	public final static String ACTIVE_CONTENT_TAB_CSS = "b_active";
	
	public final static String FORM_SAVE_XPATH = "//button[@type='button' and last()]";
	
	public final static String WIZARD_CSS = "b_wizard";
	public final static String WIZARD_NEXT_CSS = "b_wizard_button_next";
	public final static String WIZARD_FINISH_CSS = "b_wizard_button_finish";
	
	public final static String MCE_CONTENT_BODY_CSS = "mceContentBody";
	
	private String username;
	private String password;
	
	private String deploymentUrl;
	private String waitLimit;
	
	private String loginPage;
	private String acknowledgeCheckbox;
	
	private String infoDialog;
	
	private String olatTopNavigationLogoutCss;
	
	private String olatNavigationSiteCss;
	private String olatActiveNavigationSiteCss;
	
	private String olatSiteHomeCss;
	private String olatSiteGroupsCss;
	private String olatSiteLearningResourcesCss;
	private String olatSiteGroupAdministrationCss;
	private String olatSiteUserManagementCss;
	private String olatSiteAdministrationCss;
	
	private String contentCss;
	private String contentTabCss;
	private String activeContentTabCss;
	
	private String formSaveXPath;
	
	private String wizardCss;
	private String wizardNextCss;
	private String wizardFinishCss;
	
	private String mceContentBodyCss;
	
	private FunctionalHomeSiteUtil functionalHomeSiteUtil;
	private FunctionalGroupsSiteUtil functionalGroupsSiteUtil;
	private FunctionalRepositorySiteUtil functionalRepositorySiteUtil;
	private FunctionalUserManagementSiteUtil functionalUserManagementSiteUtil;
	private FunctionalAdministrationSiteUtil functionalAdministrationSiteUtil;
	
	public FunctionalUtil(){
		Properties properties = new Properties();
		
		try {
			//TODO:JK: use default properties file
			properties.load(FunctionalUtil.class.getResourceAsStream("credentials.properties"));
			
			username = properties.getProperty("admin.login");
			password = properties.getProperty("admin.password");
		} catch (IOException e) {

			username = "administrator";
			password = "openolat";
			
			//TODO:JK: Auto-generated catch block
			e.printStackTrace();
		}
		
		deploymentUrl = DEPLOYMENT_URL;
		waitLimit = WAIT_LIMIT;
		
		loginPage = LOGIN_PAGE;
		acknowledgeCheckbox = ACKNOWLEDGE_CHECKBOX;
		
		infoDialog = INFO_DIALOG;
		
		olatTopNavigationLogoutCss = OLAT_TOP_NAVIGATION_LOGOUT_CSS;
		
		olatNavigationSiteCss = OLAT_NAVIGATION_SITE_CSS;
		olatActiveNavigationSiteCss = OLAT_ACTIVE_NAVIGATION_SITE_CSS;
		
		olatSiteHomeCss = OLAT_SITE_HOME_CSS;
		olatSiteGroupsCss = OLAT_SITE_GROUPS_CSS;
		olatSiteLearningResourcesCss = OLAT_SITE_LEARNING_RESOURCES_CSS;
		olatSiteGroupAdministrationCss = OLAT_SITE_GROUP_ADMINISTRATION_CSS;
		olatSiteUserManagementCss = OLAT_SITE_USER_MANAGEMENT_CSS;
		olatSiteAdministrationCss = OLAT_SITE_ADMINISTRATION_CSS;
		
		contentCss = CONTENT_CSS;
		contentTabCss = CONTENT_TAB_CSS;
		activeContentTabCss = ACTIVE_CONTENT_TAB_CSS;
		
		formSaveXPath = FORM_SAVE_XPATH;
		
		wizardCss = WIZARD_CSS;
		wizardNextCss = WIZARD_NEXT_CSS;
		wizardFinishCss = WIZARD_FINISH_CSS;
		
		mceContentBodyCss = MCE_CONTENT_BODY_CSS;
		
		functionalHomeSiteUtil = new FunctionalHomeSiteUtil(this);
		functionalGroupsSiteUtil = new FunctionalGroupsSiteUtil(this);
		functionalRepositorySiteUtil = new FunctionalRepositorySiteUtil(this);
		functionalUserManagementSiteUtil = new FunctionalUserManagementSiteUtil(this);
		functionalAdministrationSiteUtil = new FunctionalAdministrationSiteUtil(this);
	}
	
	/**
	 * @param browser
	 * @param page
	 * 
	 * Loads the specified page with default deployment url.
	 */
	public void loadPage(Selenium browser, String page){
		/* create url */
		StringBuffer urlBuffer = new StringBuffer();
		
		urlBuffer.append(deploymentUrl)
		.append('/')
		.append(page);
		
		String url = urlBuffer.toString();
		
		/* open url and wait specified time */
		browser.open(url);
		waitForPageToLoad(browser);
	}
	
	public void waitForPageToLoad(Selenium browser){
		waitForPageToLoad(browser, WaitLimitAttribute.VERY_SAVE);
	}
	
	public void waitForPageToLoad(Selenium browser, WaitLimitAttribute wait){
		String waitLimit = Long.toString(Long.parseLong(getWaitLimit()) + Long.parseLong(wait.getExtend()));
		
		browser.waitForPageToLoad(waitLimit);
	}

	/**
	 * @param browser
	 * @param locator
	 * @return true on success otherwise false
	 * 
	 * Waits at most (waitLimit + WaitLimitAttribute.VERY_SAVE) amount of time for element to load
	 * specified by locator.
	 */
	public boolean waitForPageToLoadElement(Selenium browser, String locator){
		return(waitForPageToLoadElement(browser, locator, WaitLimitAttribute.VERY_SAVE));
	}
	
	/**
	 * @param browser
	 * @param locator
	 * @param wait
	 * @return true on success otherwise false
	 * 
	 * Waits at most (waitLimit + wait) amount of time for element to load
	 * specified by locator.
	 */
	public boolean waitForPageToLoadElement(Selenium browser, String locator, WaitLimitAttribute wait){
		long startTime = Calendar.getInstance().getTimeInMillis();
		long currentTime = startTime;
		long waitLimit = Long.parseLong(getWaitLimit()) + Long.parseLong(wait.getExtend());

		log.info("waiting for page to load element");
		
		do{
			if(browser.isElementPresent(locator)){
				log.info("found element after " + (currentTime - startTime) + "ms");
				
				return(true);
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			
			currentTime = Calendar.getInstance().getTimeInMillis();
		}while(waitLimit >  currentTime - startTime);
		
		log.warn("giving up after " + waitLimit + "ms");
		
		return(false);
	}
	
	/**
	 * @param site
	 * @return the matching CSS class
	 * 
	 * Find CSS mapping for specific olat site.
	 */
	public String findCssClassOfSite(OlatSite site){
		String selectedCss;
		
		selectedCss = null;
		
		switch(site){
		case HOME:
		{
			selectedCss = getOlatSiteHomeCss();
			break;
		}
		case GROUPS:
		{
			selectedCss = getOlatSiteGroupsCss();
			break;
		}
		case LEARNING_RESOURCES:
		{
			selectedCss = getOlatSiteLearningResourcesCss();
			break;
		}
		case GROUP_ADMINISTRATION:
		{
			selectedCss = getOlatSiteAdministrationCss();
			break;
		}
		case USER_MANAGEMENT:
		{
			selectedCss = getOlatSiteUserManagementCss();
			break;
		}
		case ADMINISTRATION:
		{
			selectedCss = getOlatSiteAdministrationCss();
			break;
		}
		}
		
		return(selectedCss);
	}
	
	/**
	 * @param browser
	 * @param site
	 * @return true if match otherwise false
	 * 
	 * Check if the correct olat site is open.
	 */
	public boolean checkCurrentSite(Selenium browser, OlatSite site){
		return(checkCurrentSite(browser, site, -1));
	}
	
	public boolean checkCurrentSite(Selenium browser, OlatSite site, long timeout){
		String selectedCss = findCssClassOfSite(site);
		
		if(selectedCss == null){
			return(false);
		}
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("css=.")
		.append(getOlatNavigationSiteCss())
		.append(".")
		.append(getOlatActiveNavigationSiteCss())
		.append(".")
		.append(selectedCss);

		long timeElapsed = 0;
		long startTime = Calendar.getInstance().getTimeInMillis();
		
		do{
			if(browser.isElementPresent(selectorBuffer.toString())){
				return(true);
			}
			
			if(timeout != -1){
				try {
					Thread.sleep(1000);
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
	 * @param browser
	 * @param site
	 * @return true on success
	 * 
	 * Clicks the appropriate action on a site that it would be
	 * on it's initial state i.e. it looks like the user wouldn't
	 * have visited site before.
	 */
	private boolean resetSite(Selenium browser, OlatSite site){
		boolean retval = false;
		
		switch(site){
		case HOME:
		{
			retval = functionalHomeSiteUtil.openActionByMenuTree(browser, HomeSiteAction.PORTAL);
		}
		break;
		case GROUPS:
		{
			retval = functionalGroupsSiteUtil.openActionByMenuTree(browser, GroupsSiteAction.MY_GROUPS);
		}
		break;
		case LEARNING_RESOURCES:
		{
			retval = functionalRepositorySiteUtil.openActionByMenuTree(browser, RepositorySiteAction.MY_ENTRIES);
		}
		break;
		case USER_MANAGEMENT:
		{
			retval = functionalUserManagementSiteUtil.openActionByMenuTree(browser, UserManagementSiteAction.USER_SEARCH);
		}
		break;
		case GROUP_ADMINISTRATION:
		{
			//FIXME:JK: need to be implemented
		}
		break;
		case ADMINISTRATION:
		{
			retval = functionalAdministrationSiteUtil.openActionByMenuTree(browser, AdministrationSiteAction.SYSTEM_ADMINISTRATION);
		}
		break;
		}
		
		return(retval);
	}
	
	/**
	 * @param browser
	 * @param site
	 * @return true on success otherwise false
	 * 
	 * Open a specific olat site.
	 */
	public boolean openSite(Selenium browser, OlatSite site){
		String selectedCss = findCssClassOfSite(site);
		
		if(selectedCss == null){
			return(false);
		}
		
		if(checkCurrentSite(browser, site, Long.parseLong(getWaitLimit()))){
			if(resetSite(browser, site)){
				return(true);
			}else{
				return(false);
			}
		}
		
		/* open the appropriate site */
		StringBuilder selectorBuffer = new StringBuilder();
		
		selectorBuffer.append("css=.")
		.append(getOlatNavigationSiteCss())
		.append(".")
		.append(selectedCss)
		.append(" a");
		
		browser.click(selectorBuffer.toString());
		waitForPageToLoad(browser);
		waitForPageToLoadElement(browser, selectorBuffer.toString(), WaitLimitAttribute.NORMAL);
		
		/* set it to it's initial state */
		resetSite(browser, site);
		
		return(true);
	}
	
	/**
	 * @param browser
	 * @return true on success otherwise false
	 * 
	 * Login to olat using selenium.
	 */
	public boolean login(Selenium browser){
		return login(browser, true);
	}
	
	public boolean login(Selenium browser, boolean closeDialogs){
		return(login(browser, getUsername(), getPassword(), closeDialogs));
	}
	
	public boolean login(Selenium browser, String username, String password, boolean closeDialogs){
		loadPage(browser, getLoginPage());
		
		/* fill in login form */
		browser.type("id=o_fiooolat_login_name", username);
		browser.type("id=o_fiooolat_login_pass", password);
	    browser.click("id=o_fiooolat_login_button");
	    waitForPageToLoad(browser);
	    
	    if(closeDialogs){
	    	/* check if it's our first login */
	    	if(browser.isElementPresent("name=" + getAcknowledgeCheckbox())){
	    		browser.click("name=" + getAcknowledgeCheckbox());

	    		/* click accept button */
	    		browser.click("xpath=//div[contains(@class, 'b_window')]//button[last()]");
	    		waitForPageToLoad(browser);
	    	}

	    	/* click away info dialogs eg. restore session */
	    	//TODO:JK: find a way to solve endless loop
	    	//while(browser.isElementPresent("class="+ getInfoDialog())){
	    		/* click last button */
	    	if(browser.isElementPresent("id="+ getInfoDialog())){
	    		browser.click("xpath=//form//div//button[@type='button']/../../span/a[@class='b_button']");
	    		waitForPageToLoad(browser);
	    	}
	    	//}
	    }
		
		/* validate page */
	    return(true);
	}
	
	/**
	 * @param browser
	 * @return
	 * 
	 * Logout from olat LMS.
	 */
	public boolean logout(Selenium browser){
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("css=#")
		.append(getOlatTopNavigationLogoutCss())
		.append(" a");
		
		browser.click(selectorBuffer.toString());
		waitForPageToLoad(browser);
		
		return(true);
	}
	
	/**
	 * @param browser
	 * @param tabIndex
	 * @return true on success otherwise false
	 * 
	 * Opens a tab at the specific tabIndex.
	 */
	public boolean openContentTab(Selenium browser, int tabIndex){
		StringBuffer activeTabSelectorBuffer = new StringBuffer();
		
		activeTabSelectorBuffer.append("css=#")
		.append(getContentCss())
		.append(" ul .")
		.append(getContentTabCss())
		.append(tabIndex + 1)
		.append('.')
		.append(getActiveContentTabCss());
		
		if(!browser.isElementPresent(activeTabSelectorBuffer.toString())){
			StringBuffer selectorBuffer = new StringBuffer();
			
			selectorBuffer.append("css=#")
			.append(getContentCss())
			.append(" ul .")
			.append(getContentTabCss())
			.append(tabIndex + 1)
			.append(" * a");
			
			browser.click(selectorBuffer.toString());
			waitForPageToLoad(browser);
		}
		
		return(true);
	}
	
	/**
	 * @param browser
	 * @param formIndex
	 * @return true on success
	 * 
	 * Save the form at the position formIndex within content element.
	 */
	public boolean saveForm(Selenium browser, int formIndex){
		saveForm(browser, formIndex, getWaitLimit());
		
		return(true);
	}
	
	public boolean saveForm(Selenium browser, int formIndex, String waitLimit){
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//form[")
		.append(formIndex + 1)
		.append("]")
		.append(getFormSaveXPath());
		
		browser.click(selectorBuffer.toString());
		browser.waitForPageToLoad(waitLimit);
		
		return(true);
	}
	
	/**
	 * @param browser
	 * @param groupCss
	 * @param value
	 * @return true on success
	 * 
	 * Clicks the radio button with specific value attribute in groupCss container.
	 */
	public boolean clickCheckbox(Selenium browser, String groupCss, String value){
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//form")
		.append("//div[@class='b_form_selection_vertical' or @class='b_form_selection_horizontal']")
		.append("//input[@type='checkbox' and @value='")
		.append(value)
		.append("']");
		
		browser.click(selectorBuffer.toString());
		
		return(true);
	}
	
	/**
	 * @param browser
	 * @param formIndex
	 * @param radioGroupIndex
	 * @param radioIndex
	 * @return true on success
	 * 
	 * Clicks the radio button at position radioIndex from the selection at position radioGroupIndex.
	 */
	@Deprecated
	public boolean clickRadio(Selenium browser, int formIndex, int radioGroupIndex, int radioIndex){
		StringBuffer selectorBuffer = new StringBuffer();
	
		selectorBuffer.append("xpath=//form[")
		.append(formIndex)
		.append("]")
		.append("//div[@class='b_form_selection_vertical' or @class='b_form_selection_horizontal'][")
		.append(radioGroupIndex)
		.append("]")
		.append("//input[@type='radio'][")
		.append(radioIndex)
		.append("]");
		
		browser.click(selectorBuffer.toString());
		
		return(true);
	}
	
	/**
	 * @param browser
	 * @param groupCss
	 * @param value
	 * @return true on success
	 * 
	 * Clicks the radio button with specific value attribute in groupCss container.
	 */
	public boolean clickRadio(Selenium browser, String groupCss, String value){
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("//form")
		.append("//div[@class='b_form_selection_vertical' or @class='b_form_selection_horizontal']")
		.append("//input[@type='radio' and @value='")
		.append(value)
		.append("']");
		
		browser.click(selectorBuffer.toString());

		return(true);
	}
	
	/**
	 * @param browser
	 * @param formIndex
	 * @param textIndex
	 * @param text
	 * @return true on success
	 * 
	 * Type text in the specified text entry at textIndex position within form at formIndex.
	 */
	@Deprecated
	public boolean typeText(Selenium browser, int formIndex, int textIndex, String text){
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//form[")
		.append(formIndex)
		.append("]")
		.append("//input[@type='text'][")
		.append(textIndex)
		.append("]");
		
		browser.type(selectorBuffer.toString(), text);
		
		return(true);
	}
	
	/**
	 * @param browser
	 * @param entryCss
	 * @param text
	 * @return true on success
	 * 
	 * Types text to the with CSS class specified entry.
	 */
	public boolean typeText(Selenium browser, String entryCss, String text){
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//form")
		.append("//div[contains(@class, '")
		.append(entryCss)
		.append("')]")
		.append("//input[@type='text']");
		
		browser.type(selectorBuffer.toString(), text);
		
		return(true);
	}
	
	public boolean typeMCE(Selenium browser, String content){
		if(content == null)
			return(true);
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("dom=document.getElementsByClassName('")
		.append("mceIframeContainer")
		.append("')[0].getElementsByTagName('iframe')[0].contentDocument.body");
		
		waitForPageToLoadElement(browser, selectorBuffer.toString());
		
		browser.type(selectorBuffer.toString(), content);
		
		return(true);
	}
	
	/**
	 * @param browser
	 * @param entryCss
	 * @param text
	 * @return true on success
	 * 
	 * Types text to the with CSS class specified password entry.
	 */
	public boolean typePassword(Selenium browser, String entryCss, String text){
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//form")
		.append("//div[contains(@class, '")
		.append(entryCss)
		.append("')]")
		.append("//input[@type='password']");
		
		browser.type(selectorBuffer.toString(), text);
		
		return(true);
	}
	
	/**
	 * @param browser
	 * @param id
	 * @param value
	 * @return
	 * 
	 * Select an item of an option box.
	 */
	public boolean selectOption(Selenium browser, String id, String value){
		StringBuffer selectLocatorBuffer = new StringBuffer();
		
		selectLocatorBuffer.append("xpath=//form")
		.append("//select[@id='")
		.append(id)
		.append("']");
		
		StringBuffer optionLocatorBuffer = new StringBuffer();
		
		optionLocatorBuffer.append("value=")
		.append(value);
		
		browser.select(selectLocatorBuffer.toString(), optionLocatorBuffer.toString());
		
		return(true);
	}
	
	/**
	 * @param browser
	 * @return
	 * 
	 * Clicks the next button of a wizard.
	 */
	public boolean clickWizardNext(Selenium browser){
		StringBuffer locatorBuffer = new StringBuffer();
		
		locatorBuffer.append("xpath=//form//a[contains(@class, '")
		.append(getWizardNextCss())
		.append("')]");
		
		browser.click(locatorBuffer.toString());
		
		return(true);
	}
	
	/**
	 * @param browser
	 * @return
	 * 
	 * Clicks the finish button of a wizard.
	 */
	public boolean clickWizardFinish(Selenium browser){
		StringBuffer locatorBuffer = new StringBuffer();
		
		locatorBuffer.append("xpath=//form//a[contains(@class, '")
		.append(getWizardFinishCss())
		.append("')]");
		
		browser.click(locatorBuffer.toString());
		
		return(true);
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDeploymentUrl() {
		return deploymentUrl;
	}

	public void setDeploymentUrl(String deploymentUrl) {
		this.deploymentUrl = deploymentUrl;
	}

	public String getWaitLimit() {
		return waitLimit;
	}

	public void setWaitLimit(String waitLimit) {
		this.waitLimit = waitLimit;
	}

	public String getLoginPage() {
		return loginPage;
	}

	public void setLoginPage(String loginPage) {
		this.loginPage = loginPage;
	}

	public String getAcknowledgeCheckbox() {
		return acknowledgeCheckbox;
	}

	public void setAcknowledgeCheckbox(String acknowledgeCheckbox) {
		this.acknowledgeCheckbox = acknowledgeCheckbox;
	}

	public String getInfoDialog() {
		return infoDialog;
	}

	public void setInfoDialog(String infoDialog) {
		this.infoDialog = infoDialog;
	}

	public String getOlatTopNavigationLogoutCss() {
		return olatTopNavigationLogoutCss;
	}

	public void setOlatTopNavigationLogoutCss(String olatTopNavigationLogoutCss) {
		this.olatTopNavigationLogoutCss = olatTopNavigationLogoutCss;
	}

	public String getOlatNavigationSiteCss() {
		return olatNavigationSiteCss;
	}

	public void setOlatNavigationSiteCss(String olatNavigationSiteCss) {
		this.olatNavigationSiteCss = olatNavigationSiteCss;
	}

	public String getOlatActiveNavigationSiteCss() {
		return olatActiveNavigationSiteCss;
	}

	public void setOlatActiveNavigationSiteCss(String olatActiveNavigationSiteCss) {
		this.olatActiveNavigationSiteCss = olatActiveNavigationSiteCss;
	}

	public String getOlatSiteHomeCss() {
		return olatSiteHomeCss;
	}

	public void setOlatSiteHomeCss(String olatSiteHomeCss) {
		this.olatSiteHomeCss = olatSiteHomeCss;
	}

	public String getOlatSiteGroupsCss() {
		return olatSiteGroupsCss;
	}

	public void setOlatSiteGroupsCss(String olatSiteGroupsCss) {
		this.olatSiteGroupsCss = olatSiteGroupsCss;
	}

	public String getOlatSiteLearningResourcesCss() {
		return olatSiteLearningResourcesCss;
	}

	public void setOlatSiteLearningResourcesCss(String olatSiteLearningResourcesCss) {
		this.olatSiteLearningResourcesCss = olatSiteLearningResourcesCss;
	}

	public String getOlatSiteGroupAdministrationCss() {
		return olatSiteGroupAdministrationCss;
	}

	public void setOlatSiteGroupAdministrationCss(
			String olatSiteGroupAdministrationCss) {
		this.olatSiteGroupAdministrationCss = olatSiteGroupAdministrationCss;
	}

	public String getOlatSiteUserManagementCss() {
		return olatSiteUserManagementCss;
	}

	public void setOlatSiteUserManagementCss(String olatSiteUserManagementCss) {
		this.olatSiteUserManagementCss = olatSiteUserManagementCss;
	}

	public String getOlatSiteAdministrationCss() {
		return olatSiteAdministrationCss;
	}

	public void setOlatSiteAdministrationCss(String olatSiteAdministrationCss) {
		this.olatSiteAdministrationCss = olatSiteAdministrationCss;
	}
	
	public String getContentCss() {
		return contentCss;
	}

	public void setContentCss(String contentCss) {
		this.contentCss = contentCss;
	}

	public String getContentTabCss() {
		return contentTabCss;
	}

	public void setContentTabCss(String contentTabCss) {
		this.contentTabCss = contentTabCss;
	}

	public String getActiveContentTabCss() {
		return activeContentTabCss;
	}

	public void setActiveContentTabCss(String activeContentTabCss) {
		this.activeContentTabCss = activeContentTabCss;
	}

	public String getFormSaveXPath() {
		return formSaveXPath;
	}

	public void setFormSaveXPath(String formSaveXPath) {
		this.formSaveXPath = formSaveXPath;
	}

	public String getWizardCss() {
		return wizardCss;
	}

	public void setWizardCss(String wizardCss) {
		this.wizardCss = wizardCss;
	}

	public String getWizardNextCss() {
		return wizardNextCss;
	}

	public void setWizardNextCss(String wizardNextCss) {
		this.wizardNextCss = wizardNextCss;
	}

	public String getWizardFinishCss() {
		return wizardFinishCss;
	}

	public void setWizardFinishCss(String wizardFinishCss) {
		this.wizardFinishCss = wizardFinishCss;
	}

	public String getMceContentBodyCss() {
		return mceContentBodyCss;
	}

	public void setMceContentBodyCss(String mceContentBodyCss) {
		this.mceContentBodyCss = mceContentBodyCss;
	}

	public FunctionalHomeSiteUtil getFunctionalHomeSiteUtil() {
		return functionalHomeSiteUtil;
	}

	public void setFunctionalHomeSiteUtil(
			FunctionalHomeSiteUtil functionalHomeSiteUtil) {
		this.functionalHomeSiteUtil = functionalHomeSiteUtil;
	}

	public FunctionalGroupsSiteUtil getFunctionalGroupsSiteUtil() {
		return functionalGroupsSiteUtil;
	}

	public void setFunctionalGroupsSiteUtil(
			FunctionalGroupsSiteUtil functionalGroupsSiteUtil) {
		this.functionalGroupsSiteUtil = functionalGroupsSiteUtil;
	}

	public FunctionalRepositorySiteUtil getFunctionalRepositorySiteUtil() {
		return functionalRepositorySiteUtil;
	}

	public void setFunctionalRepositorySiteUtil(
			FunctionalRepositorySiteUtil functionalRepositorySiteUtil) {
		this.functionalRepositorySiteUtil = functionalRepositorySiteUtil;
	}

	public FunctionalUserManagementSiteUtil getFunctionalUserManagementSiteUtil() {
		return functionalUserManagementSiteUtil;
	}

	public void setFunctionalUserManagementSiteUtil(
			FunctionalUserManagementSiteUtil functionalUserManagementSiteUtil) {
		this.functionalUserManagementSiteUtil = functionalUserManagementSiteUtil;
	}

	public FunctionalAdministrationSiteUtil getFunctionalAdministrationSiteUtil() {
		return functionalAdministrationSiteUtil;
	}

	public void setFunctionalAdministrationSiteUtil(
			FunctionalAdministrationSiteUtil functionalAdministrationSiteUtil) {
		this.functionalAdministrationSiteUtil = functionalAdministrationSiteUtil;
	}
}
