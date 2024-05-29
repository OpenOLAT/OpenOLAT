/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.sharepoint;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * 
 * Initial date: 29 mai 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SharePointHelper {
	
	private SharePointHelper() {
		//
	}
	
	public static long toDateInMilliSeconds(OffsetDateTime dateTime) {
		if(dateTime == null) return -1l;
		return dateTime.toLocalDate().atTime(dateTime.toLocalTime()).atZone(ZoneId.systemDefault()).toEpochSecond() * 1000l;
	}
	
	public static Date toDate(OffsetDateTime dateTime) {
		if(dateTime == null) return null;
		long seconds = toDateInMilliSeconds(dateTime);
		return seconds == -1l ? null : new Date(seconds);
	}

}
