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
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.vfs.VFSMediaMapper;
import org.olat.modules.catalog.CatalogSecurityCallback;
import org.olat.modules.catalog.CatalogV2Module;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 May 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CatalogSearchHeaderController extends FormBasicController {
	
	public static final Event OPEN_ADMIN_EVENT = new Event("open.admin");
	public static final Event TAXONOMY_ADMIN_EVENT = new Event("taxonomy.admin");

	private FormLink openAdminLink;
	private FormLink taxonomyEditLink;
	private TextElement searchEl;
	private FormLink searchLink;
	
	private final CatalogSecurityCallback secCallback;
	private String header;

	@Autowired
	private CatalogV2Module catalogModule;

	public CatalogSearchHeaderController(UserRequest ureq, WindowControl wControl, CatalogSecurityCallback secCallback) {
		super(ureq, wControl, "header_search");
		this.secCallback = secCallback;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		DropdownItem dropdown = uifactory.addDropdownMenu("tools", null, null, flc, getTranslator());
		dropdown.setElementCssClass("o_catalog_admin_tools");
		dropdown.setCarretIconCSS("o_icon o_icon_commands");
		dropdown.setOrientation(DropdownOrientation.right);
		if (secCallback.canEditCatalogAdministration()) {
			openAdminLink = uifactory.addFormLink("open.admin", "open.admin", "open.admin", null, flc, Link.LINK);
			dropdown.addElement(openAdminLink);
		}
		if (secCallback.canEditTaxonomy()) {
			taxonomyEditLink = uifactory.addFormLink("taxonomy.edit", "taxonomy.edit", "taxonomy.edit", null, flc, Link.LINK);
			dropdown.addElement(taxonomyEditLink);
		}
		dropdown.setVisible(dropdown.size() > 0);
		
		FormLayoutContainer searchCont = FormLayoutContainer.createInputGroupLayout("searchWrapper", getTranslator(), null, null);
		searchCont.contextPut("inputGroupSizeCss", "input-group-lg");
		formLayout.add("searchWrapper", searchCont);
		searchCont.setRootForm(mainForm);
		
		searchEl = uifactory.addTextElement("search", 100, null, searchCont);
		searchEl.setPlaceholderText(translate("search.placeholder"));
		searchEl.setDomReplacementWrapperRequired(false);
		searchEl.setFocus(true);
		
		searchLink = uifactory.addFormLink("rightAddOn", "", "", searchCont, Link.NONTRANSLATED);
		searchLink.setIconLeftCSS("o_icon o_icon-fw o_icon_search o_icon-lg");
		String searchLabel = getTranslator().translate("search");
		searchLink.setLinkTitle(searchLabel);
		
		if (catalogModule.hasHeaderBgImage()) {
			String mapperUri = registerMapper(ureq, new VFSMediaMapper(catalogModule.getHeaderBgImage()));
			flc.contextPut("bgImageUrl", mapperUri);
		}
	}
	
	public void setSearchString(String searchString) {
		searchEl.setValue(searchString);
	}
	
	public void setHeaderOnly(String header) {
		if (!Objects.equals(this.header, header)) {
			this.header = header;
			flc.contextPut("header", header);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == openAdminLink) {
			fireEvent(ureq, OPEN_ADMIN_EVENT);
		} else if (source == taxonomyEditLink) {
			fireEvent(ureq, TAXONOMY_ADMIN_EVENT);
		} else if (source == searchLink) {
			fireEvent(ureq, new CatalogSearchEvent(searchEl.getValue()));
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, new CatalogSearchEvent(searchEl.getValue()));
	}

}
