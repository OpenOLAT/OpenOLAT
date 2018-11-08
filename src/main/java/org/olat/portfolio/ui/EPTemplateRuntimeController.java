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
package org.olat.portfolio.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.RootEvent;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.portfolio.ui.structel.EPMapViewController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.model.RepositoryEntrySecurity;
import org.olat.repository.ui.RepositoryEntryRuntimeController;

/**
 * 
 * Steal the edit button and glue it on the dropdown
 * 
 * Initial date: 15.08.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EPTemplateRuntimeController extends RepositoryEntryRuntimeController {

	public EPTemplateRuntimeController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry re, RepositoryEntrySecurity reSecurity, RuntimeControllerCreator runtimeControllerCreator) {
		super(ureq, wControl, re, reSecurity, runtimeControllerCreator);
	}
	
	@Override
	protected void initToolsMenuEditor(Dropdown toolsDropdown) {
		if(getRuntimeController() instanceof EPMapViewController) {
			EPMapViewController mapCtrl = (EPMapViewController)getRuntimeController();
			if(mapCtrl.canEditStructure()) {
				editLink = LinkFactory.createToolLink("edit.cmd", translate("details.openeditor"), this, "o_sel_repository_editor");
				editLink.setElementCssClass("o_sel_ep_edit_map");
				editLink.setIconLeftCSS("o_icon o_icon-lg o_icon_edit");
				toolsDropdown.addComponent(0, editLink);
			}
		}
	}

	@Override
	protected void doEdit(UserRequest ureq) {
		if(!reSecurity.isEntryAdmin()) return;
		
		EPMapViewController mapCtrl = (EPMapViewController)getRuntimeController();
		mapCtrl.edit(ureq);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(editLink == source) {
			EPMapViewController mapCtrl = (EPMapViewController)getRuntimeController();
			mapCtrl.edit(ureq);
		} else if(event instanceof RootEvent) {
			EPMapViewController mapCtrl = (EPMapViewController)getRuntimeController();
			mapCtrl.view(ureq);
		} else {
			super.event(ureq, source, event);
		}
	}
}