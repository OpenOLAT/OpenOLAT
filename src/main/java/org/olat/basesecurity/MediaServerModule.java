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
package org.olat.basesecurity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial date: 2024-10-03<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service
public class MediaServerModule extends AbstractSpringModule {

	private static final String MEDIA_SERVER_MODE = "media.server.mode";
	private static final String MEDIA_SERVER_ENABLED = "media.server.enabled";

	public static final String YOUTUBE_KEY = "youtube";
	public static final String VIMEO_KEY = "vimeo";
	public static final String[] PREDEFINED_KEYS = { YOUTUBE_KEY, VIMEO_KEY };

	public static final String YOUTUBE_MEDIA_SRC = "https://youtu.be https://www.youtube.com";
	public static final String VIMEO_MEDIA_SRC = "https://vimeo.com https://player.vimeo.com";

	private MediaServerMode mode;

	private Map<String, Boolean> mediaServersEnabled;

	@Autowired
	public MediaServerModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		updateProperties();
	}

	private void updateProperties() {
		String mediaServerModeObj = getStringPropertyValue(MEDIA_SERVER_MODE, true);
		if (StringHelper.containsNonWhitespace(mediaServerModeObj)) {
			mode = MediaServerMode.valueOf(mediaServerModeObj);
		} else {
			mode = MediaServerMode.configure;
		}

		mediaServersEnabled = new HashMap<>();
		for (String key : PREDEFINED_KEYS) {
			String enabledObj = getStringPropertyValue(MEDIA_SERVER_ENABLED + "." + key, true);
			if (Boolean.TRUE.toString().equals(enabledObj)) {
				mediaServersEnabled.put(key, Boolean.TRUE);
			} else if (Boolean.FALSE.toString().equals(enabledObj)) {
				mediaServersEnabled.put(key, Boolean.FALSE);
			} else {
				mediaServersEnabled.put(key, Boolean.TRUE);
			}
		}
	}

	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}

	public MediaServerMode getMediaServerMode() {
		return mode;
	}

	public void setMediaServerMode(MediaServerMode mediaServerMode) {
		mode = mediaServerMode;
		setStringProperty(MEDIA_SERVER_MODE, mediaServerMode.name(), true);
	}

	private boolean isAllowAll() {
		return MediaServerMode.allowAll.equals(getMediaServerMode());
	}

	public boolean isMediaServerEnabled(String key) {
		return mediaServersEnabled.containsKey(key) && mediaServersEnabled.get(key);
	}

	public void setMediaServerEnabled(String key, boolean enabled) {
		mediaServersEnabled.put(key, enabled);
		setStringProperty(MEDIA_SERVER_ENABLED + "." + key, Boolean.toString(enabled), true);
	}

	public Collection<String> getMediaSrcUrls() {
		List<String> urls = new ArrayList<>();

		if (isAllowAll() || isMediaServerEnabled(YOUTUBE_KEY)) {
			urls.addAll(Arrays.stream(YOUTUBE_MEDIA_SRC.split(" ")).toList());
		}
		if (isAllowAll() || isMediaServerEnabled(VIMEO_KEY)) {
			urls.addAll(Arrays.stream(VIMEO_MEDIA_SRC.split(" ")).toList());
		}
		return urls;
	}
}
