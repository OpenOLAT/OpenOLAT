/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.ui.importwizard;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;

/**
 * 
 * Initial date: 20 févr. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ImportedUserRow extends AbstractImportRow {

	private final String organisationIdentifier;
	private final LocalDateTime creationDate;
	private final String[] identityProps;
	private final String password;
	
	private Identity identity;
	private Organisation organisation;
	private Map<String,CurriculumImportedValue> validationHandlersMap;
	
	
	
	public ImportedUserRow(int rowNum, String[] identityProps, String organisationIdentifier, String password, LocalDateTime creationDate) {
		super(rowNum);
		this.organisationIdentifier = organisationIdentifier;
		this.creationDate = creationDate;
		this.identityProps = identityProps;
		this.password = password;
	}
	
	public Identity getIdentity() {
		return identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	public String getUsername() {
		if(identityProps != null && identityProps.length > 0) {
			return identityProps[0];
		}
		return null;
	}
	
	public String getPassword() {
		return password;
	}
	
	public String[] getIdentityProps() {
		return identityProps;
	}
	
	public String getIdentityProp(int index) {
		if(identityProps != null && index >= 0 && index < identityProps.length) {
			return identityProps[index];
		}
		return null;
	}

	public String getOrganisationIdentifier() {
		return organisationIdentifier;
	}

	public Organisation getOrganisation() {
		return organisation;
	}

	public void setOrganisation(Organisation organisation) {
		this.organisation = organisation;
	}

	public LocalDateTime getCreationDate() {
		return creationDate;
	}
	
	@Override
	public List<CurriculumImportedValue> getValues() {
		List<CurriculumImportedValue> allValues = new ArrayList<>();
		if(validationHandlersMap != null) {
			allValues.addAll(validationHandlersMap.values());
		}
		List<CurriculumImportedValue> colValues = super.getValues();
		if(colValues != null && !colValues.isEmpty()) {
			allValues.addAll(colValues);
		}
		return allValues;
	}
	
	public CurriculumImportedValue getHandlerValidation(String name) {
		if(validationHandlersMap == null) return null;
		return validationHandlersMap.get(name);
	}
	
	public void addValidationWarning(String handlerName, String column, String placeholder, String message) {
		if(validationHandlersMap == null) {
			validationHandlersMap = new HashMap<>();
		}
		validationHandlersMap.computeIfAbsent(handlerName, c -> new CurriculumImportedValue(column))
			.setWarning(placeholder, message);
	}
	
	public void addValidationError(String handlerName, String column, String placeholder, String message) {
		if(validationHandlersMap == null) {
			validationHandlersMap = new HashMap<>();
		}
		validationHandlersMap.computeIfAbsent(handlerName, c -> new CurriculumImportedValue(column))
			.setError(placeholder, message);
	}
	
	@Override
	public boolean hasValidationErrors() {
		return hasHandlersValidationErrors() || super.hasValidationErrors();
	}
	
	public boolean hasHandlersValidationErrors() {
		return validationHandlersMap != null
				&& validationHandlersMap.values().stream().anyMatch(v -> v.isError());
	}

	@Override
	public CurriculumImportedStatistics getValidationStatistics() {
		CurriculumImportedStatistics statistics = super.getValidationStatistics();
		
		if(validationHandlersMap != null && !validationHandlersMap.isEmpty()) {
			int errors = statistics.errors();
			int warnings = statistics.warnings();
			int changes = statistics.changes();
			for(CurriculumImportedValue val:validationHandlersMap.values()) {
				if(val.isChanged()) {
					changes++;
				}
				if(val.isError()) {
					errors++;
				} else if(val.isWarning()) {
					warnings++;
				}
			}
			statistics = new CurriculumImportedStatistics(errors, warnings, changes);
		}
		
		return statistics;
	}
}
