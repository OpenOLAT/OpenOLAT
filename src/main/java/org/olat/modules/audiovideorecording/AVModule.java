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

import java.io.IOException;
import java.util.ArrayList;

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
	private static final String LOCAL_TRANSCODING_ENABLED = "av.local.transcoding.enabled";
	private static final String HANDBRAKE_CLI_PATH = "av.handbrakecli.path";

	@Value("${av.video.recording.enabled:false}")
	private boolean videoRecordingEnabled;
	@Value("${av.audio.recording.enabled:false}")
	private boolean audioRecordingEnabled;
	@Value("${av.local.transcoding.enabled:false}")
	private boolean localTranscodingEnabled;
	@Value("${av.handbrakecli.path}")
	private String handbrakeCliPath;

	private Boolean localTranscodingPossible;

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

		String localTranscodingEnabledObj = getStringPropertyValue(LOCAL_TRANSCODING_ENABLED, true);
		if (StringHelper.containsNonWhitespace(localTranscodingEnabledObj)) {
			localTranscodingEnabled = "true".equals(localTranscodingEnabledObj);
		}

		String handbrakeCliPathObj = getStringPropertyValue(HANDBRAKE_CLI_PATH, true);
		if (StringHelper.containsNonWhitespace(handbrakeCliPathObj)) {
			handbrakeCliPath = handbrakeCliPathObj;
		}

		log.info("av.video.recording.enabled={}", videoRecordingEnabled);
		log.info("av.audio.recording.enabled={}", audioRecordingEnabled);
		log.info("av.local.transcoding.enabled={}", localTranscodingEnabled);
		log.info("av.handbrakecli.path={}", handbrakeCliPath);
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

	public boolean isLocalTranscodingEnabled() {
		return localTranscodingEnabled;
	}

	public void setLocalTranscodingEnabled(boolean localTranscodingEnabled) {
		this.localTranscodingEnabled = localTranscodingEnabled;
		setStringProperty(LOCAL_TRANSCODING_ENABLED, Boolean.toString(localTranscodingEnabled), true);
	}

	public String getHandbrakeCliPath() {
		return handbrakeCliPath;
	}

	public boolean isLocalTranscodingPossible() {
		if (localTranscodingPossible == null) {
			localTranscodingPossible = canStartHandBrake();
		}
		return localTranscodingPossible;
	}

	private boolean canStartHandBrake() {
		ArrayList<String> command = new ArrayList<>();
		if (StringHelper.containsNonWhitespace(handbrakeCliPath)) {
			command.add(handbrakeCliPath);
		} else {
			command.add("HandBrakeCLI");
		}
		command.add("--version");
		ProcessBuilder processBuilder = new ProcessBuilder(command);
		Process process = null;
		try {
			process = processBuilder.start();
			int exitValue = process.waitFor();
			log.debug("HandBrakeCLI --version exit value: {}", exitValue);
			return exitValue == 0;
		} catch (IOException e) {
			log.info("HandBrakeCLI --version cannot execute", e);
			return false;
		} catch (InterruptedException e) {
			log.warn("HandBrakeCLI --version interrupted", e);
			return false;
		} finally {
			if (process != null) {
				process.destroy();
			}
		}
	}
}
