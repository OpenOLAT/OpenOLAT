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
package org.olat.modules.openbadges.ui.element;

import java.util.Collection;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectSelectionBrowserEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Util;
import org.olat.course.CourseModule;
import org.olat.modules.openbadges.ui.OpenBadgesUIFactory;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.olat.repository.controllers.RepositorySearchController;

/**
 * 
 * Initial date: Sep 30, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CourseSelectionController extends BasicController {

	private final ReferencableEntriesSearchController courseBrowserCtrl;

	protected CourseSelectionController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, Util.createPackageTranslator(OpenBadgesUIFactory.class, ureq.getLocale()));
		
		courseBrowserCtrl = new ReferencableEntriesSearchController(wControl, ureq,
				new String[] {CourseModule.ORES_TYPE_COURSE},
				re -> RepositoryEntryStatusEnum.published.equals(re.getEntryStatus()), null,
				translate("course.selection.option"), false, false,
				true, false, true, false,
				RepositorySearchController.Can.referenceable);
		listenTo(courseBrowserCtrl);
		putInitialPanel(courseBrowserCtrl.getInitialComponent());
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == courseBrowserCtrl) {
			if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
				String key = courseBrowserCtrl.getSelectedEntry().getKey().toString();
				fireEvent(ureq, new ObjectSelectionBrowserEvent(List.of(key)));
			} else if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRIES_SELECTED) {
				Collection<String> keys = courseBrowserCtrl.getSelectedEntries().stream().map(entry -> entry.getKey().toString()).toList();
				fireEvent(ureq, new ObjectSelectionBrowserEvent(keys));
			}
		}
		super.event(ureq, source, event);
	}

}
