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

import java.util.Collection;
import java.util.List;

import org.olat.core.commons.persistence.DB;
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
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.ims.lti13.LTI13Module;
import org.olat.ims.lti13.ui.LTI13ResourceAccessController;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryEntryRuntimeType;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.repository.controllers.EntryChangedEvent.Change;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.ui.settings.AccessOverviewController;
import org.olat.repository.ui.settings.ReloadSettingsEvent;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.ui.AccessConfigurationController;
import org.olat.resource.accesscontrol.ui.AccessConfigurationDisabledController;
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
	private AccessConfigurationDisabledController accessOfferDisabledCtrl;
	private AccessOverviewController accessOverviewCtrl;
	private DialogBoxController confirmDeleteOffersDialog;
	
	protected RepositoryEntry entry;
	protected final boolean readOnly;
	protected RepositoryEntryRuntimeType currentRuntimeType;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ACService acService;
	@Autowired
	private LTI13Module lti13Module;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryHandlerFactory handlerFactory;
	
	public AuthoringEditAccessController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, boolean readOnly) {
		super(ureq, wControl, Util.createPackageTranslator(RepositoryService.class, ureq.getLocale()));
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		this.entry = entry;
		this.readOnly = readOnly;
		currentRuntimeType = entry.getRuntimeType();
		
		mainVC = createVelocityContainer("editproptabpub");
		initAccessShare(ureq, mainVC);
		initAccessOffers(ureq, mainVC);
		if(lti13Module.isEnabled()) {
			initLTI13Access(ureq, mainVC);
		}
		initAccessOverview(ureq, mainVC);
		updateUI();
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
				initAccessShare(ureq, mainVC);
			}
		} else if (source == confirmDeleteOffersDialog) {
			if (DialogBoxUIFactory.isOkEvent(event)) {
				doSaveAccessShare(ureq);
			} else {
				accessShareCtrl.markDirty();
			}
		} else if(accessOffersCtrl == source) {
			if(event == Event.CHANGED_EVENT) {
				doSaveAccessOffers(ureq);
				accessShareCtrl.validateOfferAvailable();
				fireEvent(ureq, new ReloadSettingsEvent());
			}
		}
		
		super.event(ureq, source, event);
	}
	
	private void doConfirmSaveAccessShare(UserRequest ureq) {
		if(accessShareCtrl.getRuntimeType() != currentRuntimeType
				&& accessShareCtrl.getRuntimeType() == RepositoryEntryRuntimeType.embedded
				&& repositoryService.hasUserManaged(entry)) {
			accessShareCtrl.revertRuntimeTypeToStandalone();
			showWarning("warning.user.managed.prevent.embedded");
		} else if (!accessShareCtrl.isPublicVisible() && accessOffersCtrl != null && accessOffersCtrl.getNumOfBookingConfigurations() > 0) {
			String title = translate("confirmation.offers.delete.title");
			String msg = translate("confirmation.offers.delete.text");
			confirmDeleteOffersDialog = activateOkCancelDialog(ureq, title, msg, confirmDeleteOffersDialog);
		} else {
			doSaveAccessShare(ureq);
		}
	}
	
	private void doSaveAccessShare(UserRequest ureq) {
		entry = repositoryManager.setAccess(entry,
				accessShareCtrl.isPublicVisible(),
				accessShareCtrl.getRuntimeType(),
				accessShareCtrl.getSelectedLeaveSetting(),
				accessShareCtrl.canCopy(),
				accessShareCtrl.canReference(),
				accessShareCtrl.canDownload(),
				accessShareCtrl.canIndexMetadata(),
				accessShareCtrl.getSelectedOrganisations());
		accessShareCtrl.validateOfferAvailable();
		// Embedded hasn't any offers
		if(accessShareCtrl.getRuntimeType() == RepositoryEntryRuntimeType.embedded) {
			List<Offer> deletedOfferList = acService.findOfferByResource(entry.getOlatResource(), true, null, null);
			for(Offer offerToDelete:deletedOfferList) {
				acService.deleteOffer(offerToDelete);
			}
			dbInstance.commit();
		}
		
		boolean publicEnabledNow = accessShareCtrl.isPublicVisible() && accessOffersCtrl == null;
		initAccessOffers(ureq, mainVC);
		if (publicEnabledNow) {
			accessOffersCtrl.doAddFirstOffer(ureq);
		}
		initAccessOverview(ureq, mainVC);
		updateUI();
		
		fireEvent(ureq, new ReloadSettingsEvent(true, true, false, false));
		
		// inform anybody interested about this change
		MultiUserEvent modifiedEvent = new EntryChangedEvent(entry, getIdentity(), Change.modifiedAccess, "authoring");
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(modifiedEvent, entry);
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(modifiedEvent, RepositoryService.REPOSITORY_EVENT_ORES);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void initAccessShare(UserRequest ureq, VelocityContainer vc) {
		removeAsListenerAndDispose(accessShareCtrl);
		
		accessShareCtrl = new AuthoringEditAccessShareController(ureq, getWindowControl(), entry, readOnly);
		listenTo(accessShareCtrl);
		vc.put("accessAndBooking", accessShareCtrl.getInitialComponent());
	}
	
	private void doSaveAccessOffers(UserRequest ureq) {
		accessOffersCtrl.commitChanges();
		initAccessOverview(ureq, mainVC);
		
		// inform anybody interested about this change
		MultiUserEvent modifiedEvent = new EntryChangedEvent(entry, getIdentity(), Change.modifiedAccess, "authoring");
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(modifiedEvent, entry);
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(modifiedEvent, RepositoryService.REPOSITORY_EVENT_ORES);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	protected void initAccessOffers(UserRequest ureq, VelocityContainer vc) {
		removeAsListenerAndDispose(accessOffersCtrl);
		removeAsListenerAndDispose(accessOfferDisabledCtrl);
		accessOffersCtrl = null;
		accessOfferDisabledCtrl = null;
		vc.remove("offers");
		
		if (entry.isPublicVisible()) {
			boolean guestSupported = handlerFactory.getRepositoryHandler(entry).supportsGuest(entry);
			Collection<Organisation> defaultOfferOrganisations = repositoryService.getOrganisations(entry);
			
			boolean managedBookings = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.bookings);
			accessOffersCtrl = new AccessConfigurationController(ureq, getWindowControl(), entry.getOlatResource(),
					entry.getDisplayname(), true, true, guestSupported, true, defaultOfferOrganisations,
					RepositoryCatalogInfoFactory.createCatalogInfo(entry, getLocale(), true), readOnly, managedBookings,
					"manual_user/learningresources/Access_configuration#offer");
			accessOffersCtrl.setReStatus(entry.getEntryStatus());
			listenTo(accessOffersCtrl);
			vc.put("offers", accessOffersCtrl.getInitialComponent());
		} else {
			accessOfferDisabledCtrl = new AccessConfigurationDisabledController(ureq, getWindowControl(), "manual_user/learningresources/Access_configuration#offer");
			listenTo(accessOfferDisabledCtrl);
			vc.put("offers", accessOfferDisabledCtrl.getInitialComponent());
		}
	}
	
	private void initLTI13Access(UserRequest ureq, VelocityContainer vc) {
		removeAsListenerAndDispose(lti13AccessCtrl);
		
		lti13AccessCtrl = new LTI13ResourceAccessController(ureq, getWindowControl(), entry, readOnly);
		listenTo(lti13AccessCtrl);
		vc.put("lti13Access", lti13AccessCtrl.getInitialComponent());
	}
	
	private void initAccessOverview(UserRequest ureq, VelocityContainer vc) {
		if (accessOverviewCtrl != null) {
			accessOverviewCtrl.reload();
		} else {
			accessOverviewCtrl = new AccessOverviewController(ureq, getWindowControl(), entry);
			listenTo(accessOverviewCtrl);
			vc.put("accessOverview", accessOverviewCtrl.getInitialComponent());
		}
	}
	
	private void updateUI() {
		boolean standalone = RepositoryEntryRuntimeType.standalone == entry.getRuntimeType();
		if(lti13AccessCtrl != null) {
			lti13AccessCtrl.getInitialComponent().setVisible(standalone);
		}
		if(accessOffersCtrl != null) {
			accessOffersCtrl.getInitialComponent().setVisible(standalone);
		}
		if(accessOfferDisabledCtrl != null) {
			accessOfferDisabledCtrl.getInitialComponent().setVisible(standalone);
		}
	}
}
