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
package org.olat.repository.ui.report;

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryService;

/**
 * 
 * Initial date: 30 juin 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class UsersMembershipsReportController extends FormBasicController {
	
	private DateChooser datesEl;
	private MultipleSelectionElement rolesEl;
	
	private CloseableModalController cmc;
	private ConfirmationController confirmationCtrl;
	
	public UsersMembershipsReportController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, Util.createPackageTranslator(RepositoryService.class, ureq.getLocale()));
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("report.memberships.title");
		
		datesEl = uifactory.addDateChooser("dates", "report.memberships.dates", null, formLayout);
		datesEl.setMandatory(true);
		datesEl.setSecondDate(true);
		datesEl.setSeparator("report.memberships.to");
		
		SelectionValues rolesPk = new SelectionValues();
		rolesPk.add(SelectionValues.entry(GroupRoles.participant.name(), translate("role.participant")));
		rolesPk.add(SelectionValues.entry(GroupRoles.coach.name(), translate("role.coach")));
		rolesPk.add(SelectionValues.entry(GroupRoles.owner.name(), translate("role.owner")));
		rolesEl = uifactory.addCheckboxesVertical("report.memberships.roles", formLayout, rolesPk.keys(), rolesPk.values(), null, 1);
		rolesEl.setMandatory(true);
		rolesEl.select(GroupRoles.participant.name(), true);
		
		uifactory.addFormSubmitButton("report.memberships.generate", formLayout);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		
		rolesEl.clearError();
		if(!rolesEl.isAtLeastSelected(1)) {
			rolesEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		datesEl.clearError();
		if(datesEl.getDate() == null || datesEl.getSecondDate() == null) {
			datesEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else {
			Date from = datesEl.getDate();
			Date to = datesEl.getSecondDate();
			if(DateUtils.countDays(from, to) > 365) {
				datesEl.setErrorKey("error.max.one.year");
				allOk &= false;
			}
		}

		return allOk;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (confirmationCtrl == source) {
			if (event == Event.DONE_EVENT) {
				doReport(ureq);
			}
			cmc.deactivate();
			cleanUp();
		} else if (cmc == source) {
			cmc.deactivate();
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmationCtrl);
		removeAsListenerAndDispose(cmc);
		confirmationCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doConfirm(ureq);
	}
	
	private void doConfirm(UserRequest ureq) {
		confirmationCtrl = new ConfirmationController(ureq, getWindowControl(), 
				translate("report.memberships.confirm.text"),
				translate("report.memberships.confirm.confirmation"),
				translate("select"));
		listenTo(confirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmationCtrl.getInitialComponent(),
				true, translate("report.memberships.confirm.title"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doReport(UserRequest ureq) {
		Date from = datesEl.getDate();
		Date to = datesEl.getSecondDate();
		List<GroupRoles> roles = rolesEl.getSelectedKeys().stream()
				.map(GroupRoles::valueOf)
				.toList();
		
		UsersMembershipsReport export = new UsersMembershipsReport(getReportLabel(from, to), from, to, roles, getTranslator());
		ureq.getDispatchResult().setResultingMediaResource(export);
	}
	
	private String getReportLabel(Date from, Date to) {
		String fromString = Formatter.formatDateFilesystemSave(from);
		String toString = Formatter.formatDateFilesystemSave(to);
		return translate("report.memberships.label", fromString, toString);
	}
}
