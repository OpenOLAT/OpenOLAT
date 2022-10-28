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
package org.olat.repository.bulk.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryService;
import org.olat.repository.bulk.SettingsBulkEditable;
import org.olat.repository.bulk.SettingsBulkEditables;
import org.olat.repository.bulk.model.SettingsContext;

/**
 * 
 * Initial date: 18 Oct 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AuthorRightsController extends StepFormBasicController {
	
	private static final String KEY_ADD = "add";
	private static final String KEY_REMOVE = "remove";
	private static final String[] CHANGE_KEYS = new String[] {"change"};
	
	private final List<MultipleSelectionElement> checkboxSwitch = new ArrayList<>(3);
	private final Map<MultipleSelectionElement, FormLayoutContainer> checkboxContainer = new HashMap<>(3);
	private SingleSelection referenceEl;
	private SingleSelection copyEl;
	private SingleSelection downloadEl;
	
	private final SettingsContext context;
	private final SettingsBulkEditables editables;
	private final String[] changeValues;
	
	
	public AuthorRightsController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_BAREBONE, null);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.context = (SettingsContext)runContext.get(SettingsContext.DEFAULT_KEY);
		this.editables = (SettingsBulkEditables)runContext.get(SettingsBulkEditables.DEFAULT_KEY);
		this.changeValues = new String[] {translate("settings.bulk.change")};
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer metadataCont = FormLayoutContainer.createDefaultFormLayout("metadataCont", getTranslator());
		metadataCont.setFormTitle(translate("settings.bulk.author.rights.title"));
		metadataCont.setFormInfo(RepositoryBulkUIFactory.getSettingsDescription(getTranslator(), context.getRepositoryEntries(), "settings.bulk.author.rights.desc"));
		metadataCont.setRootForm(mainForm);
		formLayout.add(metadataCont);
		
		SelectionValues authorRightsSV = new SelectionValues();
		authorRightsSV.add(SelectionValues.entry(KEY_ADD, translate("settings.bulk.author.rights.add")));
		authorRightsSV.add(SelectionValues.entry(KEY_REMOVE, translate("settings.bulk.author.rights.remove")));
		referenceEl = uifactory.addRadiosHorizontal("settings.bulk.author.rights.reference", metadataCont, authorRightsSV.keys(), authorRightsSV.values());
		referenceEl.select(context.isAuthorRightReference()? KEY_ADD: KEY_REMOVE, true);
		decorate(referenceEl, metadataCont, SettingsBulkEditable.authorRightReference);
		
		copyEl = uifactory.addRadiosHorizontal("settings.bulk.author.rights.copy", metadataCont, authorRightsSV.keys(), authorRightsSV.values());
		copyEl.select(context.isAuthorRightCopy()? KEY_ADD: KEY_REMOVE, true);
		decorate(copyEl, metadataCont, SettingsBulkEditable.authorRightCopy);
		
		downloadEl = uifactory.addRadiosHorizontal("settings.bulk.author.rights.download", metadataCont, authorRightsSV.keys(), authorRightsSV.values());
		downloadEl.select(context.isAuthorRightDownload()? KEY_ADD: KEY_REMOVE, true);
		decorate(downloadEl, metadataCont, SettingsBulkEditable.authorRightDownload);
	}
	
	private MultipleSelectionElement decorate(FormItem item, FormLayoutContainer formLayout, SettingsBulkEditable editable) {
		boolean selected = context.isSelected(editable);
		String itemName = item.getName();
		MultipleSelectionElement checkbox = uifactory.addCheckboxesHorizontal("cbx_" + itemName, itemName, formLayout, CHANGE_KEYS, changeValues);
		checkbox.select(checkbox.getKey(0), selected);
		checkbox.setEnabled(editables.isEditable(editable));
		checkbox.addActionListener(FormEvent.ONCLICK);
		checkbox.setUserObject(item);
		checkboxSwitch.add(checkbox);
		
		item.setLabel(null, null);
		item.setVisible(selected);
		item.setUserObject(checkbox);
		
		checkboxContainer.put(checkbox, formLayout);
		formLayout.moveBefore(checkbox, item);
		return checkbox;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (checkboxSwitch.contains(source)) {
			MultipleSelectionElement checkbox = (MultipleSelectionElement)source;
			FormItem item = (FormItem)checkbox.getUserObject();
			item.setVisible(checkbox.isAtLeastSelected(1));
			checkboxContainer.get(checkbox).setDirty(true);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		context.select(SettingsBulkEditable.authorRightReference, referenceEl.isVisible());
		if (referenceEl.isVisible()) {
			context.setAuthorRightReference(referenceEl.isKeySelected(KEY_ADD));
		}
		
		context.select(SettingsBulkEditable.authorRightCopy, copyEl.isVisible());
		if (copyEl.isVisible()) {
			context.setAuthorRightCopy(copyEl.isKeySelected(KEY_ADD));
		}
		
		context.select(SettingsBulkEditable.authorRightDownload, downloadEl.isVisible());
		if (downloadEl.isVisible()) {
			context.setAuthorRightDownload(downloadEl.isKeySelected(KEY_ADD));
		}
		
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

}
