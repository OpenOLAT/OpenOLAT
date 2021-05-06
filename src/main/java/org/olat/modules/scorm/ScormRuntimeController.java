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
package org.olat.modules.scorm;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.ui.RepositoryEntryRuntimeController;
import org.olat.repository.ui.RepositoryEntrySettingsController;

/**
 * 
 * The runtime add delivery options.
 * 
 * Initial date: 15.08.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ScormRuntimeController extends RepositoryEntryRuntimeController {
	
	public ScormRuntimeController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry re, RepositoryEntrySecurity reSecurity, RuntimeControllerCreator runtimeControllerCreator) {
		super(ureq, wControl, re, reSecurity, runtimeControllerCreator);
	}

	@Override
	protected RepositoryEntrySettingsController createSettingsController(UserRequest ureq, WindowControl bwControl, RepositoryEntry refreshedEntry) {
		return new ScormSettingsController(ureq, bwControl, toolbarPanel, refreshedEntry);
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == toolbarPanel) {
			if(event instanceof PopEvent) {
				PopEvent popEvent = (PopEvent)event;
				if(currentToolCtr instanceof ScormSettingsController && currentToolCtr == popEvent.getController()) {
					launchContent(ureq);
					initToolbar();
				}
				setActiveTool(null);
			}
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == getRuntimeController()) {
			if(event == Event.BACK_EVENT) {
				super.doClose(ureq);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void doClose(UserRequest ureq) {
		Controller runCtrl = getRuntimeController();
		if(runCtrl instanceof ScormAPIandDisplayController) {
			((ScormAPIandDisplayController)runCtrl).doBack(ureq);
		}
	}
}