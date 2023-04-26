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
package org.olat.modules.todo.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ToDoTaskSearchParams;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Initial date: 25 Apr 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ToDoTasksCompactController extends BasicController {
	
	private static final String CMD_SELECT = "select";

	private final VelocityContainer mainVC;

	@Autowired
	private ToDoService toDoService;

	public ToDoTasksCompactController(UserRequest ureq, WindowControl wControl, ToDoTaskSearchParams searchParams) {
		super(ureq, wControl);
		mainVC = createVelocityContainer("todo_tasks_compact");
		putInitialPanel(mainVC);
		
		List<ToDoTask> toDoTasks = toDoService.getToDoTasks(searchParams);
		if (toDoTasks.isEmpty()) {
			mainVC.contextPut("noTaskAvailable", Boolean.TRUE);
		} else {
			List<ToDoTaskItem> openItems = new ArrayList<>();
			List<ToDoTaskItem> inProgressItems = new ArrayList<>();
			List<ToDoTaskItem> doneItems = new ArrayList<>();
			
			for (ToDoTask toDoTask : toDoTasks) {
				ToDoTaskItem item = createItem(toDoTask);
				if (ToDoStatus.open == toDoTask.getStatus()) {
					openItems.add(item);
				} else if (ToDoStatus.inProgress == toDoTask.getStatus()) {
					inProgressItems.add(item);
				} else if (ToDoStatus.done == toDoTask.getStatus()) {
					doneItems.add(item);
				}
			}
			
			mainVC.contextPut("title", translate("compact.title", String.valueOf(doneItems.size()), String.valueOf(toDoTasks.size())));
			mainVC.contextPut("openTitle", ToDoUIFactory.getDisplayName(getTranslator(), ToDoStatus.open));
			mainVC.contextPut("openItems", openItems);
			mainVC.contextPut("inProgressTitle", ToDoUIFactory.getDisplayName(getTranslator(), ToDoStatus.inProgress));
			mainVC.contextPut("inProgressItems", inProgressItems);
			mainVC.contextPut("doneTitle", ToDoUIFactory.getDisplayName(getTranslator(), ToDoStatus.done));
			mainVC.contextPut("doneItems", doneItems);
		}
	}

	private ToDoTaskItem createItem(ToDoTask toDoTask) {
		Link link = LinkFactory.createCustomLink("sel_" + toDoTask.getKey(), CMD_SELECT, null, Link.LINK + Link.NONTRANSLATED, mainVC, this);
		String displayName = ToDoUIFactory.getDisplayName(getTranslator(), toDoTask);
		link.setCustomDisplayText(displayName);
		link.setUserObject(toDoTask);
		return new ToDoTaskItem(
				ToDoUIFactory.getIconCss(toDoTask.getPriority()),
				ToDoUIFactory.getDisplayName(getTranslator(), toDoTask.getPriority()),
				link);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link link) {
			if (link.getUserObject() instanceof ToDoTask toDoTask) {
				fireEvent(ureq, new ToDoTaskSelectionEvent(toDoTask));
			}
		}
	}
	
	public static final class ToDoTaskItem {
		
		private final String priorityIconCss;
		private final String priorityName;
		private final Link link;
		
		public ToDoTaskItem(String priorityIconCss, String priorityName, Link link) {
			this.priorityIconCss = priorityIconCss;
			this.priorityName = priorityName;
			this.link = link;
		}
		
		public String getPriorityIconCss() {
			return priorityIconCss;
		}
		
		public String getPriorityName() {
			return priorityName;
		}
		
		public Link getLink() {
			return link;
		}
		
		public String getLinkName() {
			return link.getComponentName();
		}
		
	}

}
