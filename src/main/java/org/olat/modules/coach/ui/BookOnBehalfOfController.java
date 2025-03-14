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
package org.olat.modules.coach.ui;

import java.util.Set;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.modules.catalog.CatalogEntrySearchParams;
import org.olat.modules.catalog.ui.BookedEvent;
import org.olat.modules.catalog.ui.CatalogEntryListController;
import org.olat.modules.catalog.ui.CatalogEntryListParams;
import org.olat.modules.curriculum.manager.CurriculumElementDAO;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.provider.free.FreeAccessHandler;
import org.olat.resource.accesscontrol.provider.paypal.PaypalAccessHandler;
import org.olat.resource.accesscontrol.provider.paypalcheckout.PaypalCheckoutAccessHandler;
import io.jsonwebtoken.lang.Collections;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2025-01-21<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class BookOnBehalfOfController extends BasicController {
	private final Identity identity;
	private final VelocityContainer mainVC;

	private final CatalogEntryListController catalogEntryListCtrl;

	@Autowired
	BaseSecurity securityManager;
	
	@Autowired
	ACService acService;
	
	@Autowired
	CurriculumElementDAO curriculumElementDAO; 
	
	public BookOnBehalfOfController(UserRequest ureq, WindowControl wControl, Identity identity, 
									TooledStackedPanel stackPanel) {
		super(ureq, wControl);
		this.identity = identity;
		CatalogEntrySearchParams searchParams = createSearchParams();
		CatalogEntryListParams listParams = createCatalogEntryListParams();

		Identity reloadedIdentity = securityManager.loadIdentityByKey(identity.getKey());
		String userDisplayName = reloadedIdentity.getUser().getFirstName() + " " + reloadedIdentity.getUser().getLastName();
		String title = translate("book.on.behalf.of", userDisplayName);

		mainVC = createVelocityContainer("book_on_behalf_of");
		mainVC.contextPut("title", title);

		catalogEntryListCtrl = new CatalogEntryListController(ureq, wControl, stackPanel, searchParams, listParams);
		listenTo(catalogEntryListCtrl);

		mainVC.put("catalogEntryList", catalogEntryListCtrl.getInitialComponent());

		putInitialPanel(mainVC);
	}

	private CatalogEntrySearchParams createSearchParams() {
		CatalogEntrySearchParams searchParams = new CatalogEntrySearchParams();
		searchParams.setMember(identity);
		searchParams.setOfferOrganisations(acService.getOfferOrganisations(identity));
		searchParams.setBookOnBehalfOf(true);
		return searchParams;
	}

	private CatalogEntryListParams createCatalogEntryListParams() {
		CatalogEntryListParams listParams = new CatalogEntryListParams();
		listParams.setExcludeRepositoryEntries(true);
		Set<Long> reservedCurriculumElementKeys = Collections.asSet(curriculumElementDAO.loadReservedElementKeys(identity));
		listParams.setExcludedCurriculumElementKeys(reservedCurriculumElementKeys);
		listParams.setExcludeMembers(true);
		listParams.setExcludedAccessMethodTypes(Set.of(
				PaypalAccessHandler.METHOD_TYPE, PaypalCheckoutAccessHandler.METHOD_TYPE, FreeAccessHandler.METHOD_TYPE
		));
		listParams.setFireBookedEvent(true);
		return listParams;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == catalogEntryListCtrl) {
			if (event instanceof BookedEvent) {
				fireEvent(ureq, event);
			}
		}
		super.event(ureq, source, event);
	}
}
