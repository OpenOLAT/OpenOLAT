/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.app_wizard;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.core.commons.chiefcontrollers.LanguageChangedEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionMLHelper;
import org.olat.modules.selectus.ui.RecruitingMainController;
import org.olat.modules.selectus.ui.events.SelectPositionEvent;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  11 aug. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ChoosePositionStepController extends StepFormBasicController {

	private SingleSelection positionSelection;
	private SingleSelection languagesSelection;
	
	private Application application;
	private final List<Position> openPositions;

	private final Locale defaultLocale;
	private final Locale[] availableLocales;
	private final Map<String,Locale> languageToLocales = new HashMap<>();

	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService erFrontendManager;
	
	public ChoosePositionStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form rootForm) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);
		setTranslator(Util.createPackageTranslator(RecruitingMainController.class, ureq.getLocale(), getTranslator()));

		defaultLocale = recruitingModule.getPositionDefaultLocale();
		availableLocales = recruitingModule.getPositionLocales();
		for(int i=availableLocales.length; i-->0; ) {
			languageToLocales.put(availableLocales[i].getLanguage(), availableLocales[i]);
		}
		application = (Application)getFromRunContext(WizardConstants.APPLICATION);
		openPositions = erFrontendManager.getPublishedPositions();
		
		initForm(ureq);
	}
	
	public boolean selectPosition(Position position, boolean canChange) {
		if(position == null) return false;
		
		boolean found = false;
		for(Position pos:openPositions) {
			if(position.equals(pos)) {
				found = true;
				break;
			}
		}
		
		if(found) {
			String positionKey = position.getKey().toString();
			positionSelection.select(positionKey, true);
			if(!canChange) {
				positionSelection.setEnabled(false);
			}
		}
		return found;
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("wizard.choose_position.title");
		setFormDescription("wizard.choose_position.explanation");
		formLayout.setElementCssClass("o_sel_choose_position");
		
		if(availableLocales.length > 1) {
			String[] theKeys = new String[availableLocales.length];
			String[] theValues = new String[availableLocales.length];
			
			for(int i=availableLocales.length; i-->0; ) {
				theKeys[i] = availableLocales[i].getLanguage();
				theValues[i] = availableLocales[i].getDisplayLanguage(getLocale());
			}
			languagesSelection = uifactory.addDropdownSingleselect("locale_chooser", "edit.application.language", formLayout, theKeys, theValues, null);
			languagesSelection.addActionListener(FormEvent.ONCHANGE);
			languagesSelection.setElementCssClass("o_sel_appwizard_select_language");
			
			boolean found = false;
			for(int i=theKeys.length; i-->0; ) {
				if(getLocale().getLanguage().equals(theKeys[i])) {
					languagesSelection.select(theKeys[i], true);
					found = true;
				}
			}
			
			if(!found) {
				for(int i=theKeys.length; i-->0; ) {
					if(defaultLocale.getLanguage().equals(theKeys[i])) {
						languagesSelection.select(theKeys[i], true);
						found = true;
					}
				}
			}
			
			if(!found) {
				languagesSelection.select(theKeys[0], true);
			}
		}
		
		String[] theKeys = new String[openPositions.size()];
		String[] theValues = new String[openPositions.size()];
		int count = 0;
		for(Position position:openPositions) {
			theKeys[count] = position.getKey().toString();
			theValues[count++] = PositionMLHelper.getPositionMLTitle(position, getLocale());
		}

		positionSelection = uifactory.addDropdownSingleselect("open_positions", "edit.application.position", formLayout, theKeys, theValues, null);
		positionSelection.setElementCssClass("o_sel_appwizard_select_position");
		positionSelection.addActionListener(FormEvent.ONCHANGE);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(languagesSelection == source) {
			if(languagesSelection.isOneSelected()) {
				Application app = (Application)getFromRunContext(WizardConstants.APPLICATION);
				app = commitChanges(app);
				addToRunContext(WizardConstants.APPLICATION, app);

				Locale newLocale = languageToLocales.get(languagesSelection.getSelectedKey());
				fireEvent(ureq, new LanguageChangedEvent(newLocale, ureq));
			}
		} else if(positionSelection == source) {
			if(positionSelection.isOneSelected()) {
				String selectedKey = positionSelection.getSelectedKey();
				for(Position position:openPositions) {
					if(selectedKey.equals(position.getKey().toString())) {
						fireEvent(ureq, new SelectPositionEvent(position));
					}
				}
			}
		}
	}

	public void forceNextStep(UserRequest ureq) {
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		positionSelection.clearError();
		if(application.getPosition() == null && !positionSelection.isOneSelected()) {
			positionSelection.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		if(languagesSelection != null) {
			allOk &= validateLanguage();
		}
		
		return allOk;
	}
	
	private boolean validateLanguage() {
		boolean allOk = true;
		
		languagesSelection.clearError();
		if(languagesSelection.isOneSelected()) {
			String selectedLanguage = languagesSelection.getSelectedKey();
			Position position = getSelectedPosition();
			if(position != null && position.getAvailableLanguagesArray() != null && position.getAvailableLanguagesArray().length >= 1) {
				String[] availableLanguages = position.getAvailableLanguagesArray();
				
				boolean foundLanguage = false;
				for(String availableLanguage:availableLanguages) {
					if(selectedLanguage.equals(availableLanguage) || "-".equals(availableLanguage)) {
						foundLanguage = true;
					}
				}
				
				if(!foundLanguage) {
					languagesSelection.setErrorKey("error.language");
					allOk &= false;
				}
			}	
		} else {
			languagesSelection.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Application app = (Application)getFromRunContext(WizardConstants.APPLICATION);
		app = commitChanges(app);
		addToRunContext(WizardConstants.APPLICATION, app);
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
	
	public Application commitChanges(Application app) {
		if(positionSelection.isOneSelected()) {
			String selectedKey = positionSelection.getSelectedKey();
			for(Position position:openPositions) {
				if(selectedKey.equals(position.getKey().toString())) {
					app.setPosition(position);
				}
			}
		}
		if(languagesSelection != null && languagesSelection.isOneSelected()) {
			app.setLanguage(languagesSelection.getSelectedKey());
		}
		
		boolean valid = app.isValid();
		application = erFrontendManager.saveTempApplication(app, valid);
		return application;
	}
	
	private Position getSelectedPosition() {
		if(positionSelection.isOneSelected()) {
			String selectedKey = positionSelection.getSelectedKey();
			for(Position position:openPositions) {
				if(selectedKey.equals(position.getKey().toString())) {
					return position;
				}
			}
		}
		return null;
	}
}