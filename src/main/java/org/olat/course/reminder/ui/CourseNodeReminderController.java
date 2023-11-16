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
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.editor.NodeEditController;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.reminder.CourseNodeReminderProvider;
import org.olat.course.todo.ui.CourseNodeToDoTaskController;
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
	private CourseNodeToDoTaskController toDoCtrl;

	private final CourseNodeReminderProvider reminderProvider;
	private boolean hasToDos = false;

	public CourseNodeReminderController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel toolbarPanel,
			RepositoryEntry repositoryEntry, CourseNodeReminderProvider reminderProvider, boolean editor) {
		super(ureq, wControl);
		this.reminderProvider = reminderProvider;
		
		VelocityContainer mainVC = createVelocityContainer("node_reminder_todo");
		putInitialPanel(mainVC);
		
		String warningI18nKey = editor? "node.reminders.publish": null;
		remindersCtrl = new CourseReminderListController(ureq, wControl, toolbarPanel, repositoryEntry,
				reminderProvider, warningI18nKey, true);
		listenTo(remindersCtrl);
		mainVC.put("reminders", remindersCtrl.getInitialComponent());
		
		if (reminderProvider.isToDoTasks()) {
			ICourse course = CourseFactory.loadCourse(repositoryEntry);
			if (LearningPathNodeAccessProvider.TYPE.equals(NodeAccessType.of(course).getType())) {
				hasToDos = true;
				toDoCtrl = new CourseNodeToDoTaskController(ureq, wControl, repositoryEntry, reminderProvider, editor);
				listenTo(toDoCtrl);
				mainVC.put("todos", toDoCtrl.getInitialComponent());
			}
		}
	}

	public void reload(UserRequest ureq) {
		remindersCtrl.reload(ureq);
		if (toDoCtrl != null) {
			toDoCtrl.reload(ureq);
		}
	}

	public boolean hasDataOrActions() {
		boolean reminderAddable = reminderProvider.getMainRuleSPITypes() != null && !reminderProvider.getMainRuleSPITypes().isEmpty();
		return  hasToDos || reminderAddable || remindersCtrl.hasReminders();
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == remindersCtrl) {
			fireEvent(ureq, event);
		} else if (source == toDoCtrl) {
			if (event == NodeEditController.NODECONFIG_CHANGED_EVENT) {
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
