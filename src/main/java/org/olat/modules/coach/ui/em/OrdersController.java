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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.resource.accesscontrol.model.UserOrder;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2025-01-16<br>
 * 
 * In the DB we call the items displayed in this controller 'orders', in the UI we call them 'Bookings'.
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class OrdersController extends FormBasicController {

	public static final int USER_PROPS_OFFSET = 100;

	private final List<UserOrder> orders;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final String propsIdentifier;
	
	private FlexiTableElement tableEl;
	private OrdersTableModel tableModel;

	@Autowired
	private UserManager userManager;

	public OrdersController(UserRequest ureq, WindowControl wControl, List<UserOrder> orders, List<UserPropertyHandler> userPropertyHandlers, String propsIdentifier) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.orders = orders;
		this.userPropertyHandlers = userPropertyHandlers;
		this.propsIdentifier = propsIdentifier;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrdersTableModel.OrdersCols.orderId));
		
		int colIndex = USER_PROPS_OFFSET;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			boolean visible = userManager.isMandatoryUserProperty(propsIdentifier, userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible,
					userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex++, true,
					userPropertyHandler.i18nColumnDescriptorLabelKey()));
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrdersTableModel.OrdersCols.orderStatus));

		tableModel = new OrdersTableModel(columnsModel, getLocale());
		tableModel.setObjects(orders);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, 
				getTranslator(), formLayout);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	public void reload(List<UserOrder> orders) {
		tableModel.setObjects(orders);
		tableEl.reloadData();
	}
}
