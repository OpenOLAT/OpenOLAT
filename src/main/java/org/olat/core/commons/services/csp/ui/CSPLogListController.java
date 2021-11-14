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
package org.olat.core.commons.services.csp.ui;

import org.olat.core.commons.services.csp.CSPLog;
import org.olat.core.commons.services.csp.ui.CSPLogTableModel.CSPCols;
import org.olat.core.commons.services.csp.ui.event.NextEntryEvent;
import org.olat.core.commons.services.csp.ui.event.PreviousEntryEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.stack.BreadcrumbPanelAware;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;

/**
 * 
 * Initial date: 19 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CSPLogListController extends FormBasicController implements BreadcrumbPanelAware {
	
	private CSPLogDataSource dataSource;
	private FlexiTableElement tableEl;
	private CSPLogTableModel tableModel;
	private TooledStackedPanel stackPanel;
	
	private CSPLogEntryController logEntryCtrl;

	public CSPLogListController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "log_list");
		
		initForm(ureq);
		loadModel();
	}

	@Override
	public void setBreadcrumbPanel(BreadcrumbPanel stackPanel) {
		this.stackPanel = (TooledStackedPanel)stackPanel;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CSPCols.key, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CSPCols.creationDate, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CSPCols.effectiveDirective));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CSPCols.blockedUri));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CSPCols.documentUri));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CSPCols.referrer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CSPCols.sourceFile));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CSPCols.lineNumber));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CSPCols.columnNumber));
		DefaultFlexiColumnModel selectCol = new DefaultFlexiColumnModel("select", translate("select"), "select");
		selectCol.setExportable(false);
		columnsModel.addFlexiColumnModel(selectCol);
		
		dataSource = new CSPLogDataSource();
		tableModel = new CSPLogTableModel(dataSource, columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setAndLoadPersistedPreferences(ureq, "csp-log-list");
		tableEl.setCustomizeColumns(true);
		tableEl.setPageSize(25);
		tableEl.setExportEnabled(true);
	}
	
	private void loadModel() {
		dataSource.reset();
		tableEl.reset(true, true, true);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == logEntryCtrl) {
			if(event == Event.CANCELLED_EVENT) {
				stackPanel.setToolbarEnabled(false);
				stackPanel.popController(logEntryCtrl);
			} else if(event instanceof NextEntryEvent) {
				doNext(ureq, ((NextEntryEvent)event).getEntry());
			} else if(event instanceof PreviousEntryEvent) {
				doPrevious(ureq, ((NextEntryEvent)event).getEntry());
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				CSPLog row = tableModel.getObject(se.getIndex());
				if("select".equals(cmd)) {
					doOpenLogEntry(ureq, row);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doNext(UserRequest ureq, CSPLog currentEntry) {
		CSPLog nextLogEntry = tableModel.getNextObject(currentEntry, tableEl);
		if(nextLogEntry != null) {
			doOpenLogEntry(ureq, nextLogEntry);
		}
	}
	
	private void doPrevious(UserRequest ureq, CSPLog currentEntry) {
		CSPLog previousLogEntry = tableModel.getPreviousObject(currentEntry, tableEl);
		if(previousLogEntry != null) {
			doOpenLogEntry(ureq, previousLogEntry);
		}
	}
	
	private void doOpenLogEntry(UserRequest ureq, CSPLog logEntry) {
		if(logEntryCtrl != null) {
			stackPanel.popController(logEntryCtrl);
			removeAsListenerAndDispose(logEntryCtrl);
		}
		
		logEntryCtrl = new CSPLogEntryController(ureq, getWindowControl(), stackPanel, logEntry);
		listenTo(logEntryCtrl);
		stackPanel.pushController("Details", logEntryCtrl);
	}
}