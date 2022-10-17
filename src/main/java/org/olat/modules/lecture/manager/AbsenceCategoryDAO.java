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
package org.olat.modules.lecture.manager;

import java.util.Date;
import java.util.List;

import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.modules.lecture.AbsenceCategory;
import org.olat.modules.lecture.model.AbsenceCategoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 22 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AbsenceCategoryDAO {
	
	@Autowired
	private DB dbInstance;
	
	
	public AbsenceCategory createAbsenceCategory(String title, String description, boolean enabled) {
		AbsenceCategoryImpl category = new AbsenceCategoryImpl();
		category.setCreationDate(new Date());
		category.setLastModified(category.getCreationDate());
		category.setEnabled(enabled);
		category.setTitle(title);
		category.setDescription(description);
		dbInstance.getCurrentEntityManager().persist(category);
		return category;
	}
	
	public AbsenceCategory updateAbsenceCategory(AbsenceCategory category) {
		((AbsenceCategoryImpl)category).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(category);
	}
	
	public List<AbsenceCategory> getAbsencesCategories(Boolean enabled) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("select cat from absencecategory as cat");
		if(enabled != null) {
			sb.append(" where cat.enabled=:enabled");
		}
		TypedQuery<AbsenceCategory> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AbsenceCategory.class);
		if(enabled != null) {
			query.setParameter("enabled", enabled);
		}
		return query.getResultList();
	}
	
	public AbsenceCategory getAbsenceCategory(Long key) {
		List<AbsenceCategory> categories = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadAbsencesCategoryByKey", AbsenceCategory.class)
				.setParameter("key", key)
				.getResultList();
		return categories == null || categories.isEmpty() ? null : categories.get(0);
	}
	
	public boolean isAbsenceCategoryInUse(AbsenceCategory category) {
		StringBuilder query = new StringBuilder(256);
		query.append("select cat.key from lectureblockrollcall rollcall")
		     .append(" inner join rollcall.absenceCategory as cat")
		     .append(" where cat.key=:key");
		List<Long> categories = dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), Long.class)
				.setParameter("key", category.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return categories != null && !categories.isEmpty()
				&& categories.get(0) != null && categories.get(0).longValue() > 0;
	}
	
	public void deleteAbsenceCategory(AbsenceCategory category) {
		AbsenceCategory reloadedCategory = dbInstance.getCurrentEntityManager()
				.getReference(AbsenceCategoryImpl.class, category.getKey());
		dbInstance.getCurrentEntityManager().remove(reloadedCategory);
		
	}

}
