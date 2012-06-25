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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.junit.Assert;
import org.olat.restapi.RestConnection;
import org.olat.user.restapi.UserVO;

import com.thoughtworks.selenium.Selenium;

/**
 * 
 * @author jkraehemann, joel.kraehemann@frentix.com, frentix.com
 */
public class FunctionalUtil {
	public final static String DEPLOYMENT_URL = "http://localhost:8080/olat";
	public final static String WAIT_LIMIT = "5000";
	
	public final static String LOGIN_PAGE = "dmz";
	public final static String ACKNOWLEDGE_CHECKBOX = "acknowledge_checkbox";
	
	public final static String INFO_DIALOG = "b_info";
	
	public enum OlatSite {
		HOME,
		GROUPS,
		LEARNING_RESOURCES,
		GROUP_ADMINISTRATION,
		USER_MANAGEMENT,
		ADMINISTRATION,
	}

	public final static String OLAT_NAVIGATION_TAB_CSS = "b_nav_site";
	public final static String OLAT_ACTIVE_NAVIGATION_TAB_CSS = "b_nav_active";
	
	public final static String OLAT_SITE_HOME_CSS = "o_site_home";
	public final static String OLAT_SITE_GROUPS_CSS = "o_site_groups";
	public final static String OLAT_SITE_LEARNING_RESOURCES_CSS = "o_site_repository";
	public final static String OLAT_SITE_GROUP_ADMINISTRATION_CSS = "o_site_groupsmanagement";
	public final static String OLAT_SITE_USER_MANAGEMENT_CSS = "o_site_useradmin";
	public final static String OLAT_SITE_ADMINISTRATION_CSS = "o_site_admin";
	
	private String username;
	private String password;
	
	private HashMap<String,String> seleniumCredentials = new HashMap<String,String>();
	
	private String deploymentUrl;
	private String waitLimit;
	
	private String loginPage;
	private String acknowledgeCheckbox;
	
	private String infoDialog;
	
	private String olatNavigationSiteCss;
	private String olatActiveNavigationSiteCss;
	
	private String olatSiteHomeCss;
	private String olatSiteGroupsCss;
	private String olatSiteLearningResourcesCss;
	private String olatSiteGroupAdministrationCss;
	private String olatSiteUserManagementCss;
	private String olatSiteAdministrationCss;
	
	public FunctionalUtil(){
		Properties properties = new Properties();
		
		try {
			properties.load(FunctionalUtil.class.getResourceAsStream("credentials.properties"));
			
			username = properties.getProperty("admin.login");
			password = properties.getProperty("admin.password");
		} catch (IOException e) {

			username = "administrator";
			password = "openolat";
			
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		deploymentUrl = DEPLOYMENT_URL;
		waitLimit = WAIT_LIMIT;
		
		loginPage = LOGIN_PAGE;
		acknowledgeCheckbox = ACKNOWLEDGE_CHECKBOX;
		
		infoDialog = INFO_DIALOG;
		
		olatNavigationSiteCss = OLAT_NAVIGATION_TAB_CSS;
		olatActiveNavigationSiteCss = OLAT_ACTIVE_NAVIGATION_TAB_CSS;
		
		olatSiteHomeCss = OLAT_SITE_HOME_CSS;
		olatSiteGroupsCss = OLAT_SITE_GROUPS_CSS;
		olatSiteLearningResourcesCss = OLAT_SITE_LEARNING_RESOURCES_CSS;
		olatSiteGroupAdministrationCss = OLAT_SITE_GROUP_ADMINISTRATION_CSS;
		olatSiteUserManagementCss = OLAT_SITE_USER_MANAGEMENT_CSS;
		olatSiteAdministrationCss = OLAT_SITE_ADMINISTRATION_CSS;
	}
	
	/**
	 * @param deploymentUrl
	 * @param count
	 * @throws IOException
	 * @throws URISyntaxException
	 * 
	 * Creates the selenium test users with random passwords and
	 * writes it to credentials.properties.
	 */
	public void createTestUsers(URL deploymentUrl, int count) throws IOException, URISyntaxException{
		RestConnection restConnection = new RestConnection(deploymentUrl);

		restConnection.login(getUsername(), getPassword());
		
		for(int i = 0; i < count; i++){
			UserVO vo = new UserVO();
			String username = "selenium_" + i + "_" + UUID.randomUUID().toString();
			vo.setLogin(username);
			String password = "passwd_" + i + "_" + UUID.randomUUID().toString();
			vo.setPassword(password);
			vo.setFirstName("John_" + i);
			vo.setLastName("Smith");
			vo.setEmail(username + "@frentix.com");
			vo.putProperty("telOffice", "39847592");
			vo.putProperty("telPrivate", "39847592");
			vo.putProperty("telMobile", "39847592");
			vo.putProperty("gender", "Female");//male or female
			vo.putProperty("birthDay", "12/12/2009");

			URI request = UriBuilder.fromUri(deploymentUrl.toURI()).path("restapi").path("users").build();
			HttpPut method = restConnection.createPut(request, MediaType.APPLICATION_JSON, true);
			restConnection.addJsonEntity(method, vo);
			method.addHeader("Accept-Language", "en");

			HttpResponse response = restConnection.execute(method);
			assertTrue(response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201);
			InputStream body = response.getEntity().getContent();
			
			seleniumCredentials.put(username, password);
		}

		restConnection.shutdown();
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
		browser.waitForPageToLoad(getWaitLimit());
	}

	/**
	 * @param site
	 * @return
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
	 * Check if the correct olat tab is open.
	 */
	public boolean checkCurrentSite(Selenium browser, OlatSite site){
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
		
		if(browser.isElementPresent(selectorBuffer.toString())){
			return(true);
		}else{
			return(false);	
		}
	}
	
	/**
	 * @param browser
	 * @param site
	 * 
	 * Open a specific olat site.
	 */
	public void openSite(Selenium browser, OlatSite site){
		String selectedCss = findCssClassOfSite(site);
		
		if(selectedCss == null){
			return;
		}
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("css=.")
		.append(getOlatNavigationSiteCss())
		.append(".")
		.append(selectedCss)
		.append(" * a");
		
		browser.click(selectorBuffer.toString());
		browser.waitForPageToLoad("10000");
	}
	
	/**
	 * @param browser
	 * @return true on success otherwise false
	 * 
	 * Login to olat using selenium.
	 */
	public boolean login(Selenium browser){
		loadPage(browser, getLoginPage());
		
		/* fill in login form */
		browser.type("id=o_fiooolat_login_name", username);
		browser.type("id=o_fiooolat_login_pass", password);
	    browser.click("id=o_fiooolat_login_button");
	    browser.waitForPageToLoad(getWaitLimit());
	    
		/* check if it's our first login */
		if(browser.isElementPresent("name=" + getAcknowledgeCheckbox())){
			browser.click("name=" + getAcknowledgeCheckbox());
			
			/* click accept button */
			browser.click("xpath=//div[@class='b_window']//button[last()]");
		    browser.waitForPageToLoad(getWaitLimit());
		}
		
		/* click away info dialogs eg. restore session */
		while(browser.isElementPresent("class="+ getInfoDialog())){
			/* click last button */
			browser.click("xpath=//form//button");
			browser.waitForPageToLoad(getWaitLimit());
		}
		
		/* validate page */
		if(checkCurrentSite(browser, OlatSite.HOME)){
			return(true);
		}else{
			return(false);
		}
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

	public HashMap<String, String> getSeleniumCredentials() {
		return seleniumCredentials;
	}

	public void setSeleniumCredentials(HashMap<String, String> seleniumCredentials) {
		this.seleniumCredentials = seleniumCredentials;
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
}
