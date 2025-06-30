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
package org.olat.modules.lecture.ui;

import java.net.URL;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 30 juin 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class EditOnlineMeetingURLController extends FormBasicController {
	
	private TextElement onlineMeetingProviderUrlEl;
	private TextElement onlineMeetingProviderNameEl;
	
	private LectureBlock lectureBlock;
	
	@Autowired
	private LectureService lectureService;
	
	public EditOnlineMeetingURLController(UserRequest ureq, WindowControl wControl, LectureBlock lectureBlock) {
		super(ureq, wControl);
		this.lectureBlock = lectureBlock;
		
		initForm(ureq);
	}
	
	public LectureBlock getLectureBlock() {
		return lectureBlock;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		onlineMeetingProviderNameEl = uifactory.addTextElement("lecture.online.meeting.provider.name", 32, "Zoom", formLayout);
		onlineMeetingProviderNameEl.setMandatory(true);
		onlineMeetingProviderUrlEl = uifactory.addTextElement("lecture.online.meeting.provider.url", 256, null, formLayout);
		onlineMeetingProviderUrlEl.setMandatory(true);
		
		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		onlineMeetingProviderNameEl.clearError();
		if(!StringHelper.containsNonWhitespace(onlineMeetingProviderNameEl.getValue())) {
			onlineMeetingProviderNameEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		onlineMeetingProviderUrlEl.clearError();
			
		if (!StringHelper.containsNonWhitespace(onlineMeetingProviderUrlEl.getValue())) {
			onlineMeetingProviderUrlEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else {
			allOk &= validateUrl(onlineMeetingProviderUrlEl);
		}
		
		return allOk;
	}
	
	private boolean validateUrl(TextElement textEl) {
		boolean allOk = true;

		if (StringHelper.containsNonWhitespace(textEl.getValue())) {
			try {
				new URL(textEl.getValue()).toURI();
			} catch(Exception e) {
				textEl.setErrorKey("error.url.not.valid");
				allOk &= false;
			}
		}

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		lectureBlock.setMeetingTitle(onlineMeetingProviderNameEl.getValue());
		lectureBlock.setMeetingUrl(onlineMeetingProviderUrlEl.getValue());
		lectureBlock = lectureService.save(lectureBlock, null);
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
