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
package org.olat.core.gui;

import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 07.05.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class WindowSettings {
	
	private boolean hideHeader = false;
	private boolean hideNavigation = false;
	private boolean hideFooter = false;
	private boolean hideColumn1 = false;
	private boolean hideColumn2 = false;
	
	public WindowSettings() {
		//
	}

	public static WindowSettings parse(String settings) {
		WindowSettings wSettings = new WindowSettings();
		if(StringHelper.containsNonWhitespace(settings)) {
			wSettings.hideHeader = settings.indexOf('h') >= 0;
			wSettings.hideNavigation = settings.indexOf('n') >= 0;
			wSettings.hideFooter = settings.indexOf('f') >= 0;
			wSettings.hideColumn1 = settings.indexOf('1') >= 0;
			wSettings.hideColumn2 = settings.indexOf('2') >= 0;
		}
		return wSettings;
	}
	
	public boolean isHideHeader() {
		return hideHeader;
	}
	
	public void setHideHeader(boolean hideHeader) {
		this.hideHeader = hideHeader;
	}
	
	public boolean isHideNavigation() {
		return hideNavigation;
	}
	
	public void setHideNavigation(boolean hideNavigation) {
		this.hideNavigation = hideNavigation;
	}
	
	public boolean isHideFooter() {
		return hideFooter;
	}
	
	public void setHideFooter(boolean hideFooter) {
		this.hideFooter = hideFooter;
	}
	
	public boolean isHideColumn1() {
		return hideColumn1;
	}
	
	public void setHideColumn1(boolean hideColumn1) {
		this.hideColumn1 = hideColumn1;
	}
	
	public boolean isHideColumn2() {
		return hideColumn2;
	}
	
	public void setHideColumn2(boolean hideColumn2) {
		this.hideColumn2 = hideColumn2;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if(hideHeader) sb.append('h');
		if(hideNavigation) sb.append('n');
		if(hideFooter) sb.append('f');
		if(hideColumn1) sb.append('1');
		if(hideColumn2) sb.append('2');
		return sb.toString();
	}
}
