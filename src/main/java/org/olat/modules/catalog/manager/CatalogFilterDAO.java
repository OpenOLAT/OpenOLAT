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
package org.olat.modules.catalog.manager;

import java.util.Date;
import java.util.List;

import javax.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.util.StringHelper;
import org.olat.modules.catalog.CatalogFilter;
import org.olat.modules.catalog.CatalogFilterRef;
import org.olat.modules.catalog.CatalogFilterSearchParams;
import org.olat.modules.catalog.model.CatalogFilterImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 2 Jun 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CatalogFilterDAO {
	
	@Autowired
	private DB dbInstance;

	public CatalogFilter create(String type) {
		CatalogFilterImpl filter = new CatalogFilterImpl();
		filter.setCreationDate(new Date());
		filter.setLastModified(filter.getCreationDate());
		filter.setType(type);
		filter.setSortOrder(getNextSortOrder());
		filter.setEnabled(true);
		filter.setDefaultVisible(true);
		dbInstance.getCurrentEntityManager().persist(filter);
		return filter;
	}
	
	public int getNextSortOrder() {
		String query = "select max(filter.sortOrder) + 1 from catalogfilter filter";
		
		List<Integer> next = dbInstance.getCurrentEntityManager()
				.createQuery(query, Integer.class)
				.getResultList();
		return next != null && !next.isEmpty() && next.get(0) != null? next.get(0).intValue(): 1;
	}
	
	public CatalogFilter save(CatalogFilter catalogFilter) {
		if (catalogFilter instanceof CatalogFilterImpl) {
			CatalogFilterImpl impl = (CatalogFilterImpl)catalogFilter;
			impl.setLastModified(new Date());
			catalogFilter = dbInstance.getCurrentEntityManager().merge(catalogFilter);
		}
		return catalogFilter;
	}

	public void delete(CatalogFilterRef catalogFilter) {
		String query = "delete from catalogfilter filter where filter.key = :key";
		
		dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("key", catalogFilter.getKey())
				.executeUpdate();
	}

	public CatalogFilter loadByKey(CatalogFilterRef catalogFilter) {
		String query = "select filter from catalogfilter filter where filter.key = :key";
		
		List<CatalogFilter> catalogFilters = dbInstance.getCurrentEntityManager()
				.createQuery(query, CatalogFilter.class)
				.setParameter("key", catalogFilter.getKey())
				.getResultList();
		
		return catalogFilters.isEmpty()? null: catalogFilters.get(0);
	}

	public CatalogFilter loadNext(int sortOrder, boolean up, String type) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select filter");
		sb.append("  from catalogfilter filter");
		sb.and().append("filter.sortOrder ").append("<", ">", up).append(" :sortOrder");
		if (StringHelper.containsNonWhitespace(type)) {
			sb.and().append("filter.type = :type");
		}
		sb.orderBy().append("filter.sortOrder").appendAsc(!up);
		
		TypedQuery<CatalogFilter> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CatalogFilter.class)
				.setParameter("sortOrder", sortOrder);
		if (StringHelper.containsNonWhitespace(type)) {
			query.setParameter("type", type);
		}
		query.setMaxResults(1);
		
		List<CatalogFilter> catalogFilters = query.getResultList();
		return catalogFilters.isEmpty()? null: catalogFilters.get(0);
	}

	public List<CatalogFilter> load(CatalogFilterSearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select filter");
		sb.append("  from catalogfilter filter");
		if (searchParams.getEnabled() != null) {
			sb.and().append("filter.enabled = ").append(searchParams.getEnabled());
		}
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CatalogFilter.class)
				.getResultList();
	}

}
