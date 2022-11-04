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
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.repository.controllers.EntryChangedEvent.Change;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10 Sep 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ModifyStatusController extends FormBasicController {

	private SingleSelection statusEl;
	
	private final List<RepositoryEntry> entries;
	
	@Autowired
	private RepositoryManager repositoryManager;

	public ModifyStatusController(UserRequest ureq, WindowControl wControl, List<RepositoryEntry> entries) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, ureq.getLocale(), getTranslator()));
		this.entries = entries;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		SelectionValues statusSV = new SelectionValues();
		statusSV.add(SelectionValues.entry(RepositoryEntryStatusEnum.preparation.name(), translate("cif.status.preparation")));
		statusSV.add(SelectionValues.entry(RepositoryEntryStatusEnum.review.name(), translate("cif.status.review")));
		statusSV.add(SelectionValues.entry(RepositoryEntryStatusEnum.coachpublished.name(), translate("cif.status.coachpublished")));
		statusSV.add(SelectionValues.entry(RepositoryEntryStatusEnum.published.name(), translate("cif.status.published")));
		statusSV.add(SelectionValues.entry(RepositoryEntryStatusEnum.closed.name(), translate("cif.status.closed")));
		statusEl = uifactory.addDropdownSingleselect("status", "noTransOnlyParam", formLayout, statusSV.keys(), statusSV.values(), null);
		statusEl.setElementCssClass("o_sel_status");
		String label = entries.size() == 1
				? translate("tools.modify.status.entry", entries.get(0).getDisplayname())
				: translate("tools.modify.status.entries", String.valueOf(entries.size()));
		statusEl.setLabel("noTransOnlyParam", new String[] {label});
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		buttonsCont.setElementCssClass("o_button_group o_button_group_right o_button_group_bottom");
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("change", buttonsCont);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (statusEl.isOneSelected()) {
			RepositoryEntryStatusEnum status = RepositoryEntryStatusEnum.valueOf(statusEl.getSelectedKey());
			entries.forEach(entry -> doChangeStatus(ureq, entry, status));
		}
		
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void doChangeStatus(UserRequest ureq, RepositoryEntry entry, RepositoryEntryStatusEnum status) {
		RepositoryEntry reloadedEntry = repositoryManager.setStatus(entry, status);
		
		EntryChangedEvent e = new EntryChangedEvent(reloadedEntry, getIdentity(), Change.modifiedAccess, "authoring");
		ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(e, RepositoryService.REPOSITORY_EVENT_ORES);
		
		getLogger().info("Change status of {} to {}", reloadedEntry, status);
		ThreadLocalUserActivityLogger.log(RepositoryEntryStatusEnum.loggingAction(status), getClass(),
				LoggingResourceable.wrap(reloadedEntry, OlatResourceableType.genRepoEntry));
	}

}
