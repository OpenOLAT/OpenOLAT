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
package org.olat.ims.cp.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.UUID;

import org.apache.velocity.VelocityContext;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.User;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailLoggingAction;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.modules.co.ContactForm;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;
import org.olat.resource.references.ReferenceManager;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 1 sept. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CPNotificationsController extends FormBasicController {
	
	private static final String EXTERNAL = "external";
	
	private TextElement subjectEl;
	private RichTextElement bodyEl;
	private TextElement externalAddressesEl;
	private MultipleSelectionElement recipientsEl;
	private MultipleSelectionElement copyFromEl;
	
	private final RepositoryEntry entry;
	
	@Autowired
	private MailManager mailService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private ReferenceManager referenceManager;
	@Autowired
	private RepositoryService repositoryService;
	
	public CPNotificationsController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		super(ureq, wControl, LAYOUT_VERTICAL, Util.createPackageTranslator(ContactForm.class, ureq.getLocale()));
		this.entry = entry;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// Mail
		FormLayoutContainer mailCont = FormLayoutContainer.createVerticalFormLayout("mail", getTranslator());
		formLayout.add(mailCont);
		
		String[] args = getArguments(ureq);
		String subject = translate("contact.mail.subject", args);
		String body = translate("contact.mail.body", args);
		
		subjectEl = uifactory.addTextElement("subject", "mail.subject", 255, subject, mailCont);
		subjectEl.setElementCssClass("o_sel_mail_subject");
		subjectEl.setDisplaySize(255);
		subjectEl.setMandatory(true);
		bodyEl = uifactory.addRichTextElementForStringDataMinimalistic("body", "mail.body", body, 9, 8, mailCont, getWindowControl());
		bodyEl.setElementCssClass("o_sel_mail_body");
		bodyEl.setMandatory(true);
		bodyEl.getEditorConfiguration().setRelativeUrls(false);
		bodyEl.getEditorConfiguration().setRemoveScriptHost(false);
		
		// Recipients
		FormLayoutContainer recipientsCont = FormLayoutContainer.createVerticalFormLayout("recipients", getTranslator());
		recipientsCont.setFormTitle(translate("contact.title"));
		recipientsCont.setFormInfo(translate("contact.infos"));
		formLayout.add(recipientsCont);
		
		SelectionValues recipientsKeysValues = new SelectionValues();
		recipientsKeysValues.add(SelectionValues.entry(GroupRoles.owner.name(), translate("contact.all.owners")));
		recipientsKeysValues.add(SelectionValues.entry(GroupRoles.coach.name(), translate("contact.all.coaches")));
		recipientsKeysValues.add(SelectionValues.entry(GroupRoles.participant.name(), translate("contact.all.participants")));
		recipientsKeysValues.add(SelectionValues.entry(EXTERNAL, translate("contact.external")));

		recipientsEl = uifactory.addCheckboxesVertical("contact.all.owners", "send.mail.to", recipientsCont,
				recipientsKeysValues.keys(), recipientsKeysValues.values(), 1);
		recipientsEl.setElementCssClass("o_sel_cp_mail_recipients");
		recipientsEl.addActionListener(FormEvent.ONCHANGE);
		
		externalAddressesEl = uifactory.addTextAreaElement("contact.external.list", null, 4096, 3, 60, false, false, "", recipientsCont);
		externalAddressesEl.setExampleKey("contact.external.list.example", null);
		externalAddressesEl.setElementCssClass("o_sel_cp_external_mail");
		externalAddressesEl.setVisible(false);
		
		SelectionValues copyKeysValues = new SelectionValues();
		copyKeysValues.add(SelectionValues.entry("copy", translate("copy.from.to")));
		copyFromEl = uifactory.addCheckboxesHorizontal("copy.from", "copy.from", recipientsCont,
				copyKeysValues.keys(), copyKeysValues.values());
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		recipientsCont.add(buttonsCont);
		uifactory.addFormSubmitButton("send.notification", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		subjectEl.clearError();
		if(!StringHelper.containsNonWhitespace(subjectEl.getValue())) {
			subjectEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		bodyEl.clearError();
		if(!StringHelper.containsNonWhitespace(bodyEl.getValue())) {
			bodyEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		recipientsEl.clearError();
		if(!recipientsEl.isAtLeastSelected(1) && !copyFromEl.isAtLeastSelected(1)) {
			recipientsEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}

		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == recipientsEl) {
			externalAddressesEl.setVisible(recipientsEl.getSelectedKeys().contains(EXTERNAL));
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean success = false;
		try {
			String metaId = UUID.randomUUID().toString();
			MailerResult result = new MailerResult();
			MailContext ctxt = new MailContextImpl(entry);
			String subject = subjectEl.getValue();
			String body = bodyEl.getValue();
			
			List<Identity> recipients = getRecipients();
			if(!recipients.isEmpty()) {
				sendToIdentities(recipients, subject, body, ctxt, metaId, result);
			}
			List<String> externalEmails = getExternalEmails();
			if(!externalEmails.isEmpty()) {
				sendToExternalEmails(externalEmails, subject, body, ctxt, metaId, result);
			}
			
			if(copyFromEl.isAtLeastSelected(1)) {
				sendToCopy(subject, body, ctxt, metaId, result);
			}
			
			success = result.isSuccessful();
			if (success) {
				if(recipients.isEmpty() && externalEmails.isEmpty() && !copyFromEl.isAtLeastSelected(1)) {
					showWarning("no.recipients");
				} else {
					showInfo("msg.send.ok");
					// do logging
					ThreadLocalUserActivityLogger.log(MailLoggingAction.MAIL_SENT, getClass());
				}
				fireEvent(ureq, Event.DONE_EVENT);
			} else {
				Roles roles = ureq.getUserSession().getRoles();
				boolean admin = roles.isAdministrator() || roles.isSystemAdmin();
				MailHelper.printErrorsAndWarnings(result, getWindowControl(), admin, getLocale());
				fireEvent(ureq, Event.FAILED_EVENT);
			}
		} catch (Exception e) {
			logError("", e);
			showWarning("error.msg.send.nok");
		}
	}
	
	private void sendToCopy(String subject, String body,
			MailContext ctxt, String metaId, MailerResult result) {
		MailerResult tmpResult = new MailerResult();
		MailTemplate template = new CPMailTemplate(subject, body);
		MailBundle bundle = mailService.makeMailBundle(ctxt, getIdentity(), template, getIdentity(), metaId, tmpResult);
		result.append(tmpResult);
		MailerResult sendResult = mailService.sendMessage(bundle);
		result.append(sendResult);
	}
	
	private void sendToExternalEmails(List<String> externalEmails, String subject, String body,
			MailContext ctxt, String metaId, MailerResult result) {
		
		List<MailBundle> bundles = new ArrayList<>();
		for(String externalEmail:externalEmails) {
			MailerResult tmpResult = new MailerResult();
			MailTemplate template = new CPMailTemplate(subject, body, externalEmail);
			MailBundle bundle = mailService.makeMailBundle(ctxt, externalEmail, template, getIdentity(), metaId, tmpResult);
			bundles.add(bundle);
			result.append(tmpResult);
		}
		
		MailerResult firstResults = mailService.sendMessage(bundles.toArray(new MailBundle[bundles.size()]));
		result.append(firstResults);
	}
	
	private void sendToIdentities(List<Identity> recipients, String subject, String body,
			MailContext ctxt, String metaId, MailerResult result) {

		List<MailBundle> bundles = new ArrayList<>();
		for(Identity recipient:recipients) {
			MailerResult tmpResult = new MailerResult();
			MailTemplate template = new CPMailTemplate(subject, body);
			MailBundle bundle = mailService.makeMailBundle(ctxt, recipient, template, getIdentity(), metaId, tmpResult);
			bundles.add(bundle);
			result.append(tmpResult);
		}
		
		MailerResult firstResults = mailService.sendMessage(bundles.toArray(new MailBundle[bundles.size()]));
		result.append(firstResults);
	}
	
	private List<Identity> getRecipients() {
		List<String> roles = new ArrayList<>(recipientsEl.getSelectedKeys());
		roles.remove(EXTERNAL);
		if(!roles.isEmpty()) {
			List<RepositoryEntry> refs = referenceManager.getRepositoryReferencesTo(entry.getOlatResource());	
			return repositoryService.getMembers(refs, RepositoryEntryRelationType.all, roles.toArray(new String[roles.size()]));
		}
		return List.of();
	}
	
	private List<String> getExternalEmails() {
		List<String> externalEmails = new ArrayList<>();
		if(recipientsEl.getSelectedKeys().contains(EXTERNAL)) {
			String value = externalAddressesEl.getValue();
			if(StringHelper.containsNonWhitespace(value)) {
				for(StringTokenizer tokenizer= new StringTokenizer(value, ",\r\n", false); tokenizer.hasMoreTokens(); ) {
					String email = tokenizer.nextToken().trim();
					externalEmails.add(email);
				}
			}
		}
		return externalEmails;
	}
	
	private String[] getArguments(UserRequest ureq) {
		String date = Formatter.getInstance(getLocale()).formatDate(ureq.getRequestTimestamp());
		User from = getIdentity().getUser();
		String coursesList = getCoursesList();
		
		return new String[] {
			entry.getDisplayname(),	// 0 cpname
			date,
			from.getFirstName(),
			from.getLastName(),
			from.getEmail(),
			userManager.getUserDisplayName(getIdentity()),
			coursesList
		};
	}
	
	private String getCoursesList() {
		List<RepositoryEntry> refs = referenceManager.getRepositoryReferencesTo(entry.getOlatResource());
		StringBuilder sb = new StringBuilder(1024);
		if(!refs.isEmpty()) {
			sb.append("<ul>");
			for(RepositoryEntry ref:refs) {
				List<ContextEntry> entries = BusinessControlFactory.getInstance().createCEListFromString(ref);
				String url = BusinessControlFactory.getInstance().getAsURIString(entries, false);
				sb.append("<li><a href='").append(url).append("'>")
				  .append(StringHelper.escapeHtml(ref.getDisplayname())).append("</a></li>");
			}
			sb.append("</ul>");
		}
		return sb.toString();
	}
	
	private class CPMailTemplate extends MailTemplate {
		
		private final String externalEmail;
		
		public CPMailTemplate(String subject, String body) {
			super(subject, body, null);
			externalEmail = null;
		}
		
		public CPMailTemplate(String subject, String body, String externalEmail) {
			super(subject, body, null);
			this.externalEmail = externalEmail;
		}

		@Override
		public void putVariablesInMailContext(VelocityContext vContext, Identity recipient) {
			vContext.put("cpname", entry.getDisplayname());
			
			if(recipient != null) {
				User user = recipient.getUser();
				vContext.put("firstName", user.getFirstName());
				vContext.put("lastName", user.getLastName());
			} else if(StringHelper.containsNonWhitespace(externalEmail)) {
				vContext.put("firstName", "");
				vContext.put("lastName", externalEmail);
			}
		}
	}
}
