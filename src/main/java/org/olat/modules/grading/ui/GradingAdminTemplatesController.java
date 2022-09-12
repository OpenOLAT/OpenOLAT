/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.grading.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.Formatter;
import org.olat.core.util.i18n.ui.SingleKeyTranslatorController;
import org.olat.core.util.mail.MailHelper;
import org.olat.modules.grading.ui.component.GraderMailTemplate;

/**
 * 
 * Initial date: 11 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GradingAdminTemplatesController extends FormBasicController {
	
	private int counter = 0;
	
	private CloseableModalController cmc;
	private SingleKeyTranslatorController translatorCtrl;
	
	public GradingAdminTemplatesController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("admin.email.title");
		
		// new grader
		initForm("mail.to.grader.subject", "mail.grader.to.entry.subject", false, formLayout);
		initForm("mail.to.grader.body", "mail.grader.to.entry.body", true, formLayout);
		uifactory.addSpacerElement("spacer-new-grader", formLayout, false);
		
		// notifications
		initForm("notification.subject", "mail.notification.subject", false, formLayout);
		initForm("notification.body", "mail.notification.body", true, formLayout);
		uifactory.addSpacerElement("spacer-notification", formLayout, false);
		
		// reminder 1
		initForm("reminder.1.subject", "mail.reminder1.subject", false, formLayout);
		initForm("reminder.1.body", "mail.reminder1.body", true, formLayout);
		uifactory.addSpacerElement("spacer-1-reminder", formLayout, false);
		
		// reminder 2
		initForm("reminder.2.subject", "mail.reminder2.subject", false, formLayout);
		initForm("reminder.2.body", "mail.reminder2.body", true, formLayout);
		uifactory.addSpacerElement("spacer-2-reminder", formLayout, false);
		
		// notifications participant
		initForm("notification.participant.subject", "mail.notification.participant.subject", false, formLayout);
		initForm("notification.participant.body", "mail.notification.participant.body", true, formLayout);
	}

	private void initForm(String labelI18nKey, String textI18nKey, boolean multiLines, FormItemContainer formLayout) {
		String text = translate(textI18nKey);
		if(multiLines) {
			text = Formatter.escWithBR(text).toString();
		}
		
		StaticTextElement viewEl = uifactory.addStaticTextElement("view." + counter++, labelI18nKey, text, formLayout);
		FormLink translationLink = uifactory.addFormLink("translate." + counter++, "translate", null, formLayout, Link.LINK);
		translationLink.setUserObject(new TranslationBundle(textI18nKey, labelI18nKey, multiLines, viewEl));
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(translatorCtrl == source) {
			doUpdate((TranslationBundle)translatorCtrl.getUserObject());
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(translatorCtrl);
		removeAsListenerAndDispose(cmc);
		translatorCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink) {
			if(source.getUserObject() instanceof TranslationBundle) {
				doTranslate(ureq, (TranslationBundle)source.getUserObject());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doTranslate(UserRequest ureq, TranslationBundle bundle) {
		if(guardModalController(translatorCtrl)) return;

		SingleKeyTranslatorController.InputType inputType = bundle.isMultiLines() ? SingleKeyTranslatorController.InputType.TEXT_AREA : SingleKeyTranslatorController.InputType.TEXT_ELEMENT;
		String description = MailHelper.getVariableNamesHelp(GraderMailTemplate.variableNames(), getLocale());
		translatorCtrl = new SingleKeyTranslatorController(ureq, getWindowControl(), bundle.getI18nKey(),
				GradingAdminTemplatesController.class, inputType, description);
		translatorCtrl.setUserObject(bundle);
		listenTo(translatorCtrl);

		String title = translate("translate.title", new String[] { translate(bundle.getLabelI18nKey()) });
		cmc = new CloseableModalController(getWindowControl(), "close", translatorCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doUpdate(TranslationBundle bundle) {
		String text = translate(bundle.getI18nKey());
		if(bundle.isMultiLines()) {
			text = Formatter.escWithBR(text).toString();
		}
		bundle.getViewEl().setValue(text);
	}
	
	private static class TranslationBundle {
		
		private final String i18nKey;
		private final boolean multiLines;
		private final String labelI18nKey;
		private final StaticTextElement viewEl;
		
		public TranslationBundle(String i18nKey, String labelI18nKey, boolean multiLines, StaticTextElement viewEl) {
			this.i18nKey = i18nKey;
			this.viewEl = viewEl;
			this.multiLines = multiLines;
			this.labelI18nKey = labelI18nKey;
		}

		public StaticTextElement getViewEl() {
			return viewEl;
		}

		public String getI18nKey() {
			return i18nKey;
		}
		
		public String getLabelI18nKey() {
			return labelI18nKey;
		}

		public boolean isMultiLines() {
			return multiLines;
		}
	}
}
