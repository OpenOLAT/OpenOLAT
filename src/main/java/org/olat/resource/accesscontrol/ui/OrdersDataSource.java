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
package org.olat.resource.accesscontrol.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DefaultResultInfos;
import org.olat.core.commons.persistence.ResultInfos;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataSourceDelegate;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.OrderStatus;
import org.olat.resource.accesscontrol.ui.OrderTableItem.Status;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 5 janv. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrdersDataSource implements FlexiTableDataSourceDelegate<OrderTableRow> {
	
	public static final String FILTER_STATUS = "status";
	public static final String FILTER_METHOD = "method";
	public static final String FILTER_OFFER = "offer";
	
	private ACService acService;
	
	private Long refNo;
	private Integer count;
	private Date to;
	private Date from;
	private final OLATResource resource;
	private final IdentityRef delivery;
	private final ForgeDelegate delegate;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	private Map<Long,OrderModification> modifications = Map.of();
	
	public OrdersDataSource(ACService acService, OLATResource resource, IdentityRef delivery,
			List<UserPropertyHandler> userPropertyHandlers, ForgeDelegate delegate) {
		this.acService = acService;
		this.resource = resource;
		this.delivery = delivery;
		this.delegate = delegate;
		this.userPropertyHandlers = userPropertyHandlers;
	}
	
	public void setModifications(List<OrderModification> orderModifications) {
		modifications = orderModifications.stream()
				.collect(Collectors.toMap(OrderModification::orderKey, o -> o, (u, v) -> u));
	}

	public Long getRefNo() {
		return refNo;
	}

	public void setRefNo(Long refNo) {
		this.refNo = refNo;
	}

	public Date getFrom() {
		return from;
	}

	public void setFrom(Date from) {
		this.from = from;
	}

	public Date getTo() {
		return to;
	}

	public void setTo(Date to) {
		this.to = to;
	}
	
	public void reset() {
		count = null;
	}

	@Override
	public int getRowCount() {
		if(count == null) {
			count = acService.countOrderItems(resource, delivery, refNo, from, to, null);
		}
		return count.intValue();
	}

	@Override
	public List<OrderTableRow> reload(List<OrderTableRow> rows) {
		return rows;
	}

	@Override
	public ResultInfos<OrderTableRow> getRows(String query, List<FlexiTableFilter> filters,
			int firstResult, int maxResults, SortKey... orderBy) {
		
		OrderStatus[] states = null;
		List<OrderStatus> filterStatus = getFilterStatus(filters);
		if(filterStatus != null && !filterStatus.isEmpty()) {
			states = filterStatus.toArray(new OrderStatus[filterStatus.size()]);
		}
		
		List<Long> methods = getFilterMethods(filters, FILTER_METHOD);
		List<Long> offerAccess = getFilterMethods(filters, FILTER_OFFER);

		List<OrderTableItem> items = acService.findOrderItems(resource, delivery, refNo, from, to, states,
				methods, offerAccess, firstResult, maxResults, userPropertyHandlers, orderBy);
		List<OrderTableRow> rows = new ArrayList<>(items.size());
		for(OrderTableItem item:items) {
			OrderTableRow row = new OrderTableRow(item);
			updateModifications(row);
			delegate.forge(row);
			rows.add(row);
		}
		return new DefaultResultInfos<>(firstResult + rows.size(), -1, rows);
	}
	
	protected OrderTableRow updateModifications(OrderTableRow row) {
		OrderModification modification = modifications.get(row.getOrderKey());
		if(modification != null) {
			Status modifiedStatus = getStatus(modification.nextStatus());
			row.setModifiedStatus(modifiedStatus);
			row.setModificationsSummary(new OrderModificationSummary(modifiedStatus != null));
		} else {
			row.setModifiedStatus(null);
			row.setModificationsSummary(null);
		}
		return row;
	}
	
	private Status getStatus(OrderStatus nextStatus) {
		return switch(nextStatus) {
			case PAYED -> Status.OK;
			case CANCELED -> Status.CANCELED;
			case ERROR -> Status.ERROR;
			default -> null;
		};
	}
	
	private List<OrderStatus> getFilterStatus(List<FlexiTableFilter> filters) {
		FlexiTableFilter statusFilter = FlexiTableFilter.getFilter(filters, FILTER_STATUS);
		if (statusFilter instanceof FlexiTableExtendedFilter extendedFilter) {
			List<String> filterValues = extendedFilter.getValues();
			if(filterValues != null && !filterValues.isEmpty()) {
				return filterValues.stream()
						.map(OrderStatus::valueOf)
						.toList();
			}
		}
		return List.of();
	}
	
	private List<Long> getFilterMethods(List<FlexiTableFilter> filters, String filterName) {
		FlexiTableFilter longFilter = FlexiTableFilter.getFilter(filters, filterName);
		if (longFilter instanceof FlexiTableExtendedFilter extendedFilter) {
			List<String> filterValues = extendedFilter.getValues();
			if(filterValues != null && !filterValues.isEmpty()) {
				return filterValues.stream()
						.map(Long::valueOf)
						.toList();
			}
		}
		return List.of();
	}
	
	public interface ForgeDelegate {
		
		void forge(OrderTableRow row);
		
	}
}