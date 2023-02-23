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
package org.olat.course.assessment.ui.tool;

import java.util.List;
import java.util.Set;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 févr. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssignCoachController extends FormBasicController {
	
	private FlexiTableElement tableEl;
	private SelectedIdentityTableModel tableModel;
	
	private List<Identity> identities;
	private final List<UserPropertyHandler> userPropertyHandlers;

	private final Identity currentCoach;
	private final CourseNode courseNode;
	private final RepositoryEntry courseEntry;
	private final UserCourseEnvironment assessedUserCourseEnv;

	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	public AssignCoachController(UserRequest ureq, WindowControl wControl, Identity assessedIdentity,
			Identity currentCoach, CourseEnvironment courseEnv, CourseNode courseNode) {
		super(ureq, wControl, "assign_coach");
		
		this.courseNode = courseNode;
		this.currentCoach = currentCoach;
		courseEntry = courseEnv.getCourseGroupManager().getCourseEntry();
		assessedUserCourseEnv = AssessmentHelper
				.createAndInitUserCourseEnvironment(assessedIdentity, courseEnv);
		
		Roles roles = ureq.getUserSession().getRoles();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(AssessmentToolConstants.usageIdentifyer, isAdministrativeUser);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		identities = repositoryService.getAssignedCoaches(assessedIdentity, courseEntry);

		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		int i=0;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			int colIndex = AssessmentToolConstants.USER_PROPS_OFFSET + i++;
			if (userPropertyHandler == null) continue;
			
			String propName = userPropertyHandler.getName();
			boolean visible = userManager.isMandatoryUserProperty(AssessmentToolConstants.usageIdentifyer , userPropertyHandler);

			FlexiColumnModel col;
			if(UserConstants.FIRSTNAME.equals(propName)
					|| UserConstants.LASTNAME.equals(propName)) {
				col = new DefaultFlexiColumnModel(userPropertyHandler.i18nColumnDescriptorLabelKey(),
						colIndex, userPropertyHandler.getName(), true, propName,
						new StaticFlexiCellRenderer(userPropertyHandler.getName(), new TextFlexiCellRenderer()));
			} else {
				col = new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex, true, propName);
			}
			columnsModel.addFlexiColumnModel(col);
		}
		tableModel = new SelectedIdentityTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setSearchEnabled(true);
		tableEl.setSelection(true, false, false);
		
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("apply.assignment", formLayout);
	}
	
	private void loadModel() {
		tableModel.setObjects(identities);
		tableEl.reset(true, true, true);
		if(currentCoach != null) {
			int index = tableModel.getObjects().indexOf(currentCoach);
			if(index >= 0) {
				tableEl.setMultiSelectedIndex(Set.of(Integer.valueOf(index)));
			}
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Set<Integer> selectedIndexes = tableEl.getMultiSelectedIndex();
		if(selectedIndexes.size() == 1) {
			Identity coach = tableModel.getObject(selectedIndexes.iterator().next().intValue());
			AssessmentEntry assessmentEntry = courseAssessmentService.getAssessmentEntry(courseNode, assessedUserCourseEnv);
			courseAssessmentService.assignCoach(assessmentEntry, coach, assessedUserCourseEnv.getCourseEnvironment(), courseNode);
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private class SelectedIdentityTableModel extends DefaultFlexiTableDataModel<Identity>
	implements SortableFlexiTableDataModel<Identity> {
		
		public SelectedIdentityTableModel(FlexiTableColumnModel columnModel) {
			super(columnModel);
		}
		
		@Override
		public void sort(SortKey orderBy) {
			if(orderBy != null) {
				List<Identity> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, getLocale()).sort();
				super.setObjects(rows);
			}
		}

		@Override
		public Object getValueAt(int row, int col) {
			Identity identity = getObject(row);
			return getValueAt(identity, col);
		}

		@Override
		public Object getValueAt(Identity row, int col) {
			if(col >= AssessmentToolConstants.USER_PROPS_OFFSET) {
				int propIndex = col - AssessmentToolConstants.USER_PROPS_OFFSET;
				UserPropertyHandler userPropHandler = userPropertyHandlers.get(propIndex);
				return userPropHandler.getUserProperty(row.getUser(), getLocale());
			}
			return null;
		}
	}
}
