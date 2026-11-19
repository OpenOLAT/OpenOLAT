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

import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.emptystate.EmptyState;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.emptystate.EmptyStateConfigBuilder;
import org.olat.core.gui.components.emptystate.EmptyStateFactory;
import org.olat.core.gui.components.emptystate.EmptyStateVariant;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SliderElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableEmptyNextPrimaryActionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableEmptySecondaryActionEvent;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;

/**
 * Playground of the empty state: the settings change the preview at once,
 * including a FlexiTable with its own empty state.
 */
public class GuiDemoEmptyStateFormController extends FormBasicController {

	private static final String[] LENGTHS = { "small", "medium", "large" };
	private static final String[] ELEMENTS = { "icon", "title", "hint", "link", "buttons" };
	
	private SingleSelection widthEl;
	private SliderElement heightEl;
	private SingleSelection variantEl;
	private MultipleSelectionElement elementsEl;
	private SingleSelection buttonsEl;
	private SingleSelection titleLengthEl;
	private SingleSelection hintLengthEl;
	private FlexiTableElement tableEl;

	public GuiDemoEmptyStateFormController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "guidemo-empty-state-form");
		initForm(ureq);
		updatePreview();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		SelectionValues widthSV = new SelectionValues();
		widthSV.add(entry("desktop", translate("empty.state.demo.width.desktop")));
		widthSV.add(entry("tablet", translate("empty.state.demo.width.tablet")));
		widthSV.add(entry("phone", translate("empty.state.demo.width.phone")));
		widthEl = uifactory.addButtonGroupSingleSelectHorizontal("empty.state.demo.width", formLayout, widthSV);
		widthEl.select("desktop", true);
		widthEl.addActionListener(FormEvent.ONCHANGE);

		heightEl = uifactory.addSliderElement("empty.state.demo.height", "empty.state.demo.height", formLayout);
		heightEl.setMinValue(120);
		heightEl.setMaxValue(800);
		heightEl.setStep(10);
		heightEl.setValue(480);
		heightEl.addActionListener(FormEvent.ONCHANGE);
		
		SelectionValues variantSV = new SelectionValues();
		for (EmptyStateVariant variant : EmptyStateVariant.values()) {
			variantSV.add(entry(variant.name(), translate("empty.state.demo.variant." + variant.name())));
		}
		variantEl = uifactory.addButtonGroupSingleSelectHorizontal("empty.state.demo.variant", formLayout, variantSV);
		variantEl.select(EmptyStateVariant.standard.name(), true);
		variantEl.addActionListener(FormEvent.ONCHANGE);
		
		String[] elementValues = new String[ELEMENTS.length];
		for (int i = 0; i < ELEMENTS.length; i++) {
			elementValues[i] = translate("empty.state.demo.element." + ELEMENTS[i]);
		}
		elementsEl = uifactory.addCheckboxesHorizontal("empty.state.demo.elements", formLayout, ELEMENTS, elementValues);
		for (String element : ELEMENTS) {
			elementsEl.select(element, true);
		}
		elementsEl.setEnabled("icon", false);
		elementsEl.setEnabled("title", false);
		elementsEl.addActionListener(FormEvent.ONCHANGE);
		
		String[] buttonKeys = { "1", "2", "3", "4" };
		buttonsEl = uifactory.addDropdownSingleselect("empty.state.demo.buttons", formLayout, buttonKeys, buttonKeys);
		buttonsEl.select("4", true);
		buttonsEl.addActionListener(FormEvent.ONCHANGE);
		
		String[] lengthValues = new String[LENGTHS.length];
		for (int i = 0; i < LENGTHS.length; i++) {
			lengthValues[i] = translate("empty.state.demo.length." + LENGTHS[i]);
		}
		titleLengthEl = uifactory.addDropdownSingleselect("empty.state.demo.title.length", formLayout, LENGTHS, lengthValues);
		titleLengthEl.select("medium", true);
		titleLengthEl.addActionListener(FormEvent.ONCHANGE);
		hintLengthEl = uifactory.addDropdownSingleselect("empty.state.demo.hint.length", formLayout, LENGTHS, lengthValues);
		hintLengthEl.select("medium", true);
		hintLengthEl.addActionListener(FormEvent.ONCHANGE);

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.item));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.price));

		ItemPriceTableModel tableModel = new ItemPriceTableModel(columnsModel);
		tableModel.setObjects(List.of());

		tableEl = uifactory.addTableElement(getWindowControl(), "itemPriceTable", tableModel, getTranslator(), formLayout);
	}
	
	private void updatePreview() {
		EmptyStateVariant variant = EmptyStateVariant.valueOf(variantEl.getSelectedKey());
		boolean buttons = elementsEl.isKeySelected("buttons");
		buttonsEl.setEnabled(buttons);
		hintLengthEl.setEnabled(elementsEl.isKeySelected("hint"));
		heightEl.setEnabled(variant != EmptyStateVariant.small);
		
		EmptyStateConfigBuilder builder = EmptyStateConfig.builder()
				.withVariant(variant)
				.withIconCss("o_icon_globe")
				.withMessageI18nKey("empty.state.demo.title." + titleLengthEl.getSelectedKey());
		if (elementsEl.isKeySelected("hint")) {
			builder.withHintI18nKey("empty.state.demo.hint." + hintLengthEl.getSelectedKey());
		}
		if (elementsEl.isKeySelected("link")) {
			builder.withHelp(translate("empty.state.help.additional"), "release_notes/");
		}
		if (buttons) {
			builder.withPrimaryButton("o_icon_bolt_lightning", "empty.state.button.primary.action", null);
			int numOfButtons = Integer.parseInt(buttonsEl.getSelectedKey());
			for (int i = 1; i < numOfButtons; i++) {
				builder.withSecondaryButton("o_icon_bolt_lightning", "empty.state.button.secondary.action." + i, null, "#" + i);
			}
		}
		EmptyState emptyState = EmptyStateFactory.create("preview", flc.getFormItemComponent(), this, builder.build());
		emptyState.setTranslator(getTranslator());
		
		String width = widthEl.getSelectedKey();
		int height = (int)heightEl.getValue();
		String boxStyle = switch (variant) {
			case auto, standard -> "min-height: " + height + "px;";
			case small -> "";
		};
		flc.contextPut("frameWidth", switch (width) {
			case "tablet" -> "768px";
			case "phone" -> "390px";
			default -> "100%";
		});
		flc.contextPut("boxStyle", boxStyle);
		flc.contextPut("height", height);
		flc.contextPut("previewTitle", translate("empty.state.demo.preview", variantEl.getSelectedValue(), widthEl.getSelectedValue()));
		
		EmptyStateConfig tableConfig = EmptyStateConfig.builder()
				.withVariant(variant)
				.withMessageI18nKey("empty.state.table.message")
				.withIconCss("o_icon_square_rss")
				.withHintI18nKey("empty.state.table.hint")
				.withHelp(translate("empty.state.help.additional"), "release_notes/")
				.withPrimaryButton("o_icon_search", "select", null)
				.withSecondaryButton("o_icon_add", "create", null, "create")
				.withSecondaryButton("o_icon_link", "link", null, "link")
				.build();
		tableEl.setEmptyStateConfig(tableConfig, false);
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (event instanceof EmptyState.PrimaryEvent) {
			showInfo("empty.state.event.primary");
		} else if (event instanceof EmptyState.SecondaryEvent secondaryEvent) {
			showInfo("empty.state.event.secondary", secondaryEvent.getAction());
		} else {
			super.event(ureq, source, event);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == tableEl) {
			if (event instanceof FlexiTableEmptyNextPrimaryActionEvent) {
				showInfo("empty.state.event.primary");
			} else if (event instanceof FlexiTableEmptySecondaryActionEvent actionEvent) {
				showInfo("empty.state.event.secondary", actionEvent.getAction());
			}
		} else {
			updatePreview();
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
