/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.creditpoint.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateTimeFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.creditpoint.CreditPointService;
import org.olat.modules.creditpoint.CreditPointSystem;
import org.olat.modules.creditpoint.CreditPointTransaction;
import org.olat.modules.creditpoint.CreditPointTransactionDetails;
import org.olat.modules.creditpoint.ui.CreditPointTransactionsDetailsTableModel.TransactionDetailCols;
import org.olat.modules.creditpoint.ui.component.CreditCellRenderer;
import org.olat.modules.creditpoint.ui.component.CreditPointSourceCellRenderer;
import org.olat.modules.creditpoint.ui.component.DebitCellRenderer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CreditPointTransactionsDetailsController extends FormBasicController {
	
	private FormLink closeButton;
	private FlexiTableElement tableEl;
	private CreditPointTransactionsDetailsTableModel tableModel;
	
	private final CreditPointSystem system;
	private final CreditPointTransaction transaction;
	
	@Autowired
	private CreditPointService creditPointService;
	
	public CreditPointTransactionsDetailsController(UserRequest ureq, WindowControl wControl,
			CreditPointTransaction transaction, CreditPointSystem system) {
		super(ureq, wControl, "transaction_details");
		this.system = system;
		this.transaction = transaction;
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		closeButton = uifactory.addFormLink("close", formLayout, Link.BUTTON);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, TransactionDetailCols.id));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TransactionDetailCols.nr));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TransactionDetailCols.creationDate,
				new DateTimeFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TransactionDetailCols.credit,
				new CreditCellRenderer(system)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TransactionDetailCols.debit,
				new DebitCellRenderer(system)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TransactionDetailCols.expirationDate,
				new DateTimeFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TransactionDetailCols.source,
				new CreditPointSourceCellRenderer()));
		
		tableModel = new CreditPointTransactionsDetailsTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
	}
	
	private void loadModel() {
		List<CreditPointTransactionDetailsRow> rows = new ArrayList<>();
		rows.add(new CreditPointTransactionDetailsRow(null, transaction));
		
		List<CreditPointTransactionDetails> detailsList = creditPointService.getCreditPointTransactionsDetails(transaction);
		for(CreditPointTransactionDetails details:detailsList) {
			CreditPointTransaction detailedTransaction;
			if(details.getSource().equals(transaction)) {
				detailedTransaction = details.getTarget();
			} else {
				detailedTransaction = details.getSource();
			}
			rows.add(new CreditPointTransactionDetailsRow(details, detailedTransaction));
		}
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(closeButton == source) {
			fireEvent(ureq, Event.CLOSE_EVENT);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.CLOSE_EVENT);
	}
}
