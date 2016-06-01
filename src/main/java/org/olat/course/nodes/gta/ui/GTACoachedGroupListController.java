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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.TaskLight;
import org.olat.course.nodes.gta.ui.CoachGroupsTableModel.CGCols;
import org.olat.course.nodes.gta.ui.events.SelectBusinessGroupEvent;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTACoachedGroupListController extends GTACoachedListController {
	
	private FlexiTableElement tableEl;
	private CoachGroupsTableModel tableModel;
	private final BreadcrumbPanel stackPanel;
	
	private GTACoachController coachingCtrl;
	
	private final List<BusinessGroup> coachedGroups;
	
	@Autowired
	private GTAManager gtaManager;
	
	public GTACoachedGroupListController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			CourseEnvironment courseEnv, GTACourseNode gtaNode, List<BusinessGroup> coachedGroups) {
		super(ureq, wControl, courseEnv, gtaNode);
		this.coachedGroups = coachedGroups;
		this.stackPanel = stackPanel;
		initForm(ureq);
		updateModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		super.initForm(formLayout, listener, ureq);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CGCols.name.i18nKey(), CGCols.name.ordinal(),
				true,  CGCols.name.name()));
		
		if(gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CGCols.taskName.i18nKey(), CGCols.taskName.ordinal(),
					true, CGCols.taskName.name()));
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CGCols.taskStatus.i18nKey(), CGCols.taskStatus.ordinal(),
				true, CGCols.taskStatus.name(), new TaskStatusCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("select", translate("select"), "select"));
		tableModel = new CoachGroupsTableModel(columnsModel);

		tableEl = uifactory.addTableElement(getWindowControl(), "entries", tableModel, getTranslator(), formLayout);
	}
	
	protected void updateModel() {
		RepositoryEntry entry = courseEnv.getCourseGroupManager().getCourseEntry();
		List<TaskLight> tasks = gtaManager.getTasksLight(entry, gtaNode);
		Map<Long,TaskLight> groupToTasks = new HashMap<>();
		for(TaskLight task:tasks) {
			if(task.getBusinessGroupKey() != null) {
				groupToTasks.put(task.getBusinessGroupKey(), task);
			}
		}

		List<CoachedGroupRow> rows = new ArrayList<>(coachedGroups.size());
		for(BusinessGroup group:coachedGroups) {
			TaskLight task = groupToTasks.get(group.getKey());
			rows.add(new CoachedGroupRow(group, task));
		}
		
		tableModel.setObjects(rows);
		tableEl.reset();
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				CoachedGroupRow row = tableModel.getObject(se.getIndex());
				if("details".equals(cmd) || "select".equals(cmd)) {
					doSelect(ureq, row.getBusinessGroup());
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doSelect(UserRequest ureq, BusinessGroup businessGroup) {
		if(stackPanel == null) {
			fireEvent(ureq, new SelectBusinessGroupEvent(businessGroup));	
		} else {
			removeAsListenerAndDispose(coachingCtrl);
			
			coachingCtrl = new GTACoachController(ureq, getWindowControl(), courseEnv, gtaNode, businessGroup, true, true, true);
			listenTo(coachingCtrl);
			stackPanel.pushController(businessGroup.getName(), coachingCtrl);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
