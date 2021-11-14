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
package org.olat.group.ui.wizard;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.mail.MailTemplate;

/**
 * 
 * Description:<br>
 * The MailNotificationEditController allows the user to enter a mailtext based
 * on the MailTemplate. It will be surrounded by some comments which variables
 * that can be used in this context.
 * <p>
 * Events:
 * <ul>
 * <li>Event.DONE_EVENT</li>
 * <li>Event.CANCEL_EVENT</li>
 * </ul>
 * <p>
 * Initial Date: 23.11.2006 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH<br>
 *         http://www.frentix.com
 */
public class BGMailNotificationEditController extends BasicController {
	
	// components
	private VelocityContainer mainVC;
	private BGMailTemplateController mailForm;
	
	// data model
	private MailTemplate mailTemplate;
	private String orgMailSubject;
	private String orgMailBody;
	private Boolean cpFrom;

	/**
	 * Constructor for the email 
	 * @param wControl
	 * @param ureq
	 * @param mailTemplate
	 * @param useCancel
	 */
	public BGMailNotificationEditController(WindowControl wControl, UserRequest ureq, MailTemplate mailTemplate,
			boolean ccSenderAllowed, boolean customizingAvailable, boolean useCancel, boolean mandatory) {
		super(ureq, wControl);
		this.mailTemplate = mailTemplate;
		orgMailSubject = mailTemplate.getSubjectTemplate();
		orgMailBody = mailTemplate.getBodyTemplate();
		cpFrom = mailTemplate.getCpfrom();
		
		mainVC = createVelocityContainer("mailnotification");
		mailForm = new BGMailTemplateController(ureq, wControl, mailTemplate,
				ccSenderAllowed, customizingAvailable, useCancel, mandatory);
		listenTo(mailForm);
		
		mainVC.put("mailForm", mailForm.getInitialComponent());
		
		putInitialPanel (mainVC);
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == mailForm) {
			if (event == Event.DONE_EVENT) {
				mailForm.updateTemplateFromForm(mailTemplate);
				fireEvent(ureq, event);
			} else if (event == Event.CANCELLED_EVENT) {
				mailTemplate = null;
				fireEvent(ureq, event);
			}
		}
	}
	
	public boolean isSendMail() {
		return mailForm.sendMailSwitchEnabled();
	}

	/**
	 * @return The mail template containing the configured mail or null if user
	 *         decided to not send a mail
	 */
	public MailTemplate getMailTemplate() {
		if (mailForm.sendMailSwitchEnabled()) {
			return mailTemplate;
		} else {
			return null;
		}
	}
	
	/**
	 * Return the current template, always
	 * @return
	 */
	public MailTemplate getTemplate() {
		return mailTemplate;
	}
	
	/**
	 * 
	 * @return Boolean
	 */
	public boolean isTemplateChanged() {
		return !orgMailSubject.equals(mailTemplate.getSubjectTemplate()) 
					|| !orgMailBody.equals(mailTemplate.getBodyTemplate())
					|| !cpFrom.equals(mailTemplate.getCpfrom());
	}
}
