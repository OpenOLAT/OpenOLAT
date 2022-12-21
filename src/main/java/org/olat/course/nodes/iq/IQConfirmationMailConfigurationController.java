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
package org.olat.course.nodes.iq;

import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailHelper;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.AbstractAccessableCourseNode;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 * Initial date: 20 déc. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IQConfirmationMailConfigurationController extends FormBasicController {

	private FormToggle enableEl;
	private TextElement subjectEl;
	private RichTextElement emailEl;
	private TextElement customEmailEl;
	private SingleSelection templateEl;
	private StaticTextElement emailTemplateEl;
	private MultipleSelectionElement emailCopyEl;
	
	private final ModuleConfiguration config;
	
	public IQConfirmationMailConfigurationController(UserRequest ureq, WindowControl wControl, AbstractAccessableCourseNode courseNode) {
		super(ureq, wControl);
		config = courseNode.getModuleConfiguration();
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("confirmation.mail.title");
		setFormTitleIconCss("o_icon o_icon_mail");
		setFormDescription("confirmation.mail.description");
		
		SelectionValues onKeyValues = new SelectionValues();
		onKeyValues.add(SelectionValues.entry("on", ""));
		enableEl = uifactory.addToggleButton("email.enable", translate("confirmation.mail.enable"), "&nbsp;", formLayout, null, null);
		enableEl.addActionListener(FormEvent.ONCHANGE);
		if(config.getBooleanSafe(IQEditController.CONFIG_KEY_CONFIRMATION_EMAIL_ENABLED, false)) {
			enableEl.toggleOn();
		} else {
			enableEl.toggleOff();
		}
		
		SelectionValues emailCopyKV = new SelectionValues();
		emailCopyKV.add(entry(IQEditController.CONFIG_KEY_CONFIRMATION_EMAIL_COPY_TO_OWNER, translate("confirmation.mail.copy.owner")));
		emailCopyKV.add(entry(IQEditController.CONFIG_KEY_CONFIRMATION_EMAIL_COPY_TO_ASSIGNED_COACH, translate("confirmation.mail.copy.assignedCoach")));
		emailCopyKV.add(entry(IQEditController.CONFIG_KEY_CONFIRMATION_EMAIL_COPY_TO_CUSTOM, translate("confirmation.mail.copy.custom")));
		
		emailCopyEl = uifactory.addCheckboxesVertical("confirmation.mail.copy", formLayout, emailCopyKV.keys(), emailCopyKV.values(), 1);
		emailCopyEl.addActionListener(FormEvent.ONCHANGE);
		
		List<String> copyList = config.getList(IQEditController.CONFIG_KEY_CONFIRMATION_EMAIL_COPY, String.class);
		for (String copy : copyList) {
			if (emailCopyEl.getKeys().contains(copy)) {
				emailCopyEl.select(copy, true);
			}
		}

		String copyCustom = config.getStringValue(IQEditController.CONFIG_KEY_CONFIRMATION_EMAIL_COPY_CUSTOM);
		customEmailEl = uifactory.addTextElement("confirmation.mail.custom", "confirmation.mail.copy.custom", 1024, copyCustom, formLayout);
		customEmailEl.setExampleKey("confirmation.mail.custom.placeholder", null);
		
		//email subject
		String subject = config.getStringValue(IQEditController.CONFIG_KEY_CONFIRMATION_EMAIL_SUBJECT);
		if(!StringHelper.containsNonWhitespace(subject)) {
			subject = translate("confirmation.mail.content.subject");
		}
		subjectEl = uifactory.addTextElement("confirmation.mail.subject", "confirmation.mail.subject", 128, subject, formLayout);
		subjectEl.setNotEmptyCheck();
		subjectEl.setElementCssClass("o_sel_course_reminder_subject");
		
		SelectionValues templateKeyValues = new SelectionValues();
		templateKeyValues.add(SelectionValues.entry("template", translate("confirmation.mail.template.template")));
		templateKeyValues.add(SelectionValues.entry("custom", translate("confirmation.mail.template.custom")));
		templateEl = uifactory.addDropdownSingleselect("confirmation.mail.template", formLayout,
				templateKeyValues.keys(), templateKeyValues.values());
		templateEl.addActionListener(FormEvent.ONCHANGE);
		templateEl.setMandatory(true);
		String text = config.getStringValue(IQEditController.CONFIG_KEY_CONFIRMATION_EMAIL_BODY);
		boolean hasText = StringHelper.containsNonWhitespace(text);
		if(hasText) {
			templateEl.select("custom", true);
		} else {
			templateEl.select("template", true);
			String correctionMode = config.getStringValue(IQEditController.CONFIG_CORRECTION_MODE);
			if(IQEditController.CORRECTION_AUTO.equals(correctionMode)) {
				text = translate("confirmation.mail.content.auto");
			} else {
				text = translate("confirmation.mail.content.manual");
			}
		}
		
		emailEl = uifactory.addRichTextElementForStringDataMinimalistic("confirmation.mail.body", "confirmation.mail.body", text, 10, 60, formLayout, getWindowControl());
		emailEl.getEditorConfiguration().setRelativeUrls(false);
		emailEl.getEditorConfiguration().setRemoveScriptHost(false);
		emailEl.setMandatory(true);
		MailHelper.setVariableNamesAsHelp(emailEl, IQConfirmationMailTemplate.variableNames(), getLocale());
		
		emailTemplateEl = uifactory.addStaticTextElement("confirmation.mail.body.template", "confirmation.mail.body", text, formLayout);
		
		FormItemContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("save", buttonsCont);
	}
	
	private void updateUI() {
		boolean enabled = enableEl.isOn();
		emailCopyEl.setVisible(enabled);
		boolean customEmail = emailCopyEl.getSelectedKeys()
				.contains(IQEditController.CONFIG_KEY_CONFIRMATION_EMAIL_COPY_TO_CUSTOM);
		customEmailEl.setVisible(enabled && customEmail);
		subjectEl.setVisible(enabled);
		templateEl.setVisible(enabled);
		emailEl.setVisible(enabled);
		boolean customBody = templateEl.isOneSelected() && "custom".equals(templateEl.getSelectedKey());
		emailEl.setVisible(enabled &&customBody);
		emailTemplateEl.setVisible(enabled &&!customBody);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == emailCopyEl || source == enableEl || source == templateEl) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean enabled = enableEl.isOn();
		config.setBooleanEntry(IQEditController.CONFIG_KEY_CONFIRMATION_EMAIL_ENABLED, enabled);
		
		if(enabled) {
			Collection<String> copyKeys = emailCopyEl.getSelectedKeys();
			config.setList(IQEditController.CONFIG_KEY_CONFIRMATION_EMAIL_COPY, new ArrayList<>(copyKeys));
			String customCopy = customEmailEl.getValue();
			if(StringHelper.containsNonWhitespace(customCopy)) {
				config.setStringValue(IQEditController.CONFIG_KEY_CONFIRMATION_EMAIL_COPY_CUSTOM, customCopy);
			} else {
				config.remove(IQEditController.CONFIG_KEY_CONFIRMATION_EMAIL_COPY_CUSTOM);
			}
			String subject = subjectEl.getValue();
			config.setStringValue(IQEditController.CONFIG_KEY_CONFIRMATION_EMAIL_SUBJECT, subject);
			if(templateEl.isOneSelected()
					&& templateEl.isKeySelected(IQEditController.CONFIG_KEY_CONFIRMATION_EMAIL_COPY_TO_CUSTOM)
					&& StringHelper.containsNonWhitespace(emailEl.getValue())) {
				config.setStringValue(IQEditController.CONFIG_KEY_CONFIRMATION_EMAIL_BODY, emailEl.getValue());
			} else {
				config.remove(IQEditController.CONFIG_KEY_CONFIRMATION_EMAIL_BODY);
			}
		} else {
			config.remove(IQEditController.CONFIG_KEY_CONFIRMATION_EMAIL_COPY);
			config.remove(IQEditController.CONFIG_KEY_CONFIRMATION_EMAIL_COPY_CUSTOM);
			config.remove(IQEditController.CONFIG_KEY_CONFIRMATION_EMAIL_SUBJECT);
			config.remove(IQEditController.CONFIG_KEY_CONFIRMATION_EMAIL_BODY);
		}
		
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}
}
