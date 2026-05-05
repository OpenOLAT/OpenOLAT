/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.position;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.core.util.filter.impl.OWASPAntiSamyXSSFilter;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionApplicationAttributeTabEnum;
import org.olat.modules.selectus.model.PositionAttributeDefinition;
import org.olat.modules.selectus.model.attributes.TextConfiguration;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.app_wizard.ApplicationAttributesDelegate;

/**
 * Edit a single line 
 * 
 * 
 * Initial date: 9 sept. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionEditAdditionalSingleLineTextAttributeController extends FormBasicController implements PositionEditAdditionalAttributeController {
	
	private static final String[] mandatoryKeys = new String[] { "mandatory" };
	
	private SingleSelection multiEl;
	private TextElement maxLengthEl;
	private MultipleSelectionElement mandatoryEl;
	private List<TextElement> labelsEl = new ArrayList<>(3);
	private List<TextElement> placeholdersEl = new ArrayList<>(3);

	private final List<Locale> positionLanguages = new ArrayList<>();
	private final Map<String,Locale> positionLanguageToLocale = new HashMap<>();
	
	private final Position position;
	private TextConfiguration configuration;
	private PositionAttributeDefinition attributeDefinition;

	private final PositionApplicationAttributeTabEnum tab;
	private final ApplicationAttributesDelegate attributesDelegate;
	
	@Autowired
	private DB dbInstance;

	public PositionEditAdditionalSingleLineTextAttributeController(UserRequest ureq, WindowControl wControl,
			Position position, PositionAttributeDefinition attributeDefinition, PositionApplicationAttributeTabEnum tab) {
		super(ureq, wControl, Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.tab = tab;
		this.position = position;
		this.attributeDefinition = attributeDefinition;
		configuration = attributeDefinition.getConfiguration(TextConfiguration.class);
		if(configuration == null) {
			configuration = TextConfiguration.defaultConfiguration(); 
		}
		PositionEditHelper.calculatePositionLanguages(position, positionLanguages, positionLanguageToLocale);
		attributesDelegate = new ApplicationAttributesDelegate(attributeDefinition.getTabEnum());
		
		initForm(ureq);
	}

	@Override
	public PositionAttributeDefinition getAttributeDefinition() {
		return attributeDefinition;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		SelectionValues multiKeys = new SelectionValues();
		multiKeys.add(SelectionValues.entry("single", translate("single.line")));
		multiKeys.add(SelectionValues.entry("multi", translate("multi.line")));
		multiEl = uifactory.addRadiosHorizontal("edit.attr.multi.line", "edit.attr.multi.line", formLayout,
				multiKeys.keys(), multiKeys.values());
		multiEl.addActionListener(FormEvent.ONCHANGE);
		multiEl.setMandatory(true);
		if(configuration.isMultiLine()) {
			multiEl.select("multi", true);
		} else {
			multiEl.select("single", true);
		}
		
		for(Locale locale:positionLanguages) {
			String lang = locale.getLanguage();
			String label = attributeDefinition == null ? "" : attributeDefinition.getLabel(locale);
			TextElement labelEl = uifactory.addTextElement("attr_name_".concat(lang), "edit.attr.name", 256, label, formLayout);
			labelEl.setMandatory(true);
			labelEl.setUserObject(locale);
			if(positionLanguages.size() > 1) {
				labelEl.setLabel("edit.attr.name_ml", new String[]{ lang });
				labelEl.setElementCssClass("o_sel_attr_name_" + lang);
			} else {
				labelEl.setElementCssClass("o_sel_attr_name");
			}
			labelsEl.add(labelEl);
		}
		
		for(Locale locale:positionLanguages) {
			String lang = locale.getLanguage();
			String placeholder = attributeDefinition == null ? "" : attributeDefinition.getPlaceholder(locale);
			TextElement placeholderEl = uifactory.addTextElement("placeholder_name_".concat(lang), "edit.attr.placeholder", 256, placeholder, formLayout);
			placeholderEl.setUserObject(locale);
			if(positionLanguages.size() > 1) {
				placeholderEl.setLabel("edit.attr.placeholder_ml", new String[]{ lang });
				placeholderEl.setElementCssClass("o_sel_attr_placeholder_" + lang);
			} else {
				placeholderEl.setElementCssClass("o_sel_attr_placeholder");
			}
			placeholdersEl.add(placeholderEl);
		}

		mandatoryEl = uifactory.addCheckboxesHorizontal("edit.attr.mandatory", formLayout, mandatoryKeys, mandatoryKeys);
		mandatoryEl.setVisible(tab != PositionApplicationAttributeTabEnum.global);
		if(attributeDefinition != null && attributeDefinition.isMandatory()) {
			mandatoryEl.select(mandatoryKeys[0], true);
		}
		
		int maxLength = configuration.getMaxLength();
		if(maxLength <= 0) {
			if(configuration.isMultiLine()) {
				maxLength = 800;
			} else {
				maxLength = 255;
			}
		}
		maxLengthEl = uifactory.addTextElement("edit.attr.max.length", "edit.attr.max.length", "edit.attr.max.length", 8,
				Integer.toString(maxLength), formLayout);
		maxLengthEl.setHelpTextKey("edit.attr.max.length.hint", null);
		maxLengthEl.setMandatory(true);
		maxLengthEl.setEnabled(isMultiLine());

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("ok", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	private boolean isMultiLine() {
		return multiEl.isOneSelected() && "multi".equals(multiEl.getSelectedKey());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		for(TextElement labelEl:labelsEl) {
			allOk &= RecruitingHelper.validateTextElement(labelEl, 255, true, new OWASPAntiSamyXSSFilter());
			allOk &= attributesDelegate.validateFormLabel(labelEl, attributeDefinition, position);
		}
		
		int maxLength = dbInstance.isOracle() ? ApplicationAttributesDelegate.TEXT_MAX_LENGTH_ORACLE : ApplicationAttributesDelegate.TEXT_MAX_LENGTH;
		allOk &= RecruitingHelper.validateIntegerElement(maxLengthEl, 1, maxLength, true);
		allOk &= RecruitingHelper.validateSingleSelection(multiEl);
		return allOk;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(multiEl == source) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		for(TextElement labelEl:labelsEl) {
			Locale locale = (Locale)labelEl.getUserObject();
			attributeDefinition.setLabel(labelEl.getValue(), locale);
		}
		for(TextElement placeholderEl:placeholdersEl) {
			Locale locale = (Locale)placeholderEl.getUserObject();
			attributeDefinition.setPlaceholder(placeholderEl.getValue(), locale);
		}
		attributeDefinition.setMandatory(mandatoryEl.isAtLeastSelected(1));
		
		configuration.setMaxLength(Integer.parseInt(maxLengthEl.getValue()));
		configuration.setMultiLine(isMultiLine());
		
		attributeDefinition.setConfiguration(configuration);
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void updateUI() {
		boolean multiLine = isMultiLine();
		maxLengthEl.setEnabled(multiLine);
		if(multiLine) {
			maxLengthEl.setValue("800");
		} else {
			maxLengthEl.setValue("255");
		}
		
	}
}
