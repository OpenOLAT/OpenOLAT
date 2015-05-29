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
package org.olat.course.nodes.gta.ui;

import java.io.File;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
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
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.model.TaskDefinition;
import org.olat.course.nodes.gta.model.TaskDefinitionList;
import org.olat.course.nodes.gta.ui.TaskDefinitionTableModel.TDCols;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 * Initial date: 24.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTAAssignmentEditController extends FormBasicController {
	
	private static final String[] typeKeys = new String[] { GTACourseNode.GTASK_ASSIGNEMENT_TYPE_MANUAL, GTACourseNode.GTASK_ASSIGNEMENT_TYPE_AUTO };
	private static final String[] previewKeys = new String[] { "enabled", "disabled" };
	private static final String[] samplingKeys = new String[] { GTACourseNode.GTASK_SAMPLING_UNIQUE, GTACourseNode.GTASK_SAMPLING_REUSE };
	
	private FormLink addTaskLink;
	private RichTextElement textEl;
	private FlexiTableElement taskDefTableEl;
	private TaskDefinitionTableModel taskModel;
	private SingleSelection typeEl, previewEl, samplingEl;
	
	private CloseableModalController cmc;
	private EditTaskController addTaskCtrl, editTaskCtrl;
	
	private final TaskDefinitionList taskList;
	private final File tasksFolder;
	private final ModuleConfiguration config;
	
	public GTAAssignmentEditController(UserRequest ureq, WindowControl wControl,
			ModuleConfiguration config, File tasksFolder) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.config = config;
		this.tasksFolder = tasksFolder;
		
		if(config.get(GTACourseNode.GTASK_TASKS) == null) {
			taskList = new TaskDefinitionList();
			config.set(GTACourseNode.GTASK_TASKS, taskList);
		} else {
			taskList = (TaskDefinitionList)config.get(GTACourseNode.GTASK_TASKS);
		}
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//tasks
		String tasksPage = velocity_root + "/edit_task_list.html";
		FormLayoutContainer tasksCont = FormLayoutContainer.createCustomFormLayout("tasks", getTranslator(), tasksPage);
		tasksCont.setRootForm(mainForm);
		formLayout.add(tasksCont);
		
		addTaskLink = uifactory.addFormLink("add.task", tasksCont, Link.BUTTON);
		addTaskLink.setElementCssClass("o_sel_course_gta_add_task");
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TDCols.title.i18nKey(), TDCols.title.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TDCols.file.i18nKey(), TDCols.file.ordinal()));
		columnsModel.addFlexiColumnModel(new StaticFlexiColumnModel("edit", translate("edit"), "edit"));
		taskModel = new TaskDefinitionTableModel(columnsModel);
		taskDefTableEl = uifactory.addTableElement(getWindowControl(), "taskTable", taskModel, getTranslator(), tasksCont);
		taskDefTableEl.setExportEnabled(true);
		updateModel();
		
		FormLayoutContainer configCont = FormLayoutContainer.createDefaultFormLayout("config", getTranslator());
		configCont.setFormTitle(translate("assignment.config.title"));
		configCont.setElementCssClass("o_sel_course_gta_task_config_form");
		configCont.setRootForm(mainForm);
		formLayout.add(configCont);
		//task assignment configuration
		String[] typeValues = new String[]{
				translate("task.assignment.type.manual"),
				translate("task.assignment.type.auto")
		};
		typeEl = uifactory.addRadiosVertical("task.assignment.type", configCont, typeKeys, typeValues);
		typeEl.addActionListener(FormEvent.ONCHANGE);
		String type = config.getStringValue(GTACourseNode.GTASK_ASSIGNEMENT_TYPE);
		if(GTACourseNode.GTASK_ASSIGNEMENT_TYPE_MANUAL.equals(type)) {
			typeEl.select(GTACourseNode.GTASK_ASSIGNEMENT_TYPE_MANUAL, true);
		} else if(GTACourseNode.GTASK_ASSIGNEMENT_TYPE_AUTO.equals(type)) {
			typeEl.select(GTACourseNode.GTASK_ASSIGNEMENT_TYPE_AUTO, true);
		} else {
			typeEl.select(GTACourseNode.GTASK_ASSIGNEMENT_TYPE_MANUAL, true);
		}
		
		String[] previewValues = new String[] { translate("preview.enabled"), translate("preview.disabled") };
		previewEl = uifactory.addRadiosVertical("preview", configCont, previewKeys, previewValues);
		boolean preview = config.getBooleanSafe(GTACourseNode.GTASK_PREVIEW);
		if(preview) {
			previewEl.select(previewKeys[0], true);
		} else {
			previewEl.select(previewKeys[1], true);
		}
		
		String[] samplingValues = new String[]{ translate("sampling.unique"), translate("sampling.reuse") };
		samplingEl = uifactory.addRadiosVertical("sampling", configCont, samplingKeys, samplingValues);
		String sampling = config.getStringValue(GTACourseNode.GTASK_SAMPLING);
		if(GTACourseNode.GTASK_SAMPLING_UNIQUE.equals(sampling)) {
			samplingEl.select(GTACourseNode.GTASK_SAMPLING_UNIQUE, true);
		} else if(GTACourseNode.GTASK_SAMPLING_REUSE.equals(sampling)) {
			samplingEl.select(GTACourseNode.GTASK_SAMPLING_REUSE, true);
		} else {
			samplingEl.select(GTACourseNode.GTASK_SAMPLING_UNIQUE, true);
		}
		
		uifactory.addSpacerElement("space_man", configCont, false);

		//message for users
		String text = config.getStringValue(GTACourseNode.GTASK_USERS_TEXT);
		textEl = uifactory.addRichTextElementForStringDataMinimalistic("task.text", "task.text", text, 10, -1, configCont, getWindowControl());
		
		//save
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setElementCssClass("o_sel_course_gta_task_config_buttons");
		buttonsCont.setRootForm(mainForm);
		configCont.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	private void updateModel() {
		taskModel.setObjects(taskList.getTasks());
		taskDefTableEl.reset();
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(addTaskCtrl == source) {
			if(event == Event.DONE_EVENT) {
				TaskDefinition newTask = addTaskCtrl.getTask();
				taskList.getTasks().add(newTask);
				fireEvent(ureq, Event.DONE_EVENT);
				updateModel();
			}
			cmc.deactivate();
			cleanUp();
			fireEvent(ureq, Event.DONE_EVENT);
		} else if(editTaskCtrl == source) {
			if(event == Event.DONE_EVENT) {
				fireEvent(ureq, Event.DONE_EVENT);
				taskDefTableEl.reloadData();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editTaskCtrl);
		removeAsListenerAndDispose(addTaskCtrl);
		removeAsListenerAndDispose(cmc);
		editTaskCtrl = null;
		addTaskCtrl = null;
		cmc = null;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		allOk &= validateSingleSelection(typeEl);
		allOk &= validateSingleSelection(previewEl);
		allOk &= validateSingleSelection(samplingEl);
		return allOk & super.validateFormLogic(ureq);
	}
	
	private boolean validateSingleSelection(SingleSelection selectionEl) {
		boolean allOk = true;
		selectionEl.clearError();
		if(!selectionEl.isOneSelected()) {
			selectionEl.setErrorKey("form.mandatory.hover", null);
			allOk &= false;
		}
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addTaskLink == source) {
			doAddTask(ureq);
		} else if(taskDefTableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				TaskDefinition row = taskModel.getObject(se.getIndex());
				if("edit".equals(se.getCommand())) {
					doEdit(ureq, row);
				}
			}
		} else if(typeEl == source) {
			boolean allowPreview = typeEl.isSelected(0);
			previewEl.setVisible(allowPreview);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//assignment type
		String type = typeEl.isSelected(0) ? GTACourseNode.GTASK_ASSIGNEMENT_TYPE_MANUAL : GTACourseNode.GTASK_ASSIGNEMENT_TYPE_AUTO;
		config.setStringValue(GTACourseNode.GTASK_ASSIGNEMENT_TYPE, type);
		//preview
		if(previewEl.isVisible()) {
			config.setBooleanEntry(GTACourseNode.GTASK_PREVIEW, previewEl.isSelected(0));
		} else {
			config.setBooleanEntry(GTACourseNode.GTASK_PREVIEW, Boolean.FALSE);
		}
		
		//sampling
		String sampling = samplingEl.isSelected(0) ? GTACourseNode.GTASK_SAMPLING_UNIQUE : GTACourseNode.GTASK_SAMPLING_REUSE;
		config.setStringValue(GTACourseNode.GTASK_SAMPLING, sampling);
		//text
		String text = textEl.getValue();
		if(StringHelper.containsNonWhitespace(text)) {
			config.setStringValue(GTACourseNode.GTASK_USERS_TEXT, text);
		} else {
			config.remove(GTACourseNode.GTASK_USERS_TEXT);
		}
		
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void doAddTask(UserRequest ureq) {
		addTaskCtrl = new EditTaskController(ureq, getWindowControl(), tasksFolder);
		listenTo(addTaskCtrl);

		String title = translate("add.task");
		cmc = new CloseableModalController(getWindowControl(), null, addTaskCtrl.getInitialComponent(), true, title, false);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doEdit(UserRequest ureq, TaskDefinition taskDef) {
		editTaskCtrl = new EditTaskController(ureq, getWindowControl(), taskDef, tasksFolder);
		listenTo(editTaskCtrl);

		String title = translate("edit.task");
		cmc = new CloseableModalController(getWindowControl(), null, editTaskCtrl.getInitialComponent(), true, title, false);
		listenTo(cmc);
		cmc.activate();
	}
}