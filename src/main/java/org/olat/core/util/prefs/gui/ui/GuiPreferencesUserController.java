/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.core.util.prefs.gui.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.util.prefs.gui.GuiPreference;
import org.olat.core.util.prefs.gui.GuiPreferenceService;
import org.olat.core.util.prefs.gui.ui.GuiPreferencesUserDataModel.GuiPrefUserCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Dez 01, 2023
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class GuiPreferencesUserController extends FormBasicController {

	private final Identity identity;

	private FormLink bulkResetLink;

	private FlexiTableElement tableEl;
	private GuiPreferencesUserDataModel model;

	private DialogBoxController delSingleEntryYesNoCtrl;
	private DialogBoxController delBulkEntriesYesNoCtrl;

	@Autowired
	private GuiPreferenceService guiPreferencesService;

	public GuiPreferencesUserController(UserRequest ureq, WindowControl wControl, Identity identity) {
		super(ureq, wControl, "guipref_view");
		this.identity = identity;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, GuiPrefUserCols.id));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, GuiPrefUserCols.creationDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, GuiPrefUserCols.lastModified));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GuiPrefUserCols.attributedClass));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GuiPrefUserCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, GuiPrefUserCols.value));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GuiPrefUserCols.reset));

		model = new GuiPreferencesUserDataModel(columnsModel);

		tableEl = uifactory.addTableElement(getWindowControl(), "entries", model, 20, false, getTranslator(), formLayout);
		tableEl.setAndLoadPersistedPreferences(ureq, "gui-prefs-user");
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);

		bulkResetLink = uifactory.addFormLink("bulk.reset.link", flc, Link.BUTTON);
		tableEl.addBatchButton(bulkResetLink);

		loadModelData();
	}

	private void loadModelData() {
		List<GuiPreferencesUserRow> rows = new ArrayList<>();
		List<GuiPreference> guiPrefs = guiPreferencesService.loadGuiPrefsByUniqueProperties(identity, null, null);

		for (GuiPreference guiPref : guiPrefs) {
			FormLink resetActionEl = uifactory.addFormLink("reset_" + guiPref.getKey(), "CMD_RESET", "table.header.g.reset", null, null, Link.BUTTON);
			resetActionEl.setUserObject(guiPref);
			rows.add(
					new GuiPreferencesUserRow(guiPref.getKey(), guiPref.getAttributedClass(), guiPref.getPrefKey(),
							guiPref.getPrefValue(), guiPref.getCreationDate(), guiPref.getLastModified(), resetActionEl)
			);
		}
		model.setObjects(rows);
		tableEl.reset();
	}

	private void bulkResetGuiPrefEntries() {
		List<GuiPreference> selectedGuiPreferences = new ArrayList<>();
		for (Integer i : tableEl.getMultiSelectedIndex()) {
			selectedGuiPreferences.addAll(guiPreferencesService.loadGuiPrefsByUniqueProperties(identity, model.getObject(i).attributedClass(), model.getObject(i).guiPrefKey()));
		}

		for (GuiPreference guiPreference : selectedGuiPreferences) {
			guiPreferencesService.deleteGuiPreference(guiPreference);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == bulkResetLink) {
			if (!tableEl.getMultiSelectedIndex().isEmpty()) {
				delBulkEntriesYesNoCtrl = activateYesNoDialog(ureq, null, translate("confirm.bulk.delete"), delBulkEntriesYesNoCtrl);
			} else {
				showWarning("table.no.selection");
			}
		} else if (source instanceof FormLink link) {
			GuiPreference row = (GuiPreference) link.getUserObject();
			if ("CMD_RESET".equalsIgnoreCase(link.getCmd()) && row != null) {
				delSingleEntryYesNoCtrl = activateYesNoDialog(ureq, null, translate("confirm.delete", row.getPrefKey()), delSingleEntryYesNoCtrl);
				delSingleEntryYesNoCtrl.setUserObject(row);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == delSingleEntryYesNoCtrl) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				GuiPreference row = (GuiPreference) delSingleEntryYesNoCtrl.getUserObject();
				guiPreferencesService.deleteGuiPrefsByUniqueProperties(row.getIdentity(), row.getAttributedClass(), row.getPrefKey());
				showInfo("info.gui.pref.deleted");
				loadModelData();
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			// cleanup dialog
			delSingleEntryYesNoCtrl.dispose();
			delSingleEntryYesNoCtrl = null;
		} else if (source == delBulkEntriesYesNoCtrl) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				bulkResetGuiPrefEntries();
				showInfo("info.gui.prefs.deleted");
				loadModelData();
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			// cleanup dialog
			delBulkEntriesYesNoCtrl.dispose();
			delBulkEntriesYesNoCtrl = null;
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// no need
	}
}
