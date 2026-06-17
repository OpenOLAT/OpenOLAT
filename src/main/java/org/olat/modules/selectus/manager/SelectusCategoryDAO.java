/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.manager;

import java.util.Date;
import java.util.List;

import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.olat.modules.selectus.model.Category;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionRef;
import org.olat.modules.selectus.model.category.CategoryImpl;

/**
 * 
 * Initial date: 15 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class SelectusCategoryDAO {
	
	@Autowired
	private DB dbInstance;
	
	public Category createCategory(String name, String color, Position position) {
		CategoryImpl cat = new CategoryImpl();
		cat.setCreationDate(new Date());
		cat.setLastModified(cat.getCreationDate());
		cat.setName(name);
		cat.setColor(color);
		cat.setPosition(position);
		dbInstance.getCurrentEntityManager().persist(cat);
		return cat;
	}
	
	public Category updateCategory(Category category) {
		category.setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(category);
	}
	
	public List<Category> getCategoriesByName(String name, Position position, boolean systemTag, boolean positionTag) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select cat from rcategory as cat")
		  .append(" left join fetch cat.position pos")
		  .append(" where ( pos.key is null");
		if(position != null) {
			sb.append(" or pos.key=:positionKey");
		}
		sb.append(") and cat.name=:name");
		if(systemTag && !positionTag) {
			sb.append(" and cat.position.key is null");
		} else if(!systemTag && positionTag) {
			sb.append(" and cat.position.key is not null");
		}
	
		TypedQuery<Category> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Category.class)
				.setParameter("name", name);
		if(position != null) {
			query.setParameter("positionKey", position.getKey());
		}
		return query.getResultList();
	}
	
	public List<Category> getSystemCategories() {
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("allSystemCategories", Category.class)
				.getResultList();
	}
	
	public Category loadCategory(Long categoryKey) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select cat from rcategory as cat")
		  .append(" left join fetch cat.position as pos")
		  .append(" where cat.key=:categoryKey");

		List<Category> categories = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Category.class)
				.setParameter("categoryKey", categoryKey)
				.getResultList();
		return categories != null && !categories.isEmpty() ? categories.get(0) : null;
	}
	
	public List<Category> getPositionCategories(PositionRef position) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select cat from rcategory as cat")
		  .append(" inner join fetch cat.position as pos")
		  .append(" where pos.key=:positionKey");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Category.class)
				.setParameter("positionKey", position.getKey())
				.getResultList();
	}
	
	public void delete(PositionRef position) {
		String query = "delete from rcategory where position.key=:positionKey";
		dbInstance.getCurrentEntityManager()
			.createQuery(query)
			.setParameter("positionKey", position.getKey())
			.executeUpdate();
	}
	
	public void delete(Category category) {
		Category ref = dbInstance.getCurrentEntityManager()
			.getReference(CategoryImpl.class, category.getKey());
		dbInstance.getCurrentEntityManager().remove(ref);
	}

}
