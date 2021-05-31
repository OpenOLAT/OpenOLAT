/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.commons.calendar.ui.components;

import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.ui.CalendarColorChooserController;
import org.olat.core.util.StringHelper;

public class KalendarEventRenderWrapper {
	
	private KalendarEvent event;
	private KalendarRenderWrapper calendarWrapper;

	/**
	 * Render properties
	 */
	private int yPosAbsolute, heightAbsolute;
	private float xPosRelative;
	private float widthRelative;
	// assigned slot
	private int assignedSlot;
	// maximum slots for events in this group
	private int maxSlots;
	// how many slots to expand to the east
	private int slotExpandToEast;
	
	public KalendarEventRenderWrapper(KalendarEvent event, KalendarRenderWrapper calendarWrapper) {
		this.event = event;
		this.calendarWrapper = calendarWrapper;
	}

	public KalendarEvent getEvent() {
		return event;
	}

	public KalendarRenderWrapper getKalendarRenderWrapper() {
		return calendarWrapper;
	}
	
	public int getHeightAbsolute() {
		return heightAbsolute;
	}

	public void setHeightAbsolute(int height) {
		this.heightAbsolute = height;
	}

	public float getWidthRelative() {
		return widthRelative;
	}

	public void setWidthRelative(float width) {
		this.widthRelative = width;
	}

	public float getXPosRelative() {
		return xPosRelative;
	}

	public void setXPosRelative(float x) {
		this.xPosRelative = x;
	}

	public int getYPosAbsolute() {
		return yPosAbsolute;
	}

	public void setYPosAbsolute(int y) {
		this.yPosAbsolute = y;
	}

	public int getAssignedSlot() {
		return assignedSlot;
	}

	public void setAssignedSlot(int slot) {
		this.assignedSlot = slot;
	}

	public int getSlotExpandToEast() {
		return slotExpandToEast;
	}

	public void setSlotExpandToEast(int slotExpandToEast) {
		this.slotExpandToEast = slotExpandToEast;
	}

	public int getMaxSlots() {
		return maxSlots;
	}

	public void setMaxSlots(int maxSlots) {
		this.maxSlots = maxSlots;
	}

	public int getCalendarAccess() {
		return calendarWrapper.getAccess();
	}

	public String getCssClass() {
		if (StringHelper.containsNonWhitespace(event.getColor())) {
			String eventColor = "o_cal_".concat(event.getColor());
			if (CalendarColorChooserController.colorExists(eventColor)) {
				return eventColor;
			}
		}
		return calendarWrapper.getCssClass();
	}
	
}
