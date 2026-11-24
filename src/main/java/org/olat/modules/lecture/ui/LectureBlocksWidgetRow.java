/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.lecture.ui;

import org.olat.core.gui.components.form.flexible.elements.FormLink;

/**
 * 
 * Initial date: Jan 8, 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class LectureBlocksWidgetRow {

	private Long key;
	private String id;
	private String statusText;
	private String statusCss;
	private String dayAbbr;
	private String day;
	private String title;
	private String location;
	private String rooms;
	private boolean onlineMeeting;
	private String time;
	private FormLink titleLink;
	private FormLink rollCallLink;
	
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getStatusText() {
		return statusText;
	}

	public void setStatusText(String statusText) {
		this.statusText = statusText;
	}

	public String getStatusCss() {
		return statusCss;
	}
	
	public void setStatusCss(String statusCss) {
		this.statusCss = statusCss;
	}
	
	public String getDayAbbr() {
		return dayAbbr;
	}

	public void setDayAbbr(String dayAbbr) {
		this.dayAbbr = dayAbbr;
	}

	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}

	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getLocation() {
		return location;
	}
	
	public void setLocation(String location) {
		this.location = location;
	}
	
	public String getRooms() {
		return rooms;
	}

	public void setRooms(String rooms) {
		this.rooms = rooms;
	}

	public boolean isOnlineMeeting() {
		return onlineMeeting;
	}

	public void setOnlineMeeting(boolean onlineMeeting) {
		this.onlineMeeting = onlineMeeting;
	}

	public String getTime() {
		return time;
	}
	
	public void setTime(String time) {
		this.time = time;
	}

	public FormLink getTitleLink() {
		return titleLink;
	}

	public void setTitleLink(FormLink titleLink) {
		this.titleLink = titleLink;
	}

	public String getTitleName() {
		return titleLink != null? titleLink.getComponent().getComponentName(): null;
	}
	
	public FormLink getRollCallLink() {
		return rollCallLink;
	}
	
	public String getRollCallName() {
		return rollCallLink != null? rollCallLink.getComponent().getComponentName(): null;
	}
	
	public void setRollCallLink(FormLink rollCallLink) {
		this.rollCallLink = rollCallLink;
	}
	
}
