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

import java.util.List;

import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.Dropdown.Spacer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.iframe.DeliveryOptions;
import org.olat.core.gui.control.generic.iframe.DeliveryOptionsConfigurationController;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.fileresource.FileResourceManager;
import org.olat.ims.cp.CPManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.model.RepositoryEntrySecurity;
import org.olat.repository.ui.RepositoryEntryRuntimeController;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * The runtime add quoty management and delivery options.
 * 
 * Initial date: 15.08.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CPRuntimeController extends RepositoryEntryRuntimeController {
	
	private Link quotaLink;
	private Link deliveryOptionsLink;
	
	@Autowired
	private CPManager cpManager;
	@Autowired
	private QuotaManager quotaManager;
	
	public CPRuntimeController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry re, RepositoryEntrySecurity reSecurity, RuntimeControllerCreator runtimeControllerCreator) {
		super(ureq, wControl, re, reSecurity, runtimeControllerCreator);
	}

	@Override
	protected void initSettingsTools(Dropdown settingsDropdown) {
		super.initSettingsTools(settingsDropdown);
		if (reSecurity.isEntryAdmin()) {
			settingsDropdown.addComponent(new Spacer(""));
			
			if (quotaManager.hasQuotaEditRights(getIdentity(), roles, getOrganisations())) {
				quotaLink = LinkFactory.createToolLink("quota", translate("tab.quota.edit"), this, "o_sel_repo_quota");
				quotaLink.setIconLeftCSS("o_icon o_icon-fw o_icon_quota");
				settingsDropdown.addComponent(quotaLink);
			}
			
			deliveryOptionsLink = LinkFactory.createToolLink("layout", translate("tab.layout"), this, "o_sel_repo_layout");
			deliveryOptionsLink.setIconLeftCSS("o_icon o_icon-fw o_icon_options");
			settingsDropdown.addComponent(deliveryOptionsLink);
		}
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		entries = removeRepositoryEntry(entries);
		if(entries != null && !entries.isEmpty()) {
			String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
			if("Quota".equalsIgnoreCase(type)) {
				doQuota(ureq);
			} else if("Layout".equalsIgnoreCase(type)) {
				if (reSecurity.isEntryAdmin()) {
					doLayout(ureq);
				}
			}
		}
		super.activate(ureq, entries, state);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(quotaLink == source) {
			doQuota(ureq);
		} else if(deliveryOptionsLink == source) {
			doLayout(ureq);
		} else {
			if(source == toolbarPanel) {
				if(event instanceof PopEvent) {
					PopEvent popEvent = (PopEvent)event;
					if(currentToolCtr == editorCtrl && editorCtrl == popEvent.getController()) {
						launchContent(ureq, reSecurity);
						initToolbar();
					}
					setActiveTool(null);
				}
			}
			super.event(ureq, source, event);
		}
	}

	private void doQuota(UserRequest ureq) {
		if (quotaManager.hasQuotaEditRights(ureq.getIdentity(), roles, getOrganisations())) {
			RepositoryEntry entry = getRepositoryEntry();
			OLATResource resource = entry.getOlatResource();
			OlatRootFolderImpl cpRoot = FileResourceManager.getInstance().unzipContainerResource(resource);
			WindowControl bwControl = getSubWindowControl("Quota");
			Controller quotaCtrl = quotaManager.getQuotaEditorInstance(ureq, addToHistory(ureq, bwControl), cpRoot.getRelPath(), getOrganisations());
			pushController(ureq, translate("tab.quota.edit"), quotaCtrl);
			setActiveTool(quotaLink);
		}
	}
	
	private void doLayout(UserRequest ureq) {
		RepositoryEntry entry = getRepositoryEntry();
		final OLATResource resource = entry.getOlatResource();
		CPPackageConfig cpConfig = cpManager.getCPPackageConfig(resource);
		DeliveryOptions config = cpConfig == null ? null : cpConfig.getDeliveryOptions();
		WindowControl bwControl = getSubWindowControl("Layout");
		final DeliveryOptionsConfigurationController deliveryOptionsCtrl
			= new DeliveryOptionsConfigurationController(ureq, addToHistory(ureq, bwControl), config, "Knowledge Transfer#_cp_layout");
		deliveryOptionsCtrl.addControllerListener((uureq, source, event) -> {
			if(source == deliveryOptionsCtrl
					&& (event == Event.DONE_EVENT || event == Event.CHANGED_EVENT)) {
				DeliveryOptions newConfig = deliveryOptionsCtrl.getDeliveryOptions();
				CPPackageConfig cConfig = cpManager.getCPPackageConfig(resource);
				if(cConfig == null) {
					cConfig = new CPPackageConfig();
				}
				cConfig.setDeliveryOptions(newConfig);
				cpManager.setCPPackageConfig(resource, cConfig);
			}
		});
		
		pushController(ureq, translate("tab.layout"), deliveryOptionsCtrl);
		setActiveTool(deliveryOptionsLink);
	}
}