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
package org.olat.ims.qti21.ui.assessment;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.assessment.ui.tool.AssessmentToolConstants;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.ui.assessment.IdentitiesAssessmentTestOverviewDataModel.IACols;
import org.olat.modules.assessment.ui.event.CompleteAssessmentTestSessionEvent;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentitiesAssessmentTestOverviewController extends FormBasicController {

	private FlexiTableElement tableEl;
	private IdentitiesAssessmentTestOverviewDataModel tableModel;
	
	private final boolean isAdministrativeUser;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	private final AssessmentTestCorrection testCorrections;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	
	public IdentitiesAssessmentTestOverviewController(UserRequest ureq, WindowControl wControl,
			AssessmentTestCorrection testCorrections) {
		super(ureq, wControl, "overview_corrections");
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.testCorrections = testCorrections;
		
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(AssessmentToolConstants.usageIdentifyer, isAdministrativeUser);

		initForm(ureq);
		tableModel.setObjects(testCorrections.getTestSessions());
		tableEl.selectAll();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		if(isAdministrativeUser) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IACols.username, "select"));
		}
		
		int colIndex = AssessmentToolConstants.USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = UserManager.getInstance().isMandatoryUserProperty(AssessmentToolConstants.usageIdentifyer , userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex, "select", true, "userProp-" + colIndex));
			colIndex++;
		}

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IACols.numOfItemSessions));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IACols.responded));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IACols.corrected));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IACols.score));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IACols.manualScore));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IACols.finalScore));
		
		tableModel = new IdentitiesAssessmentTestOverviewDataModel(columnsModel, testCorrections, userPropertyHandlers, getTranslator()); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		buttonsCont.setRootForm(mainForm);
		uifactory.addFormSubmitButton("save.tests", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Set<Integer> selections = tableEl.getMultiSelectedIndex();
		List<AssessmentTestSession> rows = new ArrayList<>(selections.size());
		for(Integer i:selections) {
			AssessmentTestSession row = tableModel.getObject(i.intValue());
			if(row != null) {
				rows.add(row);
			}
		}
		
		fireEvent(ureq, new CompleteAssessmentTestSessionEvent(rows));
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
