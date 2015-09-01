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
package org.olat.upgrade.model;

import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;

/**
 * Used to upgrade de GUi preferences of calendars
 * 
 * Initial date: 25.08.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class KalendarConfig {

	private String css;
	private boolean vis;
	private Long resId;

	public KalendarConfig() {
		this(KalendarRenderWrapper.CALENDAR_COLOR_BLUE, true);
	}

	public KalendarConfig(String calendarCSS, boolean visible) {

		this.css = calendarCSS;
		this.vis = visible;
		this.resId = null;
	}

	public Long getResId() {
		return this.resId;
	}

	public void setResId(Long resId) {
		this.resId = resId;
	}

	public String getCss() {
		return css;
	}

	public void setCss(String css) {
		this.css = css;
	}

	public boolean isVis() {
		return vis;
	}

	public void setVis(boolean vis) {
		this.vis = vis;
	}
}
