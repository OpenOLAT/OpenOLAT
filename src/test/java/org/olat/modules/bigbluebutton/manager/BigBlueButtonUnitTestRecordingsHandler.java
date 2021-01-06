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
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.translator.Translator;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.BigBlueButtonRecording;
import org.olat.modules.bigbluebutton.BigBlueButtonRecordingsHandler;
import org.olat.modules.bigbluebutton.model.BigBlueButtonErrors;
import org.olat.modules.bigbluebutton.ui.BigBlueButtonAdminController;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * This is a recordings handler used as mock handler for unit tests but
 * not created using mockito to extra check database queries.
 * 
 * Initial date: 6 janv. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
@Qualifier("unittests")
public class BigBlueButtonUnitTestRecordingsHandler implements BigBlueButtonRecordingsHandler  {
	
	public static final String UNITTESTS_RECORDING_HANDLER_ID = "unittests";
	
	private List<BigBlueButtonRecording> recordingsList;
	
	public List<BigBlueButtonRecording> getRecordingsList() {
		return recordingsList;
	}

	public void setRecordingsList(List<BigBlueButtonRecording> recordingsList) {
		this.recordingsList = new ArrayList<>(recordingsList);
	}

	@Override
	public String getId() {
		return UNITTESTS_RECORDING_HANDLER_ID;
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
		return true;
	}

	@Override
	public List<BigBlueButtonRecording> getRecordings(BigBlueButtonMeeting meeting, BigBlueButtonErrors errors) {
		return recordingsList;
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
	}

	@Override
	public boolean deleteRecordings(List<BigBlueButtonRecording> recordings, BigBlueButtonMeeting meeting, BigBlueButtonErrors errors) {
		return recordingsList.removeAll(recordings);
	}

}
