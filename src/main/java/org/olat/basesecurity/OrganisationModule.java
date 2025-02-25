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

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 9 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class OrganisationModule extends AbstractSpringModule implements ConfigOnOff {
	
	private static final String ORGANISATION_ENABLED = "organisation.enabled";
	private static final String EMAIL_DOMAIN_ENABLED = "email.domain.enabled";
	private static final String LEGAL_FOLDER_ENABLED = "legal.folder.enabled";
	
	@Value("${organisation.enabled:true}")
	private boolean enabled;
	@Value("${organisation.managed.enabled:true}")
	private boolean managedEnabled;
	@Value("${organisation.email.domain.enabled:false}")
	private boolean emailDomainEnabled;
	@Value("${organisation.legal.folder.enabled:false}")
	private boolean legalFolderEnabled;

	@Autowired
	public OrganisationModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
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
		String enabledObj = getStringPropertyValue(ORGANISATION_ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
		
		String emailDomainEnabledObj = getStringPropertyValue(EMAIL_DOMAIN_ENABLED, true);
		if(StringHelper.containsNonWhitespace(emailDomainEnabledObj)) {
			emailDomainEnabled = "true".equals(emailDomainEnabledObj);
		}
		
		String legalFolderEnabledObj = getStringPropertyValue(LEGAL_FOLDER_ENABLED, true);
		if(StringHelper.containsNonWhitespace(legalFolderEnabledObj)) {
			legalFolderEnabled = "true".equals(legalFolderEnabledObj);
		}
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		setStringProperty(ORGANISATION_ENABLED, Boolean.toString(enabled), true);
	}
	
	public boolean isOrganisationManaged() {
		return managedEnabled;
	}

	public boolean isEmailDomainEnabled() {
		return emailDomainEnabled;
	}

	public void setEmailDomainEnabled(boolean emailDomainEnabled) {
		this.emailDomainEnabled = emailDomainEnabled;
		setStringProperty(EMAIL_DOMAIN_ENABLED, Boolean.toString(emailDomainEnabled), true);
	}

	public boolean isLegalFolderEnabled() {
		return legalFolderEnabled;
	}

	public void setLegalFolderEnabled(boolean legalFolderEnabled) {
		this.legalFolderEnabled = legalFolderEnabled;
		setStringProperty(LEGAL_FOLDER_ENABLED, Boolean.toString(legalFolderEnabled), true);
	}
	
}
