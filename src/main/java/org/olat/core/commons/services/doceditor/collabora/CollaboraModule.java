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
package org.olat.core.commons.services.doceditor.collabora;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 5 Mar 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CollaboraModule extends AbstractSpringModule implements ConfigOnOff {

	private static final String COLLABORA_ENABLED = "collabora.enabled";
	private static final String COLLABORA_BASE_URL = "collabora.baseUrl";
	private static final String COLLABORA_DATA_TRANSER_CONFIRMATION_ENABLED = "collabora.data.transfer.confirmation.enabled";
	private static final String COLLABORA_USAGE_AUTHORS = "collabora.usage.authors";
	private static final String COLLABORA_USAGE_COACHES = "collabora.usage.coaches";
	private static final String COLLABORA_USAGE_MANAGERS = "collabora.usage.managers";
	
	@Value("${collabora.enabled:false}")
	private boolean enabled;
	@Value("${collabora.baseUrl}")
	private String baseUrl;
	@Value("${collabora.data.transfer.confirmation.enabled:false}")
	private boolean dataTransferConfirmationEnabled;
	@Value("${collabora.usage.restricted.authors:false}")
	private boolean usageRestrictedToAuthors;
	@Value("${collabora.usage.restricted.coaches:false}")
	private boolean usageRestrictedToCoaches;
	@Value("${collabora.usage.restricted.managers:false}")
	private boolean usageRestrictedToManagers;
	
	@Autowired
	private CollaboraModule(CoordinatorManager coordinateManager) {
		super(coordinateManager);
	}

	@Override
	public void init() {
		updateProperties();
	}

	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}
	
	private void updateProperties() {
		String enabledObj = getStringPropertyValue(COLLABORA_ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
		
		String baseUrlObj = getStringPropertyValue(COLLABORA_BASE_URL, true);
		if(StringHelper.containsNonWhitespace(baseUrlObj)) {
			baseUrl = baseUrlObj;
		}
		
		String dataTransferConfirmationEnabledObj = getStringPropertyValue(COLLABORA_DATA_TRANSER_CONFIRMATION_ENABLED, true);
		if(StringHelper.containsNonWhitespace(dataTransferConfirmationEnabledObj)) {
			dataTransferConfirmationEnabled = "true".equals(dataTransferConfirmationEnabledObj);
		}
		
		String usageRestrictedToAuthorsObj = getStringPropertyValue(COLLABORA_USAGE_AUTHORS, true);
		if(StringHelper.containsNonWhitespace(usageRestrictedToAuthorsObj)) {
			usageRestrictedToAuthors = "true".equals(usageRestrictedToAuthorsObj);
		}
		
		String usageRestrictedToCoachesObj = getStringPropertyValue(COLLABORA_USAGE_COACHES, true);
		if(StringHelper.containsNonWhitespace(usageRestrictedToCoachesObj)) {
			usageRestrictedToCoaches = "true".equals(usageRestrictedToCoachesObj);
		}
		
		String usageRestrictedToManagersObj = getStringPropertyValue(COLLABORA_USAGE_MANAGERS, true);
		if(StringHelper.containsNonWhitespace(usageRestrictedToManagersObj)) {
			usageRestrictedToManagers = "true".equals(usageRestrictedToManagersObj);
		}
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		setStringProperty(COLLABORA_ENABLED, Boolean.toString(enabled), true);
	}
	
	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
		setStringProperty(COLLABORA_BASE_URL, baseUrl, true);
	}

	public boolean isDataTransferConfirmationEnabled() {
		return dataTransferConfirmationEnabled;
	}

	public void setDataTransferConfirmationEnabled(boolean dataTransferConfirmationEnabled) {
		this.dataTransferConfirmationEnabled = dataTransferConfirmationEnabled;
		setStringProperty(COLLABORA_DATA_TRANSER_CONFIRMATION_ENABLED, Boolean.toString(dataTransferConfirmationEnabled), true);
	}

	public boolean isUsageRestricted() {
		return usageRestrictedToAuthors || usageRestrictedToCoaches || usageRestrictedToManagers;
	}

	public boolean isUsageRestrictedToAuthors() {
		return usageRestrictedToAuthors;
	}

	public void setUsageRestrictedToAuthors(boolean usageRestrictedToAuthors) {
		this.usageRestrictedToAuthors = usageRestrictedToAuthors;
		setStringProperty(COLLABORA_USAGE_AUTHORS, Boolean.toString(usageRestrictedToAuthors), true);
	}

	public boolean isUsageRestrictedToCoaches() {
		return usageRestrictedToCoaches;
	}

	public void setUsageRestrictedToCoaches(boolean usageRestrictedToCoaches) {
		this.usageRestrictedToCoaches = usageRestrictedToCoaches;
		setStringProperty(COLLABORA_USAGE_COACHES, Boolean.toString(usageRestrictedToCoaches), true);
	}

	public boolean isUsageRestrictedToManagers() {
		return usageRestrictedToManagers;
	}

	public void setUsageRestrictedToManagers(boolean usageRestrictedToManagers) {
		this.usageRestrictedToManagers = usageRestrictedToManagers;
		setStringProperty(COLLABORA_USAGE_MANAGERS, Boolean.toString(usageRestrictedToManagers), true);
	}
	
}
