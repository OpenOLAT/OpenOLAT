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
package org.olat.core.commons.modules.glossary;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.glossary.GlossaryEditSettingsController;
import org.olat.modules.glossary.GlossaryRegisterSettingsController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.RepositoryEntrySettingsController;

/**
 * 
 * Initial date: 30 Oct 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GlossarySettingsController extends RepositoryEntrySettingsController {

	private Link registerLink;
	private Link permissionLink;
	
	private GlossaryEditSettingsController glossEditCtr;
	private GlossaryRegisterSettingsController glossRegisterSetCtr;

	public GlossarySettingsController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel, RepositoryEntry entry) {
		super(ureq, wControl, stackPanel, entry);
	}
	
	@Override
	protected void initOptions() {
		super.initOptions();
		
		registerLink = LinkFactory.createToolLink("register", translate("tab.glossary.register"), this);
		registerLink.setElementCssClass("o_sel_glossary_register");
		buttonsGroup.addButton(registerLink, false);
		
		permissionLink = LinkFactory.createToolLink("permissions", translate("tab.glossary.edit"), this);
		permissionLink.setElementCssClass("o_sel_glossary_permission");
		buttonsGroup.addButton(permissionLink, false);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		super.activate(ureq, entries, state);
		
		if(entries != null && !entries.isEmpty()) {
			String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
			if("GlossarySettings".equalsIgnoreCase(type)) {
				doRegister(ureq);
			} else if("GlossaryPermissions".equalsIgnoreCase(type)) {
				doPermission(ureq);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(glossEditCtr == source) {
			if(event == Event.CANCELLED_EVENT) {
				doPermission(ureq);
			}
		} else if(glossRegisterSetCtr == source) {
			if(event == Event.CANCELLED_EVENT) {
				doRegister(ureq);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(registerLink == source) {
			doRegister(ureq);
		} else if(permissionLink == source) {
			doPermission(ureq);
		}
		super.event(ureq, source, event);
	}

	private void doRegister(UserRequest ureq) {
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("GlossarySettings"), null);
		glossRegisterSetCtr = new GlossaryRegisterSettingsController(ureq, addToHistory(ureq, swControl), entry.getOlatResource(), readOnly);
		listenTo(glossRegisterSetCtr);
		mainPanel.setContent(glossRegisterSetCtr.getInitialComponent());
		buttonsGroup.setSelectedButton(registerLink);
	}
	
	private void doPermission(UserRequest ureq) {
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("GlossaryPermissions"), null);
		glossEditCtr = new GlossaryEditSettingsController(ureq, addToHistory(ureq, swControl), entry.getOlatResource(), readOnly);
		listenTo(glossEditCtr);
		mainPanel.setContent(glossEditCtr.getInitialComponent());
		buttonsGroup.setSelectedButton(permissionLink);
	}
	
	
}
