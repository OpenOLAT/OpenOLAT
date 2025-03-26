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
package org.olat.modules.bigbluebutton.manager;

import org.olat.commons.calendar.model.KalendarEventLink;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.StringHelper;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;

/**
 * 
 * Initial date: 24 mars 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class BigBlueButtonCalendarHelper {
	
	private BigBlueButtonCalendarHelper() {
		//
	}
	
	public static final String generateEventExternalId(BigBlueButtonMeeting meeting) {
		return "bigbluebutton-".concat(meeting.getMeetingId());
	}
	
	public static final KalendarEventLink generateEventLink(BigBlueButtonMeeting meeting) {
		String id = meeting.getKey().toString();
		String displayName = meeting.getName();
		if(meeting.getEntry() != null) {
			StringBuilder businessPath = new StringBuilder(128);
			businessPath.append("[RepositoryEntry:").append(meeting.getEntry().getKey()).append("]");
			if(StringHelper.containsNonWhitespace(meeting.getSubIdent())) {
				businessPath.append("[CourseNode:").append(meeting.getSubIdent()).append("]");
			}
			businessPath.append("[Meeting:").append(meeting.getKey()).append("]");
			String url = BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath.toString());
			return new KalendarEventLink("bigbluebutton", id, displayName, url, "o_CourseModule_icon");
		} else if(meeting.getBusinessGroup() != null) {
			StringBuilder businessPath = new StringBuilder(128);
			businessPath.append("[BusinessGroup:").append(meeting.getBusinessGroup().getKey())
				.append("][toolbigbluebutton:0][Meeting:").append(meeting.getKey()).append("]");
			String url = BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath.toString());
			return new KalendarEventLink("bigbluebutton", id, displayName, url, "o_icon_group");
		}
		return null;
	}
}
