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
package org.olat.repository.ui.author;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DefaultResultInfos;
import org.olat.core.commons.persistence.ResultInfos;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.license.ResourceLicense;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataSourceDelegate;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.repository.RepositoryEntryAuthorView;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.SearchAuthorRepositoryEntryViewParams;
import org.olat.repository.model.SearchAuthorRepositoryEntryViewParams.OrderBy;
import org.olat.repository.ui.PriceMethod;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.olat.resource.accesscontrol.model.OLATResourceAccess;
import org.olat.resource.accesscontrol.model.PriceMethodBundle;
import org.olat.resource.accesscontrol.ui.PriceFormat;
import org.olat.user.UserManager;

/**
 * 
 * Initial date: 28.04.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AuthoringEntryDataSource implements FlexiTableDataSourceDelegate<AuthoringEntryRow> {

	private final SearchAuthorRepositoryEntryViewParams searchParams;
	
	private final ACService acService;
	private final AccessControlModule acModule;
	private final UserManager userManager;
	private final RepositoryService repositoryService;
	private final LicenseService licenseService;
	private final AuthoringEntryDataSourceUIFactory uifactory;
	private Integer count;
	private final boolean useFilters;
	
	public AuthoringEntryDataSource(SearchAuthorRepositoryEntryViewParams searchParams,
			AuthoringEntryDataSourceUIFactory uifactory, boolean useFilters) {
		this.searchParams = searchParams;
		this.uifactory = uifactory;
		this.useFilters = useFilters;
		
		acService = CoreSpringFactory.getImpl(ACService.class);
		acModule = CoreSpringFactory.getImpl(AccessControlModule.class);
		userManager = CoreSpringFactory.getImpl(UserManager.class);
		repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
		licenseService = CoreSpringFactory.getImpl(LicenseService.class);
	}
	
	public void resetCount() {
		count = null;
	}

	@Override
	public int getRowCount() {
		if(count == null) {
			count = repositoryService.countAuthorView(searchParams);
		}
		return count.intValue();
	}

	@Override
	public List<AuthoringEntryRow> reload(List<AuthoringEntryRow> rows) {
		return Collections.emptyList();
	}

	@Override
	public final ResultInfos<AuthoringEntryRow> getRows(String query, List<FlexiTableFilter> filters,
			List<String> condQueries, int firstResult, int maxResults, SortKey... orderBy) {

		if(filters != null && !filters.isEmpty()) {
			String filter = filters.get(0).getFilter();
			if(StringHelper.containsNonWhitespace(filter)) {
				searchParams.setResourceTypes(Collections.singletonList(filter));
			} else {
				searchParams.setResourceTypes(null);
			}
		} else if(useFilters) {
			searchParams.setResourceTypes(null);
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
		
		List<RepositoryEntryAuthorView> views = repositoryService.searchAuthorView(searchParams, firstResult, maxResults);
		List<AuthoringEntryRow> rows = processViewModel(views);
		ResultInfos<AuthoringEntryRow> results = new DefaultResultInfos<>(firstResult + rows.size(), -1, rows);
		if(firstResult == 0 && views.size() < maxResults) {
			count = Integer.valueOf(views.size() );
		}
		return results;
	}

	private List<AuthoringEntryRow> processViewModel(List<RepositoryEntryAuthorView> repoEntries) {
		Set<String> newNames = new HashSet<>();
		List<OLATResource> resourcesWithAC = new ArrayList<>(repoEntries.size());
		for(RepositoryEntryAuthorView entry:repoEntries) {
			if(entry.isOfferAvailable()) {
				resourcesWithAC.add(entry.getOlatResource());
			}
			final String author = entry.getAuthor();
			if(StringHelper.containsNonWhitespace(author)) {
				newNames.add(author);
			}
		}
		
		Map<String,String> fullNames = userManager.getUserDisplayNamesByUserName(newNames);
		List<OLATResourceAccess> resourcesWithOffer = acService.filterResourceWithAC(resourcesWithAC);
		
		Collection<OLATResourceable> resources = repoEntries.stream().map(RepositoryEntryAuthorView::getOlatResource).collect(Collectors.toList());
		List<ResourceLicense> licenses = licenseService.loadLicenses(resources);

		List<AuthoringEntryRow> items = new ArrayList<>();
		for(RepositoryEntryAuthorView entry:repoEntries) {
			String fullname = fullNames.get(entry.getAuthor());
			if(fullname == null) {
				fullname = entry.getAuthor();
			}
			AuthoringEntryRow row = new AuthoringEntryRow(entry, fullname);
			// bookmark
			row.setMarked(entry.isMarked());

			// access control
			List<PriceMethod> types = new ArrayList<>();
			if (!entry.isAllUsers() && !entry.isGuests()) {//TODO repo access
				// members only always show lock icon
				types.add(new PriceMethod("", "o_ac_membersonly_icon", uifactory.getTranslator().translate("cif.access.membersonly.short")));
			} else {
				// collect access control method icons
				OLATResource resource = entry.getOlatResource();
				for(OLATResourceAccess resourceAccess:resourcesWithOffer) {
					if(resource.getKey().equals(resourceAccess.getResource().getKey())) {
						for(PriceMethodBundle bundle:resourceAccess.getMethods()) {
							String type = (bundle.getMethod().getMethodCssClass() + "_icon").intern();
							String price = bundle.getPrice() == null || bundle.getPrice().isEmpty() ? "" : PriceFormat.fullFormat(bundle.getPrice());
							AccessMethodHandler amh = acModule.getAccessMethodHandler(bundle.getMethod().getType());
							String displayName = amh.getMethodName(uifactory.getTranslator().getLocale());
							types.add(new PriceMethod(price, type, displayName));
						}
					}
				}
			}
			
			if(!types.isEmpty()) {
				row.setAccessTypes(types);
			}
			
			// license
			for (ResourceLicense license: licenses) {
				OLATResource resource = entry.getOlatResource();
				if (license.getResId().equals(resource.getResourceableId()) && license.getResName().equals(resource.getResourceableTypeName())) {
					row.setLicense(license);
				}
			}
			
			uifactory.forgeLinks(row);
			
			items.add(row);
		}
		return items;
	}
}
