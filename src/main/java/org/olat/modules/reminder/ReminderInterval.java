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
package org.olat.modules.reminder;

import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 11.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum ReminderInterval {
	
	every24(24, "interval.24"),
	every12(12, "interval.12"),
	every8(8, "interval.8"),
	every6(6, "interval.6"),
	every4(4, "interval.4"),
	every2(2, "interval.2"),
	every1(1, "interval.1");
	
	private final int interval;
	private final String key;
	private final String i18nKey;
	
	private ReminderInterval(int interval, String i18nKey) {
		this.interval = interval;
		this.key = Integer.toString(interval);
		this.i18nKey = i18nKey;
	}
	
	public String key() {
		return key;
	}

	public int interval() {
		return interval;
	}

	public String i18nKey() {
		return i18nKey;
	}
	
	public static final ReminderInterval byKey(String key) {
		ReminderInterval interval = null;
		if(StringHelper.containsNonWhitespace(key)) {
			for(ReminderInterval value:values()) {
				if(key.equals(value.key())) {
					interval = value;
					break;
				}
			}
		}
		return interval;
	}
}
