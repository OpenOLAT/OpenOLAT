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
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.course.reminder.CourseNodeReminderProvider;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 14 Jun 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseNodeReminderRunController extends BasicController {

	private final TooledStackedPanel stackPanel;
	private final CourseNodeReminderController remindersCtrl;
	
	public CourseNodeReminderRunController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry,
			CourseNodeReminderProvider reminderProvider) {
		super(ureq, wControl);
		stackPanel = new TooledStackedPanel("reminderPanel", getTranslator(), this);
		stackPanel.setToolbarAutoEnabled(false);
		stackPanel.setToolbarEnabled(false);
		stackPanel.setShowCloseLink(true, false);
		stackPanel.setCssClass("o_segment_toolbar o_block_top");
		putInitialPanel(stackPanel);
		
		remindersCtrl = new CourseNodeReminderController(ureq, getWindowControl(), stackPanel, repositoryEntry, reminderProvider, false);
		listenTo(remindersCtrl);
		
		stackPanel.pushController(translate("reminders"), remindersCtrl);
	}
	
	public void reload(UserRequest ureq) {
		remindersCtrl.reload(ureq);
		stackPanel.popUpToRootController(ureq);
	}

	public boolean hasDataOrActions() {
		return remindersCtrl.hasDataOrActions();
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}

}
