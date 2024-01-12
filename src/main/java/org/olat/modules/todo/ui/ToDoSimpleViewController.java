/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.todo.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.services.tag.ui.TagUIFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.modules.todo.ToDoExpenditureOfWork;
import org.olat.modules.todo.ToDoPriority;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoStatus;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5 Jan 2024<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ToDoSimpleViewController extends BasicController {
	
	@Autowired
	private ToDoService toDoService;

	public ToDoSimpleViewController(UserRequest ureq, WindowControl wControl, SimpleToDoTask toDoTask, List<String> tagDisplayNames) {
		super(ureq, wControl);
		
		VelocityContainer mainVC = createVelocityContainer("todo_task_simple");
		putInitialPanel(mainVC);
		
		mainVC.contextPut("title", toDoTask.getTitle());
		mainVC.contextPut("description", toDoTask.getDescription());
		mainVC.contextPut("priorityIconCss", ToDoUIFactory.getIconCss(toDoTask.getPriority()));
		mainVC.contextPut("priority", ToDoUIFactory.getDisplayName(getTranslator(), toDoTask.getPriority()));
		mainVC.contextPut("statusIconCss", ToDoUIFactory.getIconCss(toDoTask.getStatus()));
		mainVC.contextPut("status", ToDoUIFactory.getDisplayName(getTranslator(), toDoTask.getStatus()));
		if (toDoTask.getStartDate() != null) {
			mainVC.contextPut("startDate", ToDoUIFactory.getDateOrAnytime(getTranslator(), toDoTask.getStartDate()));
		}
		if (toDoTask.getDueDate() != null) {
			mainVC.contextPut("dueDate", ToDoUIFactory.getDateOrAnytime(getTranslator(), toDoTask.getDueDate()));
		}
		
		ToDoExpenditureOfWork expenditureOfWork = toDoService.getExpenditureOfWork(toDoTask.getExpenditureOfWork());
		String expenditureOfWorkStr = ToDoUIFactory.format(expenditureOfWork);
		if (StringHelper.containsNonWhitespace(expenditureOfWorkStr)) {
			mainVC.contextPut("expenditureOfWork", expenditureOfWorkStr);
		}
		
		String formattedTags = TagUIFactory.getFormattedTagDisplayNames(getLocale(), new ArrayList<>(tagDisplayNames));
		mainVC.contextPut("tags", formattedTags);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	public static interface SimpleToDoTask {
		
		public String getTitle();

		public String getDescription();

		public ToDoStatus getStatus();

		public ToDoPriority getPriority();
		
		public Long getExpenditureOfWork();

		public Date getStartDate();

		public Date getDueDate();
		
	}

}
