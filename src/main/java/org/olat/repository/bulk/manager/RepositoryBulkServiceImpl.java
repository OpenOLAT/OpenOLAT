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
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.model.OrganisationRefImpl;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarModule;
import org.olat.commons.calendar.ui.events.CalendarGUIModifiedEvent;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.license.LicenseModule;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.ResourceLicense;
import org.olat.core.commons.services.license.manager.LicenseTypeDAO;
import org.olat.core.gui.components.Window;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.logging.activity.ILoggingAction;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.config.CourseConfig;
import org.olat.course.config.CourseConfigEvent;
import org.olat.course.config.CourseConfigEvent.CourseConfigType;
import org.olat.modules.bigbluebutton.BigBlueButtonModule;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyModule;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.teams.TeamsModule;
import org.olat.modules.zoom.ZoomModule;
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
import org.olat.repository.bulk.model.SettingsContext.LifecycleType;
import org.olat.repository.bulk.model.SettingsContext.Replacement;
import org.olat.repository.bulk.model.SettingsSteps;
import org.olat.repository.bulk.model.SettingsSteps.Step;
import org.olat.repository.manager.RepositoryEntryLicenseHandler;
import org.olat.repository.manager.RepositoryEntryLifecycleDAO;
import org.olat.repository.model.RepositoryEntryLifecycle;
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
	@Autowired
	private RepositoryEntryLifecycleDAO lifecycleDao;
	@Autowired
	private CalendarModule calendarModule;
	@Autowired
	private TeamsModule teamsModule;
	@Autowired
	private BigBlueButtonModule bigBlueButtonModule;
	@Autowired
	private ZoomModule zoomModule;
	

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
		
		for (RepositoryEntry repositoryEntry: repositoryEntries) {
			OLATResource resource = repositoryEntry.getOlatResource();
			if (CourseModule.ORES_TYPE_COURSE.equals(resource.getResourceableTypeName())) {
				boolean locked = CoordinatorManager.getInstance().getCoordinator().getLocker().isLocked(resource, CourseFactory.COURSE_EDITOR_LOCK);
				reKeyToInfo.get(repositoryEntry.getKey()).setCourseLocked(locked);
				ICourse course = CourseFactory.loadCourse(resource);
				reKeyToInfo.get(repositoryEntry.getKey()).setCourseConfig(course.getCourseConfig());
			}
		}
		
		boolean toolCalendarEnabled = calendarModule.isEnabled() && calendarModule.isEnableCourseToolCalendar();
		boolean toolTeamsEnables = teamsModule.isEnabled() && teamsModule.isCoursesEnabled();
		boolean toolBigBlueButtonEnabled = bigBlueButtonModule.isEnabled() && bigBlueButtonModule.isCoursesEnabled();
		boolean toolZoomEnabled = zoomModule.isEnabled() && zoomModule.isEnabledForCourseTool();
		
		return new DefaultSettingsBulkEditables(
				repositoryEntries,
				reKeyToInfo,
				licenseEnabled,
				noLicenseKey,
				taxonomyEnabled,
				organisationEnabled,
				toolCalendarEnabled,
				toolTeamsEnables,
				toolBigBlueButtonEnabled,
				toolZoomEnabled
				);
	}
	
	@Override
	public void update(Window window, Identity identity, SettingsContext context) {
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
		
		RepositoryEntryLifecycle publicLifecycle = null;
		if (isSelectedAndChanged(context, editables, SettingsBulkEditable.lifecycleType) && context.getLifecycleType() == LifecycleType.publicCycle) {
			publicLifecycle = lifecycleDao.loadById(context.getLifecyclePublicKey());
		}
		
		String verifiedBlogSoftKey = null;
		if (isSelectedAndChanged(context, editables, SettingsBulkEditable.toolBlog) && Replacement.remove != context.getToolBlog()) {
			RepositoryEntry blogEntry = repositoryManager.lookupRepositoryEntryBySoftkey(context.getToolBlogKey(), false);
			if (blogEntry != null) {
				verifiedBlogSoftKey = blogEntry.getSoftkey();
			}
		}
		String verifiedWikiSoftKey = null;
		if (isSelectedAndChanged(context, editables, SettingsBulkEditable.toolWiki) && Replacement.remove != context.getToolWiki()) {
			RepositoryEntry wikiEntry = repositoryManager.lookupRepositoryEntryBySoftkey(context.getToolWikiKey(), false);
			if (wikiEntry != null) {
				verifiedWikiSoftKey = wikiEntry.getSoftkey();
			}
		}
		
		for (RepositoryEntry repositoryEntry : entries) {
			RepositoryEntry updatedEntry = updateRepositoryEntry(context, editables, repositoryEntry, educationalType,
					taxonomyLevelsAdd, organisationsAdd, publicLifecycle);
			updatedEntry = updateRepositoryEntryAccess(context, editables, updatedEntry);
			updateLicense(context, editables, updatedEntry, licenseType);
			updateCourse(window, identity, context, editables, updatedEntry, verifiedBlogSoftKey, verifiedWikiSoftKey);
			dbInsance.commit();
		}
	}

	private RepositoryEntry updateRepositoryEntry(SettingsContext context, SettingsBulkEditables editables,
			RepositoryEntry repositoryEntry, RepositoryEntryEducationalType educationalType,
			List<TaxonomyLevel> taxonomyLevelsAdd, List<Organisation> organisationsAdd,
			RepositoryEntryLifecycle publicLifecycle) {
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
		if (isSelectedAndChanged(context, editables, SettingsBulkEditable.location, repositoryEntry)) {
			repositoryEntry.setLocation(context.getLocation());
			changed = true;
		}
		
		Set<TaxonomyLevel> taxonomyLevels = null;
		if (isSelectedAndChanged(context, editables, SettingsBulkEditable.taxonomyLevelsAdd, repositoryEntry)) {
			taxonomyLevels = new HashSet<>(editables.getTaxonomyLevels(repositoryEntry));
			taxonomyLevels.addAll(taxonomyLevelsAdd);
			changed = true;
		}
		if (isSelectedAndChanged(context, editables, SettingsBulkEditable.taxonomyLevelsRemove, repositoryEntry)) {
			if (taxonomyLevels == null) {
				taxonomyLevels = new HashSet<>(editables.getTaxonomyLevels(repositoryEntry));
			}
			taxonomyLevels.removeIf(level -> context.getTaxonomyLevelRemoveKeys().contains(level.getKey()));
			changed = true;
		}
		
		List<Organisation> organisations = null;
		if (isSelectedAndChanged(context, editables, SettingsBulkEditable.organisationsAdd, repositoryEntry)) {
			organisations = new ArrayList<>(editables.getOrganisations(repositoryEntry));
			organisations.addAll(organisationsAdd);
			changed = true;
		}
		if (isSelectedAndChanged(context, editables, SettingsBulkEditable.organisationsRemove, repositoryEntry)) {
			if (organisations == null) {
				organisations = new ArrayList<>(editables.getOrganisations(repositoryEntry));
			}
			organisations.removeIf(level -> context.getOrganisationRemoveKeys().contains(level.getKey()));
			changed = true;
		}
		
		RepositoryEntryLifecycle lifecycle = repositoryEntry.getLifecycle();
		if (isSelectedAndChanged(context, editables, SettingsBulkEditable.lifecycleType, repositoryEntry)) {
			if (context.getLifecycleType() == null || context.getLifecycleType() == LifecycleType.none) {
				lifecycle = null;
				changed = true;
			} else if (context.getLifecycleType() == LifecycleType.publicCycle) {
				if (publicLifecycle != null) {
					lifecycle = publicLifecycle;
					changed = true;
				}
			} else if (context.getLifecycleType() == LifecycleType.privateCycle) {
				if (lifecycle == null || !lifecycle.isPrivateCycle()) {
					String softKey = "lf_" + repositoryEntry.getSoftkey();
					lifecycle = lifecycleDao.create(repositoryEntry.getDisplayname(), softKey, true, context.getLifecycleValidFrom(), context.getLifecycleValidTo());
					changed = true;
				} else {
					lifecycle.setValidFrom(context.getLifecycleValidFrom());
					lifecycle.setValidTo(context.getLifecycleValidTo());
					lifecycle = lifecycleDao.updateLifecycle(lifecycle);
					changed = true;
				}
			}
		}
		repositoryEntry.setLifecycle(lifecycle);
		
		if (changed) {
			return repositoryManager.setDescriptionAndName(repositoryEntry, repositoryEntry.getDisplayname(),
					repositoryEntry.getExternalRef(), repositoryEntry.getAuthors(), repositoryEntry.getDescription(),
					repositoryEntry.getTeaser(), repositoryEntry.getObjectives(), repositoryEntry.getRequirements(),
					repositoryEntry.getCredits(), repositoryEntry.getMainLanguage(), repositoryEntry.getLocation(),
					repositoryEntry.getExpenditureOfWork(), repositoryEntry.getLifecycle(), organisations, taxonomyLevels,
					repositoryEntry.getEducationalType());
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
			license.setLicensor(context.getLicensor());
			changed = true;
		}
		
		if (changed) {
			licenseService.update(license);
		}
	}

	private void updateCourse(Window window, Identity identity, SettingsContext context,
			SettingsBulkEditables editables, RepositoryEntry repositoryEntry, String verifiedBlogSoftKey,
			String verifiedWikiSoftKey) {
		
		boolean changed = SettingsSteps.getEditables(Step.toolbar).stream()
				.anyMatch(editable -> isSelectedAndChanged(context, editables, editable));
		if (!changed) {
			return;
		}
		
		LockResult lockEntry = CoordinatorManager.getInstance().getCoordinator().getLocker()
				.acquireLock(repositoryEntry.getOlatResource(), identity, CourseFactory.COURSE_EDITOR_LOCK, window);
		if (lockEntry == null || !lockEntry.isSuccess()) {
			return;
		}
		
		OLATResourceable courseOres = repositoryEntry.getOlatResource();
		ICourse course = CourseFactory.openCourseEditSession(courseOres.getResourceableId());
		CourseConfig courseConfig = course.getCourseEnvironment().getCourseConfig();
		
		if (isSelectedAndChanged(context, editables, SettingsBulkEditable.toolSearch, repositoryEntry)) {
			courseConfig.setCourseSearchEnabled(context.isToolSearch());
			
			ILoggingAction loggingAction = context.isToolSearch() ?
					LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_COURSESEARCH_ENABLED:
					LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_COURSESEARCH_DISABLED;
			ThreadLocalUserActivityLogger.log(loggingAction, getClass());
			
			CoordinatorManager.getInstance().getCoordinator().getEventBus()
				.fireEventToListenersOf(new CourseConfigEvent(CourseConfigType.search, course.getResourceableId()), course);
		}
		
		if (isSelectedAndChanged(context, editables, SettingsBulkEditable.toolCalendar, repositoryEntry)) {
			courseConfig.setCalendarEnabled(context.isToolCalendar());
			
			ILoggingAction loggingAction = context.isToolCalendar() ?
					LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_CALENDAR_ENABLED:
					LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_CALENDAR_DISABLED;
			
			ThreadLocalUserActivityLogger.log(loggingAction, getClass());
			CoordinatorManager.getInstance().getCoordinator().getEventBus()
				.fireEventToListenersOf(new CalendarGUIModifiedEvent(), OresHelper.lookupType(CalendarManager.class));
			CoordinatorManager.getInstance().getCoordinator().getEventBus()
				.fireEventToListenersOf(new CourseConfigEvent(CourseConfigType.calendar, course.getResourceableId()), course);
		}
		
		if (isSelectedAndChanged(context, editables, SettingsBulkEditable.toolParticipantList, repositoryEntry)) {
			courseConfig.setParticipantListEnabled(context.isToolParticipantList());
			
			ILoggingAction loggingAction = context.isToolParticipantList() ?
					LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_PARTICIPANTLIST_ENABLED:
					LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_PARTICIPANTLIST_DISABLED;
			ThreadLocalUserActivityLogger.log(loggingAction, getClass());
			
			CoordinatorManager.getInstance().getCoordinator().getEventBus()
				.fireEventToListenersOf(new CourseConfigEvent(CourseConfigType.participantList, course.getResourceableId()), course);
		}
		
		if (isSelectedAndChanged(context, editables, SettingsBulkEditable.toolParticipantInfo, repositoryEntry)) {
			courseConfig.setParticipantInfoEnabled(context.isToolParticipantInfo());
			
			ILoggingAction loggingAction = context.isToolParticipantInfo() ?
					LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_PARTICIPANTINFO_ENABLED:
					LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_PARTICIPANTINFO_DISABLED;
			ThreadLocalUserActivityLogger.log(loggingAction, getClass());
			
			CoordinatorManager.getInstance().getCoordinator().getEventBus()
				.fireEventToListenersOf(new CourseConfigEvent(CourseConfigType.participantInfo, course.getResourceableId()), course);
		}
		
		if (isSelectedAndChanged(context, editables, SettingsBulkEditable.toolEmail, repositoryEntry)) {
			courseConfig.setEmailEnabled(context.isToolEmail());
			
			ILoggingAction loggingAction = context.isToolEmail() ?
					LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_EMAIL_ENABLED:
					LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_EMAIL_DISABLED;
			ThreadLocalUserActivityLogger.log(loggingAction, getClass());
			
			CoordinatorManager.getInstance().getCoordinator().getEventBus()
				.fireEventToListenersOf(new CourseConfigEvent(CourseConfigType.email, course.getResourceableId()), course);
		}
		
		if (isSelectedAndChanged(context, editables, SettingsBulkEditable.toolTeams, repositoryEntry)) {
			courseConfig.setTeamsEnabled(context.isToolTeams());
			
			ILoggingAction loggingAction = context.isToolTeams() ?
					LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_TEAMS_ENABLED:
					LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_TEAMS_DISABLED;
			ThreadLocalUserActivityLogger.log(loggingAction, getClass());
			
			CoordinatorManager.getInstance().getCoordinator().getEventBus()
				.fireEventToListenersOf(new CourseConfigEvent(CourseConfigType.teams, course.getResourceableId()), course);
		}
		
		boolean bigBlueButtonChanged = false;
		if (isSelectedAndChanged(context, editables, SettingsBulkEditable.toolBigBlueButton, repositoryEntry)) {
			courseConfig.setBigBlueButtonEnabled(context.isToolBigBlueButton());
			courseConfig.setBigBlueButtonModeratorStartsMeeting(context.isToolBigBlueButtonModeratorStartsMeeting());
			bigBlueButtonChanged = true;
		}
		if (bigBlueButtonChanged) {
			ILoggingAction loggingAction = context.isToolBigBlueButton() ?
					LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_BIGBLUEBUTTON_ENABLED:
					LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_BIGBLUEBUTTON_DISABLED;
			ThreadLocalUserActivityLogger.log(loggingAction, getClass());
			
			CoordinatorManager.getInstance().getCoordinator().getEventBus()
				.fireEventToListenersOf(new CourseConfigEvent(CourseConfigType.bigbluebutton, course.getResourceableId()), course);
		}
		
		if (isSelectedAndChanged(context, editables, SettingsBulkEditable.toolZoom, repositoryEntry)) {
			courseConfig.setZoomEnabled(context.isToolZoom());
				
			ILoggingAction loggingAction = context.isToolZoom() ?
					LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_ZOOM_ENABLED :
					LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_ZOOM_DISABLED;
			ThreadLocalUserActivityLogger.log(loggingAction, getClass());

			CoordinatorManager.getInstance().getCoordinator().getEventBus()
					.fireEventToListenersOf(new CourseConfigEvent(CourseConfigType.zoom, course.getResourceableId()), course);
		}
		
		if (isSelectedAndChanged(context, editables, SettingsBulkEditable.toolBlog, repositoryEntry)) {
			Boolean enabled = null;
			Replacement replacement = context.getToolBlog();
			if (Replacement.remove == replacement) {
				courseConfig.setBlogEnabled(false);
				courseConfig.setBlogSoftKey(null);
				enabled = Boolean.FALSE;
			} else if (verifiedBlogSoftKey != null) {
				boolean changeBlog = false;
				if (Replacement.add == replacement && !courseConfig.isBlogEnabled()) {
					changeBlog = true;
				} else if (Replacement.change == replacement 
						&& courseConfig.isBlogEnabled() && !Objects.equals(context.getToolBlogKey(), courseConfig.getBlogSoftKey())) {
					changeBlog = true;
				} else if (Replacement.addChange == replacement
						&& (!courseConfig.isBlogEnabled() || !Objects.equals(context.getToolBlogKey(), courseConfig.getBlogSoftKey()))) {
					changeBlog = true;
				}
				if (changeBlog) {
					courseConfig.setBlogEnabled(true);
					courseConfig.setBlogSoftKey(verifiedBlogSoftKey);
					enabled = Boolean.TRUE;
				}
			}
			
			if (enabled != null) {
				ILoggingAction loggingAction = enabled.booleanValue()?
						LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_BLOG_ENABLED:
						LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_BLOG_DISABLED;
				ThreadLocalUserActivityLogger.log(loggingAction, getClass());
				
				CoordinatorManager.getInstance().getCoordinator().getEventBus()
					.fireEventToListenersOf(new CourseConfigEvent(CourseConfigType.blog, course.getResourceableId()), course);
			}
		}
		
		if (isSelectedAndChanged(context, editables, SettingsBulkEditable.toolWiki, repositoryEntry)) {
			Boolean enabled = null;
			Replacement replacement = context.getToolWiki();
			if (Replacement.remove == replacement) {
				courseConfig.setWikiEnabled(false);
				courseConfig.setWikiSoftKey(null);
				enabled = Boolean.FALSE;
			} else if (verifiedWikiSoftKey != null) {
				boolean changeWiki = false;
				if (Replacement.add == replacement && !courseConfig.isWikiEnabled()) {
					changeWiki = true;
				} else if (Replacement.change == replacement 
						&& courseConfig.isWikiEnabled() && !Objects.equals(context.getToolWikiKey(), courseConfig.getWikiSoftKey())) {
					changeWiki = true;
				} else if (Replacement.addChange == replacement
						&& (!courseConfig.isWikiEnabled() || !Objects.equals(context.getToolWikiKey(), courseConfig.getWikiSoftKey()))) {
					changeWiki = true;
				}
				if (changeWiki) {
					courseConfig.setWikiEnabled(true);
					courseConfig.setWikiSoftKey(verifiedWikiSoftKey);
					enabled = Boolean.TRUE;
				}
			}
			
			if (enabled != null) {
				ILoggingAction loggingAction = enabled.booleanValue() ?
						LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_WIKI_ENABLED:
						LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_WIKI_DISABLED;
				ThreadLocalUserActivityLogger.log(loggingAction, getClass());
				
				CoordinatorManager.getInstance().getCoordinator().getEventBus()
					.fireEventToListenersOf(new CourseConfigEvent(CourseConfigType.wiki, course.getResourceableId()), course);
			}
		}
		
		if (isSelectedAndChanged(context, editables, SettingsBulkEditable.toolForum, repositoryEntry)) {
			courseConfig.setForumEnabled(context.isToolForum());
			
			ILoggingAction loggingAction = context.isToolForum() ?
					LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_FORUM_ENABLED:
					LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_FORUM_DISABLED;
			ThreadLocalUserActivityLogger.log(loggingAction, getClass());
			
			CoordinatorManager.getInstance().getCoordinator().getEventBus()
				.fireEventToListenersOf(new CourseConfigEvent(CourseConfigType.forum, course.getResourceableId()), course);
			
		}
		
		if (isSelectedAndChanged(context, editables, SettingsBulkEditable.toolDocuments, repositoryEntry)) {
			courseConfig.setDocumentsEnabled(context.isToolDocuments());
			courseConfig.setDocumentPath(null);
			
			ILoggingAction loggingAction = context.isToolDocuments() ?
					LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_DOCUMENTS_ENABLED:
					LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_DOCUMENTS_DISABLED;
			ThreadLocalUserActivityLogger.log(loggingAction, getClass());
			
			CoordinatorManager.getInstance().getCoordinator().getEventBus()
				.fireEventToListenersOf(new CourseConfigEvent(CourseConfigType.documents, course.getResourceableId()), course);
		}
		
		if (isSelectedAndChanged(context, editables, SettingsBulkEditable.toolChat, repositoryEntry)) {
			courseConfig.setChatIsEnabled(context.isToolChat());
			
			ILoggingAction loggingAction = context.isToolChat() ?
					LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_IM_ENABLED:
					LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_IM_DISABLED;
			ThreadLocalUserActivityLogger.log(loggingAction, getClass());
			
			CoordinatorManager.getInstance().getCoordinator().getEventBus()
				.fireEventToListenersOf(new CourseConfigEvent(CourseConfigType.chat, course.getResourceableId()), course);
			
		}
		
		CourseFactory.setCourseConfig(course.getResourceableId(), courseConfig);
		CourseFactory.closeCourseEditSession(course.getResourceableId(), true);
		
		CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lockEntry);
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
