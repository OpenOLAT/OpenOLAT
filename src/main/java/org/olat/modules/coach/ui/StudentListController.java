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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
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
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.coach.CoachingService;
import org.olat.modules.coach.model.StudentStatEntry;
import org.olat.modules.coach.ui.StudentsTableDataModel.Columns;
import org.olat.user.UserManager;
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
public class StudentListController extends BasicController implements Activateable2 {
	
	private final Panel content;
	private final TableController tableCtr;
	private final VelocityContainer mainVC;
	private StudentCoursesController studentCtrl;
	
	private boolean hasChanged;
	
	private final Map<Long,String> identityFullNameMap= new HashMap<Long,String>();
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private CoachingService coachingService;
	
	public StudentListController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("error.no.found"));
		tableConfig.setDownloadOffered(true);
		tableConfig.setPreferencesOffered(true, "studentListController");

		tableCtr = new TableController(tableConfig, ureq, getWindowControl(), null, null, null, null, true, getTranslator());
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("student.name", Columns.name.ordinal(), "select", getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.countCourses", Columns.countCourse.ordinal(), null, getLocale()));
		tableCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.login", Columns.initialLaunch.ordinal(), null, getLocale(),
				ColumnDescriptor.ALIGNMENT_LEFT, new LightIconRenderer()));
		tableCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.passed", Columns.countPassed.ordinal(), null, getLocale(),
				ColumnDescriptor.ALIGNMENT_LEFT, new ProgressRenderer(false, getTranslator())));

		loadModel();
		listenTo(tableCtr);
		
		mainVC = createVelocityContainer("student_list");
		content = new Panel("studentList");
		content.setContent(tableCtr.getInitialComponent());
		mainVC.put("content", content);

		putInitialPanel(mainVC);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	private void loadModel() {
		List<StudentStatEntry> students = coachingService.getStudentsStatistics(getIdentity());
		Set<Long> identityKeys = new HashSet<Long>();
		for(StudentStatEntry student:students) {
			if(!identityFullNameMap.containsKey(student.getStudentKey())) {
				identityKeys.add(student.getStudentKey());
			}
		}
		if(!identityKeys.isEmpty()) {
			Map<Long,String> newIdentityFullNameMap = userManager.getUserDisplayNamesByKey(identityKeys);
			identityFullNameMap.putAll(newIdentityFullNameMap);
		}
		TableDataModel<StudentStatEntry> model = new StudentsTableDataModel(students, identityFullNameMap);
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
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == tableCtr) {
			if(event instanceof TableEvent) {
				TableEvent e = (TableEvent) event;
				if("select".equals(e.getActionId())) {
					StudentStatEntry studentStat = (StudentStatEntry)tableCtr.getTableDataModel().getObject(e.getRowId());
					selectStudent(ureq, studentStat);
				}
			}
		} else if(event == Event.BACK_EVENT) {
			reloadModel();
			content.setContent(tableCtr.getInitialComponent());
			removeAsListenerAndDispose(studentCtrl);
			studentCtrl = null;
			addToHistory(ureq);
		} else if (source == studentCtrl) {
			if(event == Event.CHANGED_EVENT) {
				hasChanged = true;
			} else if("next.student".equals(event.getCommand())) {
				nextStudent(ureq);
			} else if("previous.student".equals(event.getCommand())) {
				previousStudent(ureq);
			}
		}
		super.event(ureq, source, event);
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		ContextEntry ce = entries.get(0);
		OLATResourceable ores = ce.getOLATResourceable();
		if("Identity".equals(ores.getResourceableTypeName())) {
			Long identityKey = ores.getResourceableId();
			for(int i=tableCtr.getRowCount(); i-->0; ) {
				StudentStatEntry studentStat = (StudentStatEntry)tableCtr.getTableDataModel().getObject(i);
				if(identityKey.equals(studentStat.getStudentKey())) {
					selectStudent(ureq, studentStat);
					studentCtrl.activate(ureq, entries.subList(1, entries.size()), ce.getTransientState());
					break;
				}
			}
		}
	}
	
	protected void previousStudent(UserRequest ureq) {
		StudentStatEntry currentEntry = studentCtrl.getEntry();
		int previousIndex = tableCtr.getIndexOfSortedObject(currentEntry) - 1;
		if(previousIndex < 0 || previousIndex >= tableCtr.getRowCount()) {
			previousIndex = tableCtr.getRowCount() - 1;
		}
		StudentStatEntry previousEntry = (StudentStatEntry)tableCtr.getSortedObjectAt(previousIndex);
		selectStudent(ureq, previousEntry);
	}
	
	protected void nextStudent(UserRequest ureq) {
		StudentStatEntry currentEntry = studentCtrl.getEntry();
		int nextIndex = tableCtr.getIndexOfSortedObject(currentEntry) + 1;
		if(nextIndex < 0 || nextIndex >= tableCtr.getRowCount()) {
			nextIndex = 0;
		}
		StudentStatEntry nextEntry = (StudentStatEntry)tableCtr.getSortedObjectAt(nextIndex);
		selectStudent(ureq, nextEntry);
	}

	protected void selectStudent(UserRequest ureq, StudentStatEntry studentStat) {
		removeAsListenerAndDispose(studentCtrl);
		Identity student = securityManager.loadIdentityByKey(studentStat.getStudentKey());
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(Identity.class, student.getKey());
		WindowControl bwControl = addToHistory(ureq, ores, null);
		
		int index = tableCtr.getIndexOfSortedObject(studentStat);
		studentCtrl = new StudentCoursesController(ureq, bwControl, studentStat, student, index, tableCtr.getRowCount(), false);
		
		listenTo(studentCtrl);
		content.setContent(studentCtrl.getInitialComponent());
	}
}