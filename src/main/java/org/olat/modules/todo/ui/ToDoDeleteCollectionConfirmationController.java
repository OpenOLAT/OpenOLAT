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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ToDoTaskSearchParams;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5 Jan 2024<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.comm
 *
 */
public class ToDoDeleteCollectionConfirmationController extends ToDoConfirmationController {
	
	@Autowired
	private ToDoService toDoService;

	public ToDoDeleteCollectionConfirmationController(UserRequest ureq, WindowControl wControl, ToDoTask toDoTask) {
		super(ureq, wControl, "", "task.delete.collection.confirmation.confirm", "delete", true, false);
		
		ToDoTaskSearchParams searchParams = new ToDoTaskSearchParams();
		searchParams.setCollectionKeys(List.of(toDoTask.getKey()));
		Long toDoTaskCount = toDoService.getToDoTaskCount(searchParams);
		
		this.message = translate("task.delete.collection.conformation.message",
				StringHelper.escapeHtml(ToDoUIFactory.getDisplayName(getTranslator(), toDoTask)), String.valueOf(toDoTaskCount));
		
		initForm(ureq);
	}

}
