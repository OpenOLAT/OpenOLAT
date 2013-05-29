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


/**
 * 
 * Initial date: 07.05.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class WindowSettings {
	
	private final boolean hideHeader;
	private final boolean hideNavigation;
	private final boolean hideFooter;
	private final boolean hideColumn1;
	private final boolean hideColumn2;

	public WindowSettings() {
		this(false, false, false, false, false);
	}
	
	public WindowSettings(boolean hideHeader, boolean hideNavigation, boolean hideFooter, boolean hideColumn1, boolean hideColumn2) {
		this.hideHeader = hideHeader;
		this.hideNavigation = hideNavigation;
		this.hideFooter = hideFooter;
		this.hideColumn1 = hideColumn1;
		this.hideColumn2 = hideColumn2;
	}

	public static WindowSettings parse(String settings) {
		WindowSettings wSettings;
		if(settings != null) {
			boolean hideHeader = settings.indexOf('h') >= 0;
			boolean hideNavigation = settings.indexOf('n') >= 0;
			boolean hideFooter = settings.indexOf('f') >= 0;
			boolean hideColumn1 = settings.indexOf('1') >= 0;
			boolean hideColumn2 = settings.indexOf('2') >= 0;
			wSettings = new WindowSettings(hideHeader, hideNavigation, hideFooter, hideColumn1, hideColumn2);
		} else {
			wSettings = new WindowSettings();
		}
		return wSettings;
	}
	
	public boolean isHideHeader() {
		return hideHeader;
	}
	
	public boolean isHideNavigation() {
		return hideNavigation;
	}
	
	public boolean isHideFooter() {
		return hideFooter;
	}
	
	public boolean isHideColumn1() {
		return hideColumn1;
	}
	
	public boolean isHideColumn2() {
		return hideColumn2;
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

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof WindowSettings) {
			WindowSettings settings = (WindowSettings)obj;
			return settings.hideHeader == hideHeader && settings.hideNavigation == hideNavigation
					&& settings.hideFooter == hideFooter && settings.hideColumn1 == hideColumn1
					&& settings.hideColumn2 == hideColumn2;
		}
		return false;
	}
}