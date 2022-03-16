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
package org.olat.modules.message.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.message.AssessmentMessageService;
import org.olat.modules.message.model.AssessmentMessageWithReadFlag;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 15 mars 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentMessageDisplayCalloutController extends FormBasicController {
	
	private FormLink closeButton;
	private FormLink markAllAsReadButton;
	
	private final RepositoryEntry entry;
	private final String resSubPath;
	private List<AssessmentMessageDisplayRow> messagesList;

	@Autowired
	private AssessmentMessageService assessmentMessageService;
	
	public AssessmentMessageDisplayCalloutController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, String resSubPath) {
		super(ureq, wControl, "messages_callout");
		this.entry = entry;
		this.resSubPath = resSubPath;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		List<AssessmentMessageWithReadFlag> messages = assessmentMessageService
				.getMessagesFor(entry, resSubPath, getIdentity(), ureq.getRequestTimestamp());
		messagesList = new ArrayList<>(messages.size());
		boolean hasNotRead = false;
		for(AssessmentMessageWithReadFlag message:messages) {
			hasNotRead |= !message.isRead();
			messagesList.add(new AssessmentMessageDisplayRow(message.getMessage(), message.isRead(), null));
		}
		flc.contextPut("messages", messagesList);
		
		closeButton = uifactory.addFormLink("close", formLayout, Link.BUTTON);
		closeButton.setVisible(!hasNotRead);
		markAllAsReadButton = uifactory.addFormLink("mark.all.as.read", formLayout, Link.BUTTON);
		markAllAsReadButton.setVisible(hasNotRead);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(closeButton == source) {
			fireEvent(ureq, Event.CLOSE_EVENT);
		} else if(markAllAsReadButton == source) {
			doMarkAllAsRead();
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doMarkAllAsRead() {
		for(AssessmentMessageDisplayRow message:messagesList) {
			if(!message.isRead()) {
				assessmentMessageService.markAsRead(message.getMessage(), getIdentity());
			}
		}
	}
}
