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
package org.olat.repository.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.admin.securitygroup.gui.IdentitiesAddEvent;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.ResourceLicense;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSManager;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.config.CourseConfig;
import org.olat.course.editor.overview.OverviewRow;
import org.olat.course.learningpath.LearningPathConfigs;
import org.olat.course.learningpath.LearningPathService;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.course.wizard.CourseDisclaimerContext;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.modules.reminder.ReminderService;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.repository.CatalogEntry;
import org.olat.repository.CopyService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryEntryToOrganisation;
import org.olat.repository.RepositoryEntryToTaxonomyLevel;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.repository.model.RepositoryEntryToGroupRelation;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial date: 25.06.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
@Service
public class CopyServiceImpl implements CopyService {

	@Autowired
	private DB dbInstance;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private RepositoryEntryRelationDAO reToGroupDao;
	@Autowired
	private OLATResourceManager resourceManager;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryEntryLifecycleDAO lifecycleDao;
	@Autowired
	private RepositoryEntryToOrganisationDAO repositoryEntryToOrganisationDao;
	@Autowired
	private RepositoryEntryToTaxonomyLevelDAO repositoryEntryToTaxonomyLevelDao;
	@Autowired
	private CatalogManager catalogManager;
	@Autowired
	private LearningPathService learningPathService;
	@Autowired
	private ReminderService reminderService;

	
	
	@Override
	public RepositoryEntry copyLearningPathCourse(CopyCourseContext context) {
		RepositoryEntry sourceEntry = context.getSourceRepositoryEntry();
		OLATResource sourceResource = sourceEntry.getOlatResource();
		OLATResource copyResource = resourceManager.createOLATResourceInstance(sourceResource.getResourceableTypeName());
		
		// For easier handling, put all nodes into a map with their identifier
		Map<String, OverviewRow> sourceCourseNodesMap = context.getCourseNodes().stream().collect(Collectors.toMap(row -> row.getEditorNode().getIdent(), Function.identity()));
		context.setCourseNodesMap(sourceCourseNodesMap);
		
		RepositoryEntry target = repositoryService.create(context.getExecutingIdentity(), null, sourceEntry.getResourcename(), context.getDisplayName(),
				sourceEntry.getDescription(), copyResource, RepositoryEntryStatusEnum.preparation, null);

		// Copy metadata
		target.setTechnicalType(sourceEntry.getTechnicalType());
		target.setCredits(sourceEntry.getCredits());
		target.setExpenditureOfWork(context.getExpenditureOfWork());
		target.setMainLanguage(sourceEntry.getMainLanguage());
		target.setObjectives(sourceEntry.getObjectives());
		target.setRequirements(sourceEntry.getRequirements());
		target.setEducationalType(sourceEntry.getEducationalType());
		target.setExternalRef(context.getExternalRef());
		target.setAuthors(context.getAuthors());
		
		// Copy taxonomy levels
		List<TaxonomyLevel> taxonomyLevels = repositoryEntryToTaxonomyLevelDao.getTaxonomyLevels(sourceEntry);
		for (TaxonomyLevel taxonomyLevel : taxonomyLevels) {
			RepositoryEntryToTaxonomyLevel relation = repositoryEntryToTaxonomyLevelDao.createRelation(target, taxonomyLevel);
			target.getTaxonomyLevels().add(relation);
		}

		// Add to organisations
		List<Organisation> sourceOrganisations = reToGroupDao.getOrganisations(sourceEntry);
		for(Organisation sourceOrganisation:sourceOrganisations) {
			RepositoryEntryToOrganisation orgRelation = repositoryEntryToOrganisationDao.createRelation(sourceOrganisation, target, false);
			target.getOrganisations().add(orgRelation);
			RepositoryEntryToGroupRelation grpRelation = reToGroupDao.createRelation(sourceOrganisation.getGroup(), target);
			target.getGroups().add(grpRelation);
		}

		
		target = dbInstance.getCurrentEntityManager().merge(target);

		RepositoryHandler handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(sourceEntry);
		target = handler.copyCourse(context, target);
		
		// Copy the license
		licenseService.copy(sourceResource, copyResource);
		
		if (StringHelper.containsNonWhitespace(context.getLicenseTypeKey())) {
			LicenseType licenseType = licenseService.loadLicenseTypeByKey(context.getLicenseTypeKey());
			
			if (licenseType != null) {
				ResourceLicense license = licenseService.loadLicense(copyResource);
				
				license.setLicenseType(licenseType);
				license.setLicensor(context.getLicensor());
				
				if (licenseService.isFreetext(licenseType)) {
					license.setFreetext(context.getLicenseFreetext());
				}
				
				licenseService.update(license);
			}			
		}		
		
		dbInstance.commit();

		// Set execution period
		setLifecycle(context, target);
				
		// Copy the image
		RepositoryManager.getInstance().copyImage(sourceEntry, target, context.getExecutingIdentity());

		// Copy media container
		VFSContainer sourceMediaContainer = handler.getMediaContainer(sourceEntry);
		if(sourceMediaContainer != null) {
			VFSContainer targetMediaContainer = handler.getMediaContainer(target);
			VFSManager.copyContent(sourceMediaContainer, targetMediaContainer);
		}
		
		// Copy groups
		copyGroups(context, target);
		
		// Copy owners
		copyOwners(context, target);
		
		// Copy coaches
		copyCoaches(context, target);
		
		// Define publication in catalog
		publishInCatalog(context, target);
		
		ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_CREATE, getClass(),
				LoggingResourceable.wrap(target, OlatResourceableType.genRepoEntry));

		target = repositoryManager.setDescriptionAndName(target,
				target.getDisplayname(), target.getExternalRef(), target.getAuthors(),
				target.getDescription(), target.getObjectives(), target.getRequirements(),
				target.getCredits(), target.getMainLanguage(), target.getLocation(),
				target.getExpenditureOfWork(), target.getLifecycle(), null, null,
				target.getEducationalType());
		
		// Copy disclaimer
		copyDisclaimer(context, target);
		
		// Move dates
		moveDates(context, target, sourceCourseNodesMap);
		
		return target;
	}
	
	private void copyGroups(CopyCourseContext context, RepositoryEntry target) {
		switch (context.getGroupCopyType()) {
		case copy:
			List<BusinessGroup> copiedGroups = new ArrayList<>();
			for (BusinessGroup group : businessGroupService.findBusinessGroups(null, context.getSourceRepositoryEntry(), 0, -1)) {
				BusinessGroup copiedGroup = businessGroupService.copyBusinessGroup(context.getExecutingIdentity(), group, group.getName(), group.getDescription(), group.getMinParticipants(), group.getMaxParticipants(), true, true, true, true, true, true, true, false, null);
				copiedGroups.add(copiedGroup);
			}
			businessGroupService.addResourcesTo(copiedGroups, Collections.singletonList(target));
			break;
		case ignore: 
			// Nothing to do here
			break;
		case reference: 
			businessGroupService.addResourcesTo(businessGroupService.findBusinessGroups(null, context.getSourceRepositoryEntry(), 0, -1), Collections.singletonList(target));
			break;
		case custom:
			switch (context.getCustomGroupCopyType()) {
				case copy:
					List<BusinessGroup> groupsToCopy = businessGroupService.loadBusinessGroups(context.getGroups().stream().map(group -> group.getKey()).collect(Collectors.toList()));
					List<BusinessGroup> cusomCopiedGroups = new ArrayList<>();
					for (BusinessGroup group : groupsToCopy) {
						BusinessGroup copiedGroup = businessGroupService.copyBusinessGroup(context.getExecutingIdentity(), group, group.getName(), group.getDescription(), group.getMinParticipants(), group.getMaxParticipants(), true, true, true, true, true, true, true, false, null);
						cusomCopiedGroups.add(copiedGroup);
					}
					businessGroupService.addResourcesTo(cusomCopiedGroups, Collections.singletonList(target));
					break;
				case reference:
					List<BusinessGroup> groupsToReference = businessGroupService.loadBusinessGroups(context.getGroups().stream().map(group -> group.getKey()).collect(Collectors.toList()));
					businessGroupService.addResourcesTo(groupsToReference, Collections.singletonList(target));
					break;
				case ignore:
					// Nothing to do here
					break;
				default:
					break;	
			}
			break;
		default: 
			break;
		}
	}
	
	private void copyOwners(CopyCourseContext context, RepositoryEntry target) {
		switch (context.getOwnersCopyType()) {
		case copy:
			List<Identity> ownersToCopy = repositoryService.getMembers(context.getSourceRepositoryEntry(), RepositoryEntryRelationType.defaultGroup, GroupRoles.owner.name());
			
			if (ownersToCopy.isEmpty()) {
				return;
			}
			
			IdentitiesAddEvent identitiesAddEvent = new IdentitiesAddEvent(ownersToCopy);
			repositoryManager.addOwners(context.getExecutingIdentity(), identitiesAddEvent, target, new MailPackage(false));
			break;
		case custom:
			switch (context.getCustomOwnersCopyType()) {
				case copy:
					List<Identity> customOwnersToCopy = context.getNewOwners();
					
					if (customOwnersToCopy == null  || customOwnersToCopy.isEmpty()) {
						return;
					}
					
					IdentitiesAddEvent customIdentitiesAddEvent = new IdentitiesAddEvent(customOwnersToCopy);
					repositoryManager.addOwners(context.getExecutingIdentity(), customIdentitiesAddEvent, target, new MailPackage(false));
					break;
				case replace:
					IdentitiesAddEvent replaceOwnersEvent = new IdentitiesAddEvent(Collections.singletonList(context.getExecutingIdentity()));
					repositoryManager.addOwners(context.getExecutingIdentity(), replaceOwnersEvent, target, new MailPackage(false));
				default:
					break;	
			}
			break;
		default: 
			break;
		}
	}
	
	private void copyCoaches(CopyCourseContext context, RepositoryEntry target) {
		switch (context.getCoachesCopyType()) {
		case copy:
			List<Identity> coachesToCopy = repositoryService.getMembers(context.getSourceRepositoryEntry(), RepositoryEntryRelationType.defaultGroup, GroupRoles.coach.name());
			
			if (coachesToCopy.isEmpty()) {
				return;
			}
			
			IdentitiesAddEvent identitiesAddEvent = new IdentitiesAddEvent(coachesToCopy);
			repositoryManager.addTutors(context.getExecutingIdentity(), null, identitiesAddEvent, target, new MailPackage(false));
			break;
		case custom:
			switch (context.getCustomCoachesCopyType()) {
				case copy:
					List<Identity> customCoachesToCopy = context.getNewCoaches();
					
					if (customCoachesToCopy == null  || customCoachesToCopy.isEmpty()) {
						return;
					}
					
					IdentitiesAddEvent customIdentitiesAddEvent = new IdentitiesAddEvent(customCoachesToCopy);
					repositoryManager.addTutors(context.getExecutingIdentity(), null, customIdentitiesAddEvent, target, new MailPackage(false));
					break;
				default:
					break;	
			}
			break;
		default: 
			break;
		}
	}
	
	private void setLifecycle(CopyCourseContext context, RepositoryEntry target) {
		switch (context.getExecutionType()) {
		case none:
			target.setLifecycle(null);
			break;
		case beginAndEnd:
			RepositoryEntryLifecycle privateCycle = target.getLifecycle();
			if(privateCycle == null || !privateCycle.isPrivateCycle()) {
				String softKey = "lf_" + target.getSoftkey();
				privateCycle = lifecycleDao.create(target.getDisplayname(), softKey, true, context.getBeginDate(), context.getEndDate());
			} else {
				privateCycle.setValidFrom(context.getBeginDate());
				privateCycle.setValidTo(context.getEndDate());
				privateCycle = lifecycleDao.updateLifecycle(privateCycle);
			}
			target.setLifecycle(privateCycle);
		case semester: 
			if (context.getSemesterKey() == null) {
				break; 
			}
			
			RepositoryEntryLifecycle semesterCycle = lifecycleDao.loadById(context.getSemesterKey());
			target.setLifecycle(semesterCycle);
			break;		
		}
		
		target.setLocation(context.getLocation());
	}
	
	private void publishInCatalog(CopyCourseContext context, RepositoryEntry target) {
		switch (context.getCatalogCopyType()) {
		case copy:
			copyCatalog(context, target);
			break;
		case custom:
			switch (context.getCustomCatalogCopyType()) {
			case copy:
				copyCatalog(context, target);
				break;
			default:
				break;
			}
		default:
			break;
		}
	}
	
	private void copyCatalog(CopyCourseContext context, RepositoryEntry target) {
		List<CatalogEntry> catalogEntries = catalogManager.getCatalogEntriesReferencing(context.getSourceRepositoryEntry());
		
		for (CatalogEntry catalogEntry : catalogEntries) {
			CatalogEntry newEntry = catalogManager.createCatalogEntry();
			newEntry.setRepositoryEntry(target);
			newEntry.setName(catalogEntry.getName());
			newEntry.setDescription(catalogEntry.getDescription());
			newEntry.setType(CatalogEntry.TYPE_LEAF);
			
			catalogManager.addCatalogEntry(catalogEntry.getParent(), newEntry);
		}
	}
	
	private void copyDisclaimer(CopyCourseContext context, RepositoryEntry target) {
		if (context.getDisclaimerCopyType() != null) {
			switch(context.getDisclaimerCopyType()) {
			case copy:
				// Nothing to do here, course config is copied anyway
				break;
			case ignore:
				// Necessary to remove, because course config is copied
				removeDisclaimerSettings(target);
				break;
			case custom:
				copyDisclaimerSettings(context, target);
				break;
			default:
				break;
			}
		}
	}
	
	private void copyDisclaimerSettings(CopyCourseContext context, RepositoryEntry target) {
		if (context.getDisclaimerCopyContext() != null) {
			OLATResourceable courseOres = target.getOlatResource();
			ICourse course = CourseFactory.openCourseEditSession(courseOres.getResourceableId());
			CourseConfig courseConfig = course.getCourseEnvironment().getCourseConfig();
			
			CourseDisclaimerContext disclaimerContext = context.getDisclaimerCopyContext();
			
			courseConfig.setDisclaimerEnabled(1, disclaimerContext.isTermsOfUseEnabled());
			if (disclaimerContext.isTermsOfUseEnabled()) {
				courseConfig.setDisclaimerTitle(1, disclaimerContext.getTermsOfUseTitle());
				courseConfig.setDisclaimerTerms(1, disclaimerContext.getTermsOfUseContent());
				courseConfig.setDisclaimerLabel(1, 1, disclaimerContext.getTermsOfUseLabel1());
				courseConfig.setDisclaimerLabel(1, 2, disclaimerContext.getTermsOfUseLabel2());
			}

			courseConfig.setDisclaimerEnabled(2, disclaimerContext.isDataProtectionEnabled());
			if (disclaimerContext.isDataProtectionEnabled()) {
				courseConfig.setDisclaimerTitle(2, disclaimerContext.getDataProtectionTitle());
				courseConfig.setDisclaimerTerms(2, disclaimerContext.getDataProtectionContent());
				courseConfig.setDisclaimerLabel(2, 1, disclaimerContext.getDataProtectionLabel1());
				courseConfig.setDisclaimerLabel(2, 2, disclaimerContext.getDataProtectionLabel2());
			}
			
			CourseFactory.setCourseConfig(courseOres.getResourceableId(), courseConfig);
			CourseFactory.saveCourse(courseOres.getResourceableId());
			CourseFactory.closeCourseEditSession(courseOres.getResourceableId(), true);
		}
	}
	
	private void removeDisclaimerSettings(RepositoryEntry target) {
		OLATResourceable courseOres = target.getOlatResource();
		ICourse course = CourseFactory.openCourseEditSession(courseOres.getResourceableId());
		CourseConfig courseConfig = course.getCourseEnvironment().getCourseConfig();
		
		
		courseConfig.setDisclaimerEnabled(1, false);
		courseConfig.setDisclaimerTitle(1, "");
		courseConfig.setDisclaimerTerms(1, "");
		courseConfig.setDisclaimerLabel(1, 1, "");
		courseConfig.setDisclaimerLabel(1, 2, "");
			
		courseConfig.setDisclaimerEnabled(2, false);
		courseConfig.setDisclaimerTitle(2, "");
		courseConfig.setDisclaimerTerms(2, "");
		courseConfig.setDisclaimerLabel(2, 1, "");
		courseConfig.setDisclaimerLabel(2, 2, "");
		
		CourseFactory.setCourseConfig(courseOres.getResourceableId(), courseConfig);
		CourseFactory.saveCourse(courseOres.getResourceableId());
		CourseFactory.closeCourseEditSession(courseOres.getResourceableId(), true);
	}
	
	private void moveDates(CopyCourseContext context, RepositoryEntry target, Map<String, OverviewRow> sourceCourseNodesMap) {
		if (context.getCourseNodes() != null && (context.isCustomConfigsLoaded() || context.getDateDifference() != 0)) {
			
			OLATResourceable targetOres = target.getOlatResource();
			ICourse course = CourseFactory.openCourseEditSession(targetOres.getResourceableId());
			CourseEditorTreeModel editorTreeModel = course.getEditorTreeModel();
			
			for (String ident : sourceCourseNodesMap.keySet()) {
				CourseEditorTreeNode node = (CourseEditorTreeNode) editorTreeModel.getNodeById(ident);
				LearningPathConfigs targetConfigs = learningPathService.getConfigs(editorTreeModel.getCourseNode(ident));
				
				// If the course overview step has been shown, the 
				if (context.isCustomConfigsLoaded()) {
					OverviewRow overviewRow = sourceCourseNodesMap.get(ident);
					
					// Obligation
					if (overviewRow.getObligationChooser() != null) {
						targetConfigs.setObligation(AssessmentObligation.valueOf(overviewRow.getObligationChooser().getSelectedKey()));
					}
					
					// Start date
					if (overviewRow.getStartChooser() != null) {
						targetConfigs.setStartDate(overviewRow.getStartChooser().getDate());
					}
				
					// Due date
					if (overviewRow.getEndChooser() != null) {
						targetConfigs.setEndDate(overviewRow.getEndChooser().getDate());
					}
				} else if (context.getDateDifference() != 0) {
					if (targetConfigs.getStartDate() != null) {
						Date startDate = new Date(targetConfigs.getStartDate().getTime() + context.getDateDifference());
						targetConfigs.setStartDate(startDate);
					}
					
					if (targetConfigs.getEndDate() != null) {
						Date endDate = new Date(targetConfigs.getEndDate().getTime() + context.getDateDifference());
						targetConfigs.setEndDate(endDate);
					}
				}
			}
		}
	}
}
