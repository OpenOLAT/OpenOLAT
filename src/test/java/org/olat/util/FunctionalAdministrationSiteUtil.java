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

import org.olat.util.FunctionalUtil.OlatSite;
import org.olat.util.xss.XssUtil;

import com.thoughtworks.selenium.Selenium;

/**
 * 
 * @author jkraehemann, joel.kraehemann@frentix.com, frentix.com
 */
@XssUtil
public class FunctionalAdministrationSiteUtil {
	
	public enum AdministrationSiteAction {
		SYSTEM("o_sel_system"),
		CORE_FUNCTIONS("o_sel_sysconfig"),
		MODULES("o_sel_modules"),
		CUSTOMIZATION("o_sel_customizing"),
		DEVELOPMENT("o_sel_devel");
		
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
	
	public enum SystemAction {
		SYSTEM_INFO("o_sel_sysinfo"),
		SESSIONS("o_sel_sessions"),
		ERRORS("o_sel_errors"),
		CACHES("o_sel_caches"),
		LOCKS("o_sel_locks"),
		HIBERNATE("o_sel_hibernate"),
		JAVAVM("o_sel_javavm");
		
		private String actionCss;
		
		SystemAction(String actionCss){
			setActionCss(actionCss);
		}

		public String getActionCss() {
			return actionCss;
		}

		public void setActionCss(String actionCss) {
			this.actionCss = actionCss;
		}
	}
	
	public enum CoreFunctionsAction {
		LANGUAGES("o_sel_i18n"),
		QUOTA("o_sel_quota"),
		VERSIONIG("o_sel_versioning"),
		RESTAPI("o_sel_restapi"),
		EMAIL("o_sel_mail"),
		SELF_REGISTRATION("o_sel_selfregistration"),
		STATISTICS("o_sel_statistics"),
		SEARCH("o_sel_search"),
		NOTIFICATIONS("o_sel_notifications"),
		PASSWORDS("o_sel_userbulkchangepw");
		
		private String actionCss;
		
		CoreFunctionsAction(String actionCss){
			setActionCss(actionCss);
		}

		public String getActionCss() {
			return actionCss;
		}

		public void setActionCss(String actionCss) {
			this.actionCss = actionCss;
		}
	}
	
	public enum ModulesAction {
		PORTFOLIO("o_sel_portfolio"),
		VITERO("o_sel_vitero"),
		OPENMEETINGS("o_sel_openmeetings"),
		PAYPAL("o_sel_paypal"),
		BOOKING("o_sel_booking"),
		GROUP("o_sel_group"),
		CATALOG("o_sel_catalog");
		
		private String actionCss;
		
		ModulesAction(String actionCss){
			setActionCss(actionCss);
		}

		public String getActionCss() {
			return actionCss;
		}

		public void setActionCss(String actionCss) {
			this.actionCss = actionCss;
		}
	}
	
	public enum CustomizationAction {
		LAYOUT("o_sel_layout"),
		TRANSLATION("o_sel_translation"),
		REGISTRATION("o_sel_registration");
		
		private String actionCss;
		
		CustomizationAction(String actionCss){
			setActionCss(actionCss);
		}

		public String getActionCss() {
			return actionCss;
		}

		public void setActionCss(String actionCss) {
			this.actionCss = actionCss;
		}
	}
	
	public enum DevelopmentAction {
		EXTENSIONS("o_sel_extensions"),
		PROPERTIES("o_sel_properties"),
		ADVANCED_PROPERTIES("o_sel_advancedproperties"),
		SNOOP("o_sel_snoop");
		
		private String actionCss;
		
		DevelopmentAction(String actionCss){
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
			.append(functionalUtil.getTreeLevel0Css())
			.append("')]");
		}else{
			String actionL1Css = null;
			String actionL2Css = null;
			
			if(action instanceof SystemAction){
				actionL1Css = AdministrationSiteAction.SYSTEM.getActionCss();
				actionL2Css = ((SystemAction) action).getActionCss();
			}else if(action instanceof CoreFunctionsAction){
				actionL1Css = AdministrationSiteAction.CORE_FUNCTIONS.getActionCss();
				actionL2Css = ((CoreFunctionsAction) action).getActionCss();
			}else if(action instanceof ModulesAction){
				actionL1Css = AdministrationSiteAction.MODULES.getActionCss();
				actionL2Css = ((ModulesAction) action).getActionCss();
			}else if(action instanceof CustomizationAction){
				actionL1Css = AdministrationSiteAction.CUSTOMIZATION.getActionCss();
				actionL2Css = ((SystemAction) action).getActionCss();
			}else if(action instanceof DevelopmentAction){
				actionL1Css = AdministrationSiteAction.DEVELOPMENT.getActionCss();
				actionL2Css = ((DevelopmentAction) action).getActionCss();
			}else{
				return(false);
			}
			
			/* check if not collapsed */
			selectorBuffer = new StringBuffer();

			selectorBuffer.append("xpath=//li[contains(@class, '")
			.append(actionL1Css)
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
			.append(actionL2Css)
			.append("')]//a[contains(@class, '")
			.append(functionalUtil.getTreeLevel1Css())
			.append("')]");
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
		
		if(!openActionByMenuTree(browser, AdministrationSiteAction.SYSTEM)){
			return(false);
		}
		
		if(!openActionByMenuTree(browser, SystemAction.CACHES)){
			return(false);
		}
		
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
