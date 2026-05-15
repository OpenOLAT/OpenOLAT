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
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.impl.OWASPAntiSamyXSSFilter;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.position.TabConfiguration;
import org.olat.modules.selectus.model.position.TabsConfiguration.Tab;
import org.olat.modules.selectus.ui.RecruitingHelper;

/**
 * 
 * Initial date: 23 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class PositionEditInstructionsTextController extends FormBasicController {
	
	private FormLink resetTextButton;
	private final List<RichTextElement> textEls = new ArrayList<>();
	
	private final Position position;
	private final TabConfiguration configuration;
	private final List<Locale> positionLanguages;
	private final TabsConfigurationDelegate tabsConfigurationDelegate = new TabsConfigurationDelegate(Tab.instructions);

	@Autowired
	private RecruitingModule recruitingModule;
	
	public PositionEditInstructionsTextController(UserRequest ureq, WindowControl wControl,
			Position position, TabConfiguration configuration) {
		super(ureq, wControl, LAYOUT_DEFAULT_2_10);
		this.position = position;
		this.configuration = configuration;
		positionLanguages = recruitingModule.getPositionLocales(position);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		for(Locale locale:positionLanguages) {
			String lang = locale.getLanguage();
			String text = getInstructions(locale);	
			RichTextElement textEl = uifactory.addRichTextElementForStringData("attr_name_".concat(lang), "edit.text.name", text,
					24, 60, false, null, null, formLayout, ureq.getUserSession(), getWindowControl());
			textEl.getEditorConfiguration().setPathInStatusBar(false);
			textEl.setMandatory(true);
			textEl.setUserObject(locale);
			if(positionLanguages.size() > 1) {
				textEl.setLabel("edit.text.name_ml", new String[]{ lang });
				textEl.setElementCssClass("o_sel_attr_name_" + lang);
			} else {
				textEl.setElementCssClass("o_sel_attr_name");
			}
			textEls.add(textEl);
		}
	
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("ok", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		resetTextButton = uifactory.addFormLink("reset.text.standard", buttonsCont, Link.BUTTON);
	}
	
	private String getInstructions(Locale locale) {
		String text = configuration.getHelp(locale);
		if(StringHelper.containsNonWhitespace(text)) {
			return text;
		}
		
		return tabsConfigurationDelegate.getDefaultInstructions(getIdentity(), position, getWindowControl(), getTranslator(), locale);
	}
	
	public TabConfiguration getConfiguration() {
		for(TextElement textEl:textEls) {
			Locale loc = (Locale)textEl.getUserObject();
			configuration.setHelp(textEl.getValue(), loc);
		}
		return configuration;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		for(RichTextElement textEl:textEls) {
			allOk &= RecruitingHelper.validateRichTextElement(textEl, 32000, true, new OWASPAntiSamyXSSFilter());
		}
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(resetTextButton == source) {
			doReset();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doReset() {
		for(TextElement textEl:textEls) {
			Locale loc = (Locale)textEl.getUserObject();
			String standardText = tabsConfigurationDelegate
					.getDefaultInstructions(getIdentity(), position, getWindowControl(), getTranslator(), loc);
			textEl.setValue(standardText);
		}
	}
}
