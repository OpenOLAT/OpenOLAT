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

	@Before
	public void setup(){
		functionalUtil = new FunctionalUtil();
		functionalUtil.setDeploymentUrl(deploymentUrl.toString());
		
		functionalVOUtil = new FunctionalVOUtil(functionalUtil.getUsername(), functionalUtil.getPassword());
		functionalHomeSiteUtil = new FunctionalHomeSiteUtil(functionalUtil);
	}
	
	@Test
	@RunAsClient
	public void checkSettings() throws IOException, URISyntaxException, InterruptedException{
		/* create test user via REST */
		List<UserVO> userVO = functionalVOUtil.createTestUsers(deploymentUrl, 1);
		
		/* login for test setup */
		Assert.assertTrue(functionalUtil.login(browser));
		Thread.sleep(5000);
		
		/* reset settings */
		Assert.assertTrue(functionalHomeSiteUtil.resetSettings(browser));
		Thread.sleep(5000);
		
		/* set language */
		functionalHomeSiteUtil.selectLanguage(browser, FunctionalHomeSiteUtil.GERMAN_LANGUAGE_VALUE);
		Thread.sleep(10000);
		
		/* resume off */
		functionalHomeSiteUtil.disableResume(browser);
		
		/* logout */
		Assert.assertTrue(functionalUtil.logout(browser));
		Thread.sleep(5000);
		
		/* login for test case */
		Assert.assertTrue(functionalUtil.login(browser));
		
		/* click configure */
		functionalHomeSiteUtil.beginEditingPortal(browser);
		Thread.sleep(5000);
		
		/* de-/activate portlets */
		functionalHomeSiteUtil.deactivatePortlet(browser, functionalHomeSiteUtil.getPortletEffCss());
		functionalHomeSiteUtil.activatePortlet(browser, functionalHomeSiteUtil.getPortletNotesCss());
		
		functionalHomeSiteUtil.movePortlet(browser, functionalHomeSiteUtil.getPortletDykCss(), FunctionalHomeSiteUtil.Direction.UP);
		functionalHomeSiteUtil.movePortlet(browser, functionalHomeSiteUtil.getPortletNotiCss(), FunctionalHomeSiteUtil.Direction.LEFT);
		
		functionalHomeSiteUtil.endEditingPortal(browser);
		
		//TODO:JK: do something fancy to test the result
		
		/* edit settings */
		functionalHomeSiteUtil.openActionByMenuTree(browser, FunctionalHomeSiteUtil.HomeSiteAction.SETTINGS);
		
		functionalUtil.openContentTab(browser, SettingsTab.SYSTEM.ordinal());
		
		functionalHomeSiteUtil.selectLanguage(browser, FunctionalHomeSiteUtil.ENGLISH_LANGUAGE_VALUE);
		
		functionalHomeSiteUtil.enableBack(browser);
		
		/* check if settings were applied */
		Assert.assertTrue(functionalUtil.logout(browser));
		Assert.assertTrue(functionalUtil.login(browser));
		
		HashMap<String,String> pages = new HashMap<String,String>();
		
		//TODO:JK: add locators as key to click and as value to test for.
		//pages.put();
		
		
		/* visit specified pages */
		Iterator iter = pages.keySet().iterator();
		
		while(iter.hasNext()){
			Map.Entry pairs = (Map.Entry) iter.next();
			
			browser.click((String) pairs.getKey());
		}
		
		/* test for the appropriate pages */
		String[] keys = (String[]) pages.entrySet().toArray();
		
		for(int i = pages.size() -1; i >= 0; i--){
			browser.goBack();
			Assert.assertTrue(browser.isElementPresent(pages.get(keys[i])));
		}
		
		/* password test */
		UserVO current = userVO.get(0);
		
		Assert.assertTrue(functionalUtil.logout(browser));
		Assert.assertTrue(functionalUtil.login(browser, current.getLogin(), current.getPassword(), true));
		
		functionalUtil.openSite(browser, FunctionalUtil.OlatSite.HOME);
		functionalHomeSiteUtil.openActionByMenuTree(browser, FunctionalHomeSiteUtil.HomeSiteAction.SETTINGS);
		
		String newPassword = "passwd_" + 0 + "_" + UUID.randomUUID().toString();
		
		functionalUtil.openContentTab(browser, SettingsTab.PASSWORD.ordinal());
		functionalUtil.typePassword(browser, functionalHomeSiteUtil.getOldPasswordCss(), functionalUtil.getPassword());
		functionalUtil.typePassword(browser, functionalHomeSiteUtil.getNewPasswordCss(), newPassword);
		functionalUtil.typePassword(browser, functionalHomeSiteUtil.getConfirmPasswordCss(), newPassword);
		
		functionalUtil.saveForm(browser, 0);
		
		Assert.assertTrue(functionalUtil.logout(browser));
		Assert.assertTrue(functionalUtil.login(browser, current.getLogin(), newPassword, true));
		
		
		functionalHomeSiteUtil.endEditingPortal(browser);
	}
}
