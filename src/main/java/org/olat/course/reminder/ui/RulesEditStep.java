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
package org.olat.course.reminder.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.course.reminder.CourseNodeReminderProvider;
import org.olat.modules.reminder.Reminder;

/**
 * 
 * Initial date: 31 May 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RulesEditStep extends BasicStep {

	public static final String CONTEXT_KEY = "course.reminder";
	
	private final Reminder reminder;
	private final CourseNodeReminderProvider reminderProvider;
	private final String warningI18nKey;

	public RulesEditStep(UserRequest ureq, Reminder reminder, CourseNodeReminderProvider reminderProvider, String warningI18nKey) {
		super(ureq);
		this.reminder = reminder;
		this.reminderProvider = reminderProvider;
		this.warningI18nKey = warningI18nKey;
		setI18nTitleAndDescr("edit.rules", null);
		setNextStep(new RulesOverviewStep(ureq));
		init(ureq);
	}

	private void init(UserRequest ureq) {
		setI18nTitleAndDescr("edit.rules", null);
		setNextStep(new RulesOverviewStep(ureq));
	}
	
	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return PrevNextFinishConfig.NEXT;
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl,
			StepsRunContext stepsRunContext, Form form) {
		if (!stepsRunContext.containsKey(CONTEXT_KEY)) {
			stepsRunContext.put(CONTEXT_KEY, reminder);
		}
		return new RulesEditController(ureq, wControl, form, stepsRunContext, reminderProvider, warningI18nKey);
	}

}
