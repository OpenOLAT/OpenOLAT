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

package org.olat.commons.calendar.ui;

import java.util.List;

import org.olat.core.util.StringHelper;

public class CalendarColors {

	private static final String CALENDAR_CLASS_PREFIX = "o_cal_";

	private static final String[] colors = new String[] {
			"green", "lime", "blue", "orange", "fuchsia", "yellow", "red", "rebeccapurple", "navy", "olive", "maroon",
			"grey"
	};
	private static final String[] colorClasses = new String[]{
		"o_cal_green", "o_cal_lime", "o_cal_blue", "o_cal_orange", "o_cal_fuchsia",
		"o_cal_yellow", "o_cal_red", "o_cal_rebeccapurple", "o_cal_navy", "o_cal_olive",
		"o_cal_maroon", "o_cal_grey"
	};
	
	public static boolean colorClassExists(String testColorClass) {
		if (StringHelper.containsNonWhitespace(testColorClass)) {
			for (String colorClass : colorClasses) {
				if (colorClass.equalsIgnoreCase(testColorClass)) {
					return true;
				}
			}
		}
		return false;
	}

	public static String colorFromColorClass(String colorCssClass) {
		if (!StringHelper.containsNonWhitespace(colorCssClass)) {
			return null;
		}
		if (!colorCssClass.startsWith(CALENDAR_CLASS_PREFIX)) {
			return colorCssClass;
		}
		return colorCssClass.substring(CALENDAR_CLASS_PREFIX.length());
	}

	public static String colorClassFromColor(String color) {
		if (!StringHelper.containsNonWhitespace(color)) {
			return null;
		}
		if (color.startsWith(CALENDAR_CLASS_PREFIX)) {
			return color;
		}
		return CALENDAR_CLASS_PREFIX + color;
	}

	public static String[] getColors() {
		return colors;
	}

	public static List<String> getColorsList() {
		return List.of(colors);
	}
}