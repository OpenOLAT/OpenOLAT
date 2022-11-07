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
package org.olat.repository.bulk;

import java.util.List;
import java.util.Set;

import org.olat.core.commons.services.license.ResourceLicense;
import org.olat.core.id.Organisation;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.bulk.model.SettingsContext;
import org.olat.repository.bulk.model.SettingsSteps;

/**
 * 
 * Initial date: 24 Oct 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface SettingsBulkEditables {
	
	public static final String DEFAULT_KEY = "editables";
	
	public boolean isLicensesEnabled();

	public boolean isEducationalTypeEnabled();
	
	public boolean isToolCalendarEnabled();
	
	public boolean isToolTeamsEnables();
	
	public boolean isToolBigBlueButtonEnabled();
	
	public boolean isToolZoomEnabled();
	
	public boolean isEditable();
	
	public boolean isEditable(SettingsSteps.Step step);

	public boolean isEditable(SettingsBulkEditable editable);
	
	public boolean isChanged(SettingsContext context, SettingsBulkEditable editable);
	
	public boolean isChanged(SettingsContext context, SettingsBulkEditable editable, RepositoryEntry repositoryEntry);

	public List<RepositoryEntry> getChanges(SettingsContext context, SettingsBulkEditable editable);

	public List<RepositoryEntry> getTaxonomyLevelAddChanges(Long taxonomyLevelKey);

	public List<RepositoryEntry> getTaxonomyLevelRemoveChanges(Long taxonomyLevelKey);

	public List<RepositoryEntry> getOrganisationAddChanges(Long organisationKey);

	public List<RepositoryEntry> getOrganisationRemoveChanges(Long organisationKey, Set<Long> organisationRemoveKeys);
	
	public ResourceLicense getLicense(RepositoryEntry repositoryEntry);
	
	public Set<TaxonomyLevel> getTaxonomyLevels(RepositoryEntry repositoryEntry);
	
	public Set<Organisation> getOrganisations(RepositoryEntry repositoryEntry);
	
}
