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

import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupMembership;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementMembership;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.model.CurriculumElementImpl;
import org.olat.modules.curriculum.model.CurriculumElementInfos;
import org.olat.modules.curriculum.model.CurriculumElementMembershipImpl;
import org.olat.modules.curriculum.model.CurriculumElementNode;
import org.olat.modules.curriculum.model.CurriculumElementSearchInfos;
import org.olat.modules.curriculum.model.CurriculumElementSearchParams;
import org.olat.modules.curriculum.model.CurriculumImpl;
import org.olat.repository.RepositoryEntryRef;
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
		if(status == null) {
			element.setStatus(CurriculumElementStatus.active.name());
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
		dbInstance.getCurrentEntityManager().merge(element);
		return element;
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
		StringBuilder sb = new StringBuilder(128);
		sb.append("select el from curriculumelement el")
		  .append(" inner join fetch el.curriculum curriculum")
		  .append(" inner join fetch el.group baseGroup")
		  .append(" where el.key=:key");
		
		List<CurriculumElement> elements = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CurriculumElement.class)
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
		rootElement.setPosCurriculum(Long.valueOf(curriculum.getRootElements().size()));
		rootElement.setCurriculumParent(curriculum);
		rootElement = dbInstance.getCurrentEntityManager().merge(rootElement);
		curriculum = dbInstance.getCurrentEntityManager().merge(curriculum);
		
		for(CurriculumElement descendant:descendants) {
			((CurriculumElementImpl)descendant).setCurriculum(curriculum);
			dbInstance.getCurrentEntityManager().merge(descendant);
		}
		return rootElement;
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
	
	public List<CurriculumElementInfos> loadElementsWithInfos(CurriculumRef curriculum) {
		QueryBuilder sb = new QueryBuilder(512);
		sb.append("select el, ")
		  .append(" (select count(distinct reToGroup.entry.key) from repoentrytogroup reToGroup")
		  .append("  where reToGroup.group.key=baseGroup.key")
		  .append(" ) as numOfElements,")
		  .append(" (select count(distinct participants.identity.key) from bgroupmember as participants")
		  .append("  where participants.group.key=baseGroup.key and participants.role='").append(GroupRoles.participant.name()).append("'")
		  .append(" ) as numOfParticipants,")
		  .append(" (select count(distinct coaches.identity.key) from bgroupmember as coaches")
		  .append("  where coaches.group.key=baseGroup.key and coaches.role='").append(GroupRoles.coach.name()).append("'")
		  .append(" ) as numOfCoaches,")
		  .append(" (select count(distinct owners.identity.key) from bgroupmember as owners")
		  .append("  where owners.group.key=baseGroup.key and owners.role='").append(GroupRoles.owner.name()).append("'")
		  .append(" ) as numOfOwners")
		  .append(" from curriculumelement el")
		  .append(" inner join fetch el.curriculum curriculum")
		  .append(" inner join fetch el.group baseGroup")
		  .append(" left join fetch el.type curElementType")
		  .append(" left join el.parent parentEl")
		  .append(" where el.curriculum.key=:curriculumKey and el.status ").in(CurriculumElementStatus.notDeleted());
		
		List<Object[]> rawObjects = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("curriculumKey", curriculum.getKey())
				.getResultList();
		List<CurriculumElementInfos> infos = new ArrayList<>(rawObjects.size());
		for(Object[] rawObject:rawObjects) {
			CurriculumElement element = (CurriculumElement)rawObject[0];
			Long rawNumOfResources = PersistenceHelper.extractLong(rawObject, 1);
			long numOfResources = rawNumOfResources == null ? 0l : rawNumOfResources.longValue();
			Long rawNumOfParticipants = PersistenceHelper.extractLong(rawObject, 2);
			long numOfParticipants = rawNumOfParticipants == null ? 0l : rawNumOfParticipants.longValue();
			Long rawNumOfCoaches = PersistenceHelper.extractLong(rawObject, 3);
			long numOfCoaches = rawNumOfCoaches == null ? 0l : rawNumOfCoaches.longValue();
			Long rawNumOfOwners = PersistenceHelper.extractLong(rawObject, 4);
			long numOfOwners = rawNumOfOwners == null ? 0l : rawNumOfOwners.longValue();
			infos.add(new CurriculumElementInfos(element, numOfResources, numOfParticipants, numOfCoaches, numOfOwners));
		}
		return infos;
	}
	
	public List<CurriculumElement> loadElements(RepositoryEntryRef entry) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select el from curriculumelement el")
		  .append(" inner join fetch el.curriculum curriculum")
		  .append(" inner join fetch el.group bGroup")
		  .append(" inner join repoentrytogroup as rel on (bGroup.key=rel.group.key)")
		  .append(" left join fetch curriculum.organisation org")
		  .append(" where rel.entry.key=:entryKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CurriculumElement.class)
				.setParameter("entryKey", entry.getKey())
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
		  .append(" (select count(distinct reToGroup.entry.key) from repoentrytogroup reToGroup")
		  .append("  where reToGroup.group.key=bGroup.key")
		  .append(" ) as numOfElements")
		  .append(" from curriculumelement curEl")
		  .append(" inner join fetch curEl.curriculum cur")
		  .append(" inner join fetch cur.group baseGroup")
		  .append(" inner join fetch curEl.group bGroup")
		  .append(" left join fetch cur.organisation organis")
		  .append(" left join repoentrytogroup as rel on (bGroup.key=rel.group.key)")
		  .append(" left join repositoryentry as v on (rel.entry.key=v.key)")
		  .append(" left join v.olatResource as res");
		
		sb.and()
		  .append(" curEl.status ").in(CurriculumElementStatus.notDeleted());
		
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
			sb.and()
			  .append("exists (select membership.key from bgroupmember as membership")
			  .append("  where membership.identity.key=:managerKey")
			  .append("  and membership.role").in(CurriculumRoles.curriculummanager, CurriculumRoles.owner, OrganisationRoles.administrator)
			  .append("  and (membership.group.key=baseGroup.key or membership.group.key=organis.group.key)")
			  .append(")");
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

		List<Object[]> rawObjects = query.getResultList();
		List<CurriculumElementSearchInfos> infos = new ArrayList<>(rawObjects.size());
		Set<CurriculumElement> deduplicates = new HashSet<>();
		for(Object[] rawObject:rawObjects) {
			CurriculumElement element = (CurriculumElement)rawObject[0];
			if(!deduplicates.contains(element)) {
				Long rawNumOfResources = PersistenceHelper.extractLong(rawObject, 1);
				long numOfResources = rawNumOfResources == null ? 0l : rawNumOfResources.longValue();
				
				infos.add(new CurriculumElementSearchInfos(element, numOfResources));
				deduplicates.add(element);
			}
		}
		return infos;
	}
	
	public List<CurriculumElement> getParentLine(CurriculumElement curriculumElement) {
		StringBuilder sb = new StringBuilder(384);
		sb.append("select el from curriculumelement as el")
		  .append(" inner join el.curriculum as curriculum")
		  .append(" inner join el.group as baseGroup")
		  .append(" left join fetch el.parent as parent")
		  .append(" left join fetch el.type as type")
		  .append(" where curriculum.key=:curriculumKey and locate(el.materializedPathKeys,:materializedPath) = 1");
		  
		List<CurriculumElement> elements = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), CurriculumElement.class)
			.setParameter("curriculumKey", curriculumElement.getCurriculum().getKey())
			.setParameter("materializedPath", curriculumElement.getMaterializedPathKeys() + "%")
			.getResultList();
		Collections.sort(elements, new PathMaterializedPathLengthComparator());
		return elements;
	}
	
	public List<CurriculumElement> getDescendants(CurriculumElement curriculumElement) {
		StringBuilder sb = new StringBuilder(384);
		sb.append("select el from curriculumelement as el")
		  .append(" inner join el.curriculum as curriculum")
		  .append(" inner join el.group as baseGroup")
		  .append(" left join fetch el.parent as parent")
		  .append(" left join fetch el.type as type")
		  .append(" where el.curriculum.key=:curriculumKey")
		  .append(" and el.key!=:elementKey and el.materializedPathKeys like :materializedPath");
		  
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
		StringBuilder sb = new StringBuilder(256);
		sb.append("select count(el.key) from curriculumelement el")
		  .append(" where el.parent.key=:elementKey");
		List<Long> count = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
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
	
	/**
	 * The method returns all the children, inclusive the marked as deleted.
	 * 
	 * @param curriculumElement The parent element
	 * @return A list of curriculum elements
	 */
	public List<CurriculumElement> getChildren(CurriculumRef curriculum) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select el from curriculumelement el")
		  .append(" inner join fetch el.curriculum curriculum")
		  .append(" inner join fetch el.group bGroup")
		  .append(" left join fetch curriculum.organisation org")
		  .append(" where el.curriculumParent.key=:curriculumKey")
		  .append(" order by el.posCurriculum");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CurriculumElement.class)
				.setParameter("curriculumKey", curriculum.getKey())
				.getResultList();
	}
	
	public CurriculumElementNode getDescendantTree(CurriculumElement rootElement) {
		List<CurriculumElement> descendants = getChildren(rootElement);
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
		StringBuilder sb = new StringBuilder(256);
		sb.append("select ident from curriculumelement el")
		  .append(" inner join el.group baseGroup")
		  .append(" inner join baseGroup.members membership")
		  .append(" inner join membership.identity ident")
		  .append(" inner join fetch ident.user identUser")
		  .append(" where el.key=:elementKey and membership.role=:role");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("elementKey", element.getKey())
				.setParameter("role", role)
				.getResultList();
	}
	
	public List<Identity> getMembersIdentity(List<Long> elementKeys, String role) {
		if(role == null || elementKeys == null || elementKeys.isEmpty()) return new ArrayList<>();
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("select ident from curriculumelement el")
		  .append(" inner join el.group baseGroup")
		  .append(" inner join baseGroup.members membership")
		  .append(" inner join membership.identity ident")
		  .append(" inner join fetch ident.user identUser")
		  .append(" where el.key in (:elementKeys) and membership.role=:role");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("elementKeys", elementKeys)
				.setParameter("role", role)
				.getResultList();
	}
	
	public List<Long> getMemberKeys(List<CurriculumElementRef> elements, String... roles) {
		if(elements == null || elements.isEmpty()) return new ArrayList<>();
		
		List<Long> elementKeys = elements.stream()
				.map(CurriculumElementRef::getKey).collect(Collectors.toList());
		List<String> roleList = CurriculumRoles.toList(roles);
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("select membership.identity.key from curriculumelement el")
		  .append(" inner join el.group baseGroup")
		  .append(" inner join baseGroup.members membership")
		  .append(" where el.key in (:elementKeys) and membership.role in (:roles)");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("elementKeys", elementKeys)
				.setParameter("roles", roleList)
				.getResultList();
	}
	
	public boolean hasCurriculumElementRole(IdentityRef identity, String role) {
		QueryBuilder sb = new QueryBuilder(256);
		sb.append("select el.key from curriculumelement el")
		  .append(" inner join el.group baseGroup")
		  .append(" inner join baseGroup.members membership")
		  .append(" where membership.identity.key=:identityKey and membership.role=:role");
		List<Long> has = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
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
	
	public List<CurriculumElementMembership> getMembershipInfos(List<? extends CurriculumRef> curriculums, Collection<CurriculumElement> elements, Identity... identities) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select el.key, membership from curriculumelement el")
		  .append(" inner join el.group baseGroup")
		  .append(" inner join baseGroup.members membership")
		  .append(" inner join fetch membership.identity ident");
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
					.map(CurriculumElement::getKey).collect(Collectors.toList());
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
			if(obj instanceof IdentityToElementKey) {
				IdentityToElementKey el = (IdentityToElementKey)obj;
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
