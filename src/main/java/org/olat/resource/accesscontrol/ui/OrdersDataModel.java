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
import java.util.Locale;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataSourceModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataSourceDelegate;
import org.olat.core.util.StringHelper;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.user.UserManager;

/**
 * 
 * Initial date: 5 janv. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrdersDataModel extends DefaultFlexiTableDataSourceModel<OrderTableItem> {
	
	private final Locale locale;
	private final UserManager userManager;
	
	public OrdersDataModel(FlexiTableDataSourceDelegate<OrderTableItem> dataSource, Locale locale, UserManager userManager, FlexiTableColumnModel columnModel) {
		super(dataSource, columnModel);
		this.locale = locale;
		this.userManager = userManager;
	}

	@Override
	public Object getValueAt(int row, int col) {
		OrderTableItem order = getObject(row);
		 if(col >= OrdersAdminController.USER_PROPS_OFFSET) {
			int propIndex = col - OrdersAdminController.USER_PROPS_OFFSET;
			return order.getUserProperties()[propIndex];
		}
		
		switch(OrderCol.values()[col]) {
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
	public OrdersDataModel createCopyWithEmptyList() {
		return new OrdersDataModel(getSourceDelegate(), locale, userManager, getTableColumnModel());
	}
	
	public enum OrderCol implements FlexiSortableColumnDef {
		orderNr("order.nr", "order_id"),
		creationDate("order.creationDate", "creationdate"),
		delivery("order.delivery", "delivery_id"),
		methods("order.part.payment", "trxMethodIds"),
		total("order.total", "total_amount"),
		summary("order.summary", "resDisplaynames"),
		status("order.status", "o_status");

		private final String i18nKey;
		private final String sortKey;
		
		private OrderCol(String i18nKey, String sortKey) {
			this.i18nKey = i18nKey;
			this.sortKey = sortKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return sortKey;
		}
	}
}
