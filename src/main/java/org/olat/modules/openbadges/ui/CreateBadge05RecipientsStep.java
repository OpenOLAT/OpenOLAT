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
package org.olat.modules.openbadges.ui;

import java.util.List;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.course.CourseFactory;
import org.olat.course.archiver.ScoreAccountingHelper;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-06-15<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CreateBadge05RecipientsStep extends BasicStep {

	public CreateBadge05RecipientsStep(UserRequest ureq) {
		super(ureq);
		setI18nTitleAndDescr("form.recipients", null);
		setNextStep(Step.NOSTEP);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		return new CreateBadge05RecipientsForm(ureq, wControl, form, runContext);
	}

	private class CreateBadge05RecipientsForm extends StepFormBasicController {
		private CreateBadgeClassWizardContext createContext;

		@Autowired
		private UserManager userManager;
		@Autowired
		private BaseSecurityModule baseSecurityModule;
		private List<UserPropertyHandler> userPropertyHandlers;
		private BadgeEarnersTableModel tableModel;
		private FlexiTableElement tableEl;

		public CreateBadge05RecipientsForm(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);

			if (runContext.get(CreateBadgeClassWizardContext.KEY) instanceof CreateBadgeClassWizardContext createBadgeClassWizardContext) {
				createContext = createBadgeClassWizardContext;
			}

			initForm(ureq);
		}

		@Override
		protected void formNext(UserRequest ureq) {
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}

		@Override
		protected void formOK(UserRequest ureq) {
			//
		}

		@Override
		protected void formFinish(UserRequest ureq) {
			fireEvent(ureq, StepsEvent.INFORM_FINISHED);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			setFormTitle("form.recipients.preview");

			uifactory.addStaticTextElement("form.recipients.preview.description", null,
					translate("form.recipients.preview.description"), formLayout);

			CourseEnvironment courseEnv = CourseFactory.loadCourse(createContext.getCourse()).getCourseEnvironment();
			List<Identity> identities = ScoreAccountingHelper.loadParticipants(courseEnv);

			boolean isAdministrator = baseSecurityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
			userPropertyHandlers = userManager.getUserPropertyHandlersFor(BadgeEarnersTableModel.usageIdentifier, isAdministrator);

			FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
			int colIndex = BadgeEarnersTableModel.USER_PROPS_OFFSET;
			for (int i = 0; i < userPropertyHandlers.size(); i++) {
				UserPropertyHandler userPropertyHandler = userPropertyHandlers.get(i);
				boolean visible = userManager.isMandatoryUserProperty(BadgeEarnersTableModel.usageIdentifier, userPropertyHandler);
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(
						visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex,
						null, true, "userProp-" + colIndex
				));
				colIndex++;
			}
			List<BadgeEarnerRow> rows = identities.stream().map(i -> new BadgeEarnerRow(i, userPropertyHandlers, getLocale())).collect(Collectors.toList());
			tableModel = new BadgeEarnersTableModel(columnsModel, getLocale());
			tableModel.setObjects(rows);
			tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20,
					false, getTranslator(), formLayout);
			tableEl.reset();
			tableEl.reloadData();
		}
	}
}
