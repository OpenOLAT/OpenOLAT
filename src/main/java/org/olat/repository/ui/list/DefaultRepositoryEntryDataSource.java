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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DefaultResultInfos;
import org.olat.core.commons.persistence.ResultInfos;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataSourceDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.condition.ConditionNodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.repository.RepositoryEntryMyView;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.SearchMyRepositoryEntryViewParams;
import org.olat.repository.model.SearchMyRepositoryEntryViewParams.Filter;
import org.olat.repository.model.SearchMyRepositoryEntryViewParams.OrderBy;
import org.olat.repository.ui.PriceMethod;
import org.olat.repository.ui.RepositoryEntryImageMapper;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.Price;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.olat.resource.accesscontrol.model.OLATResourceAccess;
import org.olat.resource.accesscontrol.model.PriceMethodBundle;
import org.olat.resource.accesscontrol.provider.free.FreeAccessHandler;
import org.olat.resource.accesscontrol.provider.token.TokenAccessHandler;
import org.olat.resource.accesscontrol.ui.PriceFormat;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28.01.2014<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class DefaultRepositoryEntryDataSource implements FlexiTableDataSourceDelegate<RepositoryEntryRow> {

	private final RepositoryEntryDataSourceUIFactory uifactory;
	private final SearchMyRepositoryEntryViewParams searchParams;
	private RepositoryEntryStatusEnum[] baseEntryStatus;

	private Integer count;
	
	@Autowired
	private ACService acService;
	@Autowired
	private AccessControlModule acModule;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private NodeAccessService nodeAccessService;

	public DefaultRepositoryEntryDataSource(SearchMyRepositoryEntryViewParams searchParams,
			RepositoryEntryDataSourceUIFactory uifactory) {
		CoreSpringFactory.autowireObject(this);
		this.uifactory = uifactory;
		this.searchParams = searchParams;
		baseEntryStatus = searchParams.getEntryStatus();
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
			int firstResult, int maxResults, SortKey... orderBy) {
		
		if(filters != null && !filters.isEmpty()) {
			if(filters.get(0) instanceof FlexiTableExtendedFilter) {
				searchParams.setFilters(null);
				for(FlexiTableFilter filter:filters) {
					setFilter(filter);
				}
			} else {
				String filter = filters.get(0).getFilter();
				if(StringHelper.containsNonWhitespace(filter)) {
					searchParams.setFilters(Collections.singletonList(Filter.valueOf(filter)));
				} else {
					searchParams.setFilters(null);
				}
			}
		} else {
			searchParams.setFilters(null);
		}

		if(StringHelper.containsNonWhitespace(query)) {
			searchParams.setIdRefsAndTitle(query);
		} else {
			searchParams.setIdRefsAndTitle(null);
		}
		
		if(orderBy != null && orderBy.length > 0 && orderBy[0] != null) {
			OrderBy o = OrderBy.valueOf(orderBy[0].getKey());
			searchParams.setOrderBy(o);
			searchParams.setOrderByAsc(orderBy[0].isAsc());
		}
		
		List<RepositoryEntryMyView> views = repositoryService.searchMyView(searchParams, firstResult, maxResults);
		List<RepositoryEntryRow> rows = processViewModel(views);
		ResultInfos<RepositoryEntryRow> results = new DefaultResultInfos<>(firstResult + rows.size(), -1, rows);
		if(firstResult == 0 && views.size() < maxResults) {
			count = Integer.valueOf(views.size());
		}
		return results;
	}
	
	private void setFilter(FlexiTableFilter filter) {
		FilterButton button = FilterButton.secureValueOf(filter.getFilter());
		if(button == null) return;
		
		switch(button) {
			case MARKED:
				String markedValue = ((FlexiTableExtendedFilter)filter).getValue();
				searchParams.setMarked(StringHelper.containsNonWhitespace(markedValue) ? Boolean.TRUE : null);
				break;
			case OWNED:
				String ownedValue = ((FlexiTableExtendedFilter)filter).getValue();
				searchParams.setMembershipMandatory(StringHelper.containsNonWhitespace(ownedValue) || searchParams.isMembershipOnly());
				break;
			case STATUS:
				setStatusFilter((FlexiTableExtendedFilter)filter);
				break;
			case DATES:
				List<String> filterVals = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (filterVals != null) {
					filterVals.forEach(filterVal -> searchParams.addFilter(Filter.valueOf(filterVal)));
				}
				break;
			case EDUCATIONALTYPE:
				List<Long> educationalTypes = ((FlexiTableMultiSelectionFilter)filter).getLongValues();
				searchParams.setEducationalTypeKeys(educationalTypes);
				break;
			case AUTHORS:
				searchParams.setAuthor(filter.getValue());
				break;
			default:
				// BOOKING, PASSED are all old style filters
				String filterVal = ((FlexiTableExtendedFilter)filter).getValue();
				if(filterVal != null) {
					searchParams.addFilter(Filter.valueOf(filterVal));
				}
				break;
		}
	}
	
	private void setStatusFilter(FlexiTableExtendedFilter filter) {
		List<String> values = filter.getValues();
		Set<RepositoryEntryStatusEnum> statusSet = new HashSet<>();
		if(values == null || values.isEmpty()) {
			searchParams.setEntryStatus(baseEntryStatus);
		} else {
			for(String val:values) {
				FilterStatus status = FilterStatus.secureValueOf(val);
				if(status == null) continue;
				
				switch(status) {
					case CLOSED:
						statusSet.add(RepositoryEntryStatusEnum.closed);
						break;
					case ACTIVE:
						statusSet.add(RepositoryEntryStatusEnum.published);
						break;
					case PREPARATION:
						Collections.addAll(statusSet, RepositoryEntryStatusEnum.preparationToCoachPublished());
						break;
				}
			}
			RepositoryEntryStatusEnum[] statusArr = statusSet.toArray(new RepositoryEntryStatusEnum[statusSet.size()]);
			searchParams.setEntryStatus(statusArr);
		}
	}
	
	public enum FilterButton {
		MARKED,
		OWNED,
		STATUS,
		PASSED,
		BOOKING,
		DATES,
		EDUCATIONALTYPE,
		AUTHORS;
		
		public static FilterButton secureValueOf(String val) {
			for(FilterButton button:values()) {
				if(button.name().equalsIgnoreCase(val)) {
					return button;
				}
			}
			return null;
		}
	}
	
	public enum FilterStatus {
		PREPARATION,
		ACTIVE,
		CLOSED;
		
		public static FilterStatus secureValueOf(String val) {
			for(FilterStatus status:values()) {
				if(status.name().equalsIgnoreCase(val)) {
					return status;
				}
			}
			return null;
		}
	}

	private List<RepositoryEntryRow> processViewModel(List<RepositoryEntryMyView> repoEntries) {
		List<Long> repoKeys = new ArrayList<>(repoEntries.size());
		List<OLATResource> resourcesWithAC = new ArrayList<>(repoEntries.size());
		for(RepositoryEntryMyView entry:repoEntries) {
			repoKeys.add(entry.getKey());
			if(entry.isValidOfferAvailable()) {
				resourcesWithAC.add(entry.getOlatResource());
			}
		}
		List<OLATResourceAccess> resourcesWithOffer = acService.filterResourceWithAC(resourcesWithAC, searchParams.getOfferOrganisations());
		repositoryService.filterMembership(searchParams.getIdentity(), repoKeys);
		
		final Locale locale = uifactory.getTranslator().getLocale();
		
		List<RepositoryEntryRow> items = new ArrayList<>();
		for(RepositoryEntryMyView entry:repoEntries) {
			RepositoryEntryRow row = new RepositoryEntryRow(entry);
			
			VFSLeaf image = repositoryManager.getImage(entry.getKey(), entry.getOlatResource());
			if(image != null) {
				row.setThumbnailRelPath(RepositoryEntryImageMapper.getImageUrl(uifactory.getMapperThumbnailUrl() , image));
			}
			
			String translatedType;
			if(StringHelper.containsNonWhitespace(entry.getTechnicalType())) {
				NodeAccessType type = NodeAccessType.of(entry.getTechnicalType());
				translatedType = ConditionNodeAccessProvider.TYPE.equals(type.getType())
						? uifactory.getTranslator().translate("CourseModule")
						: nodeAccessService.getNodeAccessTypeName(type, locale);
			} else {
				translatedType = uifactory.getTranslator().translate(row.getOLATResourceable().getResourceableTypeName());
			}
			row.setTranslatedTechnicalType(translatedType);
			row.setNumOfTaxonomyLevels(entry.getNumOfTaxonomyLevels());
			row.setMember(repoKeys.contains(entry.getKey()));
			
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
							String displayName = amh.getMethodName(locale);
							types.add(new PriceMethod(price, type, displayName));
						}
					}
				}
				updateAccessInfo(row, resourcesWithOffer);
			} else {
				types.add(new PriceMethod("", "o_ac_membersonly_icon", uifactory.getTranslator().translate("cif.access.membersonly.short")));
			} 
			
			if(!types.isEmpty()) {
				row.setAccessTypes(types);
			}
			
			uifactory.forgeMarkLink(row);
			uifactory.forgeCompletion(row);
			uifactory.forgeSelectLink(row);
			uifactory.forgeStartLink(row);
			uifactory.forgeDetails(row);
			uifactory.forgeRatings(row);
			uifactory.forgeTaxonomyLevels(row);
			
			items.add(row);
		}
		return items;
	}
	
	private void updateAccessInfo(RepositoryEntryRow row, List<OLATResourceAccess> resourcesWithOffer) {
		if (row.isMember() || resourcesWithOffer.isEmpty()) {
			return;
		}
		
		for(OLATResourceAccess resourceAccess:resourcesWithOffer) {
			for(PriceMethodBundle bundle:resourceAccess.getMethods()) {
				if (bundle.getMethod().getType().equals(FreeAccessHandler.METHOD_TYPE)) {
					row.setAccessInfo(uifactory.getTranslator().translate("access.info.freely.available"));
					return;
				}
			}
		}
		
		for(OLATResourceAccess resourceAccess:resourcesWithOffer) {
			for(PriceMethodBundle bundle:resourceAccess.getMethods()) {
				if (bundle.getMethod().getType().equals(TokenAccessHandler.METHOD_TYPE)) {
					row.setAccessInfo(uifactory.getTranslator().translate("access.info.token"));
					return;
				}
			}
		}
		
		BigDecimal lowestPriceAmount = null;
		String lowestPrice = null;
		int numOfPrices = 0;
		for(OLATResourceAccess resourceAccess:resourcesWithOffer) {
			for(PriceMethodBundle bundle:resourceAccess.getMethods()) {
				Price p = bundle.getPrice();
				String price = p == null || p.isEmpty() ? "" : PriceFormat.fullFormat(p);
				if (p != null && StringHelper.containsNonWhitespace(price)) {
					numOfPrices++;
					if (lowestPriceAmount == null || lowestPriceAmount.compareTo(p.getAmount()) > 0) {
						lowestPriceAmount = p.getAmount();
						lowestPrice = price;
					}
				}
			}
		}
		if (lowestPriceAmount != null) {
			if (numOfPrices > 1) {
				lowestPrice = uifactory.getTranslator().translate("book.price.from", lowestPrice);
			}
			row.setAccessInfo(lowestPrice);
		}
	}
}
