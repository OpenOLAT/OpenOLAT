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
import org.olat.modules.assessment.ui.ScoreCellRenderer;
import org.olat.modules.coach.CoachingService;
import org.olat.modules.coach.model.CourseStatEntry;
import org.olat.modules.coach.ui.CoursesTableDataModel.Columns;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * Overview of all students under the scrutiny of a coach.
 * 
 * <P>
 * Initial Date:  8 févr. 2012 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CourseListController extends BasicController implements Activateable2 {
	
	private final TooledStackedPanel stackPanel;
	private final TableController tableCtr;
	private final VelocityContainer mainVC;
	private CourseController courseCtrl;
	
	private boolean hasChanged = false;
	
	@Autowired
	private CoachingService coachingService;
	@Autowired
	private RepositoryManager repositoryManager;
	
	public CourseListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel) {
		super(ureq, wControl);
		this.stackPanel = stackPanel;
		stackPanel.addListener(this);

		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("error.no.found"));
		tableConfig.setDownloadOffered(true);
		tableConfig.setPreferencesOffered(true, "courseListController");
		
		tableCtr = new TableController(tableConfig, ureq, getWindowControl(), null, null, null, null, true, getTranslator());
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.course.name", Columns.name.ordinal(), "select", getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.countStudents", Columns.countStudents.ordinal(), null, getLocale()));
		tableCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.login", Columns.initialLaunch.ordinal(), null, getLocale(),
				ColumnDescriptor.ALIGNMENT_LEFT, new LightIconRenderer()));
		tableCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.passed", Columns.countPassed.ordinal(), null, getLocale(),
				ColumnDescriptor.ALIGNMENT_LEFT, new ProgressRenderer(false, getTranslator())));
		tableCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.averageScore", Columns.averageScore.ordinal(), null, getLocale(),
				ColumnDescriptor.ALIGNMENT_RIGHT, new ScoreCellRenderer()));
		
		listenTo(tableCtr);
		loadModel();
		
		mainVC = createVelocityContainer("course_list");
		mainVC.put("coursTable", tableCtr.getInitialComponent());
		putInitialPanel(mainVC);
	}
	
	private void loadModel() {
		List<CourseStatEntry> courseStatistics = coachingService.getCoursesStatistics(getIdentity());
		TableDataModel<CourseStatEntry> model = new CoursesTableDataModel(courseStatistics);
		tableCtr.setTableDataModel(model);
	}
	
	private void reloadModel() {
		if(hasChanged) {
			loadModel();
			hasChanged = false;
		}
	}
	
	@Override
	protected void doDispose() {
		stackPanel.removeListener(this);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == stackPanel) {
			if(event instanceof PopEvent) {
				PopEvent pe = (PopEvent)event;
				if(pe.getController() == courseCtrl && hasChanged) {
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
					CourseStatEntry courseStat = (CourseStatEntry)tableCtr.getTableDataModel().getObject(e.getRowId());
					selectCourse(ureq, courseStat);
				}
			}
		} else if (source == courseCtrl) {
			if(event == Event.CHANGED_EVENT) {
				hasChanged = true;
			} else if("next.course".equals(event.getCommand())) {
				nextCourse(ureq);
			} else if("previous.course".equals(event.getCommand())) {
				previousCourse(ureq);
			}
		}
		super.event(ureq, source, event);
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		ContextEntry ce = entries.get(0);
		OLATResourceable ores = ce.getOLATResourceable();
		if("RepositoryEntry".equals(ores.getResourceableTypeName())) {
			Long repoKey = ores.getResourceableId();
			for(int i=tableCtr.getRowCount(); i-->0; ) {
				CourseStatEntry courseStat = (CourseStatEntry)tableCtr.getTableDataModel().getObject(i);
				if(repoKey.equals(courseStat.getRepoKey())) {
					selectCourse(ureq, courseStat);
					if(courseCtrl != null) {
						courseCtrl.activate(ureq, entries.subList(1, entries.size()), ce.getTransientState());
					}
					break;
				}
			}
		}
	}
	
	private void previousCourse(UserRequest ureq) {
		CourseStatEntry currentEntry = courseCtrl.getEntry();
		int previousIndex = tableCtr.getIndexOfSortedObject(currentEntry) - 1;
		if(previousIndex < 0 || previousIndex >= tableCtr.getRowCount()) {
			previousIndex = tableCtr.getRowCount() - 1;
		}
		CourseStatEntry previousEntry = (CourseStatEntry)tableCtr.getSortedObjectAt(previousIndex);
		selectCourse(ureq, previousEntry);
	}
	
	private void nextCourse(UserRequest ureq) {
		CourseStatEntry currentEntry = courseCtrl.getEntry();
		int nextIndex = tableCtr.getIndexOfSortedObject(currentEntry) + 1;
		if(nextIndex < 0 || nextIndex >= tableCtr.getRowCount()) {
			nextIndex = 0;
		}
		CourseStatEntry nextEntry = (CourseStatEntry)tableCtr.getSortedObjectAt(nextIndex);
		selectCourse(ureq, nextEntry);
	}
	
	private void selectCourse(UserRequest ureq, CourseStatEntry courseStat) {
		removeAsListenerAndDispose(courseCtrl);
		courseCtrl = null;
		
		RepositoryEntry re = repositoryManager.lookupRepositoryEntry(courseStat.getRepoKey(), false);
		if(re != null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance(RepositoryEntry.class, re.getKey());
			WindowControl bwControl = addToHistory(ureq, ores, null);
			
			int index = tableCtr.getIndexOfSortedObject(courseStat);
			courseCtrl = new CourseController(ureq, bwControl, stackPanel, re, courseStat, index, tableCtr.getRowCount());
			listenTo(courseCtrl);
			stackPanel.popUpToRootController(ureq);
			stackPanel.pushController(re.getDisplayname(), courseCtrl);
		}
	}
}