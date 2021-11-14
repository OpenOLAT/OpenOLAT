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
	private MultipleSelectionElement addMultipleEntriesEl;
	private SingleSelection addEntryPosEl;
	private SingleSelection addCategoryPosEl;

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

		addMultipleEntriesEl = uifactory.addCheckboxesHorizontal("catalog.add.multiple.entries", "catalog.add.multiple.entries", formLayout, new String[]{"xx"}, new String[]{""});
		addMultipleEntriesEl.select("xx", repositoryModule.isCatalogMultiSelectEnabled());
		addMultipleEntriesEl.setEnabled(enabled);
		addMultipleEntriesEl.addActionListener(FormEvent.ONCLICK);

		String[] addEntryKeys = {AddEntryPosition.alphabetical.name(), AddEntryPosition.top.name(), AddEntryPosition.bottom.name()};
		String[] addEntryValues = {translate("catalog.add.position." + AddEntryPosition.alphabetical.name()), translate("catalog.add.position." + AddEntryPosition.top.name()), translate("catalog.add.position." + AddEntryPosition.bottom.name())};

		addCategoryPosEl = uifactory.addDropdownSingleselect("catalog.add.category.position", "catalog.add.category.position", formLayout, addEntryKeys, addEntryValues);
		if (repositoryModule.getCatalogAddCategoryPosition() == 2) {
			addCategoryPosEl.select(AddEntryPosition.bottom.name(), true);
		} else if (repositoryModule.getCatalogAddCategoryPosition() == 1) {
			addCategoryPosEl.select(AddEntryPosition.top.name(), true);
		} else {
			addCategoryPosEl.select(AddEntryPosition.alphabetical.name(), true);
		}

		addCategoryPosEl.setEnabled(enabled);
		addCategoryPosEl.addActionListener(FormEvent.ONCHANGE);

		addEntryPosEl = uifactory.addDropdownSingleselect("catalog.add.entry.position", "catalog.add.entry.position", formLayout, addEntryKeys, addEntryValues);
		if (repositoryModule.getCatalogAddEntryPosition() == 2) {
			addEntryPosEl.select(AddEntryPosition.bottom.name(), true);
		} else if (repositoryModule.getCatalogAddEntryPosition() == 1) {
			addEntryPosEl.select(AddEntryPosition.top.name(), true);
		} else {
			addEntryPosEl.select(AddEntryPosition.alphabetical.name(), true);
		}
		addEntryPosEl.setEnabled(enabled);
		addEntryPosEl.addActionListener(FormEvent.ONCHANGE);
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
				repositoryModule.setCatalogAddEntryPosition(2);
			} else if (addEntryPosEl.getSelectedKey().equals(AddEntryPosition.top.name())) {
				repositoryModule.setCatalogAddEntryPosition(1);
			} else {
				repositoryModule.setCatalogAddEntryPosition(0);
			}
		} else if (source == addCategoryPosEl) {
			if (addCategoryPosEl.getSelectedKey().equals(AddEntryPosition.bottom.name())) {
				repositoryModule.setCatalogAddCategoryPosition(2);
			} else if (addCategoryPosEl.getSelectedKey().equals(AddEntryPosition.top.name())) {
				repositoryModule.setCatalogAddCategoryPosition(1);
			} else {
				repositoryModule.setCatalogAddCategoryPosition(0);
			}
		} else if (source == addMultipleEntriesEl) {
			repositoryModule.setCatalogMultiSelectEnabled(addMultipleEntriesEl.isSelected(0));
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private enum AddEntryPosition {
		alphabetical,
		top,
		bottom;
	}
}
