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

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.AuthHelper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.image.ImageComponent;
import org.olat.core.gui.components.link.ExternalLink;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.catalog.CatalogEntry;
import org.olat.modules.catalog.CatalogEntrySearchParams;
import org.olat.modules.catalog.CatalogV2Module;
import org.olat.modules.catalog.CatalogV2Service;
import org.olat.modules.catalog.ui.CatalogBCFactory;
import org.olat.modules.oaipmh.OAIPmhModule;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.PriceMethod;
import org.olat.repository.ui.RepositoyUIFactory;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.Offer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RepositoryEntryResourceInfoDetailsHeaderController extends FormBasicController {

	public static final Event START_EVENT = new Event("start");

	private final RepositoryEntry entry;
	private final List<PriceMethod> types = new ArrayList<>(1);

	@Autowired
	protected RepositoryService repositoryService;
	@Autowired
	protected OAIPmhModule oaiPmhModule;
	@Autowired
	private CatalogV2Module catalogModule;
	@Autowired
	private CatalogV2Service catalogService;


	public RepositoryEntryResourceInfoDetailsHeaderController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		super(ureq, wControl, Util.getPackageVelocityRoot(RepositoryEntryDetailsController.class) + "/details_header.html");
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.entry = entry;
		initForm(ureq);
	}

	public List<PriceMethod> getTypes() {
		return types;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (formLayout instanceof FormLayoutContainer formLayoutContainer) {
			formLayoutContainer.contextPut("v", entry);

			addThumbnailIfPresent(ureq, formLayoutContainer);
			setupContext(formLayoutContainer);
			setupAccessLinks(ureq, formLayoutContainer);
		}
	}

	private void addThumbnailIfPresent(UserRequest ureq, FormLayoutContainer container) {
		VFSLeaf movie = repositoryService.getIntroductionMovie(entry);
		VFSLeaf image = repositoryService.getIntroductionImage(entry);
		if (image != null || movie != null) {
			ImageComponent ic = new ImageComponent(ureq.getUserSession(), "thumbnail");
			if (movie != null) {
				ic.setMedia(movie);
				ic.setMaxWithAndHeightToFitWithin(RepositoryManager.PICTURE_WIDTH, RepositoryManager.PICTURE_HEIGHT);
				if (image != null) {
					ic.setPoster(image);
				}
			} else {
				ic.setMedia(image);
				ic.setMaxWithAndHeightToFitWithin(RepositoryManager.PICTURE_WIDTH, RepositoryManager.PICTURE_HEIGHT);
			}
			container.put("thumbnail", ic);
		}
	}

	private void setupContext(FormLayoutContainer container) {
		container.contextPut("v", entry);
		container.contextPut("cssClass", RepositoyUIFactory.getIconCssClass(entry));

		if (entry.getEducationalType() != null) {
			String educationalType = translate(RepositoyUIFactory.getI18nKey(entry.getEducationalType()));
			container.contextPut("educationalType", educationalType);
		}
	}

	private void setupAccessLinks(UserRequest ureq, FormLayoutContainer container) {
		if (isUserLoggedInOrGuestAccess(ureq)) {
			createStartLink(container);
		} else if (isAvailableInCatalog()) {
			uifactory.addStaticTextElement(
					"catalog.bookable", null, translate("catalog.bookable"), container);

			String catalogUrl = CatalogBCFactory.get(true).getOfferUrl(entry.getOlatResource());

			ExternalLink catalogLink = LinkFactory.createExternalLink("showCatalog", "", catalogUrl);
			catalogLink.setElementCssClass("o_offer_login_buton btn btn-default btn-primary");
			catalogLink.setName(translate("catalog.button.label"));
			catalogLink.setTarget("_self");
			container.put("showCatalog", catalogLink);
		} else {
			createLoginLink(container);
		}
	}

	private boolean isUserLoggedInOrGuestAccess(UserRequest ureq) {
		boolean isGuestAccess = oaiPmhModule.getOffers(entry.getOlatResource()).stream().anyMatch(Offer::isGuestAccess);
		return (ureq.getUserSession().getRoles() != null && !ureq.getUserSession().getRoles().isGuestOnly()) || isGuestAccess;
	}

	private boolean isAvailableInCatalog() {
		if (catalogModule.isEnabled()
				&& catalogModule.isWebPublishEnabled()
				&& !catalogModule.isWebPublishTemporarilyDisabled()) {
			OLATResource resource = entry.getOlatResource();
			CatalogEntrySearchParams searchParams = new CatalogEntrySearchParams();
			searchParams.setWebPublish(true);
			searchParams.setResourceKeys(List.of(resource.getKey()));
			List<CatalogEntry> catalogEntries = catalogService.getCatalogEntries(searchParams);

			return !catalogEntries.isEmpty();
		}
		return false;
	}

	private FormLink createStartLink(FormLayoutContainer layoutCont) {
		String linkText = translate("start.with.type", translate(entry.getOlatResource().getResourceableTypeName()));
		FormLink link = uifactory.addFormLink("start", START_EVENT.getCommand(), linkText, null, layoutCont, Link.BUTTON + Link.NONTRANSLATED);
		link.setIconRightCSS("o_icon o_icon_start o_icon-lg");
		link.setPrimary(true);
		link.setFocus(true);
		link.setElementCssClass("o_start btn-block");
		return link;
	}

	private FormLink createLoginLink(FormLayoutContainer layoutCont) {
		String linkText = "Zum Login";
		FormLink link = uifactory.addFormLink("start", "login", linkText, null, layoutCont, Link.BUTTON + Link.NONTRANSLATED);
		link.setPrimary(true);
		link.setElementCssClass("o_start btn-block");
		return link;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof FormLink formLinkSource) {
			String cmd = formLinkSource.getCmd();
			if ("start".equals(cmd)) {
				fireEvent(ureq, START_EVENT);
			} else if ("login".equals(cmd)) {
				AuthHelper.doLogout(ureq);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
