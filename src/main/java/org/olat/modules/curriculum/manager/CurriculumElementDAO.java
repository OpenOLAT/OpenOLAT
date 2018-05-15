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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.GroupMembershipInheritance;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.modules.curriculum.model.CurriculumElementImpl;
import org.olat.modules.curriculum.model.CurriculumElementMember;
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
	
	public CurriculumElement createCurriculumElement(String identifier, String displayName, Date beginDate, Date endDate,
			CurriculumElementRef parentRef, CurriculumElementType elementType, Curriculum curriculum) {
		CurriculumElementImpl element = new CurriculumElementImpl();
		element.setCreationDate(new Date());
		element.setLastModified(element.getCreationDate());
		element.setIdentifier(identifier);
		element.setDisplayName(displayName);
		element.setBeginDate(beginDate);
		element.setEndDate(endDate);
		element.setCurriculum(curriculum);
		element.setType(elementType);
		element.setGroup(groupDao.createGroup());
		CurriculumElement parent = parentRef == null ? null : loadByKey(parentRef.getKey());
		element.setParent(parent);
		dbInstance.getCurrentEntityManager().persist(element);
		if(parent != null) {
			((CurriculumElementImpl)parent).getChildren().add(element);
			dbInstance.getCurrentEntityManager().merge(parent);
		}
		element.setMaterializedPathKeys(getMaterializedPathKeys(parent, element));
		return element;
	}
	
	public CurriculumElement loadByKey(Long key) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("select el from curriculumelement el")
		  .append(" inner join fetch el.curriculum curriculum")
		  .append(" inner join el.group baseGroup")
		  .append(" where el.key=:key");
		
		List<CurriculumElement> elements = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CurriculumElement.class)
				.setParameter("key", key)
				.getResultList();
		return elements == null || elements.isEmpty() ? null : elements.get(0);
	}
	
	public String getMaterializedPathKeys(CurriculumElement parent, CurriculumElement element) {
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
	
	public CurriculumElement move(CurriculumElement element, CurriculumElement newParentElement) {
		CurriculumElement parentElement = element.getParent();
		if(parentElement == null && newParentElement == null) {
			return element;//already root
		} else if(parentElement != null && parentElement.equals(newParentElement)) {
			return element;//same parent
		}

		String keysPath = element.getMaterializedPathKeys();
		
		List<CurriculumElement> descendants = getDescendants(element);
		CurriculumElementImpl elementImpl = (CurriculumElementImpl)element;
		elementImpl.setParent(newParentElement);
		elementImpl.setLastModified(new Date());
		String newKeysPath = getMaterializedPathKeys(newParentElement, elementImpl);
		elementImpl.setMaterializedPathKeys(newKeysPath);
		elementImpl = dbInstance.getCurrentEntityManager().merge(elementImpl);

		for(CurriculumElement descendant:descendants) {
			String descendantKeysPath = descendant.getMaterializedPathKeys();
			if(descendantKeysPath.indexOf(keysPath) == 0) {
				String end = descendantKeysPath.substring(keysPath.length(), descendantKeysPath.length());
				String updatedPath = newKeysPath + end;
				((CurriculumElementImpl)descendant).setMaterializedPathKeys(updatedPath);
			}
			dbInstance.getCurrentEntityManager().merge(descendant);
		}		
		dbInstance.commit();
		return elementImpl;
	}
	
	public List<CurriculumElement> loadElements(CurriculumRef curriculum) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select el from curriculumelement el")
		  .append(" inner join fetch el.curriculum curriculum")
		  .append(" inner join el.group baseGroup")
		  .append(" left join el.parent parentEl")
		  .append(" where el.curriculum.key=:curriculumKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CurriculumElement.class)
				.setParameter("curriculumKey", curriculum.getKey())
				.getResultList();
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
	
	public List<CurriculumElementMember> getMembers(CurriculumElementRef element) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select ident, membership.role, membership.inheritanceModeString from curriculumelement el")
		  .append(" inner join el.group baseGroup")
		  .append(" inner join baseGroup.members membership")
		  .append(" inner join membership.identity ident")
		  .append(" where el.key=:elementKey");
		List<Object[]> objects = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("elementKey", element.getKey())
				.getResultList();
		List<CurriculumElementMember> members = new ArrayList<>(objects.size());
		for(Object[] object:objects) {
			Identity identity = (Identity)object[0];
			String role = (String)object[1];
			String inheritanceModeString = (String)object[2];
			GroupMembershipInheritance inheritanceMode = GroupMembershipInheritance.none;
			if(StringHelper.containsNonWhitespace(inheritanceModeString)) {
				inheritanceMode = GroupMembershipInheritance.valueOf(inheritanceModeString);
			}
			members.add(new CurriculumElementMember(identity, role, inheritanceMode));
		}
		return members;
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
