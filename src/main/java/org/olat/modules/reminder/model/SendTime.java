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
package org.olat.modules.reminder.model;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;

/**
 * 
 * Initial date: 09.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SendTime {
	
	private static final Logger log = Tracing.createLoggerFor(SendTime.class);
	
	private final int hour;
	private final int minute;
	
	public SendTime(int hour, int minute) {
		this.hour = hour;
		this.minute = minute;
	}

	public int getHour() {
		return hour;
	}

	public int getMinute() {
		return minute;
	}
	
	public boolean isValid() {
		return (hour >= 0 && hour < 24) && (minute >= 0 && minute < 60);
	}
	
	public static SendTime parse(String defaultSendTime) {
		int hour = -1;
		int minute = -1;
		
		int index = defaultSendTime.indexOf(":");
		if(index > 0) {
			try {
				hour = Integer.parseInt(defaultSendTime.substring(0, index));
				minute = Integer.parseInt(defaultSendTime.substring(index + 1));
			} catch (Exception e) {
				log.error("", e);
			}
		} else {
			try {
				hour = Integer.parseInt(defaultSendTime);
				minute = 0;
			} catch (Exception e) {
				log.error("", e);
			}
		}
		return new SendTime(hour, minute);
	}

}
