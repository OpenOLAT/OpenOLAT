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
package org.olat.repository.ui.author;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.repository.controllers.EntryChangedEvent.Change;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 nov. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmCloseController extends FormBasicController {
	
	private FormLink readOnlyButton;
	private MultipleSelectionElement acknowledgeEl;
	private MultipleSelectionElement notificationEl;
	
	private final int numOfMembers;
	private final List<RepositoryEntry> rows;
	
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private RepositoryService repositoryService;
	
	public ConfirmCloseController(UserRequest ureq, WindowControl wControl, List<RepositoryEntry> rows) {
		super(ureq, wControl, "confirm_close", Util.createPackageTranslator(RepositoryService.class, ureq.getLocale()));

		numOfMembers = repositoryService.countMembers(rows, getIdentity());
		this.rows = rows;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layout = (FormLayoutContainer)formLayout;
			layout.contextPut("numOfMembers", Integer.toString(numOfMembers));

			FormLayoutContainer layoutCont = FormLayoutContainer.createDefaultFormLayout("confirm", getTranslator());
			formLayout.add("confirm", layoutCont);
			layoutCont.setRootForm(mainForm);
			
			StringBuilder message = new StringBuilder();
			for(RepositoryEntry row:rows) {
				if(message.length() > 0) message.append(", ");
				message.append(StringHelper.escapeHtml(row.getDisplayname()));
			}
			
			String[] acknowledge = new String[] { translate("details.read.only.acknowledge.msg") };
			acknowledgeEl = uifactory.addCheckboxesHorizontal("confirm", "details.read.only.acknowledge", layoutCont, new String[]{ "" },  acknowledge);

			String[] notifications = new String[] { translate("details.notifications.acknowledge.value") };
			notificationEl = uifactory.addCheckboxesHorizontal("notifications", "details.notifications.acknowledge", layoutCont, new String[]{ "" },  notifications);
			if(repositoryModule.isLifecycleNotificationByCloseDeleteEnabled()) {
				notificationEl.select("", true);
				notificationEl.setEnabled(ureq.getUserSession().getRoles().isSystemAdmin());
			}
			
			FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
			layoutCont.add(buttonsCont);
			uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
			readOnlyButton = uifactory.addFormLink("details.closed", buttonsCont, Link.BUTTON);
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		acknowledgeEl.clearError();
		if(!acknowledgeEl.isAtLeastSelected(1)) {
			acknowledgeEl.setErrorKey("details.read.only.acknowledge.error");
			allOk &= false;
		}

		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(readOnlyButton == source) {
			if(validateFormLogic(ureq)) {
				doCompleteReadOnlyMode(ureq);
				fireEvent(ureq, Event.DONE_EVENT);
			}
		}
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doCompleteReadOnlyMode(UserRequest ureq) {
		setReadOnlyEntries(ureq, rows);
		showInfo("info.entry.read.only");
	}
	
	private boolean setReadOnlyEntries(UserRequest ureq, List<RepositoryEntry> entries) {
		boolean allOk = true;
		for(RepositoryEntry entry:entries) {
			RepositoryEntry reloadedEntry = repositoryService.loadByKey(entry.getKey());
			if(reloadedEntry != null) {
				boolean sendNotifications = notificationEl.isAtLeastSelected(1);
				repositoryService.closeRepositoryEntry(reloadedEntry, getIdentity(), sendNotifications);
				ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_DEACTIVATE, getClass(),
						LoggingResourceable.wrap(reloadedEntry, OlatResourceableType.genRepoEntry));
				fireEvent(ureq, new EntryChangedEvent(reloadedEntry, getIdentity(), Change.closed, "closed"));
			}
		}
		return allOk;
	}
}