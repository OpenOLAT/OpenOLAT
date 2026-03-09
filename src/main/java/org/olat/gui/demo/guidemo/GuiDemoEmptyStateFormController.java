/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.gui.demo.guidemo;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.emptystate.EmptyState;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.emptystate.EmptyStateFactory;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;

/**
 * Demo of an empty state embedded inside a FormBasicController,
 * including a FlexiTable with its own empty state.
 */
public class GuiDemoEmptyStateFormController extends FormBasicController {

	private FlexiTableElement tableEl;

	public GuiDemoEmptyStateFormController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "guidemo-empty-state-form");
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		EmptyStateConfig emptyStateConfig = EmptyStateConfig.builder()
				.withIconCss("o_icon_empty_objects")
				.withMessageI18nKey("empty.state.form.message")
				.withHintI18nKey("empty.state.hint.additional")
				.withPrimaryButton("o_icon_bolt_lightning", "empty.state.button.primary.action", null)
				.build();
		EmptyState emptyState = EmptyStateFactory.create("emptyStateInForm", flc.getFormItemComponent(), this, emptyStateConfig);
		emptyState.setTranslator(getTranslator());

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.item));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.price));

		ItemPriceTableModel tableModel = new ItemPriceTableModel(columnsModel);
		tableModel.setObjects(List.of());

		tableEl = uifactory.addTableElement(getWindowControl(), "itemPriceTable", tableModel, getTranslator(), formLayout);
		tableEl.setEmptyTableSettings("empty.state.table.message", null, "o_icon_empty_objects",
				"empty.state.button.primary.action", "o_icon_bolt_lightning", false);

		uifactory.addFormSubmitButton("submit", formLayout);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// nothing to do in demo
	}

	enum Cols implements FlexiSortableColumnDef {
		item("table.col.item"),
		price("table.col.price");

		private final String i18nKey;

		Cols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return false;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}

	private static class ItemPriceTableModel extends DefaultFlexiTableDataModel<ItemPriceRow> {

		ItemPriceTableModel(FlexiTableColumnModel columnModel) {
			super(columnModel);
		}

		@Override
		public Object getValueAt(int row, int col) {
			ItemPriceRow item = getObject(row);
			return switch (Cols.values()[col]) {
				case item -> item.item();
				case price -> item.price();
			};
		}
	}

	record ItemPriceRow(String item, String price) {}
}