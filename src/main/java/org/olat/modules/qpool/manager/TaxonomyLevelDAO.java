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
package org.olat.modules.qpool.manager;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.modules.qpool.TaxonomyLevel;
import org.olat.modules.qpool.model.TaxonomyLevelImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 20.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("taxonomyLevelDao")
public class TaxonomyLevelDAO {

	@Autowired
	private DB dbInstance;
	
	public TaxonomyLevel createAndPersist(TaxonomyLevel parentField, String field) {
		TaxonomyLevelImpl newStudyField = new TaxonomyLevelImpl();
		newStudyField.setCreationDate(new Date());
		newStudyField.setLastModified(new Date());
		newStudyField.setField(field);
		if(parentField != null) {
			newStudyField.setParentField(parentField);
			
			String parentPathOfKeys = parentField.getMaterializedPathKeys();
			if(parentPathOfKeys == null || "/".equals(parentPathOfKeys)) {
				parentPathOfKeys = "";
			}
			String parentPathOfNames = parentField.getMaterializedPathNames();
			if(parentPathOfNames == null || "/".equals(parentPathOfNames)) {
				parentPathOfNames = "";
			}

			newStudyField.setMaterializedPathKeys(parentPathOfKeys + "/" + parentField.getKey());
			newStudyField.setMaterializedPathNames(parentPathOfNames + "/" + parentField.getField());
		} else {
			newStudyField.setMaterializedPathKeys("/");
			newStudyField.setMaterializedPathNames("/");
		}
		dbInstance.getCurrentEntityManager().persist(newStudyField);
		return newStudyField;
	}

	public TaxonomyLevel update(String name, TaxonomyLevel field) {
		TaxonomyLevel reloadedField = loadLevelById(field.getKey());
		String path = reloadedField.getMaterializedPathNames() + "/" + reloadedField.getField();
		String newPath = reloadedField.getMaterializedPathNames() + "/" + name;

		((TaxonomyLevelImpl)reloadedField).setField(name);
		TaxonomyLevel mergedField = dbInstance.getCurrentEntityManager().merge(reloadedField);
		List<TaxonomyLevel> descendants = getDescendants(mergedField);
		
		for(TaxonomyLevel descendant:descendants) {
			String descendantPath = descendant.getMaterializedPathNames();
			if(descendantPath.indexOf(path) == 0) {
				String end = descendantPath.substring(path.length(), descendantPath.length());
				String updatedPath = newPath + end;
				((TaxonomyLevelImpl)descendant).setMaterializedPathNames(updatedPath);
			}
			dbInstance.getCurrentEntityManager().merge(descendant);
		}
		return mergedField;
	}
	
	public int countItemUsing(TaxonomyLevel field) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(item) from questionitem item where item.taxonomyLevel.key=:taxonomyLevelKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.setParameter("taxonomyLevelKey", field.getKey())
				.getSingleResult().intValue();
	}
	
	public List<TaxonomyLevel> getDescendants(TaxonomyLevel field) {
		String path = field.getMaterializedPathKeys() + "/" + field.getKey();
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadTaxonomyDescendants", TaxonomyLevel.class)
				.setParameter("path", path + "%")
				.getResultList();
	}

	
	public TaxonomyLevel loadLevelById(Long key) {
		List<TaxonomyLevel> fields = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadTaxonomyLevelByKey", TaxonomyLevel.class)
				.setParameter("key", key)
				.getResultList();
		
		if(fields.isEmpty()) {
			return null;
		}
		return fields.get(0);
	}
	
	public List<TaxonomyLevel> loadAllLevels() {
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadAllTaxonomyLevels", TaxonomyLevel.class)
				.getResultList();
	}
	
	public List<TaxonomyLevel> loadTaxonomicPath(TaxonomyLevel parent) {
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadTaxonomicPath", TaxonomyLevel.class)
				.setParameter("parentKey", parent.getKey())
				.getResultList();
	}
}
