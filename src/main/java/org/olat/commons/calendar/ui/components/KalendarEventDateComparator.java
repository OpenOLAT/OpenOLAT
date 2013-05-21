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

import java.util.Comparator;
import java.util.Date;


public class KalendarEventDateComparator implements Comparator<KalendarEventRenderWrapper> {
	
	private static final KalendarEventDateComparator INSTANCE = new KalendarEventDateComparator();
	
	public KalendarEventDateComparator() {
		//
	}
	
	public static KalendarEventDateComparator getInstance() {
		return INSTANCE;
	}

	@Override
	public int compare(KalendarEventRenderWrapper event0, KalendarEventRenderWrapper event1) {
		Date startEvent0 = event0.getEvent().getBegin();
		Date startEvent1 = event1.getEvent().getBegin();
		return startEvent0.compareTo(startEvent1);
	}
}
