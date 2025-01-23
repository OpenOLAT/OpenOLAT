/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.manager;

import static org.olat.modules.curriculum.CurriculumElementRepositoryEntryEvent.REPOSITORY_ENTRY_ADDED;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupMembership;
import org.olat.basesecurity.GroupMembershipInheritance;
import org.olat.basesecurity.GroupMembershipStatus;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationDataDeletable;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.basesecurity.manager.GroupMembershipHistoryDAO;
import org.olat.basesecurity.model.IdentityToRoleKey;
import org.olat.commons.info.InfoMessage;
import org.olat.commons.info.InfoMessageFrontendManager;
import org.olat.commons.info.InfoMessageManager;
import org.olat.commons.info.InfoMessageToCurriculumElement;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.EventBus;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupLifecycleManager;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.ims.lti13.LTI13Service;
import org.olat.modules.coach.manager.CoachingDAO;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumDataDeletable;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementFileType;
import org.olat.modules.curriculum.CurriculumElementManagedFlag;
import org.olat.modules.curriculum.CurriculumElementMembership;
import org.olat.modules.curriculum.CurriculumElementMembershipEvent;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumElementRepositoryEntryEvent;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementToTaxonomyLevel;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumElementTypeManagedFlag;
import org.olat.modules.curriculum.CurriculumElementTypeRef;
import org.olat.modules.curriculum.CurriculumElementTypeToType;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.CurriculumStatus;
import org.olat.modules.curriculum.model.CurriculumCopySettings;
import org.olat.modules.curriculum.model.CurriculumCopySettings.CopyResources;
import org.olat.modules.curriculum.model.CurriculumElementImpl;
import org.olat.modules.curriculum.model.CurriculumElementInfos;
import org.olat.modules.curriculum.model.CurriculumElementInfosSearchParams;
import org.olat.modules.curriculum.model.CurriculumElementMembershipChange;
import org.olat.modules.curriculum.model.CurriculumElementMembershipHistory;
import org.olat.modules.curriculum.model.CurriculumElementMembershipHistorySearchParameters;
import org.olat.modules.curriculum.model.CurriculumElementNode;
import org.olat.modules.curriculum.model.CurriculumElementRepositoryEntryViews;
import org.olat.modules.curriculum.model.CurriculumElementSearchInfos;
import org.olat.modules.curriculum.model.CurriculumElementSearchParams;
import org.olat.modules.curriculum.model.CurriculumElementWebDAVInfos;
import org.olat.modules.curriculum.model.CurriculumElementWithParents;
import org.olat.modules.curriculum.model.CurriculumImpl;
import org.olat.modules.curriculum.model.CurriculumInfos;
import org.olat.modules.curriculum.model.CurriculumMember;
import org.olat.modules.curriculum.model.CurriculumMemberStats;
import org.olat.modules.curriculum.model.CurriculumRefImpl;
import org.olat.modules.curriculum.model.CurriculumSearchParameters;
import org.olat.modules.curriculum.model.SearchMemberParameters;
import org.olat.modules.curriculum.site.CurriculumElementTreeRowComparator;
import org.olat.modules.curriculum.ui.CurriculumMailing;
import org.olat.modules.curriculum.ui.member.ConfirmationByEnum;
import org.olat.modules.curriculum.ui.member.ResourceToRoleKey;
import org.olat.modules.invitation.manager.InvitationDAO;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.manager.LectureBlockDAO;
import org.olat.modules.lecture.manager.LectureBlockToGroupDAO;
import org.olat.modules.lecture.model.LectureBlockImpl;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryMyView;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryRuntimeType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.RepositoryEntryDAO;
import org.olat.repository.manager.RepositoryEntryMyCourseQueries;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.repository.model.RepositoryEntryToGroupRelation;
import org.olat.repository.model.SearchMyRepositoryEntryViewParams;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.Order;
import org.olat.resource.accesscontrol.OrderStatus;
import org.olat.resource.accesscontrol.ResourceReservation;
import org.olat.resource.accesscontrol.manager.ACReservationDAO;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 9 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service
public class CurriculumServiceImpl implements CurriculumService, OrganisationDataDeletable, InitializingBean {
	
	private static final Logger log = Tracing.createLoggerFor(CurriculumServiceImpl.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private CoachingDAO coachingDao;
	@Autowired
	private CurriculumDAO curriculumDao;
	@Autowired
	private CurriculumStorage curriculumStorage;
	@Autowired
	private CurriculumMemberQueries memberQueries;
	@Autowired
	private InfoMessageManager infoMessageManager;
	@Autowired
	private LectureBlockToGroupDAO lectureBlockToGroupDao;
	@Autowired
	private RepositoryEntryMyCourseQueries myCourseQueries;
	@Autowired
	private GroupMembershipHistoryDAO groupMembershipHistoryDao;
	@Autowired
	private ACReservationDAO reservationDao;
	@Autowired
	private ACService acService;
	@Autowired
	private I18nModule i18nModule;
	@Autowired
	private LectureBlockDAO lectureBlockDao;
	@Autowired
	private RepositoryEntryDAO repositoryEntryDao;
	@Autowired
	private CurriculumElementDAO curriculumElementDao;
	@Autowired
	private CurriculumElementTypeDAO curriculumElementTypeDao;
	@Autowired
	private CurriculumElementTypeToTypeDAO curriculumElementTypeToTypeDao;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	@Autowired
	private CurriculumElementToTaxonomyLevelDAO curriculumElementToTaxonomyLevelDao;
	@Autowired
	private CurriculumRepositoryEntryRelationDAO curriculumRepositoryEntryRelationDao;
	@Autowired
	private CoordinatorManager coordinator;
	@Autowired
	private BusinessGroupDAO businessGroupDAO;
	@Autowired
	private InvitationDAO invitationDao;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;

	@Override
	public void afterPropertiesSet() throws Exception {
		List<CurriculumElementType> defaultTypes = curriculumElementTypeDao.loadByExternalId(DEFAULT_CURRICULUM_ELEMENT_TYPE);
		if(defaultTypes == null || defaultTypes.isEmpty()) {
			CurriculumElementType defaultType = createCurriculumElementType(DEFAULT_CURRICULUM_ELEMENT_TYPE,
					"Default type", "", DEFAULT_CURRICULUM_ELEMENT_TYPE);
			defaultType.setAllowedAsRootElement(true);
			defaultType.setMaxRepositoryEntryRelations(-1);
			defaultType.setSingleElement(false);
			defaultType.setManagedFlags(new CurriculumElementTypeManagedFlag[] {
					CurriculumElementTypeManagedFlag.identifier, CurriculumElementTypeManagedFlag.externalId,
					CurriculumElementTypeManagedFlag.allowAsRoot, CurriculumElementTypeManagedFlag.composite,
					CurriculumElementTypeManagedFlag.maxEntryRelations });
			updateCurriculumElementType(defaultType);
			log.info("Default curriculum element type created.");
		}
		dbInstance.commitAndCloseSession();
	}

	@Override
	public Curriculum createCurriculum(String identifier, String displayName, String description, boolean lecturesEnabled, Organisation organisation) {
		return curriculumDao.createAndPersist(identifier, displayName, description, lecturesEnabled, organisation);
	}

	@Override
	public Curriculum getCurriculum(CurriculumRef ref) {
		return curriculumDao.loadByKey(ref.getKey());
	}

	@Override
	public Curriculum updateCurriculum(Curriculum curriculum) {
		return curriculumDao.update(curriculum);
	}
	
	@Override
	public void deleteCurriculum(CurriculumRef curriculumRef) {
		CurriculumImpl curriculum = (CurriculumImpl)getCurriculum(curriculumRef);
		boolean deleted = true;
		for(CurriculumElement rootElement:curriculum.getRootElements()) {
			deleted &= deleteCurriculumElement(rootElement);
		}
		dbInstance.commitAndCloseSession();
		curriculum = (CurriculumImpl)getCurriculum(curriculumRef);
		if(deleted) {
			curriculumDao.delete(curriculum);
		} else {
			curriculumDao.flagAsDelete(curriculum);
		}
		
		dbInstance.commit();
	}

	@Override
	public List<Curriculum> getCurriculums(Collection<? extends CurriculumRef> refs) {
		return curriculumDao.loadByKeys(refs);
	}
	
	@Override
	public List<CurriculumMember> getCurriculumMembers(SearchMemberParameters params) {
		return memberQueries.getCurriculumMembers(params);
	}

	@Override
	public List<Identity> getMembersIdentity(CurriculumRef curriculum, CurriculumRoles role) {
		return curriculumDao.getMembersIdentity(curriculum, role.name());
	}

	@Override
	public boolean hasRoleExpanded(CurriculumRef curriculum, IdentityRef identity, String... roles) {
		return curriculumDao.hasRoleExpanded(curriculum, identity, roles);
	}

	@Override
	public boolean isCurriculumOwner(IdentityRef identity) {
		return curriculumDao.hasCurriculumRole(identity, CurriculumRoles.curriculumowner.name());
	}

	@Override
	public boolean isCurriculumOwnerUptoEntryOwner(IdentityRef identity) {
		return curriculumDao.hasCurriculumRole(identity, CurriculumRoles.curriculumowner.name())
				|| curriculumDao.hasOwnerRoleInCurriculumElement(identity)
				|| curriculumElementDao.hasCurriculumElementRole(identity, CurriculumRoles.curriculumelementowner.name());
	}

	@Override
	public void addMember(Curriculum curriculum, Identity identity, CurriculumRoles role) {
		if(!groupDao.hasRole(curriculum.getGroup(), identity, role.name())) {
			groupDao.addMembershipOneWay(curriculum.getGroup(), identity, role.name(), GroupMembershipInheritance.none);
		}
	}

	@Override
	public void removeMember(Curriculum curriculum, IdentityRef member, CurriculumRoles role) {
		groupDao.removeMembership(curriculum.getGroup(), member, role.name());
	}

	@Override
	public void removeMember(Curriculum curriculum, IdentityRef member) {
		groupDao.removeMembership(curriculum.getGroup(), member);
	}

	@Override
	public List<CurriculumElementType> getCurriculumElementTypes() {
		return curriculumElementTypeDao.load();
	}

	@Override
	public CurriculumElementType getDefaultCurriculumElementType() {
		List<CurriculumElementType> defaultTypes = curriculumElementTypeDao.loadByExternalId(DEFAULT_CURRICULUM_ELEMENT_TYPE);
		return defaultTypes == null || defaultTypes.isEmpty() ? null : defaultTypes.get(0);
	}

	@Override
	public CurriculumElementType getCurriculumElementType(CurriculumElementTypeRef typeRef) {
		return curriculumElementTypeDao.loadByKey(typeRef.getKey());
	}
	
	@Override
	public CurriculumElementType getCurriculumElementType(CurriculumElementRef element) {
		return curriculumElementTypeDao.loadByCurriculumElement(element.getKey());
	}

	@Override
	public List<CurriculumElementType> getAllowedCurriculumElementType(CurriculumElement parentElement, CurriculumElement element) {
		List<CurriculumElementType> allowedTypes;
		if(parentElement == null) {
			allowedTypes = curriculumElementTypeDao.load().stream()
					.filter(CurriculumElementType::isAllowedAsRootElement)
					.collect(Collectors.toList());
		} else {
			allowedTypes = new ArrayList<>();
			List<CurriculumElement> parentLine = getCurriculumElementParentLine(parentElement);
			for(int i=parentLine.size(); i-->0; ) {
				CurriculumElement parent = parentLine.get(i);
				CurriculumElementType parentType = parent.getType();
				if(parentType != null) {
					Set<CurriculumElementTypeToType> typeToTypes = parentType.getAllowedSubTypes();
					for(CurriculumElementTypeToType typeToType:typeToTypes) {
						if(typeToType != null) {
							allowedTypes.add(typeToType.getAllowedSubType());
						}
					}
					break;
				}
			}
		}
		
		if(element != null && !allowedTypes.isEmpty()) {
			long numOfSubElements = curriculumElementDao.countChildren(element);
			long numOfEntryRelations = curriculumRepositoryEntryRelationDao.countRepositoryEntries(element);
			
			for(Iterator<CurriculumElementType> typeIterator=allowedTypes.iterator(); typeIterator.hasNext(); ) {
				CurriculumElementType type = typeIterator.next();
				if((type.isSingleElement() && numOfSubElements > 0)
						|| (type.getMaxRepositoryEntryRelations() >= 0 && numOfEntryRelations > type.getMaxRepositoryEntryRelations())) {
					typeIterator.remove();
				}
			}
		}
		return allowedTypes;
	}

	@Override
	public CurriculumElementType createCurriculumElementType(String identifier, String displayName,
			String description, String externalId) {
		return curriculumElementTypeDao.createCurriculumElementType(identifier, displayName, description, externalId);
	}
	
	@Override
	public CurriculumElementType updateCurriculumElementType(CurriculumElementType elementType) {
		return curriculumElementTypeDao.update(elementType);
	}

	@Override
	public CurriculumElementType updateCurriculumElementType(CurriculumElementType elementType, List<CurriculumElementType> allowedSubTypes) {
		curriculumElementTypeToTypeDao.setAllowedSubType(elementType, allowedSubTypes);
		return curriculumElementTypeDao.update(elementType);
	}
	
	@Override
	public void allowCurriculumElementSubType(CurriculumElementType parentType, CurriculumElementType allowedSubType) {
		curriculumElementTypeToTypeDao.addAllowedSubType(parentType, allowedSubType);
	}

	@Override
	public void disallowCurriculumElementSubType(CurriculumElementType parentType, CurriculumElementType disallowedSubType) {
		curriculumElementTypeToTypeDao.disallowedSubType(parentType, disallowedSubType);
	}

	@Override
	public CurriculumElementType cloneCurriculumElementType(CurriculumElementTypeRef elementType) {
		CurriculumElementType clonedType = curriculumElementTypeDao.cloneCurriculumElementType(elementType);
		List<CurriculumElementTypeToType> allowSubTypesToTypes = curriculumElementTypeToTypeDao.getAllowedSubTypes(elementType);
		if(!allowSubTypesToTypes.isEmpty()) {
			for(CurriculumElementTypeToType allowSubTypeToType:allowSubTypesToTypes) {
				curriculumElementTypeToTypeDao.addAllowedSubType(clonedType, allowSubTypeToType.getAllowedSubType());
			}
		}
		return clonedType;
	}

	@Override
	public boolean deleteCurriculumElementType(CurriculumElementTypeRef elementType) {
		if(curriculumElementTypeDao.hasElements(elementType)) {
			return false;
		}
		curriculumElementTypeToTypeDao.deleteAllowedSubTypes(elementType);
		curriculumElementTypeDao.deleteCurriculumElementType(elementType);
		return true;
	}
	
	@Override
	public List<Curriculum> getCurriculums(CurriculumSearchParameters params) {
		return curriculumDao.search(params);
	}

	@Override
	public CurriculumInfos getCurriculumWithInfos(CurriculumRef curriculum) {
		CurriculumSearchParameters params = new CurriculumSearchParameters();
		params.setCurriculums(List.of(curriculum));
		List<CurriculumInfos> infos = curriculumDao.searchWithInfos(params);
		return infos.size() == 1 ? infos.get(0) : null;
	}

	@Override
	public List<CurriculumInfos> getCurriculumsWithInfos(CurriculumSearchParameters params) {
		return curriculumDao.searchWithInfos(params);
	}
	
	@Override
	public List<Curriculum> getMyCurriculums(Identity identity) {
		return curriculumDao.getMyCurriculums(identity);
	}

	@Override
	public boolean hasCurriculums(IdentityRef identity) {
		return curriculumDao.hasMyCurriculums(identity);
	}

	@Override
	public List<CurriculumRef> getMyActiveCurriculumRefs(Identity identity) {
		List<Long> curriculumKeys = curriculumDao.getMyActiveCurriculumKeys(identity);
		return curriculumKeys.stream().distinct().map(CurriculumRefImpl::new).collect(Collectors.toList());
	}

	@Override
	public CurriculumElement createCurriculumElement(String identifier, String displayName,
			CurriculumElementStatus status, Date beginDate, Date endDate, CurriculumElementRef parentRef,
			CurriculumElementType elementType, CurriculumCalendars calendars, CurriculumLectures lectures,
			CurriculumLearningProgress learningProgress, Curriculum curriculum) {
		if (parentRef == null) {
			curriculum = getCurriculum(curriculum);
		}
		CurriculumElement element = curriculumElementDao.createCurriculumElement(identifier, displayName, status,
				beginDate, endDate, parentRef, elementType, calendars, lectures, learningProgress, curriculum);
		if(element.getParent() != null) {
			Group organisationGroup = element.getGroup();
			List<GroupMembership> memberships = groupDao.getMemberships(element.getParent().getGroup(),
					GroupMembershipInheritance.inherited, GroupMembershipInheritance.root);
			for(GroupMembership membership:memberships) {
				if(membership.getInheritanceMode() == GroupMembershipInheritance.inherited
						|| membership.getInheritanceMode() == GroupMembershipInheritance.root) {
					groupDao.addMembershipOneWay(organisationGroup, membership.getIdentity(), membership.getRole(), GroupMembershipInheritance.inherited);
				}
			}
		}
		return element;
	}

	@Override
	public CurriculumElement cloneCurriculumElement(Curriculum curriculum, CurriculumElement parentElement,
			CurriculumElement elementToClone, CurriculumCopySettings settings, Identity identity) {
		return cloneCurriculumElementRec(curriculum, parentElement, elementToClone, settings, identity, 0);
	}
	
	private CurriculumElement cloneCurriculumElementRec(Curriculum curriculum, CurriculumElement parentElement,
			CurriculumElement elementToClone, CurriculumCopySettings settings, Identity identity, int depth) {
		
		Date beginDate = null;
		Date endDate = null;
		if(settings.isCopyDates()) {
			beginDate = elementToClone.getBeginDate();
			endDate = elementToClone.getEndDate();
		}
		
		String identifier = elementToClone.getIdentifier();
		String displayName = elementToClone.getDisplayName();
		if(depth == 0) {
			displayName += " (Copy)";
		}
		
		CurriculumElement clone = curriculumElementDao.createCurriculumElement(identifier, displayName, CurriculumElementStatus.preparation,
				beginDate, endDate, parentElement, elementToClone.getType(), elementToClone.getCalendars(), elementToClone.getLectures(),
				elementToClone.getLearningProgress(), curriculum);
		
		if(settings.getCopyResources() == CopyResources.relation) {
			List<RepositoryEntry> entries = getRepositoryEntries(elementToClone);
			for(RepositoryEntry entry:entries) {
				repositoryEntryRelationDao.createRelation(clone.getGroup(), entry);
				fireRepositoryEntryAddedEvent(clone, entry);
			}
		} else if(settings.getCopyResources() == CopyResources.resource) {
			List<RepositoryEntry> entries = getRepositoryEntries(elementToClone);
			RepositoryService repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);// prevent service to service link at startup
			for(RepositoryEntry entry:entries) {
				if(repositoryService.canCopy(entry, identity)) {
					RepositoryEntry entryCopy = repositoryService.copy(entry, identity, entry.getDisplayname());
					repositoryEntryRelationDao.createRelation(clone.getGroup(), entryCopy);
					fireRepositoryEntryAddedEvent(clone, entryCopy);
				}
			}
		}
		
		if(settings.isCopyTaxonomy()) {
			Set<CurriculumElementToTaxonomyLevel> taxonomyLevels = clone.getTaxonomyLevels();
			for(CurriculumElementToTaxonomyLevel taxonomyLevel:taxonomyLevels) {
				TaxonomyLevel level = taxonomyLevel.getTaxonomyLevel();
				curriculumElementToTaxonomyLevelDao.createRelation(clone, level);	
			}
		}
		
		List<CurriculumElement> childrenToClone = getCurriculumElementsChildren(elementToClone);
		for(CurriculumElement childToClone:childrenToClone) {
			cloneCurriculumElementRec(curriculum, clone, childToClone, settings, identity, depth);
		}
		return clone;
	}

	@Override
	public CurriculumElement getCurriculumElement(CurriculumElementRef element) {
		return curriculumElementDao.loadByKey(element.getKey());
	}
	
	@Override
	public CurriculumElement getCurriculumElement(OLATResource resource) {
		return curriculumElementDao.loadElementByResource(resource);
	}

	@Override
	public List<CurriculumElement> getCurriculumElements(Collection<? extends CurriculumElementRef> elementRefs) {
		return curriculumElementDao.loadByKeys(elementRefs);
	}

	@Override
	public boolean deleteCurriculumElement(CurriculumElementRef element) {
		if(element == null || element.getKey() == null) return true; // nothing to do

		boolean delete = true;
		List<CurriculumElement> children = curriculumElementDao.getChildren(element);
		for(CurriculumElement child:children) {
			delete &= deleteCurriculumElement(child);
		}
		
		// remove relations to taxonomy before reloading to clear the set
		curriculumElementToTaxonomyLevelDao.deleteRelation(element);
		dbInstance.commit();

		CurriculumElementImpl reloadedElement = (CurriculumElementImpl)curriculumElementDao.loadByKey(element.getKey());
		if(reloadedElement == null) {
			return true;
		}

		// remove relations to repository entries
		List<RepositoryEntryToGroupRelation> relationsToRepo = repositoryEntryRelationDao.getCurriculumRelations(reloadedElement);
		for(RepositoryEntryToGroupRelation relationToRepo:relationsToRepo) {
			if(!relationToRepo.isDefaultGroup()) {// only paranoia
				repositoryEntryRelationDao.removeRelation(relationToRepo);
			}
		}
		
		// remove relations to lecture blocks
		lectureBlockToGroupDao.deleteLectureBlockToGroup(reloadedElement.getGroup());
		
		Map<String,CurriculumDataDeletable> deleteDelegates = CoreSpringFactory.getBeansOfType(CurriculumDataDeletable.class);
		for(CurriculumDataDeletable deleteDelegate:deleteDelegates.values()) {
			delete &= deleteDelegate.deleteCurriculumElementData(reloadedElement);
		}
		
		groupMembershipHistoryDao.deleteMembershipHistory(reloadedElement.getGroup());

		if(delete) {
			if(reloadedElement.getParent() instanceof CurriculumElementImpl parentImpl) {
				parentImpl.getChildren().remove(reloadedElement);
				curriculumElementDao.update(parentImpl);
			}
			curriculumElementDao.deleteCurriculumElement(reloadedElement);
		} else {
			groupDao.removeMemberships(reloadedElement.getGroup());
			//only flag as deleted
			reloadedElement.setParent(null);
			reloadedElement.setExternalId(null);
			reloadedElement.setMaterializedPathKeys(null);
			reloadedElement.setElementStatus(CurriculumElementStatus.deleted);
			curriculumElementDao.update(reloadedElement);
		}
		return delete;
	}

	@Override
	public CurriculumElement updateCurriculumElement(CurriculumElement element) {
		return curriculumElementDao.update(element);
	}
	
	@Override
	public CurriculumElement updateCurriculumElementStatus(Identity doer, CurriculumElementRef elementRef,
			CurriculumElementStatus newStatus, boolean updateChildren, MailPackage mailing) {
		
		CurriculumElement element = getCurriculumElement(elementRef);
		if (element.getElementStatus() == newStatus) {
			return element;
		}
		
		element.setElementStatus(newStatus);
		element = updateCurriculumElement(element);
		
		if (updateChildren) {
			getCurriculumElementsDescendants(element).stream()
				.filter(childElement -> !childElement.getElementStatus().isCancelledOrClosed())
				.filter(childElement -> childElement.getElementStatus() != newStatus)
				.forEach(childElement -> { 
					childElement.setElementStatus(newStatus);
					updateCurriculumElement(childElement); });
		}
		
		dbInstance.commitAndCloseSession();
		
		if(mailing != null && mailing.isSendEmail()) {
			List<Identity> identities = getMembersIdentity(element, CurriculumRoles.participant);
			element = getCurriculumElement(element);
			sendStatusChangeNotificationsEmail(doer, identities, mailing, element);
		}
		
		return element;
	}
	
	private void sendStatusChangeNotificationsEmail(Identity doer, List<Identity> identities, MailPackage mailing,
			CurriculumElement curriculumElement) {
		for (Identity identity : identities) {
			Curriculum curriculum = curriculumElement.getCurriculum();
			CurriculumMailing.sendEmail(doer, identity, curriculum, curriculumElement, mailing);
		}
	}
	
	@Override
	public CurriculumElement moveCurriculumElement(CurriculumElement elementToMove, CurriculumElement newParent,
			CurriculumElement siblingBefore, Curriculum targetCurriculum) {
		Curriculum reloadedTargetCurriculum = curriculumDao.loadByKey(targetCurriculum.getKey());
		CurriculumElement element = curriculumElementDao
				.move(elementToMove, newParent, siblingBefore, reloadedTargetCurriculum);
		
		// propagate inheritance of the new parent
		List<GroupMembership> membershipsToPropagate = new ArrayList<>();
		Map<IdentityToRoleKey,GroupMembership> identityRoleToNewParentMembership = new HashMap<>();
		if(newParent != null) {
			List<GroupMembership> memberships = groupDao.getMemberships(newParent.getGroup(),
					GroupMembershipInheritance.inherited, GroupMembershipInheritance.root);
			for(GroupMembership membership:memberships) {
				if(membership.getInheritanceMode() == GroupMembershipInheritance.inherited || membership.getInheritanceMode() == GroupMembershipInheritance.root) {
					membershipsToPropagate.add(membership);
					identityRoleToNewParentMembership.put(new IdentityToRoleKey(membership), membership);
				}
			}
		}
		
		// inherited of the moved element eventuel -> root
		List<GroupMembership> movedElementMemberships = groupDao.getMemberships(element.getGroup());
		for(GroupMembership movedElementMembership:movedElementMemberships) {
			if(movedElementMembership.getInheritanceMode() == GroupMembershipInheritance.inherited) {
				GroupMembership newParentMembership = identityRoleToNewParentMembership.get(new IdentityToRoleKey(movedElementMembership));
				if(newParentMembership == null || newParentMembership.getInheritanceMode() == GroupMembershipInheritance.none) {
					groupDao.updateInheritanceMode(movedElementMembership, GroupMembershipInheritance.root);
				}
			}
		}
		
		CurriculumElementNode treeToMove = curriculumElementDao.getDescendantTree(element);
		if(!membershipsToPropagate.isEmpty()) {
			propagateMembership(treeToMove, membershipsToPropagate);
		}
		
		// Flush all changes on the database
		dbInstance.commit();
		
		// Recalculate the numbering under this implementation / root element
		CurriculumElement rootElement = getImplementationOf(element);
		numberRootCurriculumElement(rootElement);
		dbInstance.commit();
		
		return element;
	}
	
	private void propagateMembership(CurriculumElementNode node, List<GroupMembership> membershipsToPropagate) {
		Group group = node.getElement().getGroup();
		List<GroupMembership> nodeMemberships = groupDao.getMemberships(group);
		Map<IdentityToRoleKey,GroupMembership> identityRoleToMembership = new HashMap<>();
		for(GroupMembership nodeMembership:nodeMemberships) {
			identityRoleToMembership.put(new IdentityToRoleKey(nodeMembership), nodeMembership);
		}

		for(GroupMembership membershipToPropagate:membershipsToPropagate) {
			GroupMembership nodeMembership = identityRoleToMembership.get(new IdentityToRoleKey(membershipToPropagate));
			if(nodeMembership == null) {
				groupDao.addMembershipOneWay(group, membershipToPropagate.getIdentity(), membershipToPropagate.getRole(), GroupMembershipInheritance.inherited);
			} else if(nodeMembership.getInheritanceMode() != GroupMembershipInheritance.inherited)  {
				groupDao.updateInheritanceMode(nodeMembership, GroupMembershipInheritance.inherited);
			}
		}

		List<CurriculumElementNode> children = node.getChildrenNode();
		if(children != null && !children.isEmpty()) {
			for(CurriculumElementNode child:children) {
				propagateMembership(child, membershipsToPropagate);
			}
		}
	}

	@Override
	public CurriculumElement moveCurriculumElement(CurriculumElement rootElement, Curriculum curriculum) {
		// root element move they entire memberships with, don't need to change them
		CurriculumElement element = curriculumElementDao.move(rootElement, curriculum);
		dbInstance.commit();
		return element;
	}

	@Override
	public List<CurriculumElement> getCurriculumElements(CurriculumRef curriculum, CurriculumElementStatus[] status) {
		return curriculumElementDao.loadElements(curriculum, status);
	}

	@Override
	public List<CurriculumElementInfos> getCurriculumElementsWithInfos(CurriculumElementInfosSearchParams searchParams) {
		return curriculumElementDao.loadElementsWithInfos(searchParams);
	}

	@Override
	public Long getCuriculumElementCount(RepositoryEntryRef entry) {
		return curriculumElementDao.countElements(entry);
	}

	@Override
	public List<CurriculumElement> getCurriculumElements(RepositoryEntry entry) {
		return curriculumElementDao.loadElements(entry);
	}

	@Override
	public List<CurriculumElementWithParents> getOrderedCurriculumElementsTree(RepositoryEntryRef entry) {
		List<CurriculumElement> elementsList = curriculumElementDao.loadElements(entry);
		Set<CurriculumElement> elements = new HashSet<>(elementsList);
		
		List<CurriculumElementWithParents> withParents = new ArrayList<>(elements.size() + 2);
		for(CurriculumElement element:elements) {
			int numOfSlashes = StringHelper.count(element.getMaterializedPathKeys(), '/');
			
			List<CurriculumElement> parentLine;
			if(numOfSlashes <= 2) {
				parentLine = List.of();
			} else if(numOfSlashes <= 3 && element.getParent() != null) {
				parentLine = List.of(element.getParent());
			} else {
				parentLine = curriculumElementDao.getParentLine(element);
			}
			withParents.add(new CurriculumElementWithParents(element, parentLine, 0));
		}
		
		try {
			Collections.sort(withParents, new CurriculumElementWithParentsComparator(Locale.GERMAN));
		} catch (Exception e) {
			log.error("", e);
		}

		return withParents;
	}

	@Override
	public List<CurriculumElement> getCurriculumElements(RepositoryEntryRef entry, Identity identity,
			Collection<CurriculumRoles> roles) {
		return curriculumRepositoryEntryRelationDao.getCurriculumElements(entry, identity, roles);
	}

	@Override
	public List<CurriculumElement> getCurriculumElementsChildren(CurriculumElementRef parentElement) {
		return curriculumElementDao.getChildren(parentElement);
	}
	
	@Override
	public boolean hasCurriculumElementChildren(CurriculumElementRef parentElement) {
		return curriculumElementDao.hasChildren(parentElement);
	}

	@Override
	public List<CurriculumElement> getCurriculumElementsDescendants(CurriculumElement parentElement) {
		return curriculumElementDao.getDescendants(parentElement);
	}

	@Override
	public List<CurriculumElement> getCurriculumElementsByCurriculums(Collection<? extends CurriculumRef> curriculumRefs) {
		return curriculumElementDao.loadElementsByCurriculums(curriculumRefs);
	}

	@Override
	public List<CurriculumElement> searchCurriculumElements(String externalId, String identifier, Long key) {
		return curriculumElementDao.searchElements(externalId, identifier, key);
	}

	@Override
	public List<CurriculumElementSearchInfos> searchCurriculumElements(CurriculumElementSearchParams params) {
		return curriculumElementDao.searchElements(params);
	}

	@Override
	public List<CurriculumElement> getCurriculumElementParentLine(CurriculumElement element) {
		return curriculumElementDao.getParentLine(element);
	}

	@Override
	public List<CurriculumMember> getCurriculumElementsMembers(SearchMemberParameters params) {
		return memberQueries.getCurriculumElementsMembers(params);
	}
	
	@Override
	public List<CurriculumMemberStats> getMembersWithStats(SearchMemberParameters params) {
		List<CurriculumMember> members = memberQueries.getCurriculumElementsMembers(params);
		
		Map<Identity, CurriculumMemberStats> rowMap = new HashMap<>();
		Map<Long, CurriculumMemberStats> rowLongMap = new HashMap<>();
		for(CurriculumMember member:members) {
			CurriculumMemberStats row = rowMap.computeIfAbsent(member.getIdentity(), CurriculumMemberStats::new);
			row.getMembership().setCurriculumElementRole(member.getRole());
			row.addFirstTime(member.getCreationDate());
			if(row.getMembership().isParticipant()) {
				rowLongMap.put(member.getIdentity().getKey(), row);
			}
		}
		
		if(!rowLongMap.isEmpty()) {
			List<CurriculumElement> elements = params.getCurriculumElements();
			coachingDao.getStudentsCompletionStatement(elements, rowLongMap);
		}
		
		return new ArrayList<>(rowMap.values());
	}

	@Override
	public List<Identity> getMembersIdentity(CurriculumElementRef element, CurriculumRoles role) {
		return curriculumElementDao.getMembersIdentity(element, role.name());
	}
	
	@Override
	public List<Long> getMemberKeys(List<CurriculumElementRef> elements, String... roles) {
		return curriculumElementDao.getMemberKeys(elements, roles);
	}
	
	@Override
	public List<CurriculumElementMembership> getCurriculumElementMemberships(Curriculum curriculum, Identity identity) {
		if(identity == null) return new ArrayList<>();
		return curriculumElementDao.getMembershipInfos(Collections.singletonList(curriculum), null, identity);
	}
	
	@Override
	public List<CurriculumElementMembership> getCurriculumElementMemberships(List<CurriculumElement> elements, List<Identity> identities) {
		Identity[] idArr = identities.toArray(new Identity[identities.size()]);
		return curriculumElementDao.getMembershipInfos(null, elements, idArr);
	}

	@Override
	public List<CurriculumElementMembership> getCurriculumElementMemberships(Collection<? extends CurriculumElementRef> elements, Identity... identities) {
		return curriculumElementDao.getMembershipInfos(null, elements, identities);
	}
	
	@Override
	public List<CurriculumElementMembershipHistory> getCurriculumElementMembershipsHistory(
			CurriculumElementMembershipHistorySearchParameters params) {
		return curriculumElementDao.getMembershipInfosAndHistory(params);
	}
	
	@Override
	public List<Identity> getMembersIdentity(List<Long> curriculumElementKeys, CurriculumRoles role) {
		if(role == null || curriculumElementKeys == null || curriculumElementKeys.isEmpty()) {
			return new ArrayList<>();
		}
		return curriculumElementDao.getMembersIdentity(curriculumElementKeys, role.name());
	}

	@Override
	public void acceptPendingParticipation(ResourceReservation reservation, Identity identity, Identity actor) {
		CurriculumRoles roles = ResourceToRoleKey.reservationToRole(reservation.getType());
		CurriculumElement curriculumElement = curriculumElementDao.loadElementByResource(reservation.getResource());
		CurriculumElementMembershipChange change = CurriculumElementMembershipChange.valueOf(identity, curriculumElement);
		change.setNextStatus(roles, GroupMembershipStatus.active);
		updateCurriculumElementMemberships(actor, null, List.of(change), null);
	}

	@Override
	public void cancelPendingParticipation(ResourceReservation reservation, Identity identity, Identity actor) {
		CurriculumRoles roles = ResourceToRoleKey.reservationToRole(reservation.getType());
		CurriculumElement curriculumElement = curriculumElementDao.loadElementByResource(reservation.getResource());
		CurriculumElementMembershipChange change = CurriculumElementMembershipChange.valueOf(identity, curriculumElement);
		change.setNextStatus(roles, GroupMembershipStatus.cancel);
		updateCurriculumElementMemberships(actor, null, List.of(change), null);
	}

	@Override
	public void updateCurriculumElementMemberships(Identity doer, Roles roles,
			List<CurriculumElementMembershipChange> changes, MailPackage mailing) {
		
		int count = 0;
		for(CurriculumElementMembershipChange change:changes) {
			updateCurriculumElementMembership(change, doer);
			if(++count % 100 == 0) {
				dbInstance.commitAndCloseSession();
			}
		}
		dbInstance.commitAndCloseSession();
		
		if(mailing != null && mailing.isSendEmail()) {
			sendMembershipNotificationsEmail(doer, changes, mailing);
		}
	}
	
	private void sendMembershipNotificationsEmail(Identity doer, List<CurriculumElementMembershipChange> changes, MailPackage mailing) {
		Map<Identity,CurriculumElementMembershipChange> changesToNotifiy = new HashMap<>();
		for(CurriculumElementMembershipChange change:changes) {
			if(change.hasStatus(GroupMembershipStatus.reservation)
					|| change.hasStatus(GroupMembershipStatus.active)
					|| change.hasStatus(GroupMembershipStatus.removed)) {
				if(!changesToNotifiy.containsKey(change.getMember())) {
					changesToNotifiy.put(change.getMember(), change);
				} else {
					CurriculumElementMembershipChange currentChange = changesToNotifiy.get(change.getMember());
					if(change.numOfSegments() < currentChange.numOfSegments()) {
						changesToNotifiy.put(change.getMember(), change);
					}
				}
			}
		}
		
		for(CurriculumElementMembershipChange additionToNotifiy:changesToNotifiy.values()) {
			CurriculumElement curriculumElement = additionToNotifiy.getCurriculumElement();
			Curriculum curriculum = curriculumElement.getCurriculum();
			CurriculumMailing.sendEmail(doer, additionToNotifiy.getMember(), curriculum, curriculumElement, mailing);
		}
	}
	
	private void updateCurriculumElementMembership(CurriculumElementMembershipChange changes, Identity actor) {
		final CurriculumElement element = changes.getCurriculumElement();
		List<CurriculumRoles> rolesToChange = changes.getRoles();
		for(CurriculumRoles role:rolesToChange) {
			GroupMembershipStatus nextStatus = changes.getNextStatus(role);
			String adminNote = changes.getAdminNoteBy(role);
			updateCurriculumElementMembership(element, changes.getMember(), role, nextStatus,
					changes.getConfirmationBy(), changes.getConfirmUntil(),
					actor, adminNote);
		}
	}
	
	private void updateCurriculumElementMembership(CurriculumElement element,
			Identity member, CurriculumRoles role, GroupMembershipStatus nextStatus,
			ConfirmationByEnum confirmationBy, Date confirmUntil,
			Identity actor, String adminNote) {
		
		if(nextStatus == GroupMembershipStatus.active) {
			removeMemberReservation(element, member, role, null, null, null);
			addMember(element, member, role, actor, adminNote);
		} else if(nextStatus == GroupMembershipStatus.reservation) {
			Boolean confirmBy = confirmationBy == ConfirmationByEnum.PARTICIPANT ? Boolean.TRUE : Boolean.FALSE;
			addMemberReservation(element, member, role, confirmUntil, confirmBy, actor, adminNote);
		} else if(nextStatus == GroupMembershipStatus.removed) {
			removeMember(element, member, role, nextStatus, actor, adminNote);
		}  else if(nextStatus == GroupMembershipStatus.cancel
				|| nextStatus == GroupMembershipStatus.cancelWithFee
				|| nextStatus == GroupMembershipStatus.declined) {
			boolean removed = removeMemberReservation(element, member, role, nextStatus, actor, adminNote);
			removed |= removeMember(element, member, role, nextStatus, actor, adminNote);
			if(!removed) {
				addMemberHistory(element, member, role, nextStatus, actor, adminNote);
			}
			
			List<Order> orders = acService.findOrders(member, element.getResource(), OrderStatus.NEW, OrderStatus.PREPAYMENT, OrderStatus.PAYED);
			for(Order order:orders) {
				acService.cancelOrder(order);
			}
		}
	}

	@Override
	public void addMember(CurriculumElement element, Identity member, CurriculumRoles role, Identity actor) {
		addMember(element, member, role, actor, null);
	}
	
	@Override
	public void addMember(CurriculumElement element, Identity member, CurriculumRoles role, Identity actor,
			String adminNote) {
		GroupMembershipInheritance inheritanceMode;
		if(CurriculumRoles.isInheritedByDefault(role)) {
			inheritanceMode = GroupMembershipInheritance.root;
		} else {
			inheritanceMode = GroupMembershipInheritance.none;
		}
		addMember(element, member, role, inheritanceMode, actor, adminNote);
		dbInstance.commit();
	}
	
	public void addMember(CurriculumElement element, Identity member, CurriculumRoles role, GroupMembershipInheritance inheritanceMode,
			Identity actor, String adminNote) {
		if(inheritanceMode == GroupMembershipInheritance.inherited) {
			throw new AssertException("Inherited are automatic");
		}
		
		List<CurriculumElementMembershipEvent> events = new ArrayList<>();
		Group elementGroup = element.getGroup();
		GroupMembership membership = groupDao.getMembership(elementGroup, member, role.name());
		if(membership == null) {
			groupDao.addMembershipOneWay(elementGroup, member, role.name(), inheritanceMode);
			groupMembershipHistoryDao.createMembershipHistory(elementGroup, member,
					role.name(), GroupMembershipStatus.active, null, null, actor, adminNote);
			events.add(CurriculumElementMembershipEvent.identityAdded(element, member, role));
		} else if(membership.getInheritanceMode() != inheritanceMode) {
			groupDao.updateInheritanceMode(membership, inheritanceMode);
		}
		
		if(inheritanceMode == GroupMembershipInheritance.root) {
			List<CurriculumElement> descendants = curriculumElementDao.getDescendants(element);
			for(CurriculumElement descendant:descendants) {
				Group descendantGroup = descendant.getGroup();
				GroupMembership inheritedMembership = groupDao.getMembership(descendantGroup, member, role.name());
				if(inheritedMembership == null) {
					groupDao.addMembershipOneWay(descendantGroup, member, role.name(), GroupMembershipInheritance.inherited);
					events.add(CurriculumElementMembershipEvent.identityAdded(element, member, role));
				} else if(inheritedMembership.getInheritanceMode() == GroupMembershipInheritance.none) {
					groupDao.updateInheritanceMode(inheritedMembership, GroupMembershipInheritance.inherited);
				}
			}
		}
		
		dbInstance.commitAndCloseSession();
		sendDeferredEvents(events);
	}

	@Override
	public void removeMember(CurriculumElement element, Identity member, Identity actor) {
		List<CurriculumElementMembershipEvent> events = new ArrayList<>();

		Group elementGroup = element.getGroup();
		List<GroupMembership> memberships = groupDao.getMemberships(elementGroup, member);
		groupDao.removeMembership(elementGroup, member);
		groupMembershipHistoryDao.createMembershipHistory(elementGroup, member,
				GroupRoles.owner.name(), GroupMembershipStatus.removed, null, null,
				actor, null);
		
		CurriculumElementNode elementNode = null;
		for(GroupMembership membership:memberships) {
			if(membership.getInheritanceMode() == GroupMembershipInheritance.none) {
				groupDao.removeMembership(membership);
				events.add(CurriculumElementMembershipEvent.identityRemoved(element, member, membership.getRole()));
			} else if(membership.getInheritanceMode() == GroupMembershipInheritance.root) {
				String role = membership.getRole();
				groupDao.removeMembership(membership);
				events.add(CurriculumElementMembershipEvent.identityRemoved(element, member, membership.getRole()));
				
				if(elementNode == null) {
					elementNode = curriculumElementDao.getDescendantTree(element);
				}
				for(CurriculumElementNode child:elementNode.getChildrenNode()) {
					removeInherithedMembership(child, member, role, events);
				}
			}
		}
		dbInstance.commitAndCloseSession();
		sendDeferredEvents(events);
	}
	
	/**
	 * The method will recursively delete the inherithed membership. If it
	 * found a mebership marked as "root" or "none". It will stop.
	 * 
	 * @param elementNode The organization node
	 * @param member The user to remove
	 * @param role The role
	 * @param events 
	 */
	private void removeInherithedMembership(CurriculumElementNode elementNode, IdentityRef member, String role, List<CurriculumElementMembershipEvent> events) {
		GroupMembership membership = groupDao
				.getMembership(elementNode.getElement().getGroup(), member, role);
		if(membership != null && membership.getInheritanceMode() == GroupMembershipInheritance.inherited) {
			groupDao.removeMembership(membership);
			events.add(CurriculumElementMembershipEvent.identityRemoved(elementNode.getElement(), member, membership.getRole()));
			
			for(CurriculumElementNode child:elementNode.getChildrenNode()) {
				removeInherithedMembership(child, member, role, events);
			}
		}
	}

	@Override
	public boolean removeMember(CurriculumElement element, Identity member, CurriculumRoles role,
			GroupMembershipStatus reason, Identity actor, String adminNote) {
		Group elementGroup = element.getGroup();
		List<CurriculumElementMembershipEvent> events = new ArrayList<>();
		GroupMembership membership = groupDao.getMembership(elementGroup, member, role.name());
		int removed = groupDao.removeMembership(elementGroup, member, role.name());
		if(removed > 0) {
			groupMembershipHistoryDao.createMembershipHistory(elementGroup, member,
					role.name(), reason, null, null,
					actor, adminNote);
			events.add(CurriculumElementMembershipEvent.identityRemoved(element, member, role));
		}
		
		if(membership != null && (membership.getInheritanceMode() == GroupMembershipInheritance.root
				|| membership.getInheritanceMode() == GroupMembershipInheritance.none)) {
			groupDao.removeMembership(membership);
			if(membership.getInheritanceMode() == GroupMembershipInheritance.root
					|| membership.getInheritanceMode() == GroupMembershipInheritance.inherited) {
				CurriculumElementNode elementNode = curriculumElementDao.getDescendantTree(element);
				for(CurriculumElementNode child:elementNode.getChildrenNode()) {
					removeInherithedMembership(child, member, role.name(), events);
				}
			}
		}
		dbInstance.commitAndCloseSession();
		sendDeferredEvents(events);
		return removed > 0;
	}

	@Override
	public boolean removeMemberReservation(CurriculumElement element, Identity member, CurriculumRoles role,
			GroupMembershipStatus reason, Identity actor, String adminNote) {
		OLATResource resource = element.getResource();
		ResourceReservation reservation = reservationDao.loadReservation(member, resource);
		if(reservation != null) {
			Group group = element.getGroup();
			reservationDao.deleteReservation(reservation);
			if(reason != null) {
				groupMembershipHistoryDao.createMembershipHistory(group, member, role.name(),
					reason, null, null, actor, adminNote);
			}
			dbInstance.commit();
		}
		return reservation != null;
	}

	@Override
	public void removeMembers(CurriculumElement element, List<Identity> members, boolean overrideManaged, Identity actor) {
		List<CurriculumElementMembershipEvent> events = new ArrayList<>();
		if(!CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.members) || overrideManaged) {
			for(Identity member:members) {
				groupDao.removeMembership(element.getGroup(), member);
				events.add(CurriculumElementMembershipEvent.identityRemoved(element, member));
			}
		}
		dbInstance.commitAndCloseSession();
		sendDeferredEvents(events);
	}
	
	@Override
	public void addMemberReservation(CurriculumElement element, Identity member, CurriculumRoles role,
			Date expirationDate, Boolean confirmBy, Identity actor, String note) {
		OLATResource resource = element.getResource();
		ResourceReservation reservation = reservationDao.loadReservation(member, resource);
		if(reservation == null) {
			Date expiration = expirationDate == null
					? DateUtils.addMonth(DateUtils.getStartOfDay(new Date()), 6)
					: expirationDate;
			Group group = element.getGroup();
			reservationDao.createReservation(member, "curriculum_" + role, expiration, confirmBy, resource);
			groupMembershipHistoryDao.createMembershipHistory(group, member, role.name(),
					GroupMembershipStatus.reservation, null, null, actor, note);
			dbInstance.commit();
		}
	}
	
	@Override
	public void addMemberHistory(CurriculumElement element, Identity member, CurriculumRoles role,
			GroupMembershipStatus status, Identity actor, String note) {
		Group group = element.getGroup();
		groupMembershipHistoryDao.createMembershipHistory(group, member, role.name(),
				status, null, null, actor, note);
		dbInstance.commit();
	}
	
	@Override
	public Map<Long, Long> getCurriculumElementKeyToNumParticipants(List<CurriculumElement> curriculumElements, boolean countReservations) {
		if (curriculumElements.isEmpty()) {
			return Map.of();
		}
		
		// Memberships
		Map<Long, Long> curriculumElementKeyToNumParticipants = new HashMap<>(curriculumElements.size());
		for (CurriculumElementMembership membership : getCurriculumElementMemberships(curriculumElements)) {
			if (membership.isParticipant()) {
				Long numParticipants = curriculumElementKeyToNumParticipants.getOrDefault(membership.getCurriculumElementKey(), Long.valueOf(0));
				numParticipants++;
				curriculumElementKeyToNumParticipants.put(membership.getCurriculumElementKey(), numParticipants);
			}
		}
		
		// Reservations
		if (countReservations) {
			List<OLATResource> resources = new ArrayList<>(curriculumElements.size());
			Map<Long, Long> resourceKeyToElementKey = new HashMap<>(curriculumElements.size());
			for (CurriculumElement curriculumElement : curriculumElements) {
				resources.add(curriculumElement.getResource());
				resourceKeyToElementKey.put(curriculumElement.getResource().getKey(), curriculumElement.getKey());
			}
			
			List<ResourceReservation> reservations = acService.getReservations(resources);
			for (ResourceReservation reservation : reservations) {
				Long resourceKey = reservation.getResource().getKey();
				Long ceKey = resourceKeyToElementKey.get(resourceKey);
				if (ceKey != null) {
					Long numParticipants = curriculumElementKeyToNumParticipants.getOrDefault(ceKey, Long.valueOf(0));
					numParticipants++;
					curriculumElementKeyToNumParticipants.put(ceKey, numParticipants);
				}
			}
		}
		
		return curriculumElementKeyToNumParticipants;
	}
	
	@Override
	public boolean hasRepositoryEntries(CurriculumElementRef element) {
		return curriculumRepositoryEntryRelationDao.hasRepositoryEntries(element);
	}

	@Override
	public List<RepositoryEntry> getRepositoryEntries(CurriculumElementRef element) {
		List<CurriculumElementRef> elements = Collections.singletonList(element);
		return curriculumRepositoryEntryRelationDao
				.getRepositoryEntries(null, elements, RepositoryEntryStatusEnum.preparationToClosed(), false, null, null);
	}

	@Override
	public List<RepositoryEntry> getRepositoryEntriesWithDescendants(CurriculumElement element) {
		List<CurriculumElement> descendants = curriculumElementDao.getDescendants(element);
		descendants.add(element);
		List<CurriculumElementRef> descendantRefs = new ArrayList<>(descendants);
		return curriculumRepositoryEntryRelationDao
				.getRepositoryEntries(null, descendantRefs, RepositoryEntryStatusEnum.preparationToClosed(), false, null, null);
	}
	
	@Override
	public List<RepositoryEntry> getRepositoryEntriesOfParticipantWithDescendants(CurriculumElement element, Identity participant) {
		List<CurriculumElement> descendants = curriculumElementDao.getDescendants(element);
		descendants.add(element);
		List<String> roles = Arrays.asList(GroupRoles.participant.name());
		List<CurriculumElementRef> descendantRefs = new ArrayList<>(descendants);
		return curriculumRepositoryEntryRelationDao
				.getRepositoryEntries(null, descendantRefs, RepositoryEntryStatusEnum.preparationToClosed(), false, participant, roles);
	}

	@Override
	public List<RepositoryEntry> getRepositoryEntriesWithLectures(CurriculumElement element, Identity identity, boolean withDescendants) {
		List<CurriculumElement> descendants;
		if(withDescendants) {
			descendants = curriculumElementDao.getDescendants(element);
		} else {
			descendants = new ArrayList<>();
		}	
		descendants.add(element);
		List<String> roles = Arrays.asList(OrganisationRoles.administrator.name(), OrganisationRoles.principal.name(),
				OrganisationRoles.learnresourcemanager.name(), GroupRoles.owner.name(), CurriculumRoles.mastercoach.name());
		List<CurriculumElementRef> descendantRefs = new ArrayList<>(descendants);
		return curriculumRepositoryEntryRelationDao
				.getRepositoryEntries(null, descendantRefs, RepositoryEntryStatusEnum.preparationToClosed(), true, identity, roles);
	}

	@Override
	public List<RepositoryEntry> getRepositoryEntriesWithLectures(Curriculum curriculum, Identity identity) {
		List<String> roles = Arrays.asList(OrganisationRoles.administrator.name(), OrganisationRoles.principal.name(),
				OrganisationRoles.learnresourcemanager.name(), GroupRoles.owner.name(), CurriculumRoles.mastercoach.name());
		return curriculumRepositoryEntryRelationDao
				.getRepositoryEntries(curriculum, null, RepositoryEntryStatusEnum.preparationToClosed(), true, identity, roles);
	}

	@Override
	public AddRepositoryEntry addRepositoryEntry(CurriculumElement curriculumElement, RepositoryEntry entry, boolean moveLectureBlocks) {
		if(!hasRepositoryEntry(curriculumElement, entry)) {
			boolean moved = false;
			RepositoryEntry repoEntry = repositoryEntryDao.loadByKey(entry.getKey());
			repositoryEntryRelationDao.createRelation(curriculumElement.getGroup(), repoEntry);
			if(moveLectureBlocks) {
				moved = moveLectureBlocks(curriculumElement, entry);
			}
			fireRepositoryEntryAddedEvent(curriculumElement, entry);
			return new AddRepositoryEntry(true, moved);
		}
		return new AddRepositoryEntry(false, false);
	}
	
	private boolean moveLectureBlocks(CurriculumElement curriculumElement, RepositoryEntry entry) {
		if(curriculumElement == null || entry == null) return false;
		
		boolean moved = false;
		List<LectureBlock> lectureBlocks = lectureBlockDao.getLectureBlocks(curriculumElement);
		for(LectureBlock lectureBlock:lectureBlocks) {
			((LectureBlockImpl)lectureBlock).setEntry(entry);
			lectureBlockDao.update(lectureBlock);
			moved |= true;
		}
		return moved;
	}

	@Override
	public boolean hasRepositoryEntry(CurriculumElement element, RepositoryEntryRef entry) {
		return repositoryEntryRelationDao.hasRelation(element.getGroup(), entry);
	}

	@Override
	public void removeRepositoryEntry(RepositoryEntry entry) {
		List<CurriculumElement> elements = curriculumRepositoryEntryRelationDao.getCurriculumElements(entry);
		for(CurriculumElement element:elements) {
			internalRemoveRepositoryEntry(element, entry);
		}
	}

	@Override
	public RemovedRepositoryEntry removeRepositoryEntry(CurriculumElement element, RepositoryEntry entry) {
		// remove relation linked between infoMessages and curriculumElement
		List<InfoMessage> infoMessages = infoMessageManager.loadInfoMessageByResource(entry.getOlatResource(),
				InfoMessageFrontendManager.businessGroupResSubPath, null, null, null, 0, 0);
		for (InfoMessage infoMessage : infoMessages) {
			Set<InfoMessageToCurriculumElement> infoMessageToCurriculumElements = infoMessage.getCurriculumElements();
			if (infoMessageToCurriculumElements != null) {
				for (InfoMessageToCurriculumElement infoMessageToCurriculumElement : infoMessageToCurriculumElements) {
					if (infoMessageToCurriculumElement.getCurriculumElement().equals(element)) {
						infoMessageManager.deleteInfoMessageToCurriculumElement(infoMessageToCurriculumElement);
					}
				}
			}
		}
		int lectureBlocksMoved = internalRemoveRepositoryEntry(element, entry);
		return new RemovedRepositoryEntry(true, lectureBlocksMoved);
	}
	
	private int internalRemoveRepositoryEntry(CurriculumElement element, RepositoryEntry entry) {
		int lectureBlocksMoved = 0;
		List<LectureBlock> lectureBlocks = lectureBlockDao.getLectureBlocks(entry);
		for(LectureBlock lectureBlock:lectureBlocks) {
			if(lectureBlock.getCurriculumElement() == null) {
				// Do nothing
			} else if(element.equals(lectureBlock.getCurriculumElement())) {
				((LectureBlockImpl)lectureBlock).setEntry(null);
				lectureBlockDao.update(lectureBlock);
				lectureBlocksMoved++;
			}
		}
		repositoryEntryRelationDao.removeRelation(element.getGroup(), entry);
		return lectureBlocksMoved;
	}

	@Override
	public List<TaxonomyLevel> getTaxonomy(CurriculumElement element) {
		if(element == null || element.getKey() == null) return Collections.emptyList();
		return curriculumElementToTaxonomyLevelDao.getTaxonomyLevels(element);
	}
	
	@Override
	public Map<Long, List<TaxonomyLevel>> getCurriculumElementKeyToTaxonomyLevels(List<? extends CurriculumElementRef> curriculumElements) {
		if(curriculumElements == null || !curriculumElements.isEmpty()) return Map.of();
		return curriculumElementToTaxonomyLevelDao.getCurriculumElementKeyToTaxonomyLevels(curriculumElements);
	}
	
	@Override
	public void updateTaxonomyLevels(CurriculumElement element, Collection<TaxonomyLevel> addedLevels, Collection<TaxonomyLevel> removedLevels) {
		if (element == null || element.getKey() == null) return;
		
		List<TaxonomyLevel> taxonomyLevels = curriculumElementToTaxonomyLevelDao.getTaxonomyLevels(element);
		
		if (addedLevels != null) {
			for (TaxonomyLevel level : addedLevels) {
				if (taxonomyLevels.contains(level)) {
					continue;
				}
				
				curriculumElementToTaxonomyLevelDao.createRelation(element, level);
			}
		}
		
		if (removedLevels != null) {
			for (TaxonomyLevel level : removedLevels) {
				if (!taxonomyLevels.contains(level)) {
					continue;
				}
				
				curriculumElementToTaxonomyLevelDao.deleteRelation(element, level);
			}
		}
	}

	@Override
	public List<CurriculumElement> getCurriculumElements(TaxonomyLevelRef level) {
		return curriculumElementToTaxonomyLevelDao.getCurriculumElements(level);
	}
	
	@Override
	public long countCurriculumElements(List<? extends TaxonomyLevelRef> taxonomyLevels) {
		return curriculumElementToTaxonomyLevelDao.countCurriculumElements(taxonomyLevels);
	}

	@Override
	public List<CurriculumElementWebDAVInfos> getCurriculumElementInfosForWebDAV(IdentityRef identity) {
		List<String> roles = new ArrayList<>();
		roles.add(GroupRoles.owner.name());
		roles.add(GroupRoles.coach.name());
		roles.add(GroupRoles.participant.name());
		return curriculumRepositoryEntryRelationDao.getCurriculumElementInfosForWebDAV(identity, roles);
	}

	@Override
	public List<CurriculumElementRepositoryEntryViews> getCurriculumElements(Identity identity, Roles roles,
			List<? extends CurriculumRef> curriculums, CurriculumElementStatus[] status) {
		if(curriculums == null || curriculums.isEmpty()) return Collections.emptyList();
		
		List<CurriculumElementMembership> memberships = curriculumElementDao.getMembershipInfos(curriculums, null, identity);
		Map<Long,CurriculumElementMembership> membershipMap = new HashMap<>();
		for(CurriculumElementMembership membership:memberships) {
			membershipMap.put(membership.getCurriculumElementKey(), membership);
		}
		
		Map<CurriculumElement, List<Long>> elementsMap = curriculumRepositoryEntryRelationDao
				.getCurriculumElementsWithRepositoryEntryKeys(curriculums, status);
		List<CurriculumElementRepositoryEntryViews> elements = new ArrayList<>(elementsMap.size());
		if(!elementsMap.isEmpty()) {
			SearchMyRepositoryEntryViewParams params = new SearchMyRepositoryEntryViewParams(identity, roles);
			params.setCurriculums(curriculums);
			params.setOfferOrganisations(acService.getOfferOrganisations(identity));
			params.setOfferValidAt(new Date());
			params.setRuntimeType(RepositoryEntryRuntimeType.standalone);
			
			List<RepositoryEntryMyView> views = myCourseQueries.searchViews(params, 0, -1);
			Map<Long, RepositoryEntryMyView> viewMap = new HashMap<>();
			for(RepositoryEntryMyView view:views) {
				viewMap.put(view.getKey(), view);
			}
			
			Map<Long,CurriculumElementRepositoryEntryViews> elementKeyMap = new HashMap<>();
			for(Map.Entry<CurriculumElement, List<Long>> elementEntry:elementsMap.entrySet()) {
				CurriculumElement element = elementEntry.getKey();
				List<RepositoryEntryMyView> elementViews = new ArrayList<>(elementEntry.getValue().size());
				Set<Long> deduplicatedEntryKeys = new HashSet<>(elementEntry.getValue());
				for(Long entryKey:deduplicatedEntryKeys) {
					RepositoryEntryMyView elementView = viewMap.get(entryKey);
					if(elementView != null) {
						elementViews.add(elementView);
					}
				}
				CurriculumElementMembership membership = membershipMap.get(element.getKey());
				CurriculumElementRepositoryEntryViews view = new CurriculumElementRepositoryEntryViews(element, elementViews, membership);
				elements.add(view);
				elementKeyMap.put(element.getKey(), view);
			}

			// calculate parents
			for(CurriculumElementRepositoryEntryViews element:elements) {
				CurriculumElement parent = element.getCurriculumElement().getParent();
				if(parent != null) {
					element.setParent(elementKeyMap.get(parent.getKey()));
				}
			}
			
			// propagate to the parents
			for(CurriculumElementRepositoryEntryViews element:elements) {
				if(element.isCurriculumMember()) {
					for(CurriculumElementRepositoryEntryViews parentRow=element.getParent(); parentRow != null; parentRow=parentRow.getParent()) {
						parentRow.setCurriculumMember(true);
					}
				}
			}
			
			// trim part of the tree without member flag
			for(Iterator<CurriculumElementRepositoryEntryViews> it=elements.iterator(); it.hasNext(); ) {
				if(!it.next().isCurriculumMember()) {
					it.remove();
				}
			}
		}
		
		return elements;
	}

	@Override
	public List<CurriculumElement> filterElementsWithoutManagerRole(List<CurriculumElement> elements, Roles roles) {
		if(elements == null || elements.isEmpty()) return elements;

		List<CurriculumElement> allowedToManaged = new ArrayList<>();
		List<OrganisationRef> uOrganisations = roles.getOrganisationsWithRoles(OrganisationRoles.curriculummanager, OrganisationRoles.administrator);
		if(!uOrganisations.isEmpty()) {
			for(CurriculumElement element:elements) {
				Organisation org = element.getCurriculum().getOrganisation();
				if(org != null) {
					for(OrganisationRef uOrganisation:uOrganisations) {
						if(uOrganisation.getKey().equals(org.getKey())) {
							allowedToManaged.add(element);
							break;
						}
					}
				}
			}
		}
		
		return allowedToManaged;
	}

	@Override
	public boolean deleteOrganisationData(Organisation organisation, Organisation replacementOrganisation) {
		CurriculumSearchParameters searchParams = new CurriculumSearchParameters();
		searchParams.setOrganisations(List.of(organisation));
		searchParams.setStatusList(List.of(CurriculumStatus.active, CurriculumStatus.deleted));
		List<Curriculum> curriculums = curriculumDao.search(searchParams);
		for(Curriculum curriculum:curriculums) {
			curriculum.setOrganisation(replacementOrganisation);
			curriculumDao.update(curriculum);
		}
		return true;
	}
	
	@Override
	public CurriculumElement getImplementationOf(CurriculumElement curriculumElement) {
		if(curriculumElement.getParent() == null) {
			return curriculumElement;
		}
		List<CurriculumElement> parentLine = getCurriculumElementParentLine(curriculumElement);
		if(!parentLine.isEmpty()) {
			return parentLine.get(0);
		}
		return curriculumElement;
	}
	
	@Override
	public List<CurriculumElement> getImplementations(Curriculum curriculum) {
		return this.curriculumElementDao.getImplementations(curriculum);
	}

	@Override
	public VFSContainer getMediaContainer(CurriculumElement curriculumElement) {
		VFSContainer mediaContainer = curriculumStorage.getMediaContainer(curriculumElement);
		mediaContainer = new NamedContainerImpl(curriculumElement.getDisplayName(), mediaContainer);
		return mediaContainer;
	}
	
	@Override
	public void storeCurriculumElemenFile(CurriculumElementRef element, CurriculumElementFileType type, File file, String filename, Identity savedBy) {
		curriculumStorage.storeCurriculumElementFile(element, type, savedBy, file, filename);
	}
	
	@Override
	public void deleteCurriculumElemenFile(CurriculumElementRef element, CurriculumElementFileType type) {
		curriculumStorage.deleteCurriculumElementFile(element, type);
	}
	
	@Override
	public VFSLeaf getCurriculumElemenFile(CurriculumElementRef element, CurriculumElementFileType type) {
		return curriculumStorage.getCurriculumElementFile(element, type);
	}
	
	@Override
	public boolean numberRootCurriculumElement(CurriculumElement rootElement) {
		if(rootElement.getParent() != null) {
			log.warn("Try to number a curriculum element which is not an implementation: {}", rootElement);
		}
		
		Locale locale = i18nModule.defaultLocale();
		
		List<NumberingCurriculumElement> elements = curriculumElementDao.getDescendants(rootElement).stream()
				.map(NumberingCurriculumElement::new)
				.collect(Collectors.toList());
		// Build parent line
		Map<Long,NumberingCurriculumElement> keyToElements = elements.stream()
				.collect(Collectors.toMap(NumberingCurriculumElement::getKey, el -> el, (u, v) -> u));
		for(NumberingCurriculumElement element:elements) {
			if(element.getParentKey() != null) {
				element.setParent(keyToElements.get(element.getParentKey()));
			}
		}
	
		// Sort the tree
		Collections.sort(elements, new CurriculumElementTreeRowComparator(locale));
		boolean changed = number(null, List.of(), 0, elements);
		dbInstance.commit();
		return changed;
	}
	
	private boolean number(NumberingCurriculumElement parent, List<Long> numbering, int start, List<NumberingCurriculumElement> elements) {
		int count = 1;
		boolean changed = false;
		for(int i=start; i<elements.size(); i++) {
			NumberingCurriculumElement element = elements.get(i);
			if(Objects.equals(parent, element.getParent())) {
				List<Long> values = new ArrayList<>(numbering);
				values.add(Long.valueOf(count++));
				String number = new Numbering(values).toString();
				if(!number.equals(element.getCurriculumElement().getNumberImpl())) {
					curriculumElementDao.updateNumber(element, number);
					changed |= true;
				}
				changed |= number(element, values, i+1, elements);
			}
		}
		return changed;
	}
	
	private void sendDeferredEvents(List<CurriculumElementMembershipEvent> events) {
		EventBus eventBus = CoordinatorManager.getInstance().getCoordinator().getEventBus();
		for(CurriculumElementMembershipEvent event:events) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance(CurriculumElement.class, event.getCurriculumElementKey());
			eventBus.fireEventToListenersOf(event, ores);
		}
	}
	
	private void fireRepositoryEntryAddedEvent(CurriculumElementRef element, RepositoryEntryRef entry) {
		CurriculumElementRepositoryEntryEvent event = new CurriculumElementRepositoryEntryEvent(REPOSITORY_ENTRY_ADDED,
				element.getKey(), entry.getKey());
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(CurriculumElement.class, event.getCurriculumElementKey());
		coordinator.getCoordinator().getEventBus().fireEventToListenersOf(event, ores);
	}

	@Override
	public List<BusinessGroup> deleteInternalGroupMembershipsAndInvitations(RepositoryEntry courseEntry) {
		List<BusinessGroup> businessGroups = getAllBusinessGroups(courseEntry);
		for (BusinessGroup businessGroup : businessGroups) {
			deleteGroupMembershipsAndInvitation(businessGroup);
		}
		return businessGroups;
	}
	
	private List<BusinessGroup> getAllBusinessGroups(RepositoryEntry courseEntry) {
		if (!RepositoryEntryRuntimeType.curricular.equals(courseEntry.getRuntimeType())) {
			return Collections.emptyList();
		}

		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		return businessGroupDAO
				.findBusinessGroups(params, courseEntry, 0, -1).stream()
				.filter(bg -> {
					if (LTI13Service.LTI_GROUP_TYPE.equals(bg.getTechnicalType())) {
						return false;
					}
					if (StringHelper.containsNonWhitespace(bg.getManagedFlagsString())) {
						return false;
					}
					List<Long> entryKeys = businessGroupRelationDao.getRepositoryEntryKeys(bg);
					if (entryKeys.size() != 1) {
						return false;
					}
					return true;
				})
				.toList();
	}

	private void deleteGroupMembershipsAndInvitation(BusinessGroup businessGroup) {
		groupDao.removeMemberships(businessGroup.getBaseGroup());
		groupMembershipHistoryDao.deleteMembershipHistory(businessGroup.getBaseGroup());
		
		// Also delete invitations related to the group
		invitationDao.deleteInvitation(businessGroup.getBaseGroup());
	}

	@Override
	public void deleteInternalGroups(List<BusinessGroup> internalGroups, Identity doer) {
		BusinessGroupLifecycleManager businessGroupLifecycleManager = CoreSpringFactory.getImpl(BusinessGroupLifecycleManager.class);
		for (BusinessGroup businessGroup : internalGroups) {
			businessGroupLifecycleManager.deleteBusinessGroup(businessGroup, doer, false);
		}
	}
}
