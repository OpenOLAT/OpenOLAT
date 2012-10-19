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
import org.olat.util.FunctionalAdministrationSiteUtil.AdministrationSiteAction;
import org.olat.util.FunctionalUtil.OlatSite;

import com.thoughtworks.selenium.Selenium;

/**
 * 
 * @author jkraehemann, joel.kraehemann@frentix.com, frentix.com
 */
public class FunctionalGroupsSiteUtil {
	private final static OLog log = Tracing.createLoggerFor(FunctionalGroupsSiteUtil.class);
	
	public enum GroupsSiteAction {
		MY_GROUPS("o_sel_MyGroups"),
		PUBLISHED_GROUPS("o_sel_OpenGroups"),
		GROUPS_ADMINISTRATION("o_sel_AdminGroups");
		
		private String actionCss;
		
		GroupsSiteAction(String actionCss){
			setActionCss(actionCss);
		}

		public String getActionCss() {
			return actionCss;
		}

		public void setActionCss(String actionCss) {
			this.actionCss = actionCss;
		}
	}
	
	public enum MyGroupsTabs {
		BOOKMARK,
		ALL_GROUPS,
		COACH,
		SEARCH,
	}
	
	public enum GroupOptions {
		WAITING_LIST,
		AUTO_MOVING_UP,
	}
	
	private FunctionalUtil functionalUtil;
	
	public FunctionalGroupsSiteUtil(FunctionalUtil functionalUtil){
		this.functionalUtil = functionalUtil;
	}
	
	/**
	 * Browse the groups site's navigation.
	 * 
	 * @param browser
	 * @param action
	 * @return true on success otherwise false
	 */
	public boolean openActionByMenuTree(Selenium browser, Object action){
		functionalUtil.idle(browser);
		
		StringBuffer selectorBuffer;
		
		if(action instanceof GroupsSiteAction){
			selectorBuffer = new StringBuffer();
			
			selectorBuffer.append("xpath=//li[contains(@class, '")
			.append(((GroupsSiteAction) action).getActionCss())
			.append("')]//a[contains(@class, '")
			.append(functionalUtil.getTreeLevel1Css())
			.append("')]");;
		}else{
			return(false);
		}

		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		
		return(true);
	}

	public boolean createGroup(Selenium browser, String groupName, String groupDescription, int maxParticipants, GroupOptions[] options){
		if(!functionalUtil.openSite(browser, OlatSite.GROUPS)){
			return(false);
		}
		
		if(!openActionByMenuTree(browser, GroupsSiteAction.MY_GROUPS)){
			return(false);
		}
		
		if(!functionalUtil.openContentSegment(browser, MyGroupsTabs.ALL_GROUPS.ordinal())){
			return(false);
		}
		
		functionalUtil.idle(browser);
		
		/* click create */
		
		
		/* fill in group name */
		
		
		/* fill in group description */
		
		
		/* fill in max participants */
		
		
		/* set options */
		
		
		/* click finish */
		
		
		return(true);
	}
	
	public FunctionalUtil getFunctionalUtil() {
		return functionalUtil;
	}

	public void setFunctionalUtil(FunctionalUtil functionalUtil) {
		this.functionalUtil = functionalUtil;
	}
}
