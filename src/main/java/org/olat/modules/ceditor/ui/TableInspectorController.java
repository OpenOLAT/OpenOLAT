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
package org.olat.modules.ceditor.ui;

import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.color.ColorService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.tabbedpane.TabbedPaneItem;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.ceditor.ContentEditorModule;
import org.olat.modules.ceditor.ContentEditorXStream;
import org.olat.modules.ceditor.PageElementInspectorController;
import org.olat.modules.ceditor.PageElementStore;
import org.olat.modules.ceditor.model.AlertBoxSettings;
import org.olat.modules.ceditor.model.BlockLayoutSettings;
import org.olat.modules.ceditor.model.TableContent;
import org.olat.modules.ceditor.model.TableElement;
import org.olat.modules.ceditor.model.TableSettings;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.cemedia.ui.MediaUIHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 août 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TableInspectorController extends FormBasicController implements PageElementInspectorController {

	private static final String KEY_ROW_HEADER = "row.header";
	private static final String KEY_COLUMN_HEADER = "column.header";
	private static final String KEY_STRIPED = "striped";
	private static final String KEY_BORDERED = "bordered";
	
	private TabbedPaneItem tabbedPane;
	private MediaUIHelper.LayoutTabComponents layoutTabComponents;
	private MediaUIHelper.AlertBoxComponents alertBoxComponents;

	private TextElement rowsEl;
	private TextElement columnsEl;
	private MultipleSelectionElement dataGridEl;
	private MultipleSelectionElement rowStyleEl;
	private SingleSelection colorEl;
	
	private TableElement table;
	private final PageElementStore<TableElement> store;

	@Autowired
	private DB dbInstance;
	@Autowired
	private ContentEditorModule contentEditorModule;
	@Autowired
	private ColorService colorService;

	public TableInspectorController(UserRequest ureq, WindowControl wControl,
			TableElement table, PageElementStore<TableElement> store) {
		super(ureq, wControl, "tabs_inspector");
		this.table = table;
		this.store = store;
		initForm(ureq);
	}

	@Override
	public String getTitle() {
		return translate("inspector.table");
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		tabbedPane = uifactory.addTabbedPane("tabPane", getLocale(), formLayout);
		tabbedPane.setTabIndentation(TabbedPaneItem.TabIndentation.none);
		formLayout.add("tabs", tabbedPane);
		
		addGeneralTab(formLayout);
		addStyleTab(formLayout);
		addLayoutTab(formLayout);
	}

	private void addGeneralTab(FormItemContainer formLayout) {
		FormLayoutContainer layoutCont = FormLayoutContainer.createVerticalFormLayout("general", getTranslator());
		formLayout.add(layoutCont);
		tabbedPane.addTab(getTranslator().translate("tab.general"), layoutCont);

		TableContent content = table.getTableContent();
		int numOfRows = content.getNumOfRows() < 1 ? 3 : content.getNumOfRows();
		int numOfColumns = content.getNumOfColumns() < 1 ? 4 : content.getNumOfColumns();

		rowsEl = uifactory.addTextElement("table.row", "table.row", 4, Integer.toString(numOfRows), layoutCont);
		rowsEl.addActionListener(FormEvent.ONCHANGE);
		columnsEl = uifactory.addTextElement("table.column", "table.column", 4, Integer.toString(numOfColumns), layoutCont);
		columnsEl.addActionListener(FormEvent.ONCHANGE);

		TableSettings settings = table.getTableSettings();
		flc.contextPut("settings", settings);

		SelectionValues dataGridSV = new SelectionValues();
		dataGridSV.add(SelectionValues.entry(KEY_ROW_HEADER, translate("table.row.header")));
		dataGridSV.add(SelectionValues.entry(KEY_COLUMN_HEADER, translate("table.column.header")));
		dataGridEl = uifactory.addCheckboxesVertical("table.data.grid", "table.data.grid", layoutCont, dataGridSV.keys(), dataGridSV.values(), 1);
		dataGridEl.addActionListener(FormEvent.ONCHANGE);
		dataGridEl.select(KEY_ROW_HEADER, settings.isRowHeaders());
		dataGridEl.select(KEY_COLUMN_HEADER, settings.isColumnHeaders());

		SelectionValues rowStyleSV = new SelectionValues();
		rowStyleSV.add(SelectionValues.entry(KEY_STRIPED, translate("table.style.striped")));
		rowStyleSV.add(SelectionValues.entry(KEY_BORDERED, translate("table.style.bordered")));
		rowStyleEl = uifactory.addCheckboxesVertical("table.row.style", "table.row.style", layoutCont, rowStyleSV.keys(), rowStyleSV.values(), 1);
		rowStyleEl.addActionListener(FormEvent.ONCHANGE);
		rowStyleEl.select(KEY_STRIPED, settings.isStriped());
		rowStyleEl.select(KEY_BORDERED, settings.isBordered());

		List<String> styleList = contentEditorModule.getTableStyleList();
		String[] styles = new String[styleList.size() + 1];
		String[] stylesValues = new String[styles.length];
		styles[0] = "-";
		stylesValues[0] = translate("table.style.none");
		for(int i=styleList.size(); i-->0; ) {
			int si = i + 1;
			styles[si] = styleList.get(i);
			String stylename = translate("table.style." + styles[si]);
			if(stylename.length() < 32 && !stylename.startsWith("table.")) {
				stylesValues[si] = stylename;
			} else {
				stylesValues[si] = styles[si];
			}
		}
		colorEl = uifactory.addDropdownSingleselect("table.style.color", "table.style.color", layoutCont, styles, stylesValues, null);
		colorEl.addActionListener(FormEvent.ONCHANGE);
		String settingsTableStyle = settings.getTableStyle();
		if(StringHelper.containsNonWhitespace(settingsTableStyle)) {
			for(String style:styles) {
				if(settingsTableStyle.equals(style)) {
					colorEl.select(style, true);
				}
			}
		} else {
			colorEl.select(styles[0], true);
		}
	}

	private void addStyleTab(FormItemContainer formLayout) {
		alertBoxComponents = MediaUIHelper.addAlertBoxStyleTab(formLayout, tabbedPane, uifactory,
				getAlertBoxSettings(getTableSettings()), colorService, getLocale());
	}

	private void addLayoutTab(FormItemContainer formLayout) {
		BlockLayoutSettings layoutSettings = getLayoutSettings(getTableSettings());
		layoutTabComponents = MediaUIHelper.addLayoutTab(formLayout, tabbedPane, getTranslator(), uifactory, layoutSettings, velocity_root);
	}

	private BlockLayoutSettings getLayoutSettings(TableSettings tableSettings) {
		if (tableSettings.getLayoutSettings() != null) {
			return tableSettings.getLayoutSettings();
		}
		return BlockLayoutSettings.getPredefined();
	}

	private AlertBoxSettings getAlertBoxSettings(TableSettings tableSettings) {
		if (tableSettings.getAlertBoxSettings() != null) {
			return tableSettings.getAlertBoxSettings();
		}
		return AlertBoxSettings.getPredefined();
	}

	private TableSettings getTableSettings() {
		if (table.getTableSettings() != null) {
			return table.getTableSettings();
		}
		return new TableSettings();
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source instanceof TableEditorController && event instanceof ChangePartEvent) {
			ChangePartEvent cpe = (ChangePartEvent)event;
			if(cpe.isElement(table)) {
				table = (TableElement)cpe.getElement();
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (dataGridEl == source || rowsEl == source || columnsEl == source || colorEl == source
				|| rowStyleEl == source) {
			doValidateAndSave(ureq);
		} else if (layoutTabComponents.matches(source)) {
			doChangeLayout(ureq);
		} else if (alertBoxComponents.matches(source)) {
			doChangeAlertBoxSettings(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		rowsEl.clearError();
		if(!StringHelper.containsNonWhitespace(rowsEl.getValue())) {
			rowsEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else if (!StringHelper.isLong(rowsEl.getValue())) {
			rowsEl.setErrorKey("form.error.positive.integer");
			allOk &= false;
		}
		
		columnsEl.clearError();
		if(!StringHelper.containsNonWhitespace(columnsEl.getValue())) {
			columnsEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else if (!StringHelper.isLong(columnsEl.getValue())) {
			columnsEl.setErrorKey("form.error.positive.integer");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent fe) {
		if(!(fiSrc instanceof TextElement)) {
			super.propagateDirtinessToContainer(fiSrc, fe);
		}
	}
	
	private void doValidateAndSave(UserRequest ureq) {
		if(validateFormLogic(ureq)) {
			doSave();
			fireEvent(ureq, new ChangePartEvent(table));
		}
	}
	
	private void doSave() {
		TableContent content = table.getTableContent();
		TableSettings settings = table.getTableSettings();
		
		settings.setRowHeaders(dataGridEl.isKeySelected(KEY_ROW_HEADER));
		settings.setColumnHeaders(dataGridEl.isKeySelected(KEY_COLUMN_HEADER));
		settings.setStriped(rowStyleEl.isKeySelected(KEY_STRIPED));
		settings.setBordered(rowStyleEl.isKeySelected(KEY_BORDERED));
		if(colorEl.isOneSelected() && !colorEl.isSelected(0)) {
			settings.setTableStyle(colorEl.getSelectedKey());
		} else {
			settings.setTableStyle(null);
		}
		String settingsXml = ContentEditorXStream.toXml(settings);
		table.setLayoutOptions(settingsXml);

		String rows = rowsEl.getValue();
		if(StringHelper.isLong(rows)) {
			int numOfRows = Integer.parseInt(rows);
			content.setNumOfRows(numOfRows);
		}
		String columns = columnsEl.getValue();
		if(StringHelper.isLong(columns)) {
			int numOfColumns = Integer.parseInt(columns);
			content.setNumOfColumns(numOfColumns);
		}
		
		String contentXml = ContentEditorXStream.toXml(content);
		table.setContent(contentXml);
		table = store.savePageElement(table);
		dbInstance.commit();
	}

	private void doChangeLayout(UserRequest ureq) {
		TableSettings tableSettings = getTableSettings();

		BlockLayoutSettings layoutSettings = getLayoutSettings(tableSettings);
		layoutTabComponents.sync(layoutSettings);
		tableSettings.setLayoutSettings(layoutSettings);

		String settingsXml = ContentEditorXStream.toXml(tableSettings);
		table.setLayoutOptions(settingsXml);

		table = store.savePageElement(table);
		dbInstance.commit();
		fireEvent(ureq, new ChangePartEvent(table));

		getInitialComponent().setDirty(true);
	}

	private void doChangeAlertBoxSettings(UserRequest ureq) {
		TableSettings tableSettings = getTableSettings();

		AlertBoxSettings alertBoxSettings = getAlertBoxSettings(tableSettings);
		alertBoxComponents.sync(alertBoxSettings);
		tableSettings.setAlertBoxSettings(alertBoxSettings);

		String settingsXml = ContentEditorXStream.toXml(tableSettings);
		table.setLayoutOptions(settingsXml);
		table = store.savePageElement(table);
		dbInstance.commit();
		fireEvent(ureq, new ChangePartEvent(table));

		getInitialComponent().setDirty(true);
	}

}
