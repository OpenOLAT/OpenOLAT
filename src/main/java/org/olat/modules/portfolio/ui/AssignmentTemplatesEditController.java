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
package org.olat.modules.portfolio.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.modules.forms.handler.EvaluationFormResource;
import org.olat.modules.portfolio.Assignment;
import org.olat.modules.portfolio.AssignmentType;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.manager.PortfolioFileStorage;
import org.olat.modules.portfolio.ui.AssignmentTemplatesDataModel.TemplateCols;
import org.olat.modules.portfolio.ui.component.AssignmentTypeCellRenderer;
import org.olat.modules.portfolio.ui.model.AssignmentTemplateRow;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13 Nov 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssignmentTemplatesEditController extends FormBasicController {
	
	private FormLink addFormButton;
	private FormLink addDocumentButton;
	
	private FlexiTableElement tableEl;
	private AssignmentTemplatesDataModel tableModel;

	private CloseableModalController cmc;
	private DialogBoxController confirmDeleteCtrl;
	private AssignmentEditController editAssignmentCtrl;
	private AddAssignmentDocumentController addDocumentCtrl;
	private ReferencableEntriesSearchController searchFormCtrl;
	
	private Binder binder;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PortfolioService portfolioService;
	@Autowired
	private PortfolioFileStorage portfolioFileStorage;
	
	public AssignmentTemplatesEditController(UserRequest ureq, WindowControl wControl, Binder binder) {
		super(ureq, wControl, "templates_edit");
		this.binder = binder;
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		addFormButton = uifactory.addFormLink("add.template.form", formLayout, Link.BUTTON);
		addFormButton.setElementCssClass("o_sel_pf_add_template_form");
		addFormButton.setIconLeftCSS("o_icon o_icon_add");
		
		addDocumentButton = uifactory.addFormLink("add.template.document", formLayout, Link.BUTTON);
		addDocumentButton.setElementCssClass("o_sel_pf_add_template_doc");
		addDocumentButton.setIconLeftCSS("o_icon o_icon_add");
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TemplateCols.type, new AssignmentTypeCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TemplateCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TemplateCols.creationDate, new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("edit", translate("edit"), "edit"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("delete", translate("delete"), "delete"));
		
		tableModel = new AssignmentTemplatesDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 24, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(false);
		tableEl.setAndLoadPersistedPreferences(ureq, "portfolio-assignments-templates");
	}
	
	private void loadModel() {
		List<Assignment> assignments = portfolioService.getBindersAssignmentsTemplates(binder);
		List<AssignmentTemplateRow> rows = assignments.stream()
				.map(this::forgeRow).collect(Collectors.toList());
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private AssignmentTemplateRow forgeRow(Assignment assignment) {
		return new AssignmentTemplateRow(assignment);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (editAssignmentCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if (confirmDeleteCtrl == source) {
			if (DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				doDeleteAssignment((Assignment)confirmDeleteCtrl.getUserObject());
			}
			cleanUp();
		} else if(searchFormCtrl == source) {
			if(event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
				doSelectForm(searchFormCtrl.getSelectedEntry());
			}
			cmc.deactivate();
			cleanUp();
		} else if(addDocumentCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doSelectDocument(addDocumentCtrl.getUploadedFileName(), addDocumentCtrl.getUploadedFile());
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addFormButton == source) {
			doSelectForm(ureq);
		} else if(addDocumentButton == source) {
			doSelectDocument(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("delete".equals(se.getCommand())) {
					AssignmentTemplateRow row = tableModel.getObject(se.getIndex());
					confirmDeleteAssignment(ureq, row.getAssignment());
				} else if("edit".equals(se.getCommand())) {
					AssignmentTemplateRow row = tableModel.getObject(se.getIndex());
					doEditAssignment(ureq, row.getAssignment());
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editAssignmentCtrl);
		removeAsListenerAndDispose(confirmDeleteCtrl);
		removeAsListenerAndDispose(addDocumentCtrl);
		removeAsListenerAndDispose(searchFormCtrl);
		removeAsListenerAndDispose(cmc);
		editAssignmentCtrl = null;
		confirmDeleteCtrl = null;
		addDocumentCtrl = null;
		searchFormCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doSelectDocument(UserRequest ureq) {
		if(guardModalController(addDocumentCtrl)) return;

		addDocumentCtrl = new AddAssignmentDocumentController(ureq, getWindowControl());
		listenTo(addDocumentCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), addDocumentCtrl.getInitialComponent(),
				true, translate("select.document"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doSelectDocument(String uploadedFileName, File uploadedFile) {
		Assignment assignment = portfolioService.addAssignment(uploadedFileName, null, null, AssignmentType.document, true, null, binder, true, false, false, null);

		VFSContainer container = portfolioFileStorage.getAssignmentContainer(assignment);
		if (container != null) {
			try(InputStream in = new FileInputStream(uploadedFile)) {
				VFSLeaf storedFile = container.createChildLeaf(uploadedFileName);
				VFSManager.copyContent(in, storedFile, getIdentity());
			} catch (Exception e) {
				logError("", e);
			}
		}

		dbInstance.commit();
		loadModel();
	}
	
	private void doSelectForm(UserRequest ureq) {
		if(guardModalController(searchFormCtrl)) return;

		searchFormCtrl = new ReferencableEntriesSearchController(getWindowControl(), ureq, 
					EvaluationFormResource.TYPE_NAME, translate("select.form"));
		listenTo(searchFormCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), searchFormCtrl.getInitialComponent(),
				true, translate("select.form"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doSelectForm(RepositoryEntry formEntry) {
		String title = formEntry.getDisplayname();
		portfolioService.addAssignment(title, null, null, AssignmentType.form, true, null, binder, true, false, false, formEntry);
		dbInstance.commit();
		loadModel();
	}
	
	private void doEditAssignment(UserRequest ureq, Assignment assignment) {
		if(guardModalController(editAssignmentCtrl)) return;
		
		editAssignmentCtrl = new AssignmentEditController(ureq, getWindowControl(), assignment,
				AssignmentEditController.templatesTypes, 1);
		listenTo(editAssignmentCtrl);
		
		String title = translate("edit.assignment");
		cmc = new CloseableModalController(getWindowControl(), null, editAssignmentCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void confirmDeleteAssignment(UserRequest ureq, Assignment assignment) {
		String title = translate("delete.assignment.template.confirm.title");
		String text = translate("delete.assignment.template.confirm.descr", new String[] { assignment.getTitle() });
		confirmDeleteCtrl = activateYesNoDialog(ureq, title, text, confirmDeleteCtrl);
		confirmDeleteCtrl.setUserObject(assignment);
	}
	
	private void doDeleteAssignment(Assignment assignment) {
		portfolioService.deleteAssignment(assignment);
		dbInstance.commit();
		loadModel();
	}
}
