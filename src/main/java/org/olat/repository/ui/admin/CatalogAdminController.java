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
package org.olat.repository.ui.admin;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17.03.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CatalogAdminController extends FormBasicController {

	private MultipleSelectionElement enableEl;
	private MultipleSelectionElement enableBrowsingEl;
	private MultipleSelectionElement siteEl;
	private SingleSelection addEntryPosEl;
	
	
	@Autowired
	private RepositoryModule repositoryModule;
	
	/**
	 * @param ureq
	 * @param wControl
	 */
	public CatalogAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("admin.catalog.settings");

		boolean enabled = repositoryModule.isCatalogEnabled();
		enableEl = uifactory.addCheckboxesHorizontal("catalog.enable", "catalog.enable", formLayout, new String[]{"xx"}, new String[]{""});
		enableEl.select("xx", enabled);
		enableEl.addActionListener(FormEvent.ONCLICK);
		
		enableBrowsingEl = uifactory.addCheckboxesHorizontal("catalog.browsing", "catalog.browsing", formLayout, new String[]{"xx"}, new String[]{""});
		enableBrowsingEl.select("xx", repositoryModule.isCatalogBrowsingEnabled());
		enableBrowsingEl.setEnabled(enabled);
		enableBrowsingEl.addActionListener(FormEvent.ONCLICK);

		siteEl = uifactory.addCheckboxesHorizontal("catalog.site", "catalog.site", formLayout, new String[]{"xx"}, new String[]{""});
		siteEl.select("xx", repositoryModule.isCatalogSiteEnabled());
		siteEl.setEnabled(enabled);
		siteEl.addActionListener(FormEvent.ONCLICK);
		
		String[] addEntryKeys = {AddEntryPosition.top.name(), AddEntryPosition.bottom.name()};
		String[] addEntryValues = {translate("catalog.addposition." + AddEntryPosition.top.name()), translate("catalog.addposition." + AddEntryPosition.bottom.name())};
		
		addEntryPosEl = uifactory.addDropdownSingleselect("catalog.addposition", "catalog.addposition", formLayout, addEntryKeys, addEntryValues);
		if (repositoryModule.isCatalogAddAtLast()) {
			addEntryPosEl.select(AddEntryPosition.bottom.name(), true);
		} else {
			addEntryPosEl.select(AddEntryPosition.top.name(), true);
		}
		addEntryPosEl.setEnabled(enabled);
		addEntryPosEl.addActionListener(FormEvent.ONCHANGE);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == enableEl) {
			boolean enabled = enableEl.isSelected(0);
			repositoryModule.setCatalogEnabled(enabled);
			siteEl.setEnabled(enabled);
			enableBrowsingEl.setEnabled(enabled);
			addEntryPosEl.setEnabled(enabled);
		} else if(source == siteEl) {
			repositoryModule.setCatalogSiteEnabled(siteEl.isSelected(0));
		} else if(source == enableBrowsingEl) {
			repositoryModule.setCatalogBrowsingEnabled(enableBrowsingEl.isSelected(0));
		} else if (source == addEntryPosEl) {
			if (addEntryPosEl.getSelectedKey().equals(AddEntryPosition.bottom.name())) {
				repositoryModule.setCatalogAddAtLast(true);
			} else {
				repositoryModule.setCatalogAddAtLast(false);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private enum AddEntryPosition {
		top,
		bottom;
	}
}