/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.resource.accesscontrol.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.olat.resource.accesscontrol.ui.OfferSelectionController.OfferSelectedEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Jan 13, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class OffersController extends BasicController {
	
	public static final Event LOGIN_EVENT = new Event("login.or.register");

	private final VelocityContainer mainVC;
	
	private OfferSelectionController offerSelectionCtrl;
	private OfferDetailsController detailsCtrl;
	private Controller accessCtrl;
	private Identity bookedIdentity;

	private final boolean webCatalog;
	
	@Autowired
	private AccessControlModule acModule;

	public OffersController(UserRequest ureq, WindowControl wControl, List<OfferAccess> offers, boolean withTitle, boolean webCatalog, Identity bookedIdentity) {
		super(ureq, wControl);
		this.webCatalog = webCatalog;
		this.bookedIdentity = bookedIdentity;
		mainVC = createVelocityContainer("offers");
		putInitialPanel(mainVC);
		
		mainVC.contextPut("title", Boolean.valueOf(withTitle));
		
		if (offers.size() > 1) {
			offerSelectionCtrl = new OfferSelectionController(ureq, wControl, offers);
			listenTo(offerSelectionCtrl);
			mainVC.put("offerSelection", offerSelectionCtrl.getInitialComponent());
		}
		
		updateOfferUI(ureq, offers.get(0));
	}
	
	public void setWarning(String warning) {
		mainVC.contextPut("warning", warning);
	}
	
	public void setError(String error) {
		mainVC.contextPut("error", error);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == offerSelectionCtrl) {
			if (event instanceof OfferSelectedEvent osEvent) {
				updateOfferUI(ureq, osEvent.getOffer());
			}
		} else if (source == accessCtrl) {
			if (event instanceof AccessEvent aeEvent) {
				if (event.equals(AccessEvent.ACCESS_OK_EVENT)) {
					fireEvent(ureq, AccessEvent.ACCESS_OK_EVENT);
				} else {
					if(StringHelper.containsNonWhitespace(aeEvent.getMessage())) {
						getWindowControl().setError(aeEvent.getMessage());
					} else {
						showError("error.accesscontrol");
					}
				}
			} else if (event == LOGIN_EVENT) {
				fireEvent(ureq, event);
			}
		}
		super.event(ureq, source, event);
	}

	private void updateOfferUI(UserRequest ureq, OfferAccess offerAccess) {
		removeAsListenerAndDispose(detailsCtrl);
		removeAsListenerAndDispose(accessCtrl);
		
		detailsCtrl = new OfferDetailsController(ureq, getWindowControl(), offerAccess);
		listenTo(detailsCtrl);
		mainVC.put("details", detailsCtrl.getInitialComponent());
		
		if (webCatalog) {
			accessCtrl = new LoginOrRegisterController(ureq, getWindowControl());
		} else {
			AccessMethodHandler handler = acModule.getAccessMethodHandler(offerAccess.getMethod().getType());
			accessCtrl = handler.createAccessController(ureq, getWindowControl(), offerAccess, bookedIdentity);
		}
		listenTo(accessCtrl);
		mainVC.put("offer", accessCtrl.getInitialComponent());
	}

}
