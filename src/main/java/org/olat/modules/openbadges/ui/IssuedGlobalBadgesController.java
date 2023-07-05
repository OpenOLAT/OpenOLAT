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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.Formatter;
import org.olat.modules.openbadges.BadgeAssertion;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.user.UserManager;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-06-05<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class IssuedGlobalBadgesController extends FormBasicController {

	private TableModel tableModel;
	private FlexiTableElement tableEl;
	private FormLink addLink;
	private CloseableModalController cmc;
	private IssuedGlobalBadgeController issuedGlobalBadgeCtrl;
	private DialogBoxController confirmRevokeCtrl;

	@Autowired
	private OpenBadgesManager openBadgesManager;
	@Autowired
	private UserManager userManager;

	protected IssuedGlobalBadgesController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "issued_global_badges");

		flc.contextPut("noGlobalBadgesAvailable", openBadgesManager.getNumberOfBadgeClasses(null) == 0);

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.name.getI18n(), Cols.name.ordinal()));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.recipient.getI18n(), Cols.recipient.ordinal()));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.awardedBy.getI18n(), Cols.awardedBy.ordinal()));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.issuedOn.getI18n(), Cols.issuedOn.ordinal()));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.status.getI18n(), Cols.status.ordinal()));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.revoke.getI18n(), Cols.revoke.ordinal()));

		tableModel = new TableModel(columnModel, userManager);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, getTranslator(), formLayout);
		addLink = uifactory.addFormLink("add", "issueGlobalBadge", "issueGlobalBadge", formLayout, Link.BUTTON);
		updateUI();
	}

	private void updateUI() {
		List<Row> rows = openBadgesManager
				.getBadgeAssertionsWithSizes(null, null)
				.stream()
				.map(baws -> {
					FormLink revokeLink = null;
					if (baws.badgeAssertion().getStatus() == BadgeAssertion.BadgeAssertionStatus.issued) {
						revokeLink = uifactory.addFormLink("revoke_" + baws.badgeAssertion().getKey(),
								"revoke", Cols.revoke.getI18n(), null, flc, Link.LINK);
						revokeLink.setUserObject(baws.badgeAssertion());
					}
					return new Row(baws, revokeLink);
				}).toList();
		tableModel.setObjects(rows);
		tableEl.reset();
	}

	private void cleanUp() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(issuedGlobalBadgeCtrl);
		cmc = null;
		issuedGlobalBadgeCtrl = null;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == issuedGlobalBadgeCtrl) {
			cmc.deactivate();
			cleanUp();
			updateUI();
		} else if (source == confirmRevokeCtrl) {
			if (DialogBoxUIFactory.isOkEvent(event)) {
				BadgeAssertion badgeAssertion = (BadgeAssertion) confirmRevokeCtrl.getUserObject();
				doRevoke(badgeAssertion);
				updateUI();
			}
		} else if (source == cmc) {
			cleanUp();
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == addLink) {
			doUpload(ureq);
		} else if (source instanceof FormLink formLink) {
			if ("revoke".equals(formLink.getCmd())) {
				BadgeAssertion badgeAssertion = (BadgeAssertion) formLink.getUserObject();
				doConfirmRevoke(ureq, badgeAssertion);
			}
		}
	}

	private void doUpload(UserRequest ureq) {
		issuedGlobalBadgeCtrl = new IssuedGlobalBadgeController(ureq, getWindowControl(), null);
		listenTo(issuedGlobalBadgeCtrl);

		String title = translate("issueGlobalBadge");
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				issuedGlobalBadgeCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}

	private void doConfirmRevoke(UserRequest ureq, BadgeAssertion badgeAssertion) {
		String recipientDisplayName = userManager.getUserDisplayName(badgeAssertion.getRecipient());
		String title = translate("confirm.revoke.issued.badge.title", recipientDisplayName);
		String text = translate("confirm.revoke.issued.badge", recipientDisplayName);
		confirmRevokeCtrl = activateOkCancelDialog(ureq, title, text, confirmRevokeCtrl);
		confirmRevokeCtrl.setUserObject(badgeAssertion);
	}

	private void doRevoke(BadgeAssertion badgeAssertion) {
		openBadgesManager.revokeBadgeAssertion(badgeAssertion.getKey());
		updateUI();
	}

	enum Cols {
		name("form.name"),
		recipient("form.recipient"),
		awardedBy("form.awarded.by"),
		issuedOn("form.issued.on"),
		status("form.status"),
		revoke("table.revoke");

		Cols(String i18n) {
			this.i18n = i18n;
		}

		private final String i18n;

		public String getI18n() {
			return i18n;
		}
	}

	record Row(OpenBadgesManager.BadgeAssertionWithSize badgeAssertionWithSize, FormLink revokeLink) {}

	private class TableModel extends DefaultFlexiTableDataModel<Row> {
		private final UserManager userManager;

		public TableModel(FlexiTableColumnModel columnModel, UserManager userManager) {
			super(columnModel);
			this.userManager = userManager;
		}

		@Override
		public Object getValueAt(int row, int col) {
			BadgeAssertion badgeAssertion = getObject(row).badgeAssertionWithSize.badgeAssertion();
			return switch (Cols.values()[col]) {
				case name -> badgeAssertion.getBadgeClass().getName();
				case recipient -> userManager.getUserDisplayName(badgeAssertion.getRecipient());
				case status -> translate("assertion.status." + badgeAssertion.getStatus().name());
				case awardedBy -> userManager.getUserDisplayName(badgeAssertion.getAwardedBy());
				case issuedOn -> Formatter.getInstance(getLocale()).formatDateAndTime(badgeAssertion.getIssuedOn());
				case revoke -> getObject(row).revokeLink();
			};
		}
	}
}
