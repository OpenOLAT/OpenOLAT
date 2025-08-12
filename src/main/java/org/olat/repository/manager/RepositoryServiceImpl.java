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
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupMembership;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationDataDeletable;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.basesecurity.manager.GroupMembershipHistoryDAO;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
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
import org.olat.core.util.event.MultiUserEvent;
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
import org.olat.course.todo.CourseToDoService;
import org.olat.group.BusinessGroup;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.ims.lti13.manager.LTI13SharedToolDeploymentDAO;
import org.olat.ims.qti21.manager.AssessmentTestSessionDAO;
import org.olat.modules.assessment.manager.AssessmentEntryDAO;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.manager.CurriculumElementDAO;
import org.olat.modules.invitation.manager.InvitationDAO;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.reminder.manager.ReminderDAO;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.repository.ErrorList;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryAllowToLeaveOptions;
import org.olat.repository.RepositoryEntryAuditLog;
import org.olat.repository.RepositoryEntryAuthorViewResults;
import org.olat.repository.RepositoryEntryDataDeletable;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryEntryMyView;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryEntryRuntimeType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryEntryToOrganisation;
import org.olat.repository.RepositoryEntryToTaxonomyLevel;
import org.olat.repository.RepositoryMailing;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.model.MembershipInfos;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.repository.model.RepositoryEntryPermanentlyDeletedEvent;
import org.olat.repository.model.RepositoryEntryStatistics;
import org.olat.repository.model.RepositoryEntryStatusChangedEvent;
import org.olat.repository.model.RepositoryEntryToGroupRelation;
import org.olat.repository.model.SearchAuthorRepositoryEntryViewParams;
import org.olat.repository.model.SearchMyRepositoryEntryViewParams;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.manager.ACOfferDAO;
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
	private GroupMembershipHistoryDAO groupMembershipHistoryDao;
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
	private RepositoryTemplateRelationDAO templateToGroupDao;
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
	private InvitationDAO invitationDao;
	@Autowired
	private AssessmentEntryDAO assessmentEntryDao;
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private RepositoryEntryToOrganisationDAO repositoryEntryToOrganisationDao;
	@Autowired
	private RepositoryEntryToTaxonomyLevelDAO repositoryEntryToTaxonomyLevelDao;
	@Autowired
	private RepositoryEntryAuditLogDAO repositoryEntryAuditLogDAO;
	@Autowired
	private NotificationsManager notificationsManager;
	@Autowired
	private OpenBadgesManager openBadgesManager;
	@Autowired
	private CurriculumModule curriculumModule;
	@Autowired
	private CurriculumElementDAO curriculumElementDAO;
	@Autowired
	private LTI13SharedToolDeploymentDAO lti13SharedToolDeploymentDAO;
	@Autowired
	private ACOfferDAO acOfferDAO;

	@Autowired
	private LifeFullIndexer lifeIndexer;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;

	@Override
	public RepositoryEntry create(Identity initialAuthor, String initialAuthorAlt, String resourceName,
			String displayname, String description, OLATResource resource, RepositoryEntryStatusEnum status,
			RepositoryEntryRuntimeType runtimeType, Organisation organisation) {
		return create(initialAuthorAlt, initialAuthor, resourceName, displayname, description, resource,
				status, runtimeType, organisation);
	}

	private RepositoryEntry create(String initialAuthorName, Identity initialAuthor, String resourceName,
			String displayname, String description, OLATResource resource, RepositoryEntryStatusEnum status,
			RepositoryEntryRuntimeType runtimeType, Organisation organisation) {
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
		re.setCanIndexMetadata(false);
		re.setCanDownload(false);
		re.setCanCopy(false);
		re.setCanReference(false);
		re.setDisplayname(displayname);
		re.setResourcename(StringHelper.containsNonWhitespace(resourceName) ? resourceName : "-");
		re.setDescription(description == null ? "" : description);
		re.setRuntimeType(runtimeType);
		re.setAllowToLeaveOption(repositoryModule.getAllowToLeaveDefaultOption());
		re.setInvitationByOwnerWithAuthorRightsEnabled(false);
		re.setLTI13DeploymentByOwnerWithAuthorRightsEnabled(false);
		re.setVideoCollection(false);
		
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
	public RepositoryEntry copy(RepositoryEntry sourceEntry, Identity author, String displayname, String externalRef) {
		OLATResource sourceResource = sourceEntry.getOlatResource();
		OLATResource copyResource = resourceManager.createOLATResourceInstance(sourceResource.getResourceableTypeName());
		RepositoryEntry copyEntry = create(author, null, sourceEntry.getResourcename(), displayname,
				sourceEntry.getDescription(), copyResource, RepositoryEntryStatusEnum.preparation, 
				sourceEntry.getRuntimeType(), null);

		//copy all fields
		copyEntry.setExternalRef(externalRef);
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
	public RepositoryEntry loadBy(RepositoryEntryRef ref) {
		if(ref == null || ref.getKey() == null) return null;
		return repositoryEntryDAO.loadByKey(ref.getKey());
	}

	@Override
	public List<RepositoryEntry> loadByKeys(Collection<Long> keys) {
		return repositoryEntryDAO.loadByKeys(keys);
	}

	@Override
	public List<RepositoryEntry> loadRepositoryForMetadata(RepositoryEntryStatusEnum status) {
		return repositoryEntryDAO.loadForMetaData(status);
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
	public RepositoryEntry loadByResourceId(String resourceTypeName, Long resourceId) {
		return repositoryEntryDAO.loadByResourceId(resourceTypeName, resourceId);
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
		re = loadByKey(re.getKey());
		// for rest-service, if already deleted course gets deleted again
		if (re.getEntryStatus() == RepositoryEntryStatusEnum.trash) {
			return re;
		}
		String before = toAuditXml(re);

		// start delete
		RepositoryEntry reloadedRe = repositoryEntryDAO.loadForUpdate(re);
		reloadedRe.setEntryStatus(RepositoryEntryStatusEnum.trash);
		
		if(reloadedRe.getDeletionDate() == null) {
			// don't write the name of an admin which make a restore -> delete operation
			reloadedRe.setDeletedBy(deletedBy);
			reloadedRe.setDeletionDate(new Date());
			reloadedRe.setLastModified(reloadedRe.getDeletionDate());
		}
		reloadedRe = dbInstance.getCurrentEntityManager().merge(reloadedRe);
		dbInstance.commit();
		
		acService.getOffers(reloadedRe, true, false, null, false, null, null).stream()
				.filter(offer -> offer.isGuestAccess() || offer.isOpenAccess())
				.forEach(offer -> acService.deleteOffer(offer));
		
		List<Identity> ownerList = reToGroupDao.getMembers(reloadedRe, RepositoryEntryRelationType.entryAndCurriculums, GroupRoles.owner.name());

		// unsubscribe everyone except owners
		List<Identity> coachParticipantWaitingList =
				reToGroupDao.getMembers(reloadedRe, RepositoryEntryRelationType.entryAndCurriculums,
						GroupRoles.coach.name(), GroupRoles.participant.name(), GroupRoles.waiting.name());
		OLATResource resource = loadRepositoryEntryResource(reloadedRe.getKey());
		for (Identity identityToUnsubscribe : coachParticipantWaitingList) {
			if (!hasRole(identityToUnsubscribe, reloadedRe, GroupRoles.owner.name())) {
				notificationsManager.unsubscribeAllForIdentityAndResId(identityToUnsubscribe, resource.getResourceableId());
			}
		}

		// first stop assessment mode if needed
		assessmentModeCoordinationService.processRepositoryEntryChangedStatus(reloadedRe);
		//remove from catalog
		catalogManager.resourceableDeleted(reloadedRe);
		//remove participant and coach
		List<GroupMembership> memberships;
		if(owners) {
			memberships = removeMembersResourceDeleted(reloadedRe, GroupRoles.owner.name(), GroupRoles.coach.name(), GroupRoles.participant.name(), GroupRoles.waiting.name());
		} else {
			memberships = removeMembersResourceDeleted(reloadedRe, GroupRoles.coach.name(), GroupRoles.participant.name(), GroupRoles.waiting.name());
		}

		//delete reservations
		reservationDao.deleteReservations(resource);
		
		//remove relation to business groups
		List<RepositoryEntryToGroupRelation> relations = reToGroupDao.getBusinessGroupAndCurriculumRelations(reloadedRe);
		for(RepositoryEntryToGroupRelation relation:relations) {
			if(!relation.isDefaultGroup()) {
				reToGroupDao.removeRelation(relation);
			}
		}
		dbInstance.commitAndCloseSession();

		Group defaultGroup = reToGroupDao.getDefaultGroup(re);
		groupMembershipHistoryDao.saveMembershipsHistoryOfDeletedResourceAndCommit(defaultGroup, memberships, deletedBy);
		dbInstance.commitAndCloseSession();
		
		if(sendNotifications && deletedBy != null) {
			sendStatusChangedNotifications(ownerList, deletedBy, reloadedRe, RepositoryMailing.Type.deleteSoftEntry);
		}
		
		RepositoryEntryStatusChangedEvent statusChangedEvent = new RepositoryEntryStatusChangedEvent(reloadedRe.getKey());
		coordinatorManager.getCoordinator().getEventBus().fireEventToListenersOf(statusChangedEvent, OresHelper.clone(reloadedRe));

		String after = toAuditXml(reloadedRe);
		auditLog(RepositoryEntryAuditLog.Action.statusChange, before, after, reloadedRe, deletedBy);

		return reloadedRe;
	}
	
	private List<GroupMembership> removeMembersResourceDeleted(RepositoryEntry re, String... roles) {
		if(roles == null || roles.length == 0) return new ArrayList<>();
		
		List<GroupMembership> removedMemberships = new ArrayList<>();
		
		Group group = reToGroupDao.getDefaultGroup(re);
		for(String role:roles) {
			if(role != null) {
				List<GroupMembership> memberships = groupDao.getMemberships(group, role, true);
				removedMemberships.addAll(memberships);
				reToGroupDao.removeRole(re, role);
			}
		}
		
		return removedMemberships;
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
	public RepositoryEntry restoreRepositoryEntry(RepositoryEntry entry, Identity restoredBy) {
		entry = loadByKey(entry.getKey());
		if (entry.getEntryStatus() == RepositoryEntryStatusEnum.closed
				|| entry.getEntryStatus() == RepositoryEntryStatusEnum.preparation) {
			return entry;
		}
		String before = toAuditXml(entry);

		RepositoryEntry reloadedRe = repositoryEntryDAO.loadForUpdate(entry);
		acService.getOffers(entry, true, false, null, false, null, null).stream()
				.filter(offer -> offer.isGuestAccess() || offer.isOpenAccess())
				.forEach(offer -> acService.deleteOffer(offer));
		
		if("CourseModule".equals(reloadedRe.getOlatResource().getResourceableTypeName())) {
			reloadedRe.setEntryStatus(RepositoryEntryStatusEnum.closed);
		} else {
			reloadedRe.setEntryStatus(RepositoryEntryStatusEnum.preparation);
		}
		reloadedRe.setLastModified(new Date());
		reloadedRe = dbInstance.getCurrentEntityManager().merge(reloadedRe);
		dbInstance.commit();
		
		RepositoryEntryStatusChangedEvent statusChangedEvent = new RepositoryEntryStatusChangedEvent(reloadedRe.getKey());
		coordinatorManager.getCoordinator().getEventBus().fireEventToListenersOf(statusChangedEvent, OresHelper.clone(reloadedRe));

		String after = toAuditXml(reloadedRe);
		auditLog(RepositoryEntryAuditLog.Action.statusChange, before, after, reloadedRe, restoredBy);

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
		
		MultiUserEvent event = new RepositoryEntryPermanentlyDeletedEvent(entry.getKey());
		coordinatorManager.getCoordinator().getEventBus().fireEventToListenersOf(event, OresHelper.clone(entry));

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
		// delete badge configurations
		openBadgesManager.deleteConfiguration(entry);
		openBadgesManager.removeCourseEntryFromCourseBadgeClasses(entry);
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
		//delete all course to-dos
		CoreSpringFactory.getImpl(CourseToDoService.class).deleteToDoTasks(entry);
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
		deleteRepositoryEntryAndBaseGroups(entry, identity);

		log.info(Tracing.M_AUDIT, "deleteRepositoryEntry Done entry={}", entry);
		return errors;
	}

	/**
	 *
	 * @param entry
	 * @param doer
	 */
	@Override
	public void deleteRepositoryEntryAndBaseGroups(RepositoryEntry entry, Identity doer) {
		CurriculumService curriculumService = CoreSpringFactory.getImpl(CurriculumService.class);
		RepositoryEntry reloadedEntry = dbInstance.getCurrentEntityManager()
				.getReference(RepositoryEntry.class, entry.getKey());
		Long resourceKey = reloadedEntry.getOlatResource().getKey();

		Group defaultGroup = reToGroupDao.getDefaultGroup(reloadedEntry);
		if(defaultGroup != null) {
			groupDao.removeMemberships(defaultGroup);
			groupMembershipHistoryDao.deleteMembershipHistory(defaultGroup);
		}
		List<BusinessGroup> internalGroups = curriculumService.deleteInternalGroupMembershipsAndInvitations(reloadedEntry);
		reToGroupDao.removeRelations(reloadedEntry);
		templateToGroupDao.deleteRelations(reloadedEntry);
		dbInstance.commit();
		
		// Delete the invitations which are hold by the default group
		invitationDao.deleteInvitation(defaultGroup);
		
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
			curriculumService.deleteInternalGroups(internalGroups, doer);
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
		entry = loadByKey(entry.getKey());
		if (entry.getEntryStatus() == RepositoryEntryStatusEnum.closed) {
			return entry;
		}
		String before = toAuditXml(entry);

		RepositoryEntry reloadedEntry = repositoryEntryDAO.loadForUpdate(entry);
		reloadedEntry.setEntryStatus(RepositoryEntryStatusEnum.closed);
		reloadedEntry.setLastModified(new Date());
		reloadedEntry = dbInstance.getCurrentEntityManager().merge(reloadedEntry);
		List<Identity> ownerList = reToGroupDao.getMembers(reloadedEntry, RepositoryEntryRelationType.entryAndCurriculums, GroupRoles.owner.name());
		dbInstance.commit();
		if(sendNotifications && closedBy != null) {
			sendStatusChangedNotifications(ownerList, closedBy, reloadedEntry, RepositoryMailing.Type.closeEntry);
		}
		
		RepositoryEntryStatusChangedEvent statusChangedEvent = new RepositoryEntryStatusChangedEvent(reloadedEntry.getKey());
		coordinatorManager.getCoordinator().getEventBus().fireEventToListenersOf(statusChangedEvent, OresHelper.clone(entry));

		String after = toAuditXml(reloadedEntry);
		auditLog(RepositoryEntryAuditLog.Action.statusChange, before, after, reloadedEntry, closedBy);

		return reloadedEntry;
	}

	@Override
	public RepositoryEntry uncloseRepositoryEntry(RepositoryEntry entry, Identity unclosedBy) {
		entry = loadByKey(entry.getKey());
		if (entry.getEntryStatus() == RepositoryEntryStatusEnum.published) {
			return entry;
		}
		String before = toAuditXml(entry);

		RepositoryEntry reloadedEntry = repositoryEntryDAO.loadForUpdate(entry);
		reloadedEntry.setEntryStatus(RepositoryEntryStatusEnum.published);
		reloadedEntry.setLastModified(new Date());
		reloadedEntry = dbInstance.getCurrentEntityManager().merge(reloadedEntry);
		dbInstance.commit();
		
		RepositoryEntryStatusChangedEvent statusChangedEvent = new RepositoryEntryStatusChangedEvent(entry.getKey());
		coordinatorManager.getCoordinator().getEventBus().fireEventToListenersOf(statusChangedEvent, OresHelper.clone(entry));

		String after = toAuditXml(reloadedEntry);
		auditLog(RepositoryEntryAuditLog.Action.statusChange, before, after, reloadedEntry, unclosedBy);

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
	public boolean hasUserManaged(RepositoryEntryRef re) {
		return reToGroupDao.hasBusinessGroupAndCurriculumRelations(re)
				|| !reToGroupDao.getMemberKeys(re, RepositoryEntryRelationType.defaultGroup, GroupRoles.participant.name(), GroupRoles.coach.name()).isEmpty();
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
	public int countMembers(RepositoryEntryRef re, RepositoryEntryRelationType relationType, String role) {
		return reToGroupDao.countMembers(re, relationType, role);
	}

	@Override
	public int countMembers(List<? extends RepositoryEntryRef> res, Identity excludeMe) {
		return reToGroupDao.countMembers(res, excludeMe);
	}
	
	@Override
	public Map<Long, Long> getRepoKeyToCountMembers(List<? extends RepositoryEntryRef> res, String... roles) {
		return reToGroupDao.getRepoKeyToCountMembers(res, roles);
	}
	
	@Override
	public Map<String, Long> getRoleToCountMemebers(RepositoryEntryRef re) {
		return reToGroupDao.getRoleToCountMembers(re, false);
	}

	@Override
	public Map<Long, Date> getEnrollmentDates(RepositoryEntryRef re, Collection<? extends IdentityRef> identities, String... roles) {
		return reToGroupDao.getEnrollmentDates(re, identities, roles);
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
	public List<MembershipInfos> getMemberships(List<RepositoryEntryRef> entries, String role) {
		return reToGroupDao.getMemberships(entries, role);
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
	public PublisherData getPublisherData() {
		// businesspath is authoring environment
		return new PublisherData(RepositoryEntryChangeNotificationHandler.TYPE, "", "[RepositorySite:0]");
	}

	@Override
	public SubscriptionContext getSubscriptionContext() {
		return new SubscriptionContext(RepositoryEntryChangeNotificationHandler.TYPE, 0L, "");
	}

	@Override
	public String toAuditXml(RepositoryEntry repositoryEntry) {
		return repositoryEntryAuditLogDAO.toXml(repositoryEntry);
	}

	@Override
	public RepositoryEntry toAuditRepositoryEntry(String xml) {
		return repositoryEntryAuditLogDAO.repositoryEntryFromXml(xml);
	}

	@Override
	public void auditLog(RepositoryEntryAuditLog.Action action, String before, String after,
						 RepositoryEntry entry, Identity author) {
		if (Objects.equals(before, after)) {
			// do not log actions when there are no changes
			return;
		}
		repositoryEntryAuditLogDAO.auditLog(action, before, after, entry, author);
		notificationsManager.markPublisherNews(getSubscriptionContext(), author, true);
	}

	@Override
	public List<RepositoryEntryAuditLog> getAuditLogs(RepositoryEntryAuditLogSearchParams searchParams) {
		return repositoryEntryAuditLogDAO.getAuditLogs(searchParams);
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
	public boolean isTemplateInUse(RepositoryEntryRef template) {
		return templateToGroupDao.hasRelations(template);
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
	public Map<RepositoryEntryRef, AtomicLong> getNumOfTaxonomyLevels(List<? extends RepositoryEntryRef> entries) {
		if(entries == null || entries.isEmpty()) return Collections.emptyMap();
		return repositoryEntryToTaxonomyLevelDao.getNumOfTaxonomyLevels(entries);
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

	@Override
	public RuntimeTypeCheckDetails canSwitchTo(RepositoryEntry entry, RepositoryEntryRuntimeType runtimeType) {
		if ("CourseModule".equals(entry.getOlatResource().getResourceableTypeName())) {
			switch (runtimeType) {
				case embedded -> {
					return RuntimeTypeCheckDetails.wrongState;
				}
				case standalone -> {
					if (curriculumElementDAO.countElements(entry) > 0) {
						return RuntimeTypeCheckDetails.curriculumElementExists;
					}
					if (templateToGroupDao.hasRelations(entry)) {
						return RuntimeTypeCheckDetails.isTemplate;
					}
				}
				case curricular -> {
					return canSwitchToCurricular(entry);
				}
				case template -> {
					return RuntimeTypeCheckDetails.isTemplate;
				}
			}
		} else {
			if (runtimeType == RepositoryEntryRuntimeType.curricular) {
				return RuntimeTypeCheckDetails.wrongState;
			}
		}

		return RuntimeTypeCheckDetails.ok;
	}

	@Override
	public RuntimeTypesAndCheckDetails allowedRuntimeTypes(RepositoryEntry entry) {
		Set<RepositoryEntryRuntimeType> runtimeTypes = new HashSet<>();
		RuntimeTypeCheckDetails checkDetails = RuntimeTypeCheckDetails.ok;
		
		if ("CourseModule".equals(entry.getOlatResource().getResourceableTypeName())) {
			if(entry.getRuntimeType() == RepositoryEntryRuntimeType.template) {
				if(templateToGroupDao.hasRelations(entry)) {
					checkDetails = RuntimeTypeCheckDetails.isTemplate;
				} else {
					runtimeTypes.add(RepositoryEntryRuntimeType.standalone);
					if (curriculumModule.isEnabled()) {
						runtimeTypes.add(RepositoryEntryRuntimeType.curricular);
					}
				}
			} else {
				if (curriculumElementDAO.countElements(entry) == 0) {
					runtimeTypes.add(RepositoryEntryRuntimeType.standalone);
				} else {
					if (curriculumModule.isEnabled() && RepositoryEntryRuntimeType.curricular.equals(entry.getRuntimeType())) {
						checkDetails = RuntimeTypeCheckDetails.curriculumElementExists;
					}
				}
				if (curriculumModule.isEnabled()) {
					checkDetails = canSwitchToCurricular(entry);
					if (checkDetails.equals(RuntimeTypeCheckDetails.ok)) {
						runtimeTypes.add(RepositoryEntryRuntimeType.curricular);
					}
				}
				
				if(entry.getRuntimeType() == RepositoryEntryRuntimeType.standalone) {
					RuntimeTypeCheckDetails templateCheckDetails = canSwitchStandaloneToTemplate(entry);
					if(templateCheckDetails.equals(RuntimeTypeCheckDetails.ok) ) {
						runtimeTypes.add(RepositoryEntryRuntimeType.template);
					} else if(checkDetails.equals(RuntimeTypeCheckDetails.ok)) {
						checkDetails = templateCheckDetails;
					}
				}
			}
		} else {
			runtimeTypes.add(RepositoryEntryRuntimeType.embedded);
			runtimeTypes.add(RepositoryEntryRuntimeType.standalone);
		}

		return new RuntimeTypesAndCheckDetails(runtimeTypes, checkDetails);
	}
	
	private RuntimeTypeCheckDetails canSwitchStandaloneToTemplate(RepositoryEntry entry) {
		if(reToGroupDao.hasMembers(entry, GroupRoles.participant.name())) {
			return RuntimeTypeCheckDetails.participantExists;
		}
		if(reToGroupDao.hasMembers(entry, GroupRoles.coach.name())) {
			return RuntimeTypeCheckDetails.coachExists;
		}
		
		// Don't use autowired here to prevent dependency cycles
		if(CoreSpringFactory.getImpl(LectureModule.class).isEnabled()
				&& CoreSpringFactory.getImpl(LectureService.class).getRepositoryEntryLectureConfiguration(entry).isLectureEnabled()
				&& CoreSpringFactory.getImpl(LectureService.class).hasLectureBlocks(entry)) {
			return RuntimeTypeCheckDetails.lectureEnabled;
		}
		return RuntimeTypeCheckDetails.ok;
	}

	private RuntimeTypeCheckDetails canSwitchToCurricular(RepositoryEntry entry) {
		Map<String, Long> roleToCount = reToGroupDao.getRoleToCountMembers(entry, true);

		if (roleToCount.containsKey(GroupRoles.participant.name()) && roleToCount.get(GroupRoles.participant.name()) > 0) {
			return RuntimeTypeCheckDetails.participantExists;
		}
		if (roleToCount.containsKey(GroupRoles.coach.name()) && roleToCount.get(GroupRoles.coach.name()) > 0) {
			return RuntimeTypeCheckDetails.coachExists;
		}
		if(templateToGroupDao.hasRelations(entry)) {
			return RuntimeTypeCheckDetails.isTemplate;
		}
		if (lti13SharedToolDeploymentDAO.getSharedToolDeploymentCount(entry) > 0) {
			return RuntimeTypeCheckDetails.ltiDeploymentExists;
		}
		if (RepositoryEntryRuntimeType.curricular.equals(entry.getRuntimeType()) && curriculumElementDAO.countElements(entry) > 0) {
			return RuntimeTypeCheckDetails.curriculumElementExists;
		}
		if (acOfferDAO.offerExists(entry.getOlatResource())) {
			return RuntimeTypeCheckDetails.offerExists;
		}
		return checkGroupsForSwitchingToCurricular(entry);
	}

	private RuntimeTypeCheckDetails checkGroupsForSwitchingToCurricular(RepositoryEntry entry) {
		if (businessGroupRelationDao.hasGroupWithCourses(entry)) {
			return RuntimeTypeCheckDetails.groupWithOtherCoursesExists;
		}
		if (businessGroupRelationDao.hasGroupWithOffersOrLtiDeployments(entry)) {
			return RuntimeTypeCheckDetails.groupWithOffersOrLtiExists;
		}
		return RuntimeTypeCheckDetails.ok;
	}

	@Override
	public Set<RepositoryEntryRuntimeType> getPossibleRuntimeTypes(Collection<RepositoryEntry> entries) {
		HashSet<RepositoryEntryRuntimeType> types = new HashSet<>();
		for (RepositoryEntry entry : entries) {
			if ("CourseModule".equals(entry.getOlatResource().getResourceableTypeName())) {
				types.add(RepositoryEntryRuntimeType.standalone);
				if (curriculumModule.isEnabled()) {
					types.add(RepositoryEntryRuntimeType.curricular);
				}
			} else {
				types.add(RepositoryEntryRuntimeType.standalone);
				types.add(RepositoryEntryRuntimeType.embedded);
			}
		}
		return types;
	}

	@Override
	public Set<RepositoryEntryRuntimeType> getPossibleRuntimeTypes(RepositoryEntry entry) {
		Set<RepositoryEntryRuntimeType> types = EnumSet.noneOf(RepositoryEntryRuntimeType.class);
		if ("CourseModule".equals(entry.getOlatResource().getResourceableTypeName())) {
			types.add(RepositoryEntryRuntimeType.standalone);
			types.add(RepositoryEntryRuntimeType.template);
			if (curriculumModule.isEnabled()) {
				types.add(RepositoryEntryRuntimeType.curricular);
			}
		} else {
			types.add(RepositoryEntryRuntimeType.standalone);
			types.add(RepositoryEntryRuntimeType.embedded);
		}
		return types;
	}

	@Override
	public boolean canEditRuntimeType(RepositoryEntry entry) {
		if ("CourseModule".equals(entry.getOlatResource().getResourceableTypeName())) {
			if (curriculumModule.isEnabled()) {
				return true;
			}
			return false;
		}
		return true;
	}

	@Override
	public RepositoryEntryRuntimeType getDefaultRuntimeType(OLATResource resource) {
		if ("CourseModule".equals(resource.getResourceableTypeName())) {
			if (curriculumModule.isEnabled()) {
				return curriculumModule.getDefaultCourseRuntimeType();
			}
			return RepositoryEntryRuntimeType.standalone;
		}
		return null;
	}

	@Override
	public boolean isMixedSetup(RepositoryEntry entry) {
		if ("CourseModule".equals(entry.getOlatResource().getResourceableTypeName())) {
			if (!RepositoryEntryRuntimeType.standalone.equals(entry.getRuntimeType())) {
				return false;
			}
			if (curriculumModule.isEnabled()) {
				return curriculumElementDAO.countElements(entry) > 0;
			}
		}
		return false;
	}
}