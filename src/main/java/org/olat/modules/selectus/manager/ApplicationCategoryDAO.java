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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationCategory;
import org.olat.modules.selectus.model.ApplicationRef;
import org.olat.modules.selectus.model.Category;
import org.olat.modules.selectus.model.PositionRef;
import org.olat.modules.selectus.model.category.ApplicationCategoryImpl;
import org.olat.modules.selectus.model.category.ApplicationCategoryInfos;

/**
 * 
 * Initial date: 15 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ApplicationCategoryDAO {
	
	@Autowired
	private DB dbInstance;
	
	public ApplicationCategory createApplicationCategory(Application application, Category category, boolean administrative) {
		ApplicationCategoryImpl appCat = new ApplicationCategoryImpl();
		appCat.setCreationDate(new Date());
		appCat.setAdministrative(administrative);
		appCat.setApplication(application);
		appCat.setCategory(category);
		dbInstance.getCurrentEntityManager().persist(appCat);
		return appCat;
	}
	
	public List<Category> getCategories(ApplicationRef application, boolean systemCategories, boolean positionCategories) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select cat from rapplicationcategory as appCat")
		  .append(" inner join appCat.category as cat")
		  .append(" where appCat.application.key=:applicationKey");
		if(systemCategories && !positionCategories) {
			sb.append(" and cat.position.key is null");
		} else if(!systemCategories && positionCategories) {
			sb.append(" and cat.position.key is not null");
		}
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Category.class)
				.setParameter("applicationKey", application.getKey())
				.getResultList();
	}
	
	public int countApplications(Category category) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select count(distinct appCat.application.key) from rapplicationcategory as appCat")
		  .append(" where appCat.category.key=:categoryKey");
		List<Long> counts = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("categoryKey", category.getKey())
				.getResultList();
		return counts == null || counts.isEmpty() ? 0 : counts.get(0).intValue();
	}
	
	public List<Category> getCategories(PositionRef position) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select distinct cat from rapplicationcategory as appCat")
		  .append(" inner join appCat.category as cat")
		  .append(" inner join appCat.application as app")
		  .append(" where app.position.key=:positionKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Category.class)
				.setParameter("positionKey", position.getKey())
				.getResultList();
	}
	
	public List<Category> getCategories(List<ApplicationRef> applications) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select distinct cat from rapplicationcategory as appCat")
		  .append(" inner join appCat.category as cat")
		  .append(" where appCat.application.key in (:applicationKeys)");
		
		List<Long> applicationKeys = applications.stream().map(ApplicationRef::getKey).collect(Collectors.toList());
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Category.class)
				.setParameter("applicationKeys", applicationKeys)
				.getResultList();
	}
	
	public List<ApplicationCategory> getApplicationCategories(ApplicationRef application,
			boolean systemCategories, boolean positionCategories) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select appCat from rapplicationcategory as appCat")
		  .append(" inner join fetch appCat.category as cat")
		  .append(" where appCat.application.key=:applicationKey");
		if(systemCategories && !positionCategories) {
			sb.append(" and cat.position.key is null");
		} else if(!systemCategories && positionCategories) {
			sb.append(" and cat.position.key is not null");
		}
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ApplicationCategory.class)
				.setParameter("applicationKey", application.getKey())
				.getResultList();
	}
	
	public List<ApplicationCategory> getApplicationCategories(PositionRef position,
			boolean systemCategories, boolean positionCategories) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select appCat from rapplicationcategory as appCat")
		  .append(" inner join appCat.category as cat")
		  .append(" inner join appCat.application as app")
		  .append(" where app.position.key=:positionKey");
		if(systemCategories && !positionCategories) {
			sb.append(" and cat.position.key is null");
		} else if(!systemCategories && positionCategories) {
			sb.append(" and cat.position.key is not null");
		}
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ApplicationCategory.class)
				.setParameter("positionKey", position.getKey())
				.getResultList();
	}
	
	public List<ApplicationCategoryInfos> getApplicationCategoriesInfos(PositionRef position, List<ApplicationRef> applications,
			boolean systemCategories, boolean positionCategories, boolean administrativeIncluded) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select appCat.application.key, appCat.administrative, cat from rapplicationcategory as appCat")
		  .append(" inner join appCat.category as cat")
		  .append(" inner join appCat.application as app")
		  .append(" where app.position.key=:positionKey");
		if(applications != null && !applications.isEmpty()) {
			sb.append(" and app.key in (:applicationKeys)");
		}
		if(!administrativeIncluded) {
			sb.append(" and appCat.administrative=false");
		}
		if(systemCategories && !positionCategories) {
			sb.append(" and cat.position.key is null");
		} else if(!systemCategories && positionCategories) {
			sb.append(" and cat.position.key is not null");
		}
		
		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("positionKey", position.getKey());
		if(applications != null && !applications.isEmpty()) {
			List<Long> applicationKeys = applications.stream()
					.map(ApplicationRef::getKey)
					.collect(Collectors.toList());
			query.setParameter("applicationKeys", applicationKeys);
		}

		List<Object[]> rawObjects =	query.getResultList();
		List<ApplicationCategoryInfos> infos = new ArrayList<>(rawObjects.size());
		for(Object[] rawObject:rawObjects) {
			Long applicationKey = (Long)rawObject[0];
			Boolean administrativeObj = (Boolean)rawObject[1];
			Category category = (Category)rawObject[2];
			boolean administrative = administrativeObj != null && administrativeObj.booleanValue();
			infos.add(new ApplicationCategoryInfos(applicationKey, category, administrative));
		}
		return infos;
	}
	
	public boolean hasApplicationCategories(PositionRef position,
			boolean systemCategories, boolean positionCategories) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select appCat.application.key from rapplicationcategory as appCat")
		  .append(" inner join appCat.category as cat")
		  .append(" inner join appCat.application as app")
		  .append(" where app.position.key=:positionKey");
		if(systemCategories && !positionCategories) {
			sb.append(" and cat.position.key is null");
		} else if(!systemCategories && positionCategories) {
			sb.append(" and cat.position.key is not null");
		}
		
		List<Long> keys = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("positionKey", position.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return keys != null && !keys.isEmpty() && keys.get(0) != null;
	}
	
	public void replaceCategory(Category originalCategory, Category replacementCategory) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("update rapplicationcategory appCat set appCat.category.key=:replacementCategoryKey")
		  .append(" where appCat.category.key=:originalCategoryKey");
		
		dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString())
			.setParameter("originalCategoryKey", originalCategory.getKey())
			.setParameter("replacementCategoryKey", replacementCategory.getKey())
			.executeUpdate();
	}
	
	public void delete(Category category) {
		String query = "delete from rapplicationcategory where category.key=:categoryKey";
		dbInstance.getCurrentEntityManager()
			.createQuery(query)
			.setParameter("categoryKey", category.getKey())
			.executeUpdate();
	}
	
	public void delete(ApplicationRef app) {
		String query = "delete from rapplicationcategory where application.key=:appKey";
		dbInstance.getCurrentEntityManager()
			.createQuery(query)
			.setParameter("appKey", app.getKey())
			.executeUpdate();
	}
	
	public void delete(ApplicationCategory appCategory) {
		dbInstance.getCurrentEntityManager().remove(appCategory);
	}
}
