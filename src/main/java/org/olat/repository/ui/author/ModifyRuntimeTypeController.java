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
package org.olat.repository.ui.author;

import java.util.List;
import java.util.Set;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRuntimeType;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.repository.controllers.EntryChangedEvent.Change;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.Offer;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25 janv. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ModifyRuntimeTypeController extends FormBasicController {

	private SingleSelection runtimeTypeEl;
	
	private final List<RepositoryEntry> entries;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ACService acService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;

	public ModifyRuntimeTypeController(UserRequest ureq, WindowControl wControl, List<RepositoryEntry> entries) {
		super(ureq, wControl, "editruntimetype");
		setTranslator(Util.createPackageTranslator(RepositoryService.class, ureq.getLocale(), getTranslator()));
		this.entries = entries;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_edit_runtime_type_form");
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			layoutCont.contextPut("r_info", translate("change.runtime.type.info"));
			layoutCont.contextPut("r_info_help_url", "manual_user/learningresources/Access_configuration/");
		}

		Set<RepositoryEntryRuntimeType> runtimeTypes = repositoryService.getPossibleRuntimeTypes(entries);
		if (allEntriesHaveTheSameRuntimeType()) {
			runtimeTypes.add(entries.get(0).getRuntimeType());
		}

		SelectionValues runtimeTypeKV = new SelectionValues();
		if (runtimeTypes.contains(RepositoryEntryRuntimeType.embedded)) {
			runtimeTypeKV.add(SelectionValues.entry(RepositoryEntryRuntimeType.embedded.name(),
					translate("runtime.type." + RepositoryEntryRuntimeType.embedded.name() + ".title"),
					translate("runtime.type." + RepositoryEntryRuntimeType.embedded.name() + ".desc"), "o_icon o_icon_link", null, true));
		}
		if (runtimeTypes.contains(RepositoryEntryRuntimeType.standalone)) {
			runtimeTypeKV.add(SelectionValues.entry(RepositoryEntryRuntimeType.standalone.name(),
					translate("runtime.type." + RepositoryEntryRuntimeType.standalone.name() + ".title"),
					translate("runtime.type." + RepositoryEntryRuntimeType.standalone.name() + ".desc"), "o_icon o_icon_people", null, true));
		}
		if (runtimeTypes.contains(RepositoryEntryRuntimeType.curricular)) {
			runtimeTypeKV.add(SelectionValues.entry(RepositoryEntryRuntimeType.curricular.name(),
					translate("runtime.type." + RepositoryEntryRuntimeType.curricular.name() + ".title"),
					translate("runtime.type." + RepositoryEntryRuntimeType.curricular.name() + ".desc"), "o_icon o_icon_curriculum", null, false));
		}

		if (runtimeTypes.contains(RepositoryEntryRuntimeType.template)) {
			runtimeTypeKV.add(SelectionValues.entry(RepositoryEntryRuntimeType.template.name(),
					translate("runtime.type." + RepositoryEntryRuntimeType.template.name() + ".title"),
					translate("runtime.type." + RepositoryEntryRuntimeType.template.name() + ".desc"), "o_icon o_icon_template", null, false));
		}

		runtimeTypeEl = uifactory.addCardSingleSelectHorizontal("cif.runtime.type", "cif.runtime.type", formLayout, runtimeTypeKV);
		if (allEntriesHaveTheSameRuntimeType()) {
			runtimeTypeEl.select(entries.get(0).getRuntimeType().name(), true);
		}
		runtimeTypeEl.addActionListener(FormEvent.ONCHANGE);

		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("change", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	private boolean allEntriesHaveTheSameRuntimeType() {
		if (entries == null || entries.isEmpty()) {
			return false;
		}
		if (entries.size() == 1) {
			return true;
		}
		for (int i = 1; i < entries.size(); i++) {
			RepositoryEntryRuntimeType lastType = entries.get(i - 1).getRuntimeType();
			RepositoryEntryRuntimeType currentType = entries.get(i).getRuntimeType();
			if (currentType != lastType) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		runtimeTypeEl.clearError();
		if(!runtimeTypeEl.isOneSelected()) {
			runtimeTypeEl.setErrorKey("form.legende.mandatory");
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
		RepositoryEntryRuntimeType runtimeType = RepositoryEntryRuntimeType.valueOf(runtimeTypeEl.getSelectedKey());
		int updated = 0;
		for(RepositoryEntry entry:entries) {
			if(doChangeRuntimeType(ureq, entry, runtimeType)) {
				updated++;
			}
		}
		fireEvent(ureq, Event.DONE_EVENT);
		
		if(updated == 0) {
			showWarning("warning.runtime.type.no.update");
		} else if(updated < entries.size()) {
			showWarning("warning.runtime.type.some.no.update");
		} else {
			showInfo("warning.runtime.type.updated");
		}	
	}
	
	private boolean doChangeRuntimeType(UserRequest ureq, RepositoryEntry entry, RepositoryEntryRuntimeType runtimeType) {
		entry = repositoryService.loadByKey(entry.getKey());
		if (entry.getRuntimeType() == runtimeType) {
			return false;
		}
		if (repositoryService.hasUserManaged(entry) || 
				!RepositoryService.RuntimeTypeCheckDetails.ok.equals(repositoryService.canSwitchTo(entry, runtimeType))) {
			return false;
		}
		
		RepositoryEntry reloadedEntry = repositoryManager.setRuntimeType(entry, runtimeType);
		
		// Embedded hasn't any offers
		if(runtimeType == RepositoryEntryRuntimeType.embedded) {
			List<Offer> deletedOfferList = acService.findOfferByResource(reloadedEntry.getOlatResource(), true, null, null);
			for(Offer offerToDelete:deletedOfferList) {
				acService.deleteOffer(offerToDelete);
			}
		}
		dbInstance.commit();
		
		EntryChangedEvent e = new EntryChangedEvent(reloadedEntry, getIdentity(), Change.modifiedAccess, "authoring");
		ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(e, RepositoryService.REPOSITORY_EVENT_ORES);
		
		getLogger().info("Change runtime type of {} to {}", reloadedEntry, runtimeType);
		ThreadLocalUserActivityLogger.log(RepositoryEntryRuntimeType.loggingAction(runtimeType), getClass(),
				LoggingResourceable.wrap(reloadedEntry, OlatResourceableType.genRepoEntry));
		return true;
	}
}
