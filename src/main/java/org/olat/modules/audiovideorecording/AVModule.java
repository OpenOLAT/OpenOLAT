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
package org.olat.modules.audiovideorecording;

import org.apache.logging.log4j.Logger;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Initial date: 2022-10-06<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service
public class AVModule extends AbstractSpringModule {

	private static final Logger log = Tracing.createLoggerFor(AVModule.class);
	private static final String VIDEO_RECORDING_ENABLED = "av.video.recording.enabled";
	private static final String AUDIO_RECORDING_ENABLED = "av.audio.recording.enabled";

	@Value("${av.video.recording.enabled:true}")
	private boolean videoRecordingEnabled;
	@Value("${av.audio.recording.enabled:false}")
	private boolean audioRecordingEnabled;

	@Autowired
	public AVModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		String videoRecordingEnabledObj = getStringPropertyValue(VIDEO_RECORDING_ENABLED, true);
		if (StringHelper.containsNonWhitespace(videoRecordingEnabledObj)) {
			videoRecordingEnabled = "true".equals(videoRecordingEnabledObj);
		}

		String audioRecordingEnabledObj = getStringPropertyValue(AUDIO_RECORDING_ENABLED, true);
		if (StringHelper.containsNonWhitespace(audioRecordingEnabledObj)) {
			audioRecordingEnabled = "true".equals(audioRecordingEnabledObj);
		}

		log.info("av.video.recording.enabled={}", videoRecordingEnabled);
		log.info("av.audio.recording.enabled={}", audioRecordingEnabled);
	}

	@Override
	protected void initFromChangedProperties() {
		init();
	}

	public boolean isVideoRecordingEnabled() {
		return videoRecordingEnabled;
	}

	public void setVideoRecordingEnabled(boolean videoRecordingEnabled) {
		this.videoRecordingEnabled = videoRecordingEnabled;
		setStringProperty(VIDEO_RECORDING_ENABLED, Boolean.toString(videoRecordingEnabled), true);
	}

	public boolean isAudioRecordingEnabled() {
		return audioRecordingEnabled;
	}

	public void setAudioRecordingEnabled(boolean audioRecordingEnabled) {
		this.audioRecordingEnabled = audioRecordingEnabled;
		setStringProperty(AUDIO_RECORDING_ENABLED, Boolean.toString(audioRecordingEnabled), true);
	}
}
