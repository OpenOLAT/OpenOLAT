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
package org.olat.ims.lti13.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.ims.lti13.LTI13Service;
import org.olat.ims.lti13.LTI13Tool;
import org.olat.ims.lti13.LTI13ToolType;
import org.olat.ims.lti13.ui.LTI13AdminToolsTableModel.ToolsCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22 févr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LTI13AdminExternalToolsController extends FormBasicController {
	
	private FormLink addToolButton;
	private FlexiTableElement tableEl;
	private LTI13AdminToolsTableModel tableModel;
	
	private CloseableModalController cmc;
	private LTI13EditToolController editToolCtrl;
	
	@Autowired
	private LTI13Service lti13Service;
	
	public LTI13AdminExternalToolsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "admin_tools");
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		addToolButton = uifactory.addFormLink("add.tool", formLayout, Link.BUTTON);
		addToolButton.setIconLeftCSS("o_icon o_icon_add");
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ToolsCols.toolName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ToolsCols.toolUrl));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ToolsCols.clientId));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("edit", translate("edit"), "edit"));
		
		tableModel = new LTI13AdminToolsTableModel(columnsModel, getLocale());
		
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 24, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(true);
		tableEl.setEmptyTableMessageKey("tools.empty");
		tableEl.setAndLoadPersistedPreferences(ureq, "lti13-tools-admin");
	}
	
	private void loadModel() {
		List<LTI13Tool> tools = lti13Service.getTools(LTI13ToolType.EXT_TEMPLATE);
		List<ToolRow> rows = new ArrayList<>(tools.size());
		for(LTI13Tool tool:tools) {
			rows.add(new ToolRow(tool.getKey(), tool.getToolName(), tool.getToolUrl(), tool.getClientId()));
		}
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(editToolCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeControllerListener(editToolCtrl);
		removeControllerListener(cmc);
		editToolCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addToolButton == source) {
			doAddTool(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("edit".equals(se.getCommand())) {
					doEditTool(ureq, tableModel.getObject(se.getIndex()));
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doAddTool(UserRequest ureq) {
		if(guardModalController(editToolCtrl)) return;

		editToolCtrl = new LTI13EditToolController(ureq, getWindowControl(), LTI13ToolType.EXT_TEMPLATE);
		listenTo(editToolCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", editToolCtrl.getInitialComponent(),
				true, translate("add.tool"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doEditTool(UserRequest ureq, ToolRow row) {
		if(guardModalController(editToolCtrl)) return;

		LTI13Tool tool = this.lti13Service.getToolByKey(row.getKey());
		editToolCtrl = new LTI13EditToolController(ureq, getWindowControl(), tool);
		listenTo(editToolCtrl);
		
		String title = translate("edit.tool", new String[] { row.getToolName() });
		cmc = new CloseableModalController(getWindowControl(), "close", editToolCtrl.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);
	}

}
