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
package org.olat.modules.curriculum.manager;

import static org.olat.modules.curriculum.CurriculumElementRepositoryEntryEvent.REPOSITORY_ENTRY_ADDED;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupMembership;
import org.olat.basesecurity.GroupMembershipInheritance;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationDataDeletable;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.basesecurity.model.IdentityToRoleKey;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.logging.AssertException;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.coach.manager.CoachingDAO;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumDataDeletable;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementManagedFlag;
import org.olat.modules.curriculum.CurriculumElementMembership;
import org.olat.modules.curriculum.CurriculumElementMembershipEvent;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumElementRepositoryEntryEvent;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementToTaxonomyLevel;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumElementTypeRef;
import org.olat.modules.curriculum.CurriculumElementTypeToType;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumCopySettings;
import org.olat.modules.curriculum.model.CurriculumCopySettings.CopyResources;
import org.olat.modules.curriculum.model.CurriculumElementImpl;
import org.olat.modules.curriculum.model.CurriculumElementInfos;
import org.olat.modules.curriculum.model.CurriculumElementMembershipChange;
import org.olat.modules.curriculum.model.CurriculumElementNode;
import org.olat.modules.curriculum.model.CurriculumElementRepositoryEntryViews;
import org.olat.modules.curriculum.model.CurriculumElementSearchInfos;
import org.olat.modules.curriculum.model.CurriculumElementSearchParams;
import org.olat.modules.curriculum.model.CurriculumElementWebDAVInfos;
import org.olat.modules.curriculum.model.CurriculumImpl;
import org.olat.modules.curriculum.model.CurriculumInfos;
import org.olat.modules.curriculum.model.CurriculumMember;
import org.olat.modules.curriculum.model.CurriculumMemberStats;
import org.olat.modules.curriculum.model.CurriculumRefImpl;
import org.olat.modules.curriculum.model.CurriculumSearchParameters;
import org.olat.modules.curriculum.model.SearchMemberParameters;
import org.olat.modules.curriculum.ui.CurriculumMailing;
import org.olat.modules.lecture.manager.LectureBlockToGroupDAO;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryMyView;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.RepositoryEntryDAO;
import org.olat.repository.manager.RepositoryEntryMyCourseQueries;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.repository.model.RepositoryEntryToGroupRelation;
import org.olat.repository.model.SearchMyRepositoryEntryViewParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 9 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CurriculumServiceImpl implements CurriculumService, OrganisationDataDeletable {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private CoachingDAO coachingDao;
	@Autowired
	private CurriculumDAO curriculumDao;
	@Autowired
	private CurriculumMemberQueries memberQueries;
	@Autowired
	private LectureBlockToGroupDAO lectureBlockToGroupDao;
	@Autowired
	private RepositoryEntryMyCourseQueries myCourseQueries;
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

	@Override
	public Curriculum createCurriculum(String identifier, String displayName, String description, Organisation organisation) {
		return curriculumDao.createAndPersist(identifier, displayName, description, organisation);
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
	public List<CurriculumMember> getMembers(CurriculumRef curriculum, SearchMemberParameters params) {
		return memberQueries.getMembers(curriculum, params);
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
	public CurriculumElementType getCurriculumElementType(CurriculumElementTypeRef typeRef) {
		return curriculumElementTypeDao.loadByKey(typeRef.getKey());
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
		
		CurriculumElement clone = curriculumElementDao.createCurriculumElement(identifier, displayName, CurriculumElementStatus.active,
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
		
		List<CurriculumElement> childrenToClone = getCurriculumElements(elementToClone);
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
	public List<CurriculumElement> getCurriculumElements(Collection<? extends CurriculumElementRef> elementRefs) {
		return curriculumElementDao.loadByKeys(elementRefs);
	}

	@Override
	public boolean deleteCurriculumElement(CurriculumElementRef element) {
		if(element == null) return true; // nothing to do
		
		List<CurriculumElement> children = curriculumElementDao.getChildren(element);
		for(CurriculumElement child:children) {
			deleteCurriculumElement(child);
		}

		CurriculumElementImpl reloadedElement = (CurriculumElementImpl)curriculumElementDao.loadByKey(element.getKey());

		// remove relations to repository entries
		List<RepositoryEntryToGroupRelation> relationsToRepo = repositoryEntryRelationDao.getCurriculumRelations(reloadedElement);
		for(RepositoryEntryToGroupRelation relationToRepo:relationsToRepo) {
			if(!relationToRepo.isDefaultGroup()) {// only paranoia
				repositoryEntryRelationDao.removeRelation(relationToRepo);
			}
		}
		// remove relations to taxonomy
		curriculumElementToTaxonomyLevelDao.deleteRelation(reloadedElement);
		// remove relations to lecture blocks
		lectureBlockToGroupDao.deleteLectureBlockToGroup(reloadedElement.getGroup());
		
		boolean delete = true;
		Map<String,CurriculumDataDeletable> deleteDelegates = CoreSpringFactory.getBeansOfType(CurriculumDataDeletable.class);
		for(CurriculumDataDeletable deleteDelegate:deleteDelegates.values()) {
			delete &= deleteDelegate.deleteCurriculumElementData(reloadedElement);
		}

		if(delete) {
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
	public List<CurriculumElementInfos> getCurriculumElementsWithInfos(CurriculumRef curriculum) {
		return curriculumElementDao.loadElementsWithInfos(curriculum);
	}

	@Override
	public List<CurriculumElement> getCurriculumElements(RepositoryEntry entry) {
		return curriculumElementDao.loadElements(entry);
	}

	@Override
	public List<CurriculumElement> getCurriculumElements(RepositoryEntryRef entry, Identity identity,
			Collection<CurriculumRoles> roles) {
		return curriculumRepositoryEntryRelationDao.getCurriculumElements(entry, identity, roles);
	}

	@Override
	public List<CurriculumElement> getCurriculumElements(CurriculumElementRef parentElement) {
		return curriculumElementDao.getChildren(parentElement);
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
	public List<CurriculumMember> getMembers(CurriculumElement element, SearchMemberParameters params) {
		return memberQueries.getMembers(element, params);
	}
	
	@Override
	public List<CurriculumMemberStats> getMembersWithStats(CurriculumElement element, SearchMemberParameters params) {
		List<CurriculumMember> members = memberQueries.getMembers(element, params);
		
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
			List<CurriculumElement> descendants = curriculumElementDao.getDescendants(element);
			descendants.add(element);
			coachingDao.getStudentsCompletionStatement(descendants, rowLongMap);
		}
		
		return new ArrayList<>(rowMap.values());
	}

	@Override
	public List<Identity> getMembersIdentity(CurriculumElementRef element, CurriculumRoles role) {
		return curriculumElementDao.getMembersIdentity(element, role.name());
	}
	
	@Override
	public List<CurriculumElementMembership> getCurriculumElementMemberships(Curriculum curriculum, Identity identity) {
		if(identity == null) return new ArrayList<>();
		return curriculumElementDao.getMembershipInfos(Collections.singletonList(curriculum), null, identity);
	}

	@Override
	public List<CurriculumElementMembership> getCurriculumElementMemberships(Collection<CurriculumElement> elements, Identity... identities) {
		return curriculumElementDao.getMembershipInfos(null, elements, identities);
	}
	
	@Override
	public List<Identity> getMembersIdentity(List<Long> curriculumElementKeys, CurriculumRoles role) {
		if(role == null || curriculumElementKeys == null || curriculumElementKeys.isEmpty()) {
			return new ArrayList<>();
		}
		return curriculumElementDao.getMembersIdentity(curriculumElementKeys, role.name());
	}

	@Override
	public void updateCurriculumElementMemberships(Identity doer, Roles roles,
			List<CurriculumElementMembershipChange> changes, MailPackage mailing) {
		
		int count = 0;
		for(CurriculumElementMembershipChange change:changes) {
			updateCurriculumElementMembership(change);
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
		Map<Identity,CurriculumElementMembershipChange> additionsToNotifiy = new HashMap<>();
		for(CurriculumElementMembershipChange change:changes) {
			if(change.addRole()) {
				if(!additionsToNotifiy.containsKey(change.getMember())) {
					additionsToNotifiy.put(change.getMember(), change);
				} else {
					CurriculumElementMembershipChange currentChange = additionsToNotifiy.get(change.getMember());
					if(change.numOfSegments() < currentChange.numOfSegments()) {
						additionsToNotifiy.put(change.getMember(), change);
					}
				}
			}
		}
		
		for(CurriculumElementMembershipChange additionToNotifiy:additionsToNotifiy.values()) {
			Curriculum curriculum = additionToNotifiy.getElement().getCurriculum();
			CurriculumMailing.sendEmail(doer, additionToNotifiy.getMember(), curriculum, additionToNotifiy.getElement(), mailing);
		}
	}
	
	private void updateCurriculumElementMembership(CurriculumElementMembershipChange changes) {
		CurriculumElement element = changes.getElement();
		
		if(changes.getCurriculumElementOwner() != null) {
			if(changes.getCurriculumElementOwner().booleanValue()) {
				addMember(element, changes.getMember(), CurriculumRoles.curriculumelementowner);
			} else {
				removeMember(element, changes.getMember(), CurriculumRoles.curriculumelementowner);
			}
		}
		
		if(changes.getMasterCoach() != null) {
			if(changes.getMasterCoach().booleanValue()) {
				addMember(element, changes.getMember(), CurriculumRoles.mastercoach);
			} else {
				removeMember(element, changes.getMember(), CurriculumRoles.mastercoach);
			}
		}
		
		if(changes.getRepositoryEntryOwner() != null) {
			if(changes.getRepositoryEntryOwner().booleanValue()) {
				addMember(element, changes.getMember(), CurriculumRoles.owner);
			} else {
				removeMember(element, changes.getMember(), CurriculumRoles.owner);
			}
		}

		if(changes.getCoach() != null) {
			if(changes.getCoach().booleanValue()) {
				addMember(element, changes.getMember(), CurriculumRoles.coach);
			} else {
				removeMember(element, changes.getMember(), CurriculumRoles.coach);
			}
		}

		if(changes.getParticipant() != null) {
			if(changes.getParticipant().booleanValue()) {
				addMember(element, changes.getMember(), CurriculumRoles.participant);
			} else {
				removeMember(element, changes.getMember(), CurriculumRoles.participant);
			}
		}
	}

	@Override
	public void addMember(CurriculumElement element, Identity member, CurriculumRoles role) {
		GroupMembershipInheritance inheritanceMode;
		if(CurriculumRoles.isInheritedByDefault(role)) {
			inheritanceMode = GroupMembershipInheritance.root;
		} else {
			inheritanceMode = GroupMembershipInheritance.none;
		}
		addMember(element, member, role, inheritanceMode);
		dbInstance.commit();
	}
	
	public void addMember(CurriculumElement element, Identity member, CurriculumRoles role, GroupMembershipInheritance inheritanceMode) {
		if(inheritanceMode == GroupMembershipInheritance.inherited) {
			throw new AssertException("Inherited are automatic");
		}
		
		List<CurriculumElement> membershipAdded = new ArrayList<>();
		GroupMembership membership = groupDao.getMembership(element.getGroup(), member, role.name());
		if(membership == null) {
			groupDao.addMembershipOneWay(element.getGroup(), member, role.name(), inheritanceMode);
			membershipAdded.add(element);
		} else if(membership.getInheritanceMode() != inheritanceMode) {
			groupDao.updateInheritanceMode(membership, inheritanceMode);
		}
		
		if(inheritanceMode == GroupMembershipInheritance.root) {
			List<CurriculumElement> descendants = curriculumElementDao.getDescendants(element);
			for(CurriculumElement descendant:descendants) {
				GroupMembership inheritedMembership = groupDao.getMembership(descendant.getGroup(), member, role.name());
				if(inheritedMembership == null) {
					groupDao.addMembershipOneWay(descendant.getGroup(), member, role.name(), GroupMembershipInheritance.inherited);
					membershipAdded.add(element);
				} else if(inheritedMembership.getInheritanceMode() == GroupMembershipInheritance.none) {
					groupDao.updateInheritanceMode(inheritedMembership, GroupMembershipInheritance.inherited);
				}
			}
		}
		
		fireMemberAddedEvent(membershipAdded, member, role);
	}

	@Override
	public void removeMember(CurriculumElement element, IdentityRef member) {
		List<GroupMembership> memberships = groupDao.getMemberships(element.getGroup(), member);
		
		groupDao.removeMembership(element.getGroup(), member);
		
		CurriculumElementNode elementNode = null;
		for(GroupMembership membership:memberships) {
			if(membership.getInheritanceMode() == GroupMembershipInheritance.none) {
				groupDao.removeMembership(membership);
			} else if(membership.getInheritanceMode() == GroupMembershipInheritance.root) {
				String role = membership.getRole();
				groupDao.removeMembership(membership);
				
				if(elementNode == null) {
					elementNode = curriculumElementDao.getDescendantTree(element);
				}
				for(CurriculumElementNode child:elementNode.getChildrenNode()) {
					removeInherithedMembership(child, member, role);
				}
			}
		}
	}
	
	/**
	 * The method will recursively delete the inherithed membership. If it
	 * found a mebership marked as "root" or "none". It will stop.
	 * 
	 * @param elementNode The organization node
	 * @param member The user to remove
	 * @param role The role
	 */
	private void removeInherithedMembership(CurriculumElementNode elementNode, IdentityRef member, String role) {
		GroupMembership membership = groupDao
				.getMembership(elementNode.getElement().getGroup(), member, role);
		if(membership != null && membership.getInheritanceMode() == GroupMembershipInheritance.inherited) {
			groupDao.removeMembership(membership);
			for(CurriculumElementNode child:elementNode.getChildrenNode()) {
				removeInherithedMembership(child, member, role);
			}
		}
	}

	@Override
	public void removeMember(CurriculumElement element, IdentityRef member, CurriculumRoles role) {
		GroupMembership membership = groupDao.getMembership(element.getGroup(), member, role.name());
		
		groupDao.removeMembership(element.getGroup(), member, role.name());
		
		if(membership != null && (membership.getInheritanceMode() == GroupMembershipInheritance.root
				|| membership.getInheritanceMode() == GroupMembershipInheritance.none)) {
			groupDao.removeMembership(membership);
			if(membership.getInheritanceMode() == GroupMembershipInheritance.root
					|| membership.getInheritanceMode() == GroupMembershipInheritance.inherited) {
				CurriculumElementNode elementNode = curriculumElementDao.getDescendantTree(element);
				for(CurriculumElementNode child:elementNode.getChildrenNode()) {
					removeInherithedMembership(child, member, role.name());
				}
			}
		}
	}

	@Override
	public void removeMembers(CurriculumElement element, List<Identity> members, boolean overrideManaged) {
		if(!CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.members) || overrideManaged) {
			for(Identity member:members) {
				groupDao.removeMembership(element.getGroup(), member);
			}
		}
	}

	@Override
	public List<RepositoryEntry> getRepositoryEntries(CurriculumElementRef element) {
		List<CurriculumElementRef> elements = Collections.singletonList(element);
		return curriculumRepositoryEntryRelationDao
				.getRepositoryEntries(elements, RepositoryEntryStatusEnum.preparationToClosed(), false, null, null);
	}

	@Override
	public List<RepositoryEntry> getRepositoryEntriesWithDescendants(CurriculumElement element) {
		List<CurriculumElement> descendants = curriculumElementDao.getDescendants(element);
		descendants.add(element);
		List<CurriculumElementRef> descendantRefs = new ArrayList<>(descendants);
		return curriculumRepositoryEntryRelationDao
				.getRepositoryEntries(descendantRefs, RepositoryEntryStatusEnum.preparationToClosed(), false, null, null);
	}
	
	@Override
	public List<RepositoryEntry> getRepositoryEntriesOfParticipantWithDescendants(CurriculumElement element, Identity participant) {
		List<CurriculumElement> descendants = curriculumElementDao.getDescendants(element);
		descendants.add(element);
		List<String> roles = Arrays.asList(GroupRoles.participant.name());
		List<CurriculumElementRef> descendantRefs = new ArrayList<>(descendants);
		return curriculumRepositoryEntryRelationDao
				.getRepositoryEntries(descendantRefs, RepositoryEntryStatusEnum.preparationToClosed(), false, participant, roles);
	}

	@Override
	public List<RepositoryEntry> getRepositoryEntriesWithLecturesAndDescendants(CurriculumElement element, Identity identity) {
		List<CurriculumElement> descendants = curriculumElementDao.getDescendants(element);
		descendants.add(element);
		List<String> roles = Arrays.asList(OrganisationRoles.administrator.name(), OrganisationRoles.principal.name(),
				OrganisationRoles.learnresourcemanager.name(), GroupRoles.owner.name());
		List<CurriculumElementRef> descendantRefs = new ArrayList<>(descendants);
		return curriculumRepositoryEntryRelationDao
				.getRepositoryEntries(descendantRefs, RepositoryEntryStatusEnum.preparationToClosed(), true, identity, roles);
	}

	@Override
	public void addRepositoryEntry(CurriculumElement element, RepositoryEntryRef entry, boolean master) {
		RepositoryEntry repoEntry = repositoryEntryDao.loadByKey(entry.getKey());
		repositoryEntryRelationDao.createRelation(element.getGroup(), repoEntry);
		fireRepositoryEntryAddedEvent(element, entry);
	}

	@Override
	public boolean hasRepositoryEntry(CurriculumElement element, RepositoryEntryRef entry) {
		return repositoryEntryRelationDao.hasRelation(element.getGroup(), entry);
	}

	@Override
	public void removeRepositoryEntry(RepositoryEntry entry) {
		List<CurriculumElement> elements = curriculumRepositoryEntryRelationDao.getCurriculumElements(entry);
		for(CurriculumElement element:elements) {
			repositoryEntryRelationDao.removeRelation(element.getGroup(), entry);
		}
	}

	@Override
	public void removeRepositoryEntry(CurriculumElement element, RepositoryEntryRef entry) {
		repositoryEntryRelationDao.removeRelation(element.getGroup(), entry);
	}

	@Override
	public List<TaxonomyLevel> getTaxonomy(CurriculumElement element) {
		if(element == null || element.getKey() == null) return Collections.emptyList();
		return curriculumElementToTaxonomyLevelDao.getTaxonomyLevels(element);
	}

	@Override
	public List<CurriculumElement> getCurriculumElements(TaxonomyLevelRef level) {
		return curriculumElementToTaxonomyLevelDao.getCurriculumElements(level);
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
	public List<CurriculumElementRepositoryEntryViews> getCurriculumElements(Identity identity, Roles roles, List<? extends CurriculumRef> curriculums) {
		if(curriculums == null || curriculums.isEmpty()) return Collections.emptyList();
		
		List<CurriculumElementMembership> memberships = curriculumElementDao.getMembershipInfos(curriculums, null, identity);
		Map<Long,CurriculumElementMembership> membershipMap = new HashMap<>();
		for(CurriculumElementMembership membership:memberships) {
			membershipMap.put(membership.getCurriculumElementKey(), membership);
		}
		
		Map<CurriculumElement, List<Long>> elementsMap = curriculumRepositoryEntryRelationDao
				.getCurriculumElementsWithRepositoryEntryKeys(curriculums);
		List<CurriculumElementRepositoryEntryViews> elements = new ArrayList<>(elementsMap.size());
		if(!elementsMap.isEmpty()) {
			SearchMyRepositoryEntryViewParams params = new SearchMyRepositoryEntryViewParams(identity, roles);
			params.setCurriculums(curriculums);
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
		searchParams.setOrganisations(Collections.singletonList(organisation));
		searchParams.setWithDeleted(true);
		List<Curriculum> curriculums = curriculumDao.search(searchParams);
		for(Curriculum curriculum:curriculums) {
			curriculum.setOrganisation(replacementOrganisation);
			curriculumDao.update(curriculum);
		}
		return true;
	}
	
	private void fireMemberAddedEvent(Collection<? extends CurriculumElementRef> elements, Identity member,
			CurriculumRoles role) {
		CurriculumElementMembershipEvent event = new CurriculumElementMembershipEvent(
				CurriculumElementMembershipEvent.MEMEBER_ADDED, elements, member, role);
		coordinator.getCoordinator().getEventBus().fireEventToListenersOf(event,
				OresHelper.lookupType(CurriculumElement.class));
	}
	
	private void fireRepositoryEntryAddedEvent(CurriculumElementRef element, RepositoryEntryRef entry) {
		CurriculumElementRepositoryEntryEvent event = new CurriculumElementRepositoryEntryEvent(REPOSITORY_ENTRY_ADDED,
				element.getKey(), entry.getKey());
		coordinator.getCoordinator().getEventBus().fireEventToListenersOf(event,
				OresHelper.lookupType(CurriculumElement.class));
	}
	
}
