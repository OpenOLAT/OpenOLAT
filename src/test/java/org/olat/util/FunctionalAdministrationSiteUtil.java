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

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.util.FunctionalUtil.OlatSite;

import com.thoughtworks.selenium.Selenium;

/**
 * 
 * @author jkraehemann, joel.kraehemann@frentix.com, frentix.com
 */
public class FunctionalAdministrationSiteUtil {
	private final static OLog log = Tracing.createLoggerFor(FunctionalAdministrationSiteUtil.class);
	
	public enum AdministrationSiteAction {
		INFORMATION("o_sel_sysinfo"),
		CONFIGURATION("o_sel_sysconfig"),
		MAINTENANCE("o_sel_sysadmin"),
		CUSTOMIZATION("o_sel_customizing"),
		ADVANCED_PROPERTIES("o_sel_advancedproperties"),
		BOOKINGS("o_sel_booking");
		
		private String actionCss;
		
		AdministrationSiteAction(String actionCss){
			setActionCss(actionCss);
		}

		public String getActionCss() {
			return actionCss;
		}

		public void setActionCss(String actionCss) {
			this.actionCss = actionCss;
		}
	}
	
	public enum SystemConfigurationAction {
		LAYOUT("o_sel_layout"),
		LANGUAGES("o_sel_i18n"),
		QUOTA("o_sel_quota"),
		VERSIONIG("o_sel_versioning"),
		PORTFOLIO("o_sel_portfolio"),
		RESAPI("o_sel_restapi"),
		EXTENSIONS("o_sel_extensions"),
		EMAIL("o_sel_mail"),
		VITERO("o_sel_vitero"),
		SETUP("o_sel_properties"),
		SELF_REGISTRATION("o_sel_selfregistration"),
		PAYPAL("o_sel_paypal"),
		GROUP("o_sel_group");
		
		private String actionCss;
		
		SystemConfigurationAction(String actionCss){
			setActionCss(actionCss);
		}

		public String getActionCss() {
			return actionCss;
		}

		public void setActionCss(String actionCss) {
			this.actionCss = actionCss;
		}
	}
	
	public enum SystemMaintenanceAction {
		STATISTICS("o_sel_statistics"),
		SEARCH("o_sel_search"),
		NOTIFICATIONS("o_sel_notifications"),
		PASSWORDS("o_sel_userbulkchangepw");
		
		private String actionCss;
		
		SystemMaintenanceAction(String actionCss){
			setActionCss(actionCss);
		}

		public String getActionCss() {
			return actionCss;
		}

		public void setActionCss(String actionCss) {
			this.actionCss = actionCss;
		}
	}
	
	public enum CustomizingAction {
		TRANSLATION("o_sel_translation"),
		REGISTRATION("o_sel_registration");
		
		private String actionCss;
		
		CustomizingAction(String actionCss){
			setActionCss(actionCss);
		}

		public String getActionCss() {
			return actionCss;
		}

		public void setActionCss(String actionCss) {
			this.actionCss = actionCss;
		}
	}
	
	public enum SystemInformationTabs {
		SESSIONS,
		INFOMSG,
		ERRORS,
		LOGLEVELS,
		SYSINFO,
		SNOOP,
		REQUESTLOGLEVEL,
		USERSESSIONS,
		LOCKS,
		HIBERNATE,
		CACHES,
		BUILDINFO;
	}
	
	private FunctionalUtil functionalUtil;
	
	public FunctionalAdministrationSiteUtil(FunctionalUtil functionalUtil){
		this.functionalUtil = functionalUtil;
	}
	
	/**
	 * Browse the administrations site's navigation.
	 * 
	 * @param browser
	 * @param action
	 * @return true on success otherwise false
	 */
	public boolean openActionByMenuTree(Selenium browser, Object action){
		functionalUtil.idle(browser);
		
		StringBuffer selectorBuffer;
		
		if(action instanceof AdministrationSiteAction){
			 selectorBuffer = new StringBuffer();
			
			selectorBuffer.append("xpath=//li[contains(@class, '")
			.append(((AdministrationSiteAction) action).getActionCss())
			.append("')]//a[contains(@class, '")
			.append(functionalUtil.getTreeLevel1Css())
			.append("')]");
		}else if(action instanceof SystemConfigurationAction){
			/* check if not collapsed */
			 selectorBuffer = new StringBuffer();
			
			 selectorBuffer.append("xpath=//li[contains(@class, '")
			 .append(AdministrationSiteAction.CONFIGURATION.getActionCss())
			 .append("')]//a[contains(@class, '")
			 .append(functionalUtil.getTreeLevelOpenCss())
			 .append("')]");
			 
			if(browser.isElementPresent(selectorBuffer.toString())){
				browser.click(selectorBuffer.toString());
				functionalUtil.idle(browser);
			}

			/* click */
			selectorBuffer = new StringBuffer();
			
			selectorBuffer.append("xpath=//li[contains(@class, '")
			.append(((SystemConfigurationAction) action).getActionCss())
			.append("')]//a[contains(@class, '")
			.append(functionalUtil.getTreeLevel2Css())
			.append("')]");
		}else if(action instanceof SystemMaintenanceAction){
			/* check if not collapsed */
			 selectorBuffer = new StringBuffer();
			
			 selectorBuffer.append("xpath=//li[contains(@class, '")
			 .append(AdministrationSiteAction.MAINTENANCE.getActionCss())
			 .append("')]//a[contains(@class, '")
			 .append(functionalUtil.getTreeLevelOpenCss())
			 .append("')]");
			 
			if(browser.isElementPresent(selectorBuffer.toString())){
				browser.click(selectorBuffer.toString());
				functionalUtil.idle(browser);
			}

			/* click */
			selectorBuffer.append("xpath=//li[contains(@class, '")
			.append(((SystemMaintenanceAction) action).getActionCss())
			.append("')]//a[contains(@class, '")
			.append(functionalUtil.getTreeLevel2Css())
			.append("')]");
		}else if(action instanceof CustomizingAction){
			/* check if not collapsed */
			 selectorBuffer = new StringBuffer();
			
			 selectorBuffer.append("xpath=//li[contains(@class, '")
			 .append(AdministrationSiteAction.CUSTOMIZATION.getActionCss())
			 .append("')]//a[contains(@class, '")
			 .append(functionalUtil.getTreeLevelOpenCss())
			 .append("')]");
			 
			if(browser.isElementPresent(selectorBuffer.toString())){
				browser.click(selectorBuffer.toString());
				functionalUtil.idle(browser);
			}

			/* click */
			selectorBuffer.append("xpath=//li[contains(@class, '")
			.append(((CustomizingAction) action).getActionCss())
			.append("')]//a[contains(@class, '")
			.append(functionalUtil.getTreeLevel2Css())
			.append("')]");
		}else{
			return(false);
		}
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		
		return(true);
	}
	
	/**
	 * Clears the specified cache which matches to keys. 
	 * 
	 * @param browser
	 * @param keys
	 * @return
	 */
	public boolean clearCache(Selenium browser, String[] keys){
		if(!functionalUtil.openSite(browser, OlatSite.ADMINISTRATION)){
			return(false);
		}
		
		if(!openActionByMenuTree(browser, AdministrationSiteAction.INFORMATION)){
			return(false);
		}
		
		if(!functionalUtil.openContentTab(browser, SystemInformationTabs.CACHES.ordinal())){
			return(false);
		}
		
		functionalUtil.idle(browser);
		
		/* click show all*/
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//div[contains(@class, '")
		.append(functionalUtil.getTableAllCss())
		.append("')]//a");
		
		if(browser.isElementPresent(selectorBuffer.toString())){
			browser.click(selectorBuffer.toString());
		}
		
		/* clear appropriate cache */
		for(String currentKey: keys){
			functionalUtil.idle(browser);
			
			/* click clear */
			selectorBuffer = new StringBuffer();
			
			selectorBuffer.append("//table//tr//td[text()='")
			.append(currentKey)
			.append("']");
			
			functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
			selectorBuffer.append("/../td[last()]//a");
				
			browser.click(selectorBuffer.toString());
			
			/* confirm */
			selectorBuffer = new StringBuffer();
			
			selectorBuffer.append("xpath=(//div[contains(@class, '")
			.append(functionalUtil.getWindowCss())
			.append("')]//a[contains(@class, '")
			.append(functionalUtil.getButtonCss())
			.append("')])[last()]");
			
			functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
			browser.click(selectorBuffer.toString());
		}
		
		return(true);
	}

	public FunctionalUtil getFunctionalUtil() {
		return functionalUtil;
	}

	public void setFunctionalUtil(FunctionalUtil functionalUtil) {
		this.functionalUtil = functionalUtil;
	}
}
