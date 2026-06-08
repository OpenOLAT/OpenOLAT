/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.position;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.filter.impl.OWASPAntiSamyXSSFilter;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.DocumentType;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.position.model.PositionDocumentRow;

/**
 * 
 * Initial date: 4 févr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionEditDocumentConfigurationController extends FormBasicController {
	
	private static final String[] onKeys = new String[] { "on" };
	private static final String[] usageKeys = new String[] { "wizard", "staff" };
	
	private SingleSelection usageEl;
	private MultipleSelectionElement enableEl;
	private MultipleSelectionElement mandatoryEl;
	private MultipleSelectionElement combinedEl;
	private MultipleSelectionElement formatEl;
	private TextElement sizeEl;
	private final List<TextElement> namesEls = new ArrayList<>();
	private final List<TextElement> explainEls = new ArrayList<>();
	
	private final DocumentType[] docTypes;
	private final PositionDocumentRow row;
	
	private final List<Locale> positionLanguages;
	
	@Autowired
	private RecruitingModule recruitingModule;
	
	public PositionEditDocumentConfigurationController(UserRequest ureq, WindowControl wControl, Position position, PositionDocumentRow row) {
		super(ureq, wControl, Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.row = row;
		docTypes = recruitingModule.getDocumentTypes();
		positionLanguages = recruitingModule.getPositionLocales(position);
		initForm(ureq);
		updateType();
	}
	
	public boolean isDocumentEnabled() {
		return enableEl.isAtLeastSelected(1);
	}
	
	public PositionDocumentRow getDocumentRow() {
		return row;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String[] enableValues = new String[] { translate("document.enabled.on") };
		enableEl = uifactory.addCheckboxesHorizontal("enable", "document.enabled", formLayout, onKeys, enableValues);
		enableEl.addActionListener(FormEvent.ONCHANGE);
		if(row.getEnableEl().isAtLeastSelected(1)) {
			enableEl.select(onKeys[0], true);
		} 

		// Name ml
		String elI18nKey = "edit.document.name";
		String elMlI18nKey = "edit.document.name_ml";
		for(Locale locale:positionLanguages) {
			String lang = locale.getLanguage();
			String name;
			if(locale.equals(row.getDocumentNameEl().getUserObject()) && row.getDocumentNameEl().isEnabled()) {
				name = row.getDocumentNameEl().getValue();
			} else {
				name = row.getDocumentName(locale);
			}
			if(!StringHelper.containsNonWhitespace(name)) {
				Translator translator = getLocale().equals(locale) ? getTranslator() : Util.createPackageTranslator(PositionController.class, locale);
				name = translator.translate(row.getDocument().i18nKey());
			}
			TextElement nameEl = uifactory.addTextElement("doc_name_".concat(lang), elI18nKey, 256, name, formLayout);
			nameEl.setMandatory(true);
			nameEl.setUserObject(locale);
			if(positionLanguages.size() > 1) {
				nameEl.setLabel(elMlI18nKey, new String[]{ lang });
				nameEl.setElementCssClass("o_sel_doc_name_" + lang);
			} else {
				nameEl.setElementCssClass("o_sel_doc_name");
			}
			namesEls.add(nameEl);
		}
		
		// explain ml
		String explainI18nKey = "edit.document.explain";
		String explainMlI18nKey = "edit.document.explain_ml";
		for(Locale locale:positionLanguages) {
			String lang = locale.getLanguage();
			String explain = row.getDocumentExplain(locale);
			if(!StringHelper.containsNonWhitespace(explain)) {
				Translator translator = getLocale().equals(locale) ? getTranslator() : Util.createPackageTranslator(PositionController.class, locale);
				explain = translator.translate(row.getDocument().i18nExplainKey());
			}
			TextElement explainEl = uifactory.addTextAreaElement("doc_explain_".concat(lang), explainI18nKey, 2000, 3, 60, true, false, false, explain, formLayout);
			explainEl.setUserObject(locale);
			if(positionLanguages.size() > 1) {
				explainEl.setLabel(explainMlI18nKey, new String[]{ lang });
				explainEl.setElementCssClass("o_sel_doc_explain_" + lang);
			} else {
				explainEl.setElementCssClass("o_sel_doc_explain");
			}
			explainEls.add(explainEl);
		}

		// usage
		String[] usageValues = new String[] { translate("document.wizard"), translate("document.staff") };
		usageEl = uifactory.addDropdownSingleselect("usage", "document.usage", formLayout, usageKeys, usageValues, null);
		usageEl.addActionListener(FormEvent.ONCHANGE);
		if(row.getUsageEl().isOneSelected()) {
			usageEl.select(row.getUsageEl().getSelectedKey(), true);
		} else {
			usageEl.select(usageKeys[0], true);
		}

		String[] mandatoryValues = new String[] { translate("document.mandatory.on") };
		mandatoryEl = uifactory.addCheckboxesHorizontal("mandatory", "document.mandatory", formLayout, onKeys, mandatoryValues);
		if(row.getMandatoryEl().isAtLeastSelected(1)) {
			mandatoryEl.select(onKeys[0], true);
		}
		
		// format
		String[] typeKeys = new String[docTypes.length];
		String[] typeValues = new String[docTypes.length];
		for(int i=docTypes.length; i-->0; ) {
			String typeName = docTypes[i].name();
			typeKeys[i] = typeName;
			typeValues[i] = translate("document.type.".concat(typeName));	
		}
		formatEl = uifactory.addCheckboxesHorizontal("doctypes", "document.doctypes", formLayout, typeKeys, typeValues);
		formatEl.addActionListener(FormEvent.ONCHANGE);
		formatEl.setVisible(typeKeys.length > 1);
		if(row.isTypeXlsx()) {
			formatEl.select(DocumentType.xlsx.name(), true);
		}
		if(row.isTypeDocx()) {
			formatEl.select(DocumentType.docx.name(), true);
		}
		if(row.isTypeJpg()) {
			formatEl.select(DocumentType.jpg.name(), true);
		}
		if(row.isTypePdf()) {
			formatEl.select(DocumentType.pdf.name(), true);
		}
		if(formatEl.getSelectedKeys().isEmpty() && docTypes.length > 0) {
			formatEl.select(docTypes[0].name(), true);
		}
		
		String[] combinedValues = new String[] { translate("document.combined.on") };
		combinedEl = uifactory.addCheckboxesHorizontal("combined", "document.combined", formLayout, onKeys, combinedValues);
		combinedEl.setDomReplacementWrapperRequired(false);
		if(row.getCombinedEl().isAtLeastSelected(1)) {
			combinedEl.select(onKeys[0], true);
		}
		
		String size = row.getDocumentSizeEl().getValue();
		sizeEl = uifactory.addTextElement("docsize", "document.size", 4, size, formLayout);
		sizeEl.setDisplaySize(4);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("ok", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		for(TextElement nameEl:namesEls) {
			allOk &= RecruitingHelper.validateTextElement(nameEl, 255, true, new OWASPAntiSamyXSSFilter());
		}
		
		for(TextElement explainEl:explainEls) {
			allOk &= RecruitingHelper.validateTextElement(explainEl, 2000, false, new OWASPAntiSamyXSSFilter());
		}
		
		formatEl.clearError();
		if(formatEl.isVisible() && formatEl.getSelectedKeys().isEmpty()) {
			formatEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {	
		if(formatEl == source) {
			updateType();
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void updateType() {
		Collection<String> selectedFormats = formatEl.getSelectedKeys();
		if(selectedFormats.size() == 1 &&  selectedFormats.contains(DocumentType.pdf.name())) {
			combinedEl.setEnabled(true);
		} else {
			if(combinedEl.isAtLeastSelected(1)) {
				combinedEl.uncheckAll();
			}
			combinedEl.setEnabled(false);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		
		if(enableEl.isAtLeastSelected(1)) {
			row.getEnableEl().select("on", true);
		} else {
			row.getEnableEl().uncheckAll();
		}
		
		for(TextElement nameEl:namesEls) {
			Locale locale = (Locale)nameEl.getUserObject();
			row.setDocumentNames(nameEl.getValue(), locale);
			if(locale.getLanguage().equals(getLocale().getLanguage()) || positionLanguages.size() == 1) {
				row.getDocumentNameEl().setValue(nameEl.getValue());
			}
		}
		
		for(TextElement explainEl:explainEls) {
			Locale locale = (Locale)explainEl.getUserObject();
			row.setDocumentExplain(explainEl.getValue(), locale);
		}
		
		row.getUsageEl().select(usageEl.getSelectedKey(), true);
		
		if(mandatoryEl.isAtLeastSelected(1)) {
			row.getMandatoryEl().select("on", true);
		} else {
			row.getMandatoryEl().uncheckAll();
		}
		
		Collection<String> selectedDocTypes ;
		if(formatEl.isVisible()) {
			selectedDocTypes = formatEl.getSelectedKeys();
		} else if(docTypes.length == 1) {
			selectedDocTypes = new ArrayList<>();
			selectedDocTypes.add(docTypes[0].name());
		} else {
			selectedDocTypes = Collections.emptyList();
		}
		row.setTypePdf(selectedDocTypes.contains(DocumentType.pdf.name()));
		row.setTypeXlsx(selectedDocTypes.contains(DocumentType.xlsx.name()));
		row.setTypeDocx(selectedDocTypes.contains(DocumentType.docx.name()));
		row.setTypeJpg(selectedDocTypes.contains(DocumentType.jpg.name()));
		
		if(combinedEl.isAtLeastSelected(1)) {
			row.getCombinedEl().select("on", true);
		} else {
			row.getCombinedEl().uncheckAll();
		}
		
		String size = sizeEl.getValue();
		row.getDocumentSizeEl().setValue(size);
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
