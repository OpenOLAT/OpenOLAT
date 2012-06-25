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

/**
 * 
 * @author jkraehemann, joel.kraehemann@frentix.com, frentix.com
 */
public class FunctionalHomeSiteUtil {
	public enum HomePage {
		HOME,
		SETTINGS,
		CALENDAR,
		SUBSCRIPTIONS,
		BOOKMARKS,
		PERSONAL_FOLDER,
		NOTES,
		EVIDENCES_OF_ACHIEVEMENT,
		OTHER_USERS,
	};
	
	public enum EPortfolioPage {
		MY_ARTIFACTS,
		MY_BINDERS,
		MY_PORTFOLIO_TASKS,
		RELEASED_BINDERS,
	};
	
	private FunctionalUtil functionalUtil;
	
	public FunctionalHomeSiteUtil(FunctionalUtil functionalUtil){
		setUtil(functionalUtil);
	}
	
	public String findCssClassOfPage(Object page){
		if(page == null)
			return(null);
		
		String selectedCss = null;

		if(page instanceof HomePage){
			//TODO:JK: implement me
		}else if(page instanceof EPortfolioPage){
			//TODO:JK: implement me
		}
		
		return(selectedCss);
	}
	
	public boolean openPageByNavigation(Object page){
		//TODO:JK: implement me
		return(false);
	}

	public FunctionalUtil getUtil() {
		return functionalUtil;
	}

	public void setUtil(FunctionalUtil functionalUtil) {
		this.functionalUtil = functionalUtil;
	}
}
