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
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.filter.impl.OWASPAntiSamyXSSFilter;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailModule;
import org.olat.core.util.mail.model.DBMail;
import org.olat.core.util.mail.model.DBMailAttachment;
import org.olat.core.util.mail.model.DBMailRecipient;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  24 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MailController extends FormBasicController {
	
	private FormLink backLink, showAllRecipientsLink;
	
	private String mapperBaseURI;
	private final boolean back;
	private final boolean outbox;
	private final DBMail mail;
	private final List<DBMailAttachment> attachments;
	private boolean showAllRecipients = false;
	private int maxRecipients = 10;
	private FormLayoutContainer vcLayout;
	
	@Autowired
	private MailModule mailModule;
	@Autowired
	private MailManager mailManager;

	public MailController(UserRequest ureq, WindowControl wControl, DBMail mail, boolean back, boolean outbox) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		setTranslator(Util.createPackageTranslator(MailModule.class, ureq.getLocale()));
		this.mail = mail;
		this.back = back;
		this.outbox = outbox;
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
		vcLayout = FormLayoutContainer.createCustomFormLayout("wrapper", getTranslator(), page);
		vcLayout.setRootForm(mainForm);
		mainLayout.add(vcLayout);
		
		if(back) {
			backLink = uifactory.addFormLink("back", vcLayout, Link.LINK_BACK);
			vcLayout.add("back", backLink);
		}

		if (mail.getRecipients().size() > maxRecipients) {
			showAllRecipientsLink = uifactory.addFormLink("recipients.all", vcLayout, Link.LINK_CUSTOM_CSS);
			showAllRecipientsLink.setElementCssClass("o_showAllLink");
			vcLayout.add("showAllRecipients", showAllRecipientsLink);			
		}
		
		String subject = StringHelper.escapeHtml(mail.getSubject());
		vcLayout.contextPut("subject", subject);		
		
		String from = getFrom();
		vcLayout.contextPut("from", from);
		
		String recipients = getRecipients();
		vcLayout.contextPut("recipients", recipients);

		String date = DateFormat.getDateInstance(DateFormat.MEDIUM, getLocale()).format(mail.getCreationDate());
		vcLayout.contextPut("date", date);

		String formattedBody = formattedBody();
		vcLayout.contextPut("body", formattedBody);
		
		if(!attachments.isEmpty()) {
			String attachmentsPage = Util.getPackageVelocityRoot(MailModule.class) + "/attachments.html";
			FormLayoutContainer container = FormLayoutContainer.createCustomFormLayout("attachments", getTranslator(), attachmentsPage);
			container.setLabel("mail.attachments", null);
			container.setRootForm(mainForm);
			container.contextPut("attachments", attachments);
			container.contextPut("mapperBaseURI", mapperBaseURI);
			vcLayout.add(container);
		}
	}
	
	private String getFrom() {
		StringBuilder sb = new StringBuilder();
		DBMailRecipient from = mail.getFrom();
		sb.append("<ul class='list-inline'><li>");
		if (from != null) {
			sb.append(getFullName(from));
			if (showMailAdresses()) {
				Identity fromIdentity = from.getRecipient();
				if (fromIdentity != null) {
					sb.append(" &lt;");
					sb.append(UserManager.getInstance().getUserDisplayEmail(fromIdentity, getLocale()));
					sb.append("&gt; ");
				}
			}			
		}
		sb.append("</li></ul>");
		return sb.toString();
	}
	
	private String getRecipients() {
		StringBuilder sb = new StringBuilder();
		Set<String> groups = new HashSet<>();
		int recipientsCounter = 0;
		int groupCounter = 0;
		sb.append("<ul class='list-inline'>");
		for(DBMailRecipient recipient:mail.getRecipients()) {
			if(recipient == null) continue;
			if (recipientsCounter >= maxRecipients && !showAllRecipients) {
				sb.append("<li class='o_more'>").append(translate("recipients.more", (mail.getRecipients().size() - recipientsCounter) + "")).append("<span>");
				break;
			}
			recipientsCounter++;
			String group = recipient.getGroup();			
			if(StringHelper.containsNonWhitespace(group) && !groups.contains(group)) {
				// recipient is the entire group
				if(sb.length() > 0) {
					sb.append("</ul>");
					sb.append("<ul class='list-inline'>");
				}
				sb.append("<li class='o_group'><i class='o_icon o_icon_group o_icon-fw'> </i><span>");
				sb.append(group);
				sb.append("</span></li>");
				groups.add(group);
				groupCounter = 0;
			}
			if (showRecipientNames()) {
				if (recipient.getRecipient() != null) {
					sb.append("<li class='o_recipient'>");
					if(groupCounter> 0) sb.append(", ");
					sb.append("<span>").append(getFullName(recipient)).append("</span>");
					if (showMailAdresses()) {
						sb.append(" &lt;");
						sb.append(UserManager.getInstance().getUserDisplayEmail(recipient.getRecipient(), getLocale()));
						sb.append("&gt;");
					}
					sb.append("</li>");
					groupCounter++;
				}
			}
			if (showMailAdresses()) {
				if (recipient.getEmailAddress() != null) {
					// recipient is not an OpenOLAT identity but an external email
					sb.append("<li class='o_mail'>");
					if(groupCounter > 0) sb.append(", ");
					sb.append("&lt;");
					sb.append(UserManager.getInstance().getUserDisplayEmail(recipient.getEmailAddress(), getLocale()));
					sb.append("&gt;</li>");
					groupCounter++;
				}
			}
		}
		sb.append("</ul>");
		return sb.toString();
	}

	private boolean showRecipientNames() {
		return (outbox && mailModule.isShowOutboxRecipientNames()) 
				|| (!outbox && mailModule.isShowInboxRecipientNames());
	}

	private boolean showMailAdresses() {
		return (outbox && mailModule.isShowOutboxMailAddresses()) 
				|| (!outbox && mailModule.isShowInboxMailAddresses());
	}
	
	private String getFullName(DBMailRecipient recipient) {
		if(recipient == null || recipient.getRecipient() == null) return "";
		// dont't use the standard user display name formatter as this one adds
		// a comma. The comma is used already to separate the users in the list
		// of recipients
		User user = recipient.getRecipient().getUser();
		return StringHelper.escapeHtml(user.getFirstName() + " " + user.getLastName());
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
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == backLink) {
			fireEvent(ureq, Event.BACK_EVENT);
		} else if (source == showAllRecipientsLink) {
			if (showAllRecipients) {
				showAllRecipientsLink.setI18nKey("recipients.all");								
			} else {
				showAllRecipientsLink.setI18nKey("recipients.hide");				
			}
			showAllRecipients = !showAllRecipients;
			// update list of recipients
			String recipients = getRecipients();
			vcLayout.contextPut("recipients", recipients);
		} else {
			super.formInnerEvent(ureq, source, event);
		}
	}
}
