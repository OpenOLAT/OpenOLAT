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
package org.olat.modules.catalog.ui;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DefaultResultInfos;
import org.olat.core.commons.persistence.ResultInfos;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataSourceDelegate;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.catalog.CatalogFilterHandler;
import org.olat.modules.catalog.CatalogRepositoryEntry;
import org.olat.modules.catalog.CatalogRepositoryEntrySearchParams;
import org.olat.modules.catalog.CatalogRepositoryEntrySearchParams.OrderBy;
import org.olat.modules.catalog.CatalogSearchTerm;
import org.olat.modules.catalog.CatalogV2Service;
import org.olat.repository.ui.PriceMethod;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.olat.resource.accesscontrol.model.PriceMethodBundle;
import org.olat.resource.accesscontrol.ui.OpenAccessOfferController;
import org.olat.resource.accesscontrol.ui.PriceFormat;

/**
 * 
 * Initial date: 24 May 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CatalogRepositoryEntryDataSource implements FlexiTableDataSourceDelegate<CatalogRepositoryEntryRow> {
	
	public interface CatalogRepositoryEntryRowItemCreator {
		public void forgeSelectLink(CatalogRepositoryEntryRow row);
		public void forgeStartLink(CatalogRepositoryEntryRow row);
		public void forgeDetailsLink(CatalogRepositoryEntryRow row);
		public void forgeThumbnail(CatalogRepositoryEntryRow row);
		public void forgeTaxonomyLevels(CatalogRepositoryEntryRow row);
	}

	private Integer count;
	private final CatalogRepositoryEntrySearchParams searchParams;
	private final boolean withSearch;
	private final CatalogRepositoryEntryRowItemCreator rowItemCreator;
	private final Locale locale;
	private final CatalogV2Service catalogService;
	private final AccessControlModule acModule;
	
	public CatalogRepositoryEntryDataSource(CatalogRepositoryEntrySearchParams searchParams,
			boolean withSearch, CatalogRepositoryEntryRowItemCreator rowItemCreator, Locale locale) {
		this.searchParams = searchParams;
		this.withSearch = withSearch;
		this.rowItemCreator = rowItemCreator;
		this.locale = locale;
		this.catalogService = CoreSpringFactory.getImpl(CatalogV2Service.class);
		this.acModule = CoreSpringFactory.getImpl(AccessControlModule.class);
	}
	
	public void resetCount() {
		count = null;
	}

	@Override
	public int getRowCount() {
		if (count == null) {
			count = catalogService.countRepositoryEntries(searchParams);
		}
		return count;
	}

	@Override
	public List<CatalogRepositoryEntryRow> reload(List<CatalogRepositoryEntryRow> rows) {
		return Collections.emptyList();
	}

	@Override
	public ResultInfos<CatalogRepositoryEntryRow> getRows(String query, List<FlexiTableFilter> filters,
			int firstResult, int maxResults, SortKey... orderBy) {
		if (withSearch) {
			if (StringHelper.containsNonWhitespace(query)) {
				List<CatalogSearchTerm> searchTems = catalogService.getSearchTerms(query, locale);
				searchParams.setSearchTerms(searchTems);
			} else {
				searchParams.setSearchTerms(null);
			}
		}
		
		if (filters != null) {
			for(FlexiTableFilter filter:filters) {
				CatalogFilterHandler handler = catalogService.getCatalogFilterHandler(filter.getFilter());
				if (handler != null) {
					handler.enrichSearchParams(searchParams, filter);
				}
			}
		}
		
		if (orderBy != null && orderBy.length > 0 && orderBy[0] != null) {
			searchParams.setOrderBy(OrderBy.secureValueOf(orderBy[0].getKey()));
			searchParams.setOrderByAsc(orderBy[0].isAsc());
		} else {
			searchParams.setOrderBy(null);
		}
		
		List<CatalogRepositoryEntry> views = catalogService.getRepositoryEntries(searchParams, firstResult, maxResults);
		if (firstResult == 0 && views.size() < maxResults) {
			count = Integer.valueOf(views.size());
		}
		
		List<CatalogRepositoryEntryRow> rows = views.stream().map(this::toRow).collect(Collectors.toList());
		
		return new DefaultResultInfos<>(firstResult + rows.size(), -1, rows);
	}

	private CatalogRepositoryEntryRow toRow(CatalogRepositoryEntry catalogRepositoryEntry) {
		CatalogRepositoryEntryRow row = new CatalogRepositoryEntryRow(catalogRepositoryEntry);
		
		if (catalogRepositoryEntry.isPublicVisible()) {
			List<PriceMethod> accessTypes = catalogRepositoryEntry.getResourceAccess().stream()
					.flatMap(ra -> ra.getMethods().stream())
					.map(this::toPriceMethod)
					.collect(Collectors.toList());
			if (catalogRepositoryEntry.isOpenAccess()) {
				Translator translator = Util.createPackageTranslator(OpenAccessOfferController.class, locale);
				accessTypes.add(new PriceMethod(null, "o_ac_openaccess_icon", translator.translate("open.access.name")));
			}
			if (!accessTypes.isEmpty()) {
				row.setAccessTypes(accessTypes);
			}
		}
		
		rowItemCreator.forgeSelectLink(row);
		rowItemCreator.forgeStartLink(row);
		rowItemCreator.forgeDetailsLink(row);
		rowItemCreator.forgeThumbnail(row);
		rowItemCreator.forgeTaxonomyLevels(row);
		
		return row;
	}

	private PriceMethod toPriceMethod(PriceMethodBundle bundle) {
		String type = bundle.getMethod().getMethodCssClass() + "_icon";
		String price = bundle.getPrice() == null || bundle.getPrice().isEmpty() ? "" : PriceFormat.fullFormat(bundle.getPrice());
		AccessMethodHandler amh = acModule.getAccessMethodHandler(bundle.getMethod().getType());
		String displayName = amh.getMethodName(locale);
		return new PriceMethod(price, type, displayName);
	}

}
