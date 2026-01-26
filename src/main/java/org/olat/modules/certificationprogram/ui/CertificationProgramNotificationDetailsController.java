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
package org.olat.modules.certificationprogram.ui;

import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.render.DomWrapperElement;
import org.olat.core.util.StringHelper;
import org.olat.core.util.i18n.ui.SingleKeyTranslatorController;
import org.olat.core.util.i18n.ui.SingleKeyTranslatorController.InputType;
import org.olat.core.util.i18n.ui.SingleKeyTranslatorController.SingleKey;
import org.olat.core.util.mail.MailHelper;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramMailConfiguration;
import org.olat.modules.certificationprogram.CertificationProgramService;
import org.olat.modules.certificationprogram.manager.CertificationProgramMailing;
import org.olat.modules.certificationprogram.manager.CertificationProgramMailing.CPMailTemplate;
import org.olat.modules.certificationprogram.manager.CertificationProgramMailing.I18nKeys;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 nov. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationProgramNotificationDetailsController extends FormBasicController {

	private FormLink customizeButton;
	private FormLink editCustomizationButton;
	
	private final CertificationProgram certificationProgram;
	private final CertificationProgramSecurityCallback secCallback;
	private final CertificationProgramNotificationRow notificationRow;
	
	private CloseableModalController cmc;
	private SingleKeyTranslatorController translatorCtrl;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CertificationProgramService certificationProgramService;
	
	public CertificationProgramNotificationDetailsController(UserRequest ureq, WindowControl wControl, Form rootForm,
			CertificationProgram certificationProgram, CertificationProgramNotificationRow notificationRow,
			CertificationProgramSecurityCallback secCallback) {
		super(ureq, wControl, LAYOUT_CUSTOM, "program_notification_details_view", rootForm);
		this.secCallback = secCallback;
		this.notificationRow = notificationRow;
		this.certificationProgram = certificationProgram;
		initForm(ureq);
	}
	
	public CertificationProgramNotificationRow getNotificationRow() {
		return notificationRow;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(secCallback.canEditCertificationProgram()) {
			if(notificationRow.isCustomized()) {
				editCustomizationButton = uifactory.addFormLink("notification.edit.customization", formLayout, Link.BUTTON);
				editCustomizationButton.setIconLeftCSS("o_icon o_icon_edit");
			} else {
				customizeButton = uifactory.addFormLink("notification.customize", formLayout, Link.BUTTON);
				customizeButton.setIconLeftCSS("o_icon o_icon_mail");
			}
		}
		
		FormLayoutContainer templateForm = uifactory.addDefaultFormLayout("templateCont", null, formLayout);
		initTemplateForm(templateForm);
	}
	
	private void initTemplateForm(FormLayoutContainer formLayout) {
		String content;
		I18nKeys i18nKeys;
		if(notificationRow.isCustomized()) {
			content = translate("notification.customized");
			i18nKeys = CertificationProgramMailing.getCustomI18nKeys(notificationRow);
		} else {
			content = translate("notification.default.template");
			i18nKeys = CertificationProgramMailing.getDefaultI18nKeys(notificationRow.getType(), certificationProgram.hasCreditPoints());
		}
		uifactory.addStaticTextElement("content", "table.header.content", content, formLayout);
		
		String body = translate(i18nKeys.body());
		String subject = translate(i18nKeys.subject());
		StringBuilder notification = new StringBuilder(1024);
		if(StringHelper.containsNonWhitespace(subject)) {
			notification.append("<div><strong>").append(StringHelper.escapeHtml(subject)).append("</strong></div>");
		}
		if(StringHelper.containsNonWhitespace(body)) {
			notification.append("<div>").append(StringHelper.xssScan(body)).append("</div>");
		}
		StaticTextElement notificationEl = uifactory.addStaticTextElement("notification", "notification.text", notification.toString(), formLayout);
		notificationEl.setDomWrapperElement(DomWrapperElement.div);
		notificationEl.setElementCssClass("form-static-control");
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(translatorCtrl == source) {
			if(event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				doSaveCustomisedTemplate(ureq); 
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(translatorCtrl);
		removeAsListenerAndDispose(cmc);
		translatorCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(customizeButton == source || editCustomizationButton == source) {
			doTranslate(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doTranslate(UserRequest ureq) {
		if(guardModalController(translatorCtrl)) return;
		
		String description = MailHelper.getVariableNamesHelp(CPMailTemplate.variableNames(), getLocale());
		I18nKeys customTemplate = CertificationProgramMailing.getCustomI18nKeys(notificationRow);
		I18nKeys template = CertificationProgramMailing.getDefaultI18nKeys(notificationRow.getType(), certificationProgram.hasCreditPoints());
		SingleKey subjectKey = new SingleKey(customTemplate.subject(), InputType.TEXT_ELEMENT, template.subject());
		SingleKey bodyKey = new SingleKey(customTemplate.body(), InputType.RICH_TEXT_ELEMENT, template.body());
		List<SingleKey> keys = List.of(subjectKey, bodyKey);
		translatorCtrl = new SingleKeyTranslatorController(ureq, getWindowControl(), keys,
				CertificationProgramNotificationsController.class, description);
		translatorCtrl.setUserObject(notificationRow);
		listenTo(translatorCtrl);

		String title = translate("translate.title", notificationRow.getNotificationLabel());
		cmc = new CloseableModalController(getWindowControl(), translate("close"), translatorCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doSaveCustomisedTemplate(UserRequest ureq) {
		CertificationProgramMailConfiguration config = certificationProgramService.getMailConfiguration(notificationRow.getKey());
		config.setCustomized(true);
		certificationProgramService.updateMailConfiguration(config);
		dbInstance.commit();
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
}
