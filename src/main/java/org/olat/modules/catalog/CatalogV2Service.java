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
package org.olat.modules.catalog;

import java.util.List;

/**
 * 
 * Initial date: 20 May 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface CatalogV2Service {

	public Integer countRepositoryEntries(CatalogRepositoryEntrySearchParams searchParams);

	public List<CatalogRepositoryEntry> getRepositoryEntries(CatalogRepositoryEntrySearchParams searchParams, int firstResult, int maxResults);
	
	public List<CatalogLauncherHandler> getCatalogLauncherHandlers();

	public CatalogLauncherHandler getCatalogLauncherHandler(String type);

	public String createLauncherIdentifier();

	public CatalogLauncher createCatalogLauncher(String type, String identifier);

	public CatalogLauncher update(CatalogLauncher catalogLauncher);

	public void doMove(CatalogLauncherRef catalogLauncher, boolean up);

	public void deleteCatalogLauncher(CatalogLauncherRef catalogLauncher);

	public CatalogLauncher getCatalogLauncher(CatalogLauncherRef catalogLauncher);

	public List<CatalogLauncher> getCatalogLaunchers(CatalogLauncherSearchParams searchParams);
	
	public List<CatalogFilterHandler> getCatalogFilterHandlers();

	public CatalogFilterHandler getCatalogFilterHandler(String type);

	public CatalogFilter createCatalogFilter(String type);

	public CatalogFilter update(CatalogFilter catalogFilter);

	public void doMove(CatalogFilterRef catalogFilter, boolean up);

	public void deleteCatalogFilter(CatalogFilterRef catalogFilter);

	public CatalogFilter getCatalogFilter(CatalogFilterRef catalogFilter);

	public List<CatalogFilter> getCatalogFilters(CatalogFilterSearchParams searchParams);

}
