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
package org.olat.modules.portfolio.ui;

import java.util.HashSet;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.modules.ceditor.ContentRoles;
import org.olat.modules.ceditor.Page;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.ui.event.ToggleEditPageEvent;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Initial date: 22 mai 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageMetadataCompactController extends FormBasicController {
	
	private FormToggle editLink;

	private final Page page;
	private final PageSettings pageSettings;
	private final BinderSecurityCallback secCallback;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private PortfolioService portfolioService;
	
	public PageMetadataCompactController(UserRequest ureq, WindowControl wControl, BinderSecurityCallback secCallback,
			Page page, PageSettings pageSettings, boolean openInEditMode) {
		super(ureq, wControl, "page_meta_reduced");
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		this.page = page;
		this.secCallback = secCallback;
		this.pageSettings = pageSettings;
		initForm(ureq);
		updateEditLink(openInEditMode);
		if(openInEditMode) {
			editLink.toggleOn();
		} else {
			editLink.toggleOff();
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			Set<Identity> owners = new HashSet<>();
			if(page.getSection() != null && page.getSection().getBinder() != null) {
				owners.addAll(portfolioService.getMembers(page.getSection().getBinder(), ContentRoles.owner.name()));
			}
			owners.addAll(portfolioService.getMembers(page, ContentRoles.owner.name()));
		
			StringBuilder ownerSb = new StringBuilder();
			for(Identity owner:owners) {
				if(ownerSb.length() > 0) ownerSb.append(", ");
				ownerSb.append(userManager.getUserDisplayName(owner));
			}
			layoutCont.contextPut("owners", ownerSb.toString());
			layoutCont.contextPut("pageTitle", page.getTitle());
			layoutCont.contextPut("lastModified", page.getLastModified());
			layoutCont.contextPut("withTitle",  Boolean.valueOf(pageSettings.isWithTitle()));
		}
		
		editLink = uifactory.addToggleButton("edit.page", "edit.page", translate("off"), flc, null, null);
		editLink.setElementCssClass("o_sel_page_edit");
		editLink.setLabel(translate("edit.page.toggle"), null);
		editLink.setVisible(page.isEditable() && secCallback.canEditPage(page));
	}
	
	public void updateEditLink(boolean edit) {
		editLink.setI18nKey(edit ? translate("off") : translate("on"));
		flc.contextPut("edit", Boolean.valueOf(edit));
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(editLink == source) {
			updateEditLink(editLink.isOn());
			fireEvent(ureq, new ToggleEditPageEvent());
		}
		super.formInnerEvent(ureq, source, event);
	}
}
