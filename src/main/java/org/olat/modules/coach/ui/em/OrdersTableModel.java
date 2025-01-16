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
package org.olat.modules.coach.ui.em;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.resource.accesscontrol.model.UserOrder;

/**
 * Initial date: 2025-01-16<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class OrdersTableModel extends DefaultFlexiTableDataModel<UserOrder> implements SortableFlexiTableDataModel<UserOrder> {

	private final Translator translator;

	public OrdersTableModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		translator = Util.createPackageTranslator(org.olat.resource.accesscontrol.ui.OrdersController.class, locale);
	}

	@Override
	public void sort(SortKey sortKey) {
		SortableFlexiTableModelDelegate<UserOrder> sorter = new SortableFlexiTableModelDelegate<>(sortKey, this, null);
		List<UserOrder> objects = sorter.sort();
		super.setObjects(objects);
	}

	@Override
	public Object getValueAt(int row, int col) {
		UserOrder userOrder = getObject(row);
		return getValueAt(userOrder, col);
	}

	@Override
	public Object getValueAt(UserOrder row, int col) {
		if (col >= 0 && col < OrdersCols.values().length) {
			return switch (OrdersCols.values()[col]) {
				case orderId -> row.getOrder().getKey(); 
				case orderStatus -> translator.translate("order.status." + row.getOrder().getOrderStatus().name().toLowerCase());
			};
		}
		
		int propsPos = col - OrdersController.USER_PROPS_OFFSET;
		return row.getIdentityProp(propsPos);
	}

	public enum OrdersCols implements FlexiSortableColumnDef {
		orderId("table.header.order.id"),
		orderStatus("table.header.order.status");
		
		private final String i18nKey;
		
		OrdersCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
	}
}
