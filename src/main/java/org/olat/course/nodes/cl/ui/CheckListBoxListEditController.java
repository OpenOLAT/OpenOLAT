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
import org.olat.core.gui.components.form.flexible.elements.DownloadLink;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
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
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CheckListCourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.nodes.cl.CheckboxManager;
import org.olat.course.nodes.cl.model.Checkbox;
import org.olat.course.nodes.cl.model.CheckboxList;
import org.olat.course.nodes.cl.ui.CheckboxConfigDataModel.Cols;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

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
	private DefaultFlexiColumnModel pointColModel;
	private CloseableModalController cmc;
	private CheckboxEditController editCtrl;

	private final boolean inUse;
	private ModuleConfiguration config;
	private final OLATResourceable courseOres;
	private final CourseEnvironment courseEnv;
	private final CheckListCourseNode courseNode;

	@Autowired
	private CheckboxManager checkboxManager;
	
	public CheckListBoxListEditController(UserRequest ureq, WindowControl wControl,
			OLATResourceable courseOres, CheckListCourseNode courseNode, boolean inUse) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.inUse = inUse;
		this.courseOres = courseOres;
		this.courseNode = courseNode;
		
		ICourse course = CourseFactory.loadCourse(courseOres);
		courseEnv = course.getCourseEnvironment();
		config = courseNode.getModuleConfiguration();
		
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("config.checkbox.title");
		setFormDescription("config.checkbox.description");
		setFormContextHelp("Assessment#_checklist_cb");
		formLayout.setElementCssClass("o_sel_cl_edit_checklist");
		if(inUse) {
			setFormWarning("config.warning.inuse");
		}
		
		FormLayoutContainer tableCont = FormLayoutContainer
				.createCustomFormLayout("tablecontainer", getTranslator(), velocity_root + "/checkboxlist_edit.html");
		formLayout.add(tableCont);
		
		addLink = uifactory.addFormLink("add.checkbox", tableCont, Link.BUTTON);
		addLink.setElementCssClass("o_sel_cl_new_checkbox");
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.title.i18nKey(), Cols.title.ordinal()));
		
		Boolean hasScore = (Boolean)config.get(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD);
		boolean visible = (hasScore == null || hasScore.booleanValue());
		pointColModel = new DefaultFlexiColumnModel(visible, Cols.points.i18nKey(), Cols.points.ordinal(), false, null);
		columnsModel.addFlexiColumnModel(pointColModel);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.release.i18nKey(), Cols.release.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.file.i18nKey(), Cols.file.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("edit", translate("edit"), "edit"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("up", Cols.up.ordinal(), "up",
				new BooleanCellRenderer(
						new StaticFlexiCellRenderer("", "up", "o_icon o_icon-lg o_icon_move_up", translate("up")),
						null)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("down", Cols.down.ordinal(), "down",
				new BooleanCellRenderer(
						new StaticFlexiCellRenderer("", "down", "o_icon o_icon-lg o_icon_move_down", translate("down")),
						null)));
		
		model = new CheckboxConfigDataModel(getTranslator(), columnsModel);
		boxTable = uifactory.addTableElement(getWindowControl(), "checkbox-list", model, getTranslator(), tableCont);
		boxTable.setCustomizeColumns(false);
		updateModel();
	}
	
	private void updateModel() {
		CheckboxList list = (CheckboxList)config.get(CheckListCourseNode.CONFIG_KEY_CHECKBOX);
		List<CheckboxConfigRow> boxList = new ArrayList<>();

		if(list != null && list.getList() != null) {
			for(Checkbox checkbox:list.getList()) {
				DownloadLink download = null;
				VFSContainer container = checkboxManager.getFileContainer(courseEnv, courseNode);
				if(container != null) {
					VFSItem item = container.resolve(checkbox.getFilename());
					if(item instanceof VFSLeaf) {
						download = uifactory.addDownloadLink("file_" + checkbox.getCheckboxId(), checkbox.getFilename(), null, (VFSLeaf)item, boxTable);
					}
				}
				boxList.add(new CheckboxConfigRow(checkbox, download));
			}
		}
		model.setObjects(boxList);
		boxTable.reset();
		
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
			doOpenEdit(ureq, checkbox, true, translate("add.checkbox"));
		} else if(boxTable == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				if("edit".equals(cmd)) {
					CheckboxConfigRow row = model.getObject(se.getIndex());
					doOpenEdit(ureq, row.getCheckbox(), false, translate("edit.checkbox"));
				} else if("up".equals(cmd)) {
					doUp(ureq, se.getIndex());	
				} else if("down".equals(cmd)) {
					doDown(ureq, se.getIndex());
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
		} else if(source instanceof CheckListConfigurationController) {
			//update score / no score
			Boolean hasScore = (Boolean)config.get(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD);
			boolean visible = (hasScore == null || hasScore.booleanValue());
			if(visible != boxTable.isColumnModelVisible(pointColModel)) {
				boxTable.setColumnModelVisible(pointColModel, visible);
				boxTable.reset();
				boxTable.reloadData();
			}
		}
		super.event(ureq, source, event);
	}
	
	private void doUp(UserRequest ureq, int checkboxIndex) {
		CheckboxList list = (CheckboxList)config.get(CheckListCourseNode.CONFIG_KEY_CHECKBOX);
		if(checkboxIndex > 0 && checkboxIndex < list.getList().size()) {
			Checkbox box = list.getList().remove(checkboxIndex);
			list.getList().add(checkboxIndex - 1, box);
		}
		
		fireEvent(ureq, Event.DONE_EVENT);
		updateModel();
	}
	
	private void doDown(UserRequest ureq, int checkboxIndex) {
		CheckboxList list = (CheckboxList)config.get(CheckListCourseNode.CONFIG_KEY_CHECKBOX);
		if(checkboxIndex >= 0 && checkboxIndex < list.getList().size() - 1) {
			Checkbox box = list.getList().remove(checkboxIndex);
			list.getList().add(checkboxIndex + 1, box);
		}
		
		fireEvent(ureq, Event.DONE_EVENT);
		updateModel();
	}
	
	private void doDelete(UserRequest ureq, Checkbox checkbox ) {
		CheckboxList list = (CheckboxList)config.get(CheckListCourseNode.CONFIG_KEY_CHECKBOX);
		if(list == null || checkbox == null) return;
		
		list.remove(checkbox);
		config.set(CheckListCourseNode.CONFIG_KEY_CHECKBOX, list);

		Boolean sum = (Boolean)config.get(CheckListCourseNode.CONFIG_KEY_PASSED_SUM_CHECKBOX);
		Integer cut = (Integer)config.get(CheckListCourseNode.CONFIG_KEY_PASSED_SUM_CUTVALUE);
		if (sum.booleanValue() && cut.intValue() > list.getNumOfCheckbox()) {
			config.set(CheckListCourseNode.CONFIG_KEY_PASSED_SUM_CUTVALUE, Integer.valueOf(list.getNumOfCheckbox()));
			showWarning("error.cut.adjusted", new String[] {String.valueOf(list.getNumOfCheckbox())});
		}
		
		fireEvent(ureq, Event.DONE_EVENT);
		updateModel();
	}
	
	private void doEdit(UserRequest ureq, Checkbox checkbox) {
		CheckboxList list = (CheckboxList)config.get(CheckListCourseNode.CONFIG_KEY_CHECKBOX);
		if(list == null) {
			list = new CheckboxList();
		}
		list.add(checkbox);
		config.set(CheckListCourseNode.CONFIG_KEY_CHECKBOX, list);
		fireEvent(ureq, Event.DONE_EVENT);
		updateModel();
	}

	private void doOpenEdit(UserRequest ureq, Checkbox checkbox, boolean newCheckbox, String title) {
		if(guardModalController(editCtrl)) return;
		
		Boolean hasScore = (Boolean)config.get(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD);
		boolean withScore = (hasScore == null || hasScore.booleanValue());	
		editCtrl = new CheckboxEditController(ureq, getWindowControl(), courseOres, courseNode, checkbox, newCheckbox, withScore);
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