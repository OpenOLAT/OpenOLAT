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
import java.util.List;
import java.util.stream.Collectors;

import org.olat.admin.securitygroup.gui.IdentitiesAddEvent;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSManager;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
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
	private LifeFullIndexer lifeIndexer;
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

	
	
	@Override
	public RepositoryEntry copyLearningPathCourse(CopyCourseContext context) {
		RepositoryEntry sourceEntry = context.getSourceRepositoryEntry();
		OLATResource sourceResource = sourceEntry.getOlatResource();
		OLATResource copyResource = resourceManager.createOLATResourceInstance(sourceResource.getResourceableTypeName());
		
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
		
		switch (context.getMetadataCopyType()) {
			case copy: 
				target.setAuthors(sourceEntry.getAuthors());
				break;
			case custom: 
				target.setAuthors(context.getAuthors());
				break;
			default:
				break;
		}
		
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
		dbInstance.commit();

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
		
		// Set execution period
		setLifecycle(context, target);
		
		// Define publication in catalog
		publishInCatalog(context, target);
		
		ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_CREATE, getClass(),
				LoggingResourceable.wrap(target, OlatResourceableType.genRepoEntry));

		lifeIndexer.indexDocument(RepositoryEntryDocument.TYPE, target.getKey());
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
			switch (context.getCustomGroupCopyType()) {
				case copy:
					List<Identity> customOwnersToCopy = context.getNewCoaches();
					
					if (customOwnersToCopy == null  || customOwnersToCopy.isEmpty()) {
						return;
					}
					
					IdentitiesAddEvent customIdentitiesAddEvent = new IdentitiesAddEvent(customOwnersToCopy);
					repositoryManager.addOwners(context.getExecutingIdentity(), customIdentitiesAddEvent, target, new MailPackage(false));
					break;
				default:
					break;	
			}
			break;
		default: 
			break;
		}
	}
	
	private void copyCoaches(CopyCourseContext context, RepositoryEntry target) {
		switch (context.getOwnersCopyType()) {
		case copy:
			List<Identity> coachesToCopy = repositoryService.getMembers(context.getSourceRepositoryEntry(), RepositoryEntryRelationType.defaultGroup, GroupRoles.coach.name());
			
			if (coachesToCopy.isEmpty()) {
				return;
			}
			
			IdentitiesAddEvent identitiesAddEvent = new IdentitiesAddEvent(coachesToCopy);
			repositoryManager.addOwners(context.getExecutingIdentity(), identitiesAddEvent, target, new MailPackage(false));
			break;
		case custom:
			switch (context.getCustomGroupCopyType()) {
				case copy:
					List<Identity> customCoachesToCopy = context.getNewCoaches();
					
					if (customCoachesToCopy == null  || customCoachesToCopy.isEmpty()) {
						return;
					}
					
					IdentitiesAddEvent customIdentitiesAddEvent = new IdentitiesAddEvent(customCoachesToCopy);
					repositoryManager.addOwners(context.getExecutingIdentity(), customIdentitiesAddEvent, target, new MailPackage(false));
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
			target.setLocation(context.getLocation());
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
			target.setLocation(context.getLocation());
			target.setLifecycle(privateCycle);
		case semester: 
			if (context.getSemesterKey() == null) {
				return; 
			}
			
			RepositoryEntryLifecycle semesterCycle = lifecycleDao.loadById(context.getSemesterKey());
			target.setLocation(context.getLocation());
			target.setLifecycle(semesterCycle);
			break;		
		}
	}
	
	private void publishInCatalog(CopyCourseContext context, RepositoryEntry target) {
		switch (context.getCatalogCopyType()) {
		case copy:
			List<CatalogEntry> catalogEntries = catalogManager.getCatalogEntriesReferencing(context.getSourceRepositoryEntry());
			
			for (CatalogEntry catalogEntry : catalogEntries) {
				CatalogEntry newEntry = catalogManager.createCatalogEntry();
				newEntry.setRepositoryEntry(target);
				newEntry.setName(catalogEntry.getName());
				newEntry.setDescription(catalogEntry.getDescription());
				newEntry.setType(CatalogEntry.TYPE_LEAF);
				
				catalogManager.addCatalogEntry(catalogEntry.getParent(), newEntry);
			}
			break;
		default:
			break;
		}
	}
}
