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
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.fileresource.FileResourceManager;
import org.olat.ims.cp.CPManager;
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
public class CPSettingsController extends RepositoryEntrySettingsController {
	
	private Link quotaLink;
	private Link deliveryOptionsLink;
	
	private Controller quotaCtrl;
	private DeliveryOptionsConfigurationController deliveryOptionsCtrl;
	
	@Autowired
	private CPManager cpManager;
	@Autowired
	private QuotaManager quotaManager;
	
	public CPSettingsController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel, RepositoryEntry entry) {
		super(ureq, wControl, stackPanel, entry);
	}
	
	@Override
	protected void initOptions() {
		if (quotaManager.hasQuotaEditRights(getIdentity(), roles, getOrganisations())) {
			quotaLink = LinkFactory.createLink("tab.quota.edit", getTranslator(), this);
			quotaLink.setElementCssClass("o_sel_repo_quota");
			buttonsGroup.addButton(quotaLink, false);
		}
		
		deliveryOptionsLink = LinkFactory.createLink("tab.layout", getTranslator(), this);
		deliveryOptionsLink.setElementCssClass("o_sel_repo_layout");
		buttonsGroup.addButton(deliveryOptionsLink, false);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		super.activate(ureq, entries, state);
		
		if(entries != null && !entries.isEmpty()) {
			String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
			if("Quota".equalsIgnoreCase(type)) {
				doOpenQuota(ureq);
			} else if("Layout".equalsIgnoreCase(type)) {
				doLayout(ureq);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(quotaCtrl == source) {
			if(event == Event.CANCELLED_EVENT) {
				doOpenQuota(ureq);
			}
		} else if(deliveryOptionsCtrl == source) {
			if(event == Event.CANCELLED_EVENT) {
				doLayout(ureq);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(quotaLink == source) {
			doOpenQuota(ureq);
		} else if(deliveryOptionsLink == source) {
			doLayout(ureq);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void cleanUp() {
		super.cleanUp();
		
		removeAsListenerAndDispose(deliveryOptionsCtrl);
		removeAsListenerAndDispose(quotaCtrl);
		deliveryOptionsCtrl = null;
		quotaCtrl = null;
	}
	
	private void doOpenQuota(UserRequest ureq) {
		if (quotaManager.hasQuotaEditRights(ureq.getIdentity(), roles, getOrganisations())) {
			OLATResource resource = entry.getOlatResource();
			LocalFolderImpl cpRoot = FileResourceManager.getInstance().unzipContainerResource(resource);
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Quota"), null);
			if(readOnly) {
				quotaCtrl = quotaManager.getQuotaViewInstance(ureq, swControl, cpRoot.getRelPath());
			} else {
				quotaCtrl = quotaManager.getQuotaEditorInstance(ureq, addToHistory(ureq, swControl), cpRoot.getRelPath(), true, false);
			}
			listenTo(quotaCtrl);
			mainPanel.setContent(quotaCtrl.getInitialComponent());
			buttonsGroup.setSelectedButton(quotaLink);
		}
	}
	
	private void doLayout(UserRequest ureq) {
		final OLATResource resource = entry.getOlatResource();
		CPPackageConfig cpConfig = cpManager.getCPPackageConfig(resource);
		DeliveryOptions config = cpConfig == null ? null : cpConfig.getDeliveryOptions();
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Layout"), null);
		deliveryOptionsCtrl = new DeliveryOptionsConfigurationController(ureq, addToHistory(ureq,swControl), config, "Knowledge Transfer#_cp_layout", readOnly);
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
		
		mainPanel.setContent(deliveryOptionsCtrl.getInitialComponent());
		buttonsGroup.setSelectedButton(deliveryOptionsLink);
	}

}
