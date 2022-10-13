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
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.ceditor.ContentEditorModule;
import org.olat.modules.ceditor.ContentEditorXStream;
import org.olat.modules.ceditor.PageElementInspectorController;
import org.olat.modules.ceditor.PageElementStore;
import org.olat.modules.ceditor.model.TableContent;
import org.olat.modules.ceditor.model.TableElement;
import org.olat.modules.ceditor.model.TableSettings;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 août 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TableInspectorController extends FormBasicController implements PageElementInspectorController {

	private static final String[] onKeys = new String[] { "on" };
	
	private TextElement rowsEl;
	private TextElement columnsEl;
	private SingleSelection styleEl;

	private MultipleSelectionElement stripedEl;
	private MultipleSelectionElement borderedEl;
	private MultipleSelectionElement rowHeaderEl;
	private MultipleSelectionElement columnHeaderEl;
	
	private TableElement table;
	private final PageElementStore<TableElement> store;

	@Autowired
	private DB dbInstance;
	@Autowired
	private ContentEditorModule contentEditorModule;
	
	public TableInspectorController(UserRequest ureq, WindowControl wControl,
			TableElement table, PageElementStore<TableElement> store) {
		super(ureq, wControl, "table_inspector");
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
		TableContent content = table.getTableContent();
		int numOfRows = content.getNumOfRows() < 1 ? 3 : content.getNumOfRows();
		int numOfColumns = content.getNumOfColumns() < 1 ? 4 : content.getNumOfColumns();

		rowsEl = uifactory.addTextElement("table.row", null, 4, Integer.toString(numOfRows), formLayout);
		rowsEl.addActionListener(FormEvent.ONCHANGE);
		columnsEl = uifactory.addTextElement("table.column", null, 4, Integer.toString(numOfColumns), formLayout);
		columnsEl.addActionListener(FormEvent.ONCHANGE);
		
		TableSettings settings = table.getTableSettings();
		flc.contextPut("settings", settings);
		
		String[] rowHeaderValues = new String[] { translate("table.row.header") };
		rowHeaderEl = uifactory.addCheckboxesHorizontal("table.row.header", "table.row.header", formLayout, onKeys, rowHeaderValues);
		rowHeaderEl.addActionListener(FormEvent.ONCHANGE);
		rowHeaderEl.select(onKeys[0], settings.isRowHeaders());
		
		String[] columnHeaderValues = new String[] { translate("table.column.header") };
		columnHeaderEl = uifactory.addCheckboxesHorizontal("table.column.header", "table.column.header", formLayout, onKeys, columnHeaderValues);
		columnHeaderEl.addActionListener(FormEvent.ONCHANGE);
		columnHeaderEl.select(onKeys[0], settings.isColumnHeaders());
		
		String[] stripedValues = new String[] { translate("table.style.striped") };
		stripedEl = uifactory.addCheckboxesHorizontal("table.style.striped", "table.style.striped", formLayout, onKeys, stripedValues);
		stripedEl.addActionListener(FormEvent.ONCHANGE);
		stripedEl.select(onKeys[0], settings.isStriped());
		
		String[] borderedValues = new String[] { translate("table.style.bordered") };
		borderedEl = uifactory.addCheckboxesHorizontal("table.style.bordered", "table.style.bordered", formLayout, onKeys, borderedValues);
		borderedEl.addActionListener(FormEvent.ONCHANGE);
		borderedEl.select(onKeys[0], settings.isBordered());
		
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
		styleEl = uifactory.addDropdownSingleselect("table.style", "table.style", formLayout, styles, stylesValues, null);
		styleEl.addActionListener(FormEvent.ONCHANGE);
		String settingsTableStyle = settings.getTableStyle();
		if(StringHelper.containsNonWhitespace(settingsTableStyle)) {
			for(String style:styles) {
				if(settingsTableStyle.equals(style)) {
					styleEl.select(style, true);
				}
			}
		} else {
			styleEl.select(styles[0], true);
		}
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
		if(rowHeaderEl == source || columnHeaderEl == source || rowsEl == source || columnsEl == source
				|| styleEl == source || stripedEl == source || borderedEl == source) {
			doSaveSettings(ureq);
		}
		super.formInnerEvent(ureq, source, event);
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
	
	private void doSaveSettings(UserRequest ureq) {
		TableContent content = table.getTableContent();
		TableSettings settings = table.getTableSettings();
		
		settings.setRowHeaders(rowHeaderEl.isAtLeastSelected(1));
		settings.setColumnHeaders(columnHeaderEl.isAtLeastSelected(1));
		settings.setStriped(stripedEl.isAtLeastSelected(1));
		settings.setBordered(borderedEl.isAtLeastSelected(1));
		if(styleEl.isOneSelected() && !styleEl.isSelected(0)) {
			settings.setTableStyle(styleEl.getSelectedKey());
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
		fireEvent(ureq, new ChangePartEvent(table));	
	}
}
