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
import java.util.stream.Collectors;

import org.olat.admin.securitygroup.gui.IdentitiesAddEvent;
import org.olat.basesecurity.Group;
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
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSManager;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.assessment.model.AssessmentModeImpl;
import org.olat.course.config.CourseConfig;
import org.olat.course.duedate.DueDateConfig;
import org.olat.course.learningpath.LearningPathConfigs;
import org.olat.course.learningpath.LearningPathService;
import org.olat.course.nodes.bc.CoachFolderFactory;
import org.olat.course.nodes.bc.CourseDocumentsFactory;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.course.wizard.CourseDisclaimerContext;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.BusinessGroupReference;
import org.olat.group.ui.main.BGTableItem;
import org.olat.ims.lti13.LTI13Service;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.manager.LectureBlockToGroupDAO;
import org.olat.modules.lecture.manager.LectureBlockToTaxonomyLevelDAO;
import org.olat.modules.lecture.model.LectureBlockImpl;
import org.olat.modules.lecture.model.LectureBlockRow;
import org.olat.modules.lecture.model.LectureBlockWithTeachers;
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
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext.CopyType;
import org.olat.repository.ui.author.copy.wizard.CopyCourseOverviewRow;
import org.olat.repository.ui.author.copy.wizard.additional.AssessmentModeCopyInfos;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.search.service.document.RepositoryEntryDocument;
import org.olat.search.service.indexer.LifeFullIndexer;
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
	private LectureService lectureService;
	@Autowired
	private AssessmentModeManager assessmentModeManager;
	@Autowired
	private LectureBlockToGroupDAO lectureBlockToGroupDAO;
	@Autowired
	private LectureBlockToTaxonomyLevelDAO lectureBlockToTaxonomyDAO;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDAO;
	@Autowired
	private LifeFullIndexer lifeIndexer;
	
	
	@Override
	public RepositoryEntry copyLearningPathCourse(CopyCourseContext context) {
		RepositoryEntry sourceEntry = context.getSourceRepositoryEntry();
		OLATResource sourceResource = sourceEntry.getOlatResource();
		// The source course is necessary to compare settings to the original
		// At the moment it is not necessary to do so
		//ICourse sourceCourse = CourseFactory.loadCourse(sourceResource);
		
		OLATResource copyResource = resourceManager.createOLATResourceInstance(sourceResource.getResourceableTypeName());
		
		// For easier handling, put all nodes into a map with their identifier
		Map<String, CopyCourseOverviewRow> sourceCourseNodesMap = context.getCourseNodesMap();
		
		RepositoryEntry target = repositoryService.create(context.getExecutingIdentity(), null, sourceEntry.getResourcename(), context.getDisplayName(),
				sourceEntry.getDescription(), copyResource, RepositoryEntryStatusEnum.preparation, null);
		
		// Copy metadata
		target.setTechnicalType(sourceEntry.getTechnicalType());
		target.setCredits(sourceEntry.getCredits());
		target.setExpenditureOfWork(sourceEntry.getExpenditureOfWork());
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
		if (sourceOrganisations != null) {
			for(Organisation sourceOrganisation:sourceOrganisations) {
				RepositoryEntryToOrganisation orgRelation = repositoryEntryToOrganisationDao.createRelation(sourceOrganisation, target, false);
				target.getOrganisations().add(orgRelation);
				RepositoryEntryToGroupRelation grpRelation = reToGroupDao.createRelation(sourceOrganisation.getGroup(), target);
				target.getGroups().add(grpRelation);
			}
		}

		
		target = dbInstance.getCurrentEntityManager().merge(target);
		RepositoryHandler handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(sourceEntry);

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
		
		target = handler.copyCourse(context, target);
		
		ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_CREATE, getClass(),
				LoggingResourceable.wrap(target, OlatResourceableType.genRepoEntry));

		target = repositoryManager.setDescriptionAndName(target,
				target.getDisplayname(), target.getExternalRef(), target.getAuthors(),
				target.getDescription(), target.getObjectives(), target.getRequirements(),
				target.getCredits(), target.getMainLanguage(), target.getLocation(),
				target.getExpenditureOfWork(), target.getLifecycle(), null, null,
				target.getEducationalType());
		
		// Open course editing session
		OLATResourceable targetCourseOres = target.getOlatResource();
		ICourse targetCourse = CourseFactory.openCourseEditSession(targetCourseOres.getResourceableId());
		CourseConfig targetCourseConfig = targetCourse.getCourseEnvironment().getCourseConfig();
		
		// Copy disclaimer
		copyDisclaimer(context, targetCourseConfig);
		
		// Move dates
		moveDates(context, targetCourse, sourceCourseNodesMap);
		
		// Copy lecture blocks
		copyLectureBlocks(context, target);
		
		// Copy assessment modes
		copyAssessmentModes(context, target);
		
		// Copy documents		
		copyDocuments(context, targetCourse);
		
		// Copy coach documents
		copyCoachDocuments(context, targetCourse);
		
		// Close edit session
		CourseFactory.setCourseConfig(targetCourseOres.getResourceableId(), targetCourseConfig);
		CourseFactory.saveCourse(targetCourseOres.getResourceableId());
		CourseFactory.closeCourseEditSession(targetCourseOres.getResourceableId(), true);
		
		// Index new course
		lifeIndexer.indexDocument(RepositoryEntryDocument.TYPE, target.getKey());
		
		return target;
	}
	
	protected void copyGroups(CopyCourseContext context, RepositoryEntry target) {
		if (context.getGroupCopyType() == null) {
			return;
		}
		
		switch (context.getGroupCopyType()) {
		case copy:
			List<BusinessGroup> copiedGroups = new ArrayList<>();
			List<BusinessGroupReference> copiedGroupReferences = new ArrayList<>();
			for (BusinessGroup group : businessGroupService.findBusinessGroups(null, context.getSourceRepositoryEntry(), 0, -1)) {
				if(LTI13Service.LTI_GROUP_TYPE.equals(group.getTechnicalType())) {
					continue;
				}
				
				BusinessGroup copiedGroup = businessGroupService.copyBusinessGroup(context.getExecutingIdentity(), group, group.getName(), group.getDescription(), group.getMinParticipants(), group.getMaxParticipants(), true, true, true, false, false, true, false, false, null);
				businessGroupService.removeOwners(context.getExecutingIdentity(), Collections.singletonList(context.getExecutingIdentity()), copiedGroup);
				copiedGroups.add(copiedGroup);
				copiedGroupReferences.add(new BusinessGroupReference(copiedGroup, group));
			}
			context.setNewGroupReferences(copiedGroupReferences);
			businessGroupService.addResourcesTo(copiedGroups, Collections.singletonList(target));
			break;
		case ignore: 
			// Nothing to do here
			break;
		case reference: 
			List<BusinessGroup> referencedGroups = businessGroupService.findBusinessGroups(null, context.getSourceRepositoryEntry(), 0, -1);
			businessGroupService.addResourcesTo(referencedGroups, Collections.singletonList(target));
			
			List<BusinessGroupReference> newReferencedGroups = new ArrayList<>();
			for (BusinessGroup group : referencedGroups) {
				if(LTI13Service.LTI_GROUP_TYPE.equals(group.getTechnicalType())) {
					continue;
				}
				
				newReferencedGroups.add(new BusinessGroupReference(group));
			}
			
			context.setNewGroupReferences(newReferencedGroups);
			break;
		case custom:
			switch (context.getCustomGroupCopyType()) {
				case copy:
					List<BusinessGroup> groupsToCopy = businessGroupService.loadBusinessGroups(context.getGroups().stream().map(BGTableItem::getKey).collect(Collectors.toList()));
					List<BusinessGroup> customCopiedGroups = new ArrayList<>();
					List<BusinessGroupReference> customCopiedGroupReferences = new ArrayList<>();
					for (BusinessGroup group : groupsToCopy) {
						if(LTI13Service.LTI_GROUP_TYPE.equals(group.getTechnicalType())) {
							continue;
						}
						
						BusinessGroup copiedGroup = businessGroupService.copyBusinessGroup(context.getExecutingIdentity(), group, group.getName(), group.getDescription(), group.getMinParticipants(), group.getMaxParticipants(), true, true, true, false, false, true, false, false, null);
						businessGroupService.removeOwners(context.getExecutingIdentity(), Collections.singletonList(context.getExecutingIdentity()), copiedGroup);
						customCopiedGroups.add(copiedGroup);
						customCopiedGroupReferences.add(new BusinessGroupReference(copiedGroup, group));
					}
					context.setNewGroupReferences(customCopiedGroupReferences);
					businessGroupService.addResourcesTo(customCopiedGroups, Collections.singletonList(target));
					break;
				case reference:
					List<BusinessGroup> groupsToReference = businessGroupService.loadBusinessGroups(context.getGroups().stream().map(BGTableItem::getKey).collect(Collectors.toList()));
					businessGroupService.addResourcesTo(groupsToReference, Collections.singletonList(target));
					
					List<BusinessGroupReference> newCustomReferencedGroups = new ArrayList<>();
					for (BusinessGroup group : groupsToReference) {
						if(LTI13Service.LTI_GROUP_TYPE.equals(group.getTechnicalType())) {
							continue;
						}
						
						newCustomReferencedGroups.add(new BusinessGroupReference(group));
					}
					
					context.setNewGroupReferences(newCustomReferencedGroups);
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
	
	protected void copyOwners(CopyCourseContext context, RepositoryEntry target) {
		switch (context.getOwnersCopyType()) {
		case copy:
			List<Identity> ownersToCopy = repositoryService.getMembers(context.getSourceRepositoryEntry(), RepositoryEntryRelationType.defaultGroup, GroupRoles.owner.name());
			
			if (ownersToCopy.isEmpty()) {
				break;
			}
			
			IdentitiesAddEvent identitiesAddEvent = new IdentitiesAddEvent(ownersToCopy);
			repositoryManager.addOwners(context.getExecutingIdentity(), identitiesAddEvent, target, new MailPackage(false));
			break;
		case custom:
			List<Identity> customOwnersToCopy = context.getNewOwners();
			
			if (customOwnersToCopy == null  || customOwnersToCopy.isEmpty()) {
				break;
			}
			
			IdentitiesAddEvent customIdentitiesAddEvent = new IdentitiesAddEvent(customOwnersToCopy);
			repositoryManager.addOwners(context.getExecutingIdentity(), customIdentitiesAddEvent, target, new MailPackage(false));
			break;
		default: 
			break;
		}
	}
	
	protected void copyCoaches(CopyCourseContext context, RepositoryEntry target) {
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
	
	protected void setLifecycle(CopyCourseContext context, RepositoryEntry target) {
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
			break;
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
	
	protected void publishInCatalog(CopyCourseContext context, RepositoryEntry target) {
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
	
	protected void copyCatalog(CopyCourseContext context, RepositoryEntry target) {
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
	
	protected void copyDisclaimer(CopyCourseContext context, CourseConfig courseConfig) {
		if (context.getDisclaimerCopyType() != null) {
			switch(context.getDisclaimerCopyType()) {
			case copy:
				// Nothing to do here, course config is copied anyway
				break;
			case ignore:
				// Necessary to remove, because course config is copied
				removeDisclaimerSettings(courseConfig);
				break;
			case custom:
				copyDisclaimerSettings(context, courseConfig);
				break;
			default:
				break;
			}
		}
	}
	
	protected void copyDisclaimerSettings(CopyCourseContext context, CourseConfig courseConfig) {
		if (context.getDisclaimerCopyContext() != null) {
			
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
		}
	}
	
	protected void removeDisclaimerSettings(CourseConfig courseConfig) {
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
	}
	
	protected void moveDates(CopyCourseContext context, ICourse course, Map<String, CopyCourseOverviewRow> sourceCourseNodesMap) {
		if (context.getCourseNodes() != null) {
			for (String ident : sourceCourseNodesMap.keySet()) {
				CourseEditorTreeNode editorTreeNode = course.getEditorTreeModel().getCourseEditorNodeById(ident);
				LearningPathConfigs targetConfigs = learningPathService.getConfigs(editorTreeNode.getCourseNode(), editorTreeNode.getParent());
				
				// If the course overview step has been shown, the 
				if (context.isCustomConfigsLoaded()) {
					CopyCourseOverviewRow overviewRow = sourceCourseNodesMap.get(ident);
					
					// Obligation
					if (overviewRow.getAssesssmentObligation() != null) {
						targetConfigs.setObligation(overviewRow.getAssesssmentObligation());
					}
					
					// Start date
					if (overviewRow.getNewStartDate() != null) {
						targetConfigs.setStartDateConfig(DueDateConfig.absolute(overviewRow.getNewStartDate()));
					}
				
					// Due date
					if (overviewRow.getNewEndDate() != null) {
						targetConfigs.setEndDateConfig(DueDateConfig.absolute(overviewRow.getNewEndDate()));
					}
				} else if (context.getDateDifference() != 0) {
					if (DueDateConfig.isAbsolute(targetConfigs.getStartDateConfig())) {
						Date startDate = new Date(targetConfigs.getStartDateConfig().getAbsoluteDate().getTime() + context.getDateDifference());
						targetConfigs.setStartDateConfig(DueDateConfig.absolute(startDate));
					}
					
					if (DueDateConfig.isAbsolute(targetConfigs.getEndDateConfig())) {
						Date endDate = new Date(targetConfigs.getEndDateConfig().getAbsoluteDate().getTime() + context.getDateDifference());
						targetConfigs.setEndDateConfig(DueDateConfig.absolute(endDate));
					}
				}
			}
		}
	}
	
	protected void copyLectureBlocks(CopyCourseContext context, RepositoryEntry target) {
		if (context.getLectureBlockCopyType() == null || context.getLectureBlockCopyType().equals(CopyType.ignore)) {
			return;
		}
		
		List<LectureBlockWithTeachers> lectureBlocks = lectureService.getLectureBlocksWithTeachers(context.getSourceRepositoryEntry());
		List<Identity> coaches = repositoryService.getMembers(target, RepositoryEntryRelationType.defaultGroup, GroupRoles.coach.name());
		
		if (context.getLectureBlockCopyType().equals(CopyType.copy)) {			
			for (LectureBlockWithTeachers lectureBlockWithTeachers : lectureBlocks) {
				LectureBlockImpl original = (LectureBlockImpl) lectureBlockWithTeachers.getLectureBlock();
				LectureBlockImpl copy = (LectureBlockImpl) lectureService.createLectureBlock(target);
				
				Date startDate = original.getStartDate();
				Date endDate = original.getEndDate();
				Date effectiveEndDate = original.getEffectiveEndDate();
				Date autoCloseDate = original.getAutoClosedDate();
				
				if (startDate != null) {
					startDate = new Date(startDate.getTime() + context.getDateDifference());
				}
				
				if (endDate != null) {
					endDate = new Date(endDate.getTime() + context.getDateDifference());
				}
				
				if (effectiveEndDate != null) {
					effectiveEndDate = new Date(effectiveEndDate.getTime() + context.getDateDifference());
				}
				
				if (autoCloseDate != null) {
					autoCloseDate = new Date(autoCloseDate.getTime() + context.getDateDifference());
				}
				
				LectureBlock result = copyLectureBlockDetails(copy, original, context, original.getLocation(), startDate, endDate, effectiveEndDate, autoCloseDate);
				
				for (Identity coach : lectureService.getTeachers(original)) {
					if (coaches.contains(coach)) {
						lectureService.addTeacher(result, coach);
					}
				}
			}
		} else if (context.getLectureBlockCopyType().equals(CopyType.custom) && context.getLectureBlockRows() != null) {
			for (LectureBlockRow lectureBlockRow : context.getLectureBlockRows()) {
				LectureBlockImpl original = (LectureBlockImpl) lectureBlockRow.getLectureBlock();
				LectureBlockImpl copy = (LectureBlockImpl) lectureService.createLectureBlock(target);
				
				Date startDate = lectureBlockRow.getDateChooser().getDate();
				Date endDate = lectureBlockRow.getDateChooser().getSecondDate();
				
				long dateDifference = context.getDateDifference();
				
				if (startDate != null && original.getStartDate() != null) {
					dateDifference = startDate.getTime() - original.getStartDate().getTime();
				} else if (endDate != null && original.getEndDate() != null) {
					dateDifference = endDate.getTime() - original.getEndDate().getTime();
				}
				
				Date effectiveEndDate = original.getEffectiveEndDate();
				Date autoClosingDate = original.getAutoClosedDate();
				
				if (effectiveEndDate != null) {
					effectiveEndDate = new Date(effectiveEndDate.getTime() + dateDifference);
				}
				
				if (autoClosingDate != null) {
					autoClosingDate = new Date(autoClosingDate.getTime() + dateDifference);
				}
				
				LectureBlock result = copyLectureBlockDetails(copy, original, context, lectureBlockRow.getLocationElement().getValue(), startDate, endDate, effectiveEndDate, autoClosingDate);
				
				for (Identity coach : lectureBlockRow.getTeachersList()) {
					lectureService.addTeacher(result, coach);
				}
			}
		}
	}
	
	protected LectureBlock copyLectureBlockDetails(LectureBlockImpl copy, LectureBlockImpl original, CopyCourseContext context, String location, Date startDate, Date endDate, Date effectiveEndDate, Date autoClosedDate) {
		copy.setTitle(original.getTitle());
		copy.setDescription(original.getDescription());
		copy.setPreparation(original.getPreparation());
		copy.setLocation(location);
		copy.setComment(original.getComment());
		
		copy.setEndDate(endDate);
		copy.setStartDate(startDate);
		copy.setEffectiveEndDate(effectiveEndDate);
		copy.setCompulsory(original.isCompulsory());
		
		copy.setPlannedLecturesNumber(original.getPlannedLecturesNumber());
		copy.setEffectiveLecturesNumber(original.getEffectiveLecturesNumber());
		
		copy.setAutoClosedDate(autoClosedDate);
		copy.setStatusString(original.getStatusString());
		copy.setRollCallStatus(LectureRollCallStatus.open);
		
		copy.setReasonEffectiveEnd(original.getReasonEffectiveEnd());
		
		copy = (LectureBlockImpl) lectureService.save(copy, null);
		
		// Copy taxonomy levels
		for (TaxonomyLevel level : lectureBlockToTaxonomyDAO.getTaxonomyLevels(original)) {
			lectureBlockToTaxonomyDAO.createRelation(copy, level);
		}
		
		// Copy groups
		List<Group> groups = lectureBlockToGroupDAO.getGroups(original);
		for (RepositoryEntryToGroupRelation groupRelation : repositoryEntryRelationDAO.getRelations(groups)) {
			if (groupRelation.isDefaultGroup()) {
				Group newDefaultGroup = repositoryEntryRelationDAO.getDefaultGroup(copy.getEntry());
				lectureBlockToGroupDAO.createAndPersist(copy, newDefaultGroup);
				continue;
			}
			
			List<BusinessGroupReference> groupReferences = context.getNewGroupReferences();
			
			if (groupReferences == null || groupReferences.isEmpty()) {
				continue;
			}
			
			BusinessGroupReference newGroupReference = groupReferences.stream().filter(reference -> reference.getOriginalGroup().equals(groupRelation.getGroup())).findFirst().orElse(null);
			
			if (newGroupReference != null && newGroupReference.getGroup() != null) {
				lectureBlockToGroupDAO.createAndPersist(copy, newGroupReference.getGroup());
			}
		}
		
		return copy;
	}
	
	protected void copyAssessmentModes(CopyCourseContext context, RepositoryEntry target) {
		if (context.getAssessmentModeCopyType() == null || context.getAssessmentModeCopyType().equals(CopyType.ignore)) {
			return;
		}
		
		List<AssessmentMode> originals = assessmentModeManager.getAssessmentModeFor(context.getSourceRepositoryEntry());
		
		for (AssessmentMode original : originals) {
			if (original.getLectureBlock() != null) {
				continue;
			}
			
			AssessmentModeImpl copy = (AssessmentModeImpl) assessmentModeManager.createAssessmentMode(original);
			
			Date begin = null;
			Date end = null;

			if (context.getAssessmentModeCopyType().equals(CopyType.custom)) {
				AssessmentModeCopyInfos copyInfos = context.getAssessmentCopyInfos().get(original);
				begin = copyInfos.getBeginDateChooser().getDate();
				end = copyInfos.getEndDateChooser().getDate();
			} else {
				if (original.getBegin() != null) {
					begin = new Date(original.getBegin().getTime() + context.getDateDifference());
				}
				
				if (original.getEnd() != null) {
					end = new Date(original.getEnd().getTime() + context.getDateDifference());
				}
			}
			
			copy.setRepositoryEntry(target);
			copy.setBegin(begin);
			copy.setEnd(end);
			
			assessmentModeManager.merge(copy, false);
			
		}
	}
	
	private void copyDocuments(CopyCourseContext context, ICourse targetCourse) {
		if (context.getDocumentsCopyType() == null || context.getDocumentsCopyType().equals(CopyType.copy)) {
			// This is done in the CourseFactory.copyCourse
			// If the folder should be ignored, they need to be deleted in this step because they were already copied
			return;
		}
		
		CourseConfig cc = targetCourse.getCourseEnvironment().getCourseConfig();
		
		if(cc.isDocumentsEnabled() && !StringHelper.containsNonWhitespace(cc.getDocumentsPath())) {
			VFSContainer targetContainer = CourseDocumentsFactory.getFileContainer(targetCourse.getCourseBaseContainer());
			
			if (targetContainer != null) {
				for (VFSItem item : targetContainer.getItems()) {
					item.delete();
				}
			}
			
			// As specified, the container and path itself should remain, only the content should be deleted
			
			//cc.setDocumentsEnabled(false);
			//cc.setDocumentPath(null);
		}
	}
	
	private void copyCoachDocuments(CopyCourseContext context, ICourse targetCourse) {
		if (context.getCoachDocumentsCopyType() == null || context.getCoachDocumentsCopyType().equals(CopyType.copy)) {
			// This is done in the CourseFactory.copyCourse
			// If the folder should be ignored, they need to be deleted in this step because they were already copied
			return;
		}
		
		CourseConfig cc = targetCourse.getCourseEnvironment().getCourseConfig();
		
		if(cc.isCoachFolderEnabled() && !StringHelper.containsNonWhitespace(cc.getCoachFolderPath())) {
			VFSContainer targetContainer = CoachFolderFactory.getFileContainer(targetCourse.getCourseBaseContainer());
			
			if (targetContainer != null) {
				for (VFSItem item : targetContainer.getItems()) {
					item.delete();
				}
			}
			
			// As specified, the container and path itself should remain, only the content should be deleted
			
			//cc.setCoachFolderEnabled(false);
			//cc.setCoachFolderPath(null);
		}
	}
		
}
