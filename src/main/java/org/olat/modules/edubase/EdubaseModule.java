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
package org.olat.modules.edubase;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 *
 * Initial date: 11.07.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class EdubaseModule extends AbstractSpringModule implements ConfigOnOff {

	public static final String EDUBASE_ENABLED = "edubase.enabled";
	public static final String EDUBASE_OAUTH_KEY = "edubase.oauthKey";
	public static final String EDUBASE_OAUTH_SECRET = "edubase.oauth";
	public static final String EDUBASE_READER_URL = "edubase.readerUrl";
	public static final String EDUBASE_READER_URL_UNIQUE = "edubase.readerUrl.unique";
	public static final String EDUBASE_LTI_LAUNCH_URL = "edubase.ltiLaunchUrl";
	public static final String EDUBASE_INFOVER_URL = "edubase.infoverUrl";
	public static final String EDUBASE_COVER_URL = "edubase.coverUrl";

	@Value("${edubase.enabled:false}")
	private boolean enabled;
	private String oauthKey;
	private String oauthSecret;
	@Value("${edubase.readerUrl}")
	private String readerUrl;
	@Value("${edubase.readerUrl.unique:true}")
	private boolean readerUrlUnique;
	@Value("${edubase.ltiLaunchUrl}")
	private String ltiLaunchUrl;
	@Value("${edubase.infoverUrl}")
	private String infoverUrl;
	@Value("${edubase.coverUrl}")
	private String coverUrl;

	@Autowired
	public EdubaseModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void init() {
		String enabledObj = getStringPropertyValue(EDUBASE_ENABLED, true);
		if (StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}

		String oauthKeyObj = getStringPropertyValue(EDUBASE_OAUTH_KEY, true);
		if (StringHelper.containsNonWhitespace(oauthKeyObj)) {
			oauthKey = oauthKeyObj;
		}

		String oauthObj = getStringPropertyValue(EDUBASE_OAUTH_SECRET, true);
		if (StringHelper.containsNonWhitespace(oauthObj)) {
			oauthSecret = oauthObj;
		}

		String readerUrlObj = getStringPropertyValue(EDUBASE_READER_URL, true);
		if (StringHelper.containsNonWhitespace(readerUrlObj)) {
			readerUrl = readerUrlObj;
		}
		
		String readerUrlUniqueObj = getStringPropertyValue(EDUBASE_READER_URL_UNIQUE, true);
		if(StringHelper.containsNonWhitespace(readerUrlUniqueObj)) {
			readerUrlUnique = "true".equals(readerUrlUniqueObj);
		}

		String ltiLaunchUrlObj = getStringPropertyValue(EDUBASE_LTI_LAUNCH_URL, true);
		if (StringHelper.containsNonWhitespace(ltiLaunchUrlObj)) {
			ltiLaunchUrl = ltiLaunchUrlObj;
		}

		String infoverUrlObj = getStringPropertyValue(EDUBASE_INFOVER_URL, true);
		if (StringHelper.containsNonWhitespace(infoverUrlObj)) {
			infoverUrl = infoverUrlObj;
		}
		
		String coverUrlObj = getStringPropertyValue(EDUBASE_COVER_URL, true);
		if (StringHelper.containsNonWhitespace(coverUrlObj)) {
			coverUrl = coverUrlObj;
		}
	}

	@Override
	protected void initFromChangedProperties() {
		init();
	}

	public String getOauthKey() {
		return oauthKey;
	}

	public void setOauthKey(String oauthKey) {
		this.oauthKey = oauthKey;
		setStringProperty(EDUBASE_OAUTH_KEY, oauthKey, true);
	}

	public String getOauthSecret() {
		return oauthSecret;
	}

	public void setOauthSecret(String oauthSecret) {
		this.oauthSecret = oauthSecret;
		setStringProperty(EDUBASE_OAUTH_SECRET, oauthSecret, true);
	}

	public String getReaderUrl() {
		return readerUrl;
	}

	public void setReaderUrl(String readerUrl) {
		this.readerUrl = readerUrl;
		setStringProperty(EDUBASE_READER_URL, readerUrl, true);
	}

	public boolean isReaderUrlUnique() {
		return readerUrlUnique;
	}

	public void setReaderUrlUnique(boolean readerUrlUnique) {
		this.readerUrlUnique = readerUrlUnique;
	}

	public String getLtiLaunchUrl() {
		return ltiLaunchUrl;
	}

	public void setLtiLaunchUrl(String ltiLaunchUrl) {
		this.ltiLaunchUrl = ltiLaunchUrl;
		setStringProperty(EDUBASE_LTI_LAUNCH_URL, ltiLaunchUrl, true);
	}

	public String getInfoverUrl() {
		return infoverUrl;
	}

	public void setInfoverUrl(String infoverUrl) {
		this.infoverUrl = infoverUrl;
		setStringProperty(EDUBASE_INFOVER_URL, infoverUrl, true);
	}

	public String getCoverUrl() {
		return coverUrl;
	}

	public void setCoverUrl(String coverUrl) {
		this.coverUrl = coverUrl;
		setStringProperty(EDUBASE_COVER_URL, coverUrl, true);
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		setStringProperty(EDUBASE_ENABLED, Boolean.toString(enabled), true);
	}

	public String getLtiBaseUrl() {
		String ltiBaseUrl = ltiLaunchUrl;
		if (ltiBaseUrl.endsWith("/")) {
			ltiBaseUrl = ltiBaseUrl.substring(0, ltiBaseUrl.length()-1);
		}
		return ltiBaseUrl;
	}

}
