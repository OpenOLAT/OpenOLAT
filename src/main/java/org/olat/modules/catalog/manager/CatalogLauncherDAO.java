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

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.modules.catalog.CatalogLauncher;
import org.olat.modules.catalog.CatalogLauncherRef;
import org.olat.modules.catalog.CatalogLauncherSearchParams;
import org.olat.modules.catalog.model.CatalogLauncherImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 2 Jun 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CatalogLauncherDAO {
	
	@Autowired
	private DB dbInstance;

	public CatalogLauncher create(String type, String identifier) {
		CatalogLauncherImpl launcher = new CatalogLauncherImpl();
		launcher.setCreationDate(new Date());
		launcher.setLastModified(launcher.getCreationDate());
		launcher.setType(type);
		launcher.setIdentifier(identifier);
		launcher.setSortOrder(getNextSortOrder());
		launcher.setEnabled(true);
		dbInstance.getCurrentEntityManager().persist(launcher);
		return launcher;
	}
	
	public int getNextSortOrder() {
		String query = "select max(launcher.sortOrder) + 1 from cataloglauncher launcher";
		
		List<Integer> next = dbInstance.getCurrentEntityManager()
				.createQuery(query, Integer.class)
				.getResultList();
		return next != null && !next.isEmpty() && next.get(0) != null? next.get(0).intValue(): 1;
	}
	
	public CatalogLauncher save(CatalogLauncher catalogLauncher) {
		if (catalogLauncher instanceof CatalogLauncherImpl) {
			CatalogLauncherImpl impl = (CatalogLauncherImpl)catalogLauncher;
			impl.setLastModified(new Date());
			catalogLauncher = dbInstance.getCurrentEntityManager().merge(catalogLauncher);
		}
		return catalogLauncher;
	}

	public void delete(CatalogLauncherRef catalogLauncher) {
		String query = "delete from cataloglauncher launcher where launcher.key = :key";
		
		dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("key", catalogLauncher.getKey())
				.executeUpdate();
	}

	public CatalogLauncher loadByKey(CatalogLauncherRef catalogLauncher) {
		String query = "select launcher from cataloglauncher launcher where launcher.key = :key";
		
		List<CatalogLauncher> catalogLaunchers = dbInstance.getCurrentEntityManager()
				.createQuery(query, CatalogLauncher.class)
				.setParameter("key", catalogLauncher.getKey())
				.getResultList();
		
		return catalogLaunchers.isEmpty()? null: catalogLaunchers.get(0);
	}

	public CatalogLauncher loadBySortOrder(int sortOrder) {
		String query = "select launcher from cataloglauncher launcher where launcher.sortOrder = :sortOrder";
		
		List<CatalogLauncher> catalogLaunchers = dbInstance.getCurrentEntityManager()
				.createQuery(query, CatalogLauncher.class)
				.setParameter("sortOrder", sortOrder)
				.getResultList();
		
		return catalogLaunchers.isEmpty()? null: catalogLaunchers.get(0);
	}

	public List<CatalogLauncher> load(CatalogLauncherSearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select launcher");
		sb.append("  from cataloglauncher launcher");
		if (searchParams.getEnabled() != null) {
			sb.and().append("launcher.enabled = ").append(searchParams.getEnabled());
		}
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CatalogLauncher.class)
				.getResultList();
	}

}
