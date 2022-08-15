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
package org.olat.repository.ui.author;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Organisation;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.ims.lti13.LTI13Module;
import org.olat.ims.lti13.ui.LTI13ResourceAccessController;
import org.olat.modules.catalog.CatalogV2Module;
import org.olat.modules.taxonomy.model.TaxonomyLevelNamePath;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.repository.CatalogEntry;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.repository.controllers.EntryChangedEvent.Change;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.manager.CatalogManager;
import org.olat.repository.ui.settings.AccessOverviewController;
import org.olat.repository.ui.settings.ReloadSettingsEvent;
import org.olat.resource.accesscontrol.CatalogInfo;
import org.olat.resource.accesscontrol.ui.AccessConfigurationController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Configuration of the BARG and access control of a learn resource.
 * 
 * 
 * Initial date: 06.05.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AuthoringEditAccessController extends BasicController {
	
	private VelocityContainer mainVC;
	
	private LTI13ResourceAccessController lti13AccessCtrl;
	private AuthoringEditAccessShareController accessShareCtrl;
	private AccessConfigurationController accessOffersCtrl;
	private AccessOverviewController accessOverviewCtrl;
	private DialogBoxController confirmDeleteCalendarDialog;
	
	private RepositoryEntry entry;
	private final boolean readOnly;
	
	@Autowired
	private LTI13Module lti13Module;
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryHandlerFactory handlerFactory;
	@Autowired
	private CatalogV2Module catalogModule;
	@Autowired
	private CatalogManager cataloogManager;
	
	public AuthoringEditAccessController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, boolean readOnly) {
		super(ureq, wControl, Util.createPackageTranslator(RepositoryService.class, ureq.getLocale()));
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		this.entry = entry;
		this.readOnly = readOnly;
		
		mainVC = createVelocityContainer("editproptabpub");
		initAccessShare(ureq);
		initAccessOffers(ureq);
		if(lti13Module.isEnabled()) {
			initLTI13Access(ureq);
		}
		initAccessOverview(ureq);
		putInitialPanel(mainVC);
	}
	
	public RepositoryEntry getEntry() {
		return entry;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(accessShareCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doConfirmSaveAccessShare(ureq);
			} else if(event == Event.CANCELLED_EVENT) {
				initAccessShare(ureq);
			}
		} else if (source == confirmDeleteCalendarDialog) {
			if (DialogBoxUIFactory.isOkEvent(event)) {
				doSaveAccessShare(ureq);
			} else {
				accessShareCtrl.markDirty();
			}
		} else if(accessOffersCtrl == source) {
			if(event == Event.CHANGED_EVENT) {
				doSaveAccessOffers(ureq);
				fireEvent(ureq, new ReloadSettingsEvent());
			}
		}
		
		super.event(ureq, source, event);
	}
	
	private void doConfirmSaveAccessShare(UserRequest ureq) {
		if (!accessShareCtrl.isPublicVisible() && accessOffersCtrl != null && accessOffersCtrl.getNumOfBookingConfigurations() > 0) {
			String title = translate("confirmation.offers.delete.title");
			String msg = translate("confirmation.offers.delete.text");
			confirmDeleteCalendarDialog = activateOkCancelDialog(ureq, title, msg, confirmDeleteCalendarDialog);
		} else {
			doSaveAccessShare(ureq);
		}
	}
	
	private void doSaveAccessShare(UserRequest ureq) {
		entry = repositoryManager.setAccess(entry,
				accessShareCtrl.isPublicVisible(),
				accessShareCtrl.getSelectedLeaveSetting(),
				accessShareCtrl.canCopy(),
				accessShareCtrl.canReference(),
				accessShareCtrl.canDownload(),
				accessShareCtrl.getSelectedOrganisations());
		initAccessOffers(ureq);
		initAccessOverview(ureq);
		
		fireEvent(ureq, new ReloadSettingsEvent(true, true, false, false));
		
		// inform anybody interested about this change
		MultiUserEvent modifiedEvent = new EntryChangedEvent(entry, getIdentity(), Change.modifiedAccess, "authoring");
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(modifiedEvent, entry);
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(modifiedEvent, RepositoryService.REPOSITORY_EVENT_ORES);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void initAccessShare(UserRequest ureq) {
		removeAsListenerAndDispose(accessShareCtrl);
		
		accessShareCtrl = new AuthoringEditAccessShareController(ureq, getWindowControl(), entry, readOnly);
		listenTo(accessShareCtrl);
		mainVC.put("accessAndBooking", accessShareCtrl.getInitialComponent());
	}
	
	private void doSaveAccessOffers(UserRequest ureq) {
		accessOffersCtrl.commitChanges();
		initAccessOverview(ureq);
		
		// inform anybody interested about this change
		MultiUserEvent modifiedEvent = new EntryChangedEvent(entry, getIdentity(), Change.modifiedAccess, "authoring");
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(modifiedEvent, entry);
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(modifiedEvent, RepositoryService.REPOSITORY_EVENT_ORES);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void initAccessOffers(UserRequest ureq) {
		removeAsListenerAndDispose(accessOffersCtrl);
		accessOffersCtrl = null;
		mainVC.remove("offers");
		
		if (entry.isPublicVisible()) {
			boolean guestSupported = handlerFactory.getRepositoryHandler(entry).supportsGuest(entry);
			Collection<Organisation> defaultOfferOrganisations = repositoryService.getOrganisations(entry);
			
			boolean managedBookings = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.bookings);
			accessOffersCtrl = new AccessConfigurationController(ureq, getWindowControl(), entry.getOlatResource(),
					entry.getDisplayname(), true, true, guestSupported, true, defaultOfferOrganisations,
					createCatalogInfo(), readOnly, managedBookings,
					"manual_user/course_create/Access_configuration#offer");
			accessOffersCtrl.setReStatus(entry.getEntryStatus());
			listenTo(accessOffersCtrl);
			mainVC.put("offers", accessOffersCtrl.getInitialComponent());
		}
	}
	
	private CatalogInfo createCatalogInfo() {
		if (catalogModule.isEnabled()) {
			String details = null;
			String editBusinessPath = null;
			List<TaxonomyLevelNamePath> taxonomyLevels = TaxonomyUIFactory.getNamePaths(getTranslator(), repositoryService.getTaxonomy(entry));
			if (taxonomyLevels.isEmpty()) {
				details = translate("access.no.taxonomy.level");
				editBusinessPath = "[RepositoryEntry:" + entry.getKey() + "][Settings:0][Metadata:0]";
			} else {
				StringBuilder sb = new StringBuilder();
				for (TaxonomyLevelNamePath taxonomyLevel : taxonomyLevels) {
					sb.append("<span class=\"o_tag o_taxonomy\" title=\"");
					sb.append(StringHelper.escapeHtml(taxonomyLevel.getMaterializedPathIdentifiersWithoutSlash()));
					sb.append("\">");
					sb.append(taxonomyLevel.getDisplayName());
					sb.append("</span>");
				}
				details = sb.toString();
			}
			return new CatalogInfo(true, true, details, editBusinessPath, translate("access.open.metadata"));
		} else if (repositoryModule.isCatalogEnabled()) {
			String details = null;
			String editBusinessPath = null;
			List<CatalogEntry> catalogEntries = cataloogManager.getCatalogCategoriesFor(entry);
			if (catalogEntries.isEmpty()) {
				details = translate("access.no.catalog.entry");
				editBusinessPath = "[RepositoryEntry:" + entry.getKey() + "][Settings:0][Catalog:0]";
			} else {
				List<String> catalogEntryPaths = new ArrayList<>(catalogEntries.size());
				for (CatalogEntry catalogEntry : catalogEntries) {
					List<String> names = new ArrayList<>();
					addParentNames(names, catalogEntry);
					Collections.reverse(names);
					String path = names.stream().collect(Collectors.joining("/"));
					path = "/" + path;
					catalogEntryPaths.add(path);
				}
				details = catalogEntryPaths.stream()
						.sorted()
						.collect(Collectors.joining(", "));
			}
			return new CatalogInfo(true, true, details, editBusinessPath, translate("access.open.catalog"));
		}
		return CatalogInfo.UNSUPPORTED;
	}

	private void addParentNames(List<String> names, CatalogEntry catalogEntry) {
		String name = StringHelper.containsNonWhitespace(catalogEntry.getShortTitle())
				? catalogEntry.getShortTitle()
				: catalogEntry.getName();
		names.add(name);
		if (catalogEntry.getParent() != null) {
			addParentNames(names, catalogEntry.getParent());
		}
	}

	private void initLTI13Access(UserRequest ureq) {
		removeAsListenerAndDispose(lti13AccessCtrl);
		
		lti13AccessCtrl = new LTI13ResourceAccessController(ureq, getWindowControl(), entry, readOnly);
		listenTo(lti13AccessCtrl);
		mainVC.put("lti13Access", lti13AccessCtrl.getInitialComponent());
	}
	
	private void initAccessOverview(UserRequest ureq) {
		if (accessOverviewCtrl != null) {
			accessOverviewCtrl.reload();
		} else {
			accessOverviewCtrl = new AccessOverviewController(ureq, getWindowControl(), entry);
			listenTo(accessOverviewCtrl);
			mainVC.put("accessOverview", accessOverviewCtrl.getInitialComponent());
		}
	}
}
