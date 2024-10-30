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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.xml.XStreamHelper;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Initial date: 2024-10-03<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service
public class MediaServerModule extends AbstractSpringModule {

	private static final Pattern DOMAIN_NAME_PATTERN = Pattern.compile("^((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}$");

	private static final String MEDIA_SERVER_MODE = "media.server.mode";
	private static final String MEDIA_SERVER_ENABLED = "media.server.enabled";
	private static final String MEDIA_SERVERS_CUSTOM = "media.servers.custom";

	public static final String YOUTUBE_KEY = "youtube";
	public static final String VIMEO_KEY = "vimeo";
	public static final String NANOO_TV_KEY = "nanootv";
	public static final String[] PREDEFINED_KEYS = { YOUTUBE_KEY, VIMEO_KEY, NANOO_TV_KEY };

	public static final String YOUTUBE_NAME = "YouTube";
	public static final String VIMEO_NAME = "Vimeo";
	public static final String NANOO_TV_NAME = "nanoo.tv";
	public static final String PANOPTO_NAME = "Panopto"; // currently not configurable and added as a constant

	public static final String YOUTUBE_MEDIA_SRC = "https://youtu.be https://www.youtube.com";
	public static final String VIMEO_MEDIA_SRC = "https://vimeo.com https://player.vimeo.com";
	public static final String NANOO_TV_MEDIA_SRC = "https://nanoo.tv";


	@Value("${media.server.mode:configure}")
	private String mode;

	@Value("${media.server.youtube:true}")
	private String youtube;

	@Value("${media.server.vimeo:true}")
	private String vimeo;

	@Value("${media.server.nanootv:true}")
	private String nanootv;

	private Map<String, Boolean> mediaServersEnabled;

	private List<MediaServer> customMediaServers;

	private static final XStream xStream = XStreamHelper.createXStreamInstance();
	static {
		Class<?>[] types = new Class[] {
				MediaServer.class
		};
		xStream.addPermission(new ExplicitTypePermission(types));

		xStream.alias("mediaServer", MediaServer.class);
	}

	@Autowired
	public MediaServerModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		updateProperties();
	}

	private void updateProperties() {
		String modeFromOlatProperties = mode;

		String mediaServerModeObj = getStringPropertyValue(MEDIA_SERVER_MODE, true);
		if (StringHelper.containsNonWhitespace(mediaServerModeObj)) {
			mode = mediaServerModeObj;
		} else if (!StringHelper.containsNonWhitespace(modeFromOlatProperties)) {
			mode = MediaServerMode.configure.name();
		}

		mediaServersEnabled = new HashMap<>();
		Map<String, Boolean> mediaServersFromOlatProperties = new HashMap<>();
		mediaServersFromOlatProperties.put(YOUTUBE_KEY, Boolean.valueOf(youtube));
		mediaServersFromOlatProperties.put(VIMEO_KEY, Boolean.valueOf(vimeo));
		mediaServersFromOlatProperties.put(NANOO_TV_KEY, Boolean.valueOf(nanootv));

		for (String key : PREDEFINED_KEYS) {
			String enabledObj = getStringPropertyValue(MEDIA_SERVER_ENABLED + "." + key, true);
			if (Boolean.TRUE.toString().equals(enabledObj)) {
				mediaServersEnabled.put(key, Boolean.TRUE);
			} else if (Boolean.FALSE.toString().equals(enabledObj)) {
				mediaServersEnabled.put(key, Boolean.FALSE);
			} else {
				mediaServersEnabled.put(key, mediaServersFromOlatProperties.get(key));
			}
		}

		String customMediaServersObj = getStringPropertyValue(MEDIA_SERVERS_CUSTOM, true);
		if (StringHelper.containsNonWhitespace(customMediaServersObj)) {
			//noinspection unchecked
			customMediaServers = (List<MediaServer>)xStream.fromXML(customMediaServersObj);
		} else {
			customMediaServers = new ArrayList<>();
		}
	}

	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}

	public MediaServerMode getMediaServerMode() {
		return MediaServerMode.secureValueOf(mode, MediaServerMode.configure);
	}

	public void setMediaServerMode(MediaServerMode mediaServerMode) {
		mode = mediaServerMode.name();
		setStringProperty(MEDIA_SERVER_MODE, mode, true);
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

	public boolean isValidDomain(String domain) {
		return DOMAIN_NAME_PATTERN.matcher(domain).find();
	}

	public List<MediaServer> getCustomMediaServers() {
		return customMediaServers;
	}

	public void updateCustomMediaServer(MediaServer mediaServer) {
		if (customMediaServers.contains(mediaServer)) {
			customMediaServers = customMediaServers.stream()
					.map(m -> m.equals(mediaServer) ? mediaServer : m)
					.sorted(Comparator.comparing(MediaServer::getName)).collect(Collectors.toList());
		} else {
			customMediaServers = new ArrayList<>(customMediaServers);
			customMediaServers.add(mediaServer);
			customMediaServers = customMediaServers.stream()
					.sorted(Comparator.comparing(MediaServer::getName)).collect(Collectors.toList());
		}
		storeCustomMediaServers();
	}

	public void deleteCustomMediaServer(MediaServer mediaServer) {
		if (mediaServer == null || mediaServer.getId() == null) {
			return;
		}
		customMediaServers = customMediaServers.stream().filter(m -> !m.equals(mediaServer))
				.sorted(Comparator.comparing(MediaServer::getName)).collect(Collectors.toList());
		storeCustomMediaServers();
	}

	private void storeCustomMediaServers() {
		String customMediaServersObj = xStream.toXML(customMediaServers);
		setStringProperty(MEDIA_SERVERS_CUSTOM, customMediaServersObj, true);
	}

	public Collection<String> getMediaSrcUrls() {
		List<String> urls = new ArrayList<>();

		if (isAllowAll() || isMediaServerEnabled(YOUTUBE_KEY)) {
			urls.addAll(Arrays.stream(YOUTUBE_MEDIA_SRC.split(" ")).toList());
		}
		if (isAllowAll() || isMediaServerEnabled(VIMEO_KEY)) {
			urls.addAll(Arrays.stream(VIMEO_MEDIA_SRC.split(" ")).toList());
		}
		if (isAllowAll() || isMediaServerEnabled(NANOO_TV_KEY)) {
			urls.addAll(Arrays.stream(NANOO_TV_MEDIA_SRC.split(" ")).toList());
		}
		return urls;
	}

	public List<String> getMediaServerNames() {
		ArrayList<String> result = new ArrayList<>(getPredefinedMediaServerNames());
		result.addAll(getCustomMediaServerNames());
		result.add(PANOPTO_NAME);
		return result;
	}

	private List<String> getPredefinedMediaServerNames() {
		List<String> urls = new ArrayList<>();
		if (isAllowAll() || isMediaServerEnabled(YOUTUBE_KEY)) {
			urls.add(YOUTUBE_NAME);
		}
		if (isAllowAll() || isMediaServerEnabled(VIMEO_KEY)) {
			urls.add(VIMEO_NAME);
		}
		if (isAllowAll() || isMediaServerEnabled(NANOO_TV_KEY)) {
			urls.add(NANOO_TV_NAME);
		}
		return urls;
	}

	private List<String> getCustomMediaServerNames() {
		return getCustomMediaServers().stream().map(MediaServer::getName).collect(Collectors.toList());
	}

	public boolean isRestrictedDomain(String urlString) {
		if (MediaServerMode.allowAll.equals(getMediaServerMode())) {
			return false;
		}
		try {
			URL url = new URL(urlString);
			for (String mediaSrcUrl : getMediaSrcUrls()) {
				if (urlString.startsWith(mediaSrcUrl)) {
					return false;
				}
			}
			String domainToTest = url.getHost();
			for (MediaServer mediaServer : getCustomMediaServers()) {
				if (domainToTest.endsWith(mediaServer.getDomain())) {
					return false;
				}
			}
		} catch (MalformedURLException e) {
			return false;
		}

		return true;
	}
}
