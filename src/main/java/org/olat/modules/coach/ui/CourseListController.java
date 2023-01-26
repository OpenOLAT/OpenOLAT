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
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.progressbar.ProgressBar.BarColor;
import org.olat.core.gui.components.progressbar.ProgressRadialCellRenderer;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
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
import org.olat.repository.RepositoryModule;
import org.olat.repository.ui.author.AccessRenderer;
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
public class CourseListController extends FormBasicController implements Activateable2 {
	
	private FlexiTableElement tableEl;
	private CoursesTableDataModel tableModel;
	private final TooledStackedPanel stackPanel;

	private CourseController courseCtrl;
	
	private boolean hasChanged = false;

	@Autowired
	private CoachingService coachingService;
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private RepositoryManager repositoryManager;
	
	public CourseListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel) {
		super(ureq, wControl, "course_list");
		this.stackPanel = stackPanel;
		stackPanel.addListener(this);

		initForm(ureq);
		loadModel();
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Columns.key, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.name, "select"));
		if(repositoryModule.isManagedRepositoryEntries()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Columns.externalId, "select"));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Columns.externalRef, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.access, new AccessRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.countStudents));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.initialLaunch, new LightIconRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.completion, new ProgressRadialCellRenderer(BarColor.success)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.countPassed, new ProgressOfCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.averageScore, new ScoreCellRenderer()));
		
		tableModel = new CoursesTableDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 24, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setEmptyTableSettings("default.tableEmptyMessage", null, "o_CourseModule_icon");
		tableEl.setAndLoadPersistedPreferences(ureq, "courseListController-v2");
	}

	private void loadModel() {
		List<CourseStatEntry> courseStatistics = coachingService.getCoursesStatistics(getIdentity());
		tableModel.setObjects(courseStatistics);
		tableEl.reset(false, true, true);
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
        super.doDispose();
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source == stackPanel) {
			if(event instanceof PopEvent) {
				PopEvent pe = (PopEvent)event;
				if(pe.getController() == courseCtrl && hasChanged) {
					reloadModel();
				}
			}
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("select".equals(se.getCommand())) {
					CourseStatEntry courseStat = tableModel.getObject(se.getIndex());
					selectCourse(ureq, courseStat);
				}
			}
		}
		
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == courseCtrl) {
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
		if("RepositoryEntry".equalsIgnoreCase(ores.getResourceableTypeName())) {
			Long repoKey = ores.getResourceableId();
			for(int i=tableModel.getRowCount(); i-->0; ) {
				CourseStatEntry courseStat = tableModel.getObject(i);
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
		int previousIndex = tableModel.getIndexOfObject(currentEntry) - 1;
		if(previousIndex < 0 || previousIndex >= tableModel.getRowCount()) {
			previousIndex = tableModel.getRowCount() - 1;
		}
		CourseStatEntry previousEntry = tableModel.getObject(previousIndex);
		selectCourse(ureq, previousEntry);
	}
	
	private void nextCourse(UserRequest ureq) {
		CourseStatEntry currentEntry = courseCtrl.getEntry();
		int nextIndex = tableModel.getIndexOfObject(currentEntry) + 1;
		if(nextIndex < 0 || nextIndex >= tableModel.getRowCount()) {
			nextIndex = 0;
		}
		CourseStatEntry nextEntry = tableModel.getObject(nextIndex);
		selectCourse(ureq, nextEntry);
	}
	
	private void selectCourse(UserRequest ureq, CourseStatEntry courseStat) {
		removeAsListenerAndDispose(courseCtrl);
		courseCtrl = null;
		
		RepositoryEntry re = repositoryManager.lookupRepositoryEntry(courseStat.getRepoKey(), false);
		if(re != null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance(RepositoryEntry.class, re.getKey());
			WindowControl bwControl = addToHistory(ureq, ores, null);
			
			int index = tableModel.getIndexOfObject(courseStat);
			courseCtrl = new CourseController(ureq, bwControl, stackPanel, re, courseStat, index, tableModel.getRowCount());
			listenTo(courseCtrl);
			stackPanel.popUpToRootController(ureq);
			stackPanel.pushController(re.getDisplayname(), courseCtrl);
		}
	}
}