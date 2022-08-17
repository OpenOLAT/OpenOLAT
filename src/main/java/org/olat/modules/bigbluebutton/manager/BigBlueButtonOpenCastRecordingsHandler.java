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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.group.BusinessGroup;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.BigBlueButtonRecording;
import org.olat.modules.bigbluebutton.BigBlueButtonRecordingsHandler;
import org.olat.modules.bigbluebutton.model.BigBlueButtonError;
import org.olat.modules.bigbluebutton.model.BigBlueButtonErrorCodes;
import org.olat.modules.bigbluebutton.model.BigBlueButtonErrors;
import org.olat.modules.bigbluebutton.model.BigBlueButtonRecordingImpl;
import org.olat.modules.bigbluebutton.ui.BigBlueButtonAdminController;
import org.olat.modules.opencast.OpencastEvent;
import org.olat.modules.opencast.OpencastModule;
import org.olat.modules.opencast.OpencastService;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
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
public class BigBlueButtonOpenCastRecordingsHandler implements BigBlueButtonRecordingsHandler {
	
	private static final Logger log = Tracing.createLoggerFor(BigBlueButtonOpenCastRecordingsHandler.class);
	
	public static final String OPENCAST_RECORDING_HANDLER_ID = "opencast";
	
	@Autowired
	private OpencastModule opencastModule;
	@Autowired
	private OpencastService opencastService;
	@Autowired
	private BaseSecurityModule securityModule;
	
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
	public String getRecordingInfo(Locale locale) {
		return null;
	}
	
	@Override
	public boolean canDeleteRecordings() {
		return true;
	}
	
	@Override
	public boolean allowPermanentRecordings() {
		return true;
	}

	@Override
	public List<BigBlueButtonRecording> getRecordings(BigBlueButtonMeeting meeting, BigBlueButtonErrors errors) {
		if(!opencastModule.isBigBlueButtonEnabled()) {
			log.error("Try getting recordings of disabled Opencast: {}", opencastModule.getApiUrl());
			errors.append(new BigBlueButtonError(BigBlueButtonErrorCodes.opencastDisabled));
			return Collections.emptyList();
		}
		
		List<OpencastEvent> events = opencastService.getEvents(meeting.getMeetingId(), true);
		List<BigBlueButtonRecording> recordings = new ArrayList<>(events.size());
		for (OpencastEvent event : events) {
			String recordId = event.getIdentifier();
			String name = event.getTitle();
			String meetingId = meeting.getMeetingId();
			Date startTime = event.getStart();
			Date endTime = event.getEnd();
			String url = null;
			String type = BigBlueButtonRecording.OPENCAST;
			recordings.add(BigBlueButtonRecordingImpl.valueOf(recordId, name, meetingId, startTime, endTime, url, type));
		}
		return recordings;
	}

	@Override
	public String getRecordingURL(UserSession usess, BigBlueButtonRecording recording) {
		return opencastService.getLtiEventMapperUrl(usess, recording.getRecordId(), "Learner");
	}

	@Override
	public void appendMetadata(BigBlueButtonUriBuilder uriBuilder, BigBlueButtonMeeting meeting) {
		Date meetingCreation = (meeting.getStartDate() != null ? meeting.getStartDate() : meeting.getCreationDate());
		
		// Title of the episode
		uriBuilder.optionalParameter("meta_opencast-dc-title", Formatter.formatDateFilesystemSave(meetingCreation) + " - " + meeting.getName());
		// Media package and event identifier
		uriBuilder.optionalParameter("meta_opencast-dc-identifier", meeting.getMeetingId());
		
		User creator = null;
		if(meeting.getCreator() != null) {
			creator = meeting.getCreator().getUser();
			
			String creatorFirstLastName = creatorFirstLastName(creator);
			if(StringHelper.containsNonWhitespace(creatorFirstLastName)) {
				uriBuilder.optionalParameter("meta_opencast-dc-creator", creatorFirstLastName);
			}
			
			String username;
			if(securityModule.isIdentityNameAutoGenerated()) {
				username = creator.getProperty(UserConstants.NICKNAME, null);
			} else {
				username = meeting.getCreator().getName();
			}
			if(StringHelper.containsNonWhitespace(username)) {
				uriBuilder.optionalParameter("meta_opencast-dc-rightsHolder", username);
				uriBuilder.optionalParameter("meta_opencast-acl-read-roles", "ROLE_USER_" + username.toUpperCase());
				uriBuilder.optionalParameter("meta_opencast-acl-write-roles", "ROLE_USER_" + username.toUpperCase());
			}
		}

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
		uriBuilder.optionalParameter("meta_opencast-dc-isPartOf", context);

		// The primary language
		if (re != null && StringHelper.containsNonWhitespace(re.getMainLanguage())) {
			uriBuilder.optionalParameter("meta_opencast-dc-language", re.getMainLanguage().trim().toUpperCase());
		}

		// Location of the event
		uriBuilder.optionalParameter("meta_opencast-dc-spatial", "OpenOlat-BigBlueButton");
		// Date of the event
		uriBuilder.optionalParameter("meta_opencast-dc-created", Formatter.formatDatetime(meetingCreation));

		uriBuilder.optionalParameter("meta_opencast-series-dc-title", seriesTitle);
	}
	
	private String creatorFirstLastName(User creator) {
		StringBuilder sb = new StringBuilder();
		if(StringHelper.containsNonWhitespace(creator.getFirstName())) {
			sb.append(creator.getFirstName());
		}
		if(StringHelper.containsNonWhitespace(creator.getLastName())) {
			if(sb.length() > 0) sb.append(" ");
			sb.append(creator.getLastName());
		}
		return sb.toString();
	}

	@Override
	public boolean deleteRecordings(List<BigBlueButtonRecording> recordings, BigBlueButtonMeeting meeting, BigBlueButtonErrors errors) {
		if (!opencastModule.isBigBlueButtonEnabled()) {
			log.error("Try deleting a recording of disabled Opencast: {}", opencastModule.getApiUrl());
			errors.append(new BigBlueButtonError(BigBlueButtonErrorCodes.opencastDisabled));
			return false;
		}
		boolean success = false;
		for (BigBlueButtonRecording recording : recordings) {
			boolean ok = opencastService.deleteEvents(recording.getRecordId());			
			if (!ok) {
				log.warn("Could not delete Opencast event::{} for meetingId::{})", recording.getRecordId(),  meeting.getMeetingId());
				errors.append(new BigBlueButtonError(BigBlueButtonErrorCodes.unkown));
				return false;
			}
			success &= ok;			
		}
		return success;
	}
}
