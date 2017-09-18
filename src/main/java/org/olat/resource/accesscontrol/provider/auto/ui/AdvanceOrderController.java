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
package org.olat.resource.accesscontrol.provider.auto.ui;

import java.util.ArrayList;
import java.util.Collection;
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
import org.olat.core.id.Identity;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.provider.auto.AdvanceOrder;
import org.olat.resource.accesscontrol.provider.auto.AutoAccessManager;
import org.olat.resource.accesscontrol.provider.auto.ui.AdvanceOrderDataModel.AdvanceOrderCol;
import org.olat.resource.accesscontrol.ui.AccessMethodRenderer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 08.09.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AdvanceOrderController extends FormBasicController {

	private FlexiTableElement tableEl;
	private AdvanceOrderDataModel dataModel;
	private Identity identity;

	@Autowired
	private AutoAccessManager autoAccessManager;
	@Autowired
	private AccessControlModule acModule;

	public AdvanceOrderController(UserRequest ureq, WindowControl wControl, Identity identity) {
		super(ureq, wControl, "advance_order_table");
		this.identity = identity;

		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AdvanceOrderCol.creationDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AdvanceOrderCol.identifierKey));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AdvanceOrderCol.identifierValue));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AdvanceOrderCol.method, new AccessMethodRenderer(acModule)));
		dataModel = new AdvanceOrderDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, getTranslator(), formLayout);
		tableEl.setEmtpyTableMessageKey("table.advanceOrder.empty");
	}

	private void loadModel() {
		Collection<AdvanceOrder> advanceOrders = autoAccessManager.loadPendingAdvanceOrders(identity);
		List<AdvanceOrderRow> rows = new ArrayList<>(advanceOrders.size());
		for(AdvanceOrder advanceOrder: advanceOrders) {
			rows.add(new AdvanceOrderRow(advanceOrder));
		}
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}

}
