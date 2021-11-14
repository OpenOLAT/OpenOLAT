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
package org.olat.modules.library.ui;

import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.AssertException;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSLeaf;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * <h3>Description:</h3>
 * <p>
 * A controller to reject a document in the library. It takes the reasons for the
 * rejection and send an email to the submitter of the document.
 * <p>
 * Events fired:
 * <ul>
 *   <li>DONE_EVENT</li>
 *   <li>CANCELLED_EVENT</li>
 * </ul>
 * <P>
 * Initial Date:  Sep 24, 2009 <br>
 * @author twuersch, timo.wuersch@frentix.com, www.frentix.com
 */
public class RejectController extends FormBasicController {
	
	private TextElement subjectElement;
	private TextElement messageElement;
	private final VFSMetadata metaInfo;
	private final VFSLeaf file;
	
	@Autowired
	private MailManager mailManager;

	public RejectController(UserRequest ureq, WindowControl wControl, VFSLeaf file) {
		super (ureq, wControl);
		this.file = file;
		if(file.canMeta() == VFSConstants.YES) {
			metaInfo = file.getMetaInfo();
		} else {
			throw new AssertException("Only file mith meta info are accepted");
		}
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String to = getIdentityName(metaInfo.getFileInitializedBy());
		String from = getIdentityName(ureq.getIdentity());
		StaticTextElement toElement = uifactory.addStaticTextElement("reject.message.to", to, formLayout);
		StaticTextElement fromElement = uifactory.addStaticTextElement("reject.message.from", from, formLayout);
		toElement.setLabel("reject.message.to", null);
		fromElement.setLabel("reject.message.from", null);
		
		String[] args = new String[]{file.getName()};
		String defSubject = getTranslator().translate("reject.message.default.subject", args);
		String defMessage = getTranslator().translate("reject.message.default.message", args);
		subjectElement = uifactory.addTextElement("reject.message.subject", "reject.message.subject", -1, defSubject, formLayout);
		messageElement = uifactory.addTextAreaElement("reject.message", "reject.message", -1, 5, 60, true, false, defMessage,  formLayout);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("reject.form.submit", buttonLayout);
		uifactory.addFormCancelButton("reject.form.cancel", buttonLayout, ureq, getWindowControl());
	}
	
	private String getIdentityName(Identity identity) {
		if(identity == null) {
			return "-";
		}
		
		User user = identity.getUser();
		return user.getProperty(UserConstants.LASTNAME, getLocale()) + " " + user.getProperty(UserConstants.FIRSTNAME, getLocale());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean isInputValid = true;
		String subject = subjectElement.getValue();
		if(!StringHelper.containsNonWhitespace(subject)) {
			subjectElement.setErrorKey("error.mail.subject.empty", null);
			isInputValid = false;
		}
		String message = messageElement.getValue();
		if(!StringHelper.containsNonWhitespace(message)) {
			messageElement.setErrorKey("error.mail.message.empty", null);
			isInputValid = false;
		}
		return isInputValid;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//send notification e-mail
		Identity uploaderIdentity = metaInfo.getFileInitializedBy();
		try {
			if(uploaderIdentity != null) {
				String mailto = uploaderIdentity.getUser().getProperty(UserConstants.EMAIL, getLocale());
				MailBundle bundle = new MailBundle();
				bundle.setFrom(WebappHelper.getMailConfig("mailReplyTo"));
				bundle.setTo(mailto);
				bundle.setContent(subjectElement.getValue(), messageElement.getValue());
				mailManager.sendMessage(bundle);
			}
			file.delete();
			fireEvent(ureq, Event.DONE_EVENT);
		} catch (Exception e) {
			logError("Cannot send a email to: " + uploaderIdentity, e);
			fireEvent(ureq, Event.FAILED_EVENT);
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
