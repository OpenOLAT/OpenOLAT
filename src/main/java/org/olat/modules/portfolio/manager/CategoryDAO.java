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
package org.olat.modules.portfolio.manager;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.id.OLATResourceable;
import org.olat.modules.portfolio.Category;
import org.olat.modules.portfolio.model.CategoryImpl;
import org.olat.modules.portfolio.model.CategoryToElementImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 10.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CategoryDAO {
	
	@Autowired
	private DB dbInstance;
	
	public Category createAndPersistCategory(String name) {
		CategoryImpl category = new CategoryImpl();
		category.setCreationDate(new Date());
		category.setName(name);
		dbInstance.getCurrentEntityManager().persist(category);
		return category;
	}
	
	public void appendRelation(OLATResourceable ores, Category category) {
		CategoryToElementImpl relation = new CategoryToElementImpl();
		relation.setCreationDate(new Date());
		relation.setCategory(category);
		relation.setResId(ores.getResourceableId());
		relation.setResName(ores.getResourceableTypeName());
		dbInstance.getCurrentEntityManager().persist(relation);
	}
	
	public void removeRelation(OLATResourceable ores, Category category) {
		StringBuilder sb = new StringBuilder();
		sb.append("select rel from pfcategoryrelation as rel")
		  .append(" where rel.resId=:resId and rel.resName=:resName and rel.category.key=:categoryKey");
		
		List<CategoryToElementImpl> relationToDelete = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), CategoryToElementImpl.class)
			.setParameter("resName", ores.getResourceableTypeName())
			.setParameter("resId", ores.getResourceableId())
			.setParameter("categoryKey", category.getKey())
			.getResultList();
		for(CategoryToElementImpl relation:relationToDelete) {
			dbInstance.getCurrentEntityManager().remove(relation);
		}
	}
	
	public List<Category> getCategories(OLATResourceable ores) {
		StringBuilder sb = new StringBuilder();
		sb.append("select category from pfcategoryrelation as rel")
		  .append(" inner join rel.category as category")
		  .append(" where rel.resId=:resId and rel.resName=:resName");
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Category.class)
			.setParameter("resName", ores.getResourceableTypeName())
			.setParameter("resId", ores.getResourceableId())
			.getResultList();
	}

}
