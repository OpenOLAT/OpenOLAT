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
package org.olat.commons.calendar.ui;

import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.id.Identity;

/**
 * Needed for the personal configuratio.
 * 
 * 
 * Initial date: 26.08.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CalendarPersonalConfigurationRow {
	
	private final KalendarRenderWrapper wrapper;
	
	private FormLink colorLink;
	private FormLink visibleLink;
	private FormLink aggregatedLink;
	private FormLink feedLink;
	private FormLink toolsLink;
	
	public CalendarPersonalConfigurationRow(KalendarRenderWrapper wrapper) {
		this.wrapper = wrapper;
	}
	
	public KalendarRenderWrapper getWrapper() {
		return wrapper;
	}

	public int getAccess() {
		return wrapper.getAccess();
	}
	
	public String getCalendarType() {
		return wrapper.getKalendar().getType();
	}
	
	public String getCalendarId() {
		return wrapper.getKalendar().getCalendarID();
	}

	public String getDisplayName() {
		return wrapper.getDisplayName();
	}
	
	public String getIdentifier() {
		return wrapper.getIdentifier();
	}

	public String getCssClass() {
		return wrapper.getCssClass();
	}
	
	public boolean isVisible() {
		return wrapper.isVisible();
	}
	
	public boolean isAggregated() {
		return wrapper.isInAggregatedFeed();
	}
	
	public boolean isImported() {
		return wrapper.isImported();
	}

	public String getToken() {
		return wrapper.getToken();
	}

	public String getFeedUrl(Identity identity) {
		return wrapper.getFeedUrl(identity);
	}

	public FormLink getColorLink() {
		return colorLink;
	}

	public void setColorLink(FormLink colorLink) {
		this.colorLink = colorLink;
	}

	public FormLink getVisibleLink() {
		return visibleLink;
	}

	public void setVisibleLink(FormLink visibleLink) {
		this.visibleLink = visibleLink;
	}

	public FormLink getAggregatedLink() {
		return aggregatedLink;
	}

	public void setAggregatedLink(FormLink aggregatedLink) {
		this.aggregatedLink = aggregatedLink;
	}

	public FormLink getFeedLink() {
		return feedLink;
	}

	public void setFeedLink(FormLink feedLink) {
		this.feedLink = feedLink;
	}

	public FormLink getToolsLink() {
		return toolsLink;
	}

	public void setToolsLink(FormLink toolsLink) {
		this.toolsLink = toolsLink;
	}
}
