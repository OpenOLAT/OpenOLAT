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
package org.olat.portal;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.test.ArquillianDeployments;
import org.olat.user.restapi.UserVO;
import org.olat.util.FunctionalHomeSiteUtil;
import org.olat.util.FunctionalLocatorPairsFactory;
import org.olat.util.FunctionalUtil;
import org.olat.util.FunctionalVOUtil;
import org.olat.util.FunctionalHomeSiteUtil.SettingsTab;

import com.thoughtworks.selenium.DefaultSelenium;

/**
 * 
 * @author jkraehemann, joel.kraehemann@frentix.com, frentix.com
 */
@RunWith(Arquillian.class)
public class FunctionalSettingsTest {
	@Deployment(testable = false)
	public static WebArchive createDeployment() {
		return ArquillianDeployments.createDeployment();
	}

	@Drone
	DefaultSelenium browser;

	@ArquillianResource
	URL deploymentUrl;

	FunctionalUtil functionalUtil;
	FunctionalVOUtil functionalVOUtil;
	FunctionalHomeSiteUtil functionalHomeSiteUtil;
	
	int portalColumnCount;

	@Before
	public void setup(){
		functionalUtil = new FunctionalUtil();
		functionalUtil.setDeploymentUrl(deploymentUrl.toString());
		
		functionalVOUtil = new FunctionalVOUtil(functionalUtil.getUsername(), functionalUtil.getPassword());
		functionalHomeSiteUtil = new FunctionalHomeSiteUtil(functionalUtil);
		
		portalColumnCount = 2;
	}
	
	@Test
	@RunAsClient
	public void checkResetSettings(){
		/* login for test setup */
		Assert.assertTrue(functionalUtil.login(browser));
		
		/* reset settings */
		Assert.assertTrue(functionalHomeSiteUtil.resetSettings(browser));
	}
	
	@Test
	@RunAsClient
	public void checkLanguageSettings(){
		/* login for test setup */
		Assert.assertTrue(functionalUtil.login(browser));

		/* set language */
		functionalHomeSiteUtil.selectLanguage(browser, FunctionalHomeSiteUtil.GERMAN_LANGUAGE_VALUE);
	}
	
	@Test
	@RunAsClient
	public void checkDisableResume(){
		/* login for test setup */
		Assert.assertTrue(functionalUtil.login(browser));

		/* resume off */
		functionalHomeSiteUtil.disableResume(browser);
	}
	
	@Test
	@RunAsClient
	public void checkActivatePortlet(){
		/* login for test setup */
		Assert.assertTrue(functionalUtil.login(browser));
		
		/* click configure */
		functionalHomeSiteUtil.beginEditingPortal(browser);
		
		/* de-/activate portlets */
		if(functionalHomeSiteUtil.deactivatePortlet(browser, functionalHomeSiteUtil.getPortletEffCss())){
			Assert.assertFalse(functionalHomeSiteUtil.checkPortletActive(browser, functionalHomeSiteUtil.getPortletEffCss()));
		}
		
		if(functionalHomeSiteUtil.activatePortlet(browser, functionalHomeSiteUtil.getPortletNotesCss())){
			Assert.assertTrue(functionalHomeSiteUtil.checkPortletActive(browser, functionalHomeSiteUtil.getPortletNotesCss()));
		}
		
		/* end editing portal */
		functionalHomeSiteUtil.endEditingPortal(browser);
	}
	
	@Test
	@RunAsClient
	public void checkMovePortlet(){
		/* login for test setup */
		Assert.assertTrue(functionalUtil.login(browser));
		
		/* click configure */
		functionalHomeSiteUtil.beginEditingPortal(browser);
		
		/* move portlets */
		int oldPositionDyk[] = functionalHomeSiteUtil.findPortletPosition(browser, functionalHomeSiteUtil.getPortletDykCss(), portalColumnCount);
		
		if(functionalHomeSiteUtil.movePortlet(browser, functionalHomeSiteUtil.getPortletDykCss(), FunctionalHomeSiteUtil.Direction.UP)){
			browser.refresh();
			int newPosition[] = functionalHomeSiteUtil.findPortletPosition(browser, functionalHomeSiteUtil.getPortletDykCss(), portalColumnCount);
			
			Assert.assertEquals(oldPositionDyk[1], newPosition[1] + 1);
		}
		
		int oldPositionNoti[] = functionalHomeSiteUtil.findPortletPosition(browser, functionalHomeSiteUtil.getPortletNotiCss(), portalColumnCount);
		
		if(functionalHomeSiteUtil.movePortlet(browser, functionalHomeSiteUtil.getPortletNotiCss(), FunctionalHomeSiteUtil.Direction.LEFT)){
			int newPosition[] = functionalHomeSiteUtil.findPortletPosition(browser, functionalHomeSiteUtil.getPortletNotiCss(), portalColumnCount);

			Assert.assertEquals(oldPositionNoti[0] - 1, newPosition[0]);
		}
		
		/* end editing portal */
		functionalHomeSiteUtil.endEditingPortal(browser);
	}
	
	@Test
	@RunAsClient
	public void checkBrowserBack(){
		/* login for test setup */
		Assert.assertTrue(functionalUtil.login(browser));
		
		functionalHomeSiteUtil.enableBack(browser);
		
		/* check if settings were applied */
		Assert.assertTrue(functionalUtil.logout(browser));
		Assert.assertTrue(functionalUtil.login(browser));
		
		LinkedHashMap<String,String> pages = new LinkedHashMap<String,String>();
		
		/* locators as key to click and as value to test for */
		FunctionalLocatorPairsFactory pairsFactory = new FunctionalLocatorPairsFactory(functionalUtil);
		pages.put(pairsFactory.getLocatorOfSite(functionalUtil.getOlatSiteHomeCss()), pairsFactory.getApprovalOfSite(functionalUtil.getOlatSiteHomeCss()));
		pages.put(pairsFactory.getLocatorOfSite(functionalUtil.getOlatSiteAdministrationCss()), pairsFactory.getApprovalOfSite(functionalUtil.getOlatSiteAdministrationCss()));
		pages.put(pairsFactory.getLocatorOfSite(functionalUtil.getOlatSiteGroupAdministrationCss()), pairsFactory.getApprovalOfSite(functionalUtil.getOlatSiteGroupAdministrationCss()));
		pages.put(pairsFactory.getLocatorOfSite(functionalUtil.getOlatSiteGroupsCss()), pairsFactory.getApprovalOfSite(functionalUtil.getOlatSiteGroupsCss()));
		pages.put(pairsFactory.getLocatorOfSite(functionalUtil.getOlatSiteLearningResourcesCss()), pairsFactory.getApprovalOfSite(functionalUtil.getOlatSiteLearningResourcesCss()));
		pages.put(pairsFactory.getLocatorOfSite(functionalUtil.getOlatSiteUserManagementCss()), pairsFactory.getApprovalOfSite(functionalUtil.getOlatSiteUserManagementCss()));
		
		
		/* visit specified pages */
		String[] keys = pages.keySet().toArray(new String[0]);
		String[] values = (String[]) pages.values().toArray(new String[0]);
		
		int i;
		
		for(i = 0; i < pages.size(); i++){
			browser.click(keys[i]);
			
			browser.waitForPageToLoad(functionalUtil.getWaitLimit());
			functionalUtil.waitForPageToLoadElement(browser, values[i]);
		}
		
		/* test for the appropriate pages */
		i = pages.size() -1;
		
		Assert.assertTrue(browser.isElementPresent(values[i]));
		
		i--;
		
		for(; i >= 0; i--){
			browser.goBack();

			browser.waitForPageToLoad(functionalUtil.getWaitLimit());
			functionalUtil.waitForPageToLoadElement(browser, values[i]);
			
			Assert.assertTrue(browser.isElementPresent(values[i]));
		}
	}
	
	@Test
	@RunAsClient
	public void checkResetPassword() throws IOException, URISyntaxException{
		/* create test user via REST */
		List<UserVO> userVO = functionalVOUtil.createTestUsers(deploymentUrl, 1);
		
		/* login for test setup */
		Assert.assertTrue(functionalUtil.login(browser));
		
		/* password test */
		UserVO current = userVO.get(0);
		
		Assert.assertTrue(functionalUtil.logout(browser));
		Assert.assertTrue(functionalUtil.login(browser, current.getLogin(), current.getPassword(), true));
		
		functionalUtil.openSite(browser, FunctionalUtil.OlatSite.HOME);
		functionalHomeSiteUtil.openActionByMenuTree(browser, FunctionalHomeSiteUtil.HomeSiteAction.SETTINGS);
		
		String newPassword = ("passwd_" + 0 + "_" + UUID.randomUUID().toString()).substring(0, 24);
		
		functionalUtil.openContentTab(browser, SettingsTab.PASSWORD.ordinal());
		functionalUtil.typePassword(browser, functionalHomeSiteUtil.getOldPasswordCss(), current.getPassword());
		functionalUtil.typePassword(browser, functionalHomeSiteUtil.getNewPasswordCss(), newPassword);
		functionalUtil.typePassword(browser, functionalHomeSiteUtil.getConfirmPasswordCss(), newPassword);
		
		functionalUtil.saveForm(browser, 0);
		
		Assert.assertTrue(functionalUtil.logout(browser));
		Assert.assertTrue(functionalUtil.login(browser, current.getLogin(), newPassword, true));
	}
	
	
	@Test
	@RunAsClient
	public void checkSettings() throws IOException, URISyntaxException, InterruptedException{
		/* create test user via REST */
		List<UserVO> userVO = functionalVOUtil.createTestUsers(deploymentUrl, 1);
		
		/* login for test setup */
		Assert.assertTrue(functionalUtil.login(browser));
		
		/* reset settings */
		Assert.assertTrue(functionalHomeSiteUtil.resetSettings(browser));
		Assert.assertTrue(functionalUtil.login(browser));
		
		/* set language */
		functionalHomeSiteUtil.selectLanguage(browser, FunctionalHomeSiteUtil.GERMAN_LANGUAGE_VALUE);
		
		/* resume off */
		functionalHomeSiteUtil.disableResume(browser);
		
		/* logout */
		Assert.assertTrue(functionalUtil.logout(browser));
		
		/* login for test case */
		Assert.assertTrue(functionalUtil.login(browser));
		
		/* click configure */
		functionalHomeSiteUtil.beginEditingPortal(browser);
		
		/* de-/activate portlets */
		if(functionalHomeSiteUtil.deactivatePortlet(browser, functionalHomeSiteUtil.getPortletEffCss())){
			Assert.assertFalse(functionalHomeSiteUtil.checkPortletActive(browser, functionalHomeSiteUtil.getPortletEffCss()));
		}
		
		if(functionalHomeSiteUtil.activatePortlet(browser, functionalHomeSiteUtil.getPortletNotesCss())){
			Assert.assertTrue(functionalHomeSiteUtil.checkPortletActive(browser, functionalHomeSiteUtil.getPortletNotesCss()));
		}
		
		/* move portlets */
		int oldPositionDyk[] = functionalHomeSiteUtil.findPortletPosition(browser, functionalHomeSiteUtil.getPortletDykCss(), portalColumnCount);
		
		if(functionalHomeSiteUtil.movePortlet(browser, functionalHomeSiteUtil.getPortletDykCss(), FunctionalHomeSiteUtil.Direction.UP)){
			browser.refresh();
			int newPosition[] = functionalHomeSiteUtil.findPortletPosition(browser, functionalHomeSiteUtil.getPortletDykCss(), portalColumnCount);
			
			Assert.assertEquals(oldPositionDyk[1], newPosition[1] + 1);
		}
		
		int oldPositionNoti[] = functionalHomeSiteUtil.findPortletPosition(browser, functionalHomeSiteUtil.getPortletNotiCss(), portalColumnCount);
		
		if(functionalHomeSiteUtil.movePortlet(browser, functionalHomeSiteUtil.getPortletNotiCss(), FunctionalHomeSiteUtil.Direction.LEFT)){
			int newPosition[] = functionalHomeSiteUtil.findPortletPosition(browser, functionalHomeSiteUtil.getPortletNotiCss(), portalColumnCount);

			Assert.assertEquals(oldPositionNoti[0] - 1, newPosition[0]);
		}
		
		/* end editing portal */
		functionalHomeSiteUtil.endEditingPortal(browser);
		
		/* edit settings */
		functionalHomeSiteUtil.openActionByMenuTree(browser, FunctionalHomeSiteUtil.HomeSiteAction.SETTINGS);
		
		functionalUtil.openContentTab(browser, SettingsTab.SYSTEM.ordinal());
		
		functionalHomeSiteUtil.selectLanguage(browser, FunctionalHomeSiteUtil.ENGLISH_LANGUAGE_VALUE);
		
		functionalHomeSiteUtil.enableBack(browser);
		
		/* check if settings were applied */
		Assert.assertTrue(functionalUtil.logout(browser));
		Assert.assertTrue(functionalUtil.login(browser));
		
		LinkedHashMap<String,String> pages = new LinkedHashMap<String,String>();
		
		/* locators as key to click and as value to test for */
		FunctionalLocatorPairsFactory pairsFactory = new FunctionalLocatorPairsFactory(functionalUtil);
		pages.put(pairsFactory.getLocatorOfSite(functionalUtil.getOlatSiteHomeCss()), pairsFactory.getApprovalOfSite(functionalUtil.getOlatSiteHomeCss()));
		pages.put(pairsFactory.getLocatorOfSite(functionalUtil.getOlatSiteAdministrationCss()), pairsFactory.getApprovalOfSite(functionalUtil.getOlatSiteAdministrationCss()));
		pages.put(pairsFactory.getLocatorOfSite(functionalUtil.getOlatSiteGroupAdministrationCss()), pairsFactory.getApprovalOfSite(functionalUtil.getOlatSiteGroupAdministrationCss()));
		pages.put(pairsFactory.getLocatorOfSite(functionalUtil.getOlatSiteGroupsCss()), pairsFactory.getApprovalOfSite(functionalUtil.getOlatSiteGroupsCss()));
		pages.put(pairsFactory.getLocatorOfSite(functionalUtil.getOlatSiteLearningResourcesCss()), pairsFactory.getApprovalOfSite(functionalUtil.getOlatSiteLearningResourcesCss()));
		pages.put(pairsFactory.getLocatorOfSite(functionalUtil.getOlatSiteUserManagementCss()), pairsFactory.getApprovalOfSite(functionalUtil.getOlatSiteUserManagementCss()));
		
		
		/* visit specified pages */
		String[] keys = pages.keySet().toArray(new String[0]);
		String[] values = (String[]) pages.values().toArray(new String[0]);
		
		int i;
		
		for(i = 0; i < pages.size(); i++){
			browser.click(keys[i]);
			
			browser.waitForPageToLoad(functionalUtil.getWaitLimit());
			functionalUtil.waitForPageToLoadElement(browser, values[i]);
		}
		
		/* test for the appropriate pages */
		i = pages.size() -1;
		
		Assert.assertTrue(browser.isElementPresent(values[i]));
		
		i--;
		
		for(; i >= 0; i--){
			browser.goBack();

			browser.waitForPageToLoad(functionalUtil.getWaitLimit());
			functionalUtil.waitForPageToLoadElement(browser, values[i]);
			
			Assert.assertTrue(browser.isElementPresent(values[i]));
		}
		
		/* password test */
		UserVO current = userVO.get(0);
		
		Assert.assertTrue(functionalUtil.logout(browser));
		Assert.assertTrue(functionalUtil.login(browser, current.getLogin(), current.getPassword(), true));
		
		functionalUtil.openSite(browser, FunctionalUtil.OlatSite.HOME);
		functionalHomeSiteUtil.openActionByMenuTree(browser, FunctionalHomeSiteUtil.HomeSiteAction.SETTINGS);
		
		String newPassword = ("passwd_" + 0 + "_" + UUID.randomUUID().toString()).substring(0, 24);
		
		functionalUtil.openContentTab(browser, SettingsTab.PASSWORD.ordinal());
		functionalUtil.typePassword(browser, functionalHomeSiteUtil.getOldPasswordCss(), current.getPassword());
		functionalUtil.typePassword(browser, functionalHomeSiteUtil.getNewPasswordCss(), newPassword);
		functionalUtil.typePassword(browser, functionalHomeSiteUtil.getConfirmPasswordCss(), newPassword);
		
		functionalUtil.saveForm(browser, 0);
		
		Assert.assertTrue(functionalUtil.logout(browser));
		Assert.assertTrue(functionalUtil.login(browser, current.getLogin(), newPassword, true));
	}
}
