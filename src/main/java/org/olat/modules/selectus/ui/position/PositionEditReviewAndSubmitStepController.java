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
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;
import org.olat.modules.selectus.model.position.TabConfiguration;
import org.olat.modules.selectus.model.position.TabsConfiguration.Tab;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.app_wizard.RefereeList;
import org.olat.modules.selectus.ui.app_wizard.ReviewAndSubmitController;
import org.olat.modules.selectus.ui.events.NewPositionSavedEvent;
import org.olat.modules.selectus.ui.reference.ReferenceHelper;

/**
 * 
 * 
 * Initial date: 27 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionEditReviewAndSubmitStepController extends FormBasicController implements PositionEditableController {

	private FormCancel cancelButton;
	private TextElement jobAdsElement;
	private FormLayoutContainer helpContainer;
	private List<FormLink> previewButtons = new ArrayList<>(2);
	private List<TextElement> helpEls = new ArrayList<>(2);

	private Position position;
	private final boolean readOnly;
	private TabConfiguration tabConfiguration;
	private List<Locale> positionLanguages;
	private final TabsConfigurationDelegate tabsConfigurationDelegate;
	
	private CloseableModalController cmc;
	private ReviewAndSubmitController reviewAndSubmitCtrl;

	@Autowired
	private DB dbInstance;
	@Autowired
	private AuditService auditService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	
	public PositionEditReviewAndSubmitStepController(UserRequest ureq, WindowControl wControl, Position position, boolean readOnly) {
		super(ureq, wControl, "edit_review_and_submit", Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.position = position;
		this.readOnly = readOnly;
		positionLanguages = recruitingModule.getPositionLocales(position);
		tabConfiguration = position.getTabConfiguration(Tab.reviewAndSubmit);
		tabsConfigurationDelegate = new TabsConfigurationDelegate(Tab.reviewAndSubmit);
		tabsConfigurationDelegate.defaultHelpText(position, tabConfiguration);
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
				helpEls, null, getWindowControl(), false);
		initPreviewButtons(flc);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		helpContainer = tabsConfigurationDelegate
				.initHelpTexts(positionLanguages, tabConfiguration, formLayout, mainForm, helpEls, null, getWindowControl(), false, readOnly);
		initReviewAndSubmitForm(formLayout, ureq);
	}
	
	private void initReviewAndSubmitForm(FormItemContainer formLayout, UserRequest ureq) {
		FormLayoutContainer reviewAndSubmitCont = FormLayoutContainer.createDefaultFormLayout_2_10("reviewAndSubmit", getTranslator());
		formLayout.add("reviewAndSubmit", reviewAndSubmitCont);
		reviewAndSubmitCont.setRootForm(mainForm);

		String jobAds;
		if(position.getKey() == null) {
			StringBuilder ads = new StringBuilder();
			for(String ad:recruitingModule.getFeedbackOptions()) {
				if(ads.length() > 0) ads.append("\n");
				ads.append(ad);
			}
			jobAds = ads.toString();
		} else {
			jobAds = position.getJobAds();
		}
		jobAdsElement = uifactory.addTextAreaElement("jobads", "edit.jobads", 1024, 10, 60, false, false, false, jobAds, reviewAndSubmitCont);
		jobAdsElement.setVisible(!recruitingModule.isPositionJobAdsFreeTextOnlyEnabled());
		jobAdsElement.setEnabled(!readOnly);
		
		FormSubmit submitButton = uifactory.addFormSubmitButton("save", formLayout);
		submitButton.setVisible(!readOnly);
		initPreviewButtons(formLayout);
		cancelButton = uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		cancelButton.setVisible(!readOnly);
	}
	
	private void initPreviewButtons(FormItemContainer formLayout) {
		if(!previewButtons.isEmpty()) {
			for(FormLink previewButton:previewButtons) {
				formLayout.remove(previewButton);
			}
			previewButtons.clear();
			if(cancelButton != null) {
				formLayout.remove(cancelButton);
			}
		}
		
		List<Locale> locales  = recruitingModule.getPositionLocales(position);
		for(Locale locale:locales) {
			String link;
			if(locales.size() == 1) {
				link = translate("edit.template.preview");
			} else {
				link = translate("edit.template.preview_ml", new String[] { locale.getLanguage() });
			}
			FormLink previewButton = uifactory.addFormLink("preview_".concat(locale.getLanguage()), "preview", link, null, formLayout, Link.BUTTON | Link.NONTRANSLATED);
			previewButton.setUserObject(locale);
			previewButtons.add(previewButton);
		}
		formLayout.contextPut("previewButtons", previewButtons);
		if(cancelButton != null) {
			formLayout.add(cancelButton);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(reviewAndSubmitCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(reviewAndSubmitCtrl);
		removeAsListenerAndDispose(cmc);
		reviewAndSubmitCtrl = null;
		cmc = null;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		jobAdsElement.clearError();
		if(jobAdsElement.getValue() != null && jobAdsElement.getValue().length() > 1000) {
			jobAdsElement.setErrorKey("input.toolong", new String[]{ Integer.toString(1000) });
			allOk &= false;
		}
		
		return allOk;
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
		
		String before = auditService.toAuditXml(position);

		commitChanges();

		tabsConfigurationDelegate.save(position, tabConfiguration, helpEls, null, null);
		
		position = recruitingService.savePosition(position);
		dbInstance.commit();
		getLogger().info(Tracing.M_AUDIT, "Update position: {}", position.toStringFull());
		
		String after = auditService.toAuditXml(position);
		if(!before.equals(after)) {
			String messageI18n = "audit.log.position.change.configuration";
			String[] messageArgs = new String[] { position.getMLTitle(recruitingModule.getPositionDefaultLocale()) };
			auditService.auditPositionLog(Action.changeConfiguration, ActionTarget.position, before, after,
					messageI18n, messageArgs, getTranslator(), position, getIdentity());
		}
		
		fireEvent(ureq, doneEvent);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	private void commitChanges() {
		position.setJobAds(jobAdsElement.getValue());
	}
	
	private void doPreview(UserRequest ureq, Locale locale) {
		commitChanges();
		
		if(!locale.getLanguage().equals(getLocale().getLanguage())) {
			ureq = new SyntheticUserRequest(getIdentity(), locale);
		}
		
		TabConfiguration tempConfiguration = new TabConfiguration();
		for(TextElement helpEl:helpEls) {
			Locale loc = (Locale)helpEl.getUserObject();
			tempConfiguration.setHelp(helpEl.getValue(), loc);
		}
		
		Application application = ReferenceHelper.generateDummyApplicationExtended(position);
		reviewAndSubmitCtrl = new ReviewAndSubmitController(ureq, getWindowControl(), null, position, application, tempConfiguration, new RefereeList());
		listenTo(reviewAndSubmitCtrl);
		
		String title;
		if(previewButtons.size() == 1) {
			title = translate("edit.template.preview");
		} else {
			title = translate("edit.template.preview_ml", new String[] { locale.getLanguage() });
		}
		cmc = new CloseableModalController(getWindowControl(), "c", reviewAndSubmitCtrl.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();
	}
}
