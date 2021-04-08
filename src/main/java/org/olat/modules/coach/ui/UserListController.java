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

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.UserSession;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.coach.CoachingService;
import org.olat.modules.coach.model.SearchCoachedIdentityParams;
import org.olat.modules.coach.model.StudentStatEntry;
import org.olat.modules.coach.ui.StudentsTableDataModel.Columns;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserListController extends FormBasicController implements Activateable2 {
	
	public static final String usageIdentifyer = UserListController.class.getCanonicalName();
	public static final int USER_PROPS_OFFSET = 500;
	
	private Link back;
	private FlexiTableElement tableEl;
	private StudentsTableDataModel model;
	private final TooledStackedPanel stackPanel;
	private StudentCoursesController studentCtrl;
	
	private boolean hasChanged;
	private SearchCoachedIdentityParams searchParams;

	private final List<UserPropertyHandler> userPropertyHandlers;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private CoachingService coachingService;
	
	public UserListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
		this.stackPanel = stackPanel;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();

		int colIndex = UserListController.USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = userManager.isMandatoryUserProperty(UserListController.usageIdentifyer , userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex++, "select",
					true, userPropertyHandler.i18nColumnDescriptorLabelKey()));
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.countCourse));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.initialLaunch, new LightIconRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.countPassed, new ProgressRenderer(false, getTranslator())));
		
		model = new StudentsTableDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", model, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setEmptyTableSettings("default.tableEmptyMessage", null, "o_icon_user");

		UserSession usess = ureq.getUserSession();
		boolean autoCompleteAllowed = securityModule.isUserAllowedAutoComplete(usess.getRoles());
		if(autoCompleteAllowed) {
			tableEl.setSearchEnabled(new StudentListProvider(model, userManager), usess);
		}
	}

	@Override
	protected void doDispose() {
		//
	}
	
	public int size() {
		return model.getRowCount();
	}
	
	private void reloadModel() {
		if(hasChanged) {
			loadModel();
			hasChanged = false;
		}
	}
	
	private void loadModel() {
		List<StudentStatEntry> stats = coachingService.getUsersStatistics(searchParams, userPropertyHandlers, getLocale());
		model.setObjects(stats);
		tableEl.reset();
		tableEl.reloadData();
	}

	public void search(SearchCoachedIdentityParams searchParameters) {
		this.searchParams = searchParameters;
		loadModel();
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
				String cmd = se.getCommand();
				StudentStatEntry selectedRow = model.getObject(se.getIndex());
				if("select".equals(cmd)) {
					selectStudent(ureq, selectedRow);
				}
			} else if(event instanceof FlexiTableSearchEvent) {
				FlexiTableSearchEvent ftse = (FlexiTableSearchEvent)event;
				String searchString = ftse.getSearch();
				model.search(searchString);
				tableEl.reset();
				tableEl.reloadData();
			}
		} 
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source == back) {
			fireEvent(ureq, Event.BACK_EVENT);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(event == Event.BACK_EVENT) {
			reloadModel();
			initialPanel.popContent();
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

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent fe) {
		//
	}

	protected void selectUniqueStudent(UserRequest ureq) {
		if(model.getRowCount() > 0) {
			StudentStatEntry studentStat = model.getObject(0);
			selectStudent(ureq, studentStat);
		}
	}
	
	protected void previousStudent(UserRequest ureq) {
		StudentStatEntry currentEntry = studentCtrl.getEntry();
		int previousIndex = model.getObjects().indexOf(currentEntry) - 1;
		if(previousIndex < 0 || previousIndex >= model.getRowCount()) {
			previousIndex = model.getRowCount() - 1;
		}
		StudentStatEntry previousEntry = model.getObject(previousIndex);
		selectStudent(ureq, previousEntry);
	}
	
	protected void nextStudent(UserRequest ureq) {
		StudentStatEntry currentEntry = studentCtrl.getEntry();
		int nextIndex = model.getObjects().indexOf(currentEntry) + 1;
		if(nextIndex < 0 || nextIndex >= model.getRowCount()) {
			nextIndex = 0;
		}
		StudentStatEntry nextEntry = model.getObject(nextIndex);
		selectStudent(ureq, nextEntry);
	}

	protected void selectStudent(UserRequest ureq, StudentStatEntry studentStat) {
		removeAsListenerAndDispose(studentCtrl);
		Identity student = securityManager.loadIdentityByKey(studentStat.getIdentityKey());
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(Identity.class, student.getKey());
		WindowControl bwControl = addToHistory(ureq, ores, null);
		
		int index = model.getObjects().indexOf(studentStat);
		String fullname = userManager.getUserDisplayName(student);
		studentCtrl = new StudentCoursesController(ureq, bwControl, stackPanel, studentStat, student, index, model.getRowCount(), true);
		
		listenTo(studentCtrl);
		stackPanel.popUpToController(this);
		stackPanel.pushController(fullname, studentCtrl);
	}
}