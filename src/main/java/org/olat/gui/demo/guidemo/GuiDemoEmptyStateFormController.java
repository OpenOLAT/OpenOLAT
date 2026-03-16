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
import org.olat.core.gui.components.emptystate.EmptyStateVariant;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableEmptyNextPrimaryActionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableEmptySecondaryActionEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;

/**
 * Demo of an empty state embedded inside a FormBasicController,
 * including a FlexiTable with its own empty state.
 */
public class GuiDemoEmptyStateFormController extends FormBasicController {

	private final EmptyStateVariant variant;
	private FlexiTableElement tableEl;

	public GuiDemoEmptyStateFormController(UserRequest ureq, WindowControl wControl, EmptyStateVariant variant) {
		super(ureq, wControl, "guidemo-empty-state-form");
		this.variant = variant;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		flc.contextPut("variant", variant.name());

		EmptyStateConfig emptyStateConfig = EmptyStateConfig.builder()
				.withVariant(variant)
				.withIconCss("o_icon_qual_preview")
				.withMessageI18nKey("empty.state.form.message")
				.withHintI18nKey("empty.state.hint.additional")
				.withHelp(translate("empty.state.help.additional"), "release_notes/")
				.build();
		EmptyState emptyState = EmptyStateFactory.create("emptyStateInForm", flc.getFormItemComponent(), this, emptyStateConfig);
		emptyState.setTranslator(getTranslator());

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.item));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.price));

		ItemPriceTableModel tableModel = new ItemPriceTableModel(columnsModel);
		tableModel.setObjects(List.of());

		tableEl = uifactory.addTableElement(getWindowControl(), "itemPriceTable", tableModel, getTranslator(), formLayout);
		EmptyStateConfig config = EmptyStateConfig.builder()
				.withVariant(variant)
				.withMessageI18nKey("empty.state.table.message")
				.withIconCss("o_icon_square_rss")
				.withHintI18nKey("empty.state.table.hint")
				.withHelp(translate("empty.state.help.additional"), "release_notes/")
				.withPrimaryButton("o_icon_search", "select", null)
				.withSecondaryButton("o_icon_add", "create", null, "create")
				.withSecondaryButton("o_icon_link", "link", null, "link")
				.build();
		tableEl.setEmptyStateConfig(config, false);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == tableEl) {
			if (event instanceof FlexiTableEmptyNextPrimaryActionEvent) {
				showInfo("empty.state.event.primary");
			} else if (event instanceof FlexiTableEmptySecondaryActionEvent actionEvent) {
				showInfo("empty.state.event.secondary", actionEvent.getAction());
			}
		}
		super.formInnerEvent(ureq, source, event);
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