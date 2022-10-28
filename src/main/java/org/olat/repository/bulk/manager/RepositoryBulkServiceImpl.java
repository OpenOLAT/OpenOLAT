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
package org.olat.repository.bulk.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.model.OrganisationRefImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.license.LicenseModule;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.ResourceLicense;
import org.olat.core.commons.services.license.manager.LicenseTypeDAO;
import org.olat.core.id.Organisation;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyModule;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.bulk.RepositoryBulkService;
import org.olat.repository.bulk.SettingsBulkEditable;
import org.olat.repository.bulk.SettingsBulkEditables;
import org.olat.repository.bulk.model.DefaultSettingsBulkEditables;
import org.olat.repository.bulk.model.RepositoryEntryInfo;
import org.olat.repository.bulk.model.SettingsContext;
import org.olat.repository.manager.RepositoryEntryLicenseHandler;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 17 Oct 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class RepositoryBulkServiceImpl implements RepositoryBulkService {
	
	@Autowired
	private DB dbInsance;
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private LicenseModule licenseModule;
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private RepositoryEntryLicenseHandler licenseHandler;
	@Autowired
	private TaxonomyModule taxonomyModule;
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private OrganisationService organisationService;
	

	@Override
	public SettingsBulkEditables getSettingsBulkEditables(List<RepositoryEntry> repositoryEntries) {
		Map<Long, RepositoryEntryInfo> reKeyToInfo = repositoryEntries.stream()
				.collect(Collectors.toMap(RepositoryEntry::getKey, entry -> new RepositoryEntryInfo(entry.getKey())));
		
		boolean licenseEnabled = licenseModule.isEnabled(licenseHandler) && !licenseService.loadActiveLicenseTypes(licenseHandler).isEmpty();
		String noLicenseKey = null;
		if (licenseEnabled) {
			noLicenseKey = licenseService.loadLicenseTypeByName(LicenseTypeDAO.NO_LICENSE_NAME).getKey().toString();
			Map<Long, ResourceLicense> reKeyToLicense = getRepositoryEntryKeyToLicense(repositoryEntries);
			for (Entry<Long, ResourceLicense> keyToLicense : reKeyToLicense.entrySet()) {
				reKeyToInfo.get(keyToLicense.getKey()).setLicense(keyToLicense.getValue());
			}
		}
		
		boolean taxonomyEnabled = taxonomyModule.isEnabled() && !repositoryModule.getTaxonomyRefs().isEmpty();
		if (taxonomyEnabled) {
			Map<RepositoryEntryRef, List<TaxonomyLevel>> entryRefToTaxonomyLevel = repositoryService.getTaxonomy(repositoryEntries, false);
			for (Entry<RepositoryEntryRef, List<TaxonomyLevel>> entryToLevels : entryRefToTaxonomyLevel.entrySet()) {
				reKeyToInfo.get(entryToLevels.getKey().getKey()).setTaxonomyLevels(Set.copyOf(entryToLevels.getValue()));
			}
		}
		
		boolean organisationEnabled = organisationModule.isEnabled();
		if (organisationEnabled) {
			Map<RepositoryEntryRef, List<Organisation>> entryRefToOrganisation = repositoryService.getRepositoryEntryOrganisations(repositoryEntries);
			for (Entry<RepositoryEntryRef, List<Organisation>> entryToOrganisation : entryRefToOrganisation.entrySet()) {
				reKeyToInfo.get(entryToOrganisation.getKey().getKey()).setOrganisations(Set.copyOf(entryToOrganisation.getValue()));
			}
		}
		
		return new DefaultSettingsBulkEditables(
				repositoryEntries,
				reKeyToInfo,
				licenseEnabled,
				noLicenseKey,
				taxonomyEnabled,
				organisationEnabled
				);
	}
	
	@Override
	public void update(SettingsContext context) {
		List<RepositoryEntry> entries = repositoryService.loadByKeys(context.getRepositoryEntries().stream().map(RepositoryEntry::getKey).collect(Collectors.toSet()));
		SettingsBulkEditables editables = getSettingsBulkEditables(entries);
		
		RepositoryEntryEducationalType educationalType = isSelectedAndChanged(context, editables, SettingsBulkEditable.educationalType)
				? repositoryManager.getEducationalType(context.getEducationalTypeKey())
				: null;
		
		List<TaxonomyLevel> taxonomyLevelsAdd = context.getTaxonomyLevelAddKeys() != null
				? taxonomyService.getTaxonomyLevelsByKeys(context.getTaxonomyLevelAddKeys())
				: null;
		
		List<Organisation> organisationsAdd = context.getOrganisationAddKeys() != null
				? organisationService.getOrganisation(context.getOrganisationAddKeys().stream().map(OrganisationRefImpl::new).collect(Collectors.toList()))
				: null;
		
		LicenseType licenseType = null;
		if (isSelectedAndChanged(context, editables, SettingsBulkEditable.license)) {
			licenseType = licenseService.loadLicenseTypeByKey(context.getLicenseTypeKey());
		}
		
		for (RepositoryEntry repositoryEntry : entries) {
			RepositoryEntry updatedEntry = updateRepositoryEntry(context, editables, repositoryEntry, educationalType, taxonomyLevelsAdd, organisationsAdd);
			updatedEntry =updateRepositoryEntryAccess(context, editables, updatedEntry);
			updateLicense(context, editables, updatedEntry, licenseType);
			dbInsance.commit();
		}
	}

	private RepositoryEntry updateRepositoryEntry(SettingsContext context, SettingsBulkEditables editables,
			RepositoryEntry repositoryEntry, RepositoryEntryEducationalType educationalType,
			List<TaxonomyLevel> taxonomyLevelsAdd, List<Organisation> organisationsAdd) {
		boolean changed = false;
		
		if (isSelectedAndChanged(context, editables, SettingsBulkEditable.authors, repositoryEntry)) {
			repositoryEntry.setAuthors(context.getAuthors());
			changed = true;
		}
		if (isSelectedAndChanged(context, editables,SettingsBulkEditable.educationalType, repositoryEntry)) {
			repositoryEntry.setEducationalType(educationalType);
			changed = true;
		}
		if (isSelectedAndChanged(context, editables, SettingsBulkEditable.mainLanguage, repositoryEntry)) {
			repositoryEntry.setMainLanguage(context.getMainLanguage());
			changed = true;
		}
		if (isSelectedAndChanged(context, editables, SettingsBulkEditable.expenditureOfWork, repositoryEntry)) {
			repositoryEntry.setExpenditureOfWork(context.getExpenditureOfWork());
			changed = true;
		}
		
		Set<TaxonomyLevel> taxonomyLevels = null;
		if (isSelectedAndChanged(context, editables,SettingsBulkEditable.taxonomyLevelsAdd, repositoryEntry)) {
			taxonomyLevels = new HashSet<>(editables.getTaxonomyLevels(repositoryEntry));
			taxonomyLevels.addAll(taxonomyLevelsAdd);
			changed = true;
		}
		if (isSelectedAndChanged(context, editables,SettingsBulkEditable.taxonomyLevelsRemove, repositoryEntry)) {
			if (taxonomyLevels == null) {
				taxonomyLevels = new HashSet<>(editables.getTaxonomyLevels(repositoryEntry));
			}
			taxonomyLevels.removeIf(level -> context.getTaxonomyLevelRemoveKeys().contains(level.getKey()));
			changed = true;
		}
		
		List<Organisation> organisations = null;
		if (isSelectedAndChanged(context, editables,SettingsBulkEditable.organisationsAdd, repositoryEntry)) {
			organisations = new ArrayList<>(editables.getOrganisations(repositoryEntry));
			organisations.addAll(organisationsAdd);
			changed = true;
		}
		if (isSelectedAndChanged(context, editables,SettingsBulkEditable.organisationsRemove, repositoryEntry)) {
			if (organisations == null) {
				organisations = new ArrayList<>(editables.getOrganisations(repositoryEntry));
			}
			organisations.removeIf(level -> context.getOrganisationRemoveKeys().contains(level.getKey()));
			changed = true;
		}
		
		if (changed) {
			return repositoryManager.setDescriptionAndName(repositoryEntry, repositoryEntry.getDisplayname(),
					repositoryEntry.getExternalRef(), repositoryEntry.getAuthors(), repositoryEntry.getDescription(),
					repositoryEntry.getTeaser(), repositoryEntry.getObjectives(), repositoryEntry.getRequirements(),
					repositoryEntry.getCredits(), repositoryEntry.getMainLanguage(), repositoryEntry.getLocation(),
					repositoryEntry.getExpenditureOfWork(), repositoryEntry.getLifecycle(), organisations,
					taxonomyLevels, repositoryEntry.getEducationalType());
		}
		return repositoryEntry;
	}

	private RepositoryEntry updateRepositoryEntryAccess(SettingsContext context, SettingsBulkEditables editables,
			RepositoryEntry repositoryEntry) {
		boolean changed = false;
		
		boolean canReference = repositoryEntry.getCanReference();
		if (isSelectedAndChanged(context, editables,SettingsBulkEditable.authorRightReference, repositoryEntry)) {
			canReference = context.isAuthorRightReference();
			changed = true;
		}
		boolean canCopy = repositoryEntry.getCanCopy();
		if (isSelectedAndChanged(context, editables,SettingsBulkEditable.authorRightCopy, repositoryEntry)) {
			canCopy = context.isAuthorRightCopy();
			changed = true;
		}
		boolean canDownload = repositoryEntry.getCanDownload();
		if (isSelectedAndChanged(context, editables,SettingsBulkEditable.authorRightDownload, repositoryEntry)) {
			canDownload = context.isAuthorRightDownload();
			changed = true;
		}
		
		if (changed) {
			return repositoryManager.setAccess(repositoryEntry, repositoryEntry.isPublicVisible(),
					repositoryEntry.getAllowToLeaveOption(), canCopy, canReference, canDownload, null);
		}
		return repositoryEntry;
	}

	private void updateLicense(SettingsContext context, SettingsBulkEditables editables,
			RepositoryEntry repositoryEntry, LicenseType licenseType) {
		boolean changed = false;
	
		ResourceLicense license = editables.getLicense(repositoryEntry);
		if (isSelectedAndChanged(context, editables, SettingsBulkEditable.license, repositoryEntry)) {
			if (license == null) {
				license = licenseService.createDefaultLicense(repositoryEntry.getOlatResource(), licenseHandler, null);
			}
			license.setLicenseType(licenseType);
			license.setFreetext(context.getFreetext());
			changed = true;
		}
		if (isSelectedAndChanged(context, editables, SettingsBulkEditable.license, repositoryEntry)) {
			if (license != null) {
				license.setLicensor(context.getLicensor());
				changed = true;
			}
		}
		
		if (changed) {
			licenseService.update(license);
		}
	}
	
	private boolean isSelectedAndChanged(SettingsContext context, SettingsBulkEditables editables, SettingsBulkEditable editable) {
		return context.isSelected(editable) && editables.isChanged(context, editable);
	}
	
	private boolean isSelectedAndChanged(SettingsContext context, SettingsBulkEditables editables, SettingsBulkEditable editable, RepositoryEntry repositoryEntry) {
		return context.isSelected(editable) && editables.isChanged(context, editable, repositoryEntry);
	}
	
	private Map<Long, ResourceLicense> getRepositoryEntryKeyToLicense(Collection<RepositoryEntry> repositoryEntries) {
		Map<Long, OLATResource> entryKeyToOres = repositoryEntries.stream().collect(Collectors.toMap(RepositoryEntry::getKey, RepositoryEntry::getOlatResource));
		Map<Long, ResourceLicense> oresKeyToLicense = licenseService.loadLicenses(entryKeyToOres.values()).stream().collect(Collectors.toMap(ResourceLicense::getResId, Function.identity()));
		Map<Long, ResourceLicense> reKeyToLicense = new HashMap<>(oresKeyToLicense.size());
		for (Entry<Long, ResourceLicense> keyToLicense : oresKeyToLicense.entrySet()) {
			Long oresKKey = keyToLicense.getKey();
			Long reKey = entryKeyToOres.entrySet().stream().filter(entrySet -> entrySet.getValue().getResourceableId().equals(oresKKey)).findFirst().get().getKey();
			reKeyToLicense.put(reKey, keyToLicense.getValue());
		}
		return reKeyToLicense;
	}

}
