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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.resource.OresHelper;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;

import org.apache.logging.log4j.Logger;
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
	private static final String LOCAL_TRANSCODING_ENABLED = "av.local.transcoding.enabled";
	private static final String LOCAL_AUDIO_CONVERSION_ENABLED = "av.local.audio.conversion.enabled";
	private static final String HANDBRAKE_CLI_PATH = "av.handbrakecli.path";
	private static final String FFMPEG_PATH = "av.ffmpeg.path";
	private static final String PROPERTY_CATEGORY_COMMAND_PATH = "commandPath";
	private static final String PROPERTY_NAME_HAND_BRAKE_CLI = "HandBrakeCLI";
	private static final String PROPERTY_CATEGORY_TRANSCODING = "transcoding";
	private static final String PROPERTY_NAME_OPTIMIZE_MEMORY = "optimizeMemory";
	private static final String HAND_BRAKE_VERSION_OPTION = "--version";
	private static final String HAND_BRAKE_COMMAND = "HandBrakeCLI";
	private static final String HAND_BRAKE_EXPECTED_VERSION_PREFIX = "HandBrake";

	@Value("${av.video.recording.enabled:false}")
	private boolean videoRecordingEnabled;
	@Value("${av.audio.recording.enabled:false}")
	private boolean audioRecordingEnabled;
	@Value("${av.local.transcoding.enabled:false}")
	private boolean localTranscodingEnabled;
	@Value("${av.local.audio.conversion.enabled:false}")
	private boolean localAudioConversionEnabled;
	@Value("${av.handbrakecli.path}")
	private String handbrakeCliPath;
	@Value("${av.ffmpeg.path}")
	private String ffmpegPath;

	private Boolean localTranscodingPossible;
	private Boolean localAudioConversionPossible;

	@Autowired
	private PropertyManager propertyManager;
	@Autowired
	private OLATResourceManager olatResourceManager;

	private OLATResource olatResource;

	@Autowired
	public AVModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void afterPropertiesSet() {
		olatResource = olatResourceManager.findOrPersistResourceable(OresHelper.lookupType(AVModule.class));
		checkCliCommand(PROPERTY_NAME_HAND_BRAKE_CLI, handbrakeCliPath, HAND_BRAKE_COMMAND, HAND_BRAKE_VERSION_OPTION,
				HAND_BRAKE_EXPECTED_VERSION_PREFIX);
		setDefaults();
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

		String localAudioConversionEnabledObj = getStringPropertyValue(LOCAL_AUDIO_CONVERSION_ENABLED, true);
		if (StringHelper.containsNonWhitespace(localAudioConversionEnabledObj)) {
			localAudioConversionEnabled = "true".equals(localAudioConversionEnabledObj);
		}

		String handbrakeCliPathObj = getStringPropertyValue(HANDBRAKE_CLI_PATH, true);
		if (StringHelper.containsNonWhitespace(handbrakeCliPathObj)) {
			handbrakeCliPath = handbrakeCliPathObj;
		}

		String ffmpegPathObj = getStringPropertyValue(FFMPEG_PATH, true);
		if (StringHelper.containsNonWhitespace(ffmpegPathObj)) {
			ffmpegPath = ffmpegPathObj;
		}

		log.info("av.video.recording.enabled={}", videoRecordingEnabled);
		log.info("av.audio.recording.enabled={}", audioRecordingEnabled);
		log.info("av.local.transcoding.enabled={}", localTranscodingEnabled);
		log.info("av.local.audio.conversion.enabled={}", localAudioConversionEnabled);
		log.info("av.handbrakecli.path={}", handbrakeCliPath);
		log.info("av.ffmpeg.path={}", ffmpegPath);
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

	public boolean isLocalAudioConversionEnabled() {
		return localAudioConversionEnabled;
	}

	public void setLocalAudioConversionEnabled(boolean localAudioConversionEnabled) {
		this.localAudioConversionEnabled = localAudioConversionEnabled;
		setStringProperty(LOCAL_AUDIO_CONVERSION_ENABLED, Boolean.toString(localAudioConversionEnabled), true);
	}

	public boolean isOptimizeMemoryForVideos() {
		Property property = propertyManager.findProperty(null, null, olatResource,
				PROPERTY_CATEGORY_TRANSCODING, PROPERTY_NAME_OPTIMIZE_MEMORY);
		if (property == null) {
			return false;
		}

		return Boolean.parseBoolean(property.getStringValue());
	}

	public void setOptimizeMemoryForVideos(boolean value) {
		Property property = propertyManager.findProperty(null, null, olatResource,
				PROPERTY_CATEGORY_TRANSCODING, PROPERTY_NAME_OPTIMIZE_MEMORY);
		if (property == null) {
			Property newProperty = propertyManager.createPropertyInstance(null, null, olatResource,
					PROPERTY_CATEGORY_TRANSCODING, PROPERTY_NAME_OPTIMIZE_MEMORY,
					null, null, Boolean.valueOf(value).toString(), null);
			propertyManager.saveProperty(newProperty);
		} else {
			property.setStringValue(Boolean.valueOf(value).toString());
			propertyManager.updateProperty(property);
		}
	}

	private void setDefaults() {
		Property property = propertyManager.findProperty(null, null, olatResource,
				PROPERTY_CATEGORY_TRANSCODING, PROPERTY_NAME_OPTIMIZE_MEMORY);
		if (property == null) {
			Property newProperty = propertyManager.createPropertyInstance(null, null, olatResource,
					PROPERTY_CATEGORY_TRANSCODING, PROPERTY_NAME_OPTIMIZE_MEMORY,
					null, null, Boolean.TRUE.toString(), null);
			propertyManager.saveProperty(newProperty);
		}
	}

	private void checkCliCommand(String propertyName, String olatPropertiesCliCommandPath, String command,
								 String versionOption, String expectedVersionPrefix) {
		// find command path from o_property
		Property property = propertyManager.findProperty(null, null, olatResource,
				PROPERTY_CATEGORY_COMMAND_PATH, propertyName);
		if (property != null) {
			String path = property.getStringValue();
			if (StringHelper.containsNonWhitespace(path)) {
				String cliVersion = getCliCommandVersion(path, versionOption);
				if (StringHelper.containsNonWhitespace(cliVersion) && cliVersion.startsWith(expectedVersionPrefix)) {
					log.info("{} '{}' in o_property is valid and returns '{}'.", command, path, cliVersion);
					return;
				}
			}
		}

		// find command path from olat.properties
		if (StringHelper.containsNonWhitespace(olatPropertiesCliCommandPath)) {
			String cliVersion = getCliCommandVersion(olatPropertiesCliCommandPath, versionOption);
			if (StringHelper.containsNonWhitespace(cliVersion) && cliVersion.startsWith(expectedVersionPrefix)) {
				if (property == null) {
					Property newProperty = propertyManager.createPropertyInstance(null, null, olatResource,
							PROPERTY_CATEGORY_COMMAND_PATH, propertyName,
							null, null, olatPropertiesCliCommandPath, null);
					propertyManager.saveProperty(newProperty);
				} else {
					property.setStringValue(olatPropertiesCliCommandPath);
					propertyManager.updateProperty(property);
				}
				log.info("{} '{}' in olat.properties is valid and returns '{}'.", command, olatPropertiesCliCommandPath, cliVersion);
				return;
			}
		}

		// try command path
		String cliVersion = getCliCommandVersion(command, versionOption);
		if (StringHelper.containsNonWhitespace(cliVersion) && cliVersion.startsWith(expectedVersionPrefix)) {
			String whichResult = getCliCommandWhich(command);
			if (StringHelper.containsNonWhitespace(whichResult)) {
				if (property == null) {
					Property newProperty = propertyManager.createPropertyInstance(null, null, olatResource,
							PROPERTY_CATEGORY_COMMAND_PATH, propertyName,
							null, null, whichResult, null);
					propertyManager.saveProperty(newProperty);
				} else {
					property.setStringValue(whichResult);
					propertyManager.updateProperty(property);
				}
				log.info("{} '{}' on path is valid.", command, whichResult);
				return;
			}
		}

		if (property == null) {
			Property newProperty = propertyManager.createPropertyInstance(null, null, olatResource,
					PROPERTY_CATEGORY_COMMAND_PATH, propertyName,
					null, null, "", null);
			propertyManager.saveProperty(newProperty);
		} else {
			property.setStringValue("");
			propertyManager.updateProperty(property);
		}
		log.info("{} not found.", command);
	}

	private String getCliCommandVersion(String path, String versionOption) {
		return getCliCommandResult(Arrays.asList(path, versionOption));
	}

	private String getCliCommandResult(List<String> command) {
		ProcessBuilder processBuilder = new ProcessBuilder(command);
		Process process = null;
		try {
			process = processBuilder.start();
			String stdout = readStandardOutput(process);
			int exitValue = process.waitFor();
			return exitValue == 0 ? stdout : null;
		} catch (IOException e) {
			log.info("{} cannot execute", String.join(" ", command), e);
		} catch (InterruptedException e) {
			log.warn("{} interrupted", String.join(" ", command), e);
		} finally {
			if (process != null) {
				process.destroy();
			}
		}
		return null;
	}

	private String readStandardOutput(Process process) {
		InputStream inputStream = process.getInputStream();
		InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
		BufferedReader bufferedInputStreamReader = new BufferedReader(inputStreamReader);
		StringBuilder input = new StringBuilder();

		String line;
		try {
			while ((line = bufferedInputStreamReader.readLine()) != null) {
				input.append(line);
			}
		} catch (IOException e) {
			//
		}

		return input.toString();
	}

	private String getCliCommandWhich(String command) {
		return getCliCommandResult(Arrays.asList("which", command));
	}

	public String getHandbrakeCliPath() {
		return handbrakeCliPath;
	}

	public String getFfmpegPath() {
		return ffmpegPath;
	}

	public boolean isLocalTranscodingPossible() {
		if (localTranscodingPossible == null) {
			localTranscodingPossible = canStartHandBrake();
		}
		return localTranscodingPossible;
	}

	public boolean isLocalAudioConversionPossible() {
		if (localAudioConversionPossible == null) {
			localAudioConversionPossible = canStartFfmpeg();
		}
		return localAudioConversionPossible;
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

	private boolean canStartFfmpeg() {
		ArrayList<String> command = new ArrayList<>();
		if (StringHelper.containsNonWhitespace(ffmpegPath)) {
			command.add(ffmpegPath);
		} else {
			command.add("ffmpeg");
		}
		command.add("-version");
		ProcessBuilder processBuilder = new ProcessBuilder(command);
		Process process = null;
		try {
			process = processBuilder.start();
			int exitValue = process.waitFor();
			log.debug("ffmpeg -version exit value: {}", exitValue);
			return exitValue == 0;
		} catch (IOException e) {
			log.info("ffmpeg -version cannot execute", e);
			return false;
		} catch (InterruptedException e) {
			log.warn("ffmpeg -version interrupted", e);
			return false;
		} finally {
			if (process != null) {
				process.destroy();
			}
		}
	}
}
