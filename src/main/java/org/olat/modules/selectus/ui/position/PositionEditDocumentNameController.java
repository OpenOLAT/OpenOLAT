/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.position;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.filter.impl.OWASPAntiSamyXSSFilter;
import org.springframework.beans.factory.annotation.Autowired;

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
public class PositionEditDocumentNameController extends FormBasicController {
	
	private List<TextElement> labelsEl = new ArrayList<>(3);

	private final List<Locale> positionLanguages;
	
	private final PositionDocumentRow row;
	
	@Autowired
	private RecruitingModule recruitingModule;
	
	public PositionEditDocumentNameController(UserRequest ureq, WindowControl wControl, Position position, PositionDocumentRow row) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		setTranslator(Util.createPackageTranslator(PositionController.class, ureq.getLocale(), getTranslator()));
		this.row = row;
		positionLanguages = recruitingModule.getPositionLocales(position);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
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
			TextElement labelEl = uifactory.addTextElement("attr_name_".concat(lang), elI18nKey, 256, name, formLayout);
			labelEl.setMandatory(true);
			labelEl.setUserObject(locale);
			if(positionLanguages.size() > 1) {
				labelEl.setLabel(elMlI18nKey, new String[]{ lang });
				labelEl.setElementCssClass("o_sel_attr_name_" + lang);
			} else {
				labelEl.setElementCssClass("o_sel_attr_name");
			}
			labelsEl.add(labelEl);
		}

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("ok", buttonsCont);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		for(TextElement labelEl:labelsEl) {
			allOk &= RecruitingHelper.validateTextElement(labelEl, 255, true, new OWASPAntiSamyXSSFilter());
		}
		return allOk;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		for(TextElement labelEl:labelsEl) {
			Locale locale = (Locale)labelEl.getUserObject();
			row.setDocumentNames(labelEl.getValue(), locale);
			if(locale.getLanguage().equals(getLocale().getLanguage()) || positionLanguages.size() == 1) {
				row.getDocumentNameEl().setValue(labelEl.getValue());
			}
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
