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
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
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
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.model.Solution;
import org.olat.course.nodes.gta.model.SolutionList;
import org.olat.course.nodes.gta.ui.TaskDefinitionTableModel.TDCols;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 * Initial date: 25.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTASampleSolutionsEditController extends FormBasicController {
	
	private FormLink addSolutionLink;
	private SolutionTableModel solutionModel;
	private FlexiTableElement solutionTable;
	
	private CloseableModalController cmc;
	private EditSolutionController addSolutionCtrl; 
	private EditSolutionController editSolutionCtrl; 
	
	private final SolutionList solutions;
	private final File solutionContainer;
	
	public GTASampleSolutionsEditController(UserRequest ureq, WindowControl wControl,
			ModuleConfiguration config, File solutionContainer) {
		super(ureq, wControl, "edit_solution_list");
		this.solutionContainer = solutionContainer;
		if(config.get(GTACourseNode.GTASK_SOLUTIONS) == null) {
			solutions = new SolutionList();
			config.set(GTACourseNode.GTASK_SOLUTIONS, solutions);
		} else {
			solutions = (SolutionList)config.get(GTACourseNode.GTASK_SOLUTIONS);
		}

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		addSolutionLink = uifactory.addFormLink("add.solution", formLayout, Link.BUTTON);
		addSolutionLink.setElementCssClass("o_sel_course_gta_add_solution");

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TDCols.title.i18nKey(), TDCols.title.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TDCols.file.i18nKey(), TDCols.file.ordinal()));
		columnsModel.addFlexiColumnModel(new StaticFlexiColumnModel("replace", translate("replace"), "replace"));
		solutionModel = new SolutionTableModel(columnsModel);
		solutionTable = uifactory.addTableElement(getWindowControl(), "table", solutionModel, getTranslator(), formLayout);
		solutionTable.setExportEnabled(true);
		updateModel();
	}
	
	private void updateModel() {
		solutionModel.setObjects(solutions.getSolutions());
		solutionTable.reset();
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(addSolutionCtrl == source) {
			if(event == Event.DONE_EVENT) {
				Solution newSolution = addSolutionCtrl.getSolution();
				solutions.getSolutions().add(newSolution);
				fireEvent(ureq, Event.DONE_EVENT);
				updateModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(editSolutionCtrl == source) {
			if(event == Event.DONE_EVENT) {
				fireEvent(ureq, Event.DONE_EVENT);
				solutionTable.reloadData();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editSolutionCtrl);
		removeAsListenerAndDispose(addSolutionCtrl);
		removeAsListenerAndDispose(cmc);
		editSolutionCtrl = null;
		addSolutionCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addSolutionLink == source) {
			doAddSolution(ureq);
		} else if(solutionTable == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				Solution row = solutionModel.getObject(se.getIndex());
				if("replace".equals(se.getCommand())) {
					doReplace(ureq, row);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void doAddSolution(UserRequest ureq) {
		addSolutionCtrl = new EditSolutionController(ureq, getWindowControl(), solutionContainer);
		listenTo(addSolutionCtrl);

		String title = translate("add.solution");
		cmc = new CloseableModalController(getWindowControl(), null, addSolutionCtrl.getInitialComponent(), true, title, false);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doReplace(UserRequest ureq, Solution solution) {
		editSolutionCtrl = new EditSolutionController(ureq, getWindowControl(), solution, solutionContainer);
		listenTo(editSolutionCtrl);

		String title = translate("add.solution");
		cmc = new CloseableModalController(getWindowControl(), null, editSolutionCtrl.getInitialComponent(), true, title, false);
		listenTo(cmc);
		cmc.activate();
	}
}
