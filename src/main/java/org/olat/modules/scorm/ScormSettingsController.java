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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.iframe.DeliveryOptions;
import org.olat.core.gui.control.generic.iframe.DeliveryOptionsConfigurationController;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.RepositoryEntrySettingsController;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 30 Oct 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ScormSettingsController extends RepositoryEntrySettingsController {
	
	private Link deliveryOptionsLink;
	
	private DeliveryOptionsConfigurationController deliveryOptionsCtrl;
	
	@Autowired
	private ScormMainManager scormMainManager;
	
	public ScormSettingsController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel, RepositoryEntry entry) {
		super(ureq, wControl, stackPanel, entry);
	}
	
	@Override
	protected void initOptions() {
		super.initOptions();
		
		deliveryOptionsLink = LinkFactory.createToolLink("layout", translate("tab.layout"), this);
		deliveryOptionsLink.setElementCssClass("o_sel_repo_layout");
		buttonsGroup.addButton(deliveryOptionsLink, false);
	}

	@Override
	protected void cleanUp() {
		super.cleanUp();
		
		removeAsListenerAndDispose(deliveryOptionsCtrl);
		deliveryOptionsCtrl = null;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		super.activate(ureq, entries, state);
		
		if(entries != null && !entries.isEmpty()) {
			String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
			if("Layout".equalsIgnoreCase(type)) {
				doLayout(ureq);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(deliveryOptionsCtrl == source) {
			if(event == Event.CANCELLED_EVENT) {
				doLayout(ureq);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(deliveryOptionsLink == source) {
			doLayout(ureq);
		}
		super.event(ureq, source, event);
	}

	private void doLayout(UserRequest ureq) {
		ScormPackageConfig scormConfig = scormMainManager.getScormPackageConfig(entry.getOlatResource());
		DeliveryOptions config = scormConfig == null ? null : scormConfig.getDeliveryOptions();
		final OLATResource resource = entry.getOlatResource();
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Layout"), null);
		deliveryOptionsCtrl = new DeliveryOptionsConfigurationController(ureq, addToHistory(ureq, swControl), config, "Knowledge Transfer#_scorm_layout", readOnly);

		deliveryOptionsCtrl.addControllerListener((uureq, source, event) -> {
			if(source == deliveryOptionsCtrl && (event == Event.DONE_EVENT || event == Event.CHANGED_EVENT)) {
				DeliveryOptions newConfig = deliveryOptionsCtrl.getDeliveryOptions();
				ScormPackageConfig sConfig = scormMainManager.getScormPackageConfig(resource);
				if(sConfig == null) {
					sConfig = new ScormPackageConfig();
				}
				sConfig.setDeliveryOptions(newConfig);
				scormMainManager.setScormPackageConfig(resource, sConfig);
			}
		});
		
		mainPanel.setContent(deliveryOptionsCtrl.getInitialComponent());
		buttonsGroup.setSelectedButton(deliveryOptionsLink);
	}
}
