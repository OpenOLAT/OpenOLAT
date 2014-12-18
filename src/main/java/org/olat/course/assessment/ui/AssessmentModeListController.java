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
package org.olat.course.assessment.ui;

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
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiColumnModel;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.assessment.ui.AssessmentModeListModel.Cols;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentModeListController extends FormBasicController {
	
	private FormLink addLink, deleteLink;
	private AssessmentModeListModel model;
	private FlexiTableElement tableEl;
	private final TooledStackedPanel toolbarPanel;
	private AssessmentModeEditController editCtrl;
	
	private final RepositoryEntry entry;
	
	@Autowired
	private AssessmentModeManager assessmentModeMgr;
	
	public AssessmentModeListController(UserRequest ureq, WindowControl wControl,
			TooledStackedPanel toolbarPanel, RepositoryEntry entry) {
		super(ureq, wControl, "mode_list");
		this.entry = entry;
		this.toolbarPanel = toolbarPanel;
		
		initForm(ureq);
		loadModel();
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		addLink = uifactory.addFormLink("add", "add", "add.mode", null, formLayout, Link.BUTTON);
		addLink.setIconLeftCSS("o_icon o_icon_add");
		
		deleteLink = uifactory.addFormLink("delete", "delete", "delete.mode", null, formLayout, Link.BUTTON);
		deleteLink.setIconLeftCSS("o_icon o_icon_delete");
		
		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.name.i18nKey(), Cols.name.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.begin.i18nKey(), Cols.begin.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.end.i18nKey(), Cols.end.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.leadTime.i18nKey(), Cols.leadTime.ordinal(),
				new LeadTimeCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.target.i18nKey(), Cols.target.ordinal(),
				new TargetAudienceCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new StaticFlexiColumnModel("edit", translate("edit"), "edit"));
		
		model = new AssessmentModeListModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", model, 20, false, getTranslator(), formLayout);
	}
	
	private void loadModel() {
		List<AssessmentMode> modes = assessmentModeMgr.getAssessmentModeFor(entry);
		model.setObjects(modes);
		tableEl.reloadData();
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(editCtrl == source) {
			if(Event.CHANGED_EVENT == event || Event.DONE_EVENT == event) {
				loadModel();
			}
			toolbarPanel.popUpToController(this);
			removeAsListenerAndDispose(editCtrl);
			editCtrl = null;
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addLink == source) {
			doAdd(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				AssessmentMode row = model.getObject(se.getIndex());
				if("edit".equals(cmd)) {
					doEdit(ureq, row);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doAdd(UserRequest ureq) {
		removeAsListenerAndDispose(editCtrl);
		AssessmentMode newMode = assessmentModeMgr.createAssessmentMode(entry);
		editCtrl = new AssessmentModeEditController(ureq, getWindowControl(), entry.getOlatResource(), newMode);
		listenTo(editCtrl);
		toolbarPanel.pushController(translate("new.mode"), editCtrl);
	}
	
	private void doEdit(UserRequest ureq, AssessmentMode mode) {
		removeAsListenerAndDispose(editCtrl);
		editCtrl = new AssessmentModeEditController(ureq, getWindowControl(), entry.getOlatResource(), mode);
		listenTo(editCtrl);
		toolbarPanel.pushController(translate("new.mode"), editCtrl);
	}
}
