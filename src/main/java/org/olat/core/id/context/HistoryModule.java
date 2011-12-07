/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.core.id.context;

import org.olat.core.configuration.AbstractOLATModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.configuration.PersistedProperties;
import org.olat.core.util.StringHelper;

/**
 * 
 * <h3>Description:</h3>
 * <p>
 * <p>
 * Initial Date:  26 jan. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
public class HistoryModule extends AbstractOLATModule implements ConfigOnOff {

	private static final String BACK_ENABLED_PROP = "back.enabled";
	private static final String BACK_ENABLED_DEFAULT_PROP = "back.enabled.default";
	private static final String MOD_ENABLED_PROP = "history.enabled";
	private static final String RESUME_ENABLED_PROP = "resume.enabled";
	private static final String RESUME_ENABLED_DEFAULT_PROP = "resume.enabled.default";

	private boolean enabled;
	private boolean backEnabled;
	private boolean backDefaultSetting;
	private boolean resumeEnabled;
	private String resumeDefaultSetting;
	
	/**
	 * [used by Spring]
	 */
	private HistoryModule() {
		//
	}

	@Override
	public void init() {
		//back enabled/disabled
		String enabledObj = getStringPropertyValue(MOD_ENABLED_PROP, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
		
		String backObj = getStringPropertyValue(BACK_ENABLED_PROP, true);
		if(StringHelper.containsNonWhitespace(backObj)) {
			backEnabled = "true".equals(backObj);
		}
		
		String backDefSettings = getStringPropertyValue(BACK_ENABLED_DEFAULT_PROP, true);
		if(StringHelper.containsNonWhitespace(backDefSettings)) {
			backDefaultSetting = "true".equals(backDefSettings);
		}

		String resumeObj = getStringPropertyValue(RESUME_ENABLED_PROP, true);
		if(StringHelper.containsNonWhitespace(resumeObj)) {
			resumeEnabled = "true".equals(resumeObj);
		}
		
		String resumeDefSettings = getStringPropertyValue(RESUME_ENABLED_DEFAULT_PROP, true);
		if(StringHelper.containsNonWhitespace(resumeDefSettings)) {
			resumeDefaultSetting = resumeDefSettings;
		}
		
		logInfo("Back/resume module is enabled: " + Boolean.toString(enabled));
	}

	@Override
	protected void initDefaultProperties() {
		enabled = getBooleanConfigParameter(MOD_ENABLED_PROP, true);
		backEnabled = getBooleanConfigParameter(BACK_ENABLED_PROP, true);
		backDefaultSetting = getBooleanConfigParameter(BACK_ENABLED_DEFAULT_PROP, true);
		resumeEnabled = getBooleanConfigParameter(RESUME_ENABLED_PROP, true);
		resumeDefaultSetting = getStringConfigParameter(RESUME_ENABLED_DEFAULT_PROP, "ondemand", true);
	}

	@Override
	protected void initFromChangedProperties() {
		init();
	}
	
	@Override
	public void setPersistedProperties(PersistedProperties persistedProperties) {
		this.moduleConfigProperties = persistedProperties;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	public boolean isBackEnabled() {
		return enabled && backEnabled;
	}

	public boolean isBackDefaultSetting() {
		return backDefaultSetting;
	}

	public boolean isResumeEnabled() {
		return enabled && resumeEnabled;
	}
	
	public String getResumeDefaultSetting() {
		return resumeDefaultSetting;
	}

	public void setResumeDefaultSetting(String resumeDefaultSetting) {
		this.resumeDefaultSetting = resumeDefaultSetting;
	}

	public void setEnabled(boolean enabled) {
		if(this.enabled != enabled) {
			setStringProperty(BACK_ENABLED_PROP, Boolean.toString(enabled), true);
		}
	}
}
