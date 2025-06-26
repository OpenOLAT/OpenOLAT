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
package org.olat.modules.catalog.ui;

import java.util.Objects;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.vfs.VFSMediaMapper;
import org.olat.modules.catalog.CatalogV2Module;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 May 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CatalogSearchHeaderController extends FormBasicController {
	
	private TextElement searchEl;
	private FormLink searchLink;
	private FormLink exploreLink;
	
	private final CatalogBCFactory bcFactory;
	private String header;
	private Integer totalCatalogEntries;

	@Autowired
	private CatalogV2Module catalogModule;

	public CatalogSearchHeaderController(UserRequest ureq, WindowControl wControl, boolean webCatalog) {
		super(ureq, wControl, "header_search");
		bcFactory = CatalogBCFactory.get(webCatalog);
		
		initForm(ureq);
		setTotalCatalogEntries(Integer.valueOf(0));
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer searchCont = FormLayoutContainer.createInputGroupLayout("searchWrapper", getTranslator(), null, null);
		searchCont.contextPut("inputGroupSizeCss", "input-group-lg");
		formLayout.add("searchWrapper", searchCont);
		searchCont.setRootForm(mainForm);
		
		searchEl = uifactory.addTextElement("search", 100, null, searchCont);
		searchEl.setPlaceholderText(translate("search.placeholder"));
		searchEl.setAriaLabel("enter.search.term");
		searchEl.setDomReplacementWrapperRequired(false);
		searchEl.setFocus(true);
		
		searchLink = uifactory.addFormLink("rightAddOn", "", "", searchCont, Link.NONTRANSLATED);
		searchLink.setElementCssClass("input-group-addon");
		searchLink.setCustomEnabledLinkCSS("o_catalog_search_button o_undecorated");
		searchLink.setIconLeftCSS("o_icon o_icon-fw o_icon_search o_icon-lg");
		String searchLabel = getTranslator().translate("search");
		searchLink.setLinkTitle(searchLabel);
		searchLink.setI18nKey(searchLabel);
		
		exploreLink = uifactory.addFormLink("explore", "", "", formLayout, Link.NONTRANSLATED);
		
		exploreLink.setUrl(bcFactory.getSearchUrl());
		
		if (catalogModule.hasHeaderBgImage()) {
			String cacheId = "catalogHeaderBackgroundImage" + (catalogModule.hasHeaderBgImage() ? catalogModule.getHeaderBgImage().lastModified() : "");
			String mapperUri = registerCacheableMapper(null, cacheId, new VFSMediaMapper(catalogModule.getHeaderBgImage()));
			flc.contextPut("bgImageUrl", mapperUri);
		}
	}
	
	public void setSearchString(String searchString) {
		searchEl.setValue(searchString);
	}
	
	public void setTotalCatalogEntries(Integer totalCatalogEntries) {
		if (!Objects.equals(this.totalCatalogEntries, totalCatalogEntries)) {
			this.totalCatalogEntries = totalCatalogEntries;
			String explore = translate("search.explore.offers", String.valueOf(totalCatalogEntries));
			exploreLink.setI18nKey(explore);
		}
	}
	
	public void setExploreLinkVisibile(boolean visible) {
		if (visible != exploreLink.isVisible()) {
			exploreLink.setVisible(visible);
		}
	}
	
	public void setHeaderOnly(String header) {
		if (!Objects.equals(this.header, header)) {
			this.header = header;
			flc.contextPut("header", header);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == searchLink) {
			fireEvent(ureq, new CatalogSearchEvent(searchEl.getValue()));
		} else if (source == exploreLink) {
			fireEvent(ureq, new CatalogSearchEvent(null));
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, new CatalogSearchEvent(searchEl.getValue()));
	}

}
