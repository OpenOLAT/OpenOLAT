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
package org.olat.repository.ui.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DefaultResultInfos;
import org.olat.core.commons.persistence.ResultInfos;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataSourceDelegate;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.repository.RepositoryEntryMyView;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.SearchMyRepositoryEntryViewParams;
import org.olat.repository.model.SearchMyRepositoryEntryViewParams.Filter;
import org.olat.repository.model.SearchMyRepositoryEntryViewParams.OrderBy;
import org.olat.repository.ui.PriceMethod;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.olat.resource.accesscontrol.model.OLATResourceAccess;
import org.olat.resource.accesscontrol.model.PriceMethodBundle;
import org.olat.resource.accesscontrol.ui.PriceFormat;

/**
 * 
 * Initial date: 28.01.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DefaultRepositoryEntryDataSource implements FlexiTableDataSourceDelegate<RepositoryEntryRow> {

	private final RepositoryEntryRowsFactory repositoryEntryRowsFactory;
	private final SearchMyRepositoryEntryViewParams searchParams;

	private final ACService acService;
	private final AccessControlModule acModule;
	private final RepositoryService repositoryService;
	
	private Integer count;
	
	public DefaultRepositoryEntryDataSource(SearchMyRepositoryEntryViewParams searchParams,
											RepositoryEntryRowsFactory repositoryEntryRowsFactory) {
		this.repositoryEntryRowsFactory = repositoryEntryRowsFactory;
		this.searchParams = searchParams;

		acService = CoreSpringFactory.getImpl(ACService.class);
		acModule = CoreSpringFactory.getImpl(AccessControlModule.class);
		repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
	}
	
	public void setFilters(List<Filter> filters) {
		searchParams.setFilters(filters);
		count = null;
	}
	
	public void setOrderBy(OrderBy orderBy) {
		searchParams.setOrderBy(orderBy);
	}
	
	public void resetCount() {
		count = null;
	}

	@Override
	public int getRowCount() {
		if(count == null) {
			count = repositoryService.countMyView(searchParams);
		}
		return count.intValue();
	}

	@Override
	public List<RepositoryEntryRow> reload(List<RepositoryEntryRow> rows) {
		return Collections.emptyList();
	}

	@Override
	public final ResultInfos<RepositoryEntryRow> getRows(String query, List<FlexiTableFilter> filters, 
			List<String> condQueries, int firstResult, int maxResults, SortKey... orderBy) {
		
		if(filters != null && !filters.isEmpty() && filters.get(0) != null) {
			String filter = filters.get(0).getFilter();
			if(StringHelper.containsNonWhitespace(filter)) {
				searchParams.setFilters(Collections.singletonList(Filter.valueOf(filter)));
			} else {
				searchParams.setFilters(null);
			}
		} else {
			searchParams.setFilters(null);
		}
		
		if(orderBy != null && orderBy.length > 0 && orderBy[0] != null) {
			OrderBy o = OrderBy.valueOf(orderBy[0].getKey());
			searchParams.setOrderBy(o);
			searchParams.setOrderByAsc(orderBy[0].isAsc());
		}

		if(StringHelper.containsNonWhitespace(query)) {
			searchParams.setIdRefsAndTitle(query);
		} else {
			searchParams.setIdRefsAndTitle(null);
		}
		
		List<RepositoryEntryMyView> views = repositoryService.searchMyView(searchParams, firstResult, maxResults);
		List<RepositoryEntryRow> rows = processViewModel(views);
		ResultInfos<RepositoryEntryRow> results = new DefaultResultInfos<>(firstResult + rows.size(), -1, rows);
		if(firstResult == 0 && views.size() < maxResults) {
			count = Integer.valueOf(views.size());
		}
		return results;
	}

	private List<RepositoryEntryRow> processViewModel(List<RepositoryEntryMyView> repoEntries) {
		List<Long> repoKeys = new ArrayList<>(repoEntries.size());
		List<OLATResource> resourcesWithAC = new ArrayList<>(repoEntries.size());
		for(RepositoryEntryMyView entry:repoEntries) {
			if (entry.getKey() != null) {
				repoKeys.add(entry.getKey());
			}
			if(entry.isValidOfferAvailable()) {
				resourcesWithAC.add(entry.getOlatResource());
			}
		}
		List<OLATResourceAccess> resourcesWithOffer = acService.filterResourceWithAC(resourcesWithAC);
		repositoryService.filterMembership(searchParams.getIdentity(), repoKeys);

		/*
<<<<<<< HEAD
		LinkedHashMap<RepositoryEntryMyView, RepositoryEntryRow> mapOfRepositoryEntryViewsAndRepositoryEntryRows =
				repositoryEntryRowsFactory.create(repoEntries);

		List<RepositoryEntryRow> items = new ArrayList<>();
		for (Map.Entry<RepositoryEntryMyView, RepositoryEntryRow> mapEntry : mapOfRepositoryEntryViewsAndRepositoryEntryRows.entrySet()) {

			RepositoryEntryMyView entry = mapEntry.getKey();
			RepositoryEntryRow row = mapEntry.getValue();

			List<PriceMethod> types = new ArrayList<PriceMethod>();
			if (entry.isMembersOnly()) {
				// members only always show lock icon
				types.add(new PriceMethod("", "o_ac_membersonly_icon",
						repositoryEntryRowsFactory.getUiFactory()
								.getTranslator()
								.translate("cif.access.membersonly.short")));
			} else {
======= */
		List<RepositoryEntryRow> items = new ArrayList<>();
		for(RepositoryEntryMyView entry:repoEntries) {
			RepositoryEntryRow row = new RepositoryEntryRow(entry);

			VFSLeaf image = repositoryManager.getImage(entry.getKey(), entry.getOlatResource());
			if(image != null) {
				row.setThumbnailRelPath(uifactory.getMapperThumbnailUrl() + "/" + image.getName());
			}

			List<PriceMethod> types = new ArrayList<>(3);
			if(entry.isBookable()) {
// >>>>>>> OpenOLAT_14.0.2
				// collect access control method icons
				OLATResource resource = entry.getOlatResource();
				for(OLATResourceAccess resourceAccess:resourcesWithOffer) {
					if(resource.getKey().equals(resourceAccess.getResource().getKey())) {
						for(PriceMethodBundle bundle:resourceAccess.getMethods()) {
							String type = (bundle.getMethod().getMethodCssClass() + "_icon").intern();
							String price = bundle.getPrice() == null || bundle.getPrice().isEmpty() ? "" : PriceFormat.fullFormat(bundle.getPrice());
							AccessMethodHandler amh = acModule.getAccessMethodHandler(bundle.getMethod().getType());
							String displayName = amh.getMethodName(
									repositoryEntryRowsFactory.getUiFactory()
											.getTranslator().getLocale());
							types.add(new PriceMethod(price, type, displayName));
						}
					}
				}
			} else if (!entry.isAllUsers() && !entry.isGuests()) {
				// members only always show lock icon
				types.add(new PriceMethod("", "o_ac_membersonly_icon", uifactory.getTranslator().translate("cif.access.membersonly.short")));
			} 
			
			row.setMember(repoKeys.contains(entry.getKey()));

			if(!types.isEmpty()) {
				row.setAccessTypes(types);
			}

			items.add(row);
		}
		return items;
	}
}
