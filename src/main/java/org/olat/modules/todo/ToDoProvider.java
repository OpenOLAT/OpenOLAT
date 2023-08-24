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
package org.olat.modules.todo;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.olat.core.commons.services.tag.Tag;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;

/**
 * 
 * Initial date: 24 Mar 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface ToDoProvider {

	public String getType();

	public String getBusinessPath(ToDoTask toDoTask);

	public String getDisplayName(Locale locale);

	public void upateStatus(Identity doer, ToDoTaskRef toDoTask, Long originId, String originSubPath, ToDoStatus status);

	public Controller createCreateController(UserRequest ureq, WindowControl wControl, Identity doer, Long originId, String originSubPath);

	public Controller createEditController(UserRequest ureq, WindowControl wControl, ToDoTask toDoTask);

	public FormBasicController createDetailController(UserRequest ureq, WindowControl wControl, Form mainForm,
			ToDoTaskSecurityCallback secCallback, ToDoTask toDoTask, List<Tag> tags, Identity modifier,
			Set<Identity> assignees, Set<Identity> delegatees);

	public void deleteToDoTaskSoftly(Identity doer, ToDoTask toDoTask);

	public Controller createDeleteConfirmationController(UserRequest ureq, WindowControl wControl, Locale locale, ToDoTask toDoTask);

}
