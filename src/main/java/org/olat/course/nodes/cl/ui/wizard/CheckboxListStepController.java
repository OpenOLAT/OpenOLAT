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
package org.olat.course.nodes.cl.ui.wizard;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.Util;
import org.olat.course.nodes.cl.model.Checkbox;
import org.olat.course.nodes.cl.ui.CheckListEditController;
import org.olat.course.nodes.cl.ui.CheckboxConfigDataModel;
import org.olat.course.nodes.cl.ui.CheckboxConfigDataModel.Cols;
import org.olat.course.nodes.cl.ui.CheckboxConfigRow;
import org.olat.course.nodes.cl.ui.CheckboxEditController;

/**
 * 
 * Initial date: 25.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CheckboxListStepController extends StepFormBasicController {
	
	private FormLink addLink;
	private FlexiTableElement boxTable;
	private CheckboxConfigDataModel model;
	private CloseableModalController cmc;
	private CheckboxEditController editCtrl;
	
	private final GeneratorData data;
	private final OLATResourceable courseOres;

	public CheckboxListStepController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext,
			OLATResourceable courseOres) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
		setTranslator(Util.createPackageTranslator(CheckListEditController.class, getLocale(), getTranslator()));
		
		this.courseOres = courseOres;
		data = (GeneratorData)getFromRunContext("data");

		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("checkbox.template");
		setFormDescription("checkbox.template.description");

		FormLayoutContainer tableCont = FormLayoutContainer
				.createCustomFormLayout("tablecontainer", getTranslator(), velocity_root + "/checkboxlist_edit.html");
		formLayout.add(tableCont);
		
		addLink = uifactory.addFormLink("add.checkbox", tableCont, Link.BUTTON);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.title.i18nKey(), Cols.title.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.points.i18nKey(), Cols.points.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.release.i18nKey(), Cols.release.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.file.i18nKey(), Cols.file.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("edit.checkbox", translate("edit.checkbox"), "edit"));

		model = new CheckboxConfigDataModel(getTranslator(), columnsModel);
		boxTable = uifactory.addTableElement(getWindowControl(), "checkbox-list", model, getTranslator(), tableCont);
		boxTable.setCustomizeColumns(false);
	}
	
	private void updateModel() {
		List<Checkbox> boxList = data.getCheckboxList();
		List<CheckboxConfigRow> rows = new ArrayList<>();
		for(Checkbox box:boxList) {
			rows.add(new CheckboxConfigRow(box, null));
		}
		model.setObjects(rows);
		boxTable.reset();
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		setFormWarning(null);
		if(data.getCheckboxList().isEmpty()) {
			setFormWarning("error.needone.checklist");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addLink == source) {
			Checkbox checkbox = new Checkbox();
			checkbox.setCheckboxId(UUID.randomUUID().toString());
			String title = translate("add.checkbox");
			doOpenEdit(ureq, checkbox, true, title);
		} else if(boxTable == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				if("edit".equals(cmd)) {
					CheckboxConfigRow row = model.getObject(se.getIndex());
					doOpenEdit(ureq, row.getCheckbox(), false, translate("edit.checkbox"));
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(cmc == source) {
			cleanUp();
		} else if(editCtrl == source) {
			if(event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				doEdit(editCtrl.getCheckbox(), editCtrl.isNewCheckbox());
			} else if("delete".equals(event.getCommand())) {
				doDelete(editCtrl.getCheckbox());
			}
			cmc.deactivate();
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void doDelete(Checkbox checkbox) {
		List<Checkbox> boxList = data.getCheckboxList();
		boxList.remove(checkbox);
		updateModel();
	}
	
	private void doEdit(Checkbox checkbox, boolean newCheckbox) {
		if(newCheckbox) {
			List<Checkbox> boxList = data.getCheckboxList();
			boxList.add(checkbox);
			updateModel();
		} else {
			boxTable.reset();
		}
		setFormWarning(null);
		
	}

	private void doOpenEdit(UserRequest ureq, Checkbox checkbox, boolean newCheckbox, String title) {
		if(guardModalController(editCtrl)) return;
		
		editCtrl = new CheckboxEditController(ureq, getWindowControl(), courseOres, checkbox, newCheckbox, true);
		listenTo(editCtrl);

		Component content = editCtrl.getInitialComponent();
		cmc = new CloseableModalController(getWindowControl(), translate("close"), content, true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editCtrl);
		removeAsListenerAndDispose(cmc);
		editCtrl = null;
		cmc = null;
	}
}