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
package org.olat.repository.ui.author;

import java.util.Collection;
import java.util.List;

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
import org.olat.ims.lti13.DeploymentConfigurationPermission;
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
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class AuthoringEditAccessController extends BasicController {
	
	protected final VelocityContainer mainVC;
	
	private LTI13ResourceAccessController lti13AccessCtrl;
	private AuthoringEditAccessShareController accessShareCtrl;
	private AccessConfigurationController accessOffersCtrl;
	private AccessConfigurationDisabledController accessOfferDisabledCtrl;
	private AuthoringEditRuntimeTypeController runtimeTypeCtrl;
	private AccessOverviewController accessOverviewCtrl;
	private DialogBoxController confirmDeleteOffersDialog;
	
	protected RepositoryEntry entry;
	protected final boolean readOnly;
	
	@Autowired
	private ACService acService;
	@Autowired
	private LTI13Module lti13Module;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	protected RepositoryService repositoryService;
	@Autowired
	private RepositoryHandlerFactory handlerFactory;
	
	public AuthoringEditAccessController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, boolean readOnly) {
		super(ureq, wControl, Util.createPackageTranslator(RepositoryService.class, ureq.getLocale()));
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		this.entry = entry;
		this.readOnly = readOnly;
		
		mainVC = createVelocityContainer("editproptabpub");

		initRuntimeType(ureq, mainVC);
		initAccessShare(ureq, mainVC);
		initAccessOffers(ureq, mainVC);
		// LTI13 is restricted. Only if it is activated for all groups or activated for course owners with author role || administrator can always access that area
		boolean isLTI13AccessAllowed = lti13Module.getDeploymentRepositoryEntryOwnerPermission() == DeploymentConfigurationPermission.allResources
				|| entry.isLTI13DeploymentByOwnerWithAuthorRightsEnabled() || ureq.getUserSession().getRoles().isAdministrator();
		// check if lti Module is enabled and if entry is a course
		// an LTI release only makes sense in a course OO-7664
		if(lti13Module.isEnabled() && isLTI13AccessAllowed && entry.getOlatResource().getResourceableTypeName().equals("CourseModule")) {
			initLTI13Access(ureq, mainVC);
		}
		initAccessOverview(ureq, mainVC);
		updateUI();
		putInitialPanel(mainVC);
		validateOfferAvailable();
	}

	private boolean isCurricularCourse() {
		if (!entry.getOlatResource().getResourceableTypeName().equals("CourseModule")) {
			return false;
		}
		return RepositoryEntryRuntimeType.curricular.equals(entry.getRuntimeType());
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
		if(runtimeTypeCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CANCELLED_EVENT) {
				onRuntimeTypeChange(ureq);
			}
		} else if(accessShareCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doSaveAccessShare(ureq);
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
				fireEvent(ureq, new ReloadSettingsEvent());
			}
		}
		
		super.event(ureq, source, event);
	}
	
	private void onRuntimeTypeChange(UserRequest ureq) {
		// Reload the repository entry and the UI
		entry = repositoryService.loadBy(entry);
		initRuntimeType(ureq, mainVC);
		initAccessOffers(ureq, mainVC);
		initAccessShare(ureq, mainVC);
		initAccessOverview(ureq, mainVC);
		updateUI();
		validateOfferAvailable();
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void doSaveAccessShare(UserRequest ureq) {
		entry = repositoryManager.setAccess(entry,
				accessShareCtrl.isPublicVisible(),
				accessShareCtrl.getSelectedLeaveSetting(),
				accessShareCtrl.canCopy(),
				accessShareCtrl.canReference(),
				accessShareCtrl.canDownload(),
				accessShareCtrl.canIndexMetadata(),
				accessShareCtrl.getSelectedOrganisations());
		validateOfferAvailable();
		
		boolean publicEnabledNow = accessShareCtrl.isPublicVisible() && accessOffersCtrl == null;
		initAccessOffers(ureq, mainVC);
		if (publicEnabledNow) {
			accessOffersCtrl.doCreateOffer(ureq);
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
	
	private void validateOfferAvailable() {
		if (accessShareCtrl.isPublicVisible()) {
			List<Offer> offers = acService.findOfferByResource(entry.getOlatResource(), true, null, null);
			if (offers.isEmpty()) {
				mainVC.contextPut("warningOffersMsg", translate("error.public.no.offers"));
			} else {
				mainVC.contextRemove("warningOffersMsg");
			}
		} else {
			mainVC.contextRemove("warningOffersMsg");
		}
		if (repositoryService.isMixedSetup(entry)) {
			mainVC.contextPut("warningMixedSetup", translate("error.mixed.setup"));
		} else {
			mainVC.contextRemove("warningMixedSetup");
		}
	}
	
	private void initRuntimeType(UserRequest ureq, VelocityContainer vc) {
		removeAsListenerAndDispose(runtimeTypeCtrl);
		
		runtimeTypeCtrl = new AuthoringEditRuntimeTypeController(ureq, this.getWindowControl(), entry, readOnly);
		listenTo(runtimeTypeCtrl);
		vc.put("runtimeType", runtimeTypeCtrl.getInitialComponent());
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
					RepositoryCatalogInfoFactory.createCatalogInfo(entry, getLocale(), true, true), readOnly, managedBookings, false,
					"manual_user/learningresources/Access_configuration#offer");
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
		if (accessShareCtrl != null) {
			accessShareCtrl.getInitialComponent().setVisible(!isCurricularCourse());
		}

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
