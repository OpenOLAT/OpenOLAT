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

import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.ui.LicenseSelectionConfig;
import org.olat.core.commons.services.license.ui.LicenseUIFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
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
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.bulk.SettingsBulkEditable;
import org.olat.repository.bulk.SettingsBulkEditables;
import org.olat.repository.bulk.model.SettingsContext;
import org.olat.repository.manager.RepositoryEntryLicenseHandler;
import org.olat.repository.ui.RepositoyUIFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 Oct 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class MetadataController extends StepFormBasicController {
	
	private static final String[] CHANGE_KEYS = new String[] {"change"};
	
	private final List<MultipleSelectionElement> checkboxSwitch = new ArrayList<>(6);
	private final Map<MultipleSelectionElement, FormLayoutContainer> checkboxContainer = new HashMap<>(checkboxSwitch.size());
	private TextElement authorsEl;
	private SingleSelection educationalTypeEl;
	private StaticTextElement educationalTypeInfoEl;
	private TextElement languageEl;
	private TextElement expenditureOfWorkEl;
	private TextElement licensorEl;
	private SingleSelection licenseEl;
	private TextAreaElement licenseFreetextEl;
	
	private final SettingsContext context;
	private final SettingsBulkEditables editables;
	private final String[] changeValues;
	
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private RepositoryEntryLicenseHandler licenseHandler;
	
	public MetadataController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
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
		metadataCont.setFormTitle(translate("settings.bulk.metadata.title"));
		metadataCont.setFormInfo(RepositoryBulkUIFactory.getSettingsDescription(getTranslator(), context.getRepositoryEntries(), "settings.bulk.change.fields"));
		metadataCont.setRootForm(mainForm);
		formLayout.add(metadataCont);
		
		authorsEl = uifactory.addTextElement("settings.bulk.authors", 255, context.getAuthors(), metadataCont);
		decorate(authorsEl, metadataCont, SettingsBulkEditable.authors);
		
		if (editables.isEducationalTypeEnabled()) {
			SelectionValues educationalTypeKV = new SelectionValues();
			repositoryManager.getAllEducationalTypes()
					.forEach(type -> educationalTypeKV.add(entry(type.getKey().toString(), translate(RepositoyUIFactory.getI18nKey(type)))));
			educationalTypeKV.sort(SelectionValues.VALUE_ASC);
			educationalTypeEl = uifactory.addDropdownSingleselect("settings.bulk.educational.type", metadataCont,
					educationalTypeKV.keys(), educationalTypeKV.values());
			educationalTypeEl.enableNoneSelection();
			educationalTypeEl.setElementCssClass("o_form_explained");
			if (context.getEducationalTypeKey() != null && educationalTypeEl.containsKey(context.getEducationalTypeKey().toString())) {
				educationalTypeEl.select(context.getEducationalTypeKey().toString(), true);
			}
			decorate(educationalTypeEl, metadataCont, SettingsBulkEditable.educationalType);
			
			String educationalTypeInfo = "<i class='o_icon o_icon_warn'> </i> " + translate("settings.bulk.course.only.single");
			educationalTypeInfoEl = uifactory.addStaticTextElement("educational.type.info", null, educationalTypeInfo, metadataCont);
			educationalTypeInfoEl.setElementCssClass("o_form_explanation");
			educationalTypeInfoEl.setVisible(context.isSelected(SettingsBulkEditable.educationalType));
		}
		
		languageEl = uifactory.addTextElement("settings.bulk.mainLanguage", null, 16, context.getMainLanguage(), metadataCont);
		decorate(languageEl, metadataCont, SettingsBulkEditable.mainLanguage);
	
		expenditureOfWorkEl = uifactory.addTextElement("settings.bulk.expenditureOfWork", null, 100, context.getExpenditureOfWork(), metadataCont);
		expenditureOfWorkEl.setExampleKey("details.expenditureOfWork.example", null);
		decorate(expenditureOfWorkEl, metadataCont, SettingsBulkEditable.expenditureOfWork);
		
		if (editables.isLicensesEnabled()) {
			LicenseType licenseType = licenseService.loadLicenseTypeByKey(context.getLicenseTypeKey());
			
			LicenseSelectionConfig licenseSelectionConfig = LicenseUIFactory
					.createLicenseSelectionConfig(licenseHandler, licenseType);
			licenseEl = uifactory.addDropdownSingleselect("settings.bulk.license", metadataCont,
					licenseSelectionConfig.getLicenseTypeKeys(),
					licenseSelectionConfig.getLicenseTypeValues(getLocale()));
			if (licenseSelectionConfig.getSelectionLicenseTypeKey() != null) {
				licenseEl.select(licenseSelectionConfig.getSelectionLicenseTypeKey(), true);
			}
			licenseEl.addActionListener(FormEvent.ONCHANGE);
			decorate(licenseEl, metadataCont, SettingsBulkEditable.license);
			
			String freetext = licenseService.isFreetext(licenseType) ? context.getFreetext() : "";
			licenseFreetextEl = uifactory.addTextAreaElement("settings.bulk.freetext", 4, 72, freetext, metadataCont);
			
			licensorEl = uifactory.addTextElement("settings.bulk.licensor", 1000, context.getLicensor(), metadataCont);
			
			updateLicenseUI();
		}
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
	
	private void updateLicenseUI() {
		boolean freetextSelected = false;
		if (licenseEl != null && licenseEl.isVisible() && licenseEl.isOneSelected()) {
			String selectedKey = licenseEl.getSelectedKey();
			LicenseType licenseType = licenseService.loadLicenseTypeByKey(selectedKey);
			freetextSelected = licenseService.isFreetext(licenseType);
		}
		if (licenseFreetextEl != null) {
			licenseFreetextEl.setVisible(freetextSelected);
		}
		if (licensorEl != null) {
			licensorEl.setVisible(licenseEl.isVisible());
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == licenseEl) {
			updateLicenseUI();
		} else if (checkboxSwitch.contains(source)) {
			MultipleSelectionElement checkbox = (MultipleSelectionElement)source;
			FormItem item = (FormItem)checkbox.getUserObject();
			item.setVisible(checkbox.isAtLeastSelected(1));
			checkboxContainer.get(checkbox).setDirty(true);
			
			if (item == educationalTypeEl) {
				educationalTypeInfoEl.setVisible(checkbox.isAtLeastSelected(1));
			} else if (item == licenseEl) {
				updateLicenseUI();
			} 
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		context.select(SettingsBulkEditable.authors, authorsEl.isVisible());
		if (authorsEl.isVisible()) {
			context.setAuthors(authorsEl.getValue().trim());
		}
		
		context.select(SettingsBulkEditable.educationalType, educationalTypeEl != null && educationalTypeEl.isVisible());
		if (educationalTypeEl != null && educationalTypeEl.isVisible()) {
			Long educationalTypeKey = educationalTypeEl.isOneSelected()? Long.valueOf(educationalTypeEl.getSelectedKey()): null;
			context.setEducationalTypeKey(educationalTypeKey);
		}
		
		context.select(SettingsBulkEditable.mainLanguage, languageEl.isVisible());
		if (languageEl.isVisible()) {
			context.setMainLanguage(languageEl.getValue().trim());
		}
		
		context.select(SettingsBulkEditable.expenditureOfWork, expenditureOfWorkEl.isVisible());
		if (expenditureOfWorkEl.isVisible()) {
			context.setExpenditureOfWork(expenditureOfWorkEl.getValue().trim());
		}
		
		context.select(SettingsBulkEditable.license, licenseEl != null && licenseEl.isVisible());
		if (licenseEl != null && licenseEl.isVisible()) {
			context.setLicenseTypeKey(licenseEl.getSelectedKey());
			if (licenseFreetextEl.isVisible()) {
				context.setFreetext(licenseFreetextEl.getValue());
			} else {
				context.setFreetext(null);
			}
		}
		
		if (licensorEl != null && licensorEl.isVisible()) {
			context.setLicensor(licensorEl.getValue());
		} else {
			context.setLicensor(null);
		}
		
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

}
