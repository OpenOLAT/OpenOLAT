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
package org.olat.ims.cp.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.ui.RepositoryEntryRuntimeController;
import org.olat.repository.ui.RepositoryEntrySettingsController;

/**
 * 
 * The runtime add quota management and delivery options.
 * 
 * Initial date: 15.08.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CPRuntimeController extends RepositoryEntryRuntimeController {
	
	private Link notificationLink;
	
	private CloseableModalController cmc;
	private CPNotificationsController notificationsCtrl;
	
	public CPRuntimeController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry re, RepositoryEntrySecurity reSecurity, RuntimeControllerCreator runtimeControllerCreator) {
		super(ureq, wControl, re, reSecurity, runtimeControllerCreator);
	}
	
	@Override
	protected void initToolbar(Dropdown toolsDropdown) {
		if (reSecurity.isEntryAdmin()) {
			notificationLink = LinkFactory.createToolLink("cpnotifications", translate("command.cp.notifications"), this, "o_icon_message");
			toolbarPanel.addTool(notificationLink, Align.right);
		}
		super.initToolbar(toolsDropdown);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == toolbarPanel) {
			if(event instanceof PopEvent) {
				PopEvent popEvent = (PopEvent)event;
				if(currentToolCtr == editorCtrl && editorCtrl == popEvent.getController()) {
					launchContent(ureq);
					initToolbar();
				}
				setActiveTool(null);
			}
		} else if(notificationLink == source) {
			doNotifications(ureq);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(notificationsCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void cleanUp() {
		super.cleanUp();
		removeAsListenerAndDispose(notificationsCtrl);
		removeAsListenerAndDispose(cmc);
		notificationsCtrl = null;
		cmc = null;
	}
	
	@Override
	protected RepositoryEntrySettingsController createSettingsController(UserRequest ureq, WindowControl bwControl,
			RepositoryEntry refreshedEntry) {
		return new CPSettingsController(ureq, bwControl, toolbarPanel, refreshedEntry);
	}
	
	private void doNotifications(UserRequest ureq) {
		if (reSecurity.isEntryAdmin()) {
			notificationsCtrl = new CPNotificationsController(ureq, getWindowControl(), getRepositoryEntry());
			listenTo(notificationsCtrl);
			
			cmc = new CloseableModalController(getWindowControl(), "close", notificationsCtrl.getInitialComponent(),
					true, translate("command.cp.notifications"));
			cmc.activate();
			listenTo(cmc);
		}
	}
}