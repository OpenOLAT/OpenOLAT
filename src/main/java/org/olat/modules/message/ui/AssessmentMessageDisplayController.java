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
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingDeque;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.util.SyntheticUserRequest;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.coordinate.Coordinator;
import org.olat.core.util.event.GenericEventListener;
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
public class AssessmentMessageDisplayController extends FormBasicController implements GenericEventListener {
	
	private int count = 0;
	private final RepositoryEntry entry;
	private final String resSubPath;
	private final Deque<AssessmentMessageDisplayRow> messagesList = new LinkedBlockingDeque<>();
	private List<AssessmentMessageWithReadFlag> allMessagesList;
	
	private final OLATResourceable messageOres;
	
	@Autowired
	private Coordinator coordinator;
	@Autowired
	private AssessmentMessageService assessmentMessageService;
	
	public AssessmentMessageDisplayController(UserRequest ureq, WindowControl wControl, Form form, RepositoryEntry entry, String resSubPath) {
		super(ureq, wControl, LAYOUT_CUSTOM, "messages_run", form);
		this.resSubPath = resSubPath;
		this.entry = entry;
		initForm(ureq);
		loadMessages(ureq);
		
		messageOres = assessmentMessageService.getEventResourceable(entry, resSubPath);
		coordinator.getEventBus()
			.registerFor(this, getIdentity(), messageOres);
	}

	@Override
	protected void doDispose() {
		coordinator.getEventBus()
			.deregisterFor(this, messageOres);
		super.doDispose();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//
	}
	
	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent fe) {
		//
	}
	
	public boolean hasMessages() {
		return allMessagesList != null && !allMessagesList.isEmpty();
	}

	public void loadMessages(UserRequest ureq) {
		messagesList.clear();
		
		List<AssessmentMessageWithReadFlag> messages = assessmentMessageService
				.getMessagesFor(entry, resSubPath, getIdentity(), ureq.getRequestTimestamp());
		allMessagesList = messages;
		for(AssessmentMessageWithReadFlag message:messages) {
			if(!message.isRead()) {
				FormLink readButton = uifactory.addFormLink("read-" + (++count), "read", "mark.as.read", null, flc, Link.BUTTON_SMALL);
				readButton.setIconLeftCSS("o_icon o_icon_submit");
				readButton.setPrimary(true);
				AssessmentMessageDisplayRow row = new AssessmentMessageDisplayRow(message.getMessage(), message.isRead(), readButton);
				readButton.setUserObject(row);
				messagesList.add(row);
			}
		}
		
		flc.contextPut("messages", messagesList);
		flc.setDirty(true);
	}
	
	public void removeMessage(Long messageKey) {
		List<AssessmentMessageDisplayRow> tmpMessagesRows = new ArrayList<>(messagesList);
		for(AssessmentMessageDisplayRow row:tmpMessagesRows) {
			if(row.getMessage().getKey().equals(messageKey)) {
				messagesList.remove(row);
			}
		}
		
		List<AssessmentMessageWithReadFlag> tmpMessages = new ArrayList<>(allMessagesList);
		for(AssessmentMessageWithReadFlag message:tmpMessages) {
			if(message.getMessage().getKey().equals(messageKey)) {
				allMessagesList.remove(message);
			}
		}
		
		flc.contextPut("messages", messagesList);
		flc.setDirty(true);
	}

	@Override
	public void event(Event event) {
		if(event instanceof AssessmentMessageEvent) {
			processAssessmentMessageEvent((AssessmentMessageEvent)event);
		}
	}
	
	private void processAssessmentMessageEvent(AssessmentMessageEvent event) {
		if(!Objects.equals(entry.getKey(), event.getRepositoryEntryKey()) || !Objects.equals(resSubPath, event.getResSubPath())
				|| Objects.equals(getIdentity().getKey(), event.getEmitter())) {
			return;
		}
		
		if(AssessmentMessageEvent.PUBLISHED.equals(event.getCommand()) && !isInList(event.getMessageKey())) {
			int messageSize = messagesList.size();
			SyntheticUserRequest ureq = new SyntheticUserRequest(getIdentity(), getLocale());
			loadMessages(ureq);
			if(messagesList.size() != messageSize) {
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		} else if((AssessmentMessageEvent.DELETED.equals(event.getCommand()) || AssessmentMessageEvent.EXPIRED.equals(event.getCommand()))
				&& isInList(event.getMessageKey())) {
			SyntheticUserRequest ureq = new SyntheticUserRequest(getIdentity(), getLocale());
			removeMessage(event.getMessageKey());
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
	}
	
	private boolean isInList(Long messageKey) {
		for(AssessmentMessageWithReadFlag message:allMessagesList) {
			if(messageKey.equals(message.getMessage().getKey())) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("read".equals(link.getCmd()) && link.getUserObject() instanceof AssessmentMessageDisplayRow) {
				markAsRead((AssessmentMessageDisplayRow)link.getUserObject());
				return;
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void markAsRead(AssessmentMessageDisplayRow message) {
		if(messagesList.remove(message)) {
			assessmentMessageService.markAsRead(message.getMessage(), getIdentity());
		}
		flc.setDirty(true);
	}
}