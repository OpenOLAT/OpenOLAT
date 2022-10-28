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
package org.olat.repository.bulk.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.services.license.License;
import org.olat.core.commons.services.license.ResourceLicense;
import org.olat.core.id.Organisation;
import org.olat.course.CourseModule;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.bulk.SettingsBulkEditable;
import org.olat.repository.bulk.SettingsBulkEditables;
import org.olat.repository.bulk.model.SettingsSteps.Step;

/**
 * 
 * Initial date: 24 Oct 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DefaultSettingsBulkEditables implements SettingsBulkEditables {
	
	private final List<RepositoryEntry> repositoryEntries;
	private final Map<Long, RepositoryEntryInfo> reKeyToInfo;
	private final boolean licenseEnabled;
	private final String noLicenseKey;
	private final boolean courseSelected;
	private final List<SettingsSteps.Step> steps;
	
	public DefaultSettingsBulkEditables(List<RepositoryEntry> repositoryEntries,
			Map<Long, RepositoryEntryInfo> reKeyToInfo, boolean licenseEnabled, String noLicenseKey,
			boolean taxonomyEnabled, boolean organisationEnabled) {
		this.licenseEnabled = licenseEnabled;
		this.noLicenseKey = noLicenseKey;
		this.repositoryEntries = repositoryEntries;
		this.reKeyToInfo = reKeyToInfo;
		this.courseSelected =repositoryEntries.stream()
				.anyMatch(entry -> CourseModule.ORES_TYPE_COURSE.equals(entry.getOlatResource().getResourceableTypeName()));
		
		steps = new ArrayList<>(SettingsSteps.SELECTABLE_STEPS_SIZE);
		if (isOneEditable(SettingsSteps.getEditables(SettingsSteps.Step.metadata))) {
			steps.add((SettingsSteps.Step.metadata));
		}
		if (taxonomyEnabled && isOneEditable(SettingsSteps.getEditables(SettingsSteps.Step.taxonomy))) {
			steps.add(SettingsSteps.Step.taxonomy);
		}
		if (organisationEnabled && isOneEditable(SettingsSteps.getEditables(SettingsSteps.Step.organisation))) {
			steps.add((SettingsSteps.Step.organisation));
		}
		if (isOneEditable(SettingsSteps.getEditables(SettingsSteps.Step.authorRights))) {
			steps.add((SettingsSteps.Step.authorRights));
		}
	}

	@Override
	public boolean isLicensesEnabled() {
		return licenseEnabled;
	}
	
	@Override
	public boolean isEducationalTypeEnabled() {
		return courseSelected;
	}
	
	@Override
	public boolean isEditable() {
		return !steps.isEmpty();
	}

	@Override
	public boolean isEditable(Step step) {
		return steps.contains(step);
	}
	
	private boolean isOneEditable(Collection<SettingsBulkEditable> editable) {
		return editable == null || editable.isEmpty() || editable.stream().anyMatch(this::isEditable);
	}

	@Override
	public boolean isEditable(SettingsBulkEditable editable) {
		return repositoryEntries.stream().anyMatch(entry -> isEditable(editable, entry));
	}

	private boolean isEditable(SettingsBulkEditable editable, RepositoryEntry repositoryEntry) {
		switch (editable) {
		case authors:
			return !RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.details);
		case educationalType: 
			return !RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.details)
					&& CourseModule.ORES_TYPE_COURSE.equals(repositoryEntry.getOlatResource().getResourceableTypeName());
		case mainLanguage:
			return !RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.details);
		case expenditureOfWork:
			return !RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.details);
		case license:
			return !RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.details);
		case licensor:
			return !RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.details);
		case authorRightReference:
			return !RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.settings);
		case authorRightCopy:
			return !RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.settings);
		case authorRightDownload:
			return !RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.settings);
		case taxonomyLevelsAdd: 
			return !RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.details);
		case taxonomyLevelsRemove:
			return !RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.details);
		case organisationsAdd: 
			return !RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.organisations);
		case organisationsRemove:
			return !RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.organisations)
					&& reKeyToInfo.get(repositoryEntry.getKey()).getOrganisationKeys().size() > 1;
		default:
			break;
		}
		return false;
	}

	@Override
	public boolean isChanged(SettingsContext context, SettingsBulkEditable editable) {
		return repositoryEntries.stream().anyMatch(entry -> isChanged(context, editable, entry));
	}

	@Override
	public boolean isChanged(SettingsContext context, SettingsBulkEditable editable, RepositoryEntry repositoryEntry) {
		if (!isEditable(editable, repositoryEntry)) {
			return false;
		}
		
		switch (editable) {
		case authors:
			return !Objects.equals(context.getAuthors(), repositoryEntry.getAuthors());
		case educationalType: 
			return !Objects.equals(context.getEducationalTypeKey(), getEducationalTypeKey(repositoryEntry));
		case mainLanguage:
			return !Objects.equals(context.getMainLanguage(), repositoryEntry.getMainLanguage());
		case expenditureOfWork:
			return !Objects.equals(context.getExpenditureOfWork(), repositoryEntry.getExpenditureOfWork());
		case license:
			return isLicenseChanged(repositoryEntry, context.getLicenseTypeKey(), context.getFreetext());
		case licensor:
			return isLicensorChanged(repositoryEntry, context.getLicensor());
		case authorRightReference:
			return repositoryEntry.getCanReference() != context.isAuthorRightReference();
		case authorRightCopy:
			return repositoryEntry.getCanCopy() != context.isAuthorRightCopy();
		case authorRightDownload:
			return repositoryEntry.getCanDownload() != context.isAuthorRightDownload();
		case taxonomyLevelsAdd: 
			return !hasTaxonomyLevelsToAdd(repositoryEntry, context.getTaxonomyLevelAddKeys());
		case taxonomyLevelsRemove:
			return hasTaxonomyLevelsToRemove(repositoryEntry, context.getTaxonomyLevelRemoveKeys());
		case organisationsAdd: 
			return hasOrganisationsToAdd(repositoryEntry, context.getOrganisationAddKeys());
		case organisationsRemove:
			return hasOrganisationsToRemove(repositoryEntry, context.getOrganisationRemoveKeys());
		default:
			break;
		}
		return false;
	}

	private Long getEducationalTypeKey(RepositoryEntry repositoryEntry) {
		return repositoryEntry.getEducationalType() != null? repositoryEntry.getEducationalType().getKey(): null;
	}
	
	private boolean isLicenseChanged(RepositoryEntry repositoryEntry, String licenseTypeKey, String freetext) {
		License license = reKeyToInfo.get(repositoryEntry.getKey()).getLicense();
		
		if (license == null && (licenseTypeKey == null || licenseTypeKey.equals(noLicenseKey))) {
			return false;
		} else if (license == null) {
			return true;
		} else if (!license.getLicenseType().getKey().toString().equals(licenseTypeKey)) {
			return true;
		}
		return !Objects.equals(license.getFreetext(), freetext);
	}

	private boolean isLicensorChanged(RepositoryEntry repositoryEntry, String licensor) {
		License license = reKeyToInfo.get(repositoryEntry.getKey()).getLicense();
		String currentLicensor = license != null? license.getLicensor(): null;
		return !Objects.equals(currentLicensor, licensor);
	}
	
	private boolean hasTaxonomyLevelsToAdd(RepositoryEntry repositoryEntry, Set<Long> taxonomyAddKeys) {
		if (taxonomyAddKeys != null && !taxonomyAddKeys.isEmpty()) {
			Set<Long> taxonomyAddKeysCopy = new HashSet<>(taxonomyAddKeys);
			Set<Long> taxonomyCurrentKeys = reKeyToInfo.get(repositoryEntry.getKey()).getTaxonomyLevelKeys();
			taxonomyAddKeysCopy.removeAll(taxonomyCurrentKeys);
			return !taxonomyCurrentKeys.isEmpty();
		}
		return false;
	}
	
	private boolean hasTaxonomyLevelsToRemove(RepositoryEntry repositoryEntry, Set<Long> taxonomyRemoveKeys) {
		if (taxonomyRemoveKeys != null && !taxonomyRemoveKeys.isEmpty()) {
			Set<Long> taxonomyCurrentKeys = new HashSet<>(reKeyToInfo.get(repositoryEntry.getKey()).getTaxonomyLevelKeys());
			if (taxonomyCurrentKeys.isEmpty()) {
				return false;
			}
			taxonomyCurrentKeys.removeAll(taxonomyRemoveKeys);
			return !taxonomyCurrentKeys.isEmpty();
		}
		return false;
	}
	
	private boolean hasOrganisationsToAdd(RepositoryEntry repositoryEntry, Set<Long> organisationAddKeys) {
		if (organisationAddKeys != null && !organisationAddKeys.isEmpty()) {
			Set<Long> organisationAddKeysCopy = new HashSet<>(organisationAddKeys);
			Set<Long> organisationCurrentKeys = reKeyToInfo.get(repositoryEntry.getKey()).getOrganisationKeys();
			organisationAddKeysCopy.removeAll(organisationCurrentKeys);
			return !organisationCurrentKeys.isEmpty();
		}
		return false;
	}
	
	private boolean hasOrganisationsToRemove(RepositoryEntry repositoryEntry, Set<Long> organisationRemoveKeys) {
		if (organisationRemoveKeys != null && !organisationRemoveKeys.isEmpty()) {
			Set<Long> organisationCurrentKeys = new HashSet<>(reKeyToInfo.get(repositoryEntry.getKey()).getOrganisationKeys());
			// Must have at least one organisation.
			if (organisationCurrentKeys.size() <= 1) {
				return false;
			}
			organisationCurrentKeys.removeAll(organisationRemoveKeys);
			return !organisationCurrentKeys.isEmpty();
		}
		return false;
	}

	@Override
	public List<RepositoryEntry> getChanges(SettingsContext context, SettingsBulkEditable editable) {
		return repositoryEntries.stream().filter(entry -> isChanged(context, editable, entry)).collect(Collectors.toList());
	}

	@Override
	public List<RepositoryEntry> getTaxonomyLevelAddChanges(Long taxonomyLevelKey) {
		return repositoryEntries.stream()
				.filter(entry -> !reKeyToInfo.get(entry.getKey()).getTaxonomyLevelKeys().contains(taxonomyLevelKey))
				.collect(Collectors.toList());
	}

	@Override
	public List<RepositoryEntry> getTaxonomyLevelRemoveChanges(Long taxonomyLevelKey) {
		return repositoryEntries.stream()
				.filter(entry -> reKeyToInfo.get(entry.getKey()).getTaxonomyLevelKeys().contains(taxonomyLevelKey))
				.collect(Collectors.toList());
	}

	@Override
	public List<RepositoryEntry> getOrganisationAddChanges(Long organisationKey) {
		return repositoryEntries.stream()
				.filter(entry -> !reKeyToInfo.get(entry.getKey()).getOrganisationKeys().contains(organisationKey))
				.collect(Collectors.toList());
	}

	@Override
	public List<RepositoryEntry> getOrganisationRemoveChanges(Long organisationKey, Set<Long> organisationRemoveKeys) {
		return repositoryEntries.stream()
				.filter(entry -> (reKeyToInfo.get(entry.getKey()).getOrganisationKeys().contains(organisationKey)
						&& hasOrganisationsToRemove(entry, organisationRemoveKeys)))
				.collect(Collectors.toList());
	}
	@Override
	public ResourceLicense getLicense(RepositoryEntry repositoryEntry) {
		return reKeyToInfo.get(repositoryEntry.getKey()).getLicense();
	}

	@Override
	public Set<TaxonomyLevel> getTaxonomyLevels(RepositoryEntry repositoryEntry) {
		return reKeyToInfo.get(repositoryEntry.getKey()).getTaxonomyLevels();
	}

	@Override
	public Set<Organisation> getOrganisations(RepositoryEntry repositoryEntry) {
		return reKeyToInfo.get(repositoryEntry.getKey()).getOrganisations();
	}


}
