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
package org.olat.modules.quality.ui.wizard;

import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.Util;
import org.olat.modules.quality.ui.ParticipationListController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.controllers.ReferencableEntriesSearchController;


/**
 * 
 * Initial date: 13.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.comm
 *
 */
public class AddCourseUserSearchController extends StepFormBasicController {
	
	private ReferencableEntriesSearchController searchController; 

	public AddCourseUserSearchController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "user_course_add_search");
		setTranslator(Util.createPackageTranslator(ParticipationListController.class, getLocale(), getTranslator()));

		searchController = new ReferencableEntriesSearchController(getWindowControl(), ureq,
				new String[] { "CourseModule" }, translate("participation.user.course.add.choose"),
				false, false, true, false, true, false);
		listenTo(searchController);
		initForm (ureq);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == searchController) {
			List<RepositoryEntry> selectedEntries = Collections.emptyList();
			if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
				selectedEntries = Collections.singletonList(searchController.getSelectedEntry());
			} else if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRIES_SELECTED) {
				selectedEntries = searchController.getSelectedEntries();
			}
			CourseContext courseContext = (CourseContext) getFromRunContext("context");
			courseContext.setRepositoryEntries(selectedEntries);
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		} else {
			super.event(ureq, source, event);
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		flc.put("search", searchController.getInitialComponent());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}