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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;

/**
 * Initial date: 2023-06-15<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CreateBadgeStep03Criteria extends BasicStep {
	public CreateBadgeStep03Criteria(UserRequest ureq) {
		super(ureq);
		setI18nTitleAndDescr("form.award.criteria", null);
		setNextStep(new CreateBadgeStep04Summary(ureq));
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		return new CreateBadgeStep03Form(ureq, wControl, form, runContext, FormBasicController.LAYOUT_CUSTOM, "criteria_step");
	}

	private class CreateBadgeStep03Form extends StepFormBasicController {
		public CreateBadgeStep03Form(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext, int layout, String customLayoutPageName) {
			super(ureq, wControl, rootForm, runContext, layout, customLayoutPageName);
			initForm(ureq);
		}

		@Override
		protected void formOK(UserRequest ureq) {
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			uifactory.addStaticTextElement("form.criteria.summary.explanation", null,
					translate("form.criteria.summary.explanation"), formLayout);
			uifactory.addTextElement("form.criteria.description", 0, "", formLayout);
			uifactory.addStaticTextElement("form.award.procedure.description", null,
					translate("form.award.procedure.description"), formLayout);

			String[] awardProcedureKeys = new String[] { "automatic", "manual" };
			String[] awardProcedureValues = new String[] {
					translate("form.award.procedure.automatic"),
					translate("form.award.procedure.manual")
			};
			String[] awardProcedureDescriptions = new String[] {
					translate("form.award.procedure.automatic.description"),
					translate("form.award.procedure.manual.description")
			};

			uifactory.addCardSingleSelectHorizontal("form.award.procedure", formLayout, awardProcedureKeys,
					awardProcedureValues, awardProcedureDescriptions, null);

			uifactory.addStaticTextElement("form.criteria.condition.met", null,
					translate("form.criteria.condition.met"), formLayout);

			SelectionValues conditionsKV = new SelectionValues();
			conditionsKV.add(SelectionValues.entry("passed", translate("form.criteria.condition.course.passed")));
			conditionsKV.add(SelectionValues.entry("score", translate("form.criteria.condition.course.score")));
			SingleSelection conditionDropdown = uifactory.addDropdownSingleselect("form.condition", null, formLayout,
					conditionsKV.keys(), conditionsKV.values());

			uifactory.addStaticTextElement("form.criteria.condition.and", null,
					translate("form.criteria.condition.and"), formLayout);
		}
	}
}
