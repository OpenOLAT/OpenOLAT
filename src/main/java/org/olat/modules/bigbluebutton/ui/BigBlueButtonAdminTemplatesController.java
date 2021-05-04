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
package org.olat.modules.bigbluebutton.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingTemplate;
import org.olat.modules.bigbluebutton.ui.BigBlueButtonTemplateTableModel.BTemplatesCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BigBlueButtonAdminTemplatesController extends FormBasicController {
	
	private FlexiTableElement tableEl;
	private FormLink addTemplateButton;
	private BigBlueButtonTemplateTableModel tableModel;
	
	private CloseableModalController cmc;
	private DialogBoxController confirmDelete;
	private EditBigBlueButtonTemplateController editTemplateCtlr;
	
	private final boolean readOnly;
	
	@Autowired
	private BigBlueButtonManager bigBlueButtonManager;
	
	public BigBlueButtonAdminTemplatesController(UserRequest ureq, WindowControl wControl, boolean readOnly) {
		super(ureq, wControl, "templates_admin");
		this.readOnly = readOnly;
		initForm(ureq);
		updateModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		addTemplateButton = uifactory.addFormLink("add.template", formLayout, Link.BUTTON);
		addTemplateButton.setVisible(!readOnly);
		
		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BTemplatesCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BTemplatesCols.system));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BTemplatesCols.enabled));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BTemplatesCols.maxConcurrentMeetings));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BTemplatesCols.maxParticipants));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BTemplatesCols.maxDuration,
				new TemplateMinuteCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BTemplatesCols.webcamsOnlyForModerator));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BTemplatesCols.externalUsers));
		if(readOnly) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("view", translate("view"), "view"));
		} else {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("edit", translate("edit"), "edit"));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("delete", BTemplatesCols.system.ordinal(), "delete",
					new BooleanCellRenderer(null, new StaticFlexiCellRenderer(translate("delete"), "delete"))));
		}
		
		tableModel = new BigBlueButtonTemplateTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "templates", tableModel, getTranslator(), formLayout);
		tableEl.setEmptyTableMessageKey("no.template.configured");
		tableEl.setAndLoadPersistedPreferences(ureq, "bigbluebutton-connect-edit-templates-list");
	}

	@Override
	protected void doDispose() {
		//
	}
	
	public void updateModel() {
		List<BigBlueButtonMeetingTemplate> templates = bigBlueButtonManager.getTemplates();
		tableModel.setObjects(templates);
		tableEl.reset(true, true, true);	
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == editTemplateCtlr) {
			if(event == Event.DONE_EVENT) {
				updateModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(confirmDelete == source) {
			if(DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				BigBlueButtonMeetingTemplate meeting = (BigBlueButtonMeetingTemplate)confirmDelete.getUserObject();
				doDelete(meeting);
			}
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editTemplateCtlr);
		removeAsListenerAndDispose(confirmDelete);
		removeAsListenerAndDispose(cmc);
		editTemplateCtlr = null;
		confirmDelete = null;
		cmc = null;
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addTemplateButton == source) {
			doAddTemplate(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("edit".equals(se.getCommand()) || "view".equals(se.getCommand())) {
					doEditTemplate(ureq, tableModel.getObject(se.getIndex()));
				} else if("delete".equals(se.getCommand())) {
					doConfirmDelete(ureq, tableModel.getObject(se.getIndex()));
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	

	private void doAddTemplate(UserRequest ureq) {
		if(guardModalController(editTemplateCtlr)) return;
		
		editTemplateCtlr = new EditBigBlueButtonTemplateController(ureq, getWindowControl());
		listenTo(editTemplateCtlr);
		
		cmc = new CloseableModalController(getWindowControl(), "close", editTemplateCtlr.getInitialComponent(),
				true, translate("add.template"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doEditTemplate(UserRequest ureq, BigBlueButtonMeetingTemplate template) {
		if(guardModalController(editTemplateCtlr)) return;
		
		editTemplateCtlr = new EditBigBlueButtonTemplateController(ureq, getWindowControl(), template, readOnly);
		listenTo(editTemplateCtlr);
		
		String title;
		if(readOnly) {
			title = translate("view.template", new String[] { template.getName() });
		} else {
			title = translate("edit.template", new String[] { template.getName() });
		}
		cmc = new CloseableModalController(getWindowControl(), "close", editTemplateCtlr.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doConfirmDelete(UserRequest ureq, BigBlueButtonMeetingTemplate template) {
		if(bigBlueButtonManager.isTemplateInUse(template)) {
			showWarning("warning.template.in.use");
		} else {
			String confirmDeleteTitle = translate("confirm.delete.template.title", new String[]{ template.getName() });
			String confirmDeleteText = translate("confirm.delete.template", new String[]{ template.getName() });
			confirmDelete = activateYesNoDialog(ureq, confirmDeleteTitle, confirmDeleteText, confirmDelete);
			confirmDelete.setUserObject(template);
		}
	}
	
	private void doDelete(BigBlueButtonMeetingTemplate template) {
		if(!bigBlueButtonManager.isTemplateInUse(template)) {
			bigBlueButtonManager.deleteTemplate(template);
			updateModel();
		}
	}
}
