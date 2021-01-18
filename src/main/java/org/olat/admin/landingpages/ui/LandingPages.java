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
package org.olat.admin.landingpages.ui;

/**
 * 
 * Initial date: 15.05.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum LandingPages {
	
	myCourses("page.myCourses", "/MyCoursesSite/0"),
	myFavoritCourses("page.myFavoritCourses", "/MyCoursesSite/0/Favorits/0"),
	coursesCatalog("page.coursesCatalog", "/MyCoursesSite/0/Catalog/0"),
	myGroups("page.groups", "/GroupsSite/0/MyGroups/0"),
	myFavoritGroups("page.myFavoritGroups", "/GroupsSite/0/MyGroups/0/Favorits/0"),
	myNotifications("page.myNotifications", "/HomeSite/0/notifications/0"),
	myPortfolios("page.myPortfolio", "/HomeSite/0/Portfolio/0"),
	infoPage1("page.infoPage1", "/CourseSite/1"),
	infoPage2("page.infoPage2", "/CourseSite/2"),
	learnResources("page.learnResources", "/RepositorySite/0"),
	catalog("page.catalog", "/CatalogEntry/0"),
	portal("page.portal", "/Portal/0");
	
	private String i18nKey;
	private String businessPath;
	
	private LandingPages(String i18nKey, String businessPath) {
		this.i18nKey = i18nKey;
		this.businessPath = businessPath;
	}
	
	public String i18nKey() {
		return i18nKey;
	}
	
	public String businessPath() {
		return businessPath;
	}
	
	public static final LandingPages landingPageFromCmd(String cmd) {
		for(LandingPages lp:values()) {
			if(cmd.equals(lp.name())) {
				return lp;
			}
		}
		return null;
	}
}
