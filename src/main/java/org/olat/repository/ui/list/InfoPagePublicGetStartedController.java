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
package org.olat.repository.ui.list;

import java.util.List;

import org.olat.basesecurity.AuthHelper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.ExternalLink;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Util;
import org.olat.modules.catalog.CatalogEntry;
import org.olat.modules.catalog.CatalogEntrySearchParams;
import org.olat.modules.catalog.CatalogV2Module;
import org.olat.modules.catalog.CatalogV2Service;
import org.olat.modules.catalog.ui.CatalogBCFactory;
import org.olat.modules.oaipmh.OAIPmhModule;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.Offer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The anonymous-visitor "Get started" card for the public resource info page
 * (/resourceinfo/&lt;key&gt;) - start link if logged in or guest access is
 * allowed, catalog link if the entry is web-published, login link otherwise.
 * Independent of {@link AbstractInfoPageGetStartedController} - there is no
 * membership, no offers, no leave, just this three-way choice.
 *
 * Initial date: 19 Aug 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class InfoPagePublicGetStartedController extends BasicController {

	public static final Event START_EVENT = new Event("start");

	private static final String CMD_START = "start";
	private static final String CMD_LOGIN = "login";

	private final RepositoryEntry entry;

	@Autowired
	private OAIPmhModule oaiPmhModule;
	@Autowired
	private CatalogV2Module catalogModule;
	@Autowired
	private CatalogV2Service catalogService;

	public InfoPagePublicGetStartedController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		super(ureq, wControl, Util.createPackageTranslator(RepositoryService.class, ureq.getLocale()));
		setVelocityRoot(Util.getPackageVelocityRoot(RepositoryEntryDetailsController.class));
		this.entry = entry;

		VelocityContainer mainVC = createVelocityContainer("get_started");
		putInitialPanel(mainVC);

		if (isUserLoggedInOrGuestAccess(ureq)) {
			Link startLink = LinkFactory.createCustomLink(CMD_START, CMD_START,
					translate("open.with.type", translate(entry.getOlatResource().getResourceableTypeName())),
					Link.BUTTON | Link.NONTRANSLATED, mainVC, this);
			startLink.setIconRightCSS("o_icon o_icon_start o_icon-lg");
			startLink.setPrimary(true);
			startLink.setElementCssClass("o_start o_button_call_to_action");
		} else if (isAvailableInCatalog()) {
			mainVC.contextPut("catalogBookable", translate("catalog.bookable"));
			String catalogUrl = CatalogBCFactory.get(true).getOfferUrl(entry.getOlatResource());
			ExternalLink catalogLink = LinkFactory.createExternalLink("showCatalog", "", catalogUrl);
			catalogLink.setElementCssClass("o_offer_login_buton btn btn-default btn-primary");
			catalogLink.setName(translate("catalog.button.label"));
			catalogLink.setTarget("_self");
			mainVC.put("showCatalog", catalogLink);
		} else {
			Link loginLink = LinkFactory.createCustomLink(CMD_START, CMD_LOGIN, translate("resourceinfo.login"),
					Link.BUTTON | Link.NONTRANSLATED, mainVC, this);
			loginLink.setPrimary(true);
			loginLink.setElementCssClass("o_start o_button_call_to_action");
		}
	}

	public boolean hasContent() {
		// One of the three branches in the constructor always fires.
		return true;
	}

	private boolean isUserLoggedInOrGuestAccess(UserRequest ureq) {
		boolean isGuestAccess = oaiPmhModule.getOffers(entry.getOlatResource()).stream().anyMatch(Offer::isGuestAccess);
		return (ureq.getUserSession().getRoles() != null && !ureq.getUserSession().getRoles().isGuestOnly()) || isGuestAccess;
	}

	private boolean isAvailableInCatalog() {
		if (catalogModule.isEnabled() && catalogModule.isWebPublishEnabled() && !catalogModule.isWebPublishTemporarilyDisabled()) {
			OLATResource resource = entry.getOlatResource();
			CatalogEntrySearchParams searchParams = new CatalogEntrySearchParams();
			searchParams.setWebPublish(true);
			searchParams.setResourceKeys(List.of(resource.getKey()));
			List<CatalogEntry> catalogEntries = catalogService.getCatalogEntries(searchParams);
			return !catalogEntries.isEmpty();
		}
		return false;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link link) {
			if (CMD_START.equals(link.getCommand())) {
				fireEvent(ureq, START_EVENT);
			} else if (CMD_LOGIN.equals(link.getCommand())) {
				AuthHelper.doLogout(ureq);
			}
		}
	}

}
