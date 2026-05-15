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
package org.olat.modules.webFeed.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.settings.RepositoryEntryFinishedAccessOptionsController;

/**
 *
 * Initial date: 2026-05-15<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class FeedSettingsAndAccessOptionsController extends BasicController {

	private final FeedSettingsOptionsController feedOptionsCtrl;
	private final RepositoryEntryFinishedAccessOptionsController finishedAccessCtrl;

	public FeedSettingsAndAccessOptionsController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry entry, boolean readOnly) {
		super(ureq, wControl);

		VelocityContainer mainVC = createVelocityContainer("feed_settings_and_access_options");

		feedOptionsCtrl = new FeedSettingsOptionsController(ureq, wControl, entry, readOnly);
		listenTo(feedOptionsCtrl);
		mainVC.put("options", feedOptionsCtrl.getInitialComponent());

		finishedAccessCtrl = new RepositoryEntryFinishedAccessOptionsController(ureq, wControl, entry, readOnly);
		listenTo(finishedAccessCtrl);
		mainVC.put("finishedAccess", finishedAccessCtrl.getInitialComponent());

		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (feedOptionsCtrl == source || finishedAccessCtrl == source) {
			fireEvent(ureq, event);
		}
		super.event(ureq, source, event);
	}
}
