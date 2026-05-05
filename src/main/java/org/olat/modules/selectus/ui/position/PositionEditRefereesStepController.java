/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.position;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormCancel;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.util.SyntheticUserRequest;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.position.TabConfiguration;
import org.olat.modules.selectus.model.position.TabsConfiguration.Tab;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.app_wizard.RefereesStepController;
import org.olat.modules.selectus.ui.events.NewPositionSavedEvent;
import org.olat.modules.selectus.ui.reference.ReferenceHelper;

/**
 * 
 * Initial date: 12 sept. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionEditRefereesStepController extends FormBasicController implements PositionEditableController {

	private final List<FormLink> previewButtons = new ArrayList<>(2);
	private final List<TextElement> helpEls = new ArrayList<>(3);
	private final List<TextElement> additionalHelpEls;
	private FormLayoutContainer helpContainer;
	
	private Position position;
	private final boolean readOnly;
	private TabConfiguration tabConfiguration;
	private List<Locale> positionLanguages;
	private final TabsConfigurationDelegate tabsConfigurationDelegate = new TabsConfigurationDelegate(Tab.referees);
	
	private CloseableModalController cmc;
	private RefereesStepController refereesCtrl;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	
	public PositionEditRefereesStepController(UserRequest ureq, WindowControl wControl, Position position, boolean readOnly) {
		super(ureq, wControl, "edit_referees", Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.position = position;
		this.readOnly = readOnly;
		tabConfiguration = position.getTabConfiguration(Tab.referees);
		tabsConfigurationDelegate.defaultHelpText(position, tabConfiguration);
		positionLanguages = recruitingModule.getPositionLocales(position);
		if(recruitingModule.isReferenceApplicantManagement()) {
			additionalHelpEls = new ArrayList<>(3);
		} else {
			additionalHelpEls = null;// disable them
		}
		initForm(ureq);
	}

	@Override
	public Position getPosition() {
		return position;
	}

	@Override
	public void updatePosition(Position updatedPosition) {
		this.position = updatedPosition;
		positionLanguages = recruitingModule.getPositionLocales(position);
		tabsConfigurationDelegate.updateHelps(positionLanguages, tabConfiguration, helpContainer,
				helpEls, additionalHelpEls, getWindowControl(), false);
		initPreviewButtons(flc);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		helpContainer = tabsConfigurationDelegate.initHelpTexts(positionLanguages, tabConfiguration, formLayout, mainForm,
						helpEls, additionalHelpEls, getWindowControl(), false, readOnly);
		initPreviewButtons(formLayout);

		FormSubmit submitButton = uifactory.addFormSubmitButton("save", formLayout);
		submitButton.setVisible(!readOnly);
		FormCancel cancelButton = uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		cancelButton.setVisible(!readOnly);
	}
	
	private void initPreviewButtons(FormItemContainer formLayout) {
		boolean refereeEnabled = position.isRefereeRecommendationEnabled();
		boolean expertEnabled = position.isExpertRecommendationEnabled();
		boolean previewEnabled = refereeEnabled
				|| (recruitingModule.isReferenceExpertsBlackListEnabled() && expertEnabled);
		
		if(!previewButtons.isEmpty()) {
			for(FormLink previewButton:previewButtons) {
				formLayout.remove(previewButton);
			}
			previewButtons.clear();
		}
		
		for(Locale locale:positionLanguages) {
			String link;
			if(positionLanguages.size() == 1) {
				link = translate("edit.template.preview");
			} else {
				link = translate("edit.template.preview_ml", new String[] { locale.getLanguage() });
			}
			FormLink previewButton = uifactory.addFormLink("preview_".concat(locale.getLanguage()), "preview", link, null, formLayout, Link.BUTTON | Link.NONTRANSLATED);
			previewButton.setUserObject(locale);
			previewButton.setEnabled(previewEnabled);
			previewButtons.add(previewButton);
		}
		formLayout.contextPut("previewButtons", previewButtons);

		String path = "[Positions:0][Position:" + position.getKey() + "][Edit:0][References:0]";
		String refereeConfigUrl = BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathStrings(path);
		String refereeInfosI18nKey = recruitingModule.isReferenceExpertsBlackListEnabled() ? "referees.infos.exclude.list" : "referees.infos";
		String infos = translate(refereeInfosI18nKey, refereeConfigUrl);
		formLayout.contextPut("infos", infos);

		String refereesState = refereeEnabled ? translate("referees.feature.states.referees.enabled") : translate("referees.feature.states.referees.disabled");
		String expertsState = "";
		if(recruitingModule.isReferenceExpertsBlackListEnabled()) {
			expertsState = expertEnabled ? translate("referees.feature.states.experts.enabled") : translate("referees.feature.states.experts.disabled");
		}
		String stepState = "";
		if(!previewEnabled) {
			stepState = translate("referees.feature.states.step.disabled");
		}
		String[] statesArgs = new String[] { refereesState, expertsState, stepState };
		formLayout.contextPut("featureStates", translate("referees.feature.states", statesArgs));
		formLayout.contextPut("featureEnabled", Boolean.valueOf(refereeEnabled));
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(refereesCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(refereesCtrl);
		removeAsListenerAndDispose(cmc);
		refereesCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("preview".equals(link.getCmd())) {
				doPreview(ureq, (Locale)link.getUserObject());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Event doneEvent = Event.DONE_EVENT;
		if(position.getKey() != null) {
			position = recruitingService.getPosition(position.getKey());
		} else {
			doneEvent = new NewPositionSavedEvent();
		}
		
		save();
		fireEvent(ureq, doneEvent);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	private void save() {
		tabsConfigurationDelegate.save(position, tabConfiguration, helpEls, additionalHelpEls, null);
		position = recruitingService.savePosition(position);
		dbInstance.commit();
	}
	
	private void doPreview(UserRequest ureq, Locale locale) {
		if(!locale.getLanguage().equals(getLocale().getLanguage())) {
			ureq = new SyntheticUserRequest(getIdentity(), locale);
		}
		
		TabConfiguration tempConfiguration = new TabConfiguration();
		for(TextElement helpEl:helpEls) {
			Locale loc = (Locale)helpEl.getUserObject();
			tempConfiguration.setHelp(helpEl.getValue(), loc);
		}
		if(additionalHelpEls != null && !additionalHelpEls.isEmpty()) {
			for(TextElement helpEl:additionalHelpEls) {
				Locale loc = (Locale)helpEl.getUserObject();
				tempConfiguration.setAdditionalHelp(helpEl.getValue(), loc);
			}
		}
		
		Application application = ReferenceHelper.generateDummyApplication(position);
		refereesCtrl = new RefereesStepController(ureq, getWindowControl(), position, application, tempConfiguration);
		listenTo(refereesCtrl);
		
		String title;
		if(previewButtons.size() == 1) {
			title = translate("edit.template.preview");
		} else {
			title = translate("edit.template.preview_ml", new String[] { locale.getLanguage() });
		}
		cmc = new CloseableModalController(getWindowControl(), "c", refereesCtrl.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();
	}
}
