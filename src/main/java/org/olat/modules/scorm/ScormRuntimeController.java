/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.scorm;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.PopEvent;
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
 * The runtime add delivery options.
 * 
 * Initial date: 15.08.2014<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ScormRuntimeController extends RepositoryEntryRuntimeController {

	private Link changeScormLink;

	private ScormResourceEditController scormResourceEditCtrl;
	private CloseableModalController cmc;
	
	public ScormRuntimeController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry re, RepositoryEntrySecurity reSecurity, RuntimeControllerCreator runtimeControllerCreator) {
		super(ureq, wControl, re, reSecurity, runtimeControllerCreator);
	}

	@Override
	protected RepositoryEntrySettingsController createSettingsController(UserRequest ureq, WindowControl bwControl, RepositoryEntry refreshedEntry) {
		return new ScormSettingsController(ureq, bwControl, toolbarPanel, refreshedEntry);
	}

	@Override
	protected void initToolsMenuReplaceItem(Dropdown toolsDropdown) {
		if (reSecurity.isEntryAdmin()) {
			changeScormLink = LinkFactory.createToolLink("changeScorm", translate("tab.scorm.exchange"), this);
			changeScormLink.setIconLeftCSS("o_icon o_icon_refresh o_icon-fw");
			toolsDropdown.addComponent(changeScormLink);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == toolbarPanel) {
			if(event instanceof PopEvent popEvent) {
				if(currentToolCtr instanceof ScormSettingsController && currentToolCtr == popEvent.getController()) {
					launchContent(ureq);
					initToolbar();
				}
				setActiveTool(null);
			}
		} else if (source == changeScormLink) {
			doReplaceScorm(ureq);
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == getRuntimeController()) {
			if(event == Event.BACK_EVENT) {
				super.doClose(ureq);
			}
		} else if (source == scormResourceEditCtrl) {
			if (event == Event.DONE_EVENT) {
				if (toolbarPanel.getLastController() instanceof ScormAPIandDisplayController) {
					launchContent(ureq);
					initToolbar();
				}
			}
			cmc.deactivate();
		}
		super.event(ureq, source, event);
	}

	private void doReplaceScorm(UserRequest ureq) {
		scormResourceEditCtrl = new ScormResourceEditController(ureq, getWindowControl(), re);
		listenTo(scormResourceEditCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), scormResourceEditCtrl.getInitialComponent(),
				true, translate("tab.scorm.exchange"));
		listenTo(cmc);
		cmc.activate();
	}

	@Override
	protected void doClose(UserRequest ureq) {
		Controller runCtrl = getRuntimeController();
		if(runCtrl instanceof ScormAPIandDisplayController displayController) {
			displayController.doBack(ureq);
		}
	}
}