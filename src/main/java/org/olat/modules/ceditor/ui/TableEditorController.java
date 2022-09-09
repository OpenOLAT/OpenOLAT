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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.ceditor.ContentEditorXStream;
import org.olat.modules.ceditor.PageElementEditorController;
import org.olat.modules.ceditor.PageElementStore;
import org.olat.modules.ceditor.model.TableContent;
import org.olat.modules.ceditor.model.TableElement;
import org.olat.modules.ceditor.model.TableSettings;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;

/**
 * 
 * Initial date: 19 sept. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TableEditorController extends FormBasicController implements PageElementEditorController {

	private TextElement titleEl;
	private TextElement captionEl;

	private int currentNumOfRows;
	private int currentNumOfColumns;
	private TableElement table;
	private final PageElementStore<TableElement> store;
	private final List<EditorRow> rowList = new ArrayList<>();
	
	public TableEditorController(UserRequest ureq, WindowControl wControl,
			TableElement table, PageElementStore<TableElement> store) {
		super(ureq, wControl, "table_editor");
		this.table = table;
		this.store = store;
		
		initForm(ureq);
		
		TableContent content = table.getTableContent();
		currentNumOfRows = content.getNumOfRows() < 1 ? 3 : content.getNumOfRows();
		currentNumOfColumns = content.getNumOfColumns() < 1 ? 4 : content.getNumOfColumns();
		loadModel(currentNumOfRows, currentNumOfColumns);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		TableContent content = table.getTableContent();
		currentNumOfRows = content.getNumOfRows() < 1 ? 3 : content.getNumOfRows();
		currentNumOfColumns = content.getNumOfColumns() < 1 ? 4 : content.getNumOfColumns();

		if(formLayout instanceof FormLayoutContainer) {
			TableSettings settings = table.getTableSettings();
			((FormLayoutContainer)formLayout).contextPut("settings", settings);
		}

		titleEl = uifactory.addTextElement("table.title", null, 32000, content.getTitle(), formLayout);
		titleEl.setPlaceholderKey("table.title.placeholder", null);
		titleEl.setElementCssClass("h4");
		titleEl.addActionListener(FormEvent.ONCHANGE);
		captionEl = uifactory.addTextElement("table.caption", null, 32000, content.getCaption(), formLayout);
		captionEl.setPlaceholderKey("table.caption.placeholder", null);
		captionEl.setElementCssClass("o_caption");
		captionEl.addActionListener(FormEvent.ONCHANGE);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source instanceof TableInspectorController && event instanceof ChangePartEvent) {
			ChangePartEvent cpe = (ChangePartEvent)event;
			if(cpe.isElement(table)) {
				table = (TableElement)cpe.getElement();
				doUpdate();
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof TextElement) {
			doSaveSettings(ureq, false);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent fe) {
		//
	}
	
	private void doUpdate() {
		TableContent content = table.getTableContent();
		currentNumOfRows = content.getNumOfRows() < 1 ? 3 : content.getNumOfRows();
		currentNumOfColumns = content.getNumOfColumns() < 1 ? 4 : content.getNumOfColumns();
		loadModel(currentNumOfRows, currentNumOfColumns);
		
		// Live preview
		TableSettings settings = table.getTableSettings();
		flc.contextPut("settings", settings); 
	}

	private void loadModel(int numOfRows, int numOfColumns) {
		TableSettings settings = table.getTableSettings();
		TableContent content = table.getTableContent();
		for(int i=0; i<numOfRows; i++) {
			final EditorRow row;
			if(i < rowList.size()) {
				row = rowList.get(i);
			} else {
				row = new EditorRow(i, new ArrayList<>(numOfColumns));
				rowList.add(row);
			}

			final List<EditorColumn> columnList = row.getColumns();
			for(int j=0; j<numOfColumns; j++) {
				boolean header = (settings.isRowHeaders() && i == 0)
						|| (settings.isColumnHeaders() && j == 0);
				
				if(j >= columnList.size()) {
					String text = content.getContent(i, j);
					String textElId = new StringBuilder().append("text_").append(i).append("_").append(j).toString();
					TextElement textEl = uifactory.addTextElement(textElId, null, 32000, text, flc);
					textEl.addActionListener(FormEvent.ONCHANGE);
					
					EditorColumn column = new EditorColumn(j, textEl, header);
					columnList.add(column);
				} else {
					columnList.get(j).setHeader(header);
				}
			}
			
			if(columnList.size() > numOfColumns) {
				for(int r=columnList.size(); r>numOfColumns; r--) {
					columnList.remove(r - 1);
				}
			}
		}
		
		if(rowList.size() > numOfRows) {
			for( int r=rowList.size(); r>numOfRows; r--) {
				rowList.remove(r - 1);
			}
		}
		
		flc.contextPut("tableRows", rowList);
	}
	
	private void doSaveSettings(UserRequest ureq, boolean dirty) {
		TableSettings settings = table.getTableSettings();
		TableContent content = table.getTableContent();
		int numOfRows = currentNumOfRows;
		int numOfColumns = currentNumOfColumns;
	
		if(numOfColumns != currentNumOfColumns || numOfRows != currentNumOfRows) {
			loadModel(numOfRows, numOfColumns);
			flc.setDirty(true);
			currentNumOfRows = numOfRows;
			currentNumOfColumns = numOfColumns;
		}
		
		for(EditorRow row:rowList) {
			int i = row.getRow();
			for(EditorColumn col:row.getColumns()) {
				String text = col.getText().getValue();
				content.addContent(row.getRow(), col.getColumn(), text);
				if(!dirty) {
					col.getText().getComponent().setDirty(false);
				}
				boolean header = (settings.isColumnHeaders() && i == 0)
						|| (settings.isRowHeaders() && col.getColumn() == 0);
				col.setHeader(header);
			}
		}
		content.setCaption(captionEl.getValue());
		content.setTitle(titleEl.getValue());
		if(!dirty) {
			captionEl.getComponent().setDirty(false);
			titleEl.getComponent().setDirty(false);
		}
		
		String contentXml = ContentEditorXStream.toXml(content);
		table.setContent(contentXml);
		table = store.savePageElement(table);
		fireEvent(ureq, new ChangePartEvent(table));	
	}
	
	public class EditorRow {
		
		private final int row;
		private final List<EditorColumn> columns;
		
		public EditorRow(int row, List<EditorColumn> columns) {
			this.row = row;
			this.columns = columns;
		}
		
		public int getRow() {
			return row;
		}
		
		public List<EditorColumn> getColumns() {
			return columns;
		}
	}
	
	public class EditorColumn {
		private final int column;
		private boolean header;
		private TextElement textEl;
		
		public EditorColumn(int column, TextElement textEl, boolean header) {
			this.column = column;
			this.textEl = textEl;
			this.header = header;
		}
		
		public boolean isHeader() {
			return header;
		}
		
		public void setHeader(boolean header) {
			this.header = header;
		}
		
		public int getColumn() {
			return column;
		}
		
		public TextElement getText() {
			return textEl;
		}
	}
}
