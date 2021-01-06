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
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.User;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.group.BusinessGroup;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.BigBlueButtonRecording;
import org.olat.modules.bigbluebutton.BigBlueButtonRecordingsHandler;
import org.olat.modules.bigbluebutton.BigBlueButtonServer;
import org.olat.modules.bigbluebutton.model.BigBlueButtonError;
import org.olat.modules.bigbluebutton.model.BigBlueButtonErrorCodes;
import org.olat.modules.bigbluebutton.model.BigBlueButtonErrors;
import org.olat.modules.bigbluebutton.ui.BigBlueButtonAdminController;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

/**
 * 
 * Initial date: 5 août 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
@Qualifier("native")
public class BigBlueButtonNativeRecordingsHandler implements BigBlueButtonRecordingsHandler  {
	
	private static final Logger log = Tracing.createLoggerFor(BigBlueButtonNativeRecordingsHandler.class);
	public static final String NATIVE_RECORDING_HANDLER_ID = "native";
	
	@Autowired
	private BigBlueButtonManager bigBlueButtonManager;
	
	@Override
	public String getId() {
		return NATIVE_RECORDING_HANDLER_ID;
	}
	
	@Override
	public String getName(Locale locale) {
		Translator translator = Util.createPackageTranslator(BigBlueButtonAdminController.class, locale);
		return translator.translate("native.recording.handler");
	}

	@Override
	public String getRecordingInfo(Locale locale) {
		Translator translator = Util.createPackageTranslator(BigBlueButtonAdminController.class, locale);
		return translator.translate("recording.browser.infos");
	}
	
	@Override
	public boolean canDeleteRecordings() {
		return true;
	}

	@Override
	public boolean allowPermanentRecordings() {
		return false;
	}

	@Override
	public List<BigBlueButtonRecording> getRecordings(BigBlueButtonMeeting meeting, BigBlueButtonErrors errors) {
		if(meeting == null || meeting.getServer() == null || !meeting.getServer().isEnabled()) {
			return new ArrayList<>();
		}
		
		BigBlueButtonUriBuilder uriBuilder = bigBlueButtonManager.getUriBuilder(meeting.getServer());
		uriBuilder
			.operation("getRecordings")
			.parameter("meetingID", meeting.getMeetingId());
		
		Document doc = bigBlueButtonManager.sendRequest(uriBuilder, errors);
		if(BigBlueButtonUtils.checkSuccess(doc, errors)) {
			return BigBlueButtonUtils.getRecordings(doc);
		}
		return Collections.emptyList();
	}

	@Override
	public String getRecordingURL(UserSession usess, BigBlueButtonRecording recording) {
		return recording.getUrl();
	}

	@Override
	public void appendMetadata(BigBlueButtonUriBuilder uriBuilder, BigBlueButtonMeeting meeting) {
		// Title of the episode
		uriBuilder.optionalParameter("meta_dc-title", meeting.getName());
		// Media package and event identifier
		uriBuilder.optionalParameter("meta_dc-identifier", meeting.getMeetingId());

		String creator = getCreator(meeting);
		if(StringHelper.containsNonWhitespace(creator)) {
			uriBuilder.optionalParameter("meta_dc-creator", creator);
		}

		// Series identifier of which the event is part of
		RepositoryEntry re = meeting.getEntry();
		BusinessGroup bg = meeting.getBusinessGroup();
		String context = "";
		if (re != null) {
			context = "RepositoryEntry::" + re.getKey();
		} else if (bg != null) {
			context = "BusinessGroup::" + bg.getKey();
		}
		String subIdent= meeting.getSubIdent();
		if (StringHelper.containsNonWhitespace(subIdent)) {
			context += "::" + subIdent.trim();
		}
		uriBuilder.optionalParameter("meta_dc-isPartOf", context);

		// The primary language
		if (re != null && StringHelper.containsNonWhitespace(re.getMainLanguage())) {
			uriBuilder.optionalParameter("meta_dc-language", re.getMainLanguage().trim());
		}

		// Description of the event
		if (StringHelper.containsNonWhitespace(meeting.getDescription())) {
			uriBuilder.optionalParameter("meta_dc-description", meeting.getDescription());
		}
		// Location of the event
		uriBuilder.optionalParameter("meta_dc-spatial", "OpenOlat-BigBlueButton");
		// Date of the event
		Date meetingCreation = (meeting.getStartDate() != null ? meeting.getStartDate() : meeting.getCreationDate());
		uriBuilder.optionalParameter("meta_dc-created", Formatter.formatDatetime(meetingCreation));							

		// Rights holder
		if(StringHelper.containsNonWhitespace(meeting.getMainPresenter())) {
			uriBuilder.optionalParameter("meta_dc-rightsHolder", meeting.getMainPresenter());
		}
	}
	
	private String getCreator(BigBlueButtonMeeting meeting) {
		if(meeting.getCreator() == null) return null;
		
		User creator = meeting.getCreator().getUser();
		StringBuilder sb = new StringBuilder();
		if(StringHelper.containsNonWhitespace(creator.getFirstName())) {
			sb.append(creator.getFirstName());
		}
		if(StringHelper.containsNonWhitespace(creator.getLastName())) {
			if(sb.length() > 0) sb.append(" ");
			sb.append(creator.getLastName());
		}
		if(StringHelper.containsNonWhitespace(creator.getEmail())) {
			if(sb.length() > 0) sb.append(" ");
			sb.append("<").append(creator.getEmail()).append(">");
		}
		return sb.toString();
	}

	@Override
	public boolean deleteRecordings(List<BigBlueButtonRecording> recordings, BigBlueButtonMeeting meeting, BigBlueButtonErrors errors) {
		StringBuilder sb = new StringBuilder();
		if(recordings != null && !recordings.isEmpty()) {
			for(BigBlueButtonRecording recording:recordings) {
				String recordId = recording.getRecordId();
				if(StringHelper.containsNonWhitespace(recordId)) {
					if(sb.length() > 0) sb.append(",");
					sb.append(recordId);
				}
			}
		}
		return deleteRecording(sb.toString(), meeting.getServer(), errors);
	}

	private boolean deleteRecording(String recordId, BigBlueButtonServer server, BigBlueButtonErrors errors) {
		if(!server.isEnabled()) {
			log.error("Try deleting a recording of a disabled server: {}", server.getUrl());
			errors.append(new BigBlueButtonError(BigBlueButtonErrorCodes.serverDisabled));
			return false;
		}
		BigBlueButtonUriBuilder uriBuilder = bigBlueButtonManager.getUriBuilder(server);
		uriBuilder
			.operation("deleteRecordings")
			.parameter("recordID", recordId);
		
		Document doc = bigBlueButtonManager.sendRequest(uriBuilder, errors);
		return BigBlueButtonUtils.checkSuccess(doc, errors);
	}
}
