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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
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
	
	@Autowired
	private CatalogV2Module catalogModule;

	public CatalogSearchHeaderController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "header_search");
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		searchEl = uifactory.addTextElement("search", 100, null, formLayout);
		searchEl.setPlaceholderText(translate("search.placeholder"));
		searchEl.setDomReplacementWrapperRequired(false);
		
		searchLink = uifactory.addFormLink("search.icon", null, "", null, formLayout, Link.LINK + Link.NONTRANSLATED);
		searchLink.setIconLeftCSS("o_icon o_icon-fw o_icon-lg o_icon_search");
		
		if (catalogModule.hasHeaderBgImage()) {
			String mapperUri = registerMapper(ureq, new VFSMediaMapper(catalogModule.getHeaderBgImage()));
			flc.contextPut("bgImageUrl", mapperUri);
		}
	}
	
	public void setSearchString(String searchString) {
		searchEl.setValue(searchString);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == searchLink) {
			fireEvent(ureq, new CatalogSearchEvent(searchEl.getValue()));
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

}
