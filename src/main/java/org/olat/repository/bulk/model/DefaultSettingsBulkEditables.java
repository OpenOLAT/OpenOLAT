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
import java.util.Date;
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
import org.olat.course.config.CourseConfig;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.bulk.SettingsBulkEditable;
import org.olat.repository.bulk.SettingsBulkEditables;
import org.olat.repository.bulk.model.SettingsContext.LifecycleType;
import org.olat.repository.bulk.model.SettingsContext.Replacement;
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
	private final boolean toolCalendarEnabled;
	private final boolean toolTeamsEnables;
	private final boolean toolBigBlueButtonEnabled;
	private final boolean toolZoomEnabled;
	private final boolean courseSelected;
	private final List<SettingsSteps.Step> steps;
	
	public DefaultSettingsBulkEditables(List<RepositoryEntry> repositoryEntries,
			Map<Long, RepositoryEntryInfo> reKeyToInfo, boolean licenseEnabled, String noLicenseKey,
			boolean taxonomyEnabled, boolean organisationEnabled, boolean toolCalendarEnabled, boolean toolTeamsEnables,
			boolean toolBigBlueButtonEnabled, boolean toolZoomEnabled) {
		this.licenseEnabled = licenseEnabled;
		this.noLicenseKey = noLicenseKey;
		this.toolCalendarEnabled = toolCalendarEnabled;
		this.toolTeamsEnables = toolTeamsEnables;
		this.toolBigBlueButtonEnabled = toolBigBlueButtonEnabled;
		this.toolZoomEnabled = toolZoomEnabled;
		this.repositoryEntries = repositoryEntries;
		this.reKeyToInfo = reKeyToInfo;
		this.courseSelected =repositoryEntries.stream().anyMatch(this::isCourse);
		
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
		if (courseSelected && isOneEditable(SettingsSteps.getEditables(SettingsSteps.Step.execution))) {
			steps.add((SettingsSteps.Step.execution));
		}
		if (courseSelected && isOneEditable(SettingsSteps.getEditables(SettingsSteps.Step.toolbar))) {
			steps.add((SettingsSteps.Step.toolbar));
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
	public boolean isToolCalendarEnabled() {
		return toolCalendarEnabled;
	}

	@Override
	public boolean isToolTeamsEnables() {
		return toolTeamsEnables;
	}

	@Override
	public boolean isToolBigBlueButtonEnabled() {
		return toolBigBlueButtonEnabled;
	}

	@Override
	public boolean isToolZoomEnabled() {
		return toolZoomEnabled;
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
					&& isCourse(repositoryEntry);
		case mainLanguage:
			return !RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.details);
		case expenditureOfWork:
			return !RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.details);
		case location:
			return !RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.location);
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
		case lifecycleType: 
			return !RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.details)
					&& isCourse(repositoryEntry);
		case lifecyclePublicKey: 
			return !RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.details)
					&& isCourse(repositoryEntry);
		case lifecycleValidFrom: 
			return !RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.details)
					&& isCourse(repositoryEntry);
		case lifecycleValidTo: 
			return !RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.details)
					&& isCourse(repositoryEntry);
		case toolSearch:
			return !RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.search)
					&& isCourse(repositoryEntry) && !isCourseLocked(repositoryEntry);
		case toolCalendar:
			return !RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.calendar)
					&& toolCalendarEnabled && isCourse(repositoryEntry) && !isCourseLocked(repositoryEntry);
		case toolParticipantList:
			return !RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.participantList)
					&& isCourse(repositoryEntry) && !isCourseLocked(repositoryEntry);
		case toolParticipantInfo:
			return !RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.participantInfo)
					&& isCourse(repositoryEntry) && !isCourseLocked(repositoryEntry);
		case toolEmail:
			return !RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.email)
					&& isCourse(repositoryEntry) && !isCourseLocked(repositoryEntry);
		case toolTeams:
			return !RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.teams)
					&& toolTeamsEnables && isCourse(repositoryEntry) && !isCourseLocked(repositoryEntry);
		case toolBigBlueButton:
			return !RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.bigbluebutton)
					&& toolBigBlueButtonEnabled && isCourse(repositoryEntry) && !isCourseLocked(repositoryEntry);
		case toolBigBlueButtonModeratorStartsMeeting:
			return !RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.bigbluebutton)
					&& toolBigBlueButtonEnabled && isCourse(repositoryEntry) && !isCourseLocked(repositoryEntry);
		case toolZoom:
			return !RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.zoom)
					&& toolZoomEnabled && isCourse(repositoryEntry) && !isCourseLocked(repositoryEntry);
		case toolBlog:
			return !RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.blog)
					&& isCourse(repositoryEntry) && !isCourseLocked(repositoryEntry);
		case toolWiki:
			return !RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.wiki)
					&& isCourse(repositoryEntry) && !isCourseLocked(repositoryEntry);
		case toolForum:
			return !RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.forum)
					&& isCourse(repositoryEntry) && !isCourseLocked(repositoryEntry);
		case toolDocuments:
			return !RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.documents)
					&& isCourse(repositoryEntry) && !isCourseLocked(repositoryEntry);
		case toolChat:
			return !RepositoryEntryManagedFlag.isManaged(repositoryEntry, RepositoryEntryManagedFlag.chat)
					&& isCourse(repositoryEntry) && !isCourseLocked(repositoryEntry);
		default:
			break;
		}
		return false;
	}

	private boolean isCourse(RepositoryEntry repositoryEntry) {
		return CourseModule.ORES_TYPE_COURSE.equals(repositoryEntry.getOlatResource().getResourceableTypeName());
	}

	private boolean isCourseLocked(RepositoryEntry repositoryEntry) {
		return reKeyToInfo.get(repositoryEntry.getKey()).isCourseLocked();
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
		case location:
			return !Objects.equals(context.getLocation(), repositoryEntry.getLocation());
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
		case lifecycleType:
			return hasLifecycleTypeChange(repositoryEntry, context.getLifecycleType());
		case lifecyclePublicKey:
			return hasLifecyclePublicKeyChange(repositoryEntry, context.getLifecycleType(), context.getLifecyclePublicKey());
		case lifecycleValidFrom:
			return hasLifecycleValidFromChange(repositoryEntry, context.getLifecycleType(), context.getLifecycleValidFrom());
		case lifecycleValidTo:
			return hasLifecycleValidToChange(repositoryEntry, context.getLifecycleType(), context.getLifecycleValidTo());
		case toolSearch:
			return context.isToolSearch() != reKeyToInfo.get(repositoryEntry.getKey()).getCourseConfig().isCourseSearchEnabled();
		case toolCalendar:
			return context.isToolCalendar() != reKeyToInfo.get(repositoryEntry.getKey()).getCourseConfig().isCalendarEnabled();
		case toolParticipantList:
			return context.isToolParticipantList() != reKeyToInfo.get(repositoryEntry.getKey()).getCourseConfig().isParticipantListEnabled();
		case toolParticipantInfo:
			return context.isToolParticipantInfo() != reKeyToInfo.get(repositoryEntry.getKey()).getCourseConfig().isParticipantInfoEnabled();
		case toolEmail:
			return context.isToolEmail() != reKeyToInfo.get(repositoryEntry.getKey()).getCourseConfig().isEmailEnabled();
		case toolTeams:
			return context.isToolTeams() != reKeyToInfo.get(repositoryEntry.getKey()).getCourseConfig().isTeamsEnabled();
		case toolBigBlueButton:
			return context.isToolBigBlueButton() != reKeyToInfo.get(repositoryEntry.getKey()).getCourseConfig().isBigBlueButtonEnabled();
		case toolBigBlueButtonModeratorStartsMeeting:
			return context.isToolBigBlueButtonModeratorStartsMeeting() != reKeyToInfo.get(repositoryEntry.getKey()).getCourseConfig().isBigBlueButtonModeratorStartsMeeting();
		case toolZoom:
			return context.isToolZoom() != reKeyToInfo.get(repositoryEntry.getKey()).getCourseConfig().isZoomEnabled();
		case toolBlog:
			return hasToolBlogChanged(repositoryEntry, context);
		case toolWiki:
			return hasToolWikiChanged(repositoryEntry, context);
		case toolForum:
			return context.isToolForum() != reKeyToInfo.get(repositoryEntry.getKey()).getCourseConfig().isForumEnabled();
		case toolDocuments:
			return context.isToolDocuments() != reKeyToInfo.get(repositoryEntry.getKey()).getCourseConfig().isDocumentsEnabled();
		case toolChat:
			return context.isToolChat() != reKeyToInfo.get(repositoryEntry.getKey()).getCourseConfig().isChatEnabled();
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

	private boolean hasLifecycleTypeChange(RepositoryEntry repositoryEntry, LifecycleType lifecycleType) {
		if ((LifecycleType.none == lifecycleType || lifecycleType == null) && repositoryEntry.getLifecycle() == null) {
			return false;
		}
		if (LifecycleType.publicCycle == lifecycleType && repositoryEntry.getLifecycle() != null && !repositoryEntry.getLifecycle().isPrivateCycle()) {
			return false;
		}
		if (LifecycleType.privateCycle == lifecycleType && repositoryEntry.getLifecycle() != null && repositoryEntry.getLifecycle().isPrivateCycle()) {
			return false;
		}
		return true;
	}

	private boolean hasLifecyclePublicKeyChange(RepositoryEntry repositoryEntry, LifecycleType lifecycleType, Long lifecyclePublicKey) {
		if (LifecycleType.none == lifecycleType || LifecycleType.privateCycle == lifecycleType) {
			return false;
		}
		if (LifecycleType.publicCycle == lifecycleType) {
			return repositoryEntry.getLifecycle() == null
					|| repositoryEntry.getLifecycle().isPrivateCycle()
					|| !Objects.equals(lifecyclePublicKey, repositoryEntry.getLifecycle().getKey());
		}
		return !repositoryEntry.getLifecycle().isPrivateCycle()
				&& !Objects.equals(lifecyclePublicKey, repositoryEntry.getLifecycle().getKey());
	}

	private boolean hasLifecycleValidFromChange(RepositoryEntry repositoryEntry, LifecycleType lifecycleType, Date lifecycleValidFrom) {
		if (LifecycleType.none == lifecycleType || LifecycleType.publicCycle == lifecycleType) {
			return false;
		}
		if (LifecycleType.privateCycle == lifecycleType) {
			return repositoryEntry.getLifecycle() == null
					|| !repositoryEntry.getLifecycle().isPrivateCycle()
					|| !Objects.equals(lifecycleValidFrom, repositoryEntry.getLifecycle().getValidFrom());
		}
		return repositoryEntry.getLifecycle() == null
				|| (repositoryEntry.getLifecycle().isPrivateCycle()
						&& !Objects.equals(lifecycleValidFrom, repositoryEntry.getLifecycle().getValidFrom()));
	}

	private boolean hasLifecycleValidToChange(RepositoryEntry repositoryEntry, LifecycleType lifecycleType, Date lifecycleValidTo) {
		if (LifecycleType.none == lifecycleType || LifecycleType.publicCycle == lifecycleType) {
			return false;
		}
		if (LifecycleType.privateCycle == lifecycleType) {
			return repositoryEntry.getLifecycle() == null
					|| !repositoryEntry.getLifecycle().isPrivateCycle()
					|| !Objects.equals(lifecycleValidTo, repositoryEntry.getLifecycle().getValidTo());
		}
		return repositoryEntry.getLifecycle() == null
				|| (repositoryEntry.getLifecycle().isPrivateCycle()
						&& !Objects.equals(lifecycleValidTo, repositoryEntry.getLifecycle().getValidTo()));
	}

	private boolean hasToolBlogChanged(RepositoryEntry repositoryEntry, SettingsContext context) {
		Replacement replacement = context.getToolBlog();
		if (replacement == null) {
			return false;
		}
		CourseConfig courseConfig = reKeyToInfo.get(repositoryEntry.getKey()).getCourseConfig();
		switch (replacement) {
		case add: return !courseConfig.isBlogEnabled();
		case change: return courseConfig.isBlogEnabled() && !Objects.equals(context.getToolBlogKey(), courseConfig.getBlogSoftKey());
		case addChange: return !courseConfig.isBlogEnabled() || !Objects.equals(context.getToolBlogKey(), courseConfig.getBlogSoftKey());
		case remove: return courseConfig.isBlogEnabled();
		default: return false;
		}
	}

	private boolean hasToolWikiChanged(RepositoryEntry repositoryEntry, SettingsContext context) {
		Replacement replacement = context.getToolWiki();
		if (replacement == null) {
			return false;
		}
		CourseConfig courseConfig = reKeyToInfo.get(repositoryEntry.getKey()).getCourseConfig();
		switch (replacement) {
		case add: return !courseConfig.isWikiEnabled();
		case change: return courseConfig.isWikiEnabled() && !Objects.equals(context.getToolWikiKey(), courseConfig.getWikiSoftKey());
		case addChange: return !courseConfig.isWikiEnabled() || !Objects.equals(context.getToolWikiKey(), courseConfig.getWikiSoftKey());
		case remove: return courseConfig.isWikiEnabled();
		default: return false;
		}
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
