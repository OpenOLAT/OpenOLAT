/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.todo.ui;

import java.util.List;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.ComponentWrapperElement;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.course.todo.manager.CourseCollectionToDoTaskProvider;
import org.olat.course.todo.model.ToDoTaskCollectionCreateContext;
import org.olat.modules.todo.ui.ToDoSimpleViewController;
import org.olat.user.UserManager;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 Jan 2024<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ToDoCollectionCreateOverviewController extends StepFormBasicController {
	
	public static final int USER_PROPS_OFFSET = 500;
	private static final String usageIdentifyer = ToDoCollectionCreateOverviewController.class.getCanonicalName();

	private FlexiTableElement tableEl;
	private AssigneeDataModel dataModel;
	
	private ToDoSimpleViewController toDoTaskCtrl;

	private final ToDoTaskCollectionCreateContext context;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private CourseCollectionToDoTaskProvider collectionProvider;

	public ToDoCollectionCreateOverviewController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_BAREBONE, null);
		
		context = (ToDoTaskCollectionCreateContext)getFromRunContext(ToDoTaskCollectionCreateContext.KEY);
		
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer toDoCont = FormLayoutContainer.createVerticalFormLayout("todo", getTranslator());
		toDoCont.setFormTitle(translate("course.todo.collection.overview.todo.title"));
		toDoCont.setElementCssClass("o_block_bottom");
		toDoCont.setRootForm(mainForm);
		formLayout.add(toDoCont);
		
		toDoTaskCtrl = new ToDoSimpleViewController(ureq, getWindowControl(), context, context.getTagDisplayNames());
		listenTo(toDoTaskCtrl);
		toDoCont.add(new ComponentWrapperElement(toDoTaskCtrl.getInitialComponent()));
		
		FormLayoutContainer assigneesCont = FormLayoutContainer.createVerticalFormLayout("assignees", getTranslator());
		assigneesCont.setFormTitle(translate("course.todo.collection.overview.assignee.title"));
		assigneesCont.setRootForm(mainForm);
		formLayout.add(assigneesCont);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		int colIndex = USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = userManager.isMandatoryUserProperty(usageIdentifyer , userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex, null, true, "userProp-" + colIndex));
			colIndex++;
		}
		
		dataModel = new AssigneeDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), assigneesCont);
	}

	private void loadModel() {
		List<UserPropertiesRow> rows = collectionProvider.getToDoTaskCollectionAssignees(context).stream()
				.map(idenitity -> new UserPropertiesRow(idenitity, userPropertyHandlers, getLocale()))
				.toList();
		
		dataModel.setObjects(rows);
		tableEl.reset();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, StepsEvent.INFORM_FINISHED);
	}
	
	public class AssigneeDataModel extends DefaultFlexiTableDataModel<UserPropertiesRow> {
		
		public AssigneeDataModel(FlexiTableColumnModel columnModel) {
			super(columnModel);
		}
		
		@Override
		public Object getValueAt(int row, int col) {
			UserPropertiesRow assignee= getObject(row);
			int propPos = col - USER_PROPS_OFFSET;
			return assignee.getIdentityProp(propPos);
		}
	}

}
