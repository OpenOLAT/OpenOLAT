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
package org.olat.core.commons.services.doceditor.office365;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 26.04.2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class Office365Module extends AbstractSpringModule implements ConfigOnOff {

	private static final String OFFICE365_ENABLED = "office365.enabled";
	private static final String OFFICE365_BASE_URL = "office365.baseUrl";
	private static final String OFFICE365_DATA_TRANSER_CONFIRMATION_ENABLED = "office365.data.transfer.confirmation.enabled";
	private static final String OFFICE365_USAGE_AUTHORS = "office365.usage.authors";
	private static final String OFFICE365_USAGE_COACHES = "office365.usage.coaches";
	private static final String OFFICE365_USAGE_MANAGERS = "office365.usage.managers";
	
	@Value("${office365.enabled:false}")
	private boolean enabled;
	@Value("${office365.baseUrl}")
	private String baseUrl;
	@Value("${office365.data.transfer.confirmation.enabled:false}")
	private boolean dataTransferConfirmationEnabled;
	@Value("${office365.usage.restricted.authors:false}")
	private boolean usageRestrictedToAuthors;
	@Value("${office365.usage.restricted.coaches:false}")
	private boolean usageRestrictedToCoaches;
	@Value("${office365.usage.restricted.managers:false}")
	private boolean usageRestrictedToManagers;
	
	@Autowired
	private Office365Module(CoordinatorManager coordinateManager) {
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
		String enabledObj = getStringPropertyValue(OFFICE365_ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
		
		String baseUrlObj = getStringPropertyValue(OFFICE365_BASE_URL, true);
		if(StringHelper.containsNonWhitespace(baseUrlObj)) {
			baseUrl = baseUrlObj;
		}
		
		String dataTransferConfirmationEnabledObj = getStringPropertyValue(OFFICE365_DATA_TRANSER_CONFIRMATION_ENABLED, true);
		if(StringHelper.containsNonWhitespace(dataTransferConfirmationEnabledObj)) {
			dataTransferConfirmationEnabled = "true".equals(dataTransferConfirmationEnabledObj);
		}
		
		String usageRestrictedToAuthorsObj = getStringPropertyValue(OFFICE365_USAGE_AUTHORS, true);
		if(StringHelper.containsNonWhitespace(usageRestrictedToAuthorsObj)) {
			usageRestrictedToAuthors = "true".equals(usageRestrictedToAuthorsObj);
		}
		
		String usageRestrictedToCoachesObj = getStringPropertyValue(OFFICE365_USAGE_COACHES, true);
		if(StringHelper.containsNonWhitespace(usageRestrictedToCoachesObj)) {
			usageRestrictedToCoaches = "true".equals(usageRestrictedToCoachesObj);
		}
		
		String usageRestrictedToManagersObj = getStringPropertyValue(OFFICE365_USAGE_MANAGERS, true);
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
		setStringProperty(OFFICE365_ENABLED, Boolean.toString(enabled), true);
	}
	
	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
		setStringProperty(OFFICE365_BASE_URL, baseUrl, true);
	}

	public boolean isDataTransferConfirmationEnabled() {
		return dataTransferConfirmationEnabled;
	}

	public void setDataTransferConfirmationEnabled(boolean dataTransferConfirmationEnabled) {
		this.dataTransferConfirmationEnabled = dataTransferConfirmationEnabled;
		setStringProperty(OFFICE365_DATA_TRANSER_CONFIRMATION_ENABLED, Boolean.toString(dataTransferConfirmationEnabled), true);
	}

	public boolean isUsageRestricted() {
		return usageRestrictedToAuthors || usageRestrictedToCoaches || usageRestrictedToManagers;
	}

	public boolean isUsageRestrictedToAuthors() {
		return usageRestrictedToAuthors;
	}

	public void setUsageRestrictedToAuthors(boolean usageRestrictedToAuthors) {
		this.usageRestrictedToAuthors = usageRestrictedToAuthors;
		setStringProperty(OFFICE365_USAGE_AUTHORS, Boolean.toString(usageRestrictedToAuthors), true);
	}

	public boolean isUsageRestrictedToCoaches() {
		return usageRestrictedToCoaches;
	}

	public void setUsageRestrictedToCoaches(boolean usageRestrictedToCoaches) {
		this.usageRestrictedToCoaches = usageRestrictedToCoaches;
		setStringProperty(OFFICE365_USAGE_COACHES, Boolean.toString(usageRestrictedToCoaches), true);
	}

	public boolean isUsageRestrictedToManagers() {
		return usageRestrictedToManagers;
	}

	public void setUsageRestrictedToManagers(boolean usageRestrictedToManagers) {
		this.usageRestrictedToManagers = usageRestrictedToManagers;
		setStringProperty(OFFICE365_USAGE_MANAGERS, Boolean.toString(usageRestrictedToManagers), true);
	}
}
