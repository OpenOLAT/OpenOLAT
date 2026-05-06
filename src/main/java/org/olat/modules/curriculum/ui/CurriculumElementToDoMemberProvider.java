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
package org.olat.modules.curriculum.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.todo.ui.ToDoTaskMemberSearchProvider;

/**
 *
 * Initial date: 29 Apr 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumElementToDoMemberProvider implements ToDoTaskMemberSearchProvider {

	private final CurriculumElement element;

	public CurriculumElementToDoMemberProvider(CurriculumElement element) {
		this.element = element;
	}

	@Override
	public Controller createSearchController(UserRequest ureq, WindowControl wc) {
		return new CurriculumElementToDoMemberController(ureq, wc, element);
	}

	@Override
	public List<Identity> getSelectedIdentities(Controller ctrl, Event event) {
		if (event == Event.DONE_EVENT) {
			return ((CurriculumElementToDoMemberController) ctrl).getSelectedIdentities();
		}
		return List.of();
	}

}
