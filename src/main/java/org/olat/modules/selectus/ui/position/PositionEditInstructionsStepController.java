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
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.util.SyntheticUserRequest;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;
import org.olat.modules.selectus.model.position.TabConfiguration;
import org.olat.modules.selectus.model.position.TabsConfiguration.Tab;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.app_wizard.InstructionsController;
import org.olat.modules.selectus.ui.events.NewPositionSavedEvent;

/**
 * 
 * Initial date: 28 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionEditInstructionsStepController extends FormBasicController implements PositionEditableController {

	private FormLink editTextButton;
	private FormLink resetTextButton;
	private FormLink customizeTextButton;
	private List<FormLink> previewButtons = new ArrayList<>(2);
	
	private Position position;
	private final boolean readOnly;
	private TabConfiguration tabConfiguration;
	
	private CloseableModalController cmc;
	private InstructionsController previewCtrl;
	private InstructionsController instructionsCtrl;
	private PositionEditInstructionsTextController editCtrl;
	private ConfirmContinueController confirmResetCtrl;
	private ConfirmContinueController confirmCustomizeCtrl;
	
	private Locale instructionsLocale;

	@Autowired
	private DB dbInstance;
	@Autowired
	private AuditService auditService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	
	public PositionEditInstructionsStepController(UserRequest ureq, WindowControl wControl, Position position, boolean readOnly) {
		super(ureq, wControl, "edit_instructions", Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.position = position;
		this.readOnly = readOnly;
		
		List<Locale> locales = recruitingModule.getPositionLocales(position);
		if(locales.size() == 1) {
			instructionsLocale = locales.get(0);
		} else {
			instructionsLocale = getLocale();
		}
		
		tabConfiguration = position.getTabConfiguration(Tab.instructions);
		initForm(ureq);
	}

	@Override
	public Position getPosition() {
		return position;
	}

	@Override
	public void updatePosition(Position updatedPosition) {
		this.position = updatedPosition;
		tabConfiguration = position.getTabConfiguration(Tab.instructions);
		initInstructions(flc);
		initPreviewButtons(flc);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		resetTextButton = uifactory.addFormLink("reset.text", formLayout, Link.BUTTON);
		customizeTextButton = uifactory.addFormLink("customize.text", formLayout, Link.BUTTON);
		editTextButton = uifactory.addFormLink("customize.edit.text", formLayout, Link.BUTTON);
		
		initInstructions(formLayout);
		initPreviewButtons(formLayout);
		formLayout.contextPut("previewButtons", previewButtons);
		
		if(!readOnly) {
			uifactory.addFormSubmitButton("save", formLayout);
			uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		}
	}
	
	private void initInstructions(FormItemContainer formLayout) {
		removeAsListenerAndDispose(instructionsCtrl);
		
		UserRequest ureq = new SyntheticUserRequest(getIdentity(), instructionsLocale);
		String instructions = tabConfiguration.getHelp(instructionsLocale);
		if(StringHelper.containsNonWhitespace(instructions)) {
			formLayout.contextPut("instructionsText", instructions);
			resetTextButton.setVisible(!readOnly);
			editTextButton.setVisible(!readOnly);
			customizeTextButton.setVisible(!readOnly);
		} else {
			formLayout.contextPut("instructionsText", "");
			instructionsCtrl = new InstructionsController(ureq, getWindowControl(), mainForm, position, tabConfiguration, false);
			listenTo(instructionsCtrl);
			formLayout.add("instructions", instructionsCtrl.getInitialFormItem());
			resetTextButton.setVisible(false);
			editTextButton.setVisible(false);
			customizeTextButton.setVisible(!readOnly);
		}
	}
	
	private void initPreviewButtons(FormItemContainer formLayout) {
		if(!previewButtons.isEmpty()) {
			for(FormLink previewButton:previewButtons) {
				formLayout.remove(previewButton);
			}
			previewButtons.clear();
		}
		
		List<Locale> locales  = recruitingModule.getPositionLocales(position);
		for(Locale locale:locales) {
			String link;
			if(locales.size() == 1 && locale.equals(getLocale())) {
				link = translate("edit.template.preview");
			} else {
				link = translate("edit.template.preview_ml", new String[] { locale.getLanguage() });
			}
			FormLink previewButton = uifactory.addFormLink("preview_".concat(locale.getLanguage()), "preview", link, null, formLayout, Link.BUTTON | Link.NONTRANSLATED);
			previewButton.setUserObject(locale);
			previewButtons.add(previewButton);
		}
		formLayout.contextPut("previewButtons", previewButtons);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(previewCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(editCtrl == source) {
			if(event == Event.DONE_EVENT) {
				tabConfiguration = editCtrl.getConfiguration();
				initInstructions(flc);
			}
			cmc.deactivate();
			cleanUp();
			markDirty();
		} else if(confirmResetCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doResetText();
			}
			cmc.deactivate();
			cleanUp();
			markDirty();
		} else if(confirmCustomizeCtrl == source) {
			cmc.deactivate();
			cleanUp();
			if(event == Event.DONE_EVENT) {
				doCustomizeText(ureq);
			}
			markDirty();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmCustomizeCtrl);
		removeAsListenerAndDispose(confirmResetCtrl);
		removeAsListenerAndDispose(previewCtrl);
		removeAsListenerAndDispose(editCtrl);
		removeAsListenerAndDispose(cmc);
		confirmCustomizeCtrl = null;
		confirmResetCtrl = null;
		previewCtrl = null;
		editCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(resetTextButton == source) {
			doConfirmResetText(ureq);
		} else if(customizeTextButton == source) {
			doConfirmCustomizeText(ureq);
		} else if(editTextButton == source) {
			doCustomizeText(ureq);
		} else if(source instanceof FormLink) {
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
		
		position.setTabConfiguration(Tab.instructions, tabConfiguration);
		
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
	
	private void doConfirmResetText(UserRequest ureq) {
		String title = translate("confirm.reset.instructions.title");
		String message = translate("confirm.reset.instructions.text");
		confirmResetCtrl = new ConfirmContinueController(ureq, getWindowControl(), message);
		listenTo(confirmResetCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "c", confirmResetCtrl.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doResetText() {
		tabConfiguration.setHelp(null);
		tabConfiguration.setHelpDe(null);
		initInstructions(flc);
	}
	
	private void doConfirmCustomizeText(UserRequest ureq) {
		String title = translate("confirm.customize.instructions.title");
		String message = translate("confirm.customize.instructions.text");
		confirmCustomizeCtrl = new ConfirmContinueController(ureq, getWindowControl(), message);
		listenTo(confirmCustomizeCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "c", confirmCustomizeCtrl.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doCustomizeText(UserRequest ureq) {
		editCtrl = new PositionEditInstructionsTextController(ureq, getWindowControl(), position, tabConfiguration);
		listenTo(editCtrl);

		String title = "Edit";
		cmc = new CloseableModalController(getWindowControl(), "c", editCtrl.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();
	}

	private void doPreview(UserRequest ureq, Locale locale) {
		if(!locale.getLanguage().equals(getLocale().getLanguage())) {
			ureq = new SyntheticUserRequest(getIdentity(), locale);
		}
		
		previewCtrl = new InstructionsController(ureq, getWindowControl(), null, position, tabConfiguration, true);
		listenTo(previewCtrl);
		
		String title;
		if(previewButtons.size() == 1) {
			title = translate("edit.template.preview");
		} else {
			title = translate("edit.template.preview_ml", new String[] { locale.getLanguage() });
		}
		cmc = new CloseableModalController(getWindowControl(), "c", previewCtrl.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();
	}
}
