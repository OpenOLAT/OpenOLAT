package org.olat.resource.accesscontrol.ui;

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DefaultResultInfos;
import org.olat.core.commons.persistence.ResultInfos;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataSourceDelegate;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.OrderStatus;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 5 janv. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrdersDataSource implements FlexiTableDataSourceDelegate<OrderTableItem> {
	
	private ACService acService;
	
	private Long refNo;
	private Integer count;
	private Date from, to;
	private final OLATResource resource;
	private final IdentityRef delivery;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	public OrdersDataSource(ACService acService, OLATResource resource, IdentityRef delivery,
			List<UserPropertyHandler> userPropertyHandlers) {
		this.acService = acService;
		this.resource = resource;
		this.delivery = delivery;
		this.userPropertyHandlers = userPropertyHandlers;
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
	public List<OrderTableItem> reload(List<OrderTableItem> rows) {
		return rows;
	}

	@Override
	public ResultInfos<OrderTableItem> getRows(String query, List<FlexiTableFilter> filters, List<String> condQueries,
			int firstResult, int maxResults, SortKey... orderBy) {
		
		OrderStatus[] states = null;
		if(filters != null && filters.size() > 0) {
			String filter = filters.get(0).getFilter();
			states = new OrderStatus[] { OrderStatus.valueOf(filter) };
		}

		List<OrderTableItem> rows = acService.findOrderItems(resource, delivery, refNo, from, to, states,
				firstResult, maxResults, userPropertyHandlers, orderBy);
		ResultInfos<OrderTableItem> results = new DefaultResultInfos<>(firstResult + rows.size(), -1, rows);
		return results;
	}
}