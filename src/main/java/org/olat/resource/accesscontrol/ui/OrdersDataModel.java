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
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.resource.accesscontrol.model.AccessTransaction;
import org.olat.resource.accesscontrol.model.Order;
import org.olat.resource.accesscontrol.model.OrderLine;
import org.olat.resource.accesscontrol.model.OrderPart;
import org.olat.resource.accesscontrol.model.PSPTransaction;

/**
 * 
 * Description:<br>
 * A data model which hold the orders and their transactions
 * 
 * <P>
 * Initial Date:  20 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class OrdersDataModel implements TableDataModel<OrderTableItem> {
	
	private final Locale locale;
	private List<OrderTableItem> orders;
	
	public OrdersDataModel(List<OrderTableItem> orders, Locale locale) {
		this.orders = orders;
		this.locale = locale;
	}

	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public int getRowCount() {
		return orders.size();
	}

	@Override
	public Object getValueAt(int row, int col) {
		OrderTableItem order = orders.get(row);
		switch(Col.get(col)) {
			case status: {
				return order;
			}
			case orderNr: return order.getOrder().getOrderNr();
			case creationDate: return order.getOrder().getCreationDate();
			case delivery: {
				User user = order.getOrder().getDelivery().getUser();
				return user.getProperty(UserConstants.FIRSTNAME, null) + " " + user.getProperty(UserConstants.LASTNAME, null);
			}
			case methods: {
				return order.getTransactions();
			}
			case total: {
				String total = PriceFormat.fullFormat(order.getOrder().getTotal());
				if(StringHelper.containsNonWhitespace(total)) {
					return total;
				}
				return "-";
			}
			case summary: {
				StringBuilder sb = new StringBuilder();
				for(OrderPart part:order.getOrder().getParts()) {
					for(OrderLine lines:part.getOrderLines()) {
						String displayName = lines.getOffer().getResourceDisplayName();
						if(sb.length() > 0) {
							sb.append(", ");
						}
						sb.append(displayName);
					}
				}
				return sb.toString();
			}
			default: return order;
		}
	}

	@Override
	public OrderTableItem getObject(int row) {
		return orders.get(row);
	}
	
	public OrderTableItem getItem(Long key) {
		if(orders == null) return null;
		for(OrderTableItem item:orders) {
			if(item.getOrder().getKey().equals(key)) {
				return item;
			}
		}
		return null;
	}

	@Override
	public void setObjects(List<OrderTableItem> objects) {
		this.orders = objects;
	}

	@Override
	public Object createCopyWithEmptyList() {
		return new OrdersDataModel(Collections.<OrderTableItem>emptyList(), locale);
	}
	
	
	public static List<OrderTableItem> create(List<Order> orders, List<AccessTransaction> transactions, List<PSPTransaction> pspTransactions) {
		List<OrderTableItem> items = new ArrayList<OrderTableItem>();
		
		for(Order order:orders) {
			OrderTableItem item = new OrderTableItem(order);
			for(AccessTransaction transaction:transactions) {
				if(transaction.getOrder().equals(order)) {
					item.getTransactions().add(transaction);
				}
			}
			for(PSPTransaction transaction:pspTransactions) {
				if(transaction.getOrderId().equals(order.getKey())) {
					item.getPSPTransactions().add(transaction);
				}
			}
			
			items.add(item);
		}
		
		return items;
	}
	
	public enum Col {
		orderNr,
		creationDate,
		delivery,
		methods,
		total,
		summary,
		status;

		public static Col get(int index) {
			return values()[index];
		}
	}
}
