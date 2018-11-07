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
package org.olat.repository.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.stack.ButtonGroupComponent;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.author.AuthoringEditAccessController;
import org.olat.repository.ui.settings.CatalogSettingsController;
import org.olat.repository.ui.settings.ReloadSettingsEvent;
import org.olat.repository.ui.settings.RepositoryEntryInfoController;
import org.olat.repository.ui.settings.RepositoryEntryMetadataController;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Initial date: 29 Oct 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntrySettingsController extends BasicController implements Activateable2, TooledController {
	
	private Link infoLink;
	private Link accessLink;
	private Link catalogLink;
	private Link metadataLink;
	protected final StackedPanel mainPanel;
	protected final TooledStackedPanel stackPanel;
	protected final ButtonGroupComponent buttonsGroup = new ButtonGroupComponent("settings");
	 
	private CatalogSettingsController catalogCtrl;
	private RepositoryEntryInfoController infoCtrl;
	private AuthoringEditAccessController accessCtrl;
	private RepositoryEntryMetadataController metadataCtrl;
	
	protected final Roles roles;
	protected RepositoryEntry entry;
	private List<OrganisationRef> organisations;
	
	@Autowired
	protected RepositoryService repositoryService;
	
	public RepositoryEntrySettingsController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel, RepositoryEntry entry) {
		super(ureq, wControl, Util.createPackageTranslator(RepositoryService.class, ureq.getLocale()));
		roles = ureq.getUserSession().getRoles();
		this.stackPanel = stackPanel;
		this.entry = entry;
		mainPanel = putInitialPanel(new Panel("empty"));
	}
	
	public List<OrganisationRef> getOrganisations() {
		if(organisations == null) {
			organisations = repositoryService.getOrganisationReferences(entry);
		}
		return organisations;
	}

	@Override
	public void initTools() {
		initSegments();
		stackPanel.addTool(buttonsGroup, true);
	}
	
	protected void initSegments() {
		initInfos();
		initAccessAndBooking();
		initOptions();
	}
	
	protected void initInfos() {
		infoLink = LinkFactory.createLink("details.info", getTranslator(), this);
		infoLink.setElementCssClass("o_sel_infos");
		buttonsGroup.addButton(infoLink, false);
		
		metadataLink = LinkFactory.createLink("details.metadata", getTranslator(), this);
		metadataLink.setElementCssClass("o_sel_metadata");
		buttonsGroup.addButton(metadataLink, false);
	}
	
	protected void initAccessAndBooking() {
		accessLink = LinkFactory.createLink("details.access", getTranslator(), this);
		accessLink.setElementCssClass("o_sel_access");
		buttonsGroup.addButton(accessLink, false);
		
		catalogLink = LinkFactory.createLink("details.catalog", getTranslator(), this);
		catalogLink.setElementCssClass("o_sel_catalog");
		buttonsGroup.addButton(catalogLink, false);
	}
	
	protected void initOptions() {
		//
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) {
			doOpenInfos(ureq);
		} else {
			String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
			if("Info".equalsIgnoreCase(type)) {
				doOpenInfos(ureq);
			} else if("Metadata".equalsIgnoreCase(type)) {
				doOpenMetadata(ureq);
			} else if("Access".equalsIgnoreCase(type)) {
				doOpenAccess(ureq);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(infoLink == source) {
			cleanUp();
			doOpenInfos(ureq);
		} else if(metadataLink == source) {
			cleanUp();
			doOpenMetadata(ureq);
		} else if(accessLink == source) {
			cleanUp();
			doOpenAccess(ureq);
		} else if(catalogLink == source) {
			cleanUp();
			doOpenCatalog(ureq);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(event == Event.CANCELLED_EVENT) {
			if(catalogCtrl == source) {
				doOpenCatalog(ureq);
			} else if(infoCtrl == source) {
				doOpenInfos(ureq);
			} else if(accessCtrl == source) {
				doOpenAccess(ureq);
			} else if(metadataCtrl == source) {
				doOpenMetadata(ureq);
			}
		} else if(event == Event.CHANGED_EVENT || event instanceof ReloadSettingsEvent) {
			fireEvent(ureq, event);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void doDispose() {
		//
	}
	
	protected void cleanUp() {
		removeAsListenerAndDispose(metadataCtrl);
		removeAsListenerAndDispose(catalogCtrl);
		removeAsListenerAndDispose(accessCtrl);
		removeAsListenerAndDispose(infoCtrl);
		metadataCtrl = null;
		catalogCtrl = null;
		accessCtrl = null;
		infoCtrl = null;
	}
	
	protected void doOpenInfos(UserRequest ureq) {
		entry = repositoryService.loadByKey(entry.getKey());
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Info"), null);
		infoCtrl = new RepositoryEntryInfoController(ureq, swControl, entry);
		listenTo(infoCtrl);
		mainPanel.setContent(infoCtrl.getInitialComponent());
		buttonsGroup.setSelectedButton(infoLink);
	}
	
	protected void doOpenMetadata(UserRequest ureq) {
		entry = repositoryService.loadByKey(entry.getKey());
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Metadata"), null);
		metadataCtrl = new RepositoryEntryMetadataController(ureq, swControl, entry);
		listenTo(metadataCtrl);
		mainPanel.setContent(metadataCtrl.getInitialComponent());
		buttonsGroup.setSelectedButton(metadataLink);
	}
	
	protected void doOpenAccess(UserRequest ureq) {
		entry = repositoryService.loadByKey(entry.getKey());
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Access"), null);
		accessCtrl = new AuthoringEditAccessController(ureq, swControl, entry);
		listenTo(accessCtrl);
		mainPanel.setContent(accessCtrl.getInitialComponent());
		buttonsGroup.setSelectedButton(accessLink);
	}
	
	protected void doOpenCatalog(UserRequest ureq) {
		entry = repositoryService.loadByKey(entry.getKey());
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Catalog"), null);
		catalogCtrl = new CatalogSettingsController(ureq, swControl, entry);
		listenTo(catalogCtrl);
		mainPanel.setContent(catalogCtrl.getInitialComponent());
		buttonsGroup.setSelectedButton(catalogLink);
	}
}
