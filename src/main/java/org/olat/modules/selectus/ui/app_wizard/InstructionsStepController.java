/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.app_wizard;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.chiefcontrollers.LanguageChangedEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.position.TabConfiguration;
import org.olat.modules.selectus.model.position.TabsConfiguration.Tab;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  29 aug. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class InstructionsStepController extends StepFormBasicController {

	private static final String CMD_LANGUAGE = "language";
	
	private final Position position;
	private final boolean languageChooser;
	private final InstructionsController documentsController;
	
	@Autowired
	private RecruitingModule recruitingModule;
	
	public InstructionsStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext,
			boolean languageChooser, Form rootForm) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "instructions_lang");
		this.languageChooser = languageChooser;
		Application app = (Application)runContext.get(WizardConstants.APPLICATION);
		position = app.getPosition();
		TabConfiguration configuration = position.getTabConfiguration(Tab.instructions);
		documentsController = new InstructionsController(ureq, wControl, rootForm, position, configuration, true);
		listenTo(documentsController);

		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		List<Locale> locales = recruitingModule.getPositionLocales(position);
		if(languageChooser && locales.size() > 1) {
			List<FormLink> languageLinks = new ArrayList<>();
			for(Locale locale:locales) {
				String id = "apply." + locale.getLanguage();
				FormLink localeLink = uifactory.addFormLink(id, CMD_LANGUAGE, id, null, formLayout, Link.BUTTON);
				if(locale.equals(getLocale())) {
					localeLink.setCustomEnabledLinkCSS("active btn btn-default");
				} else {
					localeLink.setCustomEnabledLinkCSS("btn btn-default");
				}
				localeLink.setUserObject(locale);
				languageLinks.add(localeLink);
			}
			formLayout.contextPut("languages", languageLinks);
		}
		
		formLayout.add("instructions", documentsController.getInitialFormItem());
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if(CMD_LANGUAGE.equals(link.getCmd())) {
				Locale newLocale = (Locale)link.getUserObject();
				fireEvent(ureq, new LanguageChangedEvent(newLocale, ureq));
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	public void forceNextStep(UserRequest ureq) {
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
}
