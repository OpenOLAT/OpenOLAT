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

import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumElementTypeRef;
import org.olat.modules.curriculum.CurriculumElementTypeToType;
import org.olat.modules.curriculum.model.CurriculumElementTypeToTypeImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 14 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CurriculumElementTypeToTypeDAO {
	
	@Autowired
	private DB dbInstance;
	
	public int disallowedSubType(CurriculumElementType parentType, CurriculumElementType disallowedSubType) {
		String q = "delete from curriculumelementtypetotype type2type where type2type.type.key=:typeKey and type2type.allowedSubType.key=:subTypeKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(q)
				.setParameter("typeKey", parentType.getKey())
				.setParameter("subTypeKey", disallowedSubType.getKey())
				.executeUpdate();
	}
	
	public void setAllowedSubType(CurriculumElementType parentType, List<CurriculumElementType> allowSubTypes) {
		List<CurriculumElementTypeToType> typeToTypes = getAllowedSubTypes(parentType);
		for(CurriculumElementTypeToType typeToType:typeToTypes) {
			boolean found = isTypeToTypeInList(typeToType, allowSubTypes);
			if(!found) {
				dbInstance.getCurrentEntityManager().remove(typeToType);
			}
		}

		for(CurriculumElementType allowSubType:allowSubTypes) {
			boolean found = isSubTypeInList(allowSubType, typeToTypes);
			if(!found) {
				addAllowedSubType(parentType, allowSubType);
			}
		}
	}
	
	private boolean isSubTypeInList(CurriculumElementType allowSubType, List<CurriculumElementTypeToType> typeToTypes) {
		for(CurriculumElementTypeToType typeToType:typeToTypes) {
			if(typeToType.getAllowedSubType().equals(allowSubType)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isTypeToTypeInList(CurriculumElementTypeToType typeToType, List<CurriculumElementType> allowSubTypes) {
		for(CurriculumElementType allowSubType:allowSubTypes) {
			if(typeToType.getAllowedSubType().equals(allowSubType)) {
				return true;
			}
		}
		return false;
	}
	
	public List<CurriculumElementTypeToType> getAllowedSubTypes(CurriculumElementTypeRef parentType) {
		String q = "select type2type from curriculumelementtypetotype type2type inner join fetch type2type.allowedSubType subType where type2type.type.key=:typeKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(q, CurriculumElementTypeToType.class)
				.setParameter("typeKey", parentType.getKey())
				.getResultList();
	}
	
	public List<CurriculumElementTypeToType> getAllowedSubTypes(CurriculumElementType parentType, CurriculumElementType allowedSubType) {
		String q = "select type2type from curriculumelementtypetotype type2type where type2type.type.key=:typeKey and type2type.allowedSubType.key=:subTypeKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(q, CurriculumElementTypeToType.class)
				.setParameter("typeKey", parentType.getKey())
				.setParameter("subTypeKey", allowedSubType.getKey())
				.getResultList();
	}
	
	public int deleteAllowedSubTypes(CurriculumElementTypeRef parentType) {
		String q = "delete from curriculumelementtypetotype where type.key=:typeKey";
		int rows = dbInstance.getCurrentEntityManager()
				.createQuery(q)
				.setParameter("typeKey", parentType.getKey())
				.executeUpdate();
		
		String qReverse = "delete from curriculumelementtypetotype where allowedSubType.key=:typeKey";
		rows += dbInstance.getCurrentEntityManager()
				.createQuery(qReverse)
				.setParameter("typeKey", parentType.getKey())
				.executeUpdate();
		
		return rows;
	}
	
	public void addAllowedSubType(CurriculumElementType parentType, CurriculumElementType allowedSubType) {
		CurriculumElementTypeToTypeImpl reloadedParentType = new CurriculumElementTypeToTypeImpl();
		reloadedParentType.setType(parentType);
		reloadedParentType.setAllowedSubType(allowedSubType);
		dbInstance.getCurrentEntityManager().persist(reloadedParentType);
	}

}
