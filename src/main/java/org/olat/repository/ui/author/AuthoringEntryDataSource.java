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
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.model.OrganisationRefImpl;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DefaultResultInfos;
import org.olat.core.commons.persistence.ResultInfos;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.ResourceLicense;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataSourceDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableSingleSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableTextFilter;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.OrganisationRef;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.Resourceable;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.modules.taxonomy.model.TaxonomyLevelRefImpl;
import org.olat.repository.RepositoryEntryAuthorView;
import org.olat.repository.RepositoryEntryAuthorViewResults;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.SearchAuthorRepositoryEntryViewParams;
import org.olat.repository.model.SearchAuthorRepositoryEntryViewParams.OrderBy;
import org.olat.repository.model.SearchAuthorRepositoryEntryViewParams.ResourceUsage;
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
	
	private static final Logger log = Tracing.createLoggerFor(AuthoringEntryDataSource.class);

	private final SearchAuthorRepositoryEntryViewParams searchParams;
	
	private final ACService acService;
	private final AccessControlModule acModule;
	private final UserManager userManager;
	private final RepositoryService repositoryService;
	private final LicenseService licenseService;
	private final AuthoringEntryDataSourceUIFactory uifactory;
	private Integer count;
	private final boolean taxonomyEnabled;
	
	public AuthoringEntryDataSource(SearchAuthorRepositoryEntryViewParams searchParams,
			AuthoringEntryDataSourceUIFactory uifactory, boolean taxonomyEnabled) {
		this.searchParams = searchParams;
		this.uifactory = uifactory;
		this.taxonomyEnabled = taxonomyEnabled;
		
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
			int firstResult, int maxResults, SortKey... orderBy) {

		if(StringHelper.containsNonWhitespace(query)) {
			searchParams.setIdRefsAndTitle(query);
		} else {
			searchParams.setIdRefsAndTitle(null);
		}
		
		if(filters != null) {
			for(FlexiTableFilter filter:filters) {
				setFilterValue(filter);
			}
		}
		
		if(orderBy != null && orderBy.length > 0 && orderBy[0] != null) {
			OrderBy o = OrderBy.valueOf(orderBy[0].getKey());
			searchParams.setOrderBy(o);
			searchParams.setOrderByAsc(orderBy[0].isAsc());
		}
		
		RepositoryEntryAuthorViewResults viewResults = repositoryService.searchAuthorView(searchParams, firstResult, maxResults);
		List<RepositoryEntryAuthorView> views = viewResults.getViews();
		List<AuthoringEntryRow> rows = processViewModel(views);
		ResultInfos<AuthoringEntryRow> results = new DefaultResultInfos<>(firstResult + rows.size(), -1, rows);
		if(viewResults.isComplete() || (firstResult == 0 && views.size() < maxResults)) {
			count = Integer.valueOf(views.size() );
		}
		return results;
	}
	
	public enum AuthorSourceFilter {
		MARKED,
		OWNED,
		STATUS,
		TYPE,
		ID,
		AUTHOR,
		DISPLAYNAME,
		DESCRIPTION,
		TECHNICALTYPE,
		EDUCATIONALTYPE,
		TAXONOMYLEVEL,
		LICENSE,
		USAGE,
		ORGANISATION
	}
	
	private void setFilterValue(FlexiTableFilter filter) {
		switch(AuthorSourceFilter.valueOf(filter.getFilter())) {
			case MARKED: 
				if(StringHelper.containsNonWhitespace(((FlexiTableExtendedFilter)filter).getValue())) {
					searchParams.setMarked(Boolean.TRUE);
				} else {
					searchParams.setMarked(null);
				}
				break;
			case OWNED:
				boolean ownedOnly = StringHelper.containsNonWhitespace(((FlexiTableExtendedFilter)filter).getValue());
				searchParams.setOwnedResourcesOnly(ownedOnly);
				break;
			case STATUS:
				List<String> selectedStatus = ((FlexiTableMultiSelectionFilter)filter).getValues();
				searchParams.setStatus(RepositoryEntryStatusEnum.toArray(selectedStatus));
				break;
			case TYPE:
				searchParams.setResourceTypes(((FlexiTableMultiSelectionFilter)filter).getValues());
				break;
			case ID:
				searchParams.setIdAndRefs(((FlexiTableTextFilter)filter).getValue());
				break;
			case AUTHOR: 
				searchParams.setAuthor(((FlexiTableTextFilter)filter).getValue());
				break;
			case DISPLAYNAME:
				searchParams.setDisplayname(((FlexiTableTextFilter)filter).getValue());
				break;
			case DESCRIPTION:
				searchParams.setDescription(((FlexiTableTextFilter)filter).getValue());
				break;
			case TECHNICALTYPE:
				List<String> technicalTypes = ((FlexiTableMultiSelectionFilter)filter).getValues();
				searchParams.setTechnicalTypes(technicalTypes);
				break;
			case EDUCATIONALTYPE:
				List<Long> educationalTypes = ((FlexiTableMultiSelectionFilter)filter).getLongValues();
				searchParams.setEducationalTypeKeys(educationalTypes);
				break;
			case TAXONOMYLEVEL:
				List<String> taxonomyLevelKeys = ((FlexiTableMultiSelectionFilter)filter).getValues();
				List<TaxonomyLevelRef> taxonomyLevels = null;
				if(taxonomyLevelKeys != null) {
					taxonomyLevels = taxonomyLevelKeys
						.stream().filter(StringHelper::isLong)
						.map(Long::valueOf)
						.map(TaxonomyLevelRefImpl::new)
						.collect(Collectors.toList());
				}
				searchParams.setTaxonomyLevels(taxonomyLevels);
				break;
			case LICENSE:
				List<Long> licenseTypeKeys = ((FlexiTableMultiSelectionFilter)filter).getLongValues();
				searchParams.setLicenseTypeKeys(licenseTypeKeys);
				break;
			case USAGE:
				String usageKey = ((FlexiTableSingleSelectionFilter)filter).getValue();
				if(StringHelper.containsNonWhitespace(usageKey)) {
					searchParams.setResourceUsage(ResourceUsage.valueOf(usageKey));
				} else {
					searchParams.setResourceUsage(null);
				}
				break;
			case ORGANISATION:
				List<Long> organisationKeys = ((FlexiTableMultiSelectionFilter)filter).getLongValues();
				if(organisationKeys == null || organisationKeys.isEmpty()) {
					searchParams.setEntryOrganisations(null);
				} else {
					List<OrganisationRef> organisations = organisationKeys.stream()
							.map(OrganisationRefImpl::new)
							.collect(Collectors.toList());
					searchParams.setEntryOrganisations(organisations);
				}
				break;
			default:
				log.warn("Unkown author filter: {}", filter.getFilter());
				break;	
		}
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
		List<OLATResourceAccess> resourcesWithOffer = acService.filterResourceWithAC(resourcesWithAC, null);
		List<OLATResource> resourcesWithOpenAccess = acService.filterResourceWithOpenAccess(resourcesWithAC, null);
		List<OLATResource> resourcesWithGuestAccess = acService.filterResourceWithGuestAccess(resourcesWithAC);
		Map<Long, List<TaxonomyLevel>> entryKeyToTaxonomyLevels = getTaxonomyLevels(repoEntries);
		Map<Resourceable,ResourceLicense> licenses = getLicenses(repoEntries);
		
		List<AuthoringEntryRow> items = new ArrayList<>();
		for(RepositoryEntryAuthorView entry:repoEntries) {
			String fullname = fullNames.get(entry.getAuthor());
			if(fullname == null) {
				fullname = entry.getAuthor();
			}
			AuthoringEntryRow row = new AuthoringEntryRow(entry, fullname);
			// bookmark
			row.setMarked(entry.isMarked());
			
			boolean openAccess = resourcesWithOpenAccess.contains(entry.getOlatResource());
			row.setOpenAccess(openAccess);
			boolean guestAccess = resourcesWithGuestAccess.contains(entry.getOlatResource());
			row.setGuestAccess(guestAccess);
			
			// access control
			List<PriceMethod> types = new ArrayList<>(3);
			if(entry.isPublicVisible()) {
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
			} else {
				types.add(new PriceMethod("", "o_ac_membersonly_icon", uifactory.getTranslator().translate("cif.access.membersonly.short")));
			}
			
			if(!types.isEmpty()) {
				row.setAccessTypes(types);
			}
			
			// Taxonomy Level
			List<TaxonomyLevel> taxonomyLevels = entryKeyToTaxonomyLevels.get(entry.getKey());
			row.setTaxonomyLevels(taxonomyLevels);
			
			// license
			ResourceLicense license = licenses.get(new Resourceable(entry.getOlatResource()));
			row.setLicense(license);

			uifactory.forgeLinks(row);
			
			items.add(row);
		}
		return items;
	}

	private Map<Long, List<TaxonomyLevel>> getTaxonomyLevels(List<RepositoryEntryAuthorView> repoEntries) {
		Map<RepositoryEntryRef, List<TaxonomyLevel>> entryRefToTaxonomyLevels = taxonomyEnabled
				? repositoryService.getTaxonomy(repoEntries, true)
				: Collections.emptyMap();
		return entryRefToTaxonomyLevels.entrySet().stream()
				.collect(Collectors.toMap(e -> e.getKey().getKey(), Entry::getValue));
	}

	private Map<Resourceable,ResourceLicense> getLicenses(List<RepositoryEntryAuthorView> repoEntries) {
		Collection<OLATResourceable> resources = repoEntries.stream().map(RepositoryEntryAuthorView::getOlatResource).collect(Collectors.toList());
		List<ResourceLicense> licenses = licenseService.loadLicenses(resources);	
		return licenses.stream().collect(Collectors
				.toMap(license -> new Resourceable(license.getResName(), license.getResId()), license -> license, (u, v) -> v));
	}
}
