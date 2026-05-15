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
package org.olat.repository.ui.settings;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryFinishedAccessOptions;
import org.olat.repository.RepositoryEntryRuntimeType;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 2026-05-15<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class RepositoryEntryFinishedAccessOptionsController extends FormBasicController {

	private static final String DEFAULT_KEY = "default";
	private static final String OVERRIDE_KEY = "override";

	private SingleSelection overrideEl;
	private SingleSelection finishedAccessEl;

	private RepositoryEntry entry;
	private final boolean readOnly;

	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private RepositoryManager repositoryManager;

	public RepositoryEntryFinishedAccessOptionsController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry entry, boolean readOnly) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, ureq.getLocale(), getTranslator()));
		this.entry = entry;
		this.readOnly = readOnly;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (entry.getRuntimeType() == RepositoryEntryRuntimeType.embedded) {
			return;
		}
		setFormTitle("entry.access.title");

		SelectionValues overridePk = new SelectionValues();
		String defaultLabel = translate("admin.access.finished." + repositoryModule.getFinishedAccessDefaultOption().name());
		overridePk.add(SelectionValues.entry(DEFAULT_KEY,
				translate("entry.access.finished.system.default", defaultLabel)));
		overridePk.add(SelectionValues.entry(OVERRIDE_KEY, translate("entry.access.finished.override")));

		overrideEl = uifactory.addRadiosHorizontal("entry.access.finished", "entry.access.finished",
				formLayout, overridePk.keys(), overridePk.values());
		overrideEl.addActionListener(FormEvent.ONCLICK);
		overrideEl.setEnabled(!readOnly);

		SelectionValues finishedPk = new SelectionValues();
		finishedPk.add(SelectionValues.entry(RepositoryEntryFinishedAccessOptions.readonly.name(),
				translate("admin.access.finished.readonly"),
				translate("admin.access.finished.readonly.descr"), null, null, true));
		finishedPk.add(SelectionValues.entry(RepositoryEntryFinishedAccessOptions.noaccess.name(),
				translate("admin.access.finished.noaccess"),
				translate("admin.access.finished.noaccess.descr"), null, null, true));
		finishedAccessEl = uifactory.addCardSingleSelectHorizontal("entry.access.finished.value", "",
				formLayout, finishedPk.keys(), finishedPk.values(), finishedPk.descriptions(), finishedPk.icons());
		finishedAccessEl.addActionListener(FormEvent.ONCLICK);
		finishedAccessEl.setEnabled(!readOnly);

		RepositoryEntryFinishedAccessOptions currentOption = entry.getFinishedAccess();
		if (currentOption == null) {
			overrideEl.select(DEFAULT_KEY, true);
			finishedAccessEl.select(repositoryModule.getFinishedAccessDefaultOption().name(), true);
		} else {
			overrideEl.select(OVERRIDE_KEY, true);
			finishedAccessEl.select(currentOption.name(), true);
		}
		updateUI();
	}

	private void updateUI() {
		finishedAccessEl.setVisible(overrideEl.isOneSelected() && OVERRIDE_KEY.equals(overrideEl.getSelectedKey()));
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (overrideEl == source || finishedAccessEl == source) {
			updateUI();
			doSave(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void doSave(UserRequest ureq) {
		if (readOnly) {
			return;
		}
		RepositoryEntryFinishedAccessOptions option = null;
		if (overrideEl.isOneSelected() && OVERRIDE_KEY.equals(overrideEl.getSelectedKey())
				&& finishedAccessEl.isOneSelected()) {
			try {
				option = RepositoryEntryFinishedAccessOptions.valueOf(finishedAccessEl.getSelectedKey());
			} catch (IllegalArgumentException e) {
				option = null;
			}
		}
		entry = repositoryManager.setFinishedAccess(entry, option);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

}
