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

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationDataDeletable;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.commons.services.taskexecutor.manager.PersistentTaskDAO;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.course.assessment.AssessmentModeCoordinationService;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.manager.AssessmentModeDAO;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.disclaimer.CourseDisclaimerManager;
import org.olat.ims.qti21.manager.AssessmentTestSessionDAO;
import org.olat.modules.assessment.manager.AssessmentEntryDAO;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.reminder.manager.ReminderDAO;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.repository.ErrorList;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryAllowToLeaveOptions;
import org.olat.repository.RepositoryEntryAuthorViewResults;
import org.olat.repository.RepositoryEntryDataDeletable;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryEntryMyView;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryEntryToOrganisation;
import org.olat.repository.RepositoryEntryToTaxonomyLevel;
import org.olat.repository.RepositoryMailing;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.repository.model.RepositoryEntryStatistics;
import org.olat.repository.model.RepositoryEntryStatusChangedEvent;
import org.olat.repository.model.RepositoryEntryToGroupRelation;
import org.olat.repository.model.SearchAuthorRepositoryEntryViewParams;
import org.olat.repository.model.SearchMyRepositoryEntryViewParams;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.manager.ACReservationDAO;
import org.olat.resource.accesscontrol.provider.auto.AutoAccessManager;
import org.olat.resource.references.ReferenceManager;
import org.olat.search.service.document.RepositoryEntryDocument;
import org.olat.search.service.indexer.LifeFullIndexer;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Initial date: 20.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("repositoryService")
public class RepositoryServiceImpl implements RepositoryService, OrganisationDataDeletable {

	private static final Logger log = Tracing.createLoggerFor(RepositoryServiceImpl.class);

	@Autowired
	private DB dbInstance;
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private CatalogManager catalogManager;
	@Autowired
	private CoordinatorManager coordinatorManager;
	@Autowired
	private ACReservationDAO reservationDao;
	@Autowired
	private ACService acService;
	@Autowired
	private AutoAccessManager autoAccessManager;
	@Autowired
	private ReferenceManager referenceManager;
	@Autowired
	private RepositoryEntryDAO repositoryEntryDAO;
	@Autowired
	private RepositoryEntryRelationDAO reToGroupDao;
	@Autowired
	private RepositoryEntryStatisticsDAO repositoryEntryStatisticsDao;
	@Autowired
	private RepositoryEntryMyCourseQueries myCourseViewQueries;
	@Autowired
	private RepositoryEntryAuthorQueries authorViewQueries;
	@Autowired
	private RepositoryHandlerFactory repositoryHandlerFactory;
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private OLATResourceManager resourceManager;
	@Autowired
	private CertificatesManager certificatesManager;
	@Autowired
	private UserCourseInformationsManager userCourseInformationsManager;
	@Autowired
	private AssessmentModeDAO assessmentModeDao;
	@Autowired
	private AssessmentModeCoordinationService assessmentModeCoordinationService;
	@Autowired
	private AssessmentTestSessionDAO assessmentTestSessionDao;
	@Autowired
	private PersistentTaskDAO persistentTaskDao;
	@Autowired
	private ReminderDAO reminderDao;
	@Autowired
	private AssessmentEntryDAO assessmentEntryDao;
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private RepositoryEntryToOrganisationDAO repositoryEntryToOrganisationDao;
	@Autowired
	private RepositoryEntryToTaxonomyLevelDAO repositoryEntryToTaxonomyLevelDao;

	@Autowired
	private LifeFullIndexer lifeIndexer;

	@Override
	public RepositoryEntry create(Identity initialAuthor, String initialAuthorAlt, String resourceName,
			String displayname, String description, OLATResource resource, RepositoryEntryStatusEnum status, Organisation organisation) {
		return create(initialAuthorAlt, initialAuthor, resourceName, displayname, description, resource, status, organisation);
	}

	private RepositoryEntry create(String initialAuthorName, Identity initialAuthor, String resourceName,
			String displayname, String description, OLATResource resource, RepositoryEntryStatusEnum status, Organisation organisation) {
		Date now = new Date();

		RepositoryEntry re = new RepositoryEntry();
		if(StringHelper.containsNonWhitespace(initialAuthorName)) {
			re.setInitialAuthor(initialAuthorName);
		} else if(initialAuthor != null) {
			re.setInitialAuthor(initialAuthor.getName());
		} else {
			re.setInitialAuthor("-");
		}
		re.setCreationDate(now);
		re.setLastModified(now);
		re.setEntryStatus(status);
		re.setCanDownload(false);
		re.setCanCopy(false);
		re.setCanReference(false);
		re.setDisplayname(displayname);
		re.setResourcename(StringHelper.containsNonWhitespace(resourceName) ? resourceName : "-");
		re.setDescription(description == null ? "" : description);
		re.setAllowToLeaveOption(repositoryModule.getAllowToLeaveDefaultOption());
		re.setInvitationByOwnerWithAuthorRightsEnabled(false);
		re.setLTI13DeploymentByOwnerWithAuthorRightsEnabled(false);
		
		if(resource == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("RepositoryEntry", CodeHelper.getForeverUniqueID());
			resource = resourceManager.createAndPersistOLATResourceInstance(ores);
		} else if(resource.getKey() == null) {
			dbInstance.getCurrentEntityManager().persist(resource);
		}
		re.setOlatResource(resource);

		RepositoryEntryStatistics statistics = new RepositoryEntryStatistics();
		statistics.setLastUsage(now);
		statistics.setCreationDate(now);
		statistics.setLastModified(now);
		statistics.setDownloadCounter(0l);
		statistics.setLaunchCounter(0l);
		statistics.setNumOfRatings(0l);
		statistics.setNumOfComments(0l);
		dbInstance.getCurrentEntityManager().persist(statistics);

		re.setStatistics(statistics);

		Group group = groupDao.createGroup();
		RepositoryEntryToGroupRelation rel = new RepositoryEntryToGroupRelation();
		rel.setCreationDate(new Date());
		rel.setDefaultGroup(true);
		rel.setGroup(group);
		rel.setEntry(re);
		
		Set<RepositoryEntryToGroupRelation> rels = new HashSet<>(2);
		rels.add(rel);
		
		if(organisation != null) {
			RepositoryEntryToGroupRelation relOrg = new RepositoryEntryToGroupRelation();
			relOrg.setCreationDate(new Date());
			relOrg.setDefaultGroup(false);
			relOrg.setGroup(organisation.getGroup());
			relOrg.setEntry(re);
			rels.add(relOrg);
		}

		re.setGroups(rels);

		if(initialAuthor != null) {
			groupDao.addMembershipTwoWay(group, initialAuthor, GroupRoles.owner.name());
		}
		dbInstance.getCurrentEntityManager().persist(re);
		
		if(organisation != null) {
			RepositoryEntryToOrganisation toOrganisation = repositoryEntryToOrganisationDao.createRelation(organisation, re, false);
			re.getOrganisations().add(toOrganisation);
		}

		autoAccessManager.grantAccess(re);
		return re;
	}
	
	

	@Override
	public boolean canCopy(RepositoryEntry entryToCopy, Identity identity) {
		boolean isManager = hasRoleExpanded(identity, entryToCopy,
				OrganisationRoles.administrator.name(), OrganisationRoles.learnresourcemanager.name());
		boolean isOwner = isManager || hasRole(identity, entryToCopy, GroupRoles.owner.name());
		boolean isAuthor = isManager || hasRoleExpanded(identity, entryToCopy, OrganisationRoles.author.name());
		
		boolean copyManaged = RepositoryEntryManagedFlag.isManaged(entryToCopy, RepositoryEntryManagedFlag.copy);
		return (isAuthor || isOwner) && (entryToCopy.getCanCopy() || isOwner) && !copyManaged;
	}

	@Override
	public RepositoryEntry copy(RepositoryEntry sourceEntry, Identity author, String displayname) {
		OLATResource sourceResource = sourceEntry.getOlatResource();
		OLATResource copyResource = resourceManager.createOLATResourceInstance(sourceResource.getResourceableTypeName());
		RepositoryEntry copyEntry = create(author, null, sourceEntry.getResourcename(), displayname,
				sourceEntry.getDescription(), copyResource, RepositoryEntryStatusEnum.preparation, null);

		//copy all fields
		copyEntry.setTechnicalType(sourceEntry.getTechnicalType());
		copyEntry.setAuthors(sourceEntry.getAuthors());
		copyEntry.setCredits(sourceEntry.getCredits());
		copyEntry.setExpenditureOfWork(sourceEntry.getExpenditureOfWork());
		copyEntry.setMainLanguage(sourceEntry.getMainLanguage());
		copyEntry.setObjectives(sourceEntry.getObjectives());
		copyEntry.setRequirements(sourceEntry.getRequirements());
		copyEntry.setEducationalType(sourceEntry.getEducationalType());
		
		List<TaxonomyLevel> taxonomyLevels = repositoryEntryToTaxonomyLevelDao.getTaxonomyLevels(sourceEntry);
		for (TaxonomyLevel taxonomyLevel : taxonomyLevels) {
			RepositoryEntryToTaxonomyLevel relation = repositoryEntryToTaxonomyLevelDao.createRelation(copyEntry, taxonomyLevel);
			copyEntry.getTaxonomyLevels().add(relation);
		}

		List<Organisation> sourceOrganisations = getOrganisations(sourceEntry);
		for(Organisation sourceOrganisation:sourceOrganisations) {
			RepositoryEntryToOrganisation orgRelation = repositoryEntryToOrganisationDao.createRelation(sourceOrganisation, copyEntry, false);
			copyEntry.getOrganisations().add(orgRelation);
			RepositoryEntryToGroupRelation grpRelation = reToGroupDao.createRelation(sourceOrganisation.getGroup(), copyEntry);
			copyEntry.getGroups().add(grpRelation);
		}

		copyEntry = dbInstance.getCurrentEntityManager().merge(copyEntry);

		RepositoryHandler handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(sourceEntry);
		copyEntry = handler.copy(author, sourceEntry, copyEntry);
		
		//copy the license
		licenseService.copy(sourceResource, copyResource);
		dbInstance.commit();

		//copy the image
		RepositoryManager.getInstance().copyImage(sourceEntry, copyEntry, author);

		//copy media container
		VFSContainer sourceMediaContainer = handler.getMediaContainer(sourceEntry);
		if(sourceMediaContainer != null) {
			VFSContainer targetMediaContainer = handler.getMediaContainer(copyEntry);
			VFSManager.copyContent(sourceMediaContainer, targetMediaContainer);
		}

		ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_CREATE, getClass(),
				LoggingResourceable.wrap(copyEntry, OlatResourceableType.genRepoEntry));

		lifeIndexer.indexDocument(RepositoryEntryDocument.TYPE, copyEntry.getKey());
		return copyEntry;
	}

	@Override
	public RepositoryEntry update(RepositoryEntry re) {
		re.setLastModified(new Date());
		RepositoryEntry mergedRe = dbInstance.getCurrentEntityManager().merge(re);
		dbInstance.commit();
		lifeIndexer.indexDocument(RepositoryEntryDocument.TYPE, mergedRe.getKey());
		autoAccessManager.grantAccess(re);
		return mergedRe;
	}

	@Override
	public RepositoryEntry loadByKey(Long key) {
		return repositoryEntryDAO.loadByKey(key);
	}

	@Override
	public List<RepositoryEntry> loadByKeys(Collection<Long> keys) {
		return repositoryEntryDAO.loadByKeys(keys);
	}

	@Override
	public RepositoryEntry loadByResourceKey(Long resourceKey) {
		return repositoryEntryDAO.loadByResourceKey(resourceKey);
	}

	@Override
	public List<RepositoryEntry> loadByResourceKeys(Collection<Long> resourceKeys) {
		return repositoryEntryDAO.loadByResourceKeys(resourceKeys);
	}

	@Override
	public OLATResource loadRepositoryEntryResource(Long repositoryEntryKey) {
		return repositoryEntryDAO.loadRepositoryEntryResource(repositoryEntryKey);
	}

	@Override
	public OLATResource loadRepositoryEntryResourceBySoftKey(String softkey) {
		return repositoryEntryDAO.loadRepositoryEntryResourceBySoftKey(softkey);
	}

	@Override
	public List<RepositoryEntry> loadRepositoryEntriesByExternalId(String externalId) {
		return repositoryEntryDAO.loadRepositoryEntriesByExternalId(externalId);
	}

	@Override
	public List<RepositoryEntry> loadRepositoryEntriesByExternalRef(String externalRef) {
		return repositoryEntryDAO.loadRepositoryEntriesByExternalRef(externalRef);
	}

	@Override
	public List<RepositoryEntry> loadRepositoryEntriesLikeExternalRef(String externalRef) {
		return repositoryEntryDAO.loadRepositoryEntriesLikeExternalRef(externalRef);
	}

	@Override
	public List<RepositoryEntry> loadRepositoryEntries(int firstResult, int maxResult) {
		return repositoryEntryDAO.loadRepositoryEntries(firstResult, maxResult);
	}

	@Override
	public VFSLeaf getIntroductionImage(RepositoryEntryRef ref) {
		RepositoryEntry re;
		if(ref instanceof RepositoryEntry) {
			re = (RepositoryEntry)ref;
		} else {
			re = repositoryEntryDAO.loadByKey(ref.getKey());
		}
		
		
		RepositoryHandler handler = repositoryHandlerFactory.getRepositoryHandler(re);
		VFSContainer mediaContainer = handler.getMediaContainer(re);
		String imageName = re.getResourceableId() + ".jpg";
		VFSItem image = mediaContainer.resolve(imageName);
		if(image instanceof VFSLeaf) {
			return (VFSLeaf)image;
		}
		imageName = re.getResourceableId() + ".png";
		image = mediaContainer.resolve(imageName);
		if(image instanceof VFSLeaf) {
			return (VFSLeaf)image;
		}
		imageName = re.getResourceableId() + ".gif";
		image = mediaContainer.resolve(imageName);
		if (image instanceof VFSLeaf) {
			return (VFSLeaf)image;
		}
		return null;
	}

	@Override
	public VFSLeaf getIntroductionMovie(RepositoryEntry re) {
		RepositoryHandler handler = repositoryHandlerFactory.getRepositoryHandler(re);
		VFSContainer mediaContainer = handler.getMediaContainer(re);
		if(mediaContainer != null) {
			List<VFSItem> items = mediaContainer.getItems();
			for(VFSItem item:items) {
				if(item instanceof VFSLeaf
						&& item.getName().startsWith(re.getKey().toString())
						&& (item.getName().endsWith(".mp4") || item.getName().endsWith(".m4v") || item.getName().endsWith(".flv")) ) {
					return (VFSLeaf)item;
				}
			}
		}
		return null;
	}

	@Override
	public RepositoryEntry deleteSoftly(RepositoryEntry re, Identity deletedBy, boolean owners, boolean sendNotifications) {
		// start delete
		RepositoryEntry reloadedRe = repositoryEntryDAO.loadForUpdate(re);
		reloadedRe.setEntryStatus(RepositoryEntryStatusEnum.trash);
		
		if(reloadedRe.getDeletionDate() == null) {
			// don't write the name of an admin which make a restore -> delete operation
			reloadedRe.setDeletedBy(deletedBy);
			reloadedRe.setDeletionDate(new Date());
		}
		reloadedRe = dbInstance.getCurrentEntityManager().merge(reloadedRe);
		dbInstance.commit();
		
		acService.getOffers(reloadedRe, true, false, null, false, null).stream()
				.filter(offer -> offer.isGuestAccess() || offer.isOpenAccess())
				.forEach(offer -> acService.deleteOffer(offer));
		
		List<Identity> ownerList = reToGroupDao.getMembers(reloadedRe, RepositoryEntryRelationType.entryAndCurriculums, GroupRoles.owner.name());
		// first stop assessment mode if needed
		assessmentModeCoordinationService.processRepositoryEntryChangedStatus(reloadedRe);
		//remove from catalog
		catalogManager.resourceableDeleted(reloadedRe);
		//remove participant and coach
		if(owners) {
			removeMembers(reloadedRe, GroupRoles.owner.name(), GroupRoles.coach.name(), GroupRoles.participant.name(), GroupRoles.waiting.name());
		} else {
			removeMembers(reloadedRe, GroupRoles.coach.name(), GroupRoles.participant.name(), GroupRoles.waiting.name());
		}
		//remove relation to business groups
		List<RepositoryEntryToGroupRelation> relations = reToGroupDao.getBusinessGroupAndCurriculumRelations(reloadedRe);
		for(RepositoryEntryToGroupRelation relation:relations) {
			if(!relation.isDefaultGroup()) {
				reToGroupDao.removeRelation(relation);
			}
		}
		dbInstance.commit();
		
		if(sendNotifications && deletedBy != null) {
			sendStatusChangedNotifications(ownerList, deletedBy, reloadedRe, RepositoryMailing.Type.deleteSoftEntry);
		}
		
		RepositoryEntryStatusChangedEvent statusChangedEvent = new RepositoryEntryStatusChangedEvent(reloadedRe.getKey());
		coordinatorManager.getCoordinator().getEventBus().fireEventToListenersOf(statusChangedEvent, OresHelper.clone(reloadedRe));
		return reloadedRe;
	}
	
	private void sendStatusChangedNotifications(List<Identity> owners, Identity doer, RepositoryEntry entry, RepositoryMailing.Type mailingType) {
		MailerResult result = new MailerResult();
		MailTemplate template = RepositoryMailing.getDefaultTemplate(mailingType, entry, doer);
		for(Identity owner:owners) {
			MailPackage reMailing = new MailPackage(template, result, "[RepositoryEntry:" + entry.getKey() + "]", true);
			RepositoryMailing.sendEmail(doer, owner, entry, mailingType, reMailing);
		}
	}

	@Override
	public RepositoryEntry restoreRepositoryEntry(RepositoryEntry entry) {
		RepositoryEntry reloadedRe = repositoryEntryDAO.loadForUpdate(entry);
		acService.getOffers(entry, true, false, null, false, null).stream()
				.filter(offer -> offer.isGuestAccess() || offer.isOpenAccess())
				.forEach(offer -> acService.deleteOffer(offer));
		
		if("CourseModule".equals(reloadedRe.getOlatResource().getResourceableTypeName())) {
			reloadedRe.setEntryStatus(RepositoryEntryStatusEnum.closed);
		} else {
			reloadedRe.setEntryStatus(RepositoryEntryStatusEnum.preparation);
		}
		reloadedRe = dbInstance.getCurrentEntityManager().merge(reloadedRe);
		dbInstance.commit();
		return reloadedRe;
	}

	@Override
	public ErrorList deletePermanently(RepositoryEntryRef entryRef, Identity identity, Roles roles, Locale locale) {
		ErrorList errors = new ErrorList();

		boolean debug = log.isDebugEnabled();

		// invoke handler delete callback
		RepositoryEntry entry = repositoryEntryDAO.loadByKey(entryRef.getKey());
		if(entry == null) {
			return errors;
		}

		log.info(Tracing.M_AUDIT, "deleteRepositoryEntry after load entry={}", entry);
		RepositoryHandler handler = repositoryHandlerFactory.getRepositoryHandler(entry);
		OLATResource resource = entry.getOlatResource();
		//delete old context
		if (handler != null && !handler.readyToDelete(entry, identity, roles, locale, errors)) {
			log.info(Tracing.M_AUDIT, "deleteRepositoryEntry aborted: references detected for entry={}", entry);
			return errors;
		}

		userCourseInformationsManager.deleteUserCourseInformations(entry);
		certificatesManager.deleteRepositoryEntry(entry);

		// delete all bookmarks referencing deleted entry
		CoreSpringFactory.getImpl(MarkManager.class).deleteMarks(entry);
		// delete all catalog entries referencing deleted entry
		catalogManager.resourceableDeleted(entry);
		// delete assessment modes
		assessmentModeDao.delete(entry);
		// delete reminders
		reminderDao.delete(entry);
		//delete reservations
		reservationDao.deleteReservations(resource);
		//delete references
		referenceManager.deleteAllReferencesOf(resource);
		//delete all pending tasks
		persistentTaskDao.delete(resource);
		dbInstance.commit();
		//delete lectures
		CoreSpringFactory.getImpl(LectureService.class).delete(entry);
		dbInstance.commit();
		//delete license
		CoreSpringFactory.getImpl(LicenseService.class).delete(resource);
		dbInstance.commit();
		//delete all consents
		CoreSpringFactory.getImpl(CourseDisclaimerManager.class).removeAllConsents(entry);
		dbInstance.commit();
		//delete score accounting triggers
		CoreSpringFactory.getImpl(CourseAssessmentService.class).deleteScoreAccountingTriggers(entry);
		dbInstance.commit();
		//detach portfolio if there are some lost
		CoreSpringFactory.getImpl(PortfolioService.class).detachCourseFromBinders(entry);
		dbInstance.commit();
		//detach from curriculum
		CoreSpringFactory.getImpl(CurriculumService.class).removeRepositoryEntry(entry);
		dbInstance.commit();

		// inform handler to do any cleanup work... handler must delete the
		// referenced resourceable as well.
		if (handler != null) {
			handler.cleanupOnDelete(entry, resource);
		}
		dbInstance.commitAndCloseSession();

		//delete all test sessions
		assessmentTestSessionDao.deleteAllUserTestSessionsByCourse(entry);
		dbInstance.commit();
		//nullify the reference
		assessmentEntryDao.removeEntryForReferenceEntry(entry);
		assessmentEntryDao.deleteEntryForRepositoryEntry(entry);
		dbInstance.commit();
		repositoryEntryToOrganisationDao.delete(entry);
		repositoryEntryToTaxonomyLevelDao.deleteRelation(entry);
		dbInstance.commit();

		if(debug) log.debug("deleteRepositoryEntry after reload entry={}", entry);
		deleteRepositoryEntryAndBaseGroups(entry);

		log.info(Tracing.M_AUDIT, "deleteRepositoryEntry Done entry={}", entry);
		return errors;
	}

	/**
	 *
	 * @param entry
	 */
	@Override
	public void deleteRepositoryEntryAndBaseGroups(RepositoryEntry entry) {
		RepositoryEntry reloadedEntry = dbInstance.getCurrentEntityManager()
				.getReference(RepositoryEntry.class, entry.getKey());
		Long resourceKey = reloadedEntry.getOlatResource().getKey();

		Group defaultGroup = reToGroupDao.getDefaultGroup(reloadedEntry);
		if(defaultGroup != null) {
			groupDao.removeMemberships(defaultGroup);
		}
		reToGroupDao.removeRelations(reloadedEntry);
		dbInstance.commit();
		
		// has a delete veto?
		boolean delete = true;
		Map<String,RepositoryEntryDataDeletable> deleteDelegates = CoreSpringFactory.getBeansOfType(RepositoryEntryDataDeletable.class);
		for(RepositoryEntryDataDeletable delegate:deleteDelegates.values()) {
			delete &= delegate.deleteRepositoryEntryData(reloadedEntry);
		}
		if(delete) {
			dbInstance.getCurrentEntityManager().remove(reloadedEntry);
			if(defaultGroup != null) {
				groupDao.removeGroup(defaultGroup);
			}
			dbInstance.commit();

			OLATResource reloadedResource = resourceManager.findResourceById(resourceKey);
			if(reloadedResource != null) {
				dbInstance.getCurrentEntityManager().remove(reloadedResource);
			}
		} else {
			reloadedEntry.setExternalId(null);
			reloadedEntry.setEntryStatus(RepositoryEntryStatusEnum.deleted);
			dbInstance.getCurrentEntityManager().merge(reloadedEntry);
		}
		dbInstance.commit();
	}

	@Override
	public RepositoryEntry closeRepositoryEntry(RepositoryEntry entry, Identity closedBy, boolean sendNotifications) {
		RepositoryEntry reloadedEntry = repositoryEntryDAO.loadForUpdate(entry);
		reloadedEntry.setEntryStatus(RepositoryEntryStatusEnum.closed);
		reloadedEntry = dbInstance.getCurrentEntityManager().merge(reloadedEntry);
		List<Identity> ownerList = reToGroupDao.getMembers(reloadedEntry, RepositoryEntryRelationType.entryAndCurriculums, GroupRoles.owner.name());
		dbInstance.commit();
		if(sendNotifications && closedBy != null) {
			sendStatusChangedNotifications(ownerList, closedBy, reloadedEntry, RepositoryMailing.Type.closeEntry);
		}
		
		RepositoryEntryStatusChangedEvent statusChangedEvent = new RepositoryEntryStatusChangedEvent(reloadedEntry.getKey());
		coordinatorManager.getCoordinator().getEventBus().fireEventToListenersOf(statusChangedEvent, OresHelper.clone(entry));
		return reloadedEntry;
	}

	@Override
	public RepositoryEntry uncloseRepositoryEntry(RepositoryEntry entry) {
		RepositoryEntry reloadedEntry = repositoryEntryDAO.loadForUpdate(entry);
		reloadedEntry.setEntryStatus(RepositoryEntryStatusEnum.published);
		reloadedEntry = dbInstance.getCurrentEntityManager().merge(reloadedEntry);
		dbInstance.commit();
		return reloadedEntry;
	}

	@Override
	public void incrementLaunchCounter(RepositoryEntry re) {
		repositoryEntryStatisticsDao.incrementLaunchCounter(re);
	}

	@Override
	public void incrementDownloadCounter(RepositoryEntry re) {
		repositoryEntryStatisticsDao.incrementDownloadCounter(re);
	}

	@Override
	public void setLastUsageNowFor(RepositoryEntry re) {
		repositoryEntryStatisticsDao.setLastUsageNowFor(re);
	}

	@Override
	public Group getDefaultGroup(RepositoryEntryRef ref) {
		return reToGroupDao.getDefaultGroup(ref);
	}

	@Override
	public List<String> getRoles(Identity identity, RepositoryEntryRef re) {
		return reToGroupDao.getRoles(identity, re);
	}

	@Override
	public boolean hasRole(Identity identity, RepositoryEntryRef re, String... roles) {
		if(re == null || identity == null) return false;
		return reToGroupDao.hasRole(identity, re, false, roles);
	}

	@Override
	public boolean hasRoleExpanded(Identity identity, RepositoryEntryRef re, String... roles) {
		if(re == null || identity == null) return false;
		return reToGroupDao.hasRole(identity, re, true, roles);
	}

	@Override
	public boolean hasRoleExpanded(Identity identity, String... roles) {
		return reToGroupDao.hasRoleExpanded(identity, roles);
	}

	@Override
	public boolean isParticipantAllowedToLeave(RepositoryEntry re) {
		boolean allowed = false;
		RepositoryEntryAllowToLeaveOptions setting = re.getAllowToLeaveOption();
		if(setting == RepositoryEntryAllowToLeaveOptions.atAnyTime) {
			allowed = true;
		} else if(setting == RepositoryEntryAllowToLeaveOptions.afterEndDate) {
			RepositoryEntryLifecycle lifecycle = re.getLifecycle();
			if(lifecycle == null || lifecycle.getValidTo() == null) {
				allowed = false;
			} else {
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.HOUR, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
				Date now = cal.getTime();
				if(now.compareTo(lifecycle.getValidTo()) >= 0) {
					allowed = true;
				} else {
					allowed = false;
				}
			}
		} else {
			allowed = false;
		}
		return allowed;
	}

	@Override
	public boolean isMember(IdentityRef identity, RepositoryEntryRef entry) {
		return reToGroupDao.isMember(identity, entry);
	}

	@Override
	public void filterMembership(IdentityRef identity, Collection<Long> entries) {
		reToGroupDao.filterMembership(identity, entries);
	}

	@Override
	public int countMembers(RepositoryEntryRef re, String role) {
		return reToGroupDao.countMembers(re, role);
	}

	@Override
	public int countMembers(List<? extends RepositoryEntryRef> res, Identity excludeMe) {
		return reToGroupDao.countMembers(res, excludeMe);
	}
	
	@Override
	public Map<String, Long> getRoleToCountMemebers(RepositoryEntryRef re) {
		return reToGroupDao.getRoleToCountMemebers(re);
	}

	@Override
	public Date getEnrollmentDate(RepositoryEntryRef re, IdentityRef identity, String... roles) {
		return reToGroupDao.getEnrollmentDate(re, identity, roles);
	}

	@Override
	public Map<Long, Date> getEnrollmentDates(RepositoryEntryRef re, String... roles) {
		return reToGroupDao.getEnrollmentDates(re, roles);
	}

	@Override
	public List<Identity> getMembers(RepositoryEntryRef re, RepositoryEntryRelationType relationType, String... roles) {
		return reToGroupDao.getMembers(Collections.singletonList(re), relationType, roles);
	}

	@Override
	public List<Identity> getMembers(List<? extends RepositoryEntryRef> res, RepositoryEntryRelationType relationType, String... roles) {
		return reToGroupDao.getMembers(res, relationType, roles);
	}
	
	@Override
	public List<Long> getMemberKeys(RepositoryEntryRef re, RepositoryEntryRelationType relationType, String... roles) {
		return reToGroupDao.getMemberKeys(re, relationType, roles);
	}

	@Override
	public List<Identity> getCoachedParticipants(IdentityRef coach, RepositoryEntryRef re) {
		return reToGroupDao.getRelatedMembers(re, coach, GroupRoles.coach, GroupRoles.participant);
	}
	
	@Override
	public List<Identity> getAssignedCoaches(IdentityRef participant, RepositoryEntryRef re) {
		return reToGroupDao.getRelatedMembers(re, participant, GroupRoles.participant, GroupRoles.coach);
	}

	@Override
	public void addRole(Identity identity, RepositoryEntry re, String role) {
		reToGroupDao.addRole(identity, re, role);
	}

	@Override
	public void removeRole(Identity identity, RepositoryEntry re, String role) {
		reToGroupDao.removeRole(identity, re, role);
	}

	@Override
	public void removeMembers(RepositoryEntry re, String... roles) {
		if(roles == null || roles.length == 0) return;
		for(String role:roles) {
			if(role != null) {
				reToGroupDao.removeRole(re, role);
			}
		}
	}

	@Override
	public List<Organisation> getOrganisations(RepositoryEntryRef entry) {
		return getOrganisations(Collections.singletonList(entry));
	}
	
	@Override
	public List<Organisation> getOrganisations(Collection<? extends RepositoryEntryRef> entries) {
		return reToGroupDao.getOrganisations(entries);
	}

	@Override
	public List<OrganisationRef> getOrganisationReferences(RepositoryEntryRef entry) {
		return repositoryEntryToOrganisationDao.getOrganisationReferences(entry);
	}
	
	@Override
	public Map<RepositoryEntryRef, List<Organisation>> getRepositoryEntryOrganisations(Collection<? extends RepositoryEntryRef> entries) {
		return repositoryEntryToOrganisationDao.getRepositoryEntryOrganisations(entries);
	}

	@Override
	public List<RepositoryEntry> getRepositoryEntryByOrganisation(OrganisationRef organisation) {
		return reToGroupDao.getRepositoryEntries(organisation);
	}

	@Override
	public void addOrganisation(RepositoryEntry entry, Organisation organisation) {
		repositoryEntryToOrganisationDao.createRelation(organisation, entry, false);
		reToGroupDao.createRelation(organisation.getGroup(), entry);
	}
	
	@Override
	public void removeOrganisation(RepositoryEntry entry, Organisation organisation) {
		Group group = organisation.getGroup();
		reToGroupDao.removeRelation(group, entry);
		repositoryEntryToOrganisationDao.delete(entry, organisation);
	}

	@Override
	public List<RepositoryEntry> searchByIdAndRefs(String idAndRefs) {
		return repositoryEntryDAO.searchByIdAndRefs(idAndRefs);
	}

	@Override
	public int countMyView(SearchMyRepositoryEntryViewParams params) {
		return myCourseViewQueries.countViews(params);
	}

	@Override
	public List<RepositoryEntryMyView> searchMyView(SearchMyRepositoryEntryViewParams params,
			int firstResult, int maxResults) {
		return myCourseViewQueries.searchViews(params, firstResult, maxResults);
	}

	@Override
	public int countAuthorView(SearchAuthorRepositoryEntryViewParams params) {
		return authorViewQueries.countViews(params);
	}

	@Override
	public RepositoryEntryAuthorViewResults searchAuthorView(SearchAuthorRepositoryEntryViewParams params,
			int firstResult, int maxResults) {
		return authorViewQueries.searchViews(params, firstResult, maxResults);
	}

	@Override
	public List<TaxonomyLevel> getTaxonomy(RepositoryEntryRef entry) {
		if(entry == null || entry.getKey() == null) return Collections.emptyList();
		return repositoryEntryToTaxonomyLevelDao.getTaxonomyLevels(entry);
	}
	
	@Override
	public Map<RepositoryEntryRef,List<TaxonomyLevel>> getTaxonomy(List<? extends RepositoryEntryRef> entries, boolean fetchParents) {
		if(entries == null || entries.isEmpty()) return Collections.emptyMap();
		return repositoryEntryToTaxonomyLevelDao.getTaxonomyLevels(entries, fetchParents);
	}

	@Override
	public void addTaxonomyLevel(RepositoryEntry entry, TaxonomyLevel level) {
		repositoryEntryToTaxonomyLevelDao.createRelation(entry, level);
	}

	@Override
	public void removeTaxonomyLevel(RepositoryEntry entry, TaxonomyLevel level) {
		repositoryEntryToTaxonomyLevelDao.deleteRelation(entry, level);
	}

	@Override
	public List<RepositoryEntry> getRepositoryEntryByTaxonomy(TaxonomyLevelRef taxonomyLevel) {
		return repositoryEntryToTaxonomyLevelDao.getRepositoryEntries(taxonomyLevel);
	}

	@Override
	public boolean deleteOrganisationData(Organisation organisation, Organisation replacementOrganisation) {
		if(replacementOrganisation != null) {
			List<RepositoryEntry> entries = reToGroupDao.getRepositoryEntries(organisation);
			for(RepositoryEntry entry:entries) {
				List<Organisation> currentOrganisationsByGroups = getOrganisations(entry);
				if(!currentOrganisationsByGroups.contains(replacementOrganisation)) {
					RepositoryEntryToGroupRelation relToGroup = reToGroupDao.createRelation(replacementOrganisation.getGroup(), entry);
					entry.getGroups().add(relToGroup);
				}
				
				boolean addReplacement = true;
				for(RepositoryEntryToOrganisation reToOrganisation:entry.getOrganisations()) {
					if(reToOrganisation.getOrganisation().equals(replacementOrganisation)) {
						addReplacement = false;
					}
				}
				
				if(addReplacement) {
					RepositoryEntryToOrganisation newRelation = repositoryEntryToOrganisationDao.createRelation(replacementOrganisation, entry, false);
					entry.getOrganisations().add(newRelation);
				}
				dbInstance.getCurrentEntityManager().merge(entry);
			}
		}
		
		repositoryEntryToOrganisationDao.delete(organisation);
		reToGroupDao.removeRelation(organisation.getGroup());
		
		return true;
	}
}