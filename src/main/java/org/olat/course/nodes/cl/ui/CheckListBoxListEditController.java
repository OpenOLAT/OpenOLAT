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
package org.olat.course.nodes.cl.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiColumnModel;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.OLATResourceable;
import org.olat.course.nodes.CheckListCourseNode;
import org.olat.course.nodes.cl.model.Checkbox;
import org.olat.course.nodes.cl.model.CheckboxList;
import org.olat.course.nodes.cl.ui.CheckboxConfigDataModel.Cols;
import org.olat.modules.ModuleConfiguration;

/**
 * Controller to manage a list of checks
 * 
 * 
 * Initial date: 04.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CheckListBoxListEditController extends FormBasicController {
	
	private FormLink addLink;
	private FlexiTableElement boxTable;
	private CheckboxConfigDataModel model;
	private CloseableModalController cmc;
	private CheckboxEditController editCtrl;

	private ModuleConfiguration config;
	private final OLATResourceable courseOres;
	private final CheckListCourseNode courseNode;
	
	public CheckListBoxListEditController(UserRequest ureq, WindowControl wControl,
			OLATResourceable courseOres, CheckListCourseNode courseNode) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		
		this.courseOres = courseOres;
		this.courseNode = courseNode;
		config = courseNode.getModuleConfiguration();
		
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("config.checkbox.title");
		setFormDescription("config.checkbox.description");
		setFormContextHelp("org.olat.course.nodes.cl.ui", "cl-checkbox.html", "help.hover.checkbox");
		
		FormLayoutContainer tableCont = FormLayoutContainer
				.createCustomFormLayout("tablecontainer", getTranslator(), velocity_root + "/checkboxlist_edit.html");
		formLayout.add(tableCont);
		
		addLink = uifactory.addFormLink("add.checkbox", tableCont, Link.BUTTON);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.title.i18nKey(), Cols.title.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.points.i18nKey(), Cols.points.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.release.i18nKey(), Cols.release.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.file.i18nKey(), Cols.file.ordinal()));
		columnsModel.addFlexiColumnModel(new StaticFlexiColumnModel("edit.checkbox", translate("edit.checkbox"), "edit"));
		
		CheckboxList list = (CheckboxList)config.get(CheckListCourseNode.CONFIG_KEY_CHECKBOX);
		List<Checkbox> boxList = list == null ? null : list.getList();
		if(boxList == null) {
			boxList = new ArrayList<Checkbox>();
		}
		model = new CheckboxConfigDataModel(boxList, getTranslator(), columnsModel);
		boxTable = uifactory.addTableElement(ureq, getWindowControl(), "checkbox-list", model, getTranslator(), tableCont);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
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
					Checkbox row = model.getObject(se.getIndex());
					doOpenEdit(ureq, row, false, "edit.checkbox");
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
				doEdit(ureq, editCtrl.getCheckbox());
			} else if("delete".equals(event.getCommand())) {
				doDelete(ureq, editCtrl.getCheckbox());
			}
			cmc.deactivate();
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void doDelete(UserRequest ureq, Checkbox checkbox ) {
		CheckboxList list = (CheckboxList)config.get(CheckListCourseNode.CONFIG_KEY_CHECKBOX);
		if(list == null || checkbox == null) return;
		
		list.remove(checkbox);
		config.set(CheckListCourseNode.CONFIG_KEY_CHECKBOX, list);
		fireEvent(ureq, Event.DONE_EVENT);
		model.setObjects(list.getList());
		boxTable.reset();
	}
	
	private void doEdit(UserRequest ureq, Checkbox checkbox) {
		CheckboxList list = (CheckboxList)config.get(CheckListCourseNode.CONFIG_KEY_CHECKBOX);
		if(list == null) {
			list = new CheckboxList();
		}
		list.add(checkbox);
		config.set(CheckListCourseNode.CONFIG_KEY_CHECKBOX, list);
		fireEvent(ureq, Event.DONE_EVENT);
		model.setObjects(list.getList());
		boxTable.reset();
	}

	private void doOpenEdit(UserRequest ureq, Checkbox checkbox, boolean newCheckbox, String title) {
		if(editCtrl != null) return;
		
		editCtrl = new CheckboxEditController(ureq, getWindowControl(), courseOres, courseNode, checkbox, newCheckbox);
		listenTo(editCtrl);

		Component content = editCtrl.getInitialComponent();
		cmc = new CloseableModalController(getWindowControl(), "close", content, true, title);
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