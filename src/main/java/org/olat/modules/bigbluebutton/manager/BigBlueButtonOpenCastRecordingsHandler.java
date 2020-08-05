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
package org.olat.modules.bigbluebutton.manager;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.group.BusinessGroup;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.BigBlueButtonRecording;
import org.olat.modules.bigbluebutton.BigBlueButtonRecordingsHandler;
import org.olat.modules.bigbluebutton.model.BigBlueButtonErrors;
import org.olat.modules.bigbluebutton.ui.BigBlueButtonAdminController;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 5 août 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
@Qualifier("opencast")
public class BigBlueButtonOpenCastRecordingsHandler implements BigBlueButtonRecordingsHandler  {
	
	public static final String OPENCAST_RECORDING_HANDLER_ID = "opencast";
	
	@Override
	public String getId() {
		return OPENCAST_RECORDING_HANDLER_ID;
	}

	@Override
	public String getName(Locale locale) {
		Translator translator = Util.createPackageTranslator(BigBlueButtonAdminController.class, locale);
		return translator.translate("opencast.recording.handler");
	}
	
	@Override
	public boolean canDeleteRecordings() {
		return true;
	}

	@Override
	public List<BigBlueButtonRecording> getRecordings(BigBlueButtonMeeting meeting, BigBlueButtonErrors errors) {
		return Collections.emptyList();
	}

	@Override
	public String getRecordingURL(BigBlueButtonRecording recording) {
		return recording.getUrl();
	}

	@Override
	public void appendMetadata(BigBlueButtonUriBuilder uriBuilder, BigBlueButtonMeeting meeting) {
		Date meetingCreation = (meeting.getStartDate() != null ? meeting.getStartDate() : meeting.getCreationDate());
		
		// Title of the episode
		uriBuilder.optionalParameter("meta_dc-title", Formatter.formatDateFilesystemSave(meetingCreation) + " - " + meeting.getName());
		// Media package and event identifier
		uriBuilder.optionalParameter("meta_dc-identifier", meeting.getMeetingId());
		
		//TODO OO-4820
		uriBuilder.optionalParameter("meta_dc-creator", "creator.firstname lastname");

		// Series identifier of which the event is part of
		RepositoryEntry re = meeting.getEntry();
		BusinessGroup bg = meeting.getBusinessGroup();
		String context = "";
		String seriesTitle = null;
		if (re != null) {
			context = "RepositoryEntry::" + re.getKey();
			seriesTitle = re.getDisplayname();
		} else if (bg != null) {
			context = "BusinessGroup::" + bg.getKey();
		}
		String subIdent= meeting.getSubIdent();
		if (StringHelper.containsNonWhitespace(subIdent)) {
			context += "::" + subIdent.trim();
			
			if(re != null) {
				ICourse course = CourseFactory.loadCourse(re.getOlatResource().getResourceableId());
				CourseNode courseNode = course.getRunStructure().getNode(subIdent.trim());
				if(courseNode != null) {
					seriesTitle = courseNode.getShortTitle() + " " + seriesTitle;
				}
			}
		}
		uriBuilder.optionalParameter("meta_dc-isPartOf", context);

		// The primary language
		if (re != null) {
			uriBuilder.optionalParameter("meta_dc-language", re.getMainLanguage().trim());
		}

		// Location of the event
		uriBuilder.optionalParameter("meta_dc-spatial", "Olat-BigBlueButton");
		// Date of the event
		uriBuilder.optionalParameter("meta_dc-created", Formatter.formatDatetime(meetingCreation));							

		// Rights holder
		//TODO OO-4820
		uriBuilder.optionalParameter("meta_dc-rightsHolder", "creator.username");
		
		uriBuilder.optionalParameter("meta_opencast-series-dc-title", seriesTitle);

		//TODO OO-4820
		uriBuilder.optionalParameter("meta_opencast-acl-read-roles", "ROLE_OAUTH_USER ROLE_USER_{upperCase(creator.username)}");
	}

	@Override
	public boolean deleteRecordings(List<BigBlueButtonRecording> recordings, BigBlueButtonMeeting meeting, BigBlueButtonErrors errors) {
		return false;
	}
}
