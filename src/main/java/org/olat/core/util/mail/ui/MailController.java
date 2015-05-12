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

package org.olat.core.util.mail.ui;

import java.text.DateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.filter.impl.OWASPAntiSamyXSSFilter;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailModule;
import org.olat.core.util.mail.model.DBMail;
import org.olat.core.util.mail.model.DBMailAttachment;
import org.olat.core.util.mail.model.DBMailRecipient;
import org.olat.user.UserManager;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  24 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MailController extends FormBasicController {
	
	private FormLink backLink;
	
	private String mapperBaseURI;
	private final boolean back;
	private final DBMail mail;
	private final List<DBMailAttachment> attachments;
	private final MailManager mailManager;
	private final UserManager userManager;
	
	public MailController(UserRequest ureq, WindowControl wControl, DBMail mail, boolean back) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		setTranslator(Util.createPackageTranslator(MailModule.class, ureq.getLocale()));
		this.mail = mail;
		this.back = back;
		mailManager = CoreSpringFactory.getImpl(MailManager.class);
		userManager = CoreSpringFactory.getImpl(UserManager.class);
		attachments = mailManager.getAttachments(mail);
		if(!attachments.isEmpty()) {
			mapperBaseURI = registerMapper(ureq, new MailAttachmentMapper(mailManager));
		}
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer mainLayout, Controller listener, UserRequest ureq) {
		setTranslator(Util.createPackageTranslator(MailModule.class, ureq.getLocale()));
		String page = Util.getPackageVelocityRoot(MailModule.class) + "/mail.html";
		FormLayoutContainer vcLayout = FormLayoutContainer.createCustomFormLayout("wrapper", getTranslator(), page);
		vcLayout.setRootForm(mainForm);
		mainLayout.add(vcLayout);
		
		if(back) {
			backLink = uifactory.addFormLink("back", vcLayout, Link.LINK_BACK);
			vcLayout.add("back", backLink);
		}

		FormLayoutContainer formLayout = FormLayoutContainer.createDefaultFormLayout("mainCmp", getTranslator());
		formLayout.setRootForm(mainForm);
		vcLayout.add("mainCmp", formLayout);
		
		String subject = StringHelper.escapeHtml(mail.getSubject());
		uifactory.addStaticTextElement("subject", "mail.subject", subject, formLayout);		
		
		String from = StringHelper.escapeHtml(getFullName(mail.getFrom()));
		uifactory.addStaticTextElement("from", "mail.from", from, formLayout);
		
		String recipients = getRecipients();
		uifactory.addStaticTextElement("recipients", "mail.recipients", recipients, formLayout);

		String date = DateFormat.getDateInstance(DateFormat.MEDIUM, getLocale()).format(mail.getCreationDate());
		uifactory.addStaticTextElement("date", "mail.sendDate", date, formLayout);
		
		uifactory.addSpacerElement("spacer2", formLayout, false);
		String formattedBody = formattedBody();
		uifactory.addStaticTextElement("body", "mail.body", formattedBody, formLayout);
		
		if(!attachments.isEmpty()) {
			uifactory.addSpacerElement("spacer3", formLayout, false);
			String attachmentsPage = Util.getPackageVelocityRoot(MailModule.class) + "/attachments.html";
			FormLayoutContainer container = FormLayoutContainer.createCustomFormLayout("attachments", getTranslator(), attachmentsPage);
			container.setLabel("mail.attachments", null);
			container.setRootForm(mainForm);
			container.contextPut("attachments", attachments);
			container.contextPut("mapperBaseURI", mapperBaseURI);
			formLayout.add(container);
		}
	}
	
	private String getRecipients() {
		StringBuilder sb = new StringBuilder();
		Set<String> groups = new HashSet<String>();
		for(DBMailRecipient recipient:mail.getRecipients()) {
			if(recipient == null) continue;
			String group = recipient.getGroup();
			if(StringHelper.containsNonWhitespace(group) && !groups.contains(group)) {
				if(sb.length() > 0) sb.append(", ");
				sb.append(group);
				groups.add(group);
			}	
		}
		return sb.toString();
	}
	
	private String getFullName(DBMailRecipient recipient) {
		if(recipient == null) return "";
		return userManager.getUserDisplayName(recipient.getRecipient());
	}
	
	private String formattedBody() {
		String body = mail.getBody();
		String formattedBody;
		if(!StringHelper.containsNonWhitespace(body)) {
			formattedBody = "";
		} else if(StringHelper.isHtml(body)) {
			//html -> don't replace
			formattedBody = body;
		} else {
			//if windows
			formattedBody = body.replace("\n\r", "<br />").replace("\n", "<br />");
		}
		return new OWASPAntiSamyXSSFilter().filter(formattedBody);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == backLink) {
			fireEvent(ureq, Event.BACK_EVENT);
		} else {
			super.formInnerEvent(ureq, source, event);
		}
	}
}
