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
package org.olat.modules.coach.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.BusinessGroup;
import org.olat.modules.assessment.ui.ScoreCellRenderer;
import org.olat.modules.coach.CoachingService;
import org.olat.modules.coach.model.GroupStatEntry;
import org.olat.modules.coach.ui.GroupsTableDataModel.Columns;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * Overview of all groups under the scrutiny of a coach.
 * 
 * <P>
 * Initial Date:  8 févr. 2012 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class GroupListController extends BasicController implements Activateable2 {

	private final TooledStackedPanel stackPanel;
	private final TableController tableCtr;
	private final VelocityContainer mainVC;
	private GroupController groupCtrl;
	
	private boolean hasChanged = false;
	@Autowired
	private CoachingService coachingService;
	
	public GroupListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel) {
		super(ureq, wControl);
		this.stackPanel = stackPanel;
		
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(null, null, "o_icon_group");
		
		tableConfig.setDownloadOffered(true);
		tableConfig.setPreferencesOffered(true, "groupListController");
		
		tableCtr = new TableController(tableConfig, ureq, getWindowControl(), null, null, null, null, true, getTranslator());
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("group.name", Columns.name.ordinal(), "select", getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.countCourses", Columns.countCourses.ordinal(), null, getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.countStudents", Columns.countStudents.ordinal(), null, getLocale()));
		tableCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.login", Columns.initialLaunch.ordinal(), null, getLocale(),
				ColumnDescriptor.ALIGNMENT_LEFT, new LightIconRenderer()));
		tableCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.passed", Columns.countPassed.ordinal(), null, getLocale(),
				ColumnDescriptor.ALIGNMENT_LEFT, new ProgressRenderer(false, getTranslator())));
		tableCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.averageScore", Columns.averageScore.ordinal(), null, getLocale(),
				ColumnDescriptor.ALIGNMENT_RIGHT, new ScoreCellRenderer()));

		listenTo(tableCtr);
		loadModel();
		
		mainVC = createVelocityContainer("group_list");
		mainVC.put("groupsTable", tableCtr.getInitialComponent());

		putInitialPanel(mainVC);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	private void loadModel() {
		List<GroupStatEntry> groups = coachingService.getGroupsStatistics(getIdentity());
		TableDataModel<GroupStatEntry> model = new GroupsTableDataModel(groups);
		tableCtr.setTableDataModel(model);
	}
	
	private void reloadModel() {
		if(hasChanged) {
			loadModel();
			hasChanged = false;
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == stackPanel) {
			if(event instanceof PopEvent) {
				PopEvent pe = (PopEvent)event;
				if(pe.getController() == groupCtrl && hasChanged) {
					reloadModel();
				}
			}
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == tableCtr) {
			if(event instanceof TableEvent) {
				TableEvent e = (TableEvent) event;
				if("select".equals(e.getActionId())) {
					GroupStatEntry groupStatistic = (GroupStatEntry)tableCtr.getTableDataModel().getObject(e.getRowId());
					selectGroup(ureq, groupStatistic);
				}
			}
		} else if (source == groupCtrl) {
			if(event == Event.CHANGED_EVENT) {
				hasChanged = true;
			} else if("next.group".equals(event.getCommand())) {
				nextGroup(ureq);
			} else if("previous.group".equals(event.getCommand())) {
				previousGroup(ureq);
			}
		}
		super.event(ureq, source, event);
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		ContextEntry ce = entries.get(0);
		OLATResourceable ores = ce.getOLATResourceable();
		if("BusinessGroup".equals(ores.getResourceableTypeName())) {
			Long groupKey = ores.getResourceableId();
			for(int i=tableCtr.getRowCount(); i-->0; ) {
				GroupStatEntry groupStatistic = (GroupStatEntry)tableCtr.getTableDataModel().getObject(i);
				if(groupKey.equals(groupStatistic.getGroupKey())) {
					selectGroup(ureq, groupStatistic);
					groupCtrl.activate(ureq, entries.subList(1, entries.size()), ce.getTransientState());
					break;
				}
			}
		}
	}
	
	protected void previousGroup(UserRequest ureq) {
		GroupStatEntry currentEntry = groupCtrl.getEntry();
		int previousIndex = tableCtr.getIndexOfSortedObject(currentEntry) - 1;
		if(previousIndex < 0 || previousIndex >= tableCtr.getRowCount()) {
			previousIndex = tableCtr.getRowCount() - 1;
		}
		GroupStatEntry previousEntry = (GroupStatEntry)tableCtr.getSortedObjectAt(previousIndex);
		selectGroup(ureq, previousEntry);
	}
	
	protected void nextGroup(UserRequest ureq) {
		GroupStatEntry currentEntry = groupCtrl.getEntry();
		int nextIndex = tableCtr.getIndexOfSortedObject(currentEntry) + 1;
		if(nextIndex < 0 || nextIndex >= tableCtr.getRowCount()) {
			nextIndex = 0;
		}
		GroupStatEntry nextEntry = (GroupStatEntry)tableCtr.getSortedObjectAt(nextIndex);
		selectGroup(ureq, nextEntry);
	}
	
	protected void selectGroup(UserRequest ureq, GroupStatEntry groupStatistic) {
		removeAsListenerAndDispose(groupCtrl);
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(BusinessGroup.class, groupStatistic.getGroupKey());
		WindowControl bwControl = addToHistory(ureq, ores, null);

		int index = tableCtr.getIndexOfSortedObject(groupStatistic);
		groupCtrl = new GroupController(ureq, bwControl, stackPanel, groupStatistic, index, tableCtr.getRowCount());
		listenTo(groupCtrl);
		stackPanel.popUpToRootController(ureq);
		stackPanel.pushController(groupStatistic.getGroupName(), groupCtrl);
	}
}