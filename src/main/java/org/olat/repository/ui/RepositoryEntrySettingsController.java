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

import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.stack.ButtonGroupComponent;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.repository.controllers.EntryChangedEvent.Change;
import org.olat.repository.ui.author.AuthoringEditAccessController;
import org.olat.repository.ui.author.ConfirmCloseController;
import org.olat.repository.ui.settings.CatalogSettingsController;
import org.olat.repository.ui.settings.ReloadSettingsEvent;
import org.olat.repository.ui.settings.RepositoryEntryInfoController;
import org.olat.repository.ui.settings.RepositoryEntryMetadataController;
import org.olat.util.logging.activity.LoggingResourceable;
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
	private Dropdown status;
	protected final StackedPanel mainPanel;
	protected final TooledStackedPanel stackPanel;
	protected final ButtonGroupComponent buttonsGroup = new ButtonGroupComponent("settings");

	private Link preparationLink;
	private Link reviewLink;
	private Link coachPublishLink;
	private Link publishLink;
	private Link closeLink;
	
	private CloseableModalController cmc;
	private CatalogSettingsController catalogCtrl;
	private RepositoryEntryInfoController infoCtrl;
	private ConfirmCloseController confirmCloseCtrl;
	private AuthoringEditAccessController accessCtrl;
	private RepositoryEntryMetadataController metadataCtrl;
	
	protected final Roles roles;
	protected RepositoryEntry entry;
	protected final boolean readOnly;
	private List<OrganisationRef> organisations;
	
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	protected RepositoryService repositoryService;
	
	public RepositoryEntrySettingsController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel, RepositoryEntry entry) {
		super(ureq, wControl, Util.createPackageTranslator(RepositoryService.class, ureq.getLocale()));
		roles = ureq.getUserSession().getRoles();
		this.stackPanel = stackPanel;
		this.entry = entry;
		this.readOnly = entry.getEntryStatus() == RepositoryEntryStatusEnum.deleted || entry.getEntryStatus() == RepositoryEntryStatusEnum.trash;
		mainPanel = putInitialPanel(new Panel("empty"));
		
		status = new Dropdown("settings.toolbox.status", "cif.status", false, getTranslator());
		status.setElementCssClass("o_sel_repository_status");
		status.setIconCSS("o_icon o_icon_edit");
		initStatus(status);
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
		stackPanel.addTool(status, Align.left, false);
		stackPanel.addTool(buttonsGroup, true);
	}
	
	private void initStatus(Dropdown statusDropdown) {
		statusDropdown.removeAllComponents();
		
		RepositoryEntryStatusEnum entryStatus = entry.getEntryStatus();
		statusDropdown.setI18nKey("details.label.status");
		statusDropdown.setElementCssClass("o_repo_tools_status o_with_labeled");
		statusDropdown.setIconCSS("o_icon o_icon_repo_status_".concat(entryStatus.name()));
		statusDropdown.setInnerText(translate(entryStatus.i18nKey()));
		statusDropdown.setInnerCSS("o_labeled o_repo_status_".concat(entryStatus.name()));
		
		if(entryStatus == RepositoryEntryStatusEnum.preparation || entryStatus == RepositoryEntryStatusEnum.review
				|| entryStatus == RepositoryEntryStatusEnum.coachpublished || entryStatus == RepositoryEntryStatusEnum.published
				|| entryStatus == RepositoryEntryStatusEnum.closed) {
			preparationLink = initStatus(statusDropdown, RepositoryEntryStatusEnum.preparation, entryStatus);
			reviewLink = initStatus(statusDropdown, RepositoryEntryStatusEnum.review, entryStatus);
			coachPublishLink = initStatus(statusDropdown, RepositoryEntryStatusEnum.coachpublished, entryStatus);
			publishLink = initStatus(statusDropdown, RepositoryEntryStatusEnum.published, entryStatus);
			if(!RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.close)) {
				closeLink = initStatus(statusDropdown, RepositoryEntryStatusEnum.closed, entryStatus);
			}
		}
		
		stackPanel.setDirty(true);
	}
	
	private Link initStatus(Dropdown statusDropdown, RepositoryEntryStatusEnum entryStatus, RepositoryEntryStatusEnum currentStatus) {
		Link statusLink = LinkFactory.createToolLink("status.".concat(entryStatus.name()), translate(entryStatus.i18nKey()), this);
		statusLink.setIconLeftCSS("o_icon o_icon-fw o_icon_repo_status_".concat(entryStatus.name()));
		statusLink.setElementCssClass("o_labeled o_repo_status_".concat(entryStatus.name()));
		statusLink.setVisible(entryStatus != currentStatus);
		statusDropdown.addComponent(statusLink);
		return statusLink;
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
	
		if (repositoryModule.isCatalogEnabled()) {
			catalogLink = LinkFactory.createLink("details.catalog", getTranslator(), this);
			catalogLink.setElementCssClass("o_sel_catalog");
			buttonsGroup.addButton(catalogLink, false);
		}
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
			} else if("Catalog".equalsIgnoreCase(type)) {
				doOpenCatalog(ureq);
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
		} else if(preparationLink == source) {
			doChangeStatus(ureq, RepositoryEntryStatusEnum.preparation);
		} else if(reviewLink == source) {
			doChangeStatus(ureq, RepositoryEntryStatusEnum.review);
		} else if(coachPublishLink == source) {
			doChangeStatus(ureq, RepositoryEntryStatusEnum.coachpublished);
		} else if(publishLink == source) {
			doChangeStatus(ureq, RepositoryEntryStatusEnum.published);
		} else if(closeLink == source) {
			doConfirmCloseResource(ureq);
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
		} else if(confirmCloseCtrl == source) {
			if(event == Event.CANCELLED_EVENT ) {
				cmc.deactivate();
				cleanUp();
			} else if(event instanceof EntryChangedEvent) {
				cmc.deactivate();
				cleanUp();
				doCloseResource(ureq);
			}
		} else if(cmc == source) {
			cleanUp();
		} else if(event == Event.CHANGED_EVENT || event instanceof ReloadSettingsEvent) {
			fireEvent(ureq, event);
		}
		super.event(ureq, source, event);
	}
	
	protected void cleanUp() {
		removeAsListenerAndDispose(confirmCloseCtrl);
		removeAsListenerAndDispose(metadataCtrl);
		removeAsListenerAndDispose(catalogCtrl);
		removeAsListenerAndDispose(accessCtrl);
		removeAsListenerAndDispose(infoCtrl);
		removeAsListenerAndDispose(cmc);
		confirmCloseCtrl = null;
		metadataCtrl = null;
		catalogCtrl = null;
		accessCtrl = null;
		infoCtrl = null;
		cmc = null;
	}
	
	protected void doOpenInfos(UserRequest ureq) {
		entry = repositoryService.loadByKey(entry.getKey());
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Info"), null);
		infoCtrl = new RepositoryEntryInfoController(ureq, swControl, entry, readOnly);
		listenTo(infoCtrl);
		mainPanel.setContent(infoCtrl.getInitialComponent());
		buttonsGroup.setSelectedButton(infoLink);
	}
	
	protected void doOpenMetadata(UserRequest ureq) {
		entry = repositoryService.loadByKey(entry.getKey());
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Metadata"), null);
		metadataCtrl = new RepositoryEntryMetadataController(ureq, swControl, entry, readOnly);
		listenTo(metadataCtrl);
		mainPanel.setContent(metadataCtrl.getInitialComponent());
		buttonsGroup.setSelectedButton(metadataLink);
	}
	
	protected void doOpenAccess(UserRequest ureq) {
		entry = repositoryService.loadByKey(entry.getKey());
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Access"), null);
		accessCtrl = new AuthoringEditAccessController(ureq, swControl, entry, readOnly);
		listenTo(accessCtrl);
		mainPanel.setContent(accessCtrl.getInitialComponent());
		buttonsGroup.setSelectedButton(accessLink);
	}
	
	protected void doOpenCatalog(UserRequest ureq) {
		entry = repositoryService.loadByKey(entry.getKey());
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Catalog"), null);
		catalogCtrl = new CatalogSettingsController(ureq, swControl, entry, readOnly);
		listenTo(catalogCtrl);
		mainPanel.setContent(catalogCtrl.getInitialComponent());
		buttonsGroup.setSelectedButton(catalogLink);
	}
	
	protected final void doChangeStatus(UserRequest ureq, RepositoryEntryStatusEnum updatedStatus) {
		entry = repositoryManager.setStatus(entry, updatedStatus);
		initStatus(status);
		fireEvent(ureq, new ReloadSettingsEvent(true, true, false, false));
		event(ureq, buttonsGroup.getSelectedButton(), Event.CHANGED_EVENT);
		
		EntryChangedEvent e = new EntryChangedEvent(entry, getIdentity(), Change.modifiedAccess, "runtime");
		ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(e, RepositoryService.REPOSITORY_EVENT_ORES);
		
		getLogger().info("Change status of {} to {}", entry, updatedStatus);
		ThreadLocalUserActivityLogger.log(RepositoryEntryStatusEnum.loggingAction(updatedStatus), getClass(),
				LoggingResourceable.wrap(entry, OlatResourceableType.genRepoEntry));
	}
	
	private void doConfirmCloseResource(UserRequest ureq) {
		List<RepositoryEntry> entryToClose = Collections.singletonList(entry);
		confirmCloseCtrl = new ConfirmCloseController(ureq, getWindowControl(), entryToClose);
		listenTo(confirmCloseCtrl);
		
		String title = translate("read.only.header", entry.getDisplayname());
		cmc = new CloseableModalController(getWindowControl(), "close", confirmCloseCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	/**
	 * Remove close and edit tools, if in edit mode, pop-up-to root
	 * @param ureq
	 */
	private void doCloseResource(UserRequest ureq) {
		entry = repositoryService.loadByKey(entry.getKey());
		// the runtime will pop this controller

		fireEvent(ureq, RepositoryEntryLifeCycleChangeController.closedEvent);
		EntryChangedEvent e = new EntryChangedEvent(entry, getIdentity(), Change.closed, "runtime");
		ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(e, RepositoryService.REPOSITORY_EVENT_ORES);
	}
}
