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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.basesecurity.GroupMembershipInheritance;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementManagedFlag;
import org.olat.modules.curriculum.CurriculumElementMembership;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumElementTypeRef;
import org.olat.modules.curriculum.CurriculumElementTypeToType;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementInfos;
import org.olat.modules.curriculum.model.CurriculumElementMembershipChange;
import org.olat.modules.curriculum.model.CurriculumElementRepositoryEntryViews;
import org.olat.modules.curriculum.model.CurriculumInfos;
import org.olat.modules.curriculum.model.CurriculumMember;
import org.olat.modules.curriculum.model.CurriculumSearchParameters;
import org.olat.modules.curriculum.model.SearchMemberParameters;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryMyView;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.manager.RepositoryEntryDAO;
import org.olat.repository.manager.RepositoryEntryMyCourseQueries;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
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
public class CurriculumServiceImpl implements CurriculumService {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private CurriculumDAO curriculumDao;
	@Autowired
	private CurriculumMemberQueries memberQueries;
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
	public void addMember(Curriculum curriculum, Identity identity, CurriculumRoles role) {
		if(!groupDao.hasRole(curriculum.getGroup(), identity, role.name())) {
			groupDao.addMembershipOneWay(curriculum.getGroup(), identity, role.name(), GroupMembershipInheritance.none);
		}
	}

	@Override
	public void removeMember(Curriculum curriculum, Identity member, CurriculumRoles role) {
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
	public CurriculumElement createCurriculumElement(String identifier, String displayName, Date beginDate, Date endDate,
			CurriculumElementRef parentRef, CurriculumElementType elementType, Curriculum curriculum) {
		return curriculumElementDao.createCurriculumElement(identifier, displayName, beginDate, endDate, parentRef, elementType, curriculum);
	}

	@Override
	public CurriculumElement getCurriculumElement(CurriculumElementRef element) {
		return curriculumElementDao.loadByKey(element.getKey());
	}

	@Override
	public CurriculumElement updateCurriculumElement(CurriculumElement element) {
		return curriculumElementDao.update(element);
	}

	@Override
	public CurriculumElement moveCurriculumElement(CurriculumElement elementToMove, CurriculumElement newParent) {
		return curriculumElementDao.move(elementToMove, newParent);
	}

	@Override
	public List<CurriculumElement> getCurriculumElements(CurriculumRef curriculum) {
		return curriculumElementDao.loadElements(curriculum);
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
	public List<CurriculumElement> searchCurriculumElements(String externalId, String identifier, Long key) {
		return curriculumElementDao.searchElements(externalId, identifier, key);
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
	public List<Identity> getMembersIdentity(CurriculumElementRef element, CurriculumRoles role) {
		return curriculumElementDao.getMembersIdentity(element, role.name());
	}
	
	@Override
	public List<CurriculumElementMembership> getCurriculumElementMemberships(Collection<CurriculumElement> elements, Identity... identities) {
		return curriculumElementDao.getMembershipInfos(null, elements, identities);
	}
	
	@Override
	public void updateCurriculumElementMemberships(Identity doer, Roles roles,
			List<CurriculumElementMembershipChange> changes) {
		
		int count = 0;
		for(CurriculumElementMembershipChange change:changes) {
			updateCurriculumElementMembership(change);
			if(++count % 100 == 0) {
				dbInstance.commitAndCloseSession();
			}
		}
		dbInstance.commitAndCloseSession();
	}
	
	private void updateCurriculumElementMembership(CurriculumElementMembershipChange changes) {
		CurriculumElement element = changes.getElement();
		
		if(changes.getCurriculumManager() != null) {
			if(changes.getCurriculumManager().booleanValue()) {
				addMember(element, changes.getMember(), CurriculumRoles.curriculummanager);
			} else {
				removeMember(element, changes.getMember(), CurriculumRoles.curriculummanager);
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
		if(!groupDao.hasRole(element.getGroup(), member, role.name())) {
			groupDao.addMembershipOneWay(element.getGroup(), member, role.name(), GroupMembershipInheritance.none);
		} 
	}

	@Override
	public void removeMember(CurriculumElement element, IdentityRef member) {
		groupDao.removeMembership(element.getGroup(), member);
	}

	@Override
	public void removeMember(CurriculumElement element, IdentityRef member, CurriculumRoles role) {
		groupDao.removeMembership(element.getGroup(), member, role.name());
	}

	@Override
	public void removeMembers(CurriculumElement element, List<Identity> members) {
		if(!CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.members)) {
			for(Identity member:members) {
				groupDao.removeMembership(element.getGroup(), member);
			}
		}
	}

	@Override
	public List<RepositoryEntry> getRepositoryEntries(CurriculumElementRef element) {
		return curriculumRepositoryEntryRelationDao.getRepositoryEntries(element);
	}

	@Override
	public void addRepositoryEntry(CurriculumElement element, RepositoryEntryRef entry, boolean master) {
		RepositoryEntry repoEntry = repositoryEntryDao.loadByKey(entry.getKey());
		repositoryEntryRelationDao.createRelation(element.getGroup(), repoEntry);
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
	public List<CurriculumElementRepositoryEntryViews> getCurriculumElements(Identity identity, Roles roles, CurriculumRef curriculum) {
		if(curriculum == null) return Collections.emptyList();
		
		List<CurriculumElementMembership> memberships = curriculumElementDao.getMembershipInfos(curriculum, null, identity);
		Map<Long,CurriculumElementMembership> membershipMap = new HashMap<>();
		for(CurriculumElementMembership membership:memberships) {
			membershipMap.put(membership.getCurriculumElementKey(), membership);
		}
		
		Map<CurriculumElement, List<Long>> elementsMap = curriculumRepositoryEntryRelationDao.getCurriculumElementsWithRepositoryEntryKeys(curriculum);
		List<CurriculumElementRepositoryEntryViews> elements = new ArrayList<>(elementsMap.size());
		if(!elementsMap.isEmpty()) {
			SearchMyRepositoryEntryViewParams params = new SearchMyRepositoryEntryViewParams(identity, roles);
			params.setCurriculum(curriculum);
			List<RepositoryEntryMyView> views = myCourseQueries.searchViews(params, 0, -1);
			Map<Long, RepositoryEntryMyView> viewMap = new HashMap<>();
			for(RepositoryEntryMyView view:views) {
				viewMap.put(view.getKey(), view);
			}
			
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
				elements.add(new CurriculumElementRepositoryEntryViews(element, elementViews, membership));
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
}
