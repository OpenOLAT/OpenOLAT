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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.Util;
import org.olat.modules.ceditor.ContentAuditLog;
import org.olat.modules.ceditor.Page;
import org.olat.modules.ceditor.PageService;
import org.olat.modules.cemedia.ui.MediaCenterConfig;
import org.olat.modules.cemedia.ui.MediaCenterController;
import org.olat.modules.portfolio.BinderSecurityCallback;
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
	private FormLink mediaCenterLink;

	private final Page page;
	private final PageSettings pageSettings;
	private final BinderSecurityCallback secCallback;

	private CloseableModalController cmc;
	private MediaCenterController mediaListCtrl;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private PageService pageService;
	
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
			layoutCont.contextPut("pageTitle", page.getTitle());
			layoutCont.contextPut("withTitle",  Boolean.valueOf(pageSettings.isWithTitle()));
		}

		mediaCenterLink = uifactory.addFormLink("media.center", "", null, formLayout, Link.LINK | Link.NONTRANSLATED);
		mediaCenterLink.setIconLeftCSS("o_icon o_icon-fw o_icon-lg o_icon_actions");
		String title =  pageSettings.getBaseRepositoryEntry() == null
				? translate("media.center") : translate("media.center.entry");
		mediaCenterLink.setTitle(title);
		mediaCenterLink.setVisible(pageSettings.isWithMediaCenterPreview());
		
		editLink = uifactory.addToggleButton("edit.page", "edit.page.toggle", translate("on"), translate("off"), flc);
		editLink.setElementCssClass("o_sel_page_edit");
		editLink.setVisible(page.isEditable() && secCallback.canEditPage(page));
	}
	
	public void updateEditLink(boolean edit) {
		flc.contextPut("edit", Boolean.valueOf(edit));
		updateLastChange();
	}
	
	public void updateLastChange() {
		ContentAuditLog lastChange = pageService.lastChange(page);
		if(lastChange != null) {
			flc.contextPut("lastModified", lastChange.getLastModified());
			if(lastChange.getDoer() != null) {
				String modifier = userManager.getUserDisplayName(lastChange.getDoer());
				flc.contextPut("modifier", modifier);
			}
		} else {
			flc.contextPut("lastModified", page.getLastModified());
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(mediaListCtrl == source) {
			cleanUp();
			cmc.deactivate();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(mediaListCtrl);
		removeAsListenerAndDispose(cmc);
		mediaListCtrl = null;
		cmc = null;
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
		} else if(mediaCenterLink == source) {
			doOpenMediaCenter(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doOpenMediaCenter(UserRequest ureq) {
		mediaListCtrl = new MediaCenterController(ureq, getWindowControl(), null,
				MediaCenterConfig.valueOfChooser(pageSettings.getBaseRepositoryEntry(), false, false));
		
		String description =  pageSettings.getBaseRepositoryEntry() == null
				? translate("media.center.descr") : translate("media.center.descr.entry");
		mediaListCtrl.setFormTranslatedTitle(description);

		String title =  pageSettings.getBaseRepositoryEntry() == null
				? translate("media.center") : translate("media.center.entry");
		cmc = new CloseableModalController(getWindowControl(), null, mediaListCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
}
