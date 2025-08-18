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

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.TemporalType;
import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupMembership;
import org.olat.basesecurity.GroupMembershipHistory;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.Automation;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementMembership;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumElementTypeRef;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.model.AutomationImpl;
import org.olat.modules.curriculum.model.CurriculumElementImpl;
import org.olat.modules.curriculum.model.CurriculumElementInfos;
import org.olat.modules.curriculum.model.CurriculumElementInfosSearchParams;
import org.olat.modules.curriculum.model.CurriculumElementMembershipHistory;
import org.olat.modules.curriculum.model.CurriculumElementMembershipHistorySearchParameters;
import org.olat.modules.curriculum.model.CurriculumElementMembershipImpl;
import org.olat.modules.curriculum.model.CurriculumElementNode;
import org.olat.modules.curriculum.model.CurriculumElementSearchInfos;
import org.olat.modules.curriculum.model.CurriculumElementSearchParams;
import org.olat.modules.curriculum.model.CurriculumImpl;
import org.olat.repository.RepositoryEntryRef;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 14 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CurriculumElementDAO {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private OLATResourceManager olatResourceManager;
	
	public CurriculumElement createCurriculumElement(String identifier, String displayName,
			CurriculumElementStatus status, Date beginDate, Date endDate, CurriculumElementRef parentRef,
			CurriculumElementType elementType, CurriculumCalendars calendars, CurriculumLectures lectures,
			CurriculumLearningProgress learningProgress, Curriculum curriculum) {
		CurriculumElementImpl element = new CurriculumElementImpl();
		element.setCreationDate(new Date());
		element.setLastModified(element.getCreationDate());
		element.setIdentifier(identifier);
		element.setDisplayName(displayName);
		element.setBeginDate(beginDate);
		element.setEndDate(endDate);
		element.setCurriculum(curriculum);
		element.setType(elementType);
		element.setCalendars(calendars);
		element.setLectures(lectures);
		element.setLearningProgress(learningProgress);
		element.setShowOutline(true);
		element.setShowLectures(true);
		element.setTaughtBys(new HashSet<>());
		element.setTaxonomyLevels(new HashSet<>());
		element.setChildren(new ArrayList<>());
		if(status == null) {
			element.setStatus(CurriculumElementStatus.preparation.name());
		} else {
			element.setStatus(status.name());
		}
		element.setGroup(groupDao.createGroup());
		CurriculumElement parent = parentRef == null ? null : loadByKey(parentRef.getKey());
		element.setParent(parent);
		dbInstance.getCurrentEntityManager().persist(element);
		if(parent != null) {
			((CurriculumElementImpl)parent).getChildren().add(element);
			dbInstance.getCurrentEntityManager().merge(parent);
		} else {
			element.setCurriculumParent(curriculum);
			((CurriculumImpl)curriculum).getRootElements().add(element);
			dbInstance.getCurrentEntityManager().merge(curriculum);
		}
		element.setMaterializedPathKeys(getMaterializedPathKeys(parent, element));
		createResource(element);
		dbInstance.getCurrentEntityManager().merge(element);
		return element;
	}
	
	public CurriculumElement copyCurriculumElement(CurriculumElement elementToCopy,
			String identifier, String displayName, Date beginDate, Date endDate,
			CurriculumElement parentElement, Curriculum curriculum) {
		CurriculumElement copy = createCurriculumElement(identifier, displayName, CurriculumElementStatus.preparation,
				beginDate, endDate, parentElement, elementToCopy.getType(), elementToCopy.getCalendars(), elementToCopy.getLectures(),
				elementToCopy.getLearningProgress(), curriculum);
		
		copy.setAuthors(elementToCopy.getAuthors());
		copy.setCredits(elementToCopy.getCredits());
		copy.setDescription(elementToCopy.getDescription());
		copy.setEducationalType(elementToCopy.getEducationalType());
		copy.setExpenditureOfWork(elementToCopy.getExpenditureOfWork());
		copy.setLocation(elementToCopy.getLocation());
		copy.setMainLanguage(elementToCopy.getMainLanguage());
		copy.setMaxParticipants(elementToCopy.getMaxParticipants());
		copy.setMinParticipants(elementToCopy.getMinParticipants());
		copy.setObjectives(elementToCopy.getObjectives());
		copy.setRequirements(elementToCopy.getRequirements());
		copy.setShowLectures(elementToCopy.isShowLectures());
		copy.setShowOutline(elementToCopy.isShowOutline());
		if(elementToCopy.getTaughtBys() != null) {
			copy.setTaughtBys(elementToCopy.getTaughtBys());
		}
		copy.setTeaser(elementToCopy.getTeaser());
		copy.setAutoInstantiation(copy(elementToCopy.getAutoInstantiation()));
		copy.setAutoAccessForCoach(copy(elementToCopy.getAutoAccessForCoach()));
		copy.setAutoPublished(copy(elementToCopy.getAutoPublished()));
		copy.setAutoClosed(copy(elementToCopy.getAutoClosed()));
		
		return dbInstance.getCurrentEntityManager().merge(copy);
	}
	
	private Automation copy(Automation automationToCopy) {
		return automationToCopy == null
				? null
				: AutomationImpl.valueOf(automationToCopy.getValue(), automationToCopy.getUnit());
	}
	
	public OLATResource createResource(CurriculumElement element) {
		OLATResource resource =  olatResourceManager.createOLATResourceInstance(element);
		olatResourceManager.saveOLATResource(resource);
		((CurriculumElementImpl)element).setResource(resource);
		return resource;
	}
	
	/**
	 * The element must be loaded. The method doesn't do a reload.
	 * 
	 * @param element
	 */
	public void deleteCurriculumElement(CurriculumElement element) {
		dbInstance.getCurrentEntityManager().remove(element);
		
		Group group = element.getGroup();
		groupDao.removeMemberships(group);
		groupDao.removeGroup(group);
	}
	
	public CurriculumElement loadByKey(Long key) {
		String sb = """
			select el from curriculumelement el
			inner join fetch el.curriculum curriculum
			inner join fetch el.group baseGroup
			left join fetch el.type elementType
			left join fetch el.resource rsrc
			where el.key=:key""";
		
		List<CurriculumElement> elements = dbInstance.getCurrentEntityManager()
				.createQuery(sb, CurriculumElement.class)
				.setParameter("key", key)
				.getResultList();
		return elements == null || elements.isEmpty() ? null : elements.get(0);
	}

	public List<CurriculumElement> loadByKeys(Collection<? extends CurriculumElementRef> elementRefs) {
		if (elementRefs == null || elementRefs.isEmpty()) return new ArrayList<>(0);
		
		StringBuilder sb = new StringBuilder(128);
		sb.append("select el");
		sb.append("  from curriculumelement el");
		sb.append("       left join fetch el.type");
		sb.append("       left join el.parent parentEl");
		sb.append(" where el.key in :keys");
		
		List<Long> keys = elementRefs.stream().map(CurriculumElementRef::getKey).collect(Collectors.toList());
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CurriculumElement.class)
				.setParameter("keys", keys)
				.getResultList();
	}
	
	/**
	 * Calculate the materialized path from the parent element.
	 * 
	 * @param parent The parent element (can be null if the element is a root one)
	 * @param element The curriculum element
	 * @return The materialized path of the specified element
	 */
	private String getMaterializedPathKeys(CurriculumElement parent, CurriculumElement element) {
		if(parent != null) {
			String parentPathOfKeys = parent.getMaterializedPathKeys();
			if(parentPathOfKeys == null || "/".equals(parentPathOfKeys)) {
				parentPathOfKeys = "";
			}
			return parentPathOfKeys + element.getKey() + "/";
		}
		return "/" + element.getKey() + "/";
	}
	
	public CurriculumElement update(CurriculumElement element) {
		((CurriculumElementImpl)element).setLastModified(new Date());
		((CurriculumElementImpl)element).setMaterializedPathKeys(getMaterializedPathKeys(element.getParent(), element));
		return dbInstance.getCurrentEntityManager().merge(element);
	}
	
	public int updateNumber(CurriculumElementRef element, String number) {
		String updateQuery = "update curriculumelement curEl set curEl.numberImpl=:number where curEl.key=:curriculumElementKey";
		return dbInstance.getCurrentEntityManager()
			.createQuery(updateQuery)
			.setParameter("number", number)
			.setParameter("curriculumElementKey", element.getKey())
			.executeUpdate();
	}
	
	public CurriculumElement move(CurriculumElement rootElementToMove, Curriculum newCurriculum) {
		CurriculumElementImpl rootElement = (CurriculumElementImpl)rootElementToMove;
		List<CurriculumElement> descendants = getDescendants(rootElement);// load the descendants with the old curriculum
		descendants.remove(rootElementToMove);

		CurriculumImpl oldCurriculum = loadCurriculumByKey(rootElement);
		if(oldCurriculum == null) return rootElement;
		oldCurriculum.getRootElements().remove(rootElement);
		rootElement.setPosCurriculum(null);
		rootElement.setCurriculumParent(null);
		rootElement = dbInstance.getCurrentEntityManager().merge(rootElement);
		dbInstance.getCurrentEntityManager().merge(oldCurriculum);

		CurriculumImpl curriculum = loadCurriculumByKey(newCurriculum.getKey());
		curriculum.getRootElements().add(rootElement);
		rootElement.setCurriculum(curriculum);
		rootElement.setPosCurriculum(Integer.valueOf(curriculum.getRootElements().size()));
		rootElement.setCurriculumParent(curriculum);
		rootElement = dbInstance.getCurrentEntityManager().merge(rootElement);
		curriculum = dbInstance.getCurrentEntityManager().merge(curriculum);
		
		for(CurriculumElement descendant:descendants) {
			((CurriculumElementImpl)descendant).setCurriculum(curriculum);
			dbInstance.getCurrentEntityManager().merge(descendant);
		}
		return rootElement;
	}
	
	public CurriculumElement orderList(CurriculumElement parentElement, List<CurriculumElementRef> orderLists) {
		final List<Long> elementKeys = orderLists.stream()
				.map(CurriculumElementRef::getKey)
				.collect(Collectors.toList());

		List<CurriculumElement> children = ((CurriculumElementImpl)parentElement).getChildren();
		if(children.isEmpty() || (elementKeys.size() <= 1 && children.size() == 1)) {
			// nothing to do
			return parentElement;
		}
		Collections.sort(children, new ReorderCurriculumElementComparator(elementKeys));
		return update(parentElement);
	}

	public CurriculumElement move(CurriculumElement elementToMove, CurriculumElement newParentElement,
			CurriculumElement siblingBefore, Curriculum targetCurriculum) {
		CurriculumElement parentElement = elementToMove.getParent();
		CurriculumElementImpl element = (CurriculumElementImpl)elementToMove;
		CurriculumImpl curriculum = loadCurriculumByKey(targetCurriculum.getKey());
		
		String keysPath = element.getMaterializedPathKeys();
		List<CurriculumElement> descendants = getDescendants(element);
		
		if(parentElement == null && newParentElement == null) {
			// reorder curriculum children
			
			List<CurriculumElement> rootElements = curriculum.getRootElements();
			reorderList(element, rootElements, siblingBefore);
			dbInstance.getCurrentEntityManager().merge(curriculum);
		} else if(parentElement == null) {
			// move from curriculum as root to a curriculum element
			
			List<CurriculumElement> rootElements = curriculum.getRootElements();
			element.setCurriculumParent(null);
			rootElements.remove(element);
			curriculum = dbInstance.getCurrentEntityManager().merge(curriculum);
			
			newParentElement = loadByKey(newParentElement.getKey());
			List<CurriculumElement> newChildren = ((CurriculumElementImpl)newParentElement).getChildren();
			reorderList(element, newChildren, siblingBefore);
			element.setParent(newParentElement);
			dbInstance.getCurrentEntityManager().merge(newParentElement);	
		} else if(newParentElement == null) {
			// move from a curriculum element to root level
			
			parentElement = loadByKey(parentElement.getKey());
			List<CurriculumElement> children = ((CurriculumElementImpl)parentElement).getChildren();
			children.remove(element);
			element.setParent(null);
			dbInstance.getCurrentEntityManager().merge(parentElement);	
			
			element.setCurriculumParent(curriculum);
			List<CurriculumElement> rootElements = curriculum.getRootElements();
			reorderList(element, rootElements, siblingBefore);
			dbInstance.getCurrentEntityManager().merge(curriculum);	
		} else if(parentElement.equals(newParentElement)) {
			// reorder under the same parent curriculum element
			
			newParentElement = loadByKey(newParentElement.getKey());
			List<CurriculumElement> newChildren = ((CurriculumElementImpl)newParentElement).getChildren();
			reorderList(element, newChildren, siblingBefore);
			dbInstance.getCurrentEntityManager().merge(newParentElement);
		} else {
			// move from a curriculum element to an other
			
			parentElement = loadByKey(parentElement.getKey());
			List<CurriculumElement> children = ((CurriculumElementImpl)parentElement).getChildren();
			children.remove(element);
			element.setParent(newParentElement);
			dbInstance.getCurrentEntityManager().merge(parentElement);	
			
			newParentElement = loadByKey(newParentElement.getKey());
			List<CurriculumElement> newChildren = ((CurriculumElementImpl)newParentElement).getChildren();
			reorderList(element, newChildren, siblingBefore);
			dbInstance.getCurrentEntityManager().merge(newParentElement);
		}

		element.setLastModified(new Date());
		String newKeysPath = getMaterializedPathKeys(newParentElement, element);
		element.setMaterializedPathKeys(newKeysPath);
		element.setCurriculum(curriculum);
		element = dbInstance.getCurrentEntityManager().merge(element);

		for(CurriculumElement descendant:descendants) {
			String descendantKeysPath = descendant.getMaterializedPathKeys();
			if(descendantKeysPath.indexOf(keysPath) == 0) {
				String end = descendantKeysPath.substring(keysPath.length(), descendantKeysPath.length());
				String updatedPath = newKeysPath + end;
				((CurriculumElementImpl)descendant).setMaterializedPathKeys(updatedPath);
			}
			((CurriculumElementImpl)descendant).setCurriculum(curriculum);
			dbInstance.getCurrentEntityManager().merge(descendant);
		}		
		dbInstance.commit();
		return element;
	}
	
	private void reorderList(CurriculumElement element, List<CurriculumElement> elements, CurriculumElement siblingBefore) {
		int currentIndex = elements.indexOf(element);
		if(siblingBefore == null) {
			if(currentIndex >= 0) {
				elements.remove(element);
			}
			elements.add(0, element);
		} else if(currentIndex < 0) {
			int siblingIndex = elements.indexOf(siblingBefore) + 1;
			if(siblingIndex >= 0 && siblingIndex < elements.size()) {
				elements.add(siblingIndex, element);
			} else {
				elements.add(element);
			}
		} else {
			int siblingIndex = elements.indexOf(siblingBefore) + 1;
			int newIndex = siblingIndex;
			if(currentIndex < siblingIndex) {
				newIndex--;
			}
			elements.remove(element);
			elements.add(newIndex, element);
		}
	}
	
	private CurriculumImpl loadCurriculumByKey(CurriculumElement element) {
		return loadCurriculumByKey(element.getCurriculum().getKey());
	}
	
	private CurriculumImpl loadCurriculumByKey(Long curriculumKey) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("select cur from curriculum cur")
		  .append(" where cur.key=:key");
		
		List<CurriculumImpl> curriculums = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), CurriculumImpl.class)
			.setParameter("key", curriculumKey)
			.getResultList();
		return curriculums == null || curriculums.isEmpty() ? null : curriculums.get(0);
	}
	
	public List<CurriculumElement> loadElements(CurriculumRef curriculum, CurriculumElementStatus[] status) {
		QueryBuilder sb = new QueryBuilder(256);
		sb.append("select el from curriculumelement el")
		  .append(" inner join fetch el.curriculum curriculum")
		  .append(" inner join fetch el.group baseGroup")
		  .append(" left join el.parent parentEl")
		  .append(" where el.curriculum.key=:curriculumKey and el.status ").in(status);
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CurriculumElement.class)
				.setParameter("curriculumKey", curriculum.getKey())
				.getResultList();
	}
	
	public List<CurriculumElementInfos> loadElementsWithInfos(CurriculumElementInfosSearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder(4096);
		sb.append("select el, ")
		  .append(" (select count(distinct reToGroup.entry.key) from repoentrytogroup reToGroup")
		  .append("  where reToGroup.group.key=baseGroup.key")
		  .append(" ) as numOfElements,")
		  .append(" (select count(distinct templateToGroup.entry.key) from repotemplatetogroup templateToGroup")
		  .append("  where templateToGroup.group.key=baseGroup.key")
		  .append(" ) as numOfTemplates,")
		  .append(" (select count(distinct lblock.key) from lectureblock lblock")
		  .append("  where lblock.curriculumElement.key=el.key")
		  .append(" ) as numOfLectures,")
		  .append(" (select count(distinct lblockEntry.key) from lectureblock lblockEntry")
		  .append("  where lblockEntry.curriculumElement.key=el.key and lblockEntry.entry.key is not null")
		  .append(" ) as numOfLecturesWithEntry,")
		  .append(" (select count(distinct participants.identity.key) from bgroupmember as participants")
		  .append("  where participants.group.key=baseGroup.key and participants.role='").append(GroupRoles.participant.name()).append("'")
		  .append(" ) as numOfParticipants,")
		  .append(" (select count(distinct coaches.identity.key) from bgroupmember as coaches")
		  .append("  where coaches.group.key=baseGroup.key and coaches.role='").append(GroupRoles.coach.name()).append("'")
		  .append(" ) as numOfCoaches,")
		  .append(" (select count(distinct owners.identity.key) from bgroupmember as owners")
		  .append("  where owners.group.key=baseGroup.key and owners.role='").append(GroupRoles.owner.name()).append("'")
		  .append(" ) as numOfOwners,")
		  .append(" (select count(distinct curriculElementOwners.identity.key) from bgroupmember as curriculElementOwners")
		  .append("  where curriculElementOwners.group.key=baseGroup.key and curriculElementOwners.role='").append(CurriculumRoles.curriculumelementowner.name()).append("'")
		  .append(" ) as numOfCurriculumElementOwners,")
		  .append(" (select count(distinct masterCoaches.identity.key) from bgroupmember as masterCoaches")
		  .append("  where masterCoaches.group.key=baseGroup.key and masterCoaches.role='").append(CurriculumRoles.mastercoach.name()).append("'")
		  .append(" ) as numOfMasterCoaches,")
		  .append(" (select count(distinct reservation.key) from resourcereservation as reservation")
		  .append("  where reservation.resource.key=el.resource.key")
		  .append(" ) as numOfReservations")
		  .append(" from curriculumelement el")
		  .append(" inner join fetch el.curriculum curriculum")
		  .append(" inner join fetch el.group baseGroup")
		  .append(" left join fetch el.type curElementType")
		  .append(" left join el.parent parentEl")
		  .append(" left join curriculum.organisation organis")
		  .where().append(" el.status ").in(CurriculumElementStatus.notDeleted());
		
		if(searchParams.getCurriculums() != null && !searchParams.getCurriculums().isEmpty()) {
			sb.and().append("el.curriculum.key in (:curriculumKey)");
		}
		if(searchParams.isRootElementsOnly()) {
			sb.and().append("parentEl.key is null");
		}
		if(searchParams.getStatusList() != null && !searchParams.getStatusList().isEmpty()) {
			sb.and().append("el.status in (:statusList)");
		}
		if(searchParams.getCurriculumElements() != null && !searchParams.getCurriculumElements().isEmpty()) {
			sb.and().append("el.key in (:curriculumElementsKeys)");
		}
		if(searchParams.getParentElement() != null) {
			if(searchParams.isParentElementInclusive()) {
				sb.and().append("(el.key=:parentElementKey or el.materializedPathKeys like :materializedPath)");
			} else {
				sb.and().append("(el.key<>:parentElementKey and el.materializedPathKeys like :materializedPath)");
			}
		}
		if(searchParams.getEntry() != null) {
			sb.and()
			  .append("baseGroup.key in (select rel.group.key from repoentrytogroup as rel")
			  .append(" where rel.entry.key=:entryKey)");
		}
		
		if(searchParams.getIdentity() != null) {
			// curriculum administrator at level curriculum
			sb.and()
			  .append("(curriculum.group.key in (select cGroup.key from bgroupmember as cMembership")
			  .append("  inner join cMembership.group as cGroup")
			  .append("  where cMembership.identity.key=:managerKey")
			  .append("  and cMembership.role ").in(CurriculumRoles.curriculumowner)
			  .append(" )");
			
			sb.append(" or baseGroup.key in (select cGroup.key from bgroupmember as cMembership")
			  .append("  inner join cMembership.group as cGroup")
			  .append("  where cMembership.identity.key=:managerKey")
			  .append("  and cMembership.role ").in(CurriculumRoles.curriculumelementowner)
			  .append(" )");
			// curriculum administrator from the organisation
			sb.append(" or organis.group.key in (select oGroup.key from bgroupmember as oMembership")
			  .append("  inner join oMembership.group as oGroup")
			  .append("  where oMembership.identity.key=:managerKey")
			  .append("  and oMembership.role ").in(CurriculumRoles.curriculummanager, OrganisationRoles.administrator)
			  .append(" ))");
		}

		TypedQuery<Object[]> rawQuery = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class);
		
		if(searchParams.getCurriculums() != null && !searchParams.getCurriculums().isEmpty()) {
			List<Long> curriculumKeys = searchParams.getCurriculums().stream()
					.map(CurriculumRef::getKey).toList();
			rawQuery.setParameter("curriculumKey", curriculumKeys);
		}

		if(searchParams.getStatusList() != null && !searchParams.getStatusList().isEmpty()) {
			List<String> status = searchParams.getStatusList().stream()
					.map(CurriculumElementStatus::toString).toList();
			rawQuery.setParameter("statusList", status);
		}
		if(searchParams.getCurriculumElements() != null && !searchParams.getCurriculumElements().isEmpty()) {
			List<Long> curriculumElementsKeys = searchParams.getCurriculumElements().stream()
					.map(CurriculumElementRef::getKey)
					.toList();
			rawQuery.setParameter("curriculumElementsKeys", curriculumElementsKeys);
		}
		if(searchParams.getParentElement() != null) {
			rawQuery.setParameter("parentElementKey", searchParams.getParentElement().getKey());
			rawQuery.setParameter("materializedPath", searchParams.getParentElement().getMaterializedPathKeys() + "%");
		}
		if(searchParams.getEntry() != null) {
			rawQuery.setParameter("entryKey", searchParams.getEntry().getKey());
		}
		if(searchParams.getIdentity() != null) {
			rawQuery.setParameter("managerKey", searchParams.getIdentity().getKey());
		}
		
		List<Object[]> rawObjects =	rawQuery.getResultList();
		List<CurriculumElementInfos> infos = new ArrayList<>(rawObjects.size());
		for(Object[] rawObject:rawObjects) {
			CurriculumElement element = (CurriculumElement)rawObject[0];
			long numOfResources = PersistenceHelper.extractPrimitiveLong(rawObject, 1);
			long numOfTemplates = PersistenceHelper.extractPrimitiveLong(rawObject, 2);
			long numOfLectures = PersistenceHelper.extractPrimitiveLong(rawObject, 3);
			long numOfLecturesWithEntry = PersistenceHelper.extractPrimitiveLong(rawObject, 4);
			long numOfParticipants = PersistenceHelper.extractPrimitiveLong(rawObject, 5);
			long numOfCoaches = PersistenceHelper.extractPrimitiveLong(rawObject, 6);
			long numOfOwners = PersistenceHelper.extractPrimitiveLong(rawObject, 7);
			long numOfCurriculumElementOwners = PersistenceHelper.extractPrimitiveLong(rawObject, 8);
			long numOfMasterCoaches = PersistenceHelper.extractPrimitiveLong(rawObject, 9);
			long numOfPending = PersistenceHelper.extractPrimitiveLong(rawObject, 10);
			
			infos.add(new CurriculumElementInfos(element, element.getCurriculum(),
					numOfResources, numOfTemplates, numOfLectures, numOfLecturesWithEntry,
					numOfParticipants, numOfCoaches, numOfOwners,
					numOfCurriculumElementOwners, numOfMasterCoaches, numOfPending));
		}
		return infos;
	}
	
	public Long countElements(RepositoryEntryRef entry) {
		String query = """
				select count(*) from curriculumelement el
				inner join el.group bGroup
				inner join repoentrytogroup as rel on (bGroup.key=rel.group.key)
				where rel.entry.key=:entryKey""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, Long.class)
				.setParameter("entryKey", entry.getKey())
				.getSingleResult();
	}
	
	public List<CurriculumElement> loadElements(RepositoryEntryRef entry) {
		String query = """
				select el from curriculumelement el
				inner join fetch el.curriculum curriculum
				inner join fetch el.resource resource
				inner join fetch el.group bGroup
				inner join repoentrytogroup as rel on (bGroup.key=rel.group.key)
				left join fetch curriculum.organisation org
				left join fetch el.type elementType
				left join fetch el.parent parentEl
				where rel.entry.key=:entryKey""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, CurriculumElement.class)
				.setParameter("entryKey", entry.getKey())
				.getResultList();
	}
	
	public boolean hasElements(RepositoryEntryRef entry) {
		String query = """
				select el.key from curriculumelement el
				inner join el.group bGroup
				inner join repoentrytogroup as rel on (bGroup.key=rel.group.key)
				where rel.entry.key=:entryKey""";
		List<Long> elementsKeys = dbInstance.getCurrentEntityManager()
				.createQuery(query, Long.class)
				.setParameter("entryKey", entry.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return elementsKeys != null && !elementsKeys.isEmpty()
				&& elementsKeys.get(0) != null && elementsKeys.get(0).longValue() > 0;
	}
	
	public CurriculumElement loadElementByResource(OLATResource resource) {
		String query = """
				select el from curriculumelement el
				inner join fetch el.curriculum curriculum
				inner join fetch el.resource resource
				inner join fetch el.group bGroup
				left join fetch curriculum.organisation org
				left join fetch el.type elementType
				left join fetch el.parent parentEl
				where resource.key=:resourceKey""";
	
		List<CurriculumElement> elements = dbInstance.getCurrentEntityManager()
				.createQuery(query, CurriculumElement.class)
				.setParameter("resourceKey", resource.getKey())
				.getResultList();
		return elements == null || elements.isEmpty() ? null : elements.get(0);
	}
	
	public List<CurriculumElement> loadElementsByResources(Collection<OLATResource> resources) {
		if (resources == null || resources.isEmpty()) return new ArrayList<>(0);
		
		String sb = """
				select el from curriculumelement el
				inner join fetch el.curriculum curriculum
				inner join fetch el.resource resource
				inner join fetch el.group bGroup
				left join el.parent parentEl
				where resource.key in :resourcesKeys""";
		
		List<Long> resourcesKeys = resources.stream()
				.map(OLATResource::getKey).collect(toList());
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CurriculumElement.class)
				.setParameter("resourcesKeys", resourcesKeys)
				.getResultList();
	}

	public List<CurriculumElement> loadElementsByCurriculums(Collection<? extends CurriculumRef> curriculumRefs) {
		if (curriculumRefs == null || curriculumRefs.isEmpty()) return new ArrayList<>(0);
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("select el");
		sb.append("  from curriculumelement el");
		sb.append("       inner join fetch el.curriculum curriculum");
		sb.append("       left join el.parent parentEl");
		sb.and().append("el.curriculum.key in :curriculumKeys");
		
		List<Long> curriculumKeys = curriculumRefs.stream().map(CurriculumRef::getKey).collect(toList());
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CurriculumElement.class)
				.setParameter("curriculumKeys", curriculumKeys)
				.getResultList();
	}
	
	public List<Long> loadReservedElementKeys(IdentityRef identity) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select distinct el.key");
		sb.append(" from curriculumelement el");
		sb.append(" inner join resourcereservation reservation on reservation.resource.key = el.resource.key");
		sb.and().append("reservation.identity.key = :identityKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
	}
	
	public List<CurriculumElement> searchElements(String externalId, String identifier, Long key) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select el from curriculumelement el")
		  .append(" inner join fetch el.curriculum curriculum")
		  .append(" inner join fetch el.group bGroup")
		  .append(" left join fetch curriculum.organisation org");
		
		boolean where = false;
		if(StringHelper.containsNonWhitespace(externalId)) {
			where = PersistenceHelper.appendAnd(sb, where);
			sb.append("el.externalId=:externalId");
		}
		if(StringHelper.containsNonWhitespace(identifier)) {
			where = PersistenceHelper.appendAnd(sb, where);
			sb.append("el.identifier=:identifier");
		}
		if(key != null) {
			where = PersistenceHelper.appendAnd(sb, where);
			sb.append("el.key=:key");
		}
		TypedQuery<CurriculumElement> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CurriculumElement.class);
		if(StringHelper.containsNonWhitespace(externalId)) {
			query.setParameter("externalId", externalId);
		}
		if(StringHelper.containsNonWhitespace(identifier)) {
			query.setParameter("identifier", identifier);
		}
		if(key != null) {
			query.setParameter("key", key);
		}
		return query.getResultList();
	}
	
	/**
	 * Search curriculum, curriculum element, courses
	 * 
	 * @param params
	 * @return
	 */
	public List<CurriculumElementSearchInfos> searchElements(CurriculumElementSearchParams params) {
		QueryBuilder sb = new QueryBuilder(2048);
		sb.append("select curEl,")
		  .append(" (select count(childEl.key) from curriculumelement childEl")
		  .append("  where childEl.parent.key=curEl.key")
		  .append(" ) as numOfChildren,")
		  .append(" (select count(distinct reToGroup.entry.key) from repoentrytogroup reToGroup")
		  .append("  where reToGroup.group.key=elGroup.key")
		  .append(" ) as numOfElements,")
		  .append(" (select count(distinct lblock.key) from lectureblock lblock")
		  .append("  where lblock.curriculumElement.key=curEl.key")
		  .append(" ) as numOfLectures,")
		  .append(" (select count(distinct participants.identity.key) from bgroupmember as participants")
		  .append("  where participants.group.key=elGroup.key and participants.role='").append(CurriculumRoles.participant.name()).append("'")
		  .append(" ) as numOfParticipants,")
		  .append(" (select count(distinct coaches.identity.key) from bgroupmember as coaches")
		  .append("  where coaches.group.key=elGroup.key and coaches.role='").append(CurriculumRoles.coach.name()).append("'")
		  .append(" ) as numOfCoaches,")
		  .append(" (select count(distinct owners.identity.key) from bgroupmember as owners")
		  .append("  where owners.group.key=elGroup.key and owners.role='").append(CurriculumRoles.owner.name()).append("'")
		  .append(" ) as numOfOwners,")
		  .append(" (select count(distinct curriculumElementOwners.identity.key) from bgroupmember as curriculumElementOwners")
		  .append("  where curriculumElementOwners.group.key=elGroup.key and curriculumElementOwners.role='").append(CurriculumRoles.curriculumelementowner.name()).append("'")
		  .append(" ) as numOfCurriculumElementOwners,")
		  .append(" (select count(distinct masterCoaches.identity.key) from bgroupmember as masterCoaches")
		  .append("  where masterCoaches.group.key=elGroup.key and masterCoaches.role='").append(CurriculumRoles.mastercoach.name()).append("'")
		  .append(" ) as numOfMasterCoaches")
		  .append(" from curriculumelement curEl")
		  .append(" inner join fetch curEl.curriculum cur")
		  .append(" inner join fetch cur.group curGroup")
		  .append(" inner join fetch curEl.group elGroup")
		  .append(" left join fetch curEl.type elType")
		  .append(" left join fetch cur.organisation organis")
		  .append(" left join repoentrytogroup as rel on (elGroup.key=rel.group.key)")
		  .append(" left join repositoryentry as v on (rel.entry.key=v.key)")
		  .append(" left join v.olatResource as res");

		// generic search
		Long key = null;
		String ref = null;
		String fuzzyRef = null;
		if(StringHelper.containsNonWhitespace(params.getSearchString())) {
			ref = params.getSearchString();
			fuzzyRef = PersistenceHelper.makeFuzzyQueryString(ref);
			
			sb.and()
			  .append(" (cur.externalId=:ref or curEl.externalId=:ref or v.externalId=:ref or ")
			  .likeFuzzy("cur.displayName", "fuzzyRef")
			  .append(" or ")
			  .likeFuzzy("cur.identifier", "fuzzyRef")
			  .append(" or ")
			  .likeFuzzy("curEl.displayName", "fuzzyRef")
			  .append(" or ")
			  .likeFuzzy("curEl.identifier", "fuzzyRef")
			  .append(" or ")
			  .likeFuzzy("v.displayname", "fuzzyRef")
			  .append(" or ")
			  .likeFuzzy("v.externalRef", "fuzzyRef");
			if(StringHelper.isLong(ref)) {
				key = Long.valueOf(ref);
				sb.append(" or cur.key=:cKey or curEl.key=:cKey");
			}
			sb.append(")");	
		}
		
		//dates
		if(params.getElementBeginDate() != null && params.getElementEndDate() != null) {
			sb.and()
			  .append("(curEl.beginDate is not null or curEl.endDate is not null)")
			  .append(" and (")
			  .append(" (curEl.endDate is null and (curEl.beginDate is null or curEl.beginDate>=:elementBegin))")
			  .append("  or ")
			  .append(" (curEl.beginDate is null and (curEl.endDate is null or curEl.endDate<=:elementEnd))")
			  .append("  or ")
			  .append(" (curEl.beginDate is not null and curEl.endDate is not null and curEl.beginDate>=:elementBegin and curEl.endDate<=:elementEnd)")
			  .append(" )");
		} else if(params.getElementBeginDate() != null) {
			sb.and()
			  .append("curEl.beginDate>=:elementBegin");
		} else if(params.getElementEndDate() != null) {
			sb.and()
			  .append("curEl.endDate<=:elementEnd");
		}
		
		// curriculum element
		Long elementKey = null;
		String elementFuzzyRef = null;
		if(StringHelper.containsNonWhitespace(params.getElementId())) {
			String elementId = params.getElementId();
			elementFuzzyRef = PersistenceHelper.makeFuzzyQueryString(elementId);
			
			sb.and()
			  .append("(")
			  .likeFuzzy("curEl.identifier", "elementFuzzyRef")
			  .append(" or ")
			  .likeFuzzy("curEl.externalId", "elementFuzzyRef");
			if(StringHelper.isLong(elementId)) {
				elementKey = Long.valueOf(elementId);
				sb.append(" or curEl.key=:elementKey");
			}
			sb.append(")");	
		}

		String elementTextFuzzyRef = null;
		if(StringHelper.containsNonWhitespace(params.getElementText())) {
			elementTextFuzzyRef = PersistenceHelper.makeFuzzyQueryString(params.getElementText());
			sb.and()
			  .append("(")
			  .likeFuzzy("curEl.displayName", "elementTextFuzzyRef")
			  .append(")");
		}
		
		// repository entry
		Long entryKey = null;
		String entryRef = null;
		String entryFuzzyRef = null;
		if(StringHelper.containsNonWhitespace(params.getEntryId())) {
			entryRef = params.getEntryId();
			entryFuzzyRef = PersistenceHelper.makeFuzzyQueryString(entryRef);
			
			sb.and()
			  .append("(v.softkey=:entryRef or v.externalId=:entryRef or ")
			  .likeFuzzy("v.externalRef", "entryFuzzyRef");
			if(StringHelper.isLong(entryRef)) {
				entryKey = Long.valueOf(entryRef);
				sb.append(" or v.key=:entryKey or res.resId=:entryKey");
			}
			sb.append(")");	
		}
		
		String entryTextFuzzyRef = null;
		if(StringHelper.containsNonWhitespace(params.getEntryText())) {
			entryTextFuzzyRef = PersistenceHelper.makeFuzzyQueryString(params.getEntryText());
			sb.and()
			  .append("(")
			  .likeFuzzy("v.displayname", "entryTextFuzzyRef")
			  .append(")");
		}

		// permissions
		if(params.getManagerIdentity() != null) {
			// curriculum administrator at level curriculum
			sb.and()
			  .append("(curGroup.key in (select cGroup.key from bgroupmember as cMembership")
			  .append("  inner join cMembership.group as cGroup")
			  .append("  where cMembership.identity.key=:managerKey")
			  .append("  and cMembership.role ").in(CurriculumRoles.curriculumowner)
			  .append(" )");
			
			sb.append(" or elGroup.key in (select cGroup.key from bgroupmember as cMembership")
			  .append("  inner join cMembership.group as cGroup")
			  .append("  where cMembership.identity.key=:managerKey")
			  .append("  and cMembership.role ").in(CurriculumRoles.curriculumelementowner)
			  .append(" )");
			// curriculum administrator from the organisation
			sb.append(" or organis.group.key in (select oGroup.key from bgroupmember as oMembership")
			  .append("  inner join oMembership.group as oGroup")
			  .append("  where oMembership.identity.key=:managerKey")
			  .append("  and oMembership.role ").in(CurriculumRoles.curriculummanager, OrganisationRoles.administrator)
			  .append(" ))");
		}
		
		if(params.getCurriculums() != null && !params.getCurriculums().isEmpty()) {
			sb.and().append("curEl.curriculum.key in (:curriculumKey)");
		}
		
		if(params.getElementTypes() != null && !params.getElementTypes().isEmpty()) {
			sb.and().append("elType.key in (:elementTypeKeys)");
		}
		
		if(params.getStatus() != null && !params.getStatus().isEmpty()) {
			sb.and().append("curEl.status in (:statusList)");
		} else {
			sb.and()
			  .append(" curEl.status ").in(CurriculumElementStatus.notDeleted());
		}

		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class);
		if(params.getElementBeginDate() != null) {
			query.setParameter("elementBegin", params.getElementBeginDate(), TemporalType.TIMESTAMP);
		}
		if(params.getElementEndDate() != null) {
			query.setParameter("elementEnd", params.getElementEndDate(), TemporalType.TIMESTAMP);
		}
		
		if(key != null) {
			query.setParameter("cKey", key);
		}
		if(ref != null) {
			query.setParameter("ref", ref);
		}
		if(fuzzyRef != null) {
			query.setParameter("fuzzyRef", fuzzyRef);
		}
		if(params.getManagerIdentity() != null) {
			query.setParameter("managerKey", params.getManagerIdentity().getKey());
		}
		
		if(elementKey != null) {
			query.setParameter("elementKey", elementKey);
		}
		if(elementFuzzyRef != null) {
			query.setParameter("elementFuzzyRef", elementFuzzyRef);
		}
		if(elementTextFuzzyRef != null) {
			query.setParameter("elementTextFuzzyRef", elementTextFuzzyRef);
		}
		
		if(entryKey != null) {
			query.setParameter("entryKey", entryKey);
		}
		if(entryRef != null) {
			query.setParameter("entryRef", entryRef);
		}
		if(entryFuzzyRef != null) {
			query.setParameter("entryFuzzyRef", entryFuzzyRef);
		}
		if(entryTextFuzzyRef != null) {
			query.setParameter("entryTextFuzzyRef", entryTextFuzzyRef);
		}
		
		if(params.getManagerIdentity() != null) {
			query.setParameter("managerKey", params.getManagerIdentity().getKey());
		}
		
		if(params.getCurriculums() != null && !params.getCurriculums().isEmpty()) {
			List<Long> curriculumKeys = params.getCurriculums().stream()
					.map(CurriculumRef::getKey).toList();
			query.setParameter("curriculumKey", curriculumKeys);
		}
		
		if(params.getElementTypes() != null && !params.getElementTypes().isEmpty()) {
			List<Long> typeKeys = params.getElementTypes().stream()
					.map(CurriculumElementTypeRef::getKey).toList();
			query.setParameter("elementTypeKeys", typeKeys);
		}
		
		if(params.getStatus() != null && !params.getStatus().isEmpty()) {
			List<String> status = params.getStatus().stream()
					.map(CurriculumElementStatus::toString).toList();
			query.setParameter("statusList", status);
		}

		List<Object[]> rawObjects = query.getResultList();
		List<CurriculumElementSearchInfos> infos = new ArrayList<>(rawObjects.size());
		Set<CurriculumElement> deduplicates = new HashSet<>();
		for(Object[] rawObject:rawObjects) {
			CurriculumElement element = (CurriculumElement)rawObject[0];
			if(!deduplicates.contains(element)) {
				long numOfChildren = PersistenceHelper.extractPrimitiveLong(rawObject, 1);
				long numOfResources = PersistenceHelper.extractPrimitiveLong(rawObject, 2);
				long numOfLectureBlocks = PersistenceHelper.extractPrimitiveLong(rawObject, 3);
				long numOfParticipants = PersistenceHelper.extractPrimitiveLong(rawObject, 4);
				long numOfCoaches = PersistenceHelper.extractPrimitiveLong(rawObject, 5);
				long numOfOwners = PersistenceHelper.extractPrimitiveLong(rawObject, 6);
				long numOfCurriculumElementOwners = PersistenceHelper.extractPrimitiveLong(rawObject, 7);
				long numOfMasterCoaches = PersistenceHelper.extractPrimitiveLong(rawObject, 8);
				
				infos.add(new CurriculumElementSearchInfos(element,
						numOfChildren, numOfResources, numOfLectureBlocks,
						numOfParticipants, numOfCoaches, numOfOwners,
						numOfCurriculumElementOwners, numOfMasterCoaches));
				deduplicates.add(element);
			}
		}
		return infos;
	}
	
	public List<CurriculumElement> getParentLine(CurriculumElement curriculumElement) {
		String sb = """
				select el from curriculumelement as el
				inner join fetch el.curriculum as curriculum
				inner join fetch el.group as baseGroup
				left join fetch el.parent as parent
				left join fetch el.type as type
				where curriculum.key=:curriculumKey and locate(el.materializedPathKeys,:materializedPath) = 1""";
		  
		List<CurriculumElement> elements = dbInstance.getCurrentEntityManager()
			.createQuery(sb, CurriculumElement.class)
			.setParameter("curriculumKey", curriculumElement.getCurriculum().getKey())
			.setParameter("materializedPath", curriculumElement.getMaterializedPathKeys() + "%")
			.getResultList();
		Collections.sort(elements, new PathMaterializedPathLengthComparator());
		return elements;
	}
	
	public List<CurriculumElement> getImplementations(Curriculum curriculum, CurriculumElementStatus... status) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("""
				select el from curriculumelement as el
				inner join fetch el.curriculum as curriculum
				inner join fetch el.group as baseGroup
				left join fetch el.parent as parent
				left join fetch el.type as type
				where curriculum.key=:curriculumKey and el.parent.key is null""");
		
		List<String> statusList = new ArrayList<>();
		if(status != null && status.length > 0 && status[0] != null) {
			for(CurriculumElementStatus s:status) {
				if(s != null) {
					statusList.add(s.name());
				}
			}	
		}
		
		if(!statusList.isEmpty()) {
			sb.append(" and el.status in (:status)");
		}
		  
		TypedQuery<CurriculumElement> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), CurriculumElement.class)
			.setParameter("curriculumKey", curriculum.getKey());
		if(!statusList.isEmpty()) {
			query.setParameter("status", statusList);
		}
		
		List<CurriculumElement> elements = query.getResultList();
		Collections.sort(elements, new PathMaterializedPathLengthComparator());
		return elements;
	}
	
	public List<CurriculumElement> getDescendants(CurriculumElement curriculumElement) {
		String sb = """
				select el from curriculumelement as el
				inner join fetch el.curriculum as curriculum
				inner join fetch el.group as baseGroup
				left join fetch el.parent as parent
				left join fetch el.type as type
				left join fetch el.resource as resource
				where el.curriculum.key=:curriculumKey
				and el.key!=:elementKey and el.materializedPathKeys like :materializedPath""";
		  
		List<CurriculumElement> elements = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), CurriculumElement.class)
			.setParameter("materializedPath", curriculumElement.getMaterializedPathKeys() + "%")
			.setParameter("elementKey", curriculumElement.getKey())
			.setParameter("curriculumKey", curriculumElement.getCurriculum().getKey())
			.getResultList();
		Collections.sort(elements, new PathMaterializedPathLengthComparator());
		return elements;
	}
	
	public int countChildren(CurriculumElementRef curriculumElement) {
		String query = """
				select count(el.key) from curriculumelement el
				where el.parent.key=:elementKey""";
		List<Long> count = dbInstance.getCurrentEntityManager()
				.createQuery(query, Long.class)
				.setParameter("elementKey", curriculumElement.getKey())
				.getResultList();
		return count != null && !count.isEmpty() && count.get(0) != null ? count.get(0).intValue() : 0;
	}
	
	/**
	 * The method returns all the children, inclusive the marked as deleted.
	 * 
	 * @param curriculumElement The parent element
	 * @return A list of curriculum elements
	 */
	public List<CurriculumElement> getChildren(CurriculumElementRef curriculumElement) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select el from curriculumelement el")
		  .append(" inner join fetch el.curriculum curriculum")
		  .append(" inner join fetch el.group bGroup")
		  .append(" left join fetch curriculum.organisation org")
		  .append(" where el.parent.key=:elementKey")
		  .append(" order by el.pos");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CurriculumElement.class)
				.setParameter("elementKey", curriculumElement.getKey())
				.getResultList();
	}
	
	public boolean hasChildren(CurriculumElementRef curriculumElement) {
		List<Long> firstChild = dbInstance.getCurrentEntityManager()
				.createNamedQuery("hasCurriculumElementchildren", Long.class)
				.setParameter("elementKey", curriculumElement.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return firstChild != null && !firstChild.isEmpty()
				&& firstChild.get(0) != null && firstChild.get(0).longValue() > 0;
	}
	
	public CurriculumElementNode getDescendantTree(CurriculumElement rootElement) {
		List<CurriculumElement> descendants = getDescendants(rootElement);
		CurriculumElementNode rootNode = new CurriculumElementNode(rootElement);
		
		Map<Long,CurriculumElementNode> keyToOrganisations = new HashMap<>();
		for(CurriculumElement descendant:descendants) {
			keyToOrganisations.put(descendant.getKey(), new CurriculumElementNode(descendant));
		}

		for(CurriculumElement descendant:descendants) {
			Long key = descendant.getKey();
			if(key.equals(rootElement.getKey())) {
				continue;
			}
			
			CurriculumElementNode node = keyToOrganisations.get(key);
			CurriculumElement parentOrganisation = descendant.getParent();
			Long parentKey = parentOrganisation.getKey();
			if(parentKey.equals(rootElement.getKey())) {
				//this is a root, or the user has not access to parent
				rootNode.addChild(node);
			} else {
				CurriculumElementNode parentNode = keyToOrganisations.get(parentKey);
				parentNode.addChild(node);
			}
		}
		return rootNode;
	}
	
	public List<Identity> getMembersIdentity(CurriculumElementRef element, String role) {
		String sb = """
				select ident from curriculumelement el
				inner join el.group baseGroup
				inner join baseGroup.members membership
				inner join membership.identity ident
				inner join fetch ident.user identUser
				where el.key=:elementKey and membership.role=:role""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("elementKey", element.getKey())
				.setParameter("role", role)
				.getResultList();
	}
	
	public List<Identity> getMembersIdentity(List<Long> elementKeys, String role) {
		if(role == null || elementKeys == null || elementKeys.isEmpty()) return new ArrayList<>();
		
		String sb = """
				select ident from curriculumelement el
				inner join el.group baseGroup
				inner join baseGroup.members membership
				inner join membership.identity ident
				inner join fetch ident.user identUser
				where el.key in (:elementKeys) and membership.role=:role""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("elementKeys", elementKeys)
				.setParameter("role", role)
				.getResultList();
	}
	
	public List<Long> getMemberKeys(List<CurriculumElementRef> elements, String... roles) {
		if(elements == null || elements.isEmpty()) return new ArrayList<>();
		
		List<Long> elementKeys = elements.stream()
				.map(CurriculumElementRef::getKey)
				.toList();
		List<String> roleList = CurriculumRoles.toList(roles);
		return getMemberKeys(elementKeys, roleList);
	}

	public List<Long> getMemberKeys(List<Long> elementKeys, List<String> roles) {
		String query = """
				select distinct membership.identity.key from curriculumelement el
				inner join el.group baseGroup
				inner join baseGroup.members membership
				where el.key in (:elementKeys) and membership.role in (:roles)""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, Long.class)
				.setParameter("elementKeys", elementKeys)
				.setParameter("roles", roles)
				.getResultList();
	}
	
	public boolean hasCurriculumElementRole(IdentityRef identity, String role) {
		String query = """
				select el.key from curriculumelement el
				inner join el.group baseGroup
				inner join baseGroup.members membership
				where membership.identity.key=:identityKey and membership.role=:role""";
		List<Long> has = dbInstance.getCurrentEntityManager()
				.createQuery(query, Long.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("role", role)
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return has != null && !has.isEmpty() && has.get(0) != null;
	}
	
	public List<Identity> getMembers(List<CurriculumElementRef> elements, String... roles) {
		if(elements == null || elements.isEmpty()) return new ArrayList<>();
		
		List<Long> elementKeys = elements.stream()
				.map(CurriculumElementRef::getKey).collect(Collectors.toList());
		List<String> roleList = CurriculumRoles.toList(roles);
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("select ident from curriculumelement el")
		  .append(" inner join el.group baseGroup")
		  .append(" inner join baseGroup.members membership")
		  .append(" inner join membership.identity ident")
		  .append(" inner join fetch ident.user identUser")
		  .append(" where el.key in (:elementKeys) and membership.role in (:roles)");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("elementKeys", elementKeys)
				.setParameter("roles", roleList)
				.getResultList();
	}
	

	public List<CurriculumElementMembershipHistory> getMembershipInfosAndHistory(CurriculumElementMembershipHistorySearchParameters params) {
		
		QueryBuilder sb = new QueryBuilder(256);
		sb.append("select el.key, membershiphistory from curriculumelement el")
		  .append(" inner join el.group baseGroup")
		  .append(" inner join bgroupmemberhistory membershiphistory on (membershiphistory.group.key = baseGroup.key)")
		  .append(" inner join fetch membershiphistory.identity ident")
		  .append(" inner join fetch membershiphistory.group hGroup")
		  .append(" inner join fetch ident.user identUser")
		  .append(" inner join fetch membershiphistory.creator creator")
		  .append(" inner join fetch creator.user creatorUser");
		
		if(params.getIdentities() != null && !params.getIdentities().isEmpty()) {
			sb.and().append("ident.key in (:identIds) ");
		}
		if(params.getElements() != null && !params.getElements().isEmpty()) {
			sb.and().append("el.key in (:elementKeys)");
		}
		if(params.getCurriculum() != null) {
			sb.and().append("el.curriculum.key=:curriculumKey");
		}
		if(params.isExcludeMembers()) {
			sb.and().append("ident.key not in (select member.group.key from bgroupmember member")
			  .append(" where member.identity.key=ident.key and member.group.key=el.group.key")
			  .append(")");
		}
		if(params.isExcludeReservations()) {
			sb.and().append("baseGroup.key not in (select member.group.key from bgroupmember member")
			  .append(" where member.identity.key=ident.key")
			  .append(")");
		}
		
		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class);
		if(params.getIdentities() != null && !params.getIdentities().isEmpty()) {
			List<Long> ids = params.getIdentities().stream()
					.map(Identity::getKey)
					.toList();
			query.setParameter("identIds", ids);
		}
		if(params.getElements() != null && !params.getElements().isEmpty()) {
			List<Long> elementKeys = params.getElements().stream()
					.map(CurriculumElementRef::getKey)
					.toList();
			query.setParameter("elementKeys", elementKeys);
		}
		if(params.getCurriculum() != null) {
			query.setParameter("curriculumKey", params.getCurriculum().getKey());
		}
		
		List<Object[]> rawObjects = query.getResultList();
		Map<IdentityToElementKey, CurriculumElementMembershipHistory> history = new HashMap<>();
		for(Object[] object:rawObjects) {
			Long elementKey = (Long)object[0];
			GroupMembershipHistory membershipHistory = (GroupMembershipHistory)object[1];
			Identity identity = membershipHistory.getIdentity();
			if(CurriculumRoles.isValueOf(membershipHistory.getRole())) {
				CurriculumRoles cRole = CurriculumRoles.valueOf(membershipHistory.getRole());
				IdentityToElementKey key = new IdentityToElementKey(identity.getKey(), elementKey);
				CurriculumElementMembershipHistory membership = history
						.computeIfAbsent(key, k -> new CurriculumElementMembershipHistory(identity, k.getCurriculumElementKey()));
				membership.addHistoiryPoint(cRole, membershipHistory);
			}
		}
		return new ArrayList<>(history.values());
	}
	
	public List<CurriculumElementMembership> getMembershipInfos(List<? extends CurriculumRef> curriculums, Collection<? extends CurriculumElementRef> elements, Identity... identities) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select el.key, membership from curriculumelement el")
		  .append(" inner join el.group baseGroup")
		  .append(" inner join baseGroup.members membership")
		  .append(" inner join fetch membership.identity ident")
		  .append(" inner join fetch ident.user user");
		boolean and = false;
		if(identities != null && identities.length > 0) {
			and = and(sb, and);
			sb.append("ident.key in (:identIds) ");
		}
		if(elements != null && !elements.isEmpty()) {
			and = and(sb, and);
			sb.append("el.key in (:elementKeys)");
		}
		if(curriculums != null && !curriculums.isEmpty()) {
			and = and(sb, and);
			sb.append("el.curriculum.key in (:curriculumKeys)");
		}
		
		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class);
		if(identities != null && identities.length > 0) {
			List<Long> ids = new ArrayList<>(identities.length);
			for(Identity id:identities) {
				ids.add(id.getKey());
			}
			query.setParameter("identIds", ids);
		}
		if(elements != null && !elements.isEmpty()) {
			List<Long> elementKeys = elements.stream()
					.map(CurriculumElementRef::getKey).collect(Collectors.toList());
			query.setParameter("elementKeys", elementKeys);
		}
		if(curriculums != null &&!curriculums.isEmpty()) {
			List<Long> curriculumKeys = curriculums.stream()
					.map(CurriculumRef::getKey).collect(Collectors.toList());
			query.setParameter("curriculumKeys", curriculumKeys);
		}

		List<Object[]> rawObjects = query.getResultList();
		Map<IdentityToElementKey, CurriculumElementMembershipImpl> memberships = new HashMap<>();
		for(Object[] object:rawObjects) {
			Long elementKey = (Long)object[0];
			GroupMembership groupMembership = (GroupMembership)object[1];
			Long identityKey = groupMembership.getIdentity().getKey();
			String role = groupMembership.getRole();
			
			IdentityToElementKey key = new IdentityToElementKey(identityKey, elementKey);
			CurriculumElementMembershipImpl membership = memberships
					.computeIfAbsent(key, k -> new CurriculumElementMembershipImpl(k.getIdentityKey(), k.getCurriculumElementKey()));
			
			if(CurriculumRoles.curriculumelementowner.name().equals(role)) {
				membership.setCurriculumElementOwner(true);
			} else if(CurriculumRoles.owner.name().equals(role)) {
				membership.setRepositoryEntryOwner(true);
			} else if(CurriculumRoles.coach.name().equals(role)) {
				membership.setCoach(true);
			} else if(CurriculumRoles.participant.name().equals(role)) {
				membership.setParticipant(true);
			}else if(CurriculumRoles.mastercoach.name().equals(role)) {
				membership.setMasterCoach(true);
			}
		}
		return new ArrayList<>(memberships.values());
	}
	
	private final boolean and(StringBuilder sb, boolean and) {
		if(and) sb.append(" and ");
		else sb.append(" where ");
		return true;
	}
	
	private static class IdentityToElementKey {
		
		private final Long identityKey;
		private final Long curriculumElementKey;
		
		public IdentityToElementKey(Long identityKey, Long curriculumElementKey) {
			this.identityKey = identityKey;
			this.curriculumElementKey = curriculumElementKey;
		}

		public Long getIdentityKey() {
			return identityKey;
		}

		public Long getCurriculumElementKey() {
			return curriculumElementKey;
		}

		@Override
		public int hashCode() {
			return identityKey.hashCode() + curriculumElementKey.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			}
			if(obj instanceof IdentityToElementKey el) {
				return identityKey.equals(el.identityKey) && curriculumElementKey.equals(el.curriculumElementKey);
			}
			return false;
		}
	}
	
	private static class PathMaterializedPathLengthComparator implements Comparator<CurriculumElement> {
		@Override
		public int compare(CurriculumElement c1, CurriculumElement c2) {
			String s1 = c1.getMaterializedPathKeys();
			String s2 = c2.getMaterializedPathKeys();
			
			int len1 = s1 == null ? 0 : s1.length();
			int len2 = s2 == null ? 0 : s2.length();
			return len1 - len2;
		}
	}

}
