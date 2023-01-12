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
package org.olat.course.style.manager;

import java.util.Date;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.util.StringHelper;
import org.olat.course.style.ColorCategory;
import org.olat.course.style.ColorCategoryRef;
import org.olat.course.style.ColorCategorySearchParams;
import org.olat.course.style.model.ColorCategoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 23 Jun 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ColorCategoryDAO {

	@Autowired
	private DB dbInstance;
	
	@PostConstruct
	public void init() {
		createPredefined(ColorCategory.IDENTIFIER_NO_COLOR, ColorCategory.Type.technical, -20, ColorCategory.CSS_NO_COLOR);
		createPredefined(ColorCategory.IDENTIFIER_INHERITED, ColorCategory.Type.technical, -10, "o_colcat_inherited");
		createPredefined(ColorCategory.IDENTIFIER_COURSE, ColorCategory.Type.technical, -5, "o_colcat_course");
		createPredefined("dark.blue", ColorCategory.Type.predefined, 1, "o_colcat_dark_blue");
		createPredefined("light.blue", ColorCategory.Type.predefined, 2, "o_colcat_light_blue");
		createPredefined("purple", ColorCategory.Type.predefined, 3, "o_colcat_purple");
		createPredefined("red", ColorCategory.Type.predefined, 4, "o_colcat_red");
		createPredefined("orange", ColorCategory.Type.predefined, 5, "o_colcat_orange");
		createPredefined("yellow", ColorCategory.Type.predefined, 6, "o_colcat_yellow");
		createPredefined("light.green", ColorCategory.Type.predefined, 7, "o_colcat_light_green");
		createPredefined("dark.green", ColorCategory.Type.predefined, 8, "o_colcat_dark_green");
	}

	public ColorCategory create(String identifier) {
		return create(identifier, ColorCategory.Type.custom, getNextSortOrder(), true, null);
	}
	
	ColorCategory createPredefined(String identifier, ColorCategory.Type type, int sortOrder, String cssClass) {
		ColorCategory colorCategory = loadByIdentifier(identifier);
		return colorCategory != null? colorCategory: create(identifier, type, sortOrder, true, cssClass);
	}

	private ColorCategory create(String identifier, ColorCategory.Type type, int sortOrder, boolean enabled, String cssClass) {
		ColorCategoryImpl colorCategory = new ColorCategoryImpl();
		colorCategory.setCreationDate(new Date());
		colorCategory.setLastModified(colorCategory.getCreationDate());
		colorCategory.setIdentifier(identifier);
		colorCategory.setType(type);
		colorCategory.setSortOrder(sortOrder);
		colorCategory.setEnabled(enabled);
		colorCategory.setCssClass(cssClass);
		dbInstance.getCurrentEntityManager().persist(colorCategory);
		return colorCategory;
	}

	public ColorCategory save(ColorCategory colorCategory) {
		if (colorCategory instanceof ColorCategoryImpl) {
			ColorCategoryImpl impl = (ColorCategoryImpl)colorCategory;
			impl.setLastModified(new Date());
			colorCategory = dbInstance.getCurrentEntityManager().merge(colorCategory);
		}
		return colorCategory;
	}

	public ColorCategory loadByKey(ColorCategoryRef colorCategoryRef) {
		if (colorCategoryRef == null) return null;
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("select colorCategory");
		sb.append("  from coursecolorcategory colorCategory");
		sb.and().append("colorCategory.key = :key");

		List<ColorCategory> colorCategorys = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ColorCategory.class)
				.setParameter("key", colorCategoryRef.getKey())
				.getResultList();
		return colorCategorys.isEmpty()? null: colorCategorys.get(0);
	}
	
	public ColorCategory loadByIdentifier(String identifier) {
		if (!StringHelper.containsNonWhitespace(identifier)) return null;
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("select colorCategory");
		sb.append("  from coursecolorcategory colorCategory");
		sb.and().append("colorCategory.identifier = :identifier");

		List<ColorCategory> colorCategorys = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ColorCategory.class)
				.setParameter("identifier", identifier)
				.setHint("org.hibernate.cacheable", Boolean.TRUE)
				.getResultList();
		return colorCategorys.isEmpty()? null: colorCategorys.get(0);
	}

	public ColorCategory loadBySortOrder(int sortOrder) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select colorCategory");
		sb.append("  from coursecolorcategory colorCategory");
		sb.and().append("colorCategory.sortOrder = :sortOrder");

		List<ColorCategory> colorCategorys = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ColorCategory.class)
				.setParameter("sortOrder", sortOrder)
				.getResultList();
		return colorCategorys.isEmpty()? null: colorCategorys.get(0);
	}

	public List<ColorCategory> load(ColorCategorySearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select colorCategory");
		sb.append("  from coursecolorcategory colorCategory");
		if (searchParams.getEnabled() != null) {
			sb.and().append("colorCategory.enabled = :enabled");
		}
		if (searchParams.getTypes() != null) {
			sb.and().append("colorCategory.type in :types");
		}
		if (searchParams.getExcludedIdentifiers() != null) {
			sb.and().append("colorCategory.identifier not in :excludedIdentifiers");
		}
		
		TypedQuery<ColorCategory> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ColorCategory.class);
		if (searchParams.getEnabled() != null) {
			query.setParameter("enabled", searchParams.getEnabled());
		}
		if (searchParams.getTypes() != null) {
			query.setParameter("types", searchParams.getTypes());
		}
		if (searchParams.getExcludedIdentifiers() != null) {
			query.setParameter("excludedIdentifiers", searchParams.getExcludedIdentifiers());
		}
		
		return query.getResultList();
	}
	
	public int getNextSortOrder() {
		String query = "select max(colorCategory.sortOrder) + 1 from coursecolorcategory colorCategory";
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, Integer.class)
				.getSingleResult().intValue();
	}

	public void delete(ColorCategory colorCategory) {
		String query = "delete from coursecolorcategory colorCategory where colorCategory.key = :key";
		
		dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("key", colorCategory.getKey())
				.executeUpdate();
	}

}
