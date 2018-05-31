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

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumElementTypeRef;
import org.olat.modules.curriculum.model.CurriculumElementTypeImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 14 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CurriculumElementTypeDAO {
	
	@Autowired
	private DB dbInstance;
	
	public CurriculumElementType createCurriculumElementType(String identifier, String displayName, String description, String externalId) {
		CurriculumElementTypeImpl type = new CurriculumElementTypeImpl();
		type.setCreationDate(new Date());
		type.setLastModified(type.getCreationDate());
		type.setIdentifier(identifier);
		type.setDisplayName(displayName);
		type.setDescription(description);
		type.setExternalId(externalId);
		dbInstance.getCurrentEntityManager().persist(type);
		return type;
	}
	
	public CurriculumElementType cloneCurriculumElementType(CurriculumElementTypeRef typeRef) {
		CurriculumElementType reloadedType = loadByKey(typeRef.getKey());
		
		CurriculumElementTypeImpl clone = new CurriculumElementTypeImpl();
		clone.setCreationDate(new Date());
		clone.setLastModified(clone.getCreationDate());
		clone.setIdentifier(reloadedType.getIdentifier() + " (Copy)");
		clone.setDisplayName(reloadedType.getDisplayName());
		clone.setDescription(reloadedType.getDescription());
		clone.setCssClass(reloadedType.getCssClass());
		dbInstance.getCurrentEntityManager().persist(clone);
		return clone;
	}
	
	public CurriculumElementType loadByKey(Long key) {
		List<CurriculumElementType> types = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadCurriculumElementTypeByKey", CurriculumElementType.class)
				.setParameter("key", key)
				.getResultList();
		return types == null || types.isEmpty() ? null : types.get(0);
	}
	
	public boolean hasElements(CurriculumElementTypeRef typeRef) {
		String query = "select el.key from curriculumelement el where el.type.key=:typeKey";
		List<Long> types = dbInstance.getCurrentEntityManager()
				.createQuery(query, Long.class)
				.setParameter("typeKey", typeRef.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return types != null && !types.isEmpty() && types.get(0) != null && types.get(0).longValue() > 0;
	}
	
	public List<CurriculumElementType> load() {
		return dbInstance.getCurrentEntityManager()
			.createNamedQuery("loadCurriculumElementTypes", CurriculumElementType.class)
			.getResultList();
	}
	
	public CurriculumElementType update(CurriculumElementType type) {
		((CurriculumElementTypeImpl)type).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(type);
	}
	
	public void deleteCurriculumElementType(CurriculumElementTypeRef type) {
		CurriculumElementType reloadedType = dbInstance.getCurrentEntityManager()
				.getReference(CurriculumElementTypeImpl.class, type.getKey());
		dbInstance.getCurrentEntityManager().remove(reloadedType);
	}
}
