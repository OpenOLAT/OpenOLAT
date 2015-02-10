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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
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
import org.olat.modules.coach.model.SearchCoachedIdentityParams;
import org.olat.modules.coach.model.StudentStatEntry;
import org.olat.modules.coach.ui.StudentsTableDataModel.Columns;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserListController extends BasicController implements Activateable2 {
	
	private final Link back;
	private final Panel content;
	private final VelocityContainer listVC;
	private final TableController tableCtr;
	private StudentCoursesController studentCtrl;
	
	private boolean hasChanged;
	private SearchCoachedIdentityParams searchParams;
	private final Map<Long,String> identityFullNameMap = new HashMap<Long,String>();
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private CoachingService coachingService;
	
	public UserListController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("error.no.found"));
		tableConfig.setDownloadOffered(true);
		tableConfig.setPreferencesOffered(true, "userListController");

		tableCtr = new TableController(tableConfig, ureq, getWindowControl(), null, null, null, null, true, getTranslator());
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("student.name", Columns.name.ordinal(), "select", getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.countCourses", Columns.countCourse.ordinal(), null, getLocale()));
		tableCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.login", Columns.initialLaunch.ordinal(), null, getLocale(),
				ColumnDescriptor.ALIGNMENT_LEFT, new LightIconRenderer()));
		tableCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.passed", Columns.countPassed.ordinal(), null, getLocale(),
				ColumnDescriptor.ALIGNMENT_LEFT, new ProgressRenderer(false, getTranslator())));
		listenTo(tableCtr);
		
		listVC = createVelocityContainer("user_list");
		listVC.put("userList", tableCtr.getInitialComponent());
		back = LinkFactory.createLinkBack(listVC, this);
		listVC.put("back", back);
		
		content = new Panel("studentList");
		content.setContent(listVC);
		putInitialPanel(content);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	public int size() {
		return tableCtr.getRowCount();
	}
	
	private void reloadModel() {
		if(hasChanged) {
			loadModel();
			hasChanged = false;
		}
	}
	
	private void loadModel() {
		List<StudentStatEntry> stats = coachingService.getUsersStatistics(searchParams);
		
		List<Long> identityKeys = new ArrayList<>(stats.size());
		for(StudentStatEntry entry:stats) {
			Long identityKey = entry.getStudentKey();
			if(!identityFullNameMap.containsKey(identityKey)) {
				identityKeys.add(identityKey);
			}
		}
		Map<Long,String> maps = userManager.getUserDisplayNamesByKey(identityKeys);
		if(maps.size() > 0) {
			identityFullNameMap.putAll(maps);
		}
		
		TableDataModel<StudentStatEntry> model = new StudentsTableDataModel(stats, identityFullNameMap);
		tableCtr.setTableDataModel(model);
	}

	public void search(SearchCoachedIdentityParams searchParameters) {
		this.searchParams = searchParameters;
		loadModel();
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == back) {
			fireEvent(ureq, Event.BACK_EVENT);
		}
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
			content.setContent(listVC);
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
		//do nothing
	}
	
	protected void selectUniqueStudent(UserRequest ureq) {
		if(tableCtr.getRowCount() > 0) {
			StudentStatEntry studentStat = (StudentStatEntry)tableCtr.getTableDataModel().getObject(0);
			selectStudent(ureq, studentStat);
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
		studentCtrl = new StudentCoursesController(ureq, bwControl, studentStat, student, index, tableCtr.getRowCount(), true);
		
		listenTo(studentCtrl);
		content.setContent(studentCtrl.getInitialComponent());
	}
}