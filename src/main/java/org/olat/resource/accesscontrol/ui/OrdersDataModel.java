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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.util.StringHelper;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.user.UserManager;

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
	private final UserManager userManager;
	
	public OrdersDataModel(List<OrderTableItem> orders, Locale locale, UserManager userManager) {
		this.orders = orders;
		this.locale = locale;
		this.userManager = userManager;
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
			case orderNr: return order.getOrderNr();
			case creationDate: return order.getCreationDate();
			case delivery: {
				Long deliveryKey = order.getDeliveryKey();
				return userManager.getUserDisplayName(deliveryKey);
			}
			case methods: {
				return order.getMethods();
			}
			case total: {
				boolean paymentMethod = false;
				Collection<AccessMethod> methods = order.getMethods();
				for(AccessMethod method:methods) {
					paymentMethod |= method.isPaymentMethod();
				}
				
				if(paymentMethod) {
					String total = PriceFormat.fullFormat(order.getTotal());
					if(StringHelper.containsNonWhitespace(total)) {
						return total;
					}
				}
				return "-";
			}
			case summary: return order.getResourceDisplayname();
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
			if(item.getOrderKey().equals(key)) {
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
		return new OrdersDataModel(Collections.<OrderTableItem>emptyList(), locale, userManager);
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
