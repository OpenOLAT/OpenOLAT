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
package org.olat.modules.openbadges.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.course.nodes.CourseNode;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2024-08-20<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CourseNodeBadgesController extends BasicController {

	@Autowired
	private RepositoryManager repositoryManager;

	public CourseNodeBadgesController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, CourseNode courseNode) {
		super(ureq, wControl);

		RepositoryEntrySecurity reSecurity = repositoryManager.isAllowed(ureq, entry);

		BreadcrumbedStackedPanel badgesStackPanel = new BreadcrumbedStackedPanel("badges-stack", getTranslator(), this);
		BadgeClassesController badgeClassesController = new BadgeClassesController(ureq, wControl, entry,
				courseNode, reSecurity, badgesStackPanel, null, "form.create.new.badge",
				"form.edit.badge");
		listenTo(badgeClassesController);
		badgesStackPanel.setInvisibleCrumb(0);
		badgesStackPanel.pushController(translate("badges"), badgeClassesController);
		putInitialPanel(badgesStackPanel);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
