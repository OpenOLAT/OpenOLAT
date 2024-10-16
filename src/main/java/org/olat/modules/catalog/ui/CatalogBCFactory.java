/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.catalog.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.catalog.WebCatalogDispatcher;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 15 Oct 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CatalogBCFactory {
	
	private static final String ORES_TYPE_CATALOG = "Catalog";
	private static final ContextEntry CE_CATALOG = BusinessControlFactory.getInstance()
			.createContextEntry(OresHelper.createOLATResourceableType(ORES_TYPE_CATALOG));
	
	private static final String ORES_TYPE_SEARCH = "Search";
	private static final OLATResourceable ORES_SEARCH = OresHelper.createOLATResourceableType(ORES_TYPE_SEARCH);
	private static final ContextEntry CE_SEARCH = BusinessControlFactory.getInstance().createContextEntry(ORES_SEARCH);
	private static final List<ContextEntry> CES_SEARCH = List.of(CE_CATALOG, CE_SEARCH);
	private static final List<ContextEntry> CES_WEB_SEARCH = List.of(CE_SEARCH);
	
	private static final String ORES_TYPE_INFOS = "Infos";
	private static final String ORES_TYPE_TAXONOMY = "Microsite";
	public static final String ORES_TYPE_TAXONOMY_ADMIN = "TaxonomyAdmin";
	
	private static final CatalogBCFactory CATALOG_FACTORY = new CatalogBCFactory(false);
	private static final CatalogBCFactory WEB_CATALOG_FACTORY = new CatalogBCFactory(true);
	
	private final boolean webCatalog;
	
	public static CatalogBCFactory get(boolean webCatalog) {
		return webCatalog? WEB_CATALOG_FACTORY: CATALOG_FACTORY;
	}
	
	private CatalogBCFactory(boolean webCatalog) {
		this.webCatalog = webCatalog;
	}
	
	private List<ContextEntry> createBaseCes() {
		List<ContextEntry> baseCes = new ArrayList<>(3);
		if (!webCatalog) {
			baseCes.add(CE_CATALOG);
		}
		return baseCes;
	}
	
	public String getSearchUrl() {
		return getUrl(webCatalog? CES_WEB_SEARCH: CES_SEARCH);
	}
	
	public static OLATResourceable createSearchOres() {
		return ORES_SEARCH;
	}
	
	public static boolean isSearchType(OLATResourceable ores) {
		return ORES_TYPE_SEARCH.equalsIgnoreCase(ores.getResourceableTypeName());
	}
	
	public String getInfosUrl(RepositoryEntryRef repositoryEntry) {
		List<ContextEntry> ces = createInfosCes(repositoryEntry);
		return getUrl(ces);
	}
	
	private List<ContextEntry> createInfosCes(RepositoryEntryRef repositoryEntry) {
		List<ContextEntry> ces = createBaseCes();
		ces.add(CE_SEARCH);
		ces.add(createInfosCe(repositoryEntry));
		return ces;
	}

	private ContextEntry createInfosCe(RepositoryEntryRef repositoryEntry) {
		return BusinessControlFactory.getInstance().createContextEntry(createInfosOres(repositoryEntry));
	}

	public static OLATResourceable createInfosOres(RepositoryEntryRef repositoryEntry) {
		return OresHelper.createOLATResourceableInstance(ORES_TYPE_INFOS, repositoryEntry.getKey());
	}
	
	public static boolean isInfosType(OLATResourceable ores) {
		return ORES_TYPE_INFOS.equalsIgnoreCase(ores.getResourceableTypeName());
	}
	
	public String getTaxonomyLevelUrl(TaxonomyLevelRef taxonomyLevel) {
		List<ContextEntry> ces = createTaxonomyLevelCes(taxonomyLevel);
		return getUrl(ces);
	}

	private List<ContextEntry> createTaxonomyLevelCes(TaxonomyLevelRef taxonomyLevel) {
		List<ContextEntry> ces = createBaseCes();
		ces.add(createTaxonomyLevelCe(taxonomyLevel));
		return ces;
	}

	private ContextEntry createTaxonomyLevelCe(TaxonomyLevelRef taxonomyLevel) {
		return BusinessControlFactory.getInstance().createContextEntry(createTaxonomyLevelOres(taxonomyLevel));
	}

	public static OLATResourceable createTaxonomyLevelOres(TaxonomyLevelRef taxonomyLevel) {
		return OresHelper.createOLATResourceableInstance(ORES_TYPE_TAXONOMY, taxonomyLevel.getKey());
	}
	
	public static boolean isTaxonomyLevelType(OLATResourceable ores) {
		return ORES_TYPE_TAXONOMY.equalsIgnoreCase(ores.getResourceableTypeName());
	}
	
	private String getUrl(List<ContextEntry> ceList) {
		if (webCatalog) {
			return getAsWebCatalogURIString(ceList);
		}
		return BusinessControlFactory.getInstance().getAsURIString(ceList, true);
	}
	
	private String getAsWebCatalogURIString(List<ContextEntry> ceList) {
		StringBuilder retVal = WebCatalogDispatcher.getBaseUrl();
		
		if (ceList == null || ceList.isEmpty()) {
			return retVal.toString();
		}
		return BusinessControlFactory.getInstance().appendToURIString(retVal, ceList, true);
	}

}
