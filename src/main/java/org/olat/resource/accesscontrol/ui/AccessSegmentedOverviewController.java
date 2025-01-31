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
package org.olat.resource.accesscontrol.ui;

import java.util.Collection;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.ButtonGroupComponent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Organisation;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.StringHelper;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.CatalogInfo;
import org.olat.resource.accesscontrol.CatalogInfo.CatalogStatusEvaluator;

/**
 * 
 * Initial date: 27 janv. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class AccessSegmentedOverviewController extends BasicController {

	private Link bookingsLink ;
	private Link accessConfigLink;
	private final VelocityContainer mainVC;
	private final ButtonGroupComponent buttonsGroup = new ButtonGroupComponent("settings");

	private final boolean readOnly;
	private final boolean managedBookings;
	private final OLATResource resource;
	
	private OrdersAdminController ordersCtrl;
	private final AccessConfigurationController accessConfigCtrl;
	
	public AccessSegmentedOverviewController(UserRequest ureq, WindowControl wControl, OLATResource resource,
			String displayName, boolean allowPaymentMethod, boolean openAccessSupported, boolean guestSupported,
			boolean offerOrganisationsSupported, Collection<Organisation> defaultOfferOrganisations,
			CatalogInfo catalogInfo, boolean readOnly, boolean managedBookings, String helpUrl) {
		super(ureq, wControl);
		
		this.readOnly = readOnly;
		this.resource = resource;
		this.managedBookings = managedBookings;
		
		accessConfigCtrl = new AccessConfigurationController(ureq, wControl, resource,
				displayName, allowPaymentMethod, openAccessSupported, guestSupported,
				offerOrganisationsSupported, defaultOfferOrganisations,
				catalogInfo, readOnly, managedBookings, true, helpUrl);
		listenTo(accessConfigCtrl);
		
		mainVC = createVelocityContainer("configuration_catalog");
		mainVC.put("content", accessConfigCtrl.getInitialComponent());
		
		accessConfigLink = LinkFactory.createLink("access.config", getTranslator(), this);
		accessConfigLink.setElementCssClass("o_sel_access_offers");
		buttonsGroup.addButton(accessConfigLink, true);
		bookingsLink = LinkFactory.createLink("access.bookings", getTranslator(), this);
		bookingsLink.setElementCssClass("o_sel_access_bookings");
		buttonsGroup.addButton(bookingsLink, false);
		mainVC.put("segments", buttonsGroup);
		
		updateCatalogStatusUI();
		putInitialPanel(mainVC);
	}

	private void updateCatalogStatusUI() {
		String internalCatalog = accessConfigCtrl.getInternalCatalogStatus();
		if(StringHelper.containsNonWhitespace(internalCatalog)) {
			mainVC.contextPut("internalCatalog", internalCatalog);
		}
		String externalCatalog = accessConfigCtrl.getExternalCatalogStatus();
		if(StringHelper.containsNonWhitespace(externalCatalog)) {
			mainVC.contextPut("externalCatalog", externalCatalog);
		}
	}
	
	public void setStatusEvaluator(CatalogStatusEvaluator statusEvaluator) {
		accessConfigCtrl.setStatusEvaluator(statusEvaluator);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(accessConfigLink == source) {
			 doOpenAccessConfiguration();
		} else if(bookingsLink == source) {
			doOpenBookingsList(ureq);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == accessConfigCtrl) {
			if(event == Event.CHANGED_EVENT) {
				accessConfigCtrl.commitChanges();
				updateCatalogStatusUI();
			} else if(event instanceof OpenOrdersEvent ooe) {
				String path = "[All:0][OfferAccess:" + ooe.getOfferAccess().getKey() + "]";
				List<ContextEntry> entries = BusinessControlFactory.getInstance().createCEListFromString(path);
				doOpenBookingsList(ureq).activate(ureq, entries, null);
			}
			fireEvent(ureq, event);
		}
		super.event(ureq, source, event);
	}
	
	private void doOpenAccessConfiguration() {
		mainVC.put("content", accessConfigCtrl.getInitialComponent());
		buttonsGroup.setSelectedButton(accessConfigLink);
	}
	
	private OrdersAdminController doOpenBookingsList(UserRequest ureq) {
		if(ordersCtrl == null) {
			ordersCtrl = new OrdersAdminController(ureq, getWindowControl(), resource, (readOnly || managedBookings));
			listenTo(ordersCtrl);
		}
		mainVC.put("content", ordersCtrl.getInitialComponent());
		buttonsGroup.setSelectedButton(bookingsLink);
		return ordersCtrl;
	}
}
