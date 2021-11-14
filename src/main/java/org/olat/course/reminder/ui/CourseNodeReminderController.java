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
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.course.reminder.CourseNodeReminderProvider;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 7 Jun 2021<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseNodeReminderController extends BasicController {

	private CourseReminderListController remindersCtrl;

	private final CourseNodeReminderProvider reminderProvider;

	public CourseNodeReminderController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel toolbarPanel,
			RepositoryEntry repositoryEntry, CourseNodeReminderProvider reminderProvider, boolean editor) {
		super(ureq, wControl);
		this.reminderProvider = reminderProvider;
		String warningI18nKey = editor? "node.reminders.publish": null;
		remindersCtrl = new CourseReminderListController(ureq, wControl, toolbarPanel, repositoryEntry,
				reminderProvider, warningI18nKey);
		listenTo(remindersCtrl);

		putInitialPanel(remindersCtrl.getInitialComponent());
	}

	public void reload(UserRequest ureq) {
		remindersCtrl.reload(ureq);
	}

	public boolean hasDataOrActions() {
		boolean reminderAddable = reminderProvider.getMainRuleSPITypes() != null && !reminderProvider.getMainRuleSPITypes().isEmpty();
		return reminderAddable || remindersCtrl.hasReminders();
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == remindersCtrl) {
			fireEvent(ureq, event);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
