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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.UrlElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.catalog.ui.CatalogBCFactory;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.repository.RepositoryService;
import org.olat.resource.accesscontrol.CatalogInfo;

/**
 * 
 * Initial date: Jan 21, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class OfferLinksController extends FormBasicController {

	private final CatalogInfo catalogInfo;
	
	public OfferLinksController(UserRequest ureq, WindowControl wControl, CatalogInfo catalogInfo) {
		super(ureq, wControl, "offer_links");
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.catalogInfo = catalogInfo;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (StringHelper.containsNonWhitespace(catalogInfo.getWebCatalogBusinessPath())) {
			addUrlElement("catalogUrlExtern", catalogInfo.getWebCatalogBusinessPath(), formLayout);
		}
		if (StringHelper.containsNonWhitespace(catalogInfo.getCatalogBusinessPath())) {
			addUrlElement("catalogUrlIntern", catalogInfo.getCatalogBusinessPath(), formLayout);
		}
		if (catalogInfo.getMicrosites() != null && !catalogInfo.getMicrosites().isEmpty()) {
			HashSet<TaxonomyLevel> taxonomyLevels = new HashSet<>(catalogInfo.getMicrosites());
			List<TaxonomyLink> taxonomyLinks = new ArrayList<>(taxonomyLevels.size());
			for (TaxonomyLevel taxonomyLevel : taxonomyLevels) {
				String url = CatalogBCFactory.get(false).getTaxonomyLevelUrl(taxonomyLevel);
				String name = translate("cif.catalog.links.microsite", TaxonomyUIFactory.translateDisplayName(getTranslator(), taxonomyLevel));
				String componentName = "taxonomyLink_" + taxonomyLevel.getKey();
				addUrlElement(componentName, url, formLayout);
				taxonomyLinks.add(new TaxonomyLink(name, componentName));
			}
			taxonomyLinks.sort((l1, l2) -> l1.name().toLowerCase().compareTo(l2.name().toLowerCase()));
			flc.contextPut("taxonomyLinks", taxonomyLinks);
		}
		
		uifactory.addFormSubmitButton("close", formLayout);
	}

	private void addUrlElement(String name, String url, FormItemContainer formLayout) {
		UrlElement urlEl = uifactory.addUrlElement(name, null, url, formLayout);
		urlEl.setQrCodeEnabled(catalogInfo.isShowQRCode());
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	public record TaxonomyLink(String name, String componentName) {
		//
	}
	
}
