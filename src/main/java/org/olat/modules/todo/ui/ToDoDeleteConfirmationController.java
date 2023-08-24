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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.todo.ToDoTask;

/**
 * 
 * Initial date: 21 Apr 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.comm
 *
 */
public class ToDoDeleteConfirmationController extends ToDoConfirmationController {

	public ToDoDeleteConfirmationController(UserRequest ureq, WindowControl wControl, ToDoTask toDoTask) {
		super(ureq, wControl, "", "task.delete.confirmation.confirm", "delete", true, false);
		this.message = translate("task.delete.conformation.message", ToDoUIFactory.getDisplayName(getTranslator(), toDoTask));
		
		initForm(ureq);
	}

}
