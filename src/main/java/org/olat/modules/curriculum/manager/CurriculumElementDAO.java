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
	
	public CurriculumElement createCurriculumElement(String identifier, String displayName, CurriculumElementRef parentRef, Curriculum curriculum) {
		CurriculumElementImpl element = new CurriculumElementImpl();
		element.setCreationDate(new Date());
		element.setLastModified(element.getCreationDate());
		element.setIdentifier(identifier);
		element.setDisplayName(displayName);
		element.setCurriculum(curriculum);
		element.setGroup(groupDao.createGroup());
		CurriculumElement parent = parentRef == null ? null : loadByKey(parentRef.getKey());
		element.setParent(parent);
		dbInstance.getCurrentEntityManager().persist(element);
		
		if(parent != null) {
			((CurriculumElementImpl)parent).getChildren().add(element);
			dbInstance.getCurrentEntityManager().merge(parent);
		}
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
	
	public CurriculumElement update(CurriculumElement element) {
		((CurriculumElementImpl)element).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(element);
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
}
