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
package org.olat.course.nodes.livestream;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 22 May 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class LiveStreamModule extends AbstractSpringModule implements ConfigOnOff {

	public static final String LIVE_STREAM_ENABLED = "live.stream.enabled";
	public static final String LIVE_STREAM_MULTI_STREAM_ENABLED = "live.stream.multi.stream.enabled";
	public static final String LIVE_STREAM_URL_SEPARATOR = "live.stream.url.separator";
	public static final String LIVE_STREAM_BUFFER_BEFORE_MIN = "live.stream.buffer.before.min";
	public static final String LIVE_STREAM_BUFFER_AFTER_MIN = "live.stream.buffer.after.min";
	public static final String LIVE_STREAM_EDIT_COACH = "live.stream.edit.coach";
	public static final String LIVE_STREAM_PLAYER_PROFILE = "live.stream.player.profile";
	public static final String LIVE_STREAM_PAELLA_CONFIG = "live.stream.paella.config";

	@Value("${live.stream.enabled:false}")
	private boolean enabled;
	@Value("${live.stream.multi.stream.enabled:false}")
	private boolean multiStreamEnabled;
	@Value("${live.stream.url.separator:,}")
	private String urlSeparator;
	@Value("${live.stream.buffer.before.min:5}")
	private int bufferBeforeMin;
	@Value("${live.stream.buffer.after.min:5}")
	private int bufferAfterMin;
	@Value("${live.stream.edit.coach:false}")
	private boolean editCoach;
	@Value("${live.stream.player.profile:both}")
	private String playerProfile;
	@Value("${live.stream.paella.config}")
	private String paellaConfig;
	
	@Autowired
	public LiveStreamModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		String enabledObj = getStringPropertyValue(LIVE_STREAM_ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
		
		String multiStreamEnabledObj = getStringPropertyValue(LIVE_STREAM_MULTI_STREAM_ENABLED, true);
		if(StringHelper.containsNonWhitespace(multiStreamEnabledObj)) {
			multiStreamEnabled = "true".equals(multiStreamEnabledObj);
		}
		
		String urlSeparatorObj = getStringPropertyValue(LIVE_STREAM_URL_SEPARATOR, true);
		if(StringHelper.containsNonWhitespace(urlSeparatorObj)) {
			urlSeparator = urlSeparatorObj;
		}
		
		String bufferBeforeMinObj = getStringPropertyValue(LIVE_STREAM_BUFFER_BEFORE_MIN, true);
		if(StringHelper.containsNonWhitespace(bufferBeforeMinObj)) {
			bufferAfterMin = Integer.parseInt(bufferBeforeMinObj);
		}

		String bufferAfterMinObj = getStringPropertyValue(LIVE_STREAM_BUFFER_AFTER_MIN, true);
		if(StringHelper.containsNonWhitespace(bufferAfterMinObj)) {
			bufferAfterMin = Integer.parseInt(bufferAfterMinObj);
		}
		
		String editCoachObj = getStringPropertyValue(LIVE_STREAM_EDIT_COACH, true);
		if(StringHelper.containsNonWhitespace(editCoachObj)) {
			editCoach = "true".equals(editCoachObj);
		}
		
		String playerProfileObj = getStringPropertyValue(LIVE_STREAM_PLAYER_PROFILE, true);
		if(StringHelper.containsNonWhitespace(playerProfileObj)) {
			playerProfile = playerProfileObj;
		}
		
		paellaConfig = getStringPropertyValue(LIVE_STREAM_PAELLA_CONFIG, paellaConfig);
	}
	
	@Override
	protected void initFromChangedProperties() {
		init();
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		setStringProperty(LIVE_STREAM_ENABLED, Boolean.toString(enabled), true);
	}

	public boolean isMultiStreamEnabled() {
		return multiStreamEnabled;
	}

	public void setMultiStreamEnabled(boolean multiStreamEnabled) {
		this.multiStreamEnabled = multiStreamEnabled;
		setStringProperty(LIVE_STREAM_MULTI_STREAM_ENABLED, Boolean.toString(multiStreamEnabled), true);
	}

	public String getUrlSeparator() {
		return urlSeparator;
	}

	public void setUrlSeparator(String urlSeparator) {
		this.urlSeparator = urlSeparator;
		setStringProperty(LIVE_STREAM_URL_SEPARATOR, urlSeparator, true);
	}

	public int getBufferBeforeMin() {
		return bufferBeforeMin;
	}

	public void setBufferBeforeMin(int bufferBeforeMin) {
		this.bufferBeforeMin = bufferBeforeMin;
		setStringProperty(LIVE_STREAM_BUFFER_BEFORE_MIN, Integer.toString(bufferBeforeMin), true);
	}

	public int getBufferAfterMin() {
		return bufferAfterMin;
	}

	public void setBufferAfterMin(int bufferAfterMin) {
		this.bufferAfterMin = bufferAfterMin;
		setStringProperty(LIVE_STREAM_BUFFER_BEFORE_MIN, Integer.toString(bufferAfterMin), true);
	}

	public boolean isEditCoach() {
		return editCoach;
	}

	public void setEditCoach(boolean editCoach) {
		this.editCoach = editCoach;
		setStringProperty(LIVE_STREAM_EDIT_COACH, Boolean.toString(editCoach), true);
	}

	public String getPlayerProfile() {
		return playerProfile;
	}

	public void setPlayerProfile(String playerProfile) {
		this.playerProfile = playerProfile;
		setStringProperty(LIVE_STREAM_PLAYER_PROFILE, playerProfile, true);
	}

	public String getPaellaConfig() {
		return paellaConfig;
	}

	public void setPaellaConfig(String paellaConfig) {
		this.paellaConfig = paellaConfig;
		setStringProperty(LIVE_STREAM_PAELLA_CONFIG, paellaConfig, true);
	}

}
