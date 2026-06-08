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
package org.olat.modules.selectus.ui.rejection;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.ExternalLinkItem;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailerResult;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.Attachment;
import org.olat.modules.selectus.model.RejectionEmailLog;
import org.olat.modules.selectus.model.RejectionEmailLogFull;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.components.LogStatusCellRenderer;
import org.olat.modules.selectus.ui.resources.AttachmentMediaResource;

/**
 * The contact form is filled with the subject and content of the
 * mail in log. If they are empty, the template is used as a fallback.
 * 
 * Initial date: 22.09.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionViewEmailLogController extends FormBasicController {

	private final RejectionEmailLogFull log;
	
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	
	/**
	 * 
	 * @param ureq The user request
	 * @param wControl The window control
	 * @param log The precedent log of the email
	 * @param template The template fallback
	 */
	public PositionViewEmailLogController(UserRequest ureq, WindowControl wControl, RejectionEmailLog log) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(PositionController.class, getLocale(), getTranslator()));
		this.log = recruitingService.getFullLog(log);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String status = LogStatusCellRenderer.format(log.getStatus(), getTranslator());
		if(log.getStatus() == MailerResult.OK) {
			status += " <i class='o_icon o_icon_correct'> </i>";
		} else {
			status += " <i class='o_icon o_icon_error'> </i>";
		}
		uifactory.addStaticTextElement("status", "edit.log.status", status, formLayout);

		String subject = log.getMailSubject();
		uifactory.addStaticTextElement("subject", "mailtemplateform.subject", subject, formLayout);
		String body = log.getMailContent();
		if(StringHelper.containsNonWhitespace(body) && !StringHelper.isHtml(body)) {
			body = Formatter.escWithBR(body).toString();
		}
		uifactory.addStaticTextElement("body", "mailtemplateform.body", body, formLayout);
		
		Attachment attachment = log.getLetter();
		if(recruitingModule.isMailLetterEnabled() && attachment != null) {
			String mapperUri = registerCacheableMapper(ureq, null, new AttachmentMapper());
			String url = mapperUri + "/" + attachment.getName();
			ExternalLinkItem link = uifactory.addExternalLink("letter", url, "_blank", formLayout);
			link.setName(attachment.getName());
			link.setIconLeftCSS("o_icon o_filetype_pdf");
		}

		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("ok", formLayout);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private class AttachmentMapper implements Mapper {
		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			Attachment attachment = log.getLetter();
			if(attachment == null) {
				return new NotFoundMediaResource();
			}
			return new AttachmentMediaResource(attachment);
		}
	}
}