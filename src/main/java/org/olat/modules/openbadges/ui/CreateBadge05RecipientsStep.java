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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
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
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentToolManager;
import org.olat.course.assessment.model.SearchAssessedIdentityParams;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.modules.openbadges.criteria.BadgeCriteria;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-06-15<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CreateBadge05RecipientsStep extends BasicStep {

	public CreateBadge05RecipientsStep(UserRequest ureq, CreateBadgeClassWizardContext createBadgeClassContext) {
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
		@Autowired
		private AssessmentToolManager assessmentToolManager;

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
			if (!createContext.getBadgeCriteria().isAwardAutomatically()) {
				List<Identity> earners = new ArrayList<>();
				Set<Integer> selectedIndices = tableEl.getMultiSelectedIndex();
				for (int i = 0; i < tableModel.getRowCount(); i++) {
					if (selectedIndices.contains(i)) {
						earners.add(tableModel.getObject(i).getIdentity());
					}
				}
				createContext.setEarners(earners);
			}
			fireEvent(ureq, StepsEvent.INFORM_FINISHED);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			setFormTitle("form.recipients.preview");

			StaticTextElement description = uifactory.addStaticTextElement("form.recipients.preview.description", null,
					translate("form.recipients.preview.description"), formLayout);

			ICourse course = CourseFactory.loadCourse(createContext.getCourseResourcableId());
			String rootIdent = course.getRunStructure().getRootNode().getIdent();
			RepositoryEntry courseEntry = createContext.getBadgeClass().getEntry();
			AssessmentToolSecurityCallback secCallback = new AssessmentToolSecurityCallback(true, false, false, true, true, true, null, null);
			SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(
					courseEntry, rootIdent, null, secCallback);

			Map<Long, AssessmentEntry> identityKeyToAssessmentEntry = new HashMap<>();
			assessmentToolManager.getAssessmentEntries(getIdentity(), params, null)
					.stream()
					.filter(entry -> entry.getIdentity() != null)
					.forEach(entry -> identityKeyToAssessmentEntry.put(entry.getIdentity().getKey(), entry));
			List<Identity> assessedIdentities = assessmentToolManager.getAssessedIdentities(getIdentity(), params);

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

			BadgeCriteria badgeCriteria = createContext.getBadgeCriteria();

			List<BadgeEarnerRow> rows = new ArrayList<>();
			List<Identity> earners = new ArrayList<>();
			for (Identity assessedIdentity : assessedIdentities) {
				if ((courseEntry.getEntryStatus() != RepositoryEntryStatusEnum.published)) {
					continue;
				}
				AssessmentEntry assessmentEntry = identityKeyToAssessmentEntry.get(assessedIdentity.getKey());
				if (assessmentEntry == null) {
					continue;
				}

				boolean passed = assessmentEntry.getPassed() != null ? assessmentEntry.getPassed() : false;
				double score = assessmentEntry.getScore() != null ? assessmentEntry.getScore().doubleValue() : 0;
				if (!badgeCriteria.isAwardAutomatically() || badgeCriteria.allConditionsMet(passed, score)) {
					BadgeEarnerRow row = new BadgeEarnerRow(assessedIdentity, userPropertyHandlers, getLocale());
					rows.add(row);
					if (badgeCriteria.isAwardAutomatically()) {
						earners.add(assessedIdentity);
					}
				}
			}

			createContext.setEarners(earners);

			tableModel = new BadgeEarnersTableModel(columnsModel, getLocale());
			tableModel.setObjects(rows);
			tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20,
					false, getTranslator(), formLayout);
			tableEl.reset();
			tableEl.reloadData();

			if (rows.isEmpty()) {
				tableEl.setVisible(false);
				description.setValue(translate("form.recipients.preview.description.no.recipients"));
			} else if (!badgeCriteria.isAwardAutomatically()) {
				description.setValue(translate("form.recipients.preview.description.manual"));
				tableEl.setMultiSelect(true);
				tableEl.setSelectAllEnable(true);
			}
		}
	}
}
