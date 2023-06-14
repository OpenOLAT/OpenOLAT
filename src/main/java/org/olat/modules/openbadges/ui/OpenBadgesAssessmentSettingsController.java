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
package org.olat.modules.openbadges.ui;

import java.util.Collection;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.openbadges.BadgeEntryConfiguration;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-06-12<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class OpenBadgesAssessmentSettingsController extends FormBasicController {

	private final RepositoryEntry entry;
	private final boolean editable;
	private FormToggle awardBadgesEl;
	private MultipleSelectionElement awardBadgesManuallyEl;
	private SelectionValues awardBadgesManuallyKV;
	private enum AwardBadgesManuallyGroup {
		courseOwners,
		coaches
	}

	private BadgeEntryConfiguration configuration;

	@Autowired
	private OpenBadgesManager openBadgesManager;

	public OpenBadgesAssessmentSettingsController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, boolean editable) {
		super(ureq, wControl);
		this.entry = entry;
		this.editable = editable;

		configuration = openBadgesManager.getConfiguration(entry);

		awardBadgesManuallyKV = new SelectionValues();
		awardBadgesManuallyKV.add(SelectionValues.entry(AwardBadgesManuallyGroup.courseOwners.name(), translate("award.badges.manually.for.courseowners")));
		awardBadgesManuallyKV.add(SelectionValues.entry(AwardBadgesManuallyGroup.coaches.name(), translate("award.badges.manually.for.coaches")));

		initForm(ureq);
		updateUi();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("assessment.settings.title");

		awardBadgesEl = uifactory.addToggleButton("award.badges", "award.badges", null,
				null, formLayout);
		awardBadgesEl.addActionListener(FormEvent.ONCLICK);
		if (configuration.isAwardEnabled()) {
			awardBadgesEl.toggleOn();
		} else {
			awardBadgesEl.toggleOff();
		}

		awardBadgesManuallyEl = uifactory.addCheckboxesVertical("award.badges.manually", formLayout,
				awardBadgesManuallyKV.keys(), awardBadgesManuallyKV.values(), 1);
		awardBadgesManuallyEl.select(AwardBadgesManuallyGroup.courseOwners.name(), configuration.isOwnerCanAward());
		awardBadgesManuallyEl.select(AwardBadgesManuallyGroup.coaches.name(), configuration.isCoachCanAward());

		if (editable) {
			FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
			buttonCont.setRootForm(mainForm);
			formLayout.add(buttonCont);
			uifactory.addFormSubmitButton("save", buttonCont);
		}
	}

	private void updateUi() {
		boolean managedEfficiencyStatement = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.efficencystatement);
		awardBadgesEl.setEnabled(editable && !managedEfficiencyStatement);
		awardBadgesManuallyEl.setEnabled(editable && !managedEfficiencyStatement);

		if (awardBadgesEl.isOn()) {
			awardBadgesManuallyEl.setVisible(true);
			awardBadgesManuallyEl.select(AwardBadgesManuallyGroup.courseOwners.name(), configuration.isOwnerCanAward());
			awardBadgesManuallyEl.select(AwardBadgesManuallyGroup.coaches.name(), configuration.isCoachCanAward());
		} else {
			awardBadgesManuallyEl.setVisible(false);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == awardBadgesEl) {
			updateUi();
		}

		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doUpdateConfiguration(ureq);
	}

	private void doUpdateConfiguration(UserRequest ureq) {
		configuration = openBadgesManager.getConfiguration(entry);

		if (awardBadgesEl.isOn()) {
			configuration.setAwardEnabled(true);
			Collection<String> selectedKeys = awardBadgesManuallyEl.getSelectedKeys();
			configuration.setOwnerCanAward(selectedKeys.contains(AwardBadgesManuallyGroup.courseOwners.name()));
			configuration.setCoachCanAward(selectedKeys.contains(AwardBadgesManuallyGroup.coaches.name()));
		} else {
			configuration.setAwardEnabled(false);
		}

		configuration = openBadgesManager.updateConfiguration(configuration);

		fireEvent(ureq, Event.CHANGED_EVENT);
	}
}
