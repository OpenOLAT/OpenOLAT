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

package org.olat.commons.calendar.model;

import java.util.Comparator;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;

public class KalendarComparator implements Comparator<KalendarRenderWrapper> {

	private static final KalendarComparator INSTANCE = new KalendarComparator();
	
	public static final KalendarComparator getInstance() { return INSTANCE; }
	
	@Override
	public int compare(KalendarRenderWrapper calendar0, KalendarRenderWrapper calendar1) {
		// if of the same type, order by display name
		if (calendar0.getKalendar().getType() == calendar1.getKalendar().getType())
			return calendar0.getDisplayName().compareTo(
					calendar1.getDisplayName());
		// if of different type, order by type
		if (calendar0.getKalendar().getType() == CalendarManager.TYPE_USER) return -1; // TYPE_USER is displayed first
		if (calendar0.getKalendar().getType() == CalendarManager.TYPE_GROUP) return +1; // TYPE GROUP is displayed last
		if (calendar1.getKalendar().getType() == CalendarManager.TYPE_USER) return +1;
		return -1;
	}

}
