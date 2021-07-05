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
package org.olat.modules.lecture.ui.coach;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailLoggingAction;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;
import org.olat.modules.co.ContactForm;
import org.olat.modules.lecture.ui.LectureRepositoryAdminController;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ContactTeachersController extends FormBasicController {
	
	private TextElement subjectEl;
	private RichTextElement bodyEl;
	private MultipleSelectionElement teachersEl;
	
	private final boolean withButtons;
	private List<Identity> identities;

	@Autowired
	private MailManager mailService;
	@Autowired
	private UserManager userManager;
	
	public ContactTeachersController(UserRequest ureq, WindowControl wControl, List<Identity> identities) {
		super(ureq, wControl, Util.createPackageTranslator(LectureRepositoryAdminController.class, ureq.getLocale()));
		setTranslator(Util.createPackageTranslator(ContactForm.class, getLocale(), getTranslator()));
		this.identities = identities;
		withButtons = true;
		
		initForm(ureq);
	}
	
	public ContactTeachersController(UserRequest ureq, WindowControl wControl, List<Identity> identities, Form rootForm) {
		super(ureq, wControl, LAYOUT_DEFAULT, "", rootForm);	
		setTranslator(Util.createPackageTranslator(LectureRepositoryAdminController.class, ureq.getLocale()));
		setTranslator(Util.createPackageTranslator(ContactForm.class, getLocale(), getTranslator()));
		this.identities = identities;
		withButtons = false;
		
		initForm(ureq);
	}
	
	public List<Identity> getSelectedTeacher() {
		List<Identity> teachers = new ArrayList<>();
		Collection<String> selectedTeacherKeys = teachersEl.getSelectedKeys();
		for(Identity identity:identities) {
			if(selectedTeacherKeys.contains(identity.getKey().toString())) {
				teachers.add(identity);
			}
		}
		return teachers;
	}
	
	public String getSubject() {
		return subjectEl.getValue();
	}
	
	public String getBody() {
		return bodyEl.getValue();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		SelectionValues keyValues = new SelectionValues();
		for(Identity identity:identities) {
			String key = identity.getKey().toString();
			String fullName = userManager.getUserDisplayName(identity);
			keyValues.add(SelectionValues.entry(key, fullName));
		}
		teachersEl = uifactory.addCheckboxesVertical("contact.teachers", "contact.teachers", formLayout, keyValues.keys(), keyValues.values(), 1);
		teachersEl.selectAll();
		
		subjectEl = uifactory.addTextElement("subject", "mail.subject", 255, "", formLayout);
		subjectEl.setElementCssClass("o_sel_mail_subject");
		subjectEl.setDisplaySize(255);
		subjectEl.setMandatory(true);
		bodyEl = uifactory.addRichTextElementForStringDataMinimalistic("body", "mail.body", "", 15, 8, formLayout, getWindowControl());
		bodyEl.setElementCssClass("o_sel_mail_body");
		bodyEl.setMandatory(true);
		bodyEl.getEditorConfiguration().setRelativeUrls(false);
		bodyEl.getEditorConfiguration().setRemoveScriptHost(false);
		
		if(withButtons) {
			FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
			formLayout.add(buttonsCont);
			uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
			uifactory.addFormSubmitButton("send", buttonsCont);
		}
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	public boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		teachersEl.clearError();
		if(withButtons && !teachersEl.isAtLeastSelected(1)) {
			teachersEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		subjectEl.clearError();
		if(!StringHelper.containsNonWhitespace(subjectEl.getValue())
				&& (withButtons || teachersEl.isAtLeastSelected(1))) {
			subjectEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		bodyEl.clearError();
		if(!StringHelper.containsNonWhitespace(bodyEl.getValue())
				&& (withButtons || teachersEl.isAtLeastSelected(1))) {
			bodyEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}

		return allOk;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(withButtons) {
			doSend(ureq);
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void doSend(UserRequest ureq) {
		boolean success = false;
		try {
			ContactList teachersList = new ContactList(translate("contact.teachers.list.name"));
			List<Identity> selectedTeachers = getSelectedTeacher();
			teachersList.addAllIdentites(selectedTeachers);
			MailContext context = new MailContextImpl(getWindowControl().getBusinessControl().getAsString());
			MailBundle bundle = new MailBundle();
			bundle.setContext(context);
			bundle.setFromId(getIdentity());						
			bundle.setContactList(teachersList);
			bundle.setContent(subjectEl.getValue(), bodyEl.getValue());
			MailerResult result = mailService.sendMessage(bundle);
			success = result.isSuccessful();
			if (success) {
				showInfo("msg.send.ok");
				// do logging
				ThreadLocalUserActivityLogger.log(MailLoggingAction.MAIL_SENT, getClass());
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
}
