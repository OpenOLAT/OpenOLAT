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

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Tuple;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.OLATResourceable;
import org.olat.modules.portfolio.BinderRef;
import org.olat.modules.portfolio.Category;
import org.olat.modules.portfolio.CategoryToElement;
import org.olat.modules.portfolio.PortfolioRoles;
import org.olat.modules.portfolio.SectionRef;
import org.olat.modules.portfolio.model.CategoryImpl;
import org.olat.modules.portfolio.model.CategoryLight;
import org.olat.modules.portfolio.model.CategoryStatistics;
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
	
	public List<Category> getCategories() {
		StringBuilder sb = new StringBuilder();
		sb.append("select category from pfcategory as category");
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Category.class)
			.getResultList();
	}
	
	public List<CategoryToElement> getCategorizedSectionsAndPages(BinderRef binder) {
		StringBuilder sb = new StringBuilder();
		sb.append("select rel from pfcategoryrelation as rel")
		  .append(" inner join fetch rel.category as category")
		  .append(" where exists (select binder from pfbinder as binder")
		  .append("   inner join binder.sections as section")
		  .append("   inner join section.pages as page")
		  .append("   where binder.key=:binderKey")
		  .append("   and ((rel.resId=page.key and rel.resName='Page') or (rel.resId=section.key and rel.resName='Section'))")
		  .append(" )");
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), CategoryToElement.class)
			.setParameter("binderKey", binder.getKey())
			.getResultList();
	}
	
	public List<CategoryToElement> getCategorizedSectionAndPages(SectionRef section) {
		StringBuilder sb = new StringBuilder();
		sb.append("select rel from pfcategoryrelation as rel")
		  .append(" inner join fetch rel.category as category")
		  .append(" where exists (select section from pfsection as section")
		  .append("   inner join section.pages as page")
		  .append("   where section.key=:sectionKey")
		  .append("   and ((rel.resId=page.key and rel.resName='Page') or (rel.resId=section.key and rel.resName='Section'))")
		  .append(" )");
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), CategoryToElement.class)
			.setParameter("sectionKey", section.getKey())
			.getResultList();
	}
	
	public List<CategoryToElement> getCategorizedOwnedPages(IdentityRef identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select rel from pfcategoryrelation as rel")
		  .append(" inner join fetch rel.category as category")
		  .append(" inner join pfpage as page on (rel.resId=page.key and rel.resName='Page')")
		  .append(" left join pfsection as section on (section.key = page.section.key)")
		  .append(" left join pfbinder as binder on (binder.key=section.binder.key)")
		  .append(" where exists (select pageMember from bgroupmember as pageMember")
		  .append("   inner join pageMember.identity as ident on (ident.key=:ownerKey and pageMember.role='").append(PortfolioRoles.owner.name()).append("')")
		  .append("   where pageMember.group.key=page.baseGroup.key or pageMember.group.key=binder.baseGroup.key")
		  .append(" )");
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), CategoryToElement.class)
			.setParameter("ownerKey", identity.getKey())
			.getResultList();
	}
	
	public List<CategoryStatistics> getMediaCategoriesStatistics(IdentityRef owner) {
		StringBuilder sb = new StringBuilder();
		sb.append("select category.name, count(media.key) from pfcategoryrelation as rel")
		  .append(" inner join rel.category as category")
		  .append(" inner join pfmedia as media on (rel.resId=media.key and rel.resName='Media')")
		  .append(" where media.author.key=:identityKey")
		  .append(" group by category.name");
		
		List<Object[]> objects = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Object[].class)
			.setParameter("identityKey", owner.getKey())
			.getResultList();
		
		List<CategoryStatistics> stats = new ArrayList<>(objects.size());
		for(Object[] object:objects) {
			String name = (String)object[0];
			int count = object[1] == null ? 0 : ((Number)object[1]).intValue();
			stats.add(new CategoryStatistics(name, count));
		}
		return stats;
	}
	
	public List<CategoryLight> getMediaCategories(IdentityRef owner) {
		StringBuilder sb = new StringBuilder();
		sb.append("select category.name, media.key from pfcategoryrelation as rel")
		  .append(" inner join rel.category as category")
		  .append(" inner join pfmedia as media on (rel.resId=media.key and rel.resName='Media')")
		  .append(" where media.author.key=:identityKey");
		
		List<Object[]> objects = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Object[].class)
			.setParameter("identityKey", owner.getKey())
			.getResultList();
		
		List<CategoryLight> stats = new ArrayList<>(objects.size());
		for(Object[] object:objects) {
			String name = (String)object[0];
			Long mediaKey = (Long)object[1];
			stats.add(new CategoryLight(name, mediaKey));
		}
		return stats;
	}
	
	public LinkedHashMap<Category, Long> getCategoriesAndUsage(SectionRef section) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("select category as category, count(*) as categoryCount from pfcategoryrelation as rel")
		  .append(" inner join rel.category as category")
		  .append(" where exists (select section from pfsection as section")
		  .append("   inner join section.pages as page")
		  .append("   where section.key=:sectionKey")
		  .append("   and ((rel.resId=page.key and rel.resName='Page') or (rel.resId=section.key and rel.resName='Section'))")
		  .append(" )")
		  .append(" group by category")
		  .append(" order by categoryCount desc");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Tuple.class)
				.setParameter("sectionKey", section.getKey())
				.getResultStream()
				.collect(
					Collectors.toMap(
						tuple -> ((Category) tuple.get("category")), 
						tuple -> ((Long) tuple.get("categoryCount")),
						(category1, category2) -> category1,
						LinkedHashMap::new));
	}
}