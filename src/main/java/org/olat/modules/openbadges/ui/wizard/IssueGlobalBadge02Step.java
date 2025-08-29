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
package org.olat.modules.openbadges.ui.wizard;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.course.member.wizard.ImportMemberByUsernamesController;
import org.olat.course.member.wizard.ImportMemberOverviewIdentitiesController;
import org.olat.course.member.wizard.MembersByNameContext;

/**
 * Initial date: 2025-08-18<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class IssueGlobalBadge02Step extends BasicStep {
	
	private static final int WARNING_MIN_SIZE = 10;
	
	public IssueGlobalBadge02Step(UserRequest ureq) {
		super(ureq);
		setNextStep(NOSTEP);
		setI18nTitleAndDescr("review", null);
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, false, true);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		String warning = null;
		if (runContext.get(ImportMemberByUsernamesController.RUN_CONTEXT_KEY) instanceof MembersByNameContext membersByNameContext) {
			if (membersByNameContext.getIdentities().size() >= WARNING_MIN_SIZE) {
				warning = getTranslator().translate("review.warning", Integer.toString(membersByNameContext.getIdentities().size()));
			}
		}
		return new ImportMemberOverviewIdentitiesController(ureq, wControl, form, runContext, ImportMemberByUsernamesController.RUN_CONTEXT_KEY, null, warning);
	}
}
