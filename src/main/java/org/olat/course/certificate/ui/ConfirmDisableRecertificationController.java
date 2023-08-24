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
package org.olat.course.certificate.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.course.reminder.CourseNodeRuleSPI;
import org.olat.modules.reminder.ReminderModule;
import org.olat.modules.reminder.ReminderRule;
import org.olat.modules.reminder.ReminderService;
import org.olat.modules.reminder.RuleSPI;
import org.olat.modules.reminder.model.ReminderInfos;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27 avr. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmDisableRecertificationController extends FormBasicController {
	
	private RepositoryEntry repositoryEntry;
	
	@Autowired
	private ReminderModule reminderModule;
	@Autowired
	private ReminderService reminderService;
	
	public ConfirmDisableRecertificationController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry) {
		super(ureq, wControl, "confirm_disable_recertification");
		this.repositoryEntry = repositoryEntry;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			long reminders = getNumberOfReminders();
			String i18nKey = reminders > 1 ? "confirm.disable.recertification.text.plural" : "confirm.disable.recertification.text.singular";
			String text = translate(i18nKey, Long.toString(reminders));
			layoutCont.contextPut("msg", text);
		}
		
		uifactory.addFormSubmitButton("disable.recertification", formLayout);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private long getNumberOfReminders() {
		final CertificateReminderProvider reminderProvider = new CertificateReminderProvider();
		List<ReminderInfos> reminders = reminderService.getReminderInfos(repositoryEntry);
		return reminders.stream().filter(reminder -> isVisible(reminder, reminderProvider)).count();
	}
	
	private boolean isVisible(ReminderInfos reminder, CertificateReminderProvider reminderProvider) {
		String configuration = reminder.getConfiguration();
		if (StringHelper.containsNonWhitespace(configuration)) {
			List<ReminderRule> rules = reminderService.toRules(configuration).getRules();
			if(rules != null && !rules.isEmpty()) {
				List<String> nodeIdents = new ArrayList<>(1);
				Set<String> ruleTypes = new HashSet<>();
				for (ReminderRule rule : rules) {
					RuleSPI ruleSPI = reminderModule.getRuleSPIByType(rule.getType());
					if (ruleSPI instanceof CourseNodeRuleSPI courseNodeRuleSPI) {
						nodeIdents.add(courseNodeRuleSPI.getCourseNodeIdent(rule));
					}
					ruleTypes.add(rule.getType());
				}
				return reminderProvider.filter(nodeIdents, ruleTypes);
			}
		}
		
		return false;
	}
}
